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

package com.rodan.intruder.ss7.gateway.handler;

import com.rodan.intruder.ss7.entities.event.model.ErrorEvent;
import com.rodan.intruder.ss7.entities.event.model.LocationInfo;
import com.rodan.intruder.ss7.entities.event.model.auth.AuthQuintuplet;
import com.rodan.intruder.ss7.entities.event.model.auth.AuthTriplet;
import com.rodan.intruder.ss7.entities.event.model.auth.EpcAuthVector;
import com.rodan.intruder.ss7.entities.event.model.auth.SecurityContext;
import com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener;
import com.rodan.intruder.ss7.gateway.handler.model.lcs.PslResponseImpl;
import com.rodan.intruder.ss7.gateway.handler.model.mobility.*;
import com.rodan.library.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.primitives.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPServiceMobilityListener;
import org.mobicents.protocols.ss7.map.api.service.mobility.authentication.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.faultRecovery.ForwardCheckSSIndicationRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.faultRecovery.ResetRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.faultRecovery.RestoreDataRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.faultRecovery.RestoreDataResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.imei.CheckImeiRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.imei.CheckImeiResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.oam.ActivateTraceModeRequest_Mobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.oam.ActivateTraceModeResponse_Mobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.*;
import org.mobicents.protocols.ss7.map.primitives.IMSIImpl;
import org.mobicents.protocols.ss7.map.service.mobility.authentication.GSMSecurityContextDataImpl;
import org.mobicents.protocols.ss7.map.service.mobility.authentication.UMTSSecurityContextDataImpl;
import org.mobicents.protocols.ss7.map.service.mobility.subscriberInformation.LocationInformationGPRSImpl;
import org.mobicents.protocols.ss7.map.service.mobility.subscriberInformation.LocationInformationImpl;
import org.mobicents.protocols.ss7.map.service.mobility.subscriberInformation.SubscriberInfoImpl;
import org.mobicents.protocols.ss7.map.service.mobility.subscriberManagement.OCSIImpl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.SccpAddressImpl;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapMobilityServiceHandler extends MapServiceHandler implements MAPServiceMobilityListener {
    Logger logger = LogManager.getLogger(MapMobilityServiceHandler.class);

    private List<MapMobilityServiceListener> listeners;

    public MapMobilityServiceHandler() {
        listeners = new ArrayList<>();
    }

    public void addListener(MapMobilityServiceListener listener) {
        logger.debug("Registering MAP listener: " + listener);
        if (listeners.contains(listener)) {
            logger.warn("Registering MapMobilityServiceListener for already existing one");
            return;
        }

        listeners.add(listener);
        getBaseServiceListeners().add(listener);
    }

    public void removeListener(MapMobilityServiceListener listener) {
        logger.debug("Removing MAP listener: " + listener);
        if (!listeners.contains(listener)) {
            logger.warn("Removing a non-existing MapMobilityServiceListener");
            return;
        }

        listeners.remove(listener);
        getBaseServiceListeners().remove(listener);
    }

    @Override
    public void onUpdateLocationRequest(UpdateLocationRequest request) {
        try {
            logger.debug("[[[[[[[[[[    onUpdateLocationRequest      ]]]]]]]]]]");
            logger.debug(request);
            var imsi = Util.getValueOrElse(request.getImsi(), IMSI::getData, "");
            var vlrGt = Util.getValueOrElse(request.getMscNumber(), AddressString::getAddress, "");
            var mscGt = Util.getValueOrElse(request.getVlrNumber(), AddressString::getAddress, "");
            var content = UlRequestImpl.builder()
                    .invokeId(request.getInvokeId()).mapDialog(request.getMAPDialog())
                    .imsi(imsi).mscGt(mscGt).vlrGt(vlrGt)
                    .build();
            for (var listener : listeners) {
                listener.onUpdateLocationRequest(content);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(request.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onUpdateLocationResponse(UpdateLocationResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onUpdateLocationResponse      ]]]]]]]]]]");
            logger.debug(response);
            var content = UlResponseImpl.builder()
                    .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog())
                    .build();
            for (var listener : listeners) {
                listener.onUpdateLocationResponse(content);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onCancelLocationRequest(CancelLocationRequest request) {
        logger.debug("[[[[[[[[[[    onCancelLocationRequest      ]]]]]]]]]]");
        logger.debug(request);
        var content = ClRequestImpl.builder()
                .invokeId(request.getInvokeId()).mapDialog(request.getMAPDialog())
                .build();
        for (var listener : listeners) {
            listener.onCancelLocationRequest(content);
        }
    }

    @Override
    public void onCancelLocationResponse(CancelLocationResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onCancelLocationResponse      ]]]]]]]]]]");
            logger.debug(response);
            var content = ClResponseImpl.builder()
                    .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog()).build();
            for (var listener : listeners) {
                listener.onCancelLocationResponse(content);
            }
        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onSendIdentificationRequest(SendIdentificationRequest request) {
        logger.debug("[[[[[[[[[[    onSendIdentificationRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onSendIdentificationResponse(SendIdentificationResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onSendIdentificationResponse      ]]]]]]]]]]");
            logger.debug(response);
            var content = parseSendIdResponse(response);
            for (var listener : listeners) {
                listener.onSendIdentificationResponse(content);
            }
        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onUpdateGprsLocationRequest(UpdateGprsLocationRequest request) {
        logger.debug("[[[[[[[[[[    onUpdateGprsLocationRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onUpdateGprsLocationResponse(UpdateGprsLocationResponse response) {
        logger.debug("[[[[[[[[[[    onUpdateGprsLocationResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    @Override
    public void onPurgeMSRequest(PurgeMSRequest request) {
        logger.debug("[[[[[[[[[[    onPurgeMSRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onPurgeMSResponse(PurgeMSResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onPurgeMSResponse      ]]]]]]]]]]");
            logger.debug(response);
            var content = PurgeMsResponseImpl.builder()
                    .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog()).build();
            for (var listener : listeners) {
                listener.onPurgeMSResponse(content);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onSendAuthenticationInfoRequest(SendAuthenticationInfoRequest request) {
        try {
            logger.debug("[[[[[[[[[[    onSendAuthenticationInfoRequest      ]]]]]]]]]]");
            logger.debug(request);
            var requestingNodeType = request.getRequestingNodeType().equals(RequestingNodeType.vlr) ?
                    "vlr" : "sgsn"; // TODO support remaining node types
            var content = SaiRequestImpl.builder()
                    .invokeId(request.getInvokeId()).mapDialog(request.getMAPDialog())
                    .imsi(request.getImsi().getData()).vlrGt(request.getMAPDialog().getRemoteAddress().getGlobalTitle().getDigits())
                    .requestingNodeType(requestingNodeType)
                    .build();
            for (var listener : listeners) {
                listener.onSendAuthenticationInfoRequest(content);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(request.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onSendAuthenticationInfoResponse(SendAuthenticationInfoResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onSendAuthenticationInfoResponse      ]]]]]]]]]]");
            logger.debug(response);
            var content = parseSaiResponse(response);
            for (var listener : listeners) {
                listener.onSendAuthenticationInfoResponse(content);
            }
        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onAuthenticationFailureReportRequest(AuthenticationFailureReportRequest request) {
        logger.debug("[[[[[[[[[[    onAuthenticationFailureReportRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onAuthenticationFailureReportResponse(AuthenticationFailureReportResponse response) {
        logger.debug("[[[[[[[[[[    onAuthenticationFailureReportResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    @Override
    public void onResetRequest(ResetRequest request) {
        logger.debug("[[[[[[[[[[    onResetRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onForwardCheckSSIndicationRequest(ForwardCheckSSIndicationRequest request) {
        logger.debug("[[[[[[[[[[    onForwardCheckSSIndicationRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onRestoreDataRequest(RestoreDataRequest request) {
        logger.debug("[[[[[[[[[[    onRestoreDataRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onRestoreDataResponse(RestoreDataResponse response) {
        logger.debug("[[[[[[[[[[    onRestoreDataResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    @Override
    public void onAnyTimeInterrogationRequest(AnyTimeInterrogationRequest request) {
        try {
            logger.debug("[[[[[[[[[[    onAnyTimeInterrogationRequest      ]]]]]]]]]]");
            logger.debug(request);
            var msisdn = request.getSubscriberIdentity().getMSISDN().getAddress();
            var gsmScf = request.getGsmSCFAddress().getAddress();

            var content = AtiRequestImpl.builder()
                    .invokeId(request.getInvokeId()).mapDialog(request.getMAPDialog())
                    .msisdn(msisdn).gsmScf(gsmScf)
                    .build();
            for (var listener : listeners) {
                listener.onAnyTimeInterrogationRequest(content);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(request.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onAnyTimeInterrogationResponse(AnyTimeInterrogationResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onAnyTimeInterrogationResponse      ]]]]]]]]]]");
            logger.debug(response);
            var subscriberInfo = parseSubscriberInfo(response.getSubscriberInfo());
            var hlrGt = Util.getValueOrElse(response.getMAPDialog().getRemoteAddress().getGlobalTitle(), GlobalTitle::getDigits, "");
            var vlrGt = Util.getValueOrElse(response.getSubscriberInfo().getLocationInformation().getVlrNumber(), AddressString::getAddress, "");
            var vmscGt = Util.getValueOrElseNull(response.getSubscriberInfo().getLocationInformation().getMscNumber(), AddressString::getAddress);
            var cellInfo = Util.getValueOrElseNull(response.getSubscriberInfo().getLocationInformation().getCellGlobalIdOrServiceAreaIdOrLAI(), CellGlobalIdOrServiceAreaIdOrLAI::getCellGlobalIdOrServiceAreaIdFixedLength);
            int mcc = 0, mnc = 0, lac = 0, cellId = 0;
            if (cellInfo != null) {
                mcc = cellInfo.getMCC();
                mnc = cellInfo.getMNC();
                lac = cellInfo.getLac();
                cellId = cellInfo.getCellIdOrServiceAreaCode();
            }

            var content = AtiResponseImpl.builder()
                    .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog())
                    .subscriberInfo(subscriberInfo).hlrGt(hlrGt).vmscGt(vmscGt).vlrGt(vlrGt).mcc(mcc).mnc(mnc).lac(lac)
                    .cellId(cellId)
                    .build();
            for (var listener : listeners) {
                listener.onAnyTimeInterrogationResponse(content);
            }
        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onAnyTimeSubscriptionInterrogationRequest(AnyTimeSubscriptionInterrogationRequest request) {
        logger.debug("[[[[[[[[[[    onAnyTimeSubscriptionInterrogationRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onAnyTimeSubscriptionInterrogationResponse(AnyTimeSubscriptionInterrogationResponse response) {
        logger.debug("[[[[[[[[[[    onAnyTimeSubscriptionInterrogationResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    @Override
    public void onProvideSubscriberInfoRequest(ProvideSubscriberInfoRequest request) {
        try {
            logger.debug("[[[[[[[[[[    onProvideSubscriberInfoRequest      ]]]]]]]]]]");
            logger.debug(request);
            var imsi = Objects.requireNonNullElse(request.getImsi(), new IMSIImpl());
            var content = PsiRequestImpl.builder()
                    .invokeId(request.getInvokeId()).mapDialog(request.getMAPDialog())
                    .imsi(imsi.getData())
                    .build();
            for (var listener : listeners) {
                listener.onProvideSubscriberInfoRequest(content);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(request.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onProvideSubscriberInfoResponse(ProvideSubscriberInfoResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onProvideSubscriberInfoResponse      ]]]]]]]]]]");
            logger.debug(response);
            var jsubscriberInfo = Objects.requireNonNullElse(response.getSubscriberInfo(), new SubscriberInfoImpl());
            var jlocationInfo = Objects.requireNonNullElse(jsubscriberInfo.getLocationInformation(), new LocationInformationImpl());
            var jlocationInfoGprs = Objects.requireNonNullElse(jsubscriberInfo.getLocationInformationGPRS(), new LocationInformationGPRSImpl());

            var subscriberInfo = parseSubscriberInfo(jsubscriberInfo);
            var locationInfo = parseLocationInfo(jlocationInfo, jlocationInfoGprs);
            var vlr = Util.getValueOrElse(jlocationInfo.getVlrNumber(), AddressString::getAddress, "");
            if (StringUtils.isBlank(vlr)) {
                var remoteAddress = Objects.requireNonNullElse(response.getMAPDialog().getRemoteAddress(), new SccpAddressImpl());
                vlr = remoteAddress.getGlobalTitle().getDigits();
            }
            var sgsn = Util.getValueOrElse(jlocationInfoGprs.getSGSNNumber(), AddressString::getAddress, "");
            var content = PsiResponseImpl.builder()
                    .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog())
                    .subscriberInfo(subscriberInfo).locationInfo(locationInfo).vlrGt(vlr).sgsnGt(sgsn)
                    .build();
            for (var listener : listeners) {
                listener.onProvideSubscriberInfoResponse(content);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onInsertSubscriberDataRequest(InsertSubscriberDataRequest request) {
        try {
            logger.debug("[[[[[[[[[[    onInsertSubscriberDataRequest      ]]]]]]]]]]");
            logger.debug(request);
            var dialog = request.getMAPDialog();
            var msisdn = Util.getValueOrElse(request.getMsisdn(), ISDNAddressString::getAddress, "");
            var hlr = Objects.requireNonNullElse(dialog.getRemoteAddress(), new SccpAddressImpl());
            var hlrGt = Util.getValueOrElse(hlr.getGlobalTitle(), GlobalTitle::getDigits, "");
            var sgsn = Util.getValueOrElse(request.getSgsnNumber(), ISDNAddressString::getAddress, "");
            String gsmScf = null;
            var ocsi = Util.getValueOrElse(request.getVlrCamelSubscriptionInfo(), VlrCamelSubscriptionInfo::getOCsi,
                    new OCSIImpl(new ArrayList<>(), null, null, false, false));
            if (ocsi != null) {
                for (var camelTdp : ocsi.getOBcsmCamelTDPDataList()) {
                    gsmScf = camelTdp.getGsmSCFAddress().getAddress();
                    logger.debug("Received gsmSCF: " + gsmScf);
                    break;
                }
            }

            var content = IsdRequestImpl.builder()
                    .invokeId(request.getInvokeId()).mapDialog(request.getMAPDialog())
                    .msisdn(msisdn).hlrGt(hlrGt).sgsn(sgsn).gsmScf(gsmScf)
                    .build();
            for (var listener : listeners) {
                listener.onInsertSubscriberDataRequest(content);
            }
        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(request.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onInsertSubscriberDataResponse(InsertSubscriberDataResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onInsertSubscriberDataResponse      ]]]]]]]]]]");
            logger.debug(response);
            var content = IsdResponseImpl.builder()
                    .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog()).build();
            for (var listener : listeners) {
                listener.onInsertSubscriberDataResponse(content);
            }
        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onDeleteSubscriberDataRequest(DeleteSubscriberDataRequest request) {
        logger.debug("[[[[[[[[[[    onDeleteSubscriberDataRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onDeleteSubscriberDataResponse(DeleteSubscriberDataResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onDeleteSubscriberDataResponse      ]]]]]]]]]]");
            logger.debug(response);
            var content = DsdResponseImpl.builder()
                    .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog()).build();
            for (var listener : listeners) {
                listener.onDeleteSubscriberDataResponse(content);
            }
        } catch (Exception e) {
            var msg = "Failed to parse MAP message: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
    public void onCheckImeiRequest(CheckImeiRequest request) {
        logger.debug("[[[[[[[[[[    onCheckImeiRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onCheckImeiResponse(CheckImeiResponse response) {
        logger.debug("[[[[[[[[[[    onCheckImeiResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    @Override
    public void onActivateTraceModeRequest_Mobility(ActivateTraceModeRequest_Mobility request) {
        logger.debug("[[[[[[[[[[    onActivateTraceModeRequest_Mobility      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onActivateTraceModeResponse_Mobility(ActivateTraceModeResponse_Mobility response) {
        logger.debug("[[[[[[[[[[    onActivateTraceModeResponse_Mobility      ]]]]]]]]]]");
        logger.debug(response);
    }

    private SendIdResponseImpl parseSendIdResponse(SendIdentificationResponse response) {
        var imsi = Util.getValueOrElse(response.getImsi(), IMSI::getData, "");

        var auth2gTripletList = Util.getValueOrElse(response.getAuthenticationSetList().getTripletList(),
                TripletList::getAuthenticationTriplets, new ArrayList<AuthenticationTriplet>());
        var tripletList = parseAuthenticationTriplet(auth2gTripletList);

        var auth3gQuintupletList = Util.getValueOrElse(response.getAuthenticationSetList().getQuintupletList(),
                QuintupletList::getAuthenticationQuintuplets, new ArrayList<AuthenticationQuintuplet>());
        var quintupletList = parseAuthenticationQuintuplet(auth3gQuintupletList);

        var gsmContext = Util.getValueOrElse(response.getCurrentSecurityContext(),
                CurrentSecurityContext::getGSMSecurityContextData, new GSMSecurityContextDataImpl());
        var umtsContext = Util.getValueOrElse(response.getCurrentSecurityContext(),
                CurrentSecurityContext::getUMTSSecurityContextData, new UMTSSecurityContextDataImpl());
        var securityContext = parseSecurityContext(gsmContext, umtsContext);

        var sendIdResponse = SendIdResponseImpl.builder()
                .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog())
                .imsi(imsi).tripletList(tripletList).quintupletList(quintupletList).securityContext(securityContext)
                .build();
        return sendIdResponse;
    }

    private SaiResponseImpl parseSaiResponse(SendAuthenticationInfoResponse response) {
        var auth2gTripletList = Util.getValueOrElse(response.getAuthenticationSetList().getTripletList(),
                TripletList::getAuthenticationTriplets, new ArrayList<AuthenticationTriplet>());
        var tripletList = parseAuthenticationTriplet(auth2gTripletList);

        var auth3gQuintupletList = Util.getValueOrElse(response.getAuthenticationSetList().getQuintupletList(),
                QuintupletList::getAuthenticationQuintuplets, new ArrayList<AuthenticationQuintuplet>());
        var quintupletList = parseAuthenticationQuintuplet(auth3gQuintupletList);



        var epcAuthVectorList = Util.getValueOrElse(response.getEpsAuthenticationSetList(),
                EpsAuthenticationSetList::getEpcAv, new ArrayList<EpcAv>());
        var epcAuthVectorPsList = parseEpcAuthVector(epcAuthVectorList);

        var saiResponse = SaiResponseImpl.builder()
                .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog())
                .tripletList(tripletList).quintupletList(quintupletList).epcAuthVectorList(epcAuthVectorPsList)
                .build();
        return saiResponse;
    }

    private ArrayList<AuthTriplet> parseAuthenticationTriplet(List<AuthenticationTriplet> auth2gTripletList) {
        var tripletList = new ArrayList<AuthTriplet>();
        for (var triplet : auth2gTripletList) {
            tripletList.add(
                    AuthTriplet.builder()
                            .rand(Util.encodeHexString(triplet.getRand()))
                            .sres(Util.encodeHexString(triplet.getSres()))
                            .kc(Util.encodeHexString(triplet.getKc()))
                            .build()
            );
        }

        return tripletList;
    }

    private ArrayList<AuthQuintuplet> parseAuthenticationQuintuplet(List<AuthenticationQuintuplet> auth3gQuintupletList) {
        var quintupletList = new ArrayList<AuthQuintuplet>();
        for (var quintuplet : auth3gQuintupletList) {
            quintupletList.add(
                    AuthQuintuplet.builder()
                            .rand(Util.encodeHexString(quintuplet.getRand()))
                            .xres(Util.encodeHexString(quintuplet.getXres()))
                            .kc(Util.encodeHexString(quintuplet.getCk()))
                            .ik(Util.encodeHexString(quintuplet.getIk()))
                            .authn(Util.encodeHexString(quintuplet.getAutn()))
                            .build()
            );
        }

        return quintupletList;
    }

    private ArrayList<EpcAuthVector> parseEpcAuthVector(ArrayList<EpcAv> epcAuthVectorList) {
        var epcAuthVectorPsList = new ArrayList<EpcAuthVector>();
        for (var epcAuthVector : epcAuthVectorList) {
            epcAuthVectorPsList.add(
                    EpcAuthVector.builder()
                            .rand(Util.encodeHexString(epcAuthVector.getRand()))
                            .xres(Util.encodeHexString(epcAuthVector.getXres()))
                            .authn(Util.encodeHexString(epcAuthVector.getAutn()))
                            .kasme(Util.encodeHexString(epcAuthVector.getKasme()))
                            .build()
            );
        }

        return epcAuthVectorPsList;
    }

    private SecurityContext parseSecurityContext(GSMSecurityContextData gsmContext, UMTSSecurityContextData umtsContext) {
        var gsmKc = Util.encodeHexString(gsmContext.getKc().getData());
        var gsmCksn = String.valueOf(gsmContext.getCksn().getData());
        var umtsCk = Util.encodeHexString(umtsContext.getCK().getData());
        var umtsIk = Util.encodeHexString(umtsContext.getIK().getData());
        var umtsKsi = String.valueOf(umtsContext.getKSI().getData());
        var securityContext = SecurityContext.builder()
                .gsmKc(gsmKc).gsmCksn(gsmCksn).umtsCk(umtsCk).umtsIk(umtsIk).umtsKsi(umtsKsi)
                .build();
        return securityContext;
    }

    private com.rodan.intruder.ss7.entities.event.model.SubscriberInfo
            parseSubscriberInfo(SubscriberInfo subscriberInfo) throws MAPException {

        var imei = Util.getValueOrElse(subscriberInfo.getIMEI(), IMEI::getIMEI, "");
        var state = Util.getValueOrElse(subscriberInfo.getSubscriberState(), m -> m.getSubscriberStateChoice().name(), "");
        var gciOrLai = Util.getValueOrElseNull(subscriberInfo.getLocationInformation(), LocationInformation::getCellGlobalIdOrServiceAreaIdOrLAI);
        var cellInfo = Util.getValueOrElseNull(gciOrLai, CellGlobalIdOrServiceAreaIdOrLAI::getCellGlobalIdOrServiceAreaIdFixedLength);

        var geoInfo = Util.getValueOrElseNull(subscriberInfo.getLocationInformation(), LocationInformation::getGeographicalInformation);
        var longitude = Util.getValueOrElse(geoInfo, GeographicalInformation::getLongitude, 0.0);
        var latitude = Util.getValueOrElse(geoInfo, GeographicalInformation::getLatitude, 0.0);
        var uncertainty = Util.getValueOrElse(geoInfo, GeographicalInformation::getUncertainty, 0.0);
        var ageOfLocation = Util.getValueOrElseNull(subscriberInfo.getLocationInformation(), LocationInformation::getAgeOfLocationInformation);
        var locationAge = Util.getValueOrElse(ageOfLocation, Integer::intValue, 0);

        var info = com.rodan.intruder.ss7.entities.event.model.SubscriberInfo.builder()
                .imei(imei).state(state).longitude(longitude).latitude(latitude).uncertainty(uncertainty).ageOfLocation(locationAge)
                .build();
        return info;
    }

    private LocationInfo parseLocationInfo(LocationInformation locationInfo, LocationInformationGPRS locationInfoGprs) throws MAPException {
        var longitude = Util.getValueOrElse(locationInfo.getGeographicalInformation(), GeographicalInformation::getLongitude, 0.0);
        var latitude = Util.getValueOrElse(locationInfo.getGeographicalInformation(), GeographicalInformation::getLatitude, 0.0);
        var uncertainty = Util.getValueOrElse(locationInfo.getGeographicalInformation(), GeographicalInformation::getUncertainty, 0.0);
        var locationAge = Util.getValueOrElse(locationInfo.getAgeOfLocationInformation(), Integer::intValue, 0);
        var cellInfo = Util.getValueOrElseNull(locationInfo.getCellGlobalIdOrServiceAreaIdOrLAI(), CellGlobalIdOrServiceAreaIdOrLAI::getCellGlobalIdOrServiceAreaIdFixedLength);
        int mcc = 0, mnc = 0, lac = 0, cellId = 0;
        if (cellInfo != null && cellInfo.getData() != null) {
            mcc = cellInfo.getMCC();
            mnc = cellInfo.getMNC();
            lac = cellInfo.getLac();
            cellId = cellInfo.getCellIdOrServiceAreaCode();
        } else {
            var lai = Util.getValueOrElseNull(locationInfo.getCellGlobalIdOrServiceAreaIdOrLAI(),
                    CellGlobalIdOrServiceAreaIdOrLAI::getLAIFixedLength);
            if (lai != null) {
                mcc = lai.getMCC();
                mnc = lai.getMNC();
                lac = lai.getLac();
            }
        }

        var longitudePs = Util.getValueOrElse(locationInfoGprs.getGeographicalInformation(), GeographicalInformation::getLongitude, 0.0);
        var latitudePs = Util.getValueOrElse(locationInfoGprs.getGeographicalInformation(), GeographicalInformation::getLatitude, 0.0);
        var uncertaintyPs = Util.getValueOrElse(locationInfoGprs.getGeographicalInformation(), GeographicalInformation::getUncertainty, 0.0);

        var locationAgePs = Util.getValueOrElse(locationInfoGprs.getAgeOfLocationInformation(), Integer::intValue, 0);

        var location = LocationInfo.builder()
                .mcc(mcc).mnc(mnc).lac(lac).cellId(cellId)
                .longitude(longitude).latitude(latitude).uncertainty(uncertainty).ageOfLocationPs(locationAge)
                .longitudePs(longitudePs).latitudePs(latitudePs).uncertaintyPs(uncertaintyPs).ageOfLocationPs(locationAgePs)
                .build();
        return location;
    }
}
