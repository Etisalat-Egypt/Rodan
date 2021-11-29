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

package com.rodan.connectivity.ss7.payloadwrapper.camel;

import com.rodan.connectivity.ss7.adapter.CapAdapter;
import com.rodan.connectivity.ss7.adapter.SccpAdapter;
import com.rodan.connectivity.ss7.payloadwrapper.Jss7CapPayloadWrapper;
import com.rodan.connectivity.ss7.service.CapCsCallHandlingService;
import com.rodan.connectivity.ss7.service.CapDialogGenerator;
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
import org.mobicents.protocols.ss7.cap.api.CAPApplicationContext;
import org.mobicents.protocols.ss7.cap.api.CAPException;
import org.mobicents.protocols.ss7.cap.api.isup.CalledPartyNumberCap;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CAPDialogCircuitSwitchedCall;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.isup.message.parameter.CalledPartyNumber;
import org.mobicents.protocols.ss7.isup.message.parameter.NAINumber;

import java.util.ArrayList;

@Payload(name = Constants.CAMEL_CONNECT_PAYLOAD_NAME)
@ToString(callSuper = true)
public class CamelConnectPayloadWrapper extends Jss7CapPayloadWrapper<CapCsCallHandlingService, CAPDialogCircuitSwitchedCall> {
    @Getter(AccessLevel.PRIVATE) private String msisdn;
    @Getter(AccessLevel.PRIVATE) private String targetMscGt;
    @Getter(AccessLevel.PRIVATE) private String capVersion; // TODO IMP TRX: check supported versions

    // TODO move all CAMEL payloads to a separate java package
    @Builder
    public CamelConnectPayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig,
                                      SccpAdapter sccpAdapter, CapAdapter capAdapter,
                                      CapDialogGenerator<CAPDialogCircuitSwitchedCall> dialogGenerator, String msisdn,
                                      String targetMscGt, String capVersion) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, capAdapter, dialogGenerator);
        this.msisdn = msisdn;
        this.targetMscGt = targetMscGt;
        this.capVersion = capVersion;
    }

    @Override
    public CAPDialogCircuitSwitchedCall generateCarrier() throws SystemException {
        validate();

        var sccpFactory = getSccpAdapter().getParamFactory();
        var callingGt = sccpFactory.createGlobalTitle(getLocalGt(),
                TRANSLATION_TYPE, ISDN_TELEPHONY_INDICATOR, ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var callingPc = Integer.valueOf(getNodeConfig().getSs7Association().getLocalNode().getPointCode());
        var callingParty = sccpFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                callingGt, callingPc, getLocalSsn());
        var calledGtStr = getTargetMscGt();
        var calledGt = sccpFactory.createGlobalTitle(calledGtStr, TRANSLATION_TYPE, ISDN_TELEPHONY_INDICATOR,
                ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var calledPc = Integer.valueOf(((IntruderNodeConfig) getNodeConfig()).getSs7Association().getPeerNode().getPointCode());
        var calledParty = sccpFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, calledGt,
                calledPc, getRemoteSsn());

        return dialogGenerator.generate(callingParty, calledParty, getApplicationContext());
    }

    @Override
    public void addToCarrier(CAPDialogCircuitSwitchedCall dialog) throws SystemException {
        try {
            validate();

            var capFactory = getCapAdapter().getParamFactory();
            var cdPn = getCapAdapter().getIsupParamFactory().createCalledPartyNumber();
            cdPn.setAddress(getMsisdn());
            cdPn.setNatureOfAddresIndicator(NAINumber._NAI_INTERNATIONAL_NUMBER);
            cdPn.setNumberingPlanIndicator(CalledPartyNumber._NPI_ISDN);
            cdPn.setInternalNetworkNumberIndicator(CalledPartyNumber._INN_ROUTING_ALLOWED);
            var cdPnCap = capFactory.createCalledPartyNumberCap(cdPn);
            var cdPnCapList = new ArrayList<CalledPartyNumberCap>();
            cdPnCapList.add(cdPnCap);
            var destinationRoutingAddress = capFactory.createDestinationRoutingAddress(cdPnCapList);
            dialog.addConnectRequest(destinationRoutingAddress, null, null,
                    null, null, null, null, null,
                    null, null, null, null, null,
                    false, false, false, null, false, false);

        } catch (CAPException e) {
            // TODO IMP TRX: add message (and parent) to all thrown exceptions
            var msg = "Failed to add SRI to dialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).parent(e).build();
        }
    }

    @Override
    protected CAPApplicationContext getApplicationContext() throws SystemException {
        Integer[] supportedVersions = {1, 2, 3};
        var capContextVersion = getAcVersion(Integer.parseInt(getCapVersion()), supportedVersions);
        return CAPApplicationContext.CapV2_gsmSSF_to_gsmSCF; // TODO IMP TRX: use capVersion option
//        return CAPApplicationContext.getInstance(MAPApplicationContextName.locationInfoRetrievalContext, capContextVersion);
    }
}
