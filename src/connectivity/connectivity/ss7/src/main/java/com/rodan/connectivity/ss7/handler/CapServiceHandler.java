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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.cap.api.CAPDialog;
import org.mobicents.protocols.ss7.cap.api.CAPMessage;
import org.mobicents.protocols.ss7.cap.api.CAPServiceListener;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessage;
import org.mobicents.protocols.ss7.tcap.asn.comp.Problem;

public interface CapServiceHandler extends CAPServiceListener {
    Logger logger = LogManager.getLogger(CapServiceHandler.class);

    // TODO SS7: Convert to class and handle events
    default void onErrorComponent(CAPDialog capDialog, Long invokeId, CAPErrorMessage capErrorMessage) {
        logger.debug("[[[[[[[[[[    onErrorComponent      ]]]]]]]]]]");
        String msg = String.format("onErrorComponent received for dialogId: [%d], invokeId: [%d], capErrorMessage: [%s]",
                capDialog.getLocalDialogId(), invokeId, capErrorMessage);
        logger.error(msg);
    }

    default void onRejectComponent(CAPDialog capDialog, Long invokeId, Problem problem, boolean isLocalOriginated) {
        logger.debug("[[[[[[[[[[    onRejectComponent      ]]]]]]]]]]");
        String msg = String.format("onRejectComponent received for dialogId: [%d], invokeId: [%d], problem: [%s], isLocalOriginated: [%b]",
                capDialog.getLocalDialogId(), invokeId, problem, isLocalOriginated);
        logger.error(msg);
    }

    default void onInvokeTimeout(CAPDialog capDialog, Long invokeId) {
        // TODO IMP TRX: Override in Ss7Template and return error
        logger.debug("[[[[[[[[[[    onInvokeTimeout      ]]]]]]]]]]");
        String msg = String.format("onInvokeTimeout received for dialogId: [%d], invokeId: [%d]",
                capDialog.getLocalDialogId(), invokeId);
        logger.debug(msg);
    }

    default void onCAPMessage(CAPMessage capMessage) {
        logger.debug("[[[[[[[[[[    onCAPMessage      ]]]]]]]]]]");
        String msg = String.format("onCAPMessage received for capMessage: [%s], invokeId: [%d]",
                capMessage, capMessage.getInvokeId());
        logger.debug(msg);
    }
}
