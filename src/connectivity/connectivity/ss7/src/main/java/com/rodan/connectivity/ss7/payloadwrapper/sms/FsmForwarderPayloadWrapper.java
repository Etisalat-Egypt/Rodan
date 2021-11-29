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

package com.rodan.connectivity.ss7.payloadwrapper.sms;

import com.rodan.connectivity.ss7.adapter.MapAdapter;
import com.rodan.connectivity.ss7.adapter.SccpAdapter;
import com.rodan.connectivity.ss7.payloadwrapper.JSs7PayloadWrapper;
import com.rodan.connectivity.ss7.service.MapDialogGenerator;
import com.rodan.connectivity.ss7.service.MapSmsService;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Payload;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.error.ValidationException;
import lombok.Builder;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.service.sms.ForwardShortMessageRequest;
import org.mobicents.protocols.ss7.map.api.service.sms.MAPDialogSms;
import org.mobicents.protocols.ss7.map.api.service.sms.MtForwardShortMessageRequest;

@Payload(name = Constants.FSM_PAYLOAD_NAME)
public class FsmForwarderPayloadWrapper extends JSs7PayloadWrapper<MapSmsService, MAPDialogSms> {
    private ForwardShortMessageRequest request;
    private MtForwardShortMessageRequest mtRequest;

    @Builder
    public FsmForwarderPayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig,
                                      SccpAdapter sccpAdapter, MapAdapter mapAdapter,
                                      MapDialogGenerator<MAPDialogSms> dialogGenerator, ForwardShortMessageRequest request,
                                      MtForwardShortMessageRequest mtRequest) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.request = request;
        this.mtRequest = mtRequest;
    }

    @Override
    public MAPDialogSms generateCarrier() throws SystemException, ValidationException {
        var msg = "ForwarderPayload shall NOT generate new dialogs";
        logger.error(msg);
        throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
    }

    @Override
    public void addToCarrier(MAPDialogSms dialog) throws SystemException {
        try {
            validate();
            if (request != null) {
                dialog.addForwardShortMessageRequest(request.getSM_RP_DA(), request.getSM_RP_OA(), request.getSM_RP_UI(), request.getMoreMessagesToSend());
            } else {
                dialog.addMtForwardShortMessageRequest(mtRequest.getSM_RP_DA(), mtRequest.getSM_RP_OA(), mtRequest.getSM_RP_UI(),
                        mtRequest.getMoreMessagesToSend(), mtRequest.getExtensionContainer());
            }


        } catch (MAPException e) {
            JSs7PayloadWrapper.logger.error("Failed to add ForwardSM to dialog", e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
        }
    }

    @Override
    protected MAPApplicationContext getApplicationContext() throws SystemException {
        var msg = "ForwarderPayload shall NOT generate new dialogs";
        logger.error(msg);
        throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
    }
}
