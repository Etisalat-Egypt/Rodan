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

import com.rodan.intruder.ss7.entities.dialog.Ss7MapDialog;
import com.rodan.intruder.ss7.entities.event.model.error.details.ReturnErrorProblemType;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.map.MAPDialogImpl;
import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.tcap.asn.ProblemImpl;

@ToString
public class Ss7MapDialogImpl implements Ss7MapDialog {
    Logger logger = LogManager.getLogger(Ss7MapDialogImpl.class);

    @Getter MAPDialog jss7Dialog;

    @Builder
    public Ss7MapDialogImpl(MAPDialog jss7Dialog) {
        this.jss7Dialog = jss7Dialog;
    }

    @Override
    public void setAbuseOpcodeTag(boolean value) {
        ((MAPDialogImpl) jss7Dialog).setAbuseOpcodeTag(value);
    }

    @Override
    public Long getLocalDialogId() {
//        jss7Dialog.getRemoteDialogId()
        return jss7Dialog.getLocalDialogId();
    }

    @Override
    public Long getRemoteDialogId() {
        return jss7Dialog.getRemoteDialogId();
    }

    public void send() throws SystemException {
        try {
            jss7Dialog.send();

        } catch (MAPException e) {
            var msg = "Failed to send MAP dialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MAP_DIALOG_SEND_FAILED).message(msg).parent(e).build();
        }
    }

    @Override
    public void setUserObject(Object userObject) {
        jss7Dialog.setUserObject(userObject);
    }

    public void sendMalformedAcn() throws SystemException {
        try {
            ((MAPDialogImpl) jss7Dialog).sendMalformedAcn();

        } catch (MAPException e) {
            var msg = "Failed to send MAP dialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MAP_DIALOG_SEND_FAILED).message(msg).parent(e).build();
        }
    }

    public void sendRejectComponent(long invokeId, ReturnErrorProblemType type) throws SystemException {
        try {
            var problem = new ProblemImpl();
            org.mobicents.protocols.ss7.tcap.asn.comp.ReturnErrorProblemType jss7ReturnErrorProblemType = switch (type) {
                case UnexpectedError -> org.mobicents.protocols.ss7.tcap.asn.comp.ReturnErrorProblemType.UnexpectedError;
                default -> org.mobicents.protocols.ss7.tcap.asn.comp.ReturnErrorProblemType.UnrecognizedError;
            };
            problem.setType(org.mobicents.protocols.ss7.tcap.asn.comp.ProblemType.ReturnError);
            problem.setReturnErrorProblemType(jss7ReturnErrorProblemType);
            jss7Dialog.sendRejectComponent(invokeId, problem);
            jss7Dialog.send();

        } catch (MAPException e) {
            var msg = "Failed to close MAPDialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).build();
        }
    }

    public void close() throws SystemException {
        try {
            jss7Dialog.close(false);

        } catch (MAPException e) {
            var msg = "Failed to close MAPDialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).build();
        }
    }
}
