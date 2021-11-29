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

package com.rodan.intruder.diameter.gateway.handler;

import com.rodan.intruder.diameter.entities.event.S6aListener;
import com.rodan.intruder.diameter.entities.event.model.ResultCode;
import com.rodan.intruder.diameter.gateway.handler.model.ErrorEventImpl;
import com.rodan.intruder.diameter.gateway.handler.model.ResultCodeImpl;
import lombok.AccessLevel;
import lombok.Getter;
import net.java.slee.resource.diameter.base.events.avp.ExperimentalResultAvp;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.slee.resource.diameter.base.events.DiameterMessageImpl;

import java.util.ArrayList;
import java.util.List;

public abstract class S6aEventHandler {
    private static final Logger logger = LogManager.getLogger(S6aEventHandler.class);

    @Getter(AccessLevel.PROTECTED) private List<S6aListener> listeners;

    public S6aEventHandler() {
        listeners = new ArrayList<>();
    }

    public void addListener(S6aListener listener) {
        if (listeners.contains(listener)) {
            logger.warn("Registering S6aClientListener for already existing one");
            return;
        }

        listeners.add(listener);
    }

    public void removeListener(S6aListener listener) {
        if (!listeners.contains(listener)) {
            logger.warn("Removing a non-existing S6aClientListener");
            return;
        }

        listeners.remove(listener);
    }

    protected static boolean isSuccessResultCode(ResultCode resultCode) {
        Long code = (resultCode.getResultCode() != null) ? resultCode.getResultCode() : resultCode.getExperimentalResultCode();
        return code >= 2000L && code < 3000L;
    }

    protected ResultCode parseResultCode(DiameterMessageImpl message, ExperimentalResultAvp experimentalResult) {
        var originHost = (message.getOriginHost() != null) ? message.getOriginHost().toString() : "";
        Long resultCode = message.hasResultCode() ? message.getResultCode() : null;
        Long experimentalResultCode = null;
        if (experimentalResult != null) {
            experimentalResultCode = experimentalResult.hasExperimentalResultCode() ?
                    experimentalResult.getExperimentalResultCode() : null;
        }

        return ResultCodeImpl.builder()
                .originHost(originHost).resultCode(resultCode).experimentalResultCode(experimentalResultCode)
                .build();
    }

    protected void handleError(ResultCode resultCode) {
        var msg = String.format("Failed Diameter result code received. resultCode: [%d], experimentalResultCode[%d]",
                resultCode.getResultCode(), resultCode.getExperimentalResultCode());
        logger.error(msg);
        for (var listener : listeners) {
            listener.onFailedResultCode(resultCode);
        }
    }
}
