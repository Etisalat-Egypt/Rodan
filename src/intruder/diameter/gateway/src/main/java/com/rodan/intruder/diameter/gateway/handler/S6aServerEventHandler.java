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

import com.rodan.intruder.diameter.entities.event.model.ResultCode;
import com.rodan.intruder.diameter.gateway.handler.model.CancelLocationAnswerImpl;
import com.rodan.intruder.diameter.gateway.handler.model.ErrorEventImpl;
import com.rodan.intruder.diameter.gateway.handler.model.InsertSubscriberDataAnswerImpl;
import com.rodan.library.util.Util;
import net.java.slee.resource.diameter.base.events.avp.ExperimentalResultAvp;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.s6a.ServerS6aSession;
import org.jdiameter.api.s6a.ServerS6aSessionListener;
import org.jdiameter.api.s6a.events.*;

public class S6aServerEventHandler extends S6aEventHandler implements ServerS6aSessionListener {
    Logger logger = LogManager.getLogger(S6aServerEventHandler.class);

    @Override
    public void doOtherEvent(AppSession appSession, AppRequestEvent appRequestEvent, AppAnswerEvent appAnswerEvent) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        // TODO Diameter: implement;
    }

    @Override
    public void doAuthenticationInformationRequestEvent(ServerS6aSession session, JAuthenticationInformationRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        // TODO Diameter: implement;
    }

    @Override
    public void doPurgeUERequestEvent(ServerS6aSession session, JPurgeUERequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        // TODO Diameter: implement;
    }

    @Override
    public void doUpdateLocationRequestEvent(ServerS6aSession session, JUpdateLocationRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        // TODO Diameter: implement;
    }

    @Override
    public void doNotifyRequestEvent(ServerS6aSession session, JNotifyRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        // TODO Diameter: implement;
    }

    @Override
    public void doCancelLocationAnswerEvent(ServerS6aSession session, JCancelLocationRequest request, JCancelLocationAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        try {
            logger.debug("[[[[[[[[[[    doCancelLocationAnswerEvent      ]]]]]]]]]]");
            logger.debug(answer);
            logger.debug("sessionId: " + request.getMessage().getSessionId());

            var claAnswer = new org.mobicents.slee.resource.diameter.s6a.events.CancelLocationAnswerImpl(answer.getMessage());
            ExperimentalResultAvp experimentalResult = (claAnswer.hasExperimentalResult()) ?
                    claAnswer.getExperimentalResult() : null;
            var resultCode = parseResultCode(claAnswer, experimentalResult);
            if (isSuccessResultCode(resultCode)) {
                handleCla(claAnswer, resultCode);

            } else {
                handleError(resultCode);
            }

        } catch (Exception e) {
            var msg = "Failed to parse Diameter answer: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEventImpl.builder().message(msg).build();
            for (var listener : getListeners()) {
                listener.onMessageHandlingError(error);
            }
        }
    }

    @Override
    public void doInsertSubscriberDataAnswerEvent(ServerS6aSession session, JInsertSubscriberDataRequest request,
                                                  JInsertSubscriberDataAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        try {
            logger.debug("[[[[[[[[[[    doAuthenticationInformationAnswerEvent      ]]]]]]]]]]");
            logger.debug(answer);
            logger.debug("sessionId: " + request.getMessage().getSessionId());

            var idaAnswer = new org.mobicents.slee.resource.diameter.s6a.events.InsertSubscriberDataAnswerImpl(answer.getMessage());
            ExperimentalResultAvp experimentalResult = (idaAnswer.hasExperimentalResult()) ?
                    idaAnswer.getExperimentalResult() : null;
            var resultCode = parseResultCode(idaAnswer, experimentalResult);
            if (isSuccessResultCode(resultCode)) {
                handleIda(idaAnswer, resultCode);

            } else {
                handleError(resultCode);
            }

        } catch (Exception e) {
            var msg = "Failed to parse Diameter answer: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEventImpl.builder().message(msg).build();
            for (var listener : getListeners()) {
                listener.onMessageHandlingError(error);
            }
        }
    }

    @Override
    public void doDeleteSubscriberDataAnswerEvent(ServerS6aSession session, JDeleteSubscriberDataRequest request, JDeleteSubscriberDataAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        // TODO Diameter: implement;
    }

    @Override
    public void doResetAnswerEvent(ServerS6aSession session, JResetRequest request, JResetAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        // TODO Diameter: implement;
    }

    private void handleIda(org.mobicents.slee.resource.diameter.s6a.events.InsertSubscriberDataAnswerImpl answer,
                           ResultCode resultCode) {
        String cellId = null;
        String tac = null;
        Long ageOfLocation = null;
        if (answer.hasEPSLocationInformation()) {
            // TODO: Store both MMELocationInformation and SGSNLocationInformation in InsertSubscriberDataAnswerImpl
            var locationInfo = answer.getEPSLocationInformation();
            if (locationInfo.hasMMELocationInformation()) {
                var location = locationInfo.getMMELocationInformation();
                var cgi = Util.octetStringToString(location.getEUTRANCellGlobalIdentity());
                cellId = Util.hexToDecimal(cgi.substring(9));
                var tai = Util.octetStringToString(location.getTrackingAreaIdentity());
                tac = Util.hexToDecimal(tai.substring(6));
                ageOfLocation = location.getAgeOfLocationInformation();

            } else if (locationInfo.hasSGSNLocationInformation()) {
                var location = locationInfo.getSGSNLocationInformation();
                var cgi = Util.octetStringToString(location.getCellGlobalIdentity());
                cellId = Util.hexToDecimal(cgi.substring(9));
                var tai = Util.octetStringToString(location.getLocationAreaIdentity());
                tac = Util.hexToDecimal(tai.substring(6));
                ageOfLocation = location.getAgeOfLocationInformation();
            }
        }

        var content = InsertSubscriberDataAnswerImpl.builder()
                .resultCode(resultCode).sessionId(answer.getSessionId())
                .originHost(answer.getOriginHost().toString())
                .cellId(cellId).tac(tac).ageOfLocation(ageOfLocation)
                .build();
        for (var listener : getListeners()) {
            listener.doInsertSubscriberDataAnswerEvent(content);
        }
    }

    private void handleCla(org.mobicents.slee.resource.diameter.s6a.events.CancelLocationAnswerImpl answer,
                           ResultCode resultCode) {
        var content = CancelLocationAnswerImpl.builder()
                .resultCode(resultCode).sessionId(answer.getSessionId())
                .build();
        for (var listener : getListeners()) {
            listener.doCancelLocationAnswerEvent(content);
        }
    }
}
