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

package com.rodan.intruder.ss7.entities.payload.camel;

import com.rodan.intruder.ss7.entities.payload.Ss7Payload;
import com.rodan.library.model.Constants;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter @ToString(callSuper = true)
public class CamelConnectPayload extends Ss7Payload {
    private String msisdn;
    private String targetMscGt;
    private String capVersion; // TODO IMP TRX: check supported versions

    @Builder
    public CamelConnectPayload(String msisdn, String targetMscGt, String capVersion, String localGt) {
        super(localGt, Constants.SCCP_CAP_SSN, Constants.SCCP_CAP_SSN);
        this.msisdn = Objects.requireNonNullElse(msisdn, "");
        this.targetMscGt = Objects.requireNonNullElse(targetMscGt, "");
        this.capVersion = Objects.requireNonNullElse(capVersion, "3");
    }

    @Override
    public String getPayloadName() {
        return Constants.CAMEL_CONNECT_PAYLOAD_NAME;
    }

    @Override
    public boolean isAbuseOpcodeTagForBypass() {
        return false;
    }

    @Override
    public boolean isMalformedAcnForBypass() {
        return false;
    }
}
