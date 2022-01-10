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
import com.rodan.library.model.config.node.config.LabNodeConfig;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.error.ValidationException;
import com.rodan.library.util.Util;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextName;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPDialogMobility;

@Payload(name = Constants.RESTORE_DATA_PAYLOAD_NAME)
@ToString(callSuper = true)
public class RestoreDataPayloadWrapper extends JSs7PayloadWrapper<MapMobilityService, MAPDialogMobility> {
    @Getter(AccessLevel.PRIVATE) private String imsi;
    @Getter(AccessLevel.PRIVATE) private String mapVersion;
    @Getter(AccessLevel.PRIVATE) private String cc;
    @Getter(AccessLevel.PRIVATE) private String ndc;
    @Getter(AccessLevel.PRIVATE) private String mcc;
    @Getter(AccessLevel.PRIVATE) private String mnc;

    @Builder
    public RestoreDataPayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig,
                                     SccpAdapter sccpAdapter, MapAdapter mapAdapter,
                                     MapDialogGenerator<MAPDialogMobility> dialogGenerator, String imsi,
                                     String mapVersion, String cc, String ndc, String mcc, String mnc) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.imsi = imsi;
        this.mapVersion = mapVersion;
        this.cc = cc;
        this.ndc = ndc;
        this.mcc = mcc;
        this.mnc = mnc;
    }

    @Override
    public MAPDialogMobility generateCarrier() throws SystemException, ValidationException {
        validate();

        // TODO check if message can be sent with E.214 IMSI
        var sccpFactory = getSccpAdapter().getParamFactory();
        var callingGt = sccpFactory.createGlobalTitle(getLocalGt(),
                TRANSLATION_TYPE, ISDN_TELEPHONY_INDICATOR, ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var callingPc = Integer.valueOf(getNodeConfig().getSs7Association().getLocalNode().getPointCode());
        var callingParty = sccpFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, callingGt,
                callingPc, getLocalSsn());
        var calledGtStr = Util.generateE214Address(getImsi(), getMcc(), getMnc(),
                getCc(), getNdc());
        var calledGt = sccpFactory.createGlobalTitle(calledGtStr, TRANSLATION_TYPE, ISDN_MOBILE_INDICATOR,
                ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var peerNode = (getNodeConfig() instanceof IntruderNodeConfig) ?
                ((IntruderNodeConfig) getNodeConfig()).getSs7Association().getPeerNode() :
                ((LabNodeConfig) getNodeConfig()).getSs7Association().getPeerNode();
        var calledPc = Integer.valueOf(peerNode.getPointCode());
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

            // Ref: http://www.developingsolutions.com/MapDict/Dictionary/vlr-Capability.html
            var camelPhases = mapFactory.createSupportedCamelPhases(true, true, true, true);
            var supperChargerInfo = mapFactory.createSuperChargerInfo(true);
            var supportedLcsInfo = mapFactory.createSupportedLCSCapabilitySets(true,
                    true, true, true, true);
            var offeredCamel4CSIsInfo = mapFactory.createOfferedCamel4CSIs(true, true, true, true,
                    true, true, true);
            var vlrCapability = mapFactory.createVlrCapability(camelPhases, null, false,
                    null, supperChargerInfo, false, supportedLcsInfo, offeredCamel4CSIsInfo,
                    null, false, false);

            dialog.addRestoreDataRequest(imsi, null, vlrCapability, null, true);

        } catch (MAPException e) {
            logger.error("Failed to add restoreData to dialog", e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
        }
    }

    @Override
    protected MAPApplicationContext getApplicationContext() throws SystemException {
        Integer[] supportedVersions = {2, 3};
        var mapContextVersion = getAcVersion(Integer.parseInt(getMapVersion()), supportedVersions);
        return MAPApplicationContext.getInstance(MAPApplicationContextName.networkLocUpContext, mapContextVersion);
    }
}
