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
import com.rodan.intruder.ss7.entities.event.service.MapPdpServiceListener;
import com.rodan.intruder.ss7.gateway.handler.model.pdp.SriGprsResponseImpl;
import com.rodan.library.util.Util;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.map.api.service.pdpContextActivation.MAPServicePdpContextActivationListener;
import org.mobicents.protocols.ss7.map.api.service.pdpContextActivation.SendRoutingInfoForGprsRequest;
import org.mobicents.protocols.ss7.map.api.service.pdpContextActivation.SendRoutingInfoForGprsResponse;

import java.util.ArrayList;
import java.util.List;

public class MapPdpServiceHandler extends MapServiceHandler implements MAPServicePdpContextActivationListener {
    Logger logger = LogManager.getLogger(MapPdpServiceHandler.class);

    private List<MapPdpServiceListener> listeners;

    public MapPdpServiceHandler() {
        listeners = new ArrayList<>();
    }

    public void addListener(MapPdpServiceListener listener) {
        logger.debug("Registering MAP listener: " + listener);
        if (listeners.contains(listener)) {
            logger.warn("Registering MapPdpServiceListener for already existing one");
            return;
        }

        listeners.add(listener);
        getBaseServiceListeners().add(listener);
    }

    public void removeListener(MapPdpServiceListener listener) {
        logger.debug("Removing MAP listener: " + listener);
        if (!listeners.contains(listener)) {
            logger.warn("Removing a non-existing MapPdpServiceListener");
            return;
        }

        listeners.remove(listener);
        getBaseServiceListeners().remove(listener);
    }

    @Override
    public void onSendRoutingInfoForGprsRequest(SendRoutingInfoForGprsRequest request) {
        logger.debug("[[[[[[[[[[    onSendRoutingInfoForGprsRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
    public void onSendRoutingInfoForGprsResponse(SendRoutingInfoForGprsResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onSendRoutingInfoForGprsResponse      ]]]]]]]]]]");
            logger.debug(response);
            var ggsnAddress = Util.toIpString(response.getGgsnAddress().getGSNAddressData());
            var ggsnAddressType = Util.getValueOrElse(response.getGgsnAddress().getGSNAddressAddressType(), Enum::name, "");
            var sgsnAddress = Util.toIpString(response.getSgsnAddress().getGSNAddressData());
            var sgsnAddressType = Util.getValueOrElse(response.getSgsnAddress().getGSNAddressAddressType(), Enum::name, "");
            var notReachableReason = response.getMobileNotReachableReason();
            var notReachable = (notReachableReason != null);
            var content = SriGprsResponseImpl.builder()
                    .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog())
                    .notReachable(notReachable).notReachableReason(notReachableReason)
                    .ggsnAddressType(ggsnAddressType).ggsnAddress(ggsnAddress)
                    .sgsnAddressType(sgsnAddressType).sgsnAddress(sgsnAddress)
                    .build();

            for (var listener : listeners) {
                listener.onSendRoutingInfoForGprsResponse(content);
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
}
