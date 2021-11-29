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
import org.mobicents.protocols.ss7.map.MAPDialogImpl;
import org.mobicents.protocols.ss7.map.api.*;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;

public abstract class MapService {
    final static Logger logger = LogManager.getLogger(MapService.class);

    protected MAPStack stack;

    public MapService(MAPStack stack) {
        this.stack = stack;
    }

    public abstract void activate();

    public abstract void deactivate();

    public abstract void addListener(MAPServiceListener listener);

    public abstract void removeListener(MAPServiceListener listener);

    public abstract MAPDialog generateDialog(SccpAddress callingParty, SccpAddress calledParty,
                                             MAPApplicationContext mapContext) throws SystemException;

    public void send(MAPDialog dialog) throws SystemException {
        try {
            dialog.send();

        } catch (MAPException e) {
            logger.error("Failed to send MAP dialog", e);
            throw SystemException.builder().code(ErrorCode.MAP_DIALOG_SEND_FAILED).build();
        }
    }

    public void send(MAPDialog dialog, MAPApplicationContext context) throws SystemException {
        try {
            ((MAPDialogImpl) dialog).send(context);

        } catch (MAPException e) {
            logger.error("Failed to send MAP dialog", e);
            throw SystemException.builder().code(ErrorCode.MAP_DIALOG_SEND_FAILED).build();
        }
    }
}
