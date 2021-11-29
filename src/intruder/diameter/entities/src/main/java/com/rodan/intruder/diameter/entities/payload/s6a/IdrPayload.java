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
public class IdrPayload extends DiameterPayload {
    public enum Usage {LOCATION, BAR_ODB, UNBAR_ODB, BAR_ACCESS_RESTRICTION, UNBAR_ACCESS_RESTRICTION}

    private Usage usage;
    private String imsi;
    private String targetMmeHost;

    @Builder
    public IdrPayload(String destinationRealm, String destinationHost, String originRealm, String originHost,
                      Usage usage, String imsi, String targetMmeHost) {
        super(destinationRealm, destinationHost, originRealm, originHost);
        this.usage = usage;
        this.imsi = imsi;
        this.targetMmeHost = targetMmeHost;
    }

    @Override
    public String getPayloadName() {
        return Constants.IDR_PAYLOAD_NAME;
    }
}
