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

package com.rodan.intruder.ss7.gateway.handler.model.camel;

import com.rodan.intruder.ss7.entities.dialog.Ss7CapDialog;
import com.rodan.intruder.ss7.entities.event.model.camel.IdpRequest;
import com.rodan.intruder.ss7.gateway.dialog.Ss7CapDialogImpl;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.mobicents.protocols.ss7.cap.api.CAPDialog;

@Getter @ToString(callSuper = true)
public class IdpRequestImpl extends IdpRequest {
    private Ss7CapDialogImpl ss7Dialog;
    private long invokeId;

    @Builder
    public IdpRequestImpl(String calledNumber, CAPDialog capDialog, long invokeId) {
        super(calledNumber);
        this.ss7Dialog = Ss7CapDialogImpl.builder().jss7Dialog(capDialog).build();
        this.invokeId = invokeId;
    }

    @Override
    public Ss7CapDialog getDialog() {
        return ss7Dialog;
    }

    @Override
    public String getRemoteAddress() {
        return ss7Dialog.getJss7Dialog().getRemoteAddress().getGlobalTitle().getDigits();
    }

    @Override
    public long getInvokeId() {
        return invokeId;
    }
}
