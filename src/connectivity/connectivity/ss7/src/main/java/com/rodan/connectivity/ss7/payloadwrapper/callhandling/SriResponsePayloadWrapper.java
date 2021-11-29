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

package com.rodan.connectivity.ss7.payloadwrapper.callhandling;

import com.rodan.connectivity.ss7.adapter.MapAdapter;
import com.rodan.connectivity.ss7.adapter.SccpAdapter;
import com.rodan.connectivity.ss7.payloadwrapper.JSs7PayloadWrapper;
import com.rodan.connectivity.ss7.service.MapCallHandlingService;
import com.rodan.connectivity.ss7.service.MapDialogGenerator;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Payload;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.service.callhandling.MAPDialogCallHandling;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.SubscriberInfo;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.SubscriberStateChoice;

/**
 * @author Ayman ElSherif
 */
@Payload(name = Constants.SRI_RESPONSE_PAYLOAD_NAME)
@ToString(callSuper = true)
public class SriResponsePayloadWrapper extends JSs7PayloadWrapper<MapCallHandlingService, MAPDialogCallHandling> {
    @Getter(AccessLevel.PRIVATE) private long invokeId;
    @Getter(AccessLevel.PRIVATE) private String imsi;
    @Getter(AccessLevel.PRIVATE) private String vmscGt;
    @Getter(AccessLevel.PRIVATE) private String msrn;

    @Builder
    public SriResponsePayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig,
                                     SccpAdapter sccpAdapter, MapAdapter mapAdapter,
                                     MapDialogGenerator<MAPDialogCallHandling> dialogGenerator,
                                     long invokeId, String imsi, String vmscGt, String msrn) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.invokeId = invokeId;
        this.imsi = imsi;
        this.vmscGt = vmscGt;
        this.msrn = msrn;
    }

    @Override
    public MAPDialogCallHandling generateCarrier() throws SystemException {
        var msg = "ResponsePayload shall NOT generate new dialogs";
        logger.error(msg);
        throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
    }

    @Override
    public void addToCarrier(MAPDialogCallHandling dialog) throws SystemException {
        try {
            validate();

            var mapFactory = getMapAdapter().getParamFactory();
            var imsi = mapFactory.createIMSI(getImsi());
//            var vlr = mapFactory.createISDNAddressString(AddressNature.international_number,
//                    org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN,
//                    getVlrGt());
//            var locationInfo = mapFactory.createLocationInformation(5, null,
//                    vlr, null, null, null, null, null,
//                    null, false, false, null, null);
//            var subscriberState = mapFactory.createSubscriberState(SubscriberStateChoice.assumedIdle, null);
//            var subscriberInfo = mapFactory.createSubscriberInfo(locationInfo, subscriberState,null,null,
//                    null, imei,null, null,null);
            SubscriberInfo subscriberInfo = null;

            var vmsc = mapFactory.createISDNAddressString(AddressNature.international_number,
                    org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN,
                    getVmscGt());
            var msrn = mapFactory.createISDNAddressString(AddressNature.international_number,
                    org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN,
                    getMsrn());
            var routingInfo = mapFactory.createRoutingInfo(msrn);
            //Check org.mobicents.protocols.ss7.map.api.service.callhandling.MAPDialogCallHandling
            // to add more parameter in the response.
            dialog.addSendRoutingInformationResponse(getInvokeId(), imsi,null,null,
                    false, subscriberInfo, null,null,false,
                    vmsc,null,null,null,null,null,
                    0,null,null, routingInfo,null,null,
                    null,null,
                    false,null);

        } catch (MAPException e) {
            var msg = "Failed to add SRI-RESP to dialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).parent(e).build();
        }
    }

    @Override
    protected MAPApplicationContext getApplicationContext() throws SystemException {
        var msg = "ResponsePayload shall NOT generate new dialogs";
        logger.error(msg);
        throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
    }
}
