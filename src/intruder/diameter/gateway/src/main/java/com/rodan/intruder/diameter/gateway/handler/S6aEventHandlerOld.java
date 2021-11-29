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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdiameter.api.*;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.s6a.ClientS6aSession;
import org.jdiameter.api.s6a.ClientS6aSessionListener;
import org.jdiameter.api.s6a.events.*;

public interface S6aEventHandlerOld extends ClientS6aSessionListener, S6aListener {
    Logger logger = LogManager.getLogger(S6aEventHandlerOld.class);

    default void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        logger.debug("[[[[[[[[[[    doOtherEvent      ]]]]]]]]]]");
        String msg = String.format("doOtherEvent received for sessionAppId: [%s], destinationHost: [%s], " +
                        "destinationRealm: [%s]. originHost: [%s], originRealm: [%s], resultCodeAvp: [%s]",
                session.getSessionAppId(), S6aEventHandlerOld.getHostAvp(request), S6aEventHandlerOld.getRealmAvp(request),
                S6aEventHandlerOld.getHostAvp(answer), S6aEventHandlerOld.getRealmAvp(answer),
                S6aEventHandlerOld.getAnswerResultCode(answer));
        logger.debug(msg);
    }

    default void doCancelLocationRequestEvent(ClientS6aSession session, JCancelLocationRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        logger.debug("[[[[[[[[[[    doCancelLocationRequestEvent      ]]]]]]]]]]");
        String msg = String.format("doCancelLocationRequestEvent received for sessionAppId: [%s], destinationHost: [%s], destinationRealm: [%s]",
                session.getSessionAppId(), S6aEventHandlerOld.getHostAvp(request),
                S6aEventHandlerOld.getRealmAvp(request));
        logger.debug(msg);
    }

    default void doInsertSubscriberDataRequestEvent(ClientS6aSession session, JInsertSubscriberDataRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        logger.debug("[[[[[[[[[[    doInsertSubscriberDataRequestEvent      ]]]]]]]]]]");
        String msg = String.format("doInsertSubscriberDataRequestEvent received for sessionAppId: [%s], destinationHost: [%s], destinationRealm: [%s]",
                session.getSessionAppId(), S6aEventHandlerOld.getHostAvp(request),
                S6aEventHandlerOld.getRealmAvp(request));
        logger.debug(msg);
    }

    default void doDeleteSubscriberDataRequestEvent(ClientS6aSession session, JDeleteSubscriberDataRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        logger.debug("[[[[[[[[[[    doDeleteSubscriberDataRequestEvent      ]]]]]]]]]]");
        String msg = String.format("doDeleteSubscriberDataRequestEvent received for sessionAppId: [%s], destinationHost: [%s], destinationRealm: [%s]",
                session.getSessionAppId(), S6aEventHandlerOld.getHostAvp(request),
                S6aEventHandlerOld.getRealmAvp(request));
        logger.debug(msg);
    }

    default void doResetRequestEvent(ClientS6aSession session, JResetRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        logger.debug("[[[[[[[[[[    doResetRequestEvent      ]]]]]]]]]]");
        String msg = String.format("doResetRequestEvent received for sessionAppId: [%s], destinationHost: [%s], destinationRealm: [%s]",
                session.getSessionAppId(), S6aEventHandlerOld.getHostAvp(request),
                S6aEventHandlerOld.getRealmAvp(request));
        logger.debug(msg);
    }

    default void doAuthenticationInformationAnswerEvent(ClientS6aSession session, JAuthenticationInformationRequest request, JAuthenticationInformationAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        logger.debug("[[[[[[[[[[    doAuthenticationInformationAnswerEvent      ]]]]]]]]]]");
        String msg = String.format("doAuthenticationInformationAnswerEvent received for sessionAppId: [%s], destinationHost: [%s], " +
                        "destinationRealm: [%s]. originHost: [%s], originRealm: [%s], resultCodeAvp: [%s]",
                session.getSessionAppId(), S6aEventHandlerOld.getHostAvp(request), S6aEventHandlerOld.getRealmAvp(request),
                S6aEventHandlerOld.getHostAvp(answer), S6aEventHandlerOld.getRealmAvp(answer),
                S6aEventHandlerOld.getAnswerResultCode(answer));
        logger.debug(msg);
    }

    default void doPurgeUEAnswerEvent(ClientS6aSession session, JPurgeUERequest request, JPurgeUEAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        logger.debug("[[[[[[[[[[    doPurgeUEAnswerEvent      ]]]]]]]]]]");
        String msg = String.format("doPurgeUEAnswerEvent received for sessionAppId: [%s], destinationHost: [%s], " +
                        "destinationRealm: [%s]. originHost: [%s], originRealm: [%s], resultCodeAvp: [%s]",
                session.getSessionAppId(), S6aEventHandlerOld.getHostAvp(request), S6aEventHandlerOld.getRealmAvp(request),
                S6aEventHandlerOld.getHostAvp(answer), S6aEventHandlerOld.getRealmAvp(answer),
                S6aEventHandlerOld.getAnswerResultCode(answer));
        logger.debug(msg);
    }

    default void doUpdateLocationAnswerEvent(ClientS6aSession session, JUpdateLocationRequest request, JUpdateLocationAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        logger.debug("[[[[[[[[[[    doUpdateLocationAnswerEvent      ]]]]]]]]]]");
        String msg = String.format("doUpdateLocationAnswerEvent received for sessionAppId: [%s], destinationHost: [%s], " +
                        "destinationRealm: [%s]. originHost: [%s], originRealm: [%s], resultCodeAvp: [%s]",
                session.getSessionAppId(), S6aEventHandlerOld.getHostAvp(request), S6aEventHandlerOld.getRealmAvp(request),
                S6aEventHandlerOld.getHostAvp(answer), S6aEventHandlerOld.getRealmAvp(answer),
                S6aEventHandlerOld.getAnswerResultCode(answer));
        logger.debug(msg);
    }

    default void doNotifyAnswerEvent(ClientS6aSession session, JNotifyRequest request, JNotifyAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        logger.debug("[[[[[[[[[[    doNotifyAnswerEvent      ]]]]]]]]]]");
        String msg = String.format("doNotifyAnswerEvent received for sessionAppId: [%s], destinationHost: [%s], " +
                        "destinationRealm: [%s]. originHost: [%s], originRealm: [%s], resultCodeAvp: [%s]",
                session.getSessionAppId(), S6aEventHandlerOld.getHostAvp(request), S6aEventHandlerOld.getRealmAvp(request),
                S6aEventHandlerOld.getHostAvp(answer), S6aEventHandlerOld.getRealmAvp(answer),
                S6aEventHandlerOld.getAnswerResultCode(answer));
        logger.debug(msg);
    }

    private static String getHostAvp(AppRequestEvent request) {
        var host = "";
        try {
            host = request.getDestinationHost();
        } catch (AvpDataException e) {
        }

        return host;
    }

    private static String getHostAvp(AppAnswerEvent answer) {
        var host = "";
        try {
            host = answer.getOriginHost();
        } catch (AvpDataException e) {
        }

        return host;
    }

    private static String getRealmAvp(AppRequestEvent request) {
        var realm = "";
        try {
            realm = request.getDestinationRealm();
        } catch (AvpDataException e) {
        }

        return realm;
    }

    private static String getRealmAvp(AppAnswerEvent answer) {
        var realm = "";
        try {
            realm = answer.getOriginRealm();
        } catch (AvpDataException e) {
        }

        return realm;
    }

    private static String getAnswerResultCode(AppAnswerEvent answer) {
        var realm = "";
        try {
            var resultCode = answer.getResultCodeAvp();
            realm = (resultCode != null) ? resultCode.toString() : "";
        } catch (AvpDataException e) {
        }

        return realm;
    }
}
