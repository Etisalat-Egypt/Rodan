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

package com.rodan.intruder.ss7.entities.payload.location;

import com.rodan.intruder.ss7.entities.event.model.LocationInfo;
import com.rodan.intruder.ss7.entities.payload.Ss7Payload;
import com.rodan.library.model.Constants;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Ayman ElSherif
 */
@Getter @ToString(callSuper = true)
public class PslResponsePayload extends Ss7Payload {
    private long invokeId;
    private LocationInfo locationInfo;

    @Builder
    public PslResponsePayload(String localGt, long invokeId, LocationInfo locationInfo) {
        super(localGt, Constants.SCCP_MSC_SSN, Constants.SCCP_GMLC_SSN);
        this.invokeId = invokeId;
        this.locationInfo = locationInfo;
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
        return Constants.PSI_RESPONSE_PAYLOAD_NAME;
    }

    public PslResponsePayload withInvokeId(long invokeId) {
        return this.invokeId == invokeId ? this :
                new PslResponsePayload(getLocalGt(), invokeId, locationInfo);
    }
}
