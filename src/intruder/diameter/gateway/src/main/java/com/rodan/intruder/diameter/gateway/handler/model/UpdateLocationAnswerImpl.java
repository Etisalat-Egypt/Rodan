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

import com.rodan.intruder.diameter.entities.event.model.LcsInfo;
import com.rodan.intruder.diameter.entities.event.model.ResultCode;
import com.rodan.intruder.diameter.entities.event.model.SubscriberInfo;
import com.rodan.intruder.diameter.entities.event.model.UpdateLocationAnswer;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString(callSuper = true)
public class UpdateLocationAnswerImpl extends DiameterMessageImpl implements UpdateLocationAnswer {
    private SubscriberInfo subscriberInfo;
    private LcsInfo lcsInfo;

    @Builder
    public UpdateLocationAnswerImpl(ResultCode resultCode, String sessionId, SubscriberInfo subscriberInfo, LcsInfo lcsInfo) {
        super(resultCode, sessionId);
        this.subscriberInfo = subscriberInfo;
        this.lcsInfo = lcsInfo;
    }

    @Override
    public SubscriberInfo getSubscriberInfo() {
        return subscriberInfo;
    }

    @Override
    public LcsInfo getLcsInfo() {
        return lcsInfo;
    }
}
