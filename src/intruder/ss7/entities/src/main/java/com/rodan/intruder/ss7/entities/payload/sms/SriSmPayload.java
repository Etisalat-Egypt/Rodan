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
import com.rodan.library.model.Constants;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString(callSuper = true)
public class SriSmPayload extends Ss7Payload {
    private String msisdn;
    private String imsi;
    private String targetHlrGt;
    private String smscGt;
    private String detectSmsHomeRouting;
    private String bypassSmsHomeRouting;
    private String abuseOpcodeTag;
    private String malformedAcn;
    private String doubleMap;
    private String mapVersion;
    private String cc;
    private String ndc;
    private String mcc;
    private String mnc;

    @Builder
    public SriSmPayload(String localGt, String msisdn, String imsi, String targetHlrGt, String smscGt,
                        String detectSmsHomeRouting, String bypassSmsHomeRouting, String abuseOpcodeTag,
                        String malformedAcn, String doubleMap, String mapVersion, String cc, String ndc, String mcc, String mnc) {
        super(localGt, Constants.SCCP_MSC_SSN, Constants.SCCP_HLR_SSN);
        this.msisdn = msisdn;
        this.imsi = imsi;
        this.targetHlrGt = targetHlrGt;
        this.smscGt = smscGt;
        this.detectSmsHomeRouting = detectSmsHomeRouting;
        this.bypassSmsHomeRouting = bypassSmsHomeRouting;
        this.abuseOpcodeTag = abuseOpcodeTag;
        this.malformedAcn = malformedAcn;
        this.doubleMap = doubleMap;
        this.mapVersion = mapVersion;
        this.cc = cc;
        this.ndc = ndc;
        this.mcc = mcc;
        this.mnc = mnc;
    }

    @Override
    public boolean isAbuseOpcodeTagForBypass() {
        return "Yes".equalsIgnoreCase(abuseOpcodeTag);
    }

    @Override
    public boolean isMalformedAcnForBypass() {
        return "Yes".equalsIgnoreCase(malformedAcn);
    }

    @Override
    public String getPayloadName() {
        return Constants.SRI_SM_PAYLOAD_NAME;
    }
}
