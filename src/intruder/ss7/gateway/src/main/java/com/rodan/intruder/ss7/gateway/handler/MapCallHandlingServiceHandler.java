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
import com.rodan.intruder.ss7.entities.event.service.MapCallHandlingServiceListener;
import com.rodan.intruder.ss7.gateway.handler.model.call.PrnRequestImpl;
import com.rodan.intruder.ss7.gateway.handler.model.call.SriRequestImpl;
import com.rodan.intruder.ss7.gateway.handler.model.call.SriResponseImpl;
import com.rodan.library.util.Util;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.map.api.primitives.AddressString;
import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.map.api.service.callhandling.*;
import org.mobicents.protocols.ss7.map.primitives.ISDNAddressStringImpl;
import org.mobicents.protocols.ss7.map.service.callhandling.RoutingInfoImpl;
import org.mobicents.protocols.ss7.map.service.mobility.subscriberInformation.LocationInformationImpl;
import org.mobicents.protocols.ss7.map.service.mobility.subscriberInformation.SubscriberInfoImpl;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapCallHandlingServiceHandler extends MapServiceHandler implements MAPServiceCallHandlingListener {
    Logger logger = LogManager.getLogger(MapCallHandlingServiceHandler.class);

    private List<MapCallHandlingServiceListener> listeners;

    public MapCallHandlingServiceHandler() {
        listeners = new ArrayList<>();
    }

    public void addListener(MapCallHandlingServiceListener listener) {
        logger.debug("Registering MAP listener: " + listener);
        if (listeners.contains(listener)) {
            logger.warn("Registering MapSmsServiceListener for already existing one");
            return;
        }

        listeners.add(listener);
        getBaseServiceListeners().add(listener);
    }

    public void removeListener(MapCallHandlingServiceListener listener) {
        logger.debug("Removing MAP listener: " + listener);
        if (!listeners.contains(listener)) {
            logger.warn("Removing a non-existing MapSmsServiceListener");
            return;
        }

        listeners.remove(listener);
        getBaseServiceListeners().remove(listener);
    }

    @Override
    public void onSendRoutingInformationRequest(SendRoutingInformationRequest request) {
        try {
            logger.debug("[[[[[[[[[[    onSendRoutingInformationRequest      ]]]]]]]]]]");
            logger.debug(request);

            var msisdn = Objects.requireNonNullElse(request.getMsisdn(), new ISDNAddressStringImpl());
            var gmscOrGsmScf = Objects.requireNonNullElse(request.getGmscOrGsmSCFAddress(), new ISDNAddressStringImpl());
            var content = SriRequestImpl.builder()
                    .invokeId(request.getInvokeId()).mapDialog(request.getMAPDialog())
                    .msisdn(msisdn.getAddress()).gmscOrGsmScfAddress(gmscOrGsmScf.getAddress())
                    .build();
            for (var listener : listeners) {
                listener.onSendRoutingInformationRequest(content);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP response: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(request.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
	public void onSendRoutingInformationResponse(SendRoutingInformationResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onSendRoutingInformationResponse      ]]]]]]]]]]");
            logger.debug(response);

            var subscriberInfo = Objects.requireNonNullElse(response.getSubscriberInfo(), new SubscriberInfoImpl());
            var imsi = Util.getValueOrElse(response.getIMSI(), IMSI::getData, "");
            var hlrGt = Util.getValueOrElse(response.getMAPDialog().getRemoteAddress().getGlobalTitle(), GlobalTitle::getDigits, "");
            var vmsc = Util.getValueOrElse(response.getVmscAddress(), AddressString::getAddress, "");
            var locationInfo = Objects.requireNonNullElse(subscriberInfo.getLocationInformation(), new LocationInformationImpl());
            var vlr = Util.getValueOrElse(locationInfo.getVlrNumber(), AddressString::getAddress, "");
            var routingInfo = Objects.requireNonNullElse(response.getRoutingInfo2(), new RoutingInfoImpl());
            var msrn1 = Util.getValueOrElse(routingInfo.getRoamingNumber(), ISDNAddressString::getAddress, "");
            var extendedRoutingInfo = Util.getValueOrElse(response.getExtendedRoutingInfo(), ExtendedRoutingInfo::getRoutingInfo, new RoutingInfoImpl());
            var msrn2 = Util.getValueOrElse(extendedRoutingInfo.getRoamingNumber(), ISDNAddressString::getAddress, "");
            var content = SriResponseImpl.builder()
                    .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog())
                    .imsi(imsi).hlrGt(hlrGt).vmscGt(vmsc).vlrGt(vlr).msrn1(msrn1).msrn2(msrn2)
                    .build();
            for (var listener : listeners) {
                listener.onSendRoutingInformationResponse(content);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP response: " + e.getMessage();
            logger.error(msg, e);
            // TODO SS7: Check invoke ID on all modules to make sure that event is sent to correct listener
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
	public void onProvideRoamingNumberRequest(ProvideRoamingNumberRequest request) {
        logger.debug("[[[[[[[[[[    onProvideRoamingNumberRequest      ]]]]]]]]]]");
        logger.debug(request);
        var content = PrnRequestImpl.builder()
                .invokeId(request.getInvokeId()).mapDialog(request.getMAPDialog())
                .build();
        for (var listener : listeners) {
            listener.onProvideRoamingNumberRequest(content);
        }

    }

    @Override
	public void onProvideRoamingNumberResponse(ProvideRoamingNumberResponse response) {
        logger.debug("[[[[[[[[[[    onProvideRoamingNumberResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    @Override
	public void onIstCommandRequest(IstCommandRequest request) {
        logger.debug("[[[[[[[[[[    onIstCommandRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
	public void onIstCommandResponse(IstCommandResponse response) {
        logger.debug("[[[[[[[[[[    onIstCommandResponse      ]]]]]]]]]]");
        logger.debug(response);
    }
}
