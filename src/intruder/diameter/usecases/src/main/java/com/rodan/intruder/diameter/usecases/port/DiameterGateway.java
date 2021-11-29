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

package com.rodan.intruder.diameter.usecases.port;

import com.rodan.intruder.diameter.entities.event.S6aListener;
import com.rodan.intruder.diameter.entities.payload.DiameterPayload;
import com.rodan.intruder.diameter.entities.session.DiameterSession;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;

import java.util.function.BiConsumer;

public interface DiameterGateway {
    void connect() throws SystemException;
    boolean isConnected();
    void send(DiameterSession session, DiameterPayload payload) throws SystemException;
    void addS6aListener(S6aListener listener) throws SystemException;
    void removeS6aListener(S6aListener listener) throws SystemException;
    DiameterSession generateSession(DiameterPayload payload) throws SystemException;
    void addNotificationListener(BiConsumer<String, NotificationType> listener);
    void removeNotificationListener(BiConsumer<String, NotificationType> listener);
}
