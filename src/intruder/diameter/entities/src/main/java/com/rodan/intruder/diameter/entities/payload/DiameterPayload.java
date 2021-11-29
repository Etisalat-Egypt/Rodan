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

package com.rodan.intruder.diameter.entities.payload;

import com.rodan.intruder.kernel.entities.payload.SignalingPayload;
import lombok.Getter;
import lombok.ToString;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Getter @ToString
public abstract class DiameterPayload extends SignalingPayload {
    protected final static Logger logger = LogManager.getLogger(DiameterPayload.class);

    private String destinationRealm;
    private String destinationHost;
    private String originRealm;
    private String originHost;

    public DiameterPayload(String destinationRealm, String destinationHost, String originRealm, String originHost) {
        this.destinationRealm = destinationRealm;
        this.destinationHost = destinationHost;
        this.originRealm = originRealm;
        this.originHost = originHost;
    }

    public abstract String getPayloadName();
}
