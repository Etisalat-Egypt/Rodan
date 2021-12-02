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

package com.rodan.intruder.ss7.entities.event.service;

import com.rodan.intruder.ss7.entities.event.model.mobility.PurgeMsResponse;
import com.rodan.library.model.error.SystemException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public interface MapMobilityServiceListener extends MapServiceListener {
    Logger logger = LogManager.getLogger(MapMobilityServiceListener.class);

    default void onCancelLocationResponse(com.rodan.intruder.ss7.entities.event.model.mobility.ClResponse response) {
        logger.debug("[[[[[[[[[[    onCancelLocationResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    default void onDeleteSubscriberDataResponse(com.rodan.intruder.ss7.entities.event.model.mobility.DsdResponse response) {
        logger.debug("[[[[[[[[[[    onDeleteSubscriberDataResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    default void onPurgeMSResponse(PurgeMsResponse response) {
        logger.debug("[[[[[[[[[[    onPurgeMSResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    default void onInsertSubscriberDataResponse(com.rodan.intruder.ss7.entities.event.model.mobility.IsdResponse response) {
        logger.debug("[[[[[[[[[[    onInsertSubscriberDataResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    default void onInsertSubscriberDataRequest(com.rodan.intruder.ss7.entities.event.model.mobility.IsdRequest request) {
        logger.debug("[[[[[[[[[[    onInsertSubscriberDataRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    default void onSendIdentificationResponse(com.rodan.intruder.ss7.entities.event.model.mobility.SendIdResponse response) {
        logger.debug("[[[[[[[[[[    onSendIdentificationResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    default void onSendAuthenticationInfoRequest(com.rodan.intruder.ss7.entities.event.model.mobility.SaiRequest request) {
        logger.debug("[[[[[[[[[[    onSendAuthenticationInfoRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    default void onSendAuthenticationInfoResponse(com.rodan.intruder.ss7.entities.event.model.mobility.SaiResponse response) {
        logger.debug("[[[[[[[[[[    onSendAuthenticationInfoResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    default void onUpdateLocationResponse(com.rodan.intruder.ss7.entities.event.model.mobility.UlResponse response) throws SystemException {
        logger.debug("[[[[[[[[[[    onUpdateLocationResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    default void onAnyTimeInterrogationRequest(com.rodan.intruder.ss7.entities.event.model.mobility.AtiRequest request) throws SystemException {
        logger.debug("[[[[[[[[[[    onAnyTimeInterrogationRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    default void onAnyTimeInterrogationResponse(com.rodan.intruder.ss7.entities.event.model.mobility.AtiResponse response) throws SystemException {
        logger.debug("[[[[[[[[[[    onAnyTimeInterrogationResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    default void onProvideSubscriberInfoRequest(com.rodan.intruder.ss7.entities.event.model.mobility.PsiRequest request) {
        logger.debug("[[[[[[[[[[    onProvideSubscriberInfoRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    default void onProvideSubscriberInfoResponse(com.rodan.intruder.ss7.entities.event.model.mobility.PsiResponse response) {
        logger.debug("[[[[[[[[[[    onProvideSubscriberInfoResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    default void onCancelLocationRequest(com.rodan.intruder.ss7.entities.event.model.mobility.ClRequest request) {
        logger.debug("[[[[[[[[[[    onCancelLocationRequest      ]]]]]]]]]]");
        logger.debug(request);
    }
}
