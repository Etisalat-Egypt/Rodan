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

package com.rodan.library.util;

import lombok.Builder;

import java.util.Objects;
import java.util.function.Function;

public class LongRunningTask {
    public final int waitTime;
    public final int checkInterval;
    public final VoidFunction startWorkAction;
    public final VoidFunction workWaitAction;
    public final Function<Void, Boolean> workDoneCheck;
    public final String workStartMessage;
    public final String workWaitMessage;
    public final String workDoneMessage;
    public final String workFailedMessage;
    public final int workFailedErrorCode;
    public final Boolean throwExceptionOnFailure; // TODO: Set value on all usages

    @Builder
    public LongRunningTask(int waitTime, int checkInterval, VoidFunction startWorkAction, VoidFunction workWaitAction,
                           Function<Void, Boolean> workDoneCheck, String workStartMessage, String workWaitMessage,
                           String workDoneMessage, String workFailedMessage, int workFailedErrorCode,
                           Boolean throwExceptionOnFailure) {
        this.waitTime = waitTime;
        this.checkInterval = checkInterval;
        this.startWorkAction = startWorkAction;
        this.workWaitAction = workWaitAction;
        this.workDoneCheck = workDoneCheck;
        this.workStartMessage = workStartMessage;
        this.workWaitMessage = workWaitMessage;
        this.workDoneMessage = workDoneMessage;
        this.workFailedMessage = workFailedMessage;
        this.workFailedErrorCode = workFailedErrorCode;
        this.throwExceptionOnFailure = Objects.requireNonNullElse(throwExceptionOnFailure, true);
    }
}
