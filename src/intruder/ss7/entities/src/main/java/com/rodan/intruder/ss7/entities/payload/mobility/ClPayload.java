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

import com.rodan.intruder.ss7.entities.event.model.mobility.details.CancellationType;
import com.rodan.intruder.ss7.entities.payload.Ss7Payload;
import com.rodan.library.model.Constants;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString(callSuper = true)
public class ClPayload extends Ss7Payload {
    private String imsi;
    private String targetVlrGt;
    private String spoofHlr;
    private String targetHlrGt;
    private CancellationType cancellationType;
    private String newMscGt;
    private String mapVersion;

    @Builder
    public ClPayload(String localGt, String imsi, String targetVlrGt, String spoofHlr, String targetHlrGt,
                     CancellationType cancellationType, String newMscGt, String mapVersion) {
        super(localGt, Constants.SCCP_HLR_SSN, Constants.SCCP_VLR_SSN);
        this.imsi = imsi;
        this.targetVlrGt = targetVlrGt;
        this.spoofHlr = spoofHlr;
        this.targetHlrGt = targetHlrGt;
        this.cancellationType = cancellationType;
        this.newMscGt = newMscGt;
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
        return Constants.CL_PAYLOAD_NAME;
    }
}
