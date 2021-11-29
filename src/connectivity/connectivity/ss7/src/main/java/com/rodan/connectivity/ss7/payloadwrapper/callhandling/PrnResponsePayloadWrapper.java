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
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.error.ValidationException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.service.callhandling.MAPDialogCallHandling;

@Payload(name = Constants.PNR_RESPONSE_PAYLOAD_NAME)
@ToString(callSuper = true)
public class PrnResponsePayloadWrapper extends JSs7PayloadWrapper<MapCallHandlingService, MAPDialogCallHandling> {
    @Getter(AccessLevel.PRIVATE) private Long invokeId;
    @Getter(AccessLevel.PRIVATE) private String msrn;
    @Getter(AccessLevel.PRIVATE) private String vmsc;

    @Builder
    public PrnResponsePayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig,
                                     SccpAdapter sccpAdapter, MapAdapter mapAdapter,
                                     MapDialogGenerator<MAPDialogCallHandling> dialogGenerator, Long invokeId,
                                     String msrn, String vmsc) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.invokeId = invokeId;
        this.msrn = msrn;
        this.vmsc = vmsc;
    }

    @Override
    public MAPDialogCallHandling generateCarrier() throws SystemException, ValidationException {
        var msg = "ResponsePayload shall NOT generate new dialogs";
        logger.error(msg);
        throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
    }

    @Override
    public void addToCarrier(MAPDialogCallHandling dialog) throws SystemException {
        try {
            validate();
            var mapFactory = getMapAdapter().getParamFactory();
            var invokeId = getInvokeId();
            var msrn = mapFactory.createISDNAddressString(AddressNature.international_number,
                    NumberingPlan.ISDN, getMsrn());
            var vmsc = mapFactory.createISDNAddressString(AddressNature.international_number,
                    NumberingPlan.ISDN, getVmsc());

            dialog.addProvideRoamingNumberResponse(invokeId, msrn, null, true, vmsc);
            dialog.send();
            logger.debug("PRN response sent.");

        } catch (MAPException e) {
            var msg = "Failed to add UL to dialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
        }
    }

    @Override
    protected MAPApplicationContext getApplicationContext() throws SystemException {
        var msg = "ResponsePayload shall NOT generate new dialogs";
        logger.error(msg);
        throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
    }

}
