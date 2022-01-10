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

@Payload(name = Constants.SEND_IDENTIFICATION_PAYLOAD_NAME)
@ToString(callSuper = true)
public class SendIdentificationPayloadWrapper extends JSs7PayloadWrapper<MapMobilityService, MAPDialogMobility> {
    @Getter(AccessLevel.PRIVATE) private String tmsi;
    @Getter(AccessLevel.PRIVATE) private String targetVlrGt;
    @Getter(AccessLevel.PRIVATE) private String avNumber;
    @Getter(AccessLevel.PRIVATE) private String mapVersion;

    @Builder
    public SendIdentificationPayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig,
                                            SccpAdapter sccpAdapter, MapAdapter mapAdapter,
                                            MapDialogGenerator<MAPDialogMobility> dialogGenerator, String tmsi,
                                            String targetVlrGt, String avNumber, String mapVersion) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.tmsi = tmsi;
        this.targetVlrGt = targetVlrGt;
        this.avNumber = avNumber;
        this.mapVersion = mapVersion;
    }

    @Override
    public MAPDialogMobility generateCarrier() throws SystemException {
        validate();

        var paramFactory = getSccpAdapter().getParamFactory();
        var callingGt = paramFactory.createGlobalTitle(getLocalGt(),
                TRANSLATION_TYPE, ISDN_TELEPHONY_INDICATOR, ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var localPc = Integer.valueOf(getNodeConfig().getSs7Association().getLocalNode().getPointCode());
        var callingParty = paramFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, callingGt,
                localPc, getLocalSsn());
        var calledGt = paramFactory.createGlobalTitle(getTargetVlrGt(), TRANSLATION_TYPE,
                ISDN_TELEPHONY_INDICATOR, ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var peerNode = (getNodeConfig() instanceof IntruderNodeConfig) ?
                ((IntruderNodeConfig) getNodeConfig()).getSs7Association().getPeerNode() :
                ((LabNodeConfig) getNodeConfig()).getSs7Association().getPeerNode();
        var calledPc = Integer.valueOf(peerNode.getPointCode());
        var calledParty = paramFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, calledGt,
                calledPc, getRemoteSsn());

        return dialogGenerator.generate(callingParty, calledParty, getApplicationContext());
    }

    @Override
    public void addToCarrier(MAPDialogMobility dialog) throws SystemException {
        try {
            validate();

            var mapParamFactory = getMapAdapter().getParamFactory();
            var tmsiBytes = Util.decodeHexString(getTmsi());
            var tmsi = mapParamFactory.createTMSI(tmsiBytes);
            var avNumber = Integer.parseInt(getAvNumber());

            // newVLRNumber is required if mtRoamingForwardingSupported is set to true
            dialog.addSendIdentificationRequest(tmsi, avNumber, true, null, null,
                    null, null, false, null, null);

        } catch (MAPException e) {
            logger.error("Failed to add SID to dialog", e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
        }
    }

    @Override
    protected MAPApplicationContext getApplicationContext() throws SystemException {
        Integer[] supportedVersions = {2, 3};
        var mapContextVersion = getAcVersion(Integer.parseInt(getMapVersion()), supportedVersions);
        return MAPApplicationContext.getInstance(MAPApplicationContextName.interVlrInfoRetrievalContext, mapContextVersion);
    }
}
