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

package com.rodan.intruder.ss7.entities.payload.sms;

import com.rodan.intruder.ss7.entities.payload.Ss7Payload;
import com.rodan.intruder.ss7.entities.payload.mobility.UlResponsePayload;
import com.rodan.library.model.Constants;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString(callSuper = true)
public class FsmPayload extends Ss7Payload {
    private String imsi;
    private String sender;
    private String targetMscGt;
    private String content;
    private String messageType;
    private String spoofSmsc;
    private String smscGt;
    private String mapVersion;

    @Builder
    public FsmPayload(String localGt, String imsi, String sender, String targetMscGt, String content, String messageType,
                      String spoofSmsc, String smscGt, String mapVersion) {
        super(localGt, Constants.SCCP_MSC_SSN, Constants.SCCP_MSC_SSN);
        this.imsi = imsi;
        this.sender = sender;
        this.targetMscGt = targetMscGt;
        this.messageType = messageType;
        this.content = content;
        this.spoofSmsc = spoofSmsc;
        this.smscGt = smscGt;
        this.mapVersion = mapVersion;
    }

    @Override
    public boolean isAbuseOpcodeTagForBypass() {
        return false;
    }

    @Override
    public boolean isMalformedAcnForBypass() {
        return false;
    }

    @Override
    public String getPayloadName() {
        return Constants.FSM_PAYLOAD_NAME;
    }

    public FsmPayload withImsi(String imsi) {
        return imsi.equals(this.imsi) ? this : new FsmPayload(getLocalGt(), imsi, getSender(), getTargetMscGt(),
                getContent(), getMessageType(), getSpoofSmsc(), getSmscGt(), getMapVersion());
    }

    public FsmPayload withTargetMscGt(String targetMscGt) {
        return targetMscGt.equals(this.targetMscGt) ? this : new FsmPayload(getLocalGt(), getImsi(), getSender(), targetMscGt,
                getContent(), getMessageType(), getSpoofSmsc(), getSmscGt(), getMapVersion());
    }

    public FsmPayload withContent(String content) {
        return content.equals(this.content) ? this : new FsmPayload(getLocalGt(), getImsi(), getSender(), getTargetMscGt(),
                content, getMessageType(), getSpoofSmsc(), getSmscGt(), getMapVersion());
    }
}
