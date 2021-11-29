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
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPServiceListener;
import org.mobicents.protocols.ss7.map.api.MAPStack;
import org.mobicents.protocols.ss7.map.api.service.sms.MAPDialogSms;
import org.mobicents.protocols.ss7.map.api.service.sms.MAPServiceSmsListener;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;

public class MapSmsService extends MapService {
    final static Logger logger = LogManager.getLogger(MapSmsService.class);

    @Builder
    public MapSmsService(MAPStack stack) {
        super(stack);
    }

    @Override
    public void activate() {
        var service = stack.getMAPProvider().getMAPServiceSms();
        if (!service.isActivated()) {
            logger.debug("Activating MAP SMS service...");
            service.acivate();

        } else {
            logger.warn("MAP SMS service is already activated.");
        }
    }

    @Override
    public void deactivate() {
        var provider = stack.getMAPProvider();
        if (provider.getMAPServiceSms().isActivated()) {
            logger.debug("Deactivating MAP SMS service...");
            provider.getMAPServiceSms().deactivate();

        } else {
            logger.warn("MAP SMS service is not activated.");
        }
    }

    @Override
    public void addListener(MAPServiceListener listener) {
        stack.getMAPProvider().getMAPServiceSms()
                .addMAPServiceListener((MAPServiceSmsListener) listener);
    }

    @Override
    public void removeListener(MAPServiceListener listener) {
        stack.getMAPProvider().getMAPServiceSms()
                .removeMAPServiceListener((MAPServiceSmsListener) listener);
    }

    @Override
    public MAPDialogSms generateDialog(SccpAddress callingParty, SccpAddress calledParty,
                                       MAPApplicationContext mapContext) throws SystemException {
        try {
            var dialog = stack.getMAPProvider().getMAPServiceSms()
                    .createNewDialog(mapContext, callingParty, null, calledParty, null);
            dialog.setReturnMessageOnError(true);
            return dialog;

        } catch (MAPException e) {
            logger.error("Failed to create MAP SMS dialog", e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
        }
    }
}
