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

package com.rodan.intruder.ss7.entities.event.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @Builder @ToString
public class LocationInfo {
    private Integer mcc;
    private Integer mnc;
    private Integer lac;
    private Integer cellId;

    private Double longitude;
    private Double latitude;
    private Double uncertainty;
    private Integer ageOfLocation;

    private Double longitudePs;
    private Double latitudePs;
    private Double uncertaintyPs;
    private Integer ageOfLocationPs;

    @Builder
    public LocationInfo(Integer mcc, Integer mnc, Integer lac, Integer cellId, Double longitude, Double latitude, Double uncertainty,
                        Integer ageOfLocation, Double longitudePs, Double latitudePs, Double uncertaintyPs, Integer ageOfLocationPs) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.cellId = cellId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.uncertainty = uncertainty;
        this.ageOfLocation = ageOfLocation;
        this.longitudePs = longitudePs;
        this.latitudePs = latitudePs;
        this.uncertaintyPs = uncertaintyPs;
        this.ageOfLocationPs = ageOfLocationPs;
    }
}
