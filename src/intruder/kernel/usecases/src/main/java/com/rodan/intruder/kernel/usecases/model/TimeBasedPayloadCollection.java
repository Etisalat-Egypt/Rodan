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

package com.rodan.intruder.kernel.usecases.model;

import com.rodan.intruder.kernel.entities.payload.SignalingPayload;
import com.rodan.library.util.IteratorWithProgress;
import lombok.Builder;
import lombok.Getter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;

public class TimeBasedPayloadCollection <PL extends SignalingPayload> implements IteratorWithProgress<PL> {
    final static Logger logger = LogManager.getLogger(TimeBasedPayloadCollection.class);

    @Getter
    PL payload;
    private LocalDateTime startTime;
    private Integer duration;

    @Builder
    public TimeBasedPayloadCollection(Integer duration, PL payload) {
        this.duration = duration;
        this.payload = payload;
    }

    @Override
    public boolean hasNext() {
        return (startTime == null || LocalDateTime.now().isBefore(startTime.plusMinutes(duration)));
    }

    @Override
    public PL next() {
        if (startTime == null)
            startTime = LocalDateTime.now();

        return payload;
    }

    @Override
    public double getProgressPercentage() {
        var totalTime = duration;
        var passedTime = LocalDateTime.now().minusMinutes(startTime.getMinute()).getMinute();
        var percentage = ((passedTime * 1.0) / totalTime) * 100;

        return Math.round(percentage * 100.0) / 100.0;
    }
}
