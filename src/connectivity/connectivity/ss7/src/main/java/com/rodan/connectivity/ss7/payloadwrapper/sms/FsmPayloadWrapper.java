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

package com.rodan.connectivity.ss7.payloadwrapper.sms;

import com.rodan.connectivity.ss7.adapter.MapAdapter;
import com.rodan.connectivity.ss7.adapter.SccpAdapter;
import com.rodan.connectivity.ss7.payloadwrapper.JSs7PayloadWrapper;
import com.rodan.connectivity.ss7.service.MapDialogGenerator;
import com.rodan.connectivity.ss7.service.MapSmsService;
import com.rodan.library.model.Constants;
import com.rodan.library.model.Validator;
import com.rodan.library.model.annotation.Payload;
import com.rodan.library.model.config.node.config.IntruderNodeConfig;
import com.rodan.library.model.config.node.config.LabNodeConfig;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextName;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.service.sms.MAPDialogSms;
import org.mobicents.protocols.ss7.map.api.smstpdu.NumberingPlanIdentification;
import org.mobicents.protocols.ss7.map.api.smstpdu.TypeOfNumber;

import java.time.LocalDateTime;

@Payload(name = Constants.FSM_PAYLOAD_NAME)
public class FsmPayloadWrapper extends JSs7PayloadWrapper<MapSmsService, MAPDialogSms> {
    @Getter(AccessLevel.PRIVATE) private String imsi;
    @Getter(AccessLevel.PRIVATE) private String sender;
    @Getter(AccessLevel.PRIVATE) private String targetMscGt;
    @Getter(AccessLevel.PRIVATE) private String content;
    @Getter(AccessLevel.PRIVATE) private String messageType;
    @Getter(AccessLevel.PRIVATE) private String spoofSmsc;
    @Getter(AccessLevel.PRIVATE) private String smscGt;
    @Getter(AccessLevel.PRIVATE) private String mapVersion;

    @Builder
    public FsmPayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig, SccpAdapter sccpAdapter, 
                             MapAdapter mapAdapter, MapDialogGenerator<MAPDialogSms> dialogGenerator, String imsi, 
                             String sender, String targetMscGt, String content, String messageType, String spoofSmsc,
                             String smscGt, String mapVersion) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.imsi = imsi;
        this.sender = sender;
        this.targetMscGt = targetMscGt;
        this.content = content;
        this.messageType = messageType;
        this.spoofSmsc = spoofSmsc;
        this.smscGt = smscGt;
        this.mapVersion = mapVersion;
    }

    @Override
    public MAPDialogSms generateCarrier() throws SystemException {
        validate();

        var sccpFactory = getSccpAdapter().getParamFactory();
        var spoofSmsc = "Yes".equalsIgnoreCase(getSpoofSmsc());
        var callingStr = spoofSmsc ? getSmscGt() : getLocalGt();
        var callingGt = sccpFactory.createGlobalTitle(callingStr,
                TRANSLATION_TYPE, ISDN_TELEPHONY_INDICATOR, ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var callingPc = Integer.valueOf(getNodeConfig().getSs7Association().getLocalNode().getPointCode());
        var callingParty = sccpFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, callingGt,
                        callingPc, getLocalSsn());
        var calledGt = sccpFactory.createGlobalTitle(getTargetMscGt(),
                TRANSLATION_TYPE, ISDN_TELEPHONY_INDICATOR, ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var peerNode = (getNodeConfig() instanceof IntruderNodeConfig) ?
                ((IntruderNodeConfig) getNodeConfig()).getSs7Association().getPeerNode() :
                ((LabNodeConfig) getNodeConfig()).getSs7Association().getPeerNode();
        var calledPc = Integer.valueOf(peerNode.getPointCode());
        var calledParty = sccpFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, calledGt,
                        calledPc, getRemoteSsn());

        return dialogGenerator.generate(callingParty, calledParty, getApplicationContext());
    }

    @Override
    public void addToCarrier(MAPDialogSms dialog) throws SystemException {
        try {
            validate();

            var paramFactory = getMapAdapter().getParamFactory();
            var smsParamFactory = getMapAdapter().getSmsParamFactory();

            var imsi = paramFactory.createIMSI(getImsi());
            var da = paramFactory.createSM_RP_DA(imsi);
            var smscStr = StringUtils.isBlank(getSmscGt()) ? getLocalGt() : getSmscGt();
            var smsc = paramFactory.createAddressString(AddressNature.international_number, NumberingPlan.ISDN, smscStr);
            var oa = paramFactory.createSM_RP_OA_ServiceCentreAddressOA(smsc);
            var senderStr = getSender().replace("_", " ");
            var typeOfNumber = Validator.isValidMsisdn(senderStr) ? TypeOfNumber.InternationalNumber :
                    TypeOfNumber.Alphanumeric;
            var numberingPlanId = Validator.isValidMsisdn(senderStr) ?
                    NumberingPlanIdentification.ISDNTelephoneNumberingPlan : NumberingPlanIdentification.Unknown;
            var sender = smsParamFactory.createAddressField(typeOfNumber, numberingPlanId, senderStr);

            var pid = switch (getMessageType()) {
                case "silent" -> 0b01000000; // Silent/Type 0
                case "replace" -> 0b01000001; // Replace Type 1
                default -> 0b00000000; // Normal
            };
            var protocolId = smsParamFactory.createProtocolIdentifier(pid); // TP-Protocol-Identifier
            var currentTime = LocalDateTime.now();
            var timeZoneHours = 2; // Cairo time zone. TODO: make it a configuration
            var timeZone = timeZoneHours * 4; // 1 = 15 minutes
            var year = currentTime.minusYears(2000).getYear(); // Only 1st 2 digits are used
            var timestamp = smsParamFactory.createAbsoluteTimeStamp(year, currentTime.getMonthValue(),
                    currentTime.getDayOfMonth(), currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond(),
                    timeZone);

            var dcs = switch (getMessageType()) {
                case "flash" -> 0b00010000; // Class 0
                case "autodelete" -> 0b01000000; // Marked for Automatic Deletion
                default -> 0b00000000; // Normal
            };
            var coding = smsParamFactory.createDataCodingScheme(dcs);
            var contentStr = getContent().replace("_", " ");
            var userData = smsParamFactory.createUserData(contentStr, coding, null, null);
            var smst = smsParamFactory.createSmsDeliverTpdu(false, false, false,
                    true, sender, protocolId, timestamp, userData);
            var signalingInfo = paramFactory.createSmsSignalInfo(smst, null);
            dialog.addForwardShortMessageRequest(da, oa, signalingInfo, false);

        } catch (MAPException e) {
            logger.error("Failed to add ForwardSM to dialog", e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
        }
    }

    @Override
    protected MAPApplicationContext getApplicationContext() throws SystemException {
        Integer[] supportedVersions = {1, 2};
        var mapContextVersion = getAcVersion(Integer.parseInt(getMapVersion()), supportedVersions);
        return MAPApplicationContext.getInstance(MAPApplicationContextName.shortMsgMTRelayContext, mapContextVersion);
    }
}
