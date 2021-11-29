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

import com.rodan.library.model.Constants;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString(callSuper = true)
public abstract class IsdForwarderPayload extends IsdPayload {
    public IsdForwarderPayload(String localGt, Usage usage, String imsi, String msisdn, String forwardMsisdn,
                               String gsmScf, String targetVlrGt, String barred, String spoofHlr, String targetHlrGt,
                               String mapVersion) {
        super(localGt, usage, imsi, msisdn, forwardMsisdn, gsmScf, targetVlrGt, barred, spoofHlr, targetHlrGt, mapVersion);
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
        return Constants.ISD_FORWARDER_PAYLOAD_NAME;
    }
}
