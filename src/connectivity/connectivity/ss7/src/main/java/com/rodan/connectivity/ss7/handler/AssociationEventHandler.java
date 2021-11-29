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
import org.mobicents.protocols.api.AssociationListener;
import org.mobicents.protocols.api.PayloadData;

public interface AssociationEventHandler extends AssociationListener {
    // TODO: extend Mobicents class for proper handling and add
    // the listened after management start, to avoid "association already up issue"
    Logger logger = LogManager.getLogger(AssociationEventHandler.class);

    default void onCommunicationUp(Association association, int maxInboundStreams, int maxOutboundStreams) {
        logger.debug("[[[[[[[[[[    onCommunicationUp      ]]]]]]]]]]");
        logger.debug(String.format("SCTP Association name=%s peer=%s", association.getName(), association.getPeerAddress()));
    }

    default void onCommunicationShutdown(Association association) {
        logger.debug("[[[[[[[[[[    onCommunicationShutdown      ]]]]]]]]]]");
        logger.debug(String.format("SCTP Association name=%s peer=%s", association.getName(), association.getPeerAddress()));
    }

    default void onCommunicationLost(Association association) {
        logger.debug("[[[[[[[[[[    onCommunicationLost      ]]]]]]]]]]");
        logger.debug(String.format("SCTP Association name=%s peer=%s", association.getName(), association.getPeerAddress()));
    }

    default void onCommunicationRestart(Association association) {
        logger.debug("[[[[[[[[[[    onCommunicationRestart      ]]]]]]]]]]");
        logger.debug(String.format("SCTP Association name=%s peer=%s", association.getName(), association.getPeerAddress()));
    }

    default void onPayload(Association association, PayloadData payloadData) {
        logger.debug("[[[[[[[[[[    onPayload      ]]]]]]]]]]");
        logger.debug(String.format("SCTP Association name=%s peer=%s", association.getName(), association.getPeerAddress()));
        logger.debug(String.format("SCTP Payload protocolId=%s dataLength=%s", payloadData.getPayloadProtocolId(), payloadData.getDataLength()));
    }

    default void inValidStreamId(PayloadData payloadData) {
        logger.debug("[[[[[[[[[[    inValidStreamId      ]]]]]]]]]]");
        logger.debug(String.format("SCTP Payload protocolId=%s dataLength=%s", payloadData.getPayloadProtocolId(), payloadData.getDataLength()));
    }
}
