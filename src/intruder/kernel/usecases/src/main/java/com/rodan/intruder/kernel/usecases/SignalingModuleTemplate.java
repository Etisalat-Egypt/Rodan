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

package com.rodan.intruder.kernel.usecases;

import com.rodan.intruder.kernel.entities.payload.SignalingPayload;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.ApplicationException;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.error.ValidationException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.intruder.kernel.usecases.model.ModuleOptions;
import com.rodan.intruder.kernel.usecases.model.ModuleResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class SignalingModuleTemplate<MOD extends ModuleOptions, PL extends SignalingPayload>
        implements SignalingModule {
    protected static final int DEFAULT_TASK_CHECK_INTERVAL = 500;
    protected static final int DEFAULT_TASK_WAIT_TIME = 6 * 1000;
    protected static final int MAX_TASK_WAIT_TIME = 2 * 60 * 1000;

    final static Logger logger = LogManager.getLogger(SignalingModuleTemplate.class);

    @Getter(AccessLevel.PROTECTED) protected MOD moduleOptions; // TODO: Make private
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) private PL mainPayload;
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) private PL currentPayload;

    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) protected ModuleResponse moduleResponse; // TODO: Make private
    @Getter(AccessLevel.PROTECTED) private List<BiConsumer<String, NotificationType>> notificationListeners;

    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) private boolean resultReceived;
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) private boolean executionError;
    @Getter(AccessLevel.PROTECTED) private boolean waitForResponse;
    @Getter(AccessLevel.PROTECTED) private int taskWaitTime;
    @Getter(AccessLevel.PROTECTED) private int taskCheckInterval;
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) private String waitForResponseFailedMessage;

    public SignalingModuleTemplate(MOD moduleOptions, boolean waitForResponse) {
        this.moduleOptions = moduleOptions;
        this.waitForResponse = waitForResponse;

        this.resultReceived = false;
        this.executionError = false;
        this.notificationListeners = new ArrayList<>();
        this.taskWaitTime = DEFAULT_TASK_WAIT_TIME;
        this.taskCheckInterval = DEFAULT_TASK_CHECK_INTERVAL; // TODO: read from a hidden module options
        this.waitForResponseFailedMessage = "Request timeout!";
    }

    @Override
    public ModuleResponse run() throws ApplicationException {
        try {
            logStart();
            validateOptions();
            initModule();
            execute();
            if (waitForResponse) {
                waitForResponse();
            }

            if (!executionError) {
                postExecuteAction();
                logEnd();
            }

            return moduleResponse;

        } catch (ApplicationException e) {
            logger.error("Failed to run module: " + e.getMessage(), e);
            throw e;

        } finally {
            try {
                cleanup();

            } catch (Exception e) {
                logger.error("Failed to cleanup module: " + e.getMessage(), e);
                throw e; // TODO: Don't throw exception, find a bettwe way
            }
        }
    }

    @Override
    public void addNotificationListener(BiConsumer<String, NotificationType> listener) {
        notificationListeners.add(listener);
    }

//    protected abstract void logStart();


    protected void logStart() {
        var moduleName = getClass().getAnnotation(Module.class).name();
        var payloadName = moduleName.substring(moduleName.lastIndexOf("/") + 1);
        var msg = String.format("Sending %s message.", payloadName);
        logger.debug(msg);
    }

    protected void validateOptions() throws ValidationException {
        logger.debug("Validating module options");
        this.moduleOptions.validate();
    }

    protected void initModule() throws ApplicationException {
        logger.debug("Initializing module");
        generatePayload();
        prepareStack();
        // Diameter: Listeners should be added before generating any session/carrier
        addServiceListener();
    }

    protected abstract void generatePayload();

    protected abstract void prepareStack() throws ApplicationException;

    protected abstract void addServiceListener() throws SystemException;

    protected abstract void execute() throws ApplicationException;

    protected abstract void waitForResponse() throws SystemException;

    protected void postExecuteAction() throws SystemException {
        logger.debug("No post execution action.");
    }

    protected void logEnd() {
        var name = getClass().getAnnotation(Module.class).name();
        name = name.substring(name.lastIndexOf("/") + 1);
        String msg = String.format("%s response received successfully.\nReceived response: %s", name, getModuleResponse());
        logger.debug(msg);
    }

    protected abstract void cleanup() throws SystemException;

    protected void setTaskWaitTime(int time) {
        if (time > MAX_TASK_WAIT_TIME) {
            var msg = "Wait time exceeded maximum allowed time: " + time + ". Limiting time to: " + MAX_TASK_WAIT_TIME;
            logger.warn(msg);
            time = MAX_TASK_WAIT_TIME;
        }
        this.taskWaitTime = time;
    }

    protected void notify(String msg, NotificationType type) {
        switch (type) {
            case FAILURE -> logger.error(msg);
            case WARNING -> logger.warn(msg);
            default -> logger.info(msg);
        }

        for (var listener : getNotificationListeners()) {
            listener.accept(msg, type);
        }
    }
}
