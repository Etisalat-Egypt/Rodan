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
public class IsdPayload extends Ss7Payload {
    public enum Usage {
        BAR(0), REDIRECT_CAMEL(1), FORWARD_SS(2), LINE_IDENTIFICATION(3);

        private int code;

        private Usage(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Usage getInstance(int code) {
            switch (code) {
                case 0:
                    return BAR;
                case 1:
                    return REDIRECT_CAMEL;
                case 2:
                    return FORWARD_SS;
                case 3:
                    return LINE_IDENTIFICATION;
                default:
                    return null;
            }
        }
    }

    private Usage usage;
    private String imsi;
    private String msisdn;
    private String forwardMsisdn;
    private String gsmScf;
    private String targetVlrGt;
    private String barred;
    private String spoofHlr;
    private String targetHlrGt;
    private String mapVersion;

    @Builder
    public IsdPayload(String localGt, Usage usage, String imsi, String msisdn,
                      String forwardMsisdn, String gsmScf, String targetVlrGt, String barred, String spoofHlr,
                      String targetHlrGt, String mapVersion) {
        super(localGt, Constants.SCCP_HLR_SSN, Constants.SCCP_VLR_SSN);
        this.usage = usage;
        this.imsi = imsi;
        this.msisdn = msisdn;
        this.forwardMsisdn = forwardMsisdn;
        this.gsmScf = gsmScf;
        this.targetVlrGt = targetVlrGt;
        this.barred = barred;
        this.spoofHlr = spoofHlr;
        this.targetHlrGt = targetHlrGt;
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
        return Constants.ISD_PAYLOAD_NAME;
    }
}
