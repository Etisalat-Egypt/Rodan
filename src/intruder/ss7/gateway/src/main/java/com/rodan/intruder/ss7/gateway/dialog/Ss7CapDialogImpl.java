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

package com.rodan.intruder.ss7.gateway.dialog;

import com.rodan.intruder.ss7.entities.dialog.Ss7CapDialog;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.cap.api.CAPDialog;
import org.mobicents.protocols.ss7.cap.api.CAPException;

@ToString
public class Ss7CapDialogImpl implements Ss7CapDialog {
    Logger logger = LogManager.getLogger(Ss7CapDialogImpl.class);

    @Getter CAPDialog jss7Dialog;

    @Builder
    public Ss7CapDialogImpl(CAPDialog jss7Dialog) {
        this.jss7Dialog = jss7Dialog;
    }

    @Override
    public void setUserObject(Object userObject) {
        jss7Dialog.setUserObject(userObject);
    }

    public void send() throws SystemException {
        try {
            jss7Dialog.send();

        } catch (CAPException e) {
            var msg = "Failed to send CAP dialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MAP_DIALOG_SEND_FAILED).message(msg).parent(e).build();
        }
    }
}
