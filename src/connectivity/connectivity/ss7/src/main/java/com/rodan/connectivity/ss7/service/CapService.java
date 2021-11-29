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

package com.rodan.connectivity.ss7.service;

import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.cap.api.*;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;

public abstract class CapService {
    final static Logger logger = LogManager.getLogger(CapService.class);

    protected CAPStack stack;

    public CapService(CAPStack stack) {
        this.stack = stack;
    }

    public abstract void activate();

    public abstract void deactivate();

    public abstract void addListener(CAPServiceListener listener);

    public abstract void removeListener(CAPServiceListener listener);

    public abstract CAPDialog generateDialog(SccpAddress callingParty, SccpAddress calledParty, CAPApplicationContext capContext) throws SystemException;

    public void send(CAPDialog dialog) throws SystemException {
        try {
            dialog.send();

        } catch (CAPException e) {
            logger.error("Failed to send CAP dialog", e);
            throw SystemException.builder().code(ErrorCode.CAP_DIALOG_SEND_FAILED).build();
        }
    }
}
