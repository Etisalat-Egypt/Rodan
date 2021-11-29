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

package com.rodan.library.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public class SupportedCamelPhases {
    private boolean phase1;
    private boolean phase2;
    private boolean phase3;
    private boolean phase4;

    @Builder
    public SupportedCamelPhases(boolean phase1, boolean phase2, boolean phase3, boolean phase4) {
        this.phase1 = phase1;
        this.phase2 = phase2;
        this.phase3 = phase3;
        this.phase4 = phase4;
    }
}
