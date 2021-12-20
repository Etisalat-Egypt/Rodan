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

package com.rodan.connectivity.ss7.payloadwrapper.location;

import com.rodan.connectivity.ss7.adapter.MapAdapter;
import com.rodan.connectivity.ss7.adapter.SccpAdapter;
import com.rodan.connectivity.ss7.payloadwrapper.JSs7PayloadWrapper;
import com.rodan.connectivity.ss7.service.MapDialogGenerator;
import com.rodan.connectivity.ss7.service.MapLcsService;
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
import org.mobicents.protocols.ss7.map.api.primitives.CellGlobalIdOrServiceAreaIdOrLAI;
import org.mobicents.protocols.ss7.map.api.service.lsm.MAPDialogLsm;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPDialogMobility;

/**
 * @author Ayman ElSherif
 */
@Payload(name = Constants.PSL_RESPONSE_PAYLOAD_NAME)
public class PslResponsePayloadWrapper extends JSs7PayloadWrapper<MapLcsService, MAPDialogLsm> {
    @Getter(AccessLevel.PRIVATE) private Long invokeId;
    @Getter(AccessLevel.PRIVATE) private Double longitude;
    @Getter(AccessLevel.PRIVATE) private Double latitude;
    @Getter(AccessLevel.PRIVATE) private Double uncertainty;
    @Getter(AccessLevel.PRIVATE) private Integer ageOfLocation;

    @Builder
    public PslResponsePayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig, SccpAdapter sccpAdapter,
                                     MapAdapter mapAdapter, MapDialogGenerator<MAPDialogLsm> dialogGenerator, Long invokeId,
                                     Double longitude, Double latitude, Double uncertainty, Integer ageOfLocation) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.invokeId = invokeId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.uncertainty = uncertainty;
        this.ageOfLocation = ageOfLocation;
    }

    @Override
    public MAPDialogLsm generateCarrier() throws SystemException {
        var msg = "ResponsePayload shall NOT generate new dialogs";
        logger.error(msg);
        throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
    }

    @Override
    public void addToCarrier(MAPDialogLsm dialog) throws SystemException {
        try {
            validate();

            var mapParamFactory = getMapAdapter().getParamFactory();
//            var cellId = mapParamFactory.createCellGlobalIdOrServiceAreaIdFixedLength(mcc,mnc,lac,cid);
//            var cellIdLai = mapParamFactory.createCellGlobalIdOrServiceAreaIdOrLAI(cellId);
            CellGlobalIdOrServiceAreaIdOrLAI cellIdLai = null;
            var geoInfo = mapParamFactory.createExtGeographicalInformation_EllipsoidPointWithUncertaintyCircle(latitude, longitude, uncertainty);
            dialog.addProvideSubscriberLocationResponse(getInvokeId(), geoInfo, null, null,
                    ageOfLocation, null, null, false,
                    cellIdLai, false, null, null, false,
                    null, null, null);


        } catch (MAPException e) {
            var msg = "Failed to add PSL-RESP to dialog";
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
