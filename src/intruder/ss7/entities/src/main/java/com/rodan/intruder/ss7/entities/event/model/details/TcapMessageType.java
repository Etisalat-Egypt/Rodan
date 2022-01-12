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

package com.rodan.intruder.ss7.entities.event.model.details;

/**
 * @author Ayman ElSherif
 */
public enum TcapMessageType {
    Unidirectional(97),
    Begin(98),
    End(100),
    Continue(101),
    Abort(103),
    Unknown(-1);

    private int code;

    private TcapMessageType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static TcapMessageType getInstance(int code) {
        switch (code) {
            case 97:
                return Unidirectional;
            case 98:
                return Begin;
            case 100:
                return End;
            case 101:
                return Continue;
            case 103:
                return Abort;
            default:
                return null;
        }
    }
}
