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

package com.rodan.intruder.ss7.entities.event.model.error.details;

public enum PositionMethodFailureDiagnostic {
    congestion(0), insufficientResources(1), insufficientMeasurementData(2), inconsistentMeasurementData(3), locationProcedureNotCompleted(
            4), locationProcedureNotSupportedByTargetMS(5), qoSNotAttainable(6), positionMethodNotAvailableInNetwork(7), positionMethodNotAvailableInLocationArea(
            8);

    private int code;

    private PositionMethodFailureDiagnostic(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PositionMethodFailureDiagnostic getInstance(int code) {
        switch (code) {
            case 0:
                return congestion;
            case 1:
                return insufficientResources;
            case 2:
                return insufficientMeasurementData;
            case 3:
                return inconsistentMeasurementData;
            case 4:
                return locationProcedureNotCompleted;
            case 5:
                return locationProcedureNotSupportedByTargetMS;
            case 6:
                return qoSNotAttainable;
            case 7:
                return positionMethodNotAvailableInNetwork;
            case 8:
                return positionMethodNotAvailableInLocationArea;
            default:
                return null;
        }
    }
}
