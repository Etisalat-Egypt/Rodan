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

package com.rodan.connectivity.ss7.payloadwrapper.callhandling;

import com.rodan.connectivity.ss7.adapter.MapAdapter;
import com.rodan.connectivity.ss7.adapter.SccpAdapter;
import com.rodan.connectivity.ss7.payloadwrapper.JSs7PayloadWrapper;
import com.rodan.connectivity.ss7.service.MapCallHandlingService;
import com.rodan.connectivity.ss7.service.MapDialogGenerator;
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
import org.apache.commons.lang3.StringUtils;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextName;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.service.callhandling.InterrogationType;
import org.mobicents.protocols.ss7.map.api.service.callhandling.MAPDialogCallHandling;

@Payload(name = Constants.SRI_PAYLOAD_NAME)
@ToString(callSuper = true)
public class SriPayloadWrapper extends JSs7PayloadWrapper<MapCallHandlingService, MAPDialogCallHandling> {
    @Getter(AccessLevel.PRIVATE) private String msisdn;
    @Getter(AccessLevel.PRIVATE) private String targetHlrGt;
    @Getter(AccessLevel.PRIVATE) private String mapVersion;

    @Builder
    public SriPayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig, SccpAdapter sccpAdapter,
                             MapAdapter mapAdapter, MapDialogGenerator<MAPDialogCallHandling> dialogGenerator,
                             String msisdn, String targetHlrGt, String mapVersion) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.msisdn = msisdn;
        this.targetHlrGt = targetHlrGt;
        this.mapVersion = mapVersion;
    }

    @Override
    public MAPDialogCallHandling generateCarrier() throws SystemException {
        validate();

        var sccpFactory = getSccpAdapter().getParamFactory();
        var callingGt = sccpFactory.createGlobalTitle(getLocalGt(),
                TRANSLATION_TYPE, ISDN_TELEPHONY_INDICATOR, ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var callingPc = Integer.valueOf(getNodeConfig().getSs7Association().getLocalNode().getPointCode());
        var callingParty = sccpFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, callingGt,
                callingPc, getLocalSsn());
        var calledGtStr = StringUtils.isBlank(getTargetHlrGt()) ? getMsisdn() : getTargetHlrGt();
        var calledGt = sccpFactory.createGlobalTitle(calledGtStr, TRANSLATION_TYPE, ISDN_TELEPHONY_INDICATOR,
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
    public void addToCarrier(MAPDialogCallHandling dialog) throws SystemException {
        try {
            validate();

            var mapFactory = getMapAdapter().getParamFactory();
            var msisdn = mapFactory.createISDNAddressString(AddressNature.international_number,
                    NumberingPlan.ISDN, getMsisdn());
            var gmsc = mapFactory.createISDNAddressString(AddressNature.international_number,
                    NumberingPlan.ISDN, getLocalGt());

            // TODO how to wait for Listeners ? threads & locks. Do it for all functions
            dialog.addSendRoutingInformationRequest(msisdn, null, null,
                    InterrogationType.basicCall, false, null, gmsc, null,
                    null, null, null, null,
                    false, null, null, false,
                    null, null, null, false,
                    null, false, false, false,
                    false, null, null, null,
                    false, null);

        } catch (MAPException e) {
            var msg = "Failed to add SRI to dialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).parent(e).build();
        }
    }

    @Override
    protected MAPApplicationContext getApplicationContext() throws SystemException {
        // ref: org.mobicents.protocols.ss7.map.service.callhandling.MAPDialogCallHandlingImpl.addSendRoutingInformationRequest
        Integer[] supportedVersions = {1, 2, 3};
        var mapContextVersion = getAcVersion(Integer.parseInt(getMapVersion()), supportedVersions);
        return MAPApplicationContext.getInstance(MAPApplicationContextName.locationInfoRetrievalContext, mapContextVersion);
    }
}
