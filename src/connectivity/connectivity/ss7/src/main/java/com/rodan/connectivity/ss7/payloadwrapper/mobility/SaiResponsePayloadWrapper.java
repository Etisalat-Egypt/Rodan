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
import com.rodan.library.util.Util;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPDialogMobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.authentication.AuthenticationSetList;
import org.mobicents.protocols.ss7.map.api.service.mobility.authentication.AuthenticationTriplet;
import org.mobicents.protocols.ss7.map.api.service.mobility.authentication.EpcAv;

import java.util.ArrayList;

/**
 * @author Ayman ElSherif
 */
@Payload(name = Constants.SAI_RESPONSE_PAYLOAD_NAME)
public class SaiResponsePayloadWrapper extends JSs7PayloadWrapper<MapMobilityService, MAPDialogMobility> {
    @Getter(AccessLevel.PRIVATE) private long invokeId;
    @Getter(AccessLevel.PRIVATE) private String imsi;
    @Getter(AccessLevel.PRIVATE) private String requestingNodeType;
    @Getter(AccessLevel.PRIVATE) private String rand;
    @Getter(AccessLevel.PRIVATE) private String sres;
    @Getter(AccessLevel.PRIVATE) private String kc;
    @Getter(AccessLevel.PRIVATE) private String xres;
    @Getter(AccessLevel.PRIVATE) private String authPs;
    @Getter(AccessLevel.PRIVATE) private String kasme;

    @Builder
    public SaiResponsePayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig, SccpAdapter sccpAdapter, MapAdapter mapAdapter, MapDialogGenerator<MAPDialogMobility> dialogGenerator, long invokeId, String imsi, String requestingNodeType, String rand, String sres, String kc, String xres, String authPs, String kasme) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.invokeId = invokeId;
        this.imsi = imsi;
        this.requestingNodeType = requestingNodeType;
        this.rand = rand;
        this.sres = sres;
        this.kc = kc;
        this.xres = xres;
        this.authPs = authPs;
        this.kasme = kasme;
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
            var imsi = mapFactory.createIMSI(getImsi());

            var rand = Util.decodeHexString(getRand()); // Triplet rand is 16 bytes
            var sres = Util.decodeHexString(getSres()); // Triplet sres is 4 bytes
            var kc = Util.decodeHexString(getKc()); // Triplet kc is 8 bytes
            var authTriplet = mapFactory.createAuthenticationTriplet(rand, sres, kc);
            var authTripletList = new ArrayList<AuthenticationTriplet>();
            authTripletList.add(authTriplet);
//            var avNumber = request.getNumberOfRequestedVectors();
//            for (int i = 0; i < avNumber; i++) {
//                authTriplet = mapFactory.createAuthenticationTriplet(rand, sres, kc);
//                authTripletList.add(authTriplet);
//            }
            // 2G authentication triplets, use mapFactory.createQuintupletList() to return 3G authentication Quintuplet.
            var tripletList = mapFactory.createTripletList(authTripletList);
            AuthenticationSetList authList = mapFactory.createAuthenticationSetList(tripletList);

            rand = Util.decodeHexString(getRand()); // EPC rand is 16 bytes
            var xres = Util.decodeHexString(getXres()); // EPC xres is between 4 & 16 bytes
            var authPs = Util.decodeHexString(getAuthPs()); // EPC authn is 16 bytes
            var kasme = Util.decodeHexString(getKasme()); // EPC kasme is 32 bytes
            var epcAv = mapFactory.createEpcAv(rand, xres, authPs, kasme, null);
            var epcAuthVectorList = new ArrayList<EpcAv>();
            epcAuthVectorList.add(epcAv);
//            var avNumberPs = request.getNumberOfRequestedAdditionalVectors();
//            if (request.getAdditionalVectorsAreForEPS()) {
//                for (int i = 0; i < avNumberPs; i++) {
//                    epcAv = mapFactory.createEpcAv(randPs, xresPs, authPs, kasme, null);
//                    epcAuthVectorList.add(epcAv);
//                }
//            }
            var epsAuthList = mapFactory.createEpsAuthenticationSetList(epcAuthVectorList);
//            if ("vlr".equals(getRequestingNodeType())) {
//                epsAuthList = null;
//
//            } else {
//                authList = null;
//            }

            dialog.addSendAuthenticationInfoResponse(getInvokeId(), authList, null, epsAuthList);

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
