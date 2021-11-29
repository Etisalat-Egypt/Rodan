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

package com.rodan.intruder.diameter.gateway.handler.model;

import com.rodan.intruder.diameter.entities.event.model.CancelLocationRequest;
import com.rodan.intruder.diameter.entities.event.model.ResultCode;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString(callSuper = true)
public class CancelLocationRequestImpl extends DiameterMessageImpl implements CancelLocationRequest {
    private String cancellationType;
    private String imsi;

    @Builder
    public CancelLocationRequestImpl(ResultCode resultCode, String sessionId, String cancellationType, String imsi) {
        super(resultCode, sessionId);
        this.cancellationType = cancellationType;
        this.imsi = imsi;
    }

    @Override
    public String getCancellationType() {
        return cancellationType;
    }

    @Override
    public String getImsi() {
        return imsi;
    }
}
