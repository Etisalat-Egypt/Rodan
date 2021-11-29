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

import com.rodan.intruder.diameter.entities.event.DiameterAppListener;
import com.rodan.intruder.diameter.entities.event.model.ErrorEvent;
import com.rodan.intruder.diameter.entities.event.model.ResultCode;
import com.rodan.intruder.diameter.entities.payload.DiameterPayload;
import com.rodan.intruder.diameter.entities.session.DiameterSession;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleOptions;
import com.rodan.intruder.diameter.usecases.port.DiameterGateway;
import com.rodan.intruder.kernel.usecases.SignalingModuleTemplate;
import com.rodan.library.model.error.ApplicationException;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.library.util.LongRunningTask;
import com.rodan.library.util.Util;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class DiameterModuleTemplate extends SignalingModuleTemplate<DiameterModuleOptions, DiameterPayload>
        implements SignalingModule, DiameterAppListener {
    final static Logger logger = LogManager.getLogger(DiameterModuleTemplate.class);
    @Getter(AccessLevel.PROTECTED) private DiameterGateway gateway;
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) private DiameterSession session;

    public DiameterModuleTemplate(DiameterGateway gateway, DiameterModuleOptions moduleOptions) {
        super(moduleOptions, true);
        this.gateway = gateway;
    }

    // TODO move prepareStack to SignalingModuleTemplate and remove it from Diameter & SS7 classes
    protected void prepareStack() throws ApplicationException {
        logger.debug("Preparing stack");
        if (!gateway.isConnected()) {
            for (var listener : getNotificationListeners()) {
                gateway.addNotificationListener(listener);
            }
            gateway.connect();
        }
    }

    @Override
    protected void execute() throws ApplicationException {
        logger.debug("currentPayload: " + getCurrentPayload());
        var payloadName = getCurrentPayload().getPayloadName();
        var shouldDisplayNotifications = !(this instanceof DiameterBruteforceModuleTemplate);
        if (shouldDisplayNotifications)
            notify("Sending " + payloadName + " message.", NotificationType.PROGRESS);
        var session = this.getGateway().generateSession(getMainPayload());
        logger.debug("sessionId: " + session.getSessionId());
        setSession(session);
        gateway.send(getSession(), getCurrentPayload());
        logger.debug(payloadName + " message send successfully!");
    }

    @Override
    protected void waitForResponse() throws SystemException {
        var responseWaitTask = LongRunningTask.builder()
                .workStartMessage("Waiting for server response...").workWaitMessage(null)
                .workDoneMessage("Response received successfully")
                .workFailedMessage(getWaitForResponseFailedMessage())
                .workDoneCheck(m -> isResultReceived() || isExecutionError())
                .startWorkAction(null)
                .waitTime(getTaskWaitTime()).checkInterval(getTaskCheckInterval())
                .build();
        Util.startLongRunningTask(responseWaitTask);
    }

    @Override
    public void onFailedResultCode(ResultCode resultCode) {
        var msg = "Received failed result code: " + resultCode;
        logger.error(msg);
        notify(msg, NotificationType.FAILURE);
        setExecutionError(true);
    }

    @Override
    public void onMessageHandlingError(ErrorEvent errorEvent) {
        logger.error(errorEvent.getMessage());
        notify(errorEvent.getMessage(), NotificationType.FAILURE);
        setExecutionError(true);
    }
}
