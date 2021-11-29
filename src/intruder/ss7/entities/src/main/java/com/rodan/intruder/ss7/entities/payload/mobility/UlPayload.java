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

package com.rodan.intruder.ss7.entities.payload.mobility;

import com.rodan.intruder.ss7.entities.payload.Ss7Payload;
import com.rodan.library.model.Constants;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString(callSuper = true)
public class UlPayload extends Ss7Payload {
    private String imsi;
    private String currentMscGt;
    private String currentVlrGt;
    private String newMscGt;
    private String newVlrGt;
    private String msrn;
    private String forwardSmsToVictim;
    private String hlrGt;
    private String mapVersion;
    private String cc;
    private String ndc;
    private String mcc;
    private String mnc;

    @Builder
    public UlPayload(String localGt, String imsi, String currentMscGt,
                     String currentVlrGt, String newMscGt, String newVlrGt, String msrn, String forwardSmsToVictim,
                     String hlrGt, String mapVersion, String cc, String ndc, String mcc, String mnc) {
        super(localGt, Constants.SCCP_MSC_SSN, Constants.SCCP_HLR_SSN);
        this.imsi = imsi;
        this.currentMscGt = currentMscGt;
        this.currentVlrGt = currentVlrGt;
        this.newMscGt = newMscGt;
        this.newVlrGt = newVlrGt;
        this.msrn = msrn;
        this.forwardSmsToVictim = forwardSmsToVictim;
        this.hlrGt = hlrGt;
        this.mapVersion = mapVersion;
        this.cc = cc;
        this.ndc = ndc;
        this.mcc = mcc;
        this.mnc = mnc;
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
        return Constants.UL_PAYLOAD_NAME;
    }
}
