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

package com.rodan.intruder.ss7.gateway.fowrarderpayload.sms;

import com.rodan.intruder.ss7.entities.payload.sms.FsmForwarderPayload;
import lombok.Builder;
import lombok.Getter;
import org.mobicents.protocols.ss7.map.api.service.sms.ForwardShortMessageRequest;
import org.mobicents.protocols.ss7.map.api.service.sms.MtForwardShortMessageRequest;

public class FsmForwarderPayloadImpl extends FsmForwarderPayload {
    @Getter private ForwardShortMessageRequest request;
    @Getter private MtForwardShortMessageRequest mtRequest;

    @Builder(builderMethodName = "forwarderBuilder")
    public FsmForwarderPayloadImpl(FsmForwarderPayload payload, ForwardShortMessageRequest request,
                                   MtForwardShortMessageRequest mtRequest) {
        super(payload.getLocalGt(), payload.getImsi(), payload.getSender(),
                payload.getTargetMscGt(), payload.getContent(), payload.getSpoofSmsc(), payload.getSmscGt(),
                payload.getMapVersion());
        this.request = request;
        this.mtRequest = mtRequest;
    }
}
