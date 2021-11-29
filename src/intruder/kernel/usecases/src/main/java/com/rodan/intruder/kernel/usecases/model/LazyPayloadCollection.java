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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class LazyPayloadCollection<PL extends SignalingPayload> implements IteratorWithProgress<PL> {
    final static Logger logger = LogManager.getLogger(LazyPayloadCollection.class);

    private Iterator<String> dataSourceIterator;
    private Long totalDataSize;
    private int processedDataSize;
    private Function<String, PL> payloadGenerator;

    @Builder
    public LazyPayloadCollection(Stream<String> dataSource, Long totalDataSize, Function<String, PL> payloadGenerator) {
        this.dataSourceIterator = dataSource.iterator(); // TODO make mandatory
        this.totalDataSize = Objects.requireNonNull(totalDataSize);
        this.processedDataSize = 0;
        this.payloadGenerator = payloadGenerator; // TODO make mandatory
    }

    @Override
    public boolean hasNext() {
        return dataSourceIterator.hasNext();
    }

    @Override
    public PL next() {
        var next = dataSourceIterator.next();
        processedDataSize += next.getBytes().length;
        return payloadGenerator.apply(next);
    }

    @Override
    public double getProgressPercentage() {
        var percentage = ((processedDataSize * 1.0) / totalDataSize) * 100;
        logger.debug("###### processedDataSize: " + processedDataSize);
        logger.debug("###### totalDataSize: " + totalDataSize);
        logger.debug("###### percentage: " + percentage);
        return Math.round(percentage * 100.0) / 100.0;
    }
}
