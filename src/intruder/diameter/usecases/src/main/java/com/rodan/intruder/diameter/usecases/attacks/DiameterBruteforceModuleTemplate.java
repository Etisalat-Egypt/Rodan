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

package com.rodan.intruder.diameter.usecases.attacks;

import com.rodan.intruder.diameter.entities.event.model.DiameterMessage;
import com.rodan.intruder.diameter.entities.event.model.ErrorEvent;
import com.rodan.intruder.diameter.entities.event.model.ResultCode;
import com.rodan.intruder.diameter.entities.payload.DiameterPayload;
import com.rodan.intruder.diameter.entities.payload.s6a.IdrPayload;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleOptions;
import com.rodan.intruder.diameter.usecases.port.DiameterGateway;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.util.IteratorWithProgress;
import com.rodan.intruder.kernel.usecases.model.ModuleResponse;
import com.rodan.library.model.Constants;
import com.rodan.library.model.error.ApplicationException;
import com.rodan.library.model.notification.NotificationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public abstract class DiameterBruteforceModuleTemplate extends DiameterModuleTemplate {
    final static Logger logger = LogManager.getLogger(DiameterBruteforceModuleTemplate.class);

    private static final int REPORT_PROGRESS_AT = 10;

    private int lastReportedProgress;
    @Setter(AccessLevel.PROTECTED) private Integer bruteforceDelay;
    @Getter(AccessLevel.PROTECTED) Map<String, DiameterPayload> sentPayloads;
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) private IteratorWithProgress<DiameterPayload> payloadIterator;

    public DiameterBruteforceModuleTemplate(DiameterGateway gateway, DiameterModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
        this.sentPayloads = new HashMap<>();
        this.lastReportedProgress = -1;
    }

    @Override
    public ModuleResponse run() throws ApplicationException {
        try {
            logStart();
            validateOptions();
            initModule();

            notify("Brute forcing...", NotificationType.PROGRESS);
            var payloadIterator = this.payloadIterator;
            while (payloadIterator.hasNext() && !isResultReceived()) {
                var payload = payloadIterator.next();
                logger.debug("Sending payload: " + payload);
                setCurrentPayload(payload);
                execute(); // Signaling messages are sent in Async manner
                storeSentPayload();
                delay();
                reportProgress();
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
        var sessionId = getSession().getSessionId();
        sentPayloads.put(sessionId, getCurrentPayload());
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
    public void onMessageHandlingError(ErrorEvent errorEvent) {
        logger.debug("[[[[[[[[[[    onMessageHandlingError      ]]]]]]]]]]");
        var msg = String.format("errorEvent: [%s]", errorEvent);
        logger.debug(msg);
    }

    @Override
    public void onFailedResultCode(ResultCode resultCode) {
        logger.debug("[[[[[[[[[[    onFailedResultCode      ]]]]]]]]]]");
        var msg = String.format("resultCode: [%s]", resultCode);
        logger.debug(msg);
    }

    protected DiameterPayload getCorrespondingPayload(DiameterMessage message) throws SystemException {
        var correspondingPayload = getSentPayloads().get(message.getSessionId());
        if (correspondingPayload == null) {
            var msg = "No corresponding payload found for session ID: " + message.getSessionId();
            logger.error(msg);
            throw SystemException.builder().code(ErrorCode.MISSING_PAYLOAD).message(msg).build();
        }

        return correspondingPayload;
    }

    protected static boolean isPossibleValidNodeResultCode(ResultCode resultCode) {
        return resultCode != null && resultCode.getResultCode() != null &&
                resultCode.getResultCode() != Constants.DIAMETER_ERROR_UNABLE_TO_DELIVER;
    }

    protected static boolean isUnknownUserResultCode(ResultCode resultCode) {
        return resultCode.getResultCode() == Constants.DIAMETER_ERROR_USER_UNKNOWN;
    }

    private void reportProgress() {
        var progress = (int) payloadIterator.getProgressPercentage();
        if (progress % REPORT_PROGRESS_AT == 0 && progress != lastReportedProgress) {
            notify("Progress: " + progress + "%", NotificationType.PROGRESS);
            lastReportedProgress = progress;
        }
    }
}
