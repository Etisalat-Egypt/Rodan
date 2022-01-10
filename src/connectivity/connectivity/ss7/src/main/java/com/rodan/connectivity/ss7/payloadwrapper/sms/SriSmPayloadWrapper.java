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
import com.rodan.library.model.config.node.config.LabNodeConfig;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.error.ValidationException;
import com.rodan.library.util.Util;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextName;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.service.sms.MAPDialogSms;
import org.mobicents.protocols.ss7.map.api.service.sms.SMDeliveryNotIntended;

@Payload(name = Constants.SRI_SM_PAYLOAD_NAME)
public class SriSmPayloadWrapper extends JSs7PayloadWrapper<MapSmsService, MAPDialogSms> {
    @Getter(AccessLevel.PRIVATE) private String msisdn;
    @Getter(AccessLevel.PRIVATE) private String imsi;
    @Getter(AccessLevel.PRIVATE) private String targetHlrGt;
    @Getter(AccessLevel.PRIVATE) private String smscGt;
    @Getter(AccessLevel.PRIVATE) private String detectSmsHomeRouting;
    @Getter(AccessLevel.PRIVATE) private String bypassSmsHomeRouting;
    @Getter(AccessLevel.PRIVATE) private String abuseOpcodeTag;
    @Getter(AccessLevel.PRIVATE) private String malformedAcn;
    @Getter(AccessLevel.PRIVATE) private String mapVersion;
    @Getter(AccessLevel.PRIVATE) private String cc;
    @Getter(AccessLevel.PRIVATE) private String ndc;
    @Getter(AccessLevel.PRIVATE) private String mcc;
    @Getter(AccessLevel.PRIVATE) private String mnc;

    @Builder
    public SriSmPayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig, 
                               SccpAdapter sccpAdapter, MapAdapter mapAdapter, 
                               MapDialogGenerator<MAPDialogSms> dialogGenerator, String msisdn, String imsi, 
                               String targetHlrGt, String smscGt, String detectSmsHomeRouting, 
                               String bypassSmsHomeRouting, String abuseOpcodeTag, String malformedAcn, 
                               String mapVersion, String cc, String ndc, String mcc, String mnc) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.msisdn = msisdn;
        this.imsi = imsi;
        this.targetHlrGt = targetHlrGt;
        this.smscGt = smscGt;
        this.detectSmsHomeRouting = detectSmsHomeRouting;
        this.bypassSmsHomeRouting = bypassSmsHomeRouting;
        this.abuseOpcodeTag = abuseOpcodeTag;
        this.malformedAcn = malformedAcn;
        this.mapVersion = mapVersion;
        this.cc = cc;
        this.ndc = ndc;
        this.mcc = mcc;
        this.mnc = mnc;
    }

    @Override
    public MAPDialogSms generateCarrier() throws SystemException, ValidationException {
        validate();

        var sccpFactory = getSccpAdapter().getParamFactory();
        var callingGt = sccpFactory.createGlobalTitle(getLocalGt(),
                TRANSLATION_TYPE, ISDN_TELEPHONY_INDICATOR, ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var callingPc = Integer.valueOf(getNodeConfig().getSs7Association().getLocalNode().getPointCode());
        var callingParty = sccpFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, callingGt,
                        callingPc, getLocalSsn());

        var calledGtStr = "";
        var bypassSmsHomeRouting = "Yes".equalsIgnoreCase(getBypassSmsHomeRouting());
        if (bypassSmsHomeRouting) {
            calledGtStr = Util.generateE214Address(getImsi(), getMcc(), getMnc(),
                    getCc(), getNdc());

        } else {
            calledGtStr = StringUtils.isBlank(getTargetHlrGt()) ? getMsisdn() :
                    getTargetHlrGt();
        }

        var numberingPlan = bypassSmsHomeRouting ? ISDN_MOBILE_INDICATOR : ISDN_TELEPHONY_INDICATOR;
        var calledGt = sccpFactory.createGlobalTitle(calledGtStr, TRANSLATION_TYPE, numberingPlan, ENCODING_SCHEME,
                NATURE_OF_ADDRESS);
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

            var msisdn = getMapAdapter().getParamFactory()
                    .createISDNAddressString(AddressNature.international_number,
                            org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN, getMsisdn());
            var smscAddress = StringUtils.isBlank(getSmscGt())? getLocalGt() : getSmscGt();
            var serviceCentreAddress = getMapAdapter().getParamFactory()
                    .createISDNAddressString(AddressNature.international_number,
                            org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN, smscAddress);

            var delivery = SMDeliveryNotIntended.onlyIMSIRequested;
            delivery = null; // TODO test with onlyIMSIRequested
            dialog.addSendRoutingInfoForSMRequest(msisdn, true, serviceCentreAddress, null,
                    false, null, null, delivery,
                    false, null, false, false, null);

        } catch (MAPException e) {
            logger.error("Failed to add SRI_SM to dialog", e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
        }
    }

    @Override
    protected MAPApplicationContext getApplicationContext() throws SystemException {
        Integer[] supportedVersions = {1, 2, 3};
        var mapContextVersion = getAcVersion(Integer.parseInt(getMapVersion()), supportedVersions);
        return MAPApplicationContext.getInstance(MAPApplicationContextName.shortMsgGatewayContext, mapContextVersion);
    }
}
