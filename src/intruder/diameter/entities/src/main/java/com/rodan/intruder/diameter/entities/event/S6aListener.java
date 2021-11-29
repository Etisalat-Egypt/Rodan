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

import com.rodan.intruder.diameter.entities.event.model.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public interface S6aListener extends DiameterAppListener {
    Logger logger = LogManager.getLogger(S6aListener.class);

    // TODO pass DiameterSession object instead of sessionId
    default void doUpdateLocationAnswerEvent(UpdateLocationAnswer answer)  {
        logger.debug("[[[[[[[[[[    doUpdateLocationAnswerEvent      ]]]]]]]]]]");
        String msg = String.format("doUpdateLocationAnswerEvent received with content: %s", answer);
        logger.debug(msg);
    }

    default void doCancelLocationRequestEvent(CancelLocationRequest request)  {
        logger.debug("[[[[[[[[[[    doCancelLocationRequestEvent      ]]]]]]]]]]");
        String msg = String.format("doCancelLocationRequestEvent received with content: %s", request);
        logger.debug(msg);
    }

    default void doAuthenticationInformationAnswerEvent(AuthenticationInformationAnswer answer)  {
        logger.debug("[[[[[[[[[[    doAuthenticationInformationAnswerEvent      ]]]]]]]]]]");
        String msg = String.format("doAuthenticationInformationAnswerEvent received with content: %s", answer);
        logger.debug(msg);
    }

    default void doInsertSubscriberDataAnswerEvent(InsertSubscriberDataAnswer answer)  {
        logger.debug("[[[[[[[[[[    doInsertSubscriberDataAnswerEvent      ]]]]]]]]]]");
        String msg = String.format("doInsertSubscriberDataAnswerEvent received with content: %s", answer);
        logger.debug(msg);
    }

    default void doNotifyAnswerEvent(NotifyAnswer answer)  {
        logger.debug("[[[[[[[[[[    doNotifyAnswerEvent      ]]]]]]]]]]");
        String msg = String.format("doNotifyAnswerEvent received with content: %s", answer);
        logger.debug(msg);
    }

    default void doPurgeUEAnswerEvent(PurgeUeAnswer answer)  {
        logger.debug("[[[[[[[[[[    doPurgeUEAnswerEvent      ]]]]]]]]]]");
        String msg = String.format("doPurgeUEAnswerEvent received with content: %s", answer);
        logger.debug(msg);
    }

    default void doCancelLocationAnswerEvent(CancelLocationAnswer answer)  {
        logger.debug("[[[[[[[[[[    doCancelLocationAnswerEvent      ]]]]]]]]]]");
        String msg = String.format("doCancelLocationAnswerEvent received with content: %s", answer);
        logger.debug(msg);
    }
}
