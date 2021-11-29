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

import com.rodan.intruder.diameter.entities.event.model.DiameterMessage;
import com.rodan.intruder.diameter.entities.event.model.ResultCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter public abstract class DiameterMessageImpl implements DiameterMessage {
    private ResultCode resultCode;
    private String sessionId;

    public DiameterMessageImpl(ResultCode resultCode, String sessionId) {
        this.resultCode = resultCode;
        this.sessionId = sessionId;
    }

    @Override
    public ResultCode getResultCode() {
        return resultCode;
    }

    public String getSessionId() {
        return sessionId;
    }
}
