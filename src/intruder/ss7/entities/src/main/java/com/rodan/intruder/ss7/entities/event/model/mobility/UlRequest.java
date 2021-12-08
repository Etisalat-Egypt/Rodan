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

package com.rodan.intruder.ss7.entities.event.model.mobility;

import com.rodan.intruder.ss7.entities.event.model.MapMessage;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Ayman ElSherif
 */
@Getter @ToString
public abstract class UlRequest implements MapMessage {
    private String imsi;
    private String mscGt;
    private String vlrGt;

    public UlRequest(String imsi, String mscGt, String vlrGt) {
        this.imsi = imsi;
        this.mscGt = mscGt;
        this.vlrGt = vlrGt;
    }
}
