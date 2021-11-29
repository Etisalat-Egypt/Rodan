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

package com.rodan.connectivity.ss7.payloadwrapper.mobility;

import com.rodan.connectivity.ss7.adapter.MapAdapter;
import com.rodan.connectivity.ss7.adapter.SccpAdapter;
import com.rodan.connectivity.ss7.payloadwrapper.JSs7PayloadWrapper;
import com.rodan.connectivity.ss7.service.MapDialogGenerator;
import com.rodan.connectivity.ss7.service.MapMobilityService;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Payload;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.service.callhandling.MAPDialogCallHandling;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPDialogMobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.LocationInformationGPRS;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.SubscriberInfo;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.SubscriberState;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.SubscriberStateChoice;

/**
 * @author Ayman ElSherif
 */
@Payload(name = Constants.PSI_RESPONSE_PAYLOAD_NAME)
public class PsiResponsePayloadWrapper extends JSs7PayloadWrapper<MapMobilityService, MAPDialogMobility> {
    @Getter(AccessLevel.PRIVATE) private long invokeId;
    @Getter(AccessLevel.PRIVATE) private String imei;
    @Getter(AccessLevel.PRIVATE) private String subscriberState;
    @Getter(AccessLevel.PRIVATE) private String vlrGt;
    @Getter(AccessLevel.PRIVATE) private String vmscGt;
    @Getter(AccessLevel.PRIVATE) private int mcc;
    @Getter(AccessLevel.PRIVATE) private int mnc;
    @Getter(AccessLevel.PRIVATE) private int lac;
    @Getter(AccessLevel.PRIVATE) private int cellId;
    @Getter(AccessLevel.PRIVATE) private int ageOfLocation;

    @Builder
    public PsiResponsePayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig,
                                     SccpAdapter sccpAdapter, MapAdapter mapAdapter, MapDialogGenerator<MAPDialogMobility> dialogGenerator,
                                     long invokeId, String imei, String subscriberState, String vlrGt, String vmscGt, int mcc, int mnc,
                                     int lac, int cellId, int ageOfLocation) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.invokeId = invokeId;
        this.imei = imei;
        this.subscriberState = subscriberState;
        this.vlrGt = vlrGt;
        this.vmscGt = vmscGt;
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.cellId = cellId;
        this.ageOfLocation = ageOfLocation;
    }

    @Override
    public MAPDialogMobility generateCarrier() throws SystemException {
        var msg = "ResponsePayload shall NOT generate new dialogs";
        logger.error(msg);
        throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
    }

    @Override
    public void addToCarrier(MAPDialogMobility dialog) throws SystemException {
        try {
            validate();

            var mapFactory = getMapAdapter().getParamFactory();

            var imei = mapFactory.createIMEI(getImei());
            // TODO: Change subscriberState to enum, not a String in all Wrappers
            var subscriberState = switch (getSubscriberState()) {
                case "assumedIdle" -> mapFactory.createSubscriberState(SubscriberStateChoice.assumedIdle, null);

                default -> {
                    var msg = "Unknown subscriber state: " + getSubscriberState();
                    logger.error(msg);
                    throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
                }
            };

            var vlr = mapFactory.createISDNAddressString(AddressNature.international_number,
                    NumberingPlan.ISDN, getVlrGt());
            var vmsc = mapFactory.createISDNAddressString(AddressNature.international_number,
                    NumberingPlan.ISDN, getVmscGt());
            var cellId = mapFactory.createCellGlobalIdOrServiceAreaIdFixedLength(getMcc(),
                    getMnc(), getLac(), getCellId());
            var cellIdLai = mapFactory.createCellGlobalIdOrServiceAreaIdOrLAI(cellId);
            // age =0 is MS is on an active call, otherwise age will be >0 which means its the last known location
            var locationInfo = mapFactory.createLocationInformation(getAgeOfLocation(), null,
                    vlr, null, cellIdLai, null, null, vmsc,
                    null, false, false, null, null);

            // Reference code
//            // RAI: (RAC, LAC, MNC and MC) Ref 3GPP TS 29.060   -   7.7.3 Routeing Area Identity (RAI)
//            // MCC: 03 = 0000 0011 = 0x03
//            // MNC: 06 02: 0000 0110 0000 0010 = 0x06 0x02
//            // LAC: 01 02 = 0000 0001 0000 0010 = 0x01 0x02
//            // RAC: 3 = 0000 0011 = 0x03
//            // https://www.programcreek.com/java-api-examples/?api=org.mobicents.protocols.ss7.map.api.service.mobility.MAPDialogMobility
//            var routingAreaId = mapFactory.createRAIdentity(new byte[] {(byte) 0x03, (byte) 0x06, (byte) 0x02, (byte) 0x01,
//                    (byte) 0x02, (byte) 0x03});
//            var sgsn = mapFactory.createISDNAddressString(AddressNature.international_number,NumberingPlan.ISDN,options.getSgsnGt());
//            var locationInfoPs = mapFactory.createLocationInformationGPRS(null,
//                    routingAreaId, null, sgsn,null,null,false,null,
//                    true,5);
            LocationInformationGPRS locationInfoPs = null;

            SubscriberInfo subscriberInfo = mapFactory.createSubscriberInfo(locationInfo, subscriberState,null,
                    locationInfoPs, null, imei,null, null,null);

            dialog.addProvideSubscriberInfoResponse(getInvokeId(), subscriberInfo, null);


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
