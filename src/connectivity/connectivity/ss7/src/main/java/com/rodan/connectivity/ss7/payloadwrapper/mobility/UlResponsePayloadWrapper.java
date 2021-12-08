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
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPDialogMobility;

/**
 * @author Ayman ElSherif
 */
@Payload(name = Constants.UL_RESPONSE_PAYLOAD_NAME)
@ToString(callSuper = true)
public class UlResponsePayloadWrapper extends JSs7PayloadWrapper<MapCallHandlingService, MAPDialogMobility> {
    @Getter(AccessLevel.PRIVATE) private long invokeId;
    @Getter(AccessLevel.PRIVATE) private String hlrGt;

    @Builder
    public UlResponsePayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig,
                                    SccpAdapter sccpAdapter, MapAdapter mapAdapter,
                                    MapDialogGenerator<MAPDialogMobility> dialogGenerator, long invokeId, String hlrGt) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.invokeId = invokeId;
        this.hlrGt = hlrGt;
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
            var hlr = mapFactory.createISDNAddressString(ADDRESS_NATURE, ISDN_TELEPHONY_PLAN, hlrGt);

            dialog.addUpdateLocationResponse(getInvokeId(), hlr, null, true, false);

        } catch (MAPException e) {
            var msg = "Failed to add UL response to dialog";
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
