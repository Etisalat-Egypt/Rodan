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

package com.rodan.connectivity.ss7.handler;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.api.Association;
import org.mobicents.protocols.api.ManagementEventListener;
import org.mobicents.protocols.api.Server;

public interface SctpEventHandler extends ManagementEventListener {
    Logger logger = LogManager.getLogger(SctpEventHandler.class);

    default void onServiceStarted() {
        logger.debug("[[[[[[[[[[    onServiceStarted      ]]]]]]]]]]");
    }

    default void onServiceStopped() {
        logger.debug("[[[[[[[[[[    onServiceStopped      ]]]]]]]]]]");
    }

    default void onRemoveAllResources() {
        logger.debug("[[[[[[[[[[    onRemoveAllResources      ]]]]]]]]]]");
    }

    default void onServerAdded(Server server) {
        logger.debug("[[[[[[[[[[    onServerAdded      ]]]]]]]]]]");
        if (server != null) {
            logger.debug(String.format("Server name=%s", server.getName()));
        }
    }

    default void onServerRemoved(Server server) {
        logger.debug("[[[[[[[[[[    onServerRemoved      ]]]]]]]]]]");
        if (server != null) {
            logger.debug(String.format("Server name=%s", server.getName()));
        }
    }

    default void onAssociationAdded(Association association) {
        logger.debug("[[[[[[[[[[    onAssociationAdded      ]]]]]]]]]]");
        if (association != null) {
            logger.debug(String.format("SCTP AssociationUp name=%s peer=%s", association.getName(), association.getPeerAddress()));
        }
    }

    default void onAssociationRemoved(Association association) {
        logger.debug("[[[[[[[[[[    onAssociationRemoved      ]]]]]]]]]]");
        if (association != null) {
            logger.debug(String.format("SCTP AssociationUp name=%s peer=%s", association.getName(), association.getPeerAddress()));
        }
    }

    default void onAssociationStarted(Association association) {
        logger.debug("[[[[[[[[[[    onAssociationStarted      ]]]]]]]]]]");
        if (association != null) {
            logger.debug(String.format("SCTP AssociationStarted name=%s peer=%s", association.getName(), association.getPeerAddress()));
        }
    }

    default void onAssociationStopped(Association association) {
        logger.debug("[[[[[[[[[[    onAssociationStopped      ]]]]]]]]]]");
        if (association != null) {
            logger.debug(String.format("SCTP AssociationUp name=%s peer=%s", association.getName(), association.getPeerAddress()));
        }
    }

    default void onAssociationUp(Association association) {
        logger.debug("[[[[[[[[[[    onAssociationUp      ]]]]]]]]]]");
        if (association != null) {
            logger.debug(String.format("SCTP AssociationUp name=%s peer=%s", association.getName(), association.getPeerAddress()));
        }
    }

    default void onAssociationDown(Association association) {
        logger.debug("[[[[[[[[[[    onAssociationDown      ]]]]]]]]]]");
        if (association != null) {
            logger.debug(String.format("SCTP AssociationDown name=%s peer=%s", association.getName(), association.getPeerAddress()));
        }
    }
}
