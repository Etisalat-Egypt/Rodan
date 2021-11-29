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

package com.rodan.intruder.diameter.entities.payload.s6a;

import com.rodan.intruder.diameter.entities.payload.DiameterPayload;
import com.rodan.library.model.Constants;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString(callSuper = true)
public class NorPayload extends DiameterPayload {
    private String imsi;
    private String spoofOrigin;
    private String spoofedHost;
    private String spoofedRealm;

    @Builder
    public NorPayload(String destinationRealm, String destinationHost, String originRealm, String originHost, String imsi,
                      String spoofOrigin, String spoofedHost, String spoofedRealm) {
        super(destinationRealm, destinationHost, originRealm, originHost);
        this.imsi = imsi;
        this.spoofOrigin = spoofOrigin;
        this.spoofedHost = spoofedHost;
        this.spoofedRealm = spoofedRealm;
    }

    @Override
    public String getPayloadName() {
        return Constants.NOR_PAYLOAD_NAME;
    }
}
