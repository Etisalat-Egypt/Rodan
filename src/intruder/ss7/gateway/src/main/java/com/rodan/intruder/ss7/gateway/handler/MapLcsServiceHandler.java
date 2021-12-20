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
import com.rodan.intruder.ss7.entities.event.service.MapLcsServiceListener;
import com.rodan.intruder.ss7.gateway.handler.model.lcs.PslRequestImpl;
import com.rodan.intruder.ss7.gateway.handler.model.lcs.PslResponseImpl;
import com.rodan.intruder.ss7.gateway.handler.model.lcs.SriLcsResponseImpl;
import com.rodan.library.util.Util;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.primitives.AddressString;
import org.mobicents.protocols.ss7.map.api.primitives.CellGlobalIdOrServiceAreaIdOrLAI;
import org.mobicents.protocols.ss7.map.api.primitives.GSNAddress;
import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.map.api.service.lsm.*;
import org.mobicents.protocols.ss7.map.primitives.CellGlobalIdOrServiceAreaIdFixedLengthImpl;

import java.util.ArrayList;
import java.util.List;

public class MapLcsServiceHandler extends MapServiceHandler implements MAPServiceLsmListener {
    Logger logger = LogManager.getLogger(MapLcsServiceHandler.class);

    private List<MapLcsServiceListener> listeners;

    public MapLcsServiceHandler() {
        listeners = new ArrayList<>();
    }

    public void addListener(MapLcsServiceListener listener) {
        logger.debug("Registering MAP listener: " + listener);
        if (listeners.contains(listener)) {
            logger.warn("Registering MapLcsServiceListener for already existing one");
            return;
        }

        listeners.add(listener);
        getBaseServiceListeners().add(listener);
    }

    public void removeListener(MapLcsServiceListener listener) {
        logger.debug("Removing MAP listener: " + listener);
        if (!listeners.contains(listener)) {
            logger.warn("Removing a non-existing MapLcsServiceListener");
            return;
        }

        listeners.remove(listener);
        getBaseServiceListeners().remove(listener);
    }

    @Override
    public void onProvideSubscriberLocationRequest(ProvideSubscriberLocationRequest request) {
        try {
            logger.debug("[[[[[[[[[[    ProvideSubscriberLocationRequest      ]]]]]]]]]]");
            logger.debug(request);
            var imsi = Util.getValueOrElse(request.getIMSI(), IMSI::getData, "");
            var msisdn = Util.getValueOrElse(request.getMSISDN(), AddressString::getAddress, "");
            var mlcGt = Util.getValueOrElse(request.getMlcNumber(), AddressString::getAddress, "");
            var content = PslRequestImpl.builder()
                    .invokeId(request.getInvokeId()).mapDialog(request.getMAPDialog())
                    .imsi(imsi).msisdn(msisdn).mlcNumber(mlcGt)
                    .build();
            for (var listener : listeners) {
                listener.onProvideSubscriberLocationRequest(content);
            }

        }  catch (Exception e) {
            var msg = "Failed to parse MAP response: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(request.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
	public void onProvideSubscriberLocationResponse(ProvideSubscriberLocationResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onProvideSubscriberLocationResponse      ]]]]]]]]]]");
            logger.debug(response);

            var locationInfo = parseLocation(response);
            var saiPresent = response.getSaiPresent();
            var content = PslResponseImpl.builder()
                    .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog())
                    .locationInfo(locationInfo).saiPresent(saiPresent)
                    .build();
            for (var listener : listeners) {
                listener.onProvideSubscriberLocationResponse(content);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP response: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    @Override
	public void onSubscriberLocationReportRequest(SubscriberLocationReportRequest request) {
        logger.debug("[[[[[[[[[[    onSubscriberLocationReportRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
	public void onSubscriberLocationReportResponse(SubscriberLocationReportResponse response) {
        logger.debug("[[[[[[[[[[    onSubscriberLocationReportResponse      ]]]]]]]]]]");
        logger.debug(response);
    }

    @Override
	public void onSendRoutingInfoForLCSRequest(SendRoutingInfoForLCSRequest request) {
        logger.debug("[[[[[[[[[[    onSendRoutingInfoForLCSRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
	public void onSendRoutingInfoForLCSResponse(SendRoutingInfoForLCSResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onSendRoutingInfoForLCSResponse      ]]]]]]]]]]");
            logger.debug(response);
            // The response shall carry whichever of MSISDN/IMSI was not included in the request
            var msisdn = Util.getValueOrElse(response.getTargetMS().getMSISDN(), AddressString::getAddress, "");
            var imsi = Util.getValueOrElse(response.getTargetMS().getIMSI(), IMSI::getData, "");
            var lcsLocationInfo = response.getLCSLocationInfo();
            var sMscNumber = Util.getValueOrElse(lcsLocationInfo.getNetworkNodeNumber(), AddressString::getAddress, "");
            var sGgsnNumber = Util.getValueOrElse(lcsLocationInfo.getAdditionalNumber().getSGSNNumber(), AddressString::getAddress, "");
            var lmsi = Util.encodeHexString(lcsLocationInfo.getLMSI().getData());
            var ggsn = Util.getValueOrElseNull(response.getAdditionalVGmlcAddress(), GSNAddress::getGSNAddressData);
            var vgmlcAddress = Util.getValueOrElseNull(response.getVgmlcAddress(), GSNAddress::getGSNAddressData);
            var hGmlcAddress = Util.getValueOrElseNull(response.getHGmlcAddress(), GSNAddress::getGSNAddressData);
            var pprAddress = Util.getValueOrElseNull(response.getPprAddress(), GSNAddress::getGSNAddressData);
            var additionalVGmlcAddress = Util.getValueOrElseNull(response.getAdditionalVGmlcAddress(), GSNAddress::getGSNAddressData);
            // TODO IMP: fill all data to response
            var content = SriLcsResponseImpl.builder()
                    .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog())
                    .msisdn(msisdn).imsi(imsi).lmsi(lmsi).sMscNumber(sMscNumber).sGsnNumber(sGgsnNumber)
                    .build();
            for (var listener : listeners) {
                listener.onSendRoutingInfoForLCSResponse(content);
            }
        } catch (Exception e) {
            var msg = "Failed to parse MAP response: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }

    private LocationInfo parseLocation(ProvideSubscriberLocationResponse response) throws MAPException {
        var longitude = Util.getValueOrElseNull(response.getLocationEstimate(), ExtGeographicalInformation::getLongitude);
        var latitude = Util.getValueOrElseNull(response.getLocationEstimate(), ExtGeographicalInformation::getLatitude);
        var uncertainty = Util.getValueOrElseNull(response.getLocationEstimate(), ExtGeographicalInformation::getUncertainty);
        var locationAge = response.getAgeOfLocationEstimate();

        var cellInfo = Util.getValueOrElseNull(response.getCellIdOrSai(),
                CellGlobalIdOrServiceAreaIdOrLAI::getCellGlobalIdOrServiceAreaIdFixedLength);
        var laiInfo = Util.getValueOrElseNull(response.getCellIdOrSai(),
                CellGlobalIdOrServiceAreaIdOrLAI::getLAIFixedLength);
        boolean saiPresent = response.getSaiPresent();

        Integer mcc = null, mnc = null, lac = null, cellId = null;
        if (cellInfo != null) {
            mcc = cellInfo.getMCC();
            mnc = cellInfo.getMNC();
            lac = cellInfo.getLac();
            cellId = cellInfo.getCellIdOrServiceAreaCode();
        } else if (laiInfo != null) {
            mcc = laiInfo.getMCC();
            mnc = laiInfo.getMNC();
            lac = laiInfo.getLac();
        }

        var locationInfo = LocationInfo.builder()
                .mcc(mcc).mnc(mnc).lac(lac).cellId(cellId)
                .longitude(longitude).latitude(latitude).uncertainty(uncertainty).ageOfLocationPs(locationAge)
                .build();

        return locationInfo;
    }
}
