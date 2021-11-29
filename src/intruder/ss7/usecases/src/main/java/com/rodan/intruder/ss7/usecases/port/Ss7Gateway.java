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

package com.rodan.intruder.ss7.usecases.port;

import com.rodan.intruder.ss7.entities.dialog.Ss7CapDialog;
import com.rodan.intruder.ss7.entities.dialog.Ss7MapDialog;
import com.rodan.intruder.ss7.entities.event.dialog.CapDialogEventListener;
import com.rodan.intruder.ss7.entities.event.dialog.MapDialogEventListener;
import com.rodan.intruder.ss7.entities.event.model.MapMessage;
import com.rodan.intruder.ss7.entities.event.model.error.details.ReturnErrorProblemType;
import com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener;
import com.rodan.intruder.ss7.entities.payload.Ss7Payload;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;

import java.util.function.BiConsumer;

public interface Ss7Gateway {
    void connect() throws SystemException;
    boolean isConnected();
    Ss7MapDialog generateMapDialog(Ss7Payload payload) throws SystemException;
    Ss7CapDialog generateCapDialog(Ss7Payload payload) throws SystemException;
    void addToDialog(Ss7Payload payload, Ss7MapDialog dialog) throws SystemException;
    void addToDialog(Ss7Payload payload, Ss7CapDialog dialog) throws SystemException;
    Ss7Payload generateForwarderPayload(MapMessage message, Ss7Payload payload) throws SystemException;

    void send(Ss7MapDialog dialog) throws SystemException;
    void sendMalformedAcn(Ss7MapDialog dialog) throws SystemException;
    void sendRejectComponent(Ss7MapDialog dialog, long invokeId, ReturnErrorProblemType type) throws SystemException;
    void close(Ss7MapDialog dialog) throws SystemException; // TODO SS7: check if (boolean prearrangedEnd) is required
    void send(Ss7CapDialog dialog) throws SystemException;

    void addMapDialogEventListener(int ssn, MapDialogEventListener listener) throws SystemException;
    void removeMapDialogEventListener(int ssn, MapDialogEventListener listener) throws SystemException;
    void addCapDialogEventListener(int ssn, CapDialogEventListener listener) throws SystemException;
    void removeCapDialogEventListener(int ssn, CapDialogEventListener listener) throws SystemException;
    void addMobilityServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener listener) throws SystemException;
    void removeMobilityServiceListener(int ssn, MapMobilityServiceListener listener) throws SystemException;
    void addSmsServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapSmsServiceListener listener) throws SystemException;
    void removeSmsServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapSmsServiceListener listener) throws SystemException;
    void addOamServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapOamServiceListener listener) throws SystemException;
    void removeOamServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapOamServiceListener listener) throws SystemException;
    void addPdpServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapPdpServiceListener listener) throws SystemException;
    void removePdpServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapPdpServiceListener listener) throws SystemException;
    void addLcsServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapLcsServiceListener listener) throws SystemException;
    void removeLcsServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapLcsServiceListener listener) throws SystemException;
    void addCallHandlingServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapCallHandlingServiceListener listener) throws SystemException;
    void removeCallHandlingServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapCallHandlingServiceListener listener) throws SystemException;
    void addCapCsCallListener(int ssn, com.rodan.intruder.ss7.entities.event.service.CapCsCallListener listener) throws SystemException;
    void removeCapCsCallListener(int ssn, com.rodan.intruder.ss7.entities.event.service.CapCsCallListener listener) throws SystemException;

    void addNotificationListener(BiConsumer<String, NotificationType> listener);
    void removeNotificationListener(BiConsumer<String, NotificationType> listener);
}
