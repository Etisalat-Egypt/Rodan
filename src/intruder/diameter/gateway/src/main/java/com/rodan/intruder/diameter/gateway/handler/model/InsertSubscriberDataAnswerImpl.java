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

import com.rodan.intruder.diameter.entities.event.model.InsertSubscriberDataAnswer;
import com.rodan.intruder.diameter.entities.event.model.ResultCode;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString(callSuper = true)
public class InsertSubscriberDataAnswerImpl extends DiameterMessageImpl implements InsertSubscriberDataAnswer {
    private String originHost;
    private String cellId;
    private String tac;
    private Long ageOfLocation;

    @Builder
    public InsertSubscriberDataAnswerImpl(ResultCode resultCode, String sessionId, String originHost, String cellId,
                                          String tac, Long ageOfLocation) {
        super(resultCode, sessionId);
        this.originHost = originHost;
        this.cellId = cellId;
        this.tac = tac;
        this.ageOfLocation = ageOfLocation;
    }

    @Override
    public String getOriginHost() {
        return originHost;
    }

    @Override
    public String getCellId() {
        return cellId;
    }

    @Override
    public String getTac() {
        return tac;
    }

    @Override
    public Long getAgeOfLocation() {
        return ageOfLocation;
    }
}
