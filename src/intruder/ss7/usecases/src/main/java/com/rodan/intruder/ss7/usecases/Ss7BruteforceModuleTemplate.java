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

package com.rodan.intruder.ss7.usecases;

import com.rodan.intruder.ss7.entities.event.model.error.DialogUserAbort;
import com.rodan.intruder.kernel.usecases.model.ModuleResponse;
import com.rodan.intruder.ss7.entities.event.model.ErrorEvent;
import com.rodan.intruder.ss7.entities.payload.Ss7Payload;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.error.ApplicationException;
import com.rodan.library.model.notification.NotificationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public abstract class Ss7BruteforceModuleTemplate extends Ss7ModuleTemplate {
    final static Logger logger = LogManager.getLogger(Ss7BruteforceModuleTemplate.class);

    @Setter(AccessLevel.PROTECTED) private Integer bruteforceDelay;
    @Getter(AccessLevel.PROTECTED) Map<Long, Ss7Payload> sentPayloads;
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) private Iterable<Ss7Payload> payloadCollection;

    public Ss7BruteforceModuleTemplate(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
        this.sentPayloads = new HashMap<>();
    }

    @Override
    public ModuleResponse run() throws ApplicationException {
        try {
            logStart();
            validateOptions();
            initModule();

            notify("Brute forcing...", NotificationType.PROGRESS);
            var payloadIterator = this.payloadCollection.iterator();
            while (payloadIterator.hasNext() && !isResultReceived()) {
                setCurrentPayload(payloadIterator.next());
                execute(); // Signaling messages are sent in Async manner
                storeSentPayload();
                delay();
            }

            if (isWaitForResponse()) {
                waitForResponse();
            }

            if (!isExecutionError()) {
                postExecuteAction();
                logEnd();
            }

            return moduleResponse;

        } catch (ApplicationException e) {
            logger.error("Failed to run module: " + e.getMessage(), e);
            throw e;

        } finally {
            cleanup();
        }
    }

    private void storeSentPayload() {
        var dialogId = getDialog().getLocalDialogId();
        sentPayloads.put(dialogId, getCurrentPayload());
    }

    private void delay() {
        if (bruteforceDelay != null && bruteforceDelay > 0) {
            try {
                Thread.sleep(bruteforceDelay);
            } catch (InterruptedException e) {
                logger.error("Failed to delay BF module");
            }
        }
    }

    @Override
    public void onMapMessageHandlingError(ErrorEvent errorEvent) {
        logger.debug(errorEvent.getMessage());
    }

    @Override
    public void onCapMessageHandlingError(ErrorEvent errorEvent) {
        logger.debug(errorEvent.getMessage());
    }

    @Override
    public void onRejectComponent(com.rodan.intruder.ss7.entities.event.model.error.RejectComponent rejectComponent) {
        logger.debug("[[[[[[[[[[    onRejectComponent      ]]]]]]]]]]");
        var msg = String.format("rejectComponent: [%s]", rejectComponent);
        logger.debug(msg);
    }

    @Override
    public void onErrorComponent(com.rodan.intruder.ss7.entities.event.model.error.ErrorComponent errorComponent) {
        logger.debug("[[[[[[[[[[    onErrorComponent      ]]]]]]]]]]");
        String msg = String.format("errorComponent: [%s]", errorComponent);
        logger.debug(msg);
    }

    @Override
    public void onDialogProviderAbort(com.rodan.intruder.ss7.entities.event.model.error.DialogProviderAbort abort) {
        logger.debug("[[[[[[[[[[    onDialogProviderAbort      ]]]]]]]]]]");
        String msg = String.format("abort: [%s]", abort);
        logger.debug(msg);
    }

    @Override
    public void onDialogUserAbort(DialogUserAbort abort) {
        logger.debug("[[[[[[[[[[    onDialogUserAbort      ]]]]]]]]]]");
        String msg = String.format("onDialogUserAbort: [%s]", abort);
        logger.debug(msg);
    }

    @Override
    public void onDialogReject(com.rodan.intruder.ss7.entities.event.model.error.DialogReject reject) {
        logger.debug("[[[[[[[[[[    onDialogReject      ]]]]]]]]]]");
        String msg = String.format("reject: [%s]", reject);
        logger.debug(msg);
    }
}
