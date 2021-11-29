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
    private int mcc;
    private int mnc;
    private int lac;
    private int cellId;

    private double longitude;
    private double latitude;
    private double uncertainty;
    private int ageOfLocation;

    private double longitudePs;
    private double latitudePs;
    private double uncertaintyPs;
    private int ageOfLocationPs;

    @Builder
    public LocationInfo(int mcc, int mnc, int lac, int cellId, double longitude, double latitude, double uncertainty,
                        int ageOfLocation, double longitudePs, double latitudePs, double uncertaintyPs, int ageOfLocationPs) {
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
