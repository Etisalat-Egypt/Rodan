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

package com.rodan.intruder.diameter.entities.event;

import com.rodan.intruder.diameter.entities.event.model.ErrorEvent;
import com.rodan.intruder.diameter.entities.event.model.ResultCode;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public interface DiameterAppListener {
    Logger logger = LogManager.getLogger(DiameterAppListener.class);

    default void onFailedResultCode(ResultCode resultCode)  {
        logger.debug("[[[[[[[[[[    onFailedResultCode      ]]]]]]]]]]");
        String msg = String.format("onFailedResultCode received with content: %s", resultCode);
        logger.debug(msg);
    }

    default void onMessageHandlingError(ErrorEvent errorEvent)  {
        logger.debug("[[[[[[[[[[    onMessageHandlingError      ]]]]]]]]]]");
        String msg = String.format("onMessageHandlingError received with content: %s", errorEvent.getMessage());
        logger.debug(msg);
    }
}
