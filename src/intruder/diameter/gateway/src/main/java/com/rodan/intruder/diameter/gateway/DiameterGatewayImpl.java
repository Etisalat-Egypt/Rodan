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

package com.rodan.intruder.diameter.gateway;

import com.rodan.connectivity.diameter.JDiameterStackAdapter;
import com.rodan.intruder.diameter.entities.event.S6aListener;
import com.rodan.intruder.diameter.entities.payload.DiameterPayload;
import com.rodan.intruder.diameter.entities.payload.s6a.*;
import com.rodan.intruder.diameter.entities.session.DiameterSession;
import com.rodan.intruder.diameter.gateway.adapter.JDiameterS6aSession;
import com.rodan.intruder.diameter.gateway.adapter.JDiameterSession;
import com.rodan.intruder.diameter.gateway.handler.S6aClientEventHandler;
import com.rodan.intruder.diameter.gateway.handler.S6aServerEventHandler;
import com.rodan.intruder.diameter.usecases.port.DiameterGateway;
import com.rodan.library.model.config.node.config.IntruderNodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import lombok.Builder;
import lombok.ToString;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdiameter.api.s6a.ClientS6aSession;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@ToString
public class DiameterGatewayImpl implements DiameterGateway {
    final static Logger logger = LogManager.getLogger(DiameterGatewayImpl.class);

    private JDiameterStackAdapter adapter;
    private S6aClientEventHandler s6aClientEventHandler;
    private S6aServerEventHandler s6aServerEventHandler;
    private boolean connected;

//    @Getter private ApplicationId authAppId;

    private List<BiConsumer<String, NotificationType>> notificationListeners;

    @Builder
    public DiameterGatewayImpl(IntruderNodeConfig nodeConfig) {
        this.s6aClientEventHandler = new S6aClientEventHandler();
        this.s6aServerEventHandler = new S6aServerEventHandler();
        this.adapter = JDiameterStackAdapter.builder()
                .s6aClientEventListener(this.s6aClientEventHandler).s6aServerEventListener(this.s6aServerEventHandler)
                .nodeConfig(nodeConfig)
                .build();
        this.notificationListeners = new ArrayList<>();
    }

    @Override
    public void connect() throws SystemException {
        if (connected) {
            var msg = "Node is already connected";
            throw SystemException.builder().code(ErrorCode.DIAMETER_CONNECTION_INITIALIZATION).message(msg).build();
        }

        var peerAddress = adapter.getPeerAddress();
        notify("Connecting to Diameter network via " + peerAddress + "...", NotificationType.PROGRESS);
        adapter.initStack();
        adapter.startStack();
        connected = true;
        notify("Connected!", NotificationType.SUCCESS);
//
//        startStack();
//        connected = true;
//        notify("Connected!", NotificationType.SUCCESS);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void send(DiameterSession session, DiameterPayload payload) throws SystemException {
        ((JDiameterSession) session).send(payload);
    }

    @Override
    public void addS6aListener(S6aListener listener) throws SystemException {
        s6aClientEventHandler.addListener(listener);
        s6aServerEventHandler.addListener(listener);
    }

    @Override
    public void removeS6aListener(S6aListener listener) throws SystemException {
        s6aClientEventHandler.removeListener(listener);
        s6aServerEventHandler.removeListener(listener);
    }

    @Override
    public DiameterSession generateSession(DiameterPayload payload) throws SystemException {
        var session = this.adapter.generateSession();
        var clientSession = this.adapter.generateS6aClientSession();
        var serverSession = this.adapter.generateS6aServerSession();
        return JDiameterS6aSession.builder().appId(this.adapter.getAuthAppId())
                .messageFactory(this.adapter.getS6aSessionFactory().getMessageFactory()).session(session)
                .clientAppSession(clientSession).serverAppSession(serverSession)
                .build();
    }

    @Override
    public void addNotificationListener(BiConsumer<String, NotificationType> listener) {
        if (notificationListeners.contains(listener)) {
            logger.warn("Registering NotificationListener for already existing one");
            return;
        }

        notificationListeners.add(listener);
    }

    @Override
    public void removeNotificationListener(BiConsumer<String, NotificationType> listener) {
        if (!notificationListeners.contains(listener)) {
            logger.warn("Removing a non-existing NotificationListener");
            return;
        }

        notificationListeners.remove(listener);
    }

    private void notify(String msg, NotificationType type) {
        logger.info(msg);
        for (var listener : notificationListeners) {
            listener.accept(msg, type);
        }
    }
}
