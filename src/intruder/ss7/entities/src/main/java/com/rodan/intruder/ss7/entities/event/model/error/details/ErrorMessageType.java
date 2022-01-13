/*
 * Etisalat Egypt, Open Source
 * Copyright 2022, Etisalat Egypt and individual contributors
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

package com.rodan.intruder.ss7.entities.event.model.error.details;

/**
 * @author Ayman ElSherif
 */
public enum ErrorMessageType {
    SystemFailure(0),
    UnknownSubscriber(1),
    AbsentSubscriberSm(2),
    AbsentSubscriber(3),
    CallBarred(4);

    private int code;

    private ErrorMessageType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ErrorMessageType getInstance(int code) {
        switch (code) {
            case 0:
                return SystemFailure;
            case 1:
                return UnknownSubscriber;
            case 2:
                return AbsentSubscriberSm;
            case 3:
                return AbsentSubscriber;
            case 4:
                return CallBarred;
            default:
                return null;
        }
    }
}
