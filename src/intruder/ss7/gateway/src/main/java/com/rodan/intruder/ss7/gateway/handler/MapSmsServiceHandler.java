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

package com.rodan.intruder.ss7.gateway.handler;

import com.rodan.intruder.ss7.entities.event.model.ErrorEvent;
import com.rodan.intruder.ss7.entities.event.service.MapSmsServiceListener;
import com.rodan.intruder.ss7.gateway.handler.model.sms.*;
import com.rodan.library.util.Util;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.indicator.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.primitives.AddressString;
import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.map.api.service.sms.*;
import org.mobicents.protocols.ss7.map.api.smstpdu.SmsDeliverTpdu;
import org.mobicents.protocols.ss7.map.api.smstpdu.SmsTpduType;
import org.mobicents.protocols.ss7.map.service.sms.SM_RP_DAImpl;
import org.mobicents.protocols.ss7.map.service.sms.SM_RP_OAImpl;
import org.mobicents.protocols.ss7.map.service.sms.SmsSignalInfoImpl;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle0100;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapSmsServiceHandler extends MapServiceHandler implements MAPServiceSmsListener {
    Logger logger = LogManager.getLogger(MapSmsServiceHandler.class);

    private List<MapSmsServiceListener> listeners;

    public MapSmsServiceHandler() {
        listeners = new ArrayList<>();
    }

    public void addListener(MapSmsServiceListener listener) {
        logger.debug("Registering MAP listener: " + listener);
        if (listeners.contains(listener)) {
            logger.warn("Registering MapSmsServiceListener for already existing one");
            return;
        }

        listeners.add(listener);
        getBaseServiceListeners().add(listener);
    }

    public void removeListener(MapSmsServiceListener listener) {
        logger.debug("Removing MAP listener: " + listener);
        if (!listeners.contains(listener)) {
            logger.warn("Removing a non-existing MapSmsServiceListener");
            return;
        }

        listeners.remove(listener);
        getBaseServiceListeners().remove(listener);
    }

    // for all service handlers
    @Override
    public void onForwardShortMessageRequest(ForwardShortMessageRequest request) {
        try {
            logger.debug("[[[[[[[[[[    onForwardShortMessageRequest      ]]]]]]]]]]");
            logger.debug(request);
            // ForwardSM is used in MAP v1 & 2 for both MO & MT SMSs,
            // while a separate MTForwardSM & MOForwardSM are used in MAP v3
            logger.debug("##### Received ForwardSM request!");
            logger.debug(request);
            var dialog = request.getMAPDialog();
            var invokeId = request.getInvokeId();
            dialog.setUserObject(invokeId);

            var message = parseSm(request.getSM_RP_OA(), request.getSM_RP_DA(), request.getSM_RP_UI(),
                    request.getMoreMessagesToSend());
            message = FsmRequestImpl.builder()
                    .invokeId(invokeId).mapDialog(dialog)
                    .senderNode(message.getSenderNode()).sender(message.getSender()).receiver(message.getReceiver())
                    .timeStamp(message.getTimeStamp()).body(message.getBody()).originalRequest(request)
                    .build();
            for (var listener : listeners) {
                listener.onForwardShortMessageRequest(message);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(request.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onForwardShortMessageResponse(ForwardShortMessageResponse response) {
        logger.debug("[[[[[[[[[[    onForwardShortMessageResponse      ]]]]]]]]]]");
        logger.debug(response);
        var content = FsmResponseImpl.builder()
                .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog())
                .build();
        for (var listener : listeners) {
            listener.onForwardShortMessageResponse(content);
        }
    }

    @Override
    public void onMoForwardShortMessageRequest(MoForwardShortMessageRequest request) {
        logger.debug("[[[[[[[[[[    onMoForwardShortMessageRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onMoForwardShortMessageResponse(MoForwardShortMessageResponse response) {
        logger.debug("[[[[[[[[[[    onMoForwardShortMessageResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    @Override
    public void onMtForwardShortMessageRequest(MtForwardShortMessageRequest request) {
        try {
            logger.debug("[[[[[[[[[[    onMtForwardShortMessageRequest      ]]]]]]]]]]");
            logger.debug(request);
            // ForwardSM is used in MAP v1 & 2 for both MO & MT SMSs,
            // while a separate MTForwardSM & MOForwardSM are used in MAP v3
            logger.debug("##### Received MTForwardSM request!");
            logger.debug(request);
            var dialog = request.getMAPDialog();
            var invokeId = request.getInvokeId();
            dialog.setUserObject(invokeId);
            var message = parseSm(request.getSM_RP_OA(), request.getSM_RP_DA(), request.getSM_RP_UI(),
                    request.getMoreMessagesToSend());
            message = FsmRequestImpl.builder()
                    .senderNode(message.getSenderNode()).sender(message.getSender()).receiver(message.getReceiver())
                    .timeStamp(message.getTimeStamp()).body(message.getBody()).originalMtRequest(request)
                    .build();
            for (var listener : listeners) {
                listener.onMtForwardShortMessageRequest(message);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(request.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }

    }

    @Override
    public void onMtForwardShortMessageResponse(MtForwardShortMessageResponse response) {
        logger.debug("[[[[[[[[[[    onMtForwardShortMessageResponse      ]]]]]]]]]]");
        logger.debug(response);
        var content = FsmResponseImpl.builder()
                .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog())
                .build();
        for (var listener : listeners) {
            listener.onMtForwardShortMessageResponse(content);
        }
    }

    @Override
    public void onSendRoutingInfoForSMRequest(SendRoutingInfoForSMRequest request) {
        try {
            logger.debug("[[[[[[[[[[    onSendRoutingInfoForSMRequest      ]]]]]]]]]]");
            logger.debug(request);
            var e214NumberingPlanDetected = false;
            var cdPa = request.getMAPDialog().getLocalAddress().getGlobalTitle();
            logger.debug("CdPA: " + cdPa);
            if (cdPa instanceof GlobalTitle0100 address) {
                logger.debug("CdPA Numbering Plan: " + address.getNumberingPlan());
                if (address.getNumberingPlan().equals(NumberingPlan.ISDN_MOBILE)) {
                    e214NumberingPlanDetected = true;
                }
            }
            var msisdn = Util.getValueOrElse(request.getMsisdn(), ISDNAddressString::getAddress, "");
            var serviceCentreAddress = Util.getValueOrElse(request.getServiceCentreAddress(), AddressString::getAddress, "");
            var content = SriSmRequestImpl.builder()
                    .invokeId(request.getInvokeId()).mapDialog(request.getMAPDialog())
                    .msisdn(msisdn).serviceCentreAddress(serviceCentreAddress)
                    .e214NumberingPlanDetected(e214NumberingPlanDetected)
                    .build();
            for (var listener : listeners) {
                listener.onSendRoutingInfoForSMRequest(content);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP response: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(request.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onSendRoutingInfoForSMResponse(SendRoutingInfoForSMResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onSendRoutingInfoForSMResponse      ]]]]]]]]]]");
            logger.debug(response);
            var imsi = Util.getValueOrElse(response.getIMSI(), IMSI::getData, "");
            var vmsc = Util.getValueOrElse(response.getLocationInfoWithLMSI().getNetworkNodeNumber(), AddressString::getAddress, "");
            var hlr = Util.getValueOrElse(response.getMAPDialog().getRemoteAddress().getGlobalTitle(), GlobalTitle::getDigits, "");
            var content = SriSmResponseImpl.builder()
                    .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog())
                    .imsi(imsi).hlrGt(hlr).vmsc(vmsc)
                    .build();
            for (var listener : listeners) {
                listener.onSendRoutingInfoForSMResponse(content);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP response: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onReportSMDeliveryStatusRequest(ReportSMDeliveryStatusRequest request) {
        logger.debug("[[[[[[[[[[    onReportSMDeliveryStatusRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onReportSMDeliveryStatusResponse(ReportSMDeliveryStatusResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onReportSMDeliveryStatusResponse      ]]]]]]]]]]");
            logger.debug(response);
            var hlrGt = Util.getValueOrElse(response.getMAPDialog().getRemoteAddress().getGlobalTitle(), GlobalTitle::getDigits, "");
            var content = ReportSmDeliveryStatusResponseImpl.builder()
                    .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog())
                    .hlrGt(hlrGt)
                    .build();
            for (var listener : listeners) {
                listener.onReportSMDeliveryStatusResponse(content);
            }
        } catch (Exception e) {
            var msg = "Failed to parse MAP response: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onInformServiceCentreRequest(InformServiceCentreRequest request) {
        logger.debug("[[[[[[[[[[    onInformServiceCentreRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onAlertServiceCentreRequest(AlertServiceCentreRequest request) {
        logger.debug("[[[[[[[[[[    onAlertServiceCentreRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onAlertServiceCentreResponse(AlertServiceCentreResponse response) {
        logger.debug("[[[[[[[[[[    onAlertServiceCentreResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    @Override
    public void onReadyForSMRequest(ReadyForSMRequest request) {
        logger.debug("[[[[[[[[[[    onReadyForSMRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onReadyForSMResponse(ReadyForSMResponse response) {
        logger.debug("[[[[[[[[[[    onReadyForSMResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    @Override
    public void onNoteSubscriberPresentRequest(NoteSubscriberPresentRequest request) {
        logger.debug("[[[[[[[[[[    onNoteSubscriberPresentRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    private FsmRequestImpl parseSm(SM_RP_OA originatingAddress, SM_RP_DA destinationAddress,
                                   SmsSignalInfo signalingInfo, boolean hasMoreMessages) throws MAPException {
        originatingAddress = Objects.requireNonNullElse(originatingAddress, new SM_RP_OAImpl());
        destinationAddress = Objects.requireNonNullElse(destinationAddress, new SM_RP_DAImpl());
        signalingInfo = Objects.requireNonNullElse(signalingInfo, new SmsSignalInfoImpl());

        var senderNode = "";
        if (originatingAddress.getMsisdn() != null) {
            senderNode = "MSISDN(" + originatingAddress.getMsisdn().getAddress() + ")";
        } else if (originatingAddress.getServiceCentreAddressOA() != null) {
            senderNode = "SMSC(" + originatingAddress.getServiceCentreAddressOA().getAddress() + ")";
        }

        var receiver = "";
        if (destinationAddress.getIMSI() != null) {
            receiver = "IMSI: " + destinationAddress.getIMSI().getData();
        } else if (destinationAddress.getLMSI() != null) {
            receiver = "LMSI: " + Util.encodeHexString(destinationAddress.getLMSI().getData());
        } else if (destinationAddress.getServiceCentreAddressDA() != null) {
            receiver = "SMSC: " + destinationAddress.getServiceCentreAddressDA().getAddress();
        }


        var sender = "";
        var msgBody = "";
        var timeStampStr = "";
        var tpdu = signalingInfo.decodeTpdu(false);
        var smsType = tpdu.getSmsTpduType();
        logger.debug("moreMessages: " + hasMoreMessages); // TODO IMP TRX: handle moreMessages
        logger.debug("SM Type: " + smsType);
        if (smsType == SmsTpduType.SMS_DELIVER) {
            var smsDeliverTpdu = (SmsDeliverTpdu) tpdu;
            sender = smsDeliverTpdu.getOriginatingAddress().getAddressValue();
            var timeStamp = smsDeliverTpdu.getServiceCentreTimeStamp();
            timeStampStr = String.format("%d:%d:%d %d/%d/%d", timeStamp.getHour(), timeStamp.getMinute(),
                    timeStamp.getSecond(), timeStamp.getDay(), timeStamp.getMonth(), timeStamp.getYear());
            smsDeliverTpdu.getUserData().decode();
            msgBody = smsDeliverTpdu.getUserData().getDecodedMessage();
        }

        var message = FsmRequestImpl.builder()
                .senderNode(senderNode).sender(sender).receiver(receiver).timeStamp(timeStampStr).body(msgBody)
                .hasMoreMessages(hasMoreMessages)
                .build();

        return message;
    }
}
