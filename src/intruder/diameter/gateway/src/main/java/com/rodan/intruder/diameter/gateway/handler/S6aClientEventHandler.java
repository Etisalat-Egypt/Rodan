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

import com.rodan.intruder.diameter.entities.event.model.AuthVector;
import com.rodan.intruder.diameter.entities.event.model.LcsInfo;
import com.rodan.intruder.diameter.entities.event.model.ResultCode;
import com.rodan.intruder.diameter.entities.event.model.SubscriberInfo;
import com.rodan.intruder.diameter.gateway.handler.model.*;
import com.rodan.library.util.Util;
import net.java.slee.resource.diameter.base.events.avp.ExperimentalResultAvp;
import net.java.slee.resource.diameter.s6a.events.avp.EUTRANVectorAvp;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdiameter.api.*;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.s6a.ClientS6aSession;
import org.jdiameter.api.s6a.ClientS6aSessionListener;
import org.jdiameter.api.s6a.events.*;

import java.util.ArrayList;

public class S6aClientEventHandler extends S6aEventHandler implements ClientS6aSessionListener {
    Logger logger = LogManager.getLogger(S6aClientEventHandler.class);

    @Override
    public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        // TODO Diameter: implement;
    }

    @Override
    public void doCancelLocationRequestEvent(ClientS6aSession session, JCancelLocationRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        try {
            logger.debug("[[[[[[[[[[    doCancelLocationRequestEvent      ]]]]]]]]]]");
            logger.debug(request);
            var clrRequest = new org.mobicents.slee.resource.diameter.s6a.events.CancelLocationRequestImpl(request.getMessage());
            String cancellationType = null;
            if (clrRequest.getCancellationType() != null)
                cancellationType = clrRequest.getCancellationType().toString();
            var imsi = clrRequest.getUserName();
            var content = CancelLocationRequestImpl.builder()
                    .sessionId(request.getMessage().getSessionId())
                    .cancellationType(cancellationType).imsi(imsi)
                    .build();
            for (var listener : getListeners()) {
                listener.doCancelLocationRequestEvent(content);
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
    public void doInsertSubscriberDataRequestEvent(ClientS6aSession session, JInsertSubscriberDataRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        // TODO Diameter: implement;
    }

    @Override
    public void doDeleteSubscriberDataRequestEvent(ClientS6aSession session, JDeleteSubscriberDataRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        // TODO Diameter: implement;
    }

    @Override
    public void doResetRequestEvent(ClientS6aSession session, JResetRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        // TODO Diameter: implement;
    }

    @Override
    public void doAuthenticationInformationAnswerEvent(ClientS6aSession session, JAuthenticationInformationRequest request, JAuthenticationInformationAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        try {
            logger.debug("[[[[[[[[[[    doAuthenticationInformationAnswerEvent      ]]]]]]]]]]");
            logger.debug(answer);
            var airAnswer = new org.mobicents.slee.resource.diameter.s6a.events.AuthenticationInformationAnswerImpl(answer.getMessage());

            ExperimentalResultAvp experimentalResult = (airAnswer.hasExperimentalResult()) ?
                    airAnswer.getExperimentalResult() : null;
            var resultCode = parseResultCode(airAnswer, experimentalResult);
            if (isSuccessResultCode(resultCode)) {
                handleAia(airAnswer, resultCode);

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
    public void doPurgeUEAnswerEvent(ClientS6aSession session, JPurgeUERequest request, JPurgeUEAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        try {
            logger.debug("[[[[[[[[[[    doPurgeUEAnswerEvent      ]]]]]]]]]]");
            logger.debug(answer);
            var purAnswer = new org.mobicents.slee.resource.diameter.s6a.events.PurgeUEAnswerImpl(answer.getMessage());
            ExperimentalResultAvp experimentalResult = (purAnswer.hasExperimentalResult()) ?
                    purAnswer.getExperimentalResult() : null;
            var resultCode = parseResultCode(purAnswer, experimentalResult);
            if (isSuccessResultCode(resultCode)) {
                handlePur(purAnswer, resultCode);

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
    public void doUpdateLocationAnswerEvent(ClientS6aSession session, JUpdateLocationRequest request, JUpdateLocationAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        try {
            logger.debug("[[[[[[[[[[    doUpdateLocationAnswerEvent      ]]]]]]]]]]");
            logger.debug(answer);
            var ulaAnswer = new org.mobicents.slee.resource.diameter.s6a.events.UpdateLocationAnswerImpl(answer.getMessage());
            ExperimentalResultAvp experimentalResult = (ulaAnswer.hasExperimentalResult()) ?
                    ulaAnswer.getExperimentalResult() : null;
            var resultCode = parseResultCode(ulaAnswer, experimentalResult);
            if (isSuccessResultCode(resultCode)) {
                handleUla(ulaAnswer, resultCode);

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
    public void doNotifyAnswerEvent(ClientS6aSession session, JNotifyRequest request, JNotifyAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
        // TODO Diameter: implement;
        try {
            logger.debug("[[[[[[[[[[    doNotifyAnswerEvent      ]]]]]]]]]]");
            logger.debug(answer);

            var noaAnswer = new org.mobicents.slee.resource.diameter.s6a.events.NotifyAnswerImpl(answer.getMessage());
            ExperimentalResultAvp experimentalResult = (noaAnswer.hasExperimentalResult()) ?
                    noaAnswer.getExperimentalResult() : null;
            var resultCode = parseResultCode(noaAnswer, experimentalResult);
            if (isSuccessResultCode(resultCode)) {
                handleNoa(noaAnswer, resultCode);

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

    private AuthVector parseEutranVector(EUTRANVectorAvp vector) {
        String rand = null;
        String xres = null;
        String authn = null;
        String kasme = null;
        if (vector.hasRAND())
            rand = Util.encodeHexString(vector.getRAND());
        if (vector.hasXRES())
            xres = Util.encodeHexString(vector.getXRES());
        if (vector.hasAUTN())
            authn = Util.encodeHexString(vector.getAUTN());
        if (vector.hasKASME())
            kasme = Util.encodeHexString(vector.getKASME());

        return AuthVector.builder()
                .rand(rand).xres(xres).authn(authn).kasme(kasme)
                .build();
    }

    private void handleAia(org.mobicents.slee.resource.diameter.s6a.events.AuthenticationInformationAnswerImpl answer,
                           ResultCode resultCode) {
        var authVectorList = new ArrayList<AuthVector>();
        if (answer.getAuthenticationInfo() != null && answer.getAuthenticationInfo().getEUTRANVectors() != null) {
            for (var vector : answer.getAuthenticationInfo().getEUTRANVectors()) {
                authVectorList.add(parseEutranVector(vector));
            }
        }
        var content = AuthenticationInformationAnswerImpl.builder()
                .resultCode(resultCode).sessionId(answer.getSessionId())
                .originHost(answer.getOriginHost().toString()).authVectors(authVectorList)
                .build();
        for (var listener : getListeners()) {
            listener.doAuthenticationInformationAnswerEvent(content);
        }
    }

    private void handleUla(org.mobicents.slee.resource.diameter.s6a.events.UpdateLocationAnswerImpl answer,
                           ResultCode resultCode) {
        String msisdn = null;
        String subscriberStatus = null;
        Long accessRestrictionData = null;
        Long apnContextId = null;
        String apnServiceSelection = null;
        String apnPdnType = null;
        String gmlcNumber = null;
        Boolean hasLcsPrivacyException = null;
        Boolean hasMolr = null;

        if (answer.hasSubscriptionData()) {
            var subscriptionData = answer.getSubscriptionData();
            if (subscriptionData.hasMSISDN())
                msisdn = Util.tbcdToString(subscriptionData.getMSISDN());
            if (subscriptionData.hasSubscriberStatus())
                subscriberStatus = subscriptionData.getSubscriberStatus().toString();
            if (subscriptionData.hasAccessRestrictionData())
                accessRestrictionData = subscriptionData.getAccessRestrictionData();
            if (subscriptionData.hasLCSInfo()) {
                var lcsInfo = subscriptionData.getLCSInfo();
                if (lcsInfo.hasGMLCNumber())
                    for (var glmc : lcsInfo.getGMLCNumbers())
                        gmlcNumber = Util.tbcdToString(glmc) + ", ";
                hasLcsPrivacyException = lcsInfo.hasLCSPrivacyException(); // TODO parse LCSPrivacyException
                hasMolr = lcsInfo.hasMOLR(); // TODO parse MOLR
            }

            if (subscriptionData.hasAPNConfigurationProfile()) {
                var apnProfile = subscriptionData.getAPNConfigurationProfile();
                if (apnProfile.hasAPNConfiguration()) {
                    var apnConfig = apnProfile.getAPNConfiguration();
                    if (apnConfig.hasContextIdentifier())
                        apnContextId = apnConfig.getContextIdentifier();
                    if (apnConfig.hasServiceSelection())
                        apnServiceSelection = apnConfig.getServiceSelection();
                    if (apnConfig.hasPDNType())
                        apnPdnType = apnConfig.getPDNType().toString();
                }
            }
        }
        var subscriberInfo = SubscriberInfo.builder()
                .msisdn(msisdn).subscriberStatus(subscriberStatus).accessRestrictionData(accessRestrictionData)
                .apnContextId(apnContextId).apnServiceSelection(apnServiceSelection).apnPdnType(apnPdnType)
                .build();
        var lcsInfo = LcsInfo.builder()
                .gmlcNumber(gmlcNumber).hasLcsPrivacyException(hasLcsPrivacyException).hasMolr(hasMolr)
                .build();
        var content = UpdateLocationAnswerImpl.builder()
                .resultCode(resultCode).sessionId(answer.getSessionId())
                .subscriberInfo(subscriberInfo).lcsInfo(lcsInfo)
                .build();
        for (var listener : getListeners()) {
            listener.doUpdateLocationAnswerEvent(content);
        }
    }

    private void handleNoa(org.mobicents.slee.resource.diameter.s6a.events.NotifyAnswerImpl answer,
                           ResultCode resultCode) {
        var content = NotifyAnswerImpl.builder()
                .resultCode(resultCode).sessionId(answer.getSessionId())
                .build();
        for (var listener : getListeners()) {
            listener.doNotifyAnswerEvent(content);
        }
    }

    private void handlePur(org.mobicents.slee.resource.diameter.s6a.events.PurgeUEAnswerImpl answer, ResultCode resultCode) {
        var content = PurgeUeAnswerImpl.builder()
                .resultCode(resultCode).sessionId(answer.getSessionId())
                .build();
        for (var listener : getListeners()) {
            listener.doPurgeUEAnswerEvent(content);
        }
    }
}
