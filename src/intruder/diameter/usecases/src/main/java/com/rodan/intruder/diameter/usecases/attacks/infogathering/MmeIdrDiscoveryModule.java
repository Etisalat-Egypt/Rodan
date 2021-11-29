/*
 * Etisalat Egypt, Open Source
 * Copyright 2021, Etisalat Egypt and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

/**
 * @author Ayman ElSherif
 */

package com.rodan.intruder.diameter.usecases.attacks.infogathering;

import com.rodan.intruder.diameter.entities.event.S6aListener;
import com.rodan.intruder.diameter.entities.event.model.InsertSubscriberDataAnswer;
import com.rodan.intruder.diameter.entities.event.model.ResultCode;
import com.rodan.intruder.diameter.entities.payload.DiameterPayload;
import com.rodan.intruder.diameter.entities.payload.s6a.IdrPayload;
import com.rodan.intruder.diameter.usecases.attacks.DiameterBruteforceModuleTemplate;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleConstants;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleOptions;
import com.rodan.intruder.diameter.usecases.model.MmeDiscoveryInfo;
import com.rodan.intruder.diameter.usecases.model.infogathering.MmeIdrDiscoveryOptions;
import com.rodan.intruder.diameter.usecases.model.infogathering.MmeIdrDiscoveryResponse;
import com.rodan.intruder.diameter.usecases.port.DiameterGateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.kernel.usecases.model.LazyPayloadCollection;
import com.rodan.library.util.LongRunningTask;
import com.rodan.library.util.Util;
import lombok.Builder;
import lombok.SneakyThrows;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Module(name = DiameterModuleConstants.MME_IDR_DISCOVERY_NAME, category = DiameterModuleConstants.INFO_GATHERING_CATEGORY_DISPLAY_NAME,
        displayName = DiameterModuleConstants.MME_IDR_DISCOVERY_DISPLAY_NAME, brief = DiameterModuleConstants.MME_IDR_DISCOVERY_BRIEF,
        description = DiameterModuleConstants.MME_IDR_DISCOVERY_DESCRIPTION, rank = DiameterModuleConstants.MME_IDR_DISCOVERY_RANK,
        mainPayload = Constants.ULR_PAYLOAD_NAME)
public class MmeIdrDiscoveryModule extends DiameterBruteforceModuleTemplate implements SignalingModule, S6aListener {
    final static Logger logger = LogManager.getLogger(MmeIdrBruteforceModule.class);

    private List<MmeDiscoveryInfo> discoveredMmeHosts;

    @Builder
    public MmeIdrDiscoveryModule(DiameterGateway gateway, DiameterModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
        var options = (MmeIdrDiscoveryOptions) moduleOptions;
        if (!options.getDelayMillis().isBlank()) {
            setBruteforceDelay(Integer.valueOf(options.getDelayMillis()));
        }
        setTaskWaitTime(6000); // Increase wait time to wait for possible valid remote node replies
        this.discoveredMmeHosts = new ArrayList<>();
    }

    @SneakyThrows // TODO IMP: remove and handle/throw system exception
    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (MmeIdrDiscoveryOptions) moduleOptions;
        var filePath = Util.getWordListsDirectory() + File.separator + options.getTargetMmeFileName();

        var mmeHostStream = Util.loadWordListLazy(filePath);
        var mmeHostFileSize = Util.getFileSize(filePath);
        var imsi = Util.generateRandomImsi(options.getMcc(), options.getMnc());

        var lazyPayloadCollection = LazyPayloadCollection.<DiameterPayload>builder()
                .dataSource(mmeHostStream).totalDataSize(mmeHostFileSize)
                .payloadGenerator((host) ->
                        IdrPayload.builder()
                                .usage(IdrPayload.Usage.LOCATION)
                                .destinationRealm(options.getDestinationRealm())
                                .imsi(imsi).targetMmeHost(host)
                                .build()
                )
                .build();

        setPayloadIterator(lazyPayloadCollection);
        var dummyPayload = IdrPayload.builder()
                .usage(IdrPayload.Usage.LOCATION)
                .destinationRealm(options.getDestinationRealm())
                .imsi(imsi).targetMmeHost("host")
                .build();
        setMainPayload(dummyPayload);
        setCurrentPayload(getMainPayload());
        logger.debug("Payloads: " + lazyPayloadCollection);
    }

    @Override
    protected void addServiceListener() throws SystemException {
        logger.debug("Adding service listeners");
        getGateway().addS6aListener(this);
    }

    @Override
    protected void waitForResponse() throws SystemException {
        var responseWaitTask = LongRunningTask.builder()
                .workStartMessage("Waiting for server response...").workWaitMessage(null)
                .workDoneMessage("Response received successfully")
                .workFailedMessage(getWaitForResponseFailedMessage())
                .workDoneCheck(m -> isResultReceived() || isExecutionError())
                .startWorkAction(null)
                .waitTime(getTaskWaitTime()).checkInterval(getTaskCheckInterval())
                .throwExceptionOnFailure(false)
                .build();
        Util.startLongRunningTask(responseWaitTask);

        moduleResponse = MmeIdrDiscoveryResponse.builder()
                .discoveredMmeHosts(discoveredMmeHosts)
                .build();
        setResultReceived(true);
        logger.debug("IDR completed!");
    }

    @Override
    protected void cleanup() throws SystemException {
        if (this.getGateway() != null)
            this.getGateway().removeS6aListener(this);
    }

    @SneakyThrows
    @Override
    public void doInsertSubscriberDataAnswerEvent(InsertSubscriberDataAnswer answer) {
        logger.debug("[[[[ doInsertSubscriberDataAnswerEvent ]]]]");
        logger.debug(answer);

        var host = answer.getOriginHost();
        var notes = "";
        if(isOriginHostMismatch(answer))
            notes += "Origin-Host different from sent payload: " + answer.getOriginHost() + " | ";

        addDiscoveredMmeRecord(host, "IDA received", notes);
    }

    @Override
    public void onFailedResultCode(ResultCode resultCode) {
        logger.debug("[[[[[[[[[[    onFailedResultCode      ]]]]]]]]]]");
        var msg = String.format("resultCode: [%s]", resultCode);
        logger.debug(msg);

        if (isPossibleValidNodeResultCode(resultCode)) {
            var mmeHost = resultCode.getOriginHost();
            var reason = resultCode.getShortMessage();
            addDiscoveredMmeRecord(mmeHost, reason, "");
        }
    }

    private boolean isOriginHostMismatch(InsertSubscriberDataAnswer answer) throws SystemException {
        var correspondingPayload = (IdrPayload) getSentPayloads().get(answer.getSessionId());
        if (correspondingPayload == null) {
            var msg = "No corresponding payload found for session ID: " + answer.getSessionId();
            logger.error(msg);
            throw SystemException.builder().code(ErrorCode.MISSING_PAYLOAD).message(msg).build();
        }

        var correctOriginHost = correspondingPayload.getTargetMmeHost();
        return !correctOriginHost.equals(answer.getOriginHost());
    }

    private void addDiscoveredMmeRecord(String host, String reason, String notes) {
        notify("Discovered possible valid MME: " + host + "\tReason: " + reason, NotificationType.PROGRESS);
        var mmeInfo = new MmeDiscoveryInfo(host, reason, notes);
        discoveredMmeHosts.add(mmeInfo);
    }
}
