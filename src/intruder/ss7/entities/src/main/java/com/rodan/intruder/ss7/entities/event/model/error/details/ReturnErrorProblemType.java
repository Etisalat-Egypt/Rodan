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

public enum ReturnErrorProblemType {
    UnrecognizedInvokeID(0),
    ReturnErrorUnexpected(1),
    UnrecognizedError(2),
    UnexpectedError(3),
    MistypedParameter(4);

    private int code;

    private ReturnErrorProblemType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ReturnErrorProblemType getInstance(int code) {
        switch (code) {
            case 0:
                return UnrecognizedInvokeID;
            case 1:
                return ReturnErrorUnexpected;
            case 2:
                return UnrecognizedError;
            case 3:
                return UnexpectedError;
            case 4:
                return MistypedParameter;
            default:
                return null;
        }
    }
}
