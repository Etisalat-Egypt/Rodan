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

package com.rodan.intruder.ss7.gateway.handler;

import com.rodan.intruder.ss7.entities.event.dialog.CapDialogEventListener;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.cap.api.CAPDialog;
import org.mobicents.protocols.ss7.cap.api.CAPDialogListener;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPGeneralAbortReason;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPGprsReferenceNumber;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPNoticeProblemDiagnostic;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPUserAbortReason;
import org.mobicents.protocols.ss7.tcap.asn.comp.PAbortCauseType;

import java.util.ArrayList;
import java.util.List;

public class CapDialogEventHandler implements CAPDialogListener {
    Logger logger = LogManager.getLogger(CapDialogEventHandler.class);

    private List<CapDialogEventListener> listeners;

    public CapDialogEventHandler() {
        listeners = new ArrayList<>();
    }

    public void addListener(CapDialogEventListener listener) {
        logger.debug("Registering CAP listener: " + listener);
        if (listeners.contains(listener)) {
            logger.warn("Registering CapDialogEventListener for already existing one");
            return;
        }

        listeners.add(listener);
    }

    public void removeListener(CapDialogEventListener listener) {
        logger.debug("Removing CAP listener: " + listener);
        if (!listeners.contains(listener)) {
            logger.warn("Removing a non-existing CapDialogEventListener");
            return;
        }

        listeners.remove(listener);
    }

    @Override
	public void onDialogDelimiter(CAPDialog capDialog) {
        logger.debug("[[[[[[[[[[    onDialogDelimiter      ]]]]]]]]]]");
        String msg = String.format("onDialogDelimiter received for dialogId: [%d]", capDialog.getLocalDialogId());
        logger.debug(msg);
    }

    @Override
	public void onDialogRequest(CAPDialog capDialog, CAPGprsReferenceNumber capGprsReferenceNumber) {
        String msg = String.format(
                "onDialogRequest received for dialogId: [%d], acn: [%s], capGprsReferenceNumber: [%s]",
                capDialog.getLocalDialogId(), capDialog.getApplicationContext().toString(), capGprsReferenceNumber);
        logger.debug(msg);
    }

    @Override
	public void onDialogAccept(CAPDialog capDialog, CAPGprsReferenceNumber capGprsReferenceNumber) {
        logger.debug("[[[[[[[[[[    onDialogAccept      ]]]]]]]]]]");
        String msg = String.format("onDialogAccept received for dialogId: [%d], capGprsReferenceNumber: [%s]",
                capDialog.getLocalDialogId(), capGprsReferenceNumber);
        logger.debug(msg);
    }

    @Override
	public void onDialogUserAbort(CAPDialog capDialog, CAPGeneralAbortReason capGeneralAbortReason, CAPUserAbortReason capUserAbortReason) {
        logger.debug("[[[[[[[[[[    onDialogUserAbort      ]]]]]]]]]]");
        String msg = String.format(
                "onDialogUserAbort received for dialogId: [%d], capGeneralAbortReason: [%s], capUserAbortReason: [%s]",
                capDialog.getLocalDialogId(), capGeneralAbortReason, capUserAbortReason);
        logger.error(msg);
    }

    @Override
	public void onDialogProviderAbort(CAPDialog capDialog, PAbortCauseType pAbortCauseType) {
        logger.debug("[[[[[[[[[[    onDialogProviderAbort      ]]]]]]]]]]");
        String msg = String.format(
                "onDialogProviderAbort received for dialogId: [%d], pAbortCauseType:  [%s]",
                capDialog.getLocalDialogId(), pAbortCauseType);
        logger.error(msg);
    }

    @Override
	public void onDialogClose(CAPDialog capDialog) {
        logger.debug("[[[[[[[[[[    onDialogClose      ]]]]]]]]]]");
        String msg = String.format("onDialogClose received for dialogId: [%d]", capDialog.getLocalDialogId());
        logger.debug(msg);
    }

    @Override
	public void onDialogRelease(CAPDialog capDialog) {
        logger.debug("[[[[[[[[[[    onDialogRelease      ]]]]]]]]]]");
        String msg = String.format("onDialogRelease received for dialogId: [%d]", capDialog.getLocalDialogId());
        logger.debug(msg);
    }

    @Override
	public void onDialogTimeout(CAPDialog capDialog) {
        logger.debug("[[[[[[[[[[    onDialogTimeout      ]]]]]]]]]]");
        String msg = String.format("onDialogTimeout received for dialogId: [%d]", capDialog.getLocalDialogId());
        logger.debug(msg);
    }

    @Override
	public void onDialogNotice(CAPDialog capDialog, CAPNoticeProblemDiagnostic capNoticeProblemDiagnostic) {
//		String msg = String.format("onDialogNotice received for dialogId: [%d], capNoticeProblemDiagnostic: [%s]",
//                capDialog.getLocalDialogId(), capNoticeProblemDiagnostic);
//		logger.debug(msg);
    }
}
