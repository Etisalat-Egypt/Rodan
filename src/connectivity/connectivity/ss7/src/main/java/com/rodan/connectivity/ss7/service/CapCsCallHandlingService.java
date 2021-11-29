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
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.cap.api.CAPApplicationContext;
import org.mobicents.protocols.ss7.cap.api.CAPException;
import org.mobicents.protocols.ss7.cap.api.CAPServiceListener;
import org.mobicents.protocols.ss7.cap.api.CAPStack;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CAPDialogCircuitSwitchedCall;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CAPServiceCircuitSwitchedCallListener;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;

public class CapCsCallHandlingService extends CapService {
    final static Logger logger = LogManager.getLogger(CapCsCallHandlingService.class);

    @Builder
    public CapCsCallHandlingService(CAPStack stack) {
        super(stack);
    }

    @Override
    public void activate() {
        // TODO validate Listener type for all CAP services
        var service = stack.getCAPProvider().getCAPServiceCircuitSwitchedCall();
        if (!service.isActivated()) {
            logger.debug("Activating CAP CS call handling service...");
            service.acivate();

        } else {
            logger.warn("CAP CS call handling service is already activated.");
        }
    }

    @Override
    public void deactivate() {
        var service = stack.getCAPProvider().getCAPServiceCircuitSwitchedCall();
        if (service.isActivated()) {
            logger.debug("Deactivating CAP CS call handling service...");
            service.deactivate();

        } else {
            logger.warn("CAP CS call handling service is not activated.");
        }
    }

    @Override
    public void addListener(CAPServiceListener listener) {
        stack.getCAPProvider().getCAPServiceCircuitSwitchedCall()
                .addCAPServiceListener((CAPServiceCircuitSwitchedCallListener) listener);
    }

    @Override
    public void removeListener(CAPServiceListener listener) {
        stack.getCAPProvider().getCAPServiceCircuitSwitchedCall()
                .removeCAPServiceListener((CAPServiceCircuitSwitchedCallListener) listener);
    }

    @Override
    public CAPDialogCircuitSwitchedCall generateDialog(SccpAddress callingParty, SccpAddress calledParty,
                                                       CAPApplicationContext capContext) throws SystemException {
        try {
            var dialog = stack.getCAPProvider().getCAPServiceCircuitSwitchedCall()
                    .createNewDialog(capContext, callingParty, calledParty);
            dialog.setReturnMessageOnError(true);
            return dialog;

        } catch (CAPException e) {
            logger.error("Failed to create MAP call handling dialog", e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
        }
    }
}
