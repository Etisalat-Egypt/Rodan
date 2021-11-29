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

package com.rodan.connectivity.ss7.handler;

import lombok.Builder;
import lombok.Getter;
import org.mobicents.protocols.ss7.cap.api.CAPDialogListener;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CAPServiceCircuitSwitchedCallListener;

import java.util.Map;

public class CapListeners {
    @Getter
    private CAPDialogListener capDialogEventListener;
    @Getter private CAPServiceCircuitSwitchedCallListener capCsCallListener;

    @Builder
    public CapListeners(CAPDialogListener capDialogEventListener, CAPServiceCircuitSwitchedCallListener capCsCallListener) {
        this.capDialogEventListener = capDialogEventListener;
        this.capCsCallListener = capCsCallListener;
    }
}
