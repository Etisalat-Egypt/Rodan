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
import com.rodan.intruder.ss7.entities.event.service.MapOamServiceListener;
import com.rodan.intruder.ss7.gateway.handler.model.oam.SImsiResponseImpl;
import com.rodan.library.util.Util;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.map.api.service.oam.*;

import java.util.ArrayList;
import java.util.List;

public class MapOamServiceHandler extends MapServiceHandler implements MAPServiceOamListener {
    Logger logger = LogManager.getLogger(MapOamServiceHandler.class);

    private List<MapOamServiceListener> listeners;

    public MapOamServiceHandler() {
        listeners = new ArrayList<>();
    }

    public void addListener(MapOamServiceListener listener) {
        logger.debug("Registering MAP listener: " + listener);
        if (listeners.contains(listener)) {
            logger.warn("Registering MapMobilityServiceListener for already existing one");
            return;
        }

        listeners.add(listener);
        getBaseServiceListeners().add(listener);
    }

    public void removeListener(MapOamServiceListener listener) {
        logger.debug("Removing MAP listener: " + listener);
        if (!listeners.contains(listener)) {
            logger.warn("Removing a non-existing MapMobilityServiceListener");
            return;
        }

        listeners.remove(listener);
        getBaseServiceListeners().remove(listener);
    }

    @Override
	public void onActivateTraceModeRequest_Oam(ActivateTraceModeRequest_Oam request) {
        logger.debug("[[[[[[[[[[    onActivateTraceModeRequest_Oam      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
	public void onActivateTraceModeResponse_Oam(ActivateTraceModeResponse_Oam response) {
        logger.debug("[[[[[[[[[[    onActivateTraceModeResponse_Oam      ]]]]]]]]]]");
        logger.debug(response);
    }

    @Override
	public void onSendImsiRequest(SendImsiRequest request) {
        logger.debug("[[[[[[[[[[    onSendImsiRequest      ]]]]]]]]]]");
        logger.debug(request);
    }

    @Override
	public void onSendImsiResponse(SendImsiResponse response) {
        try {
            logger.debug("[[[[[[[[[[    onSendImsiResponse      ]]]]]]]]]]");
            logger.debug(response);
            var imsi = Util.getValueOrElseNull(response.getImsi(), IMSI::getData);
            var content = SImsiResponseImpl.builder()
                    .invokeId(response.getInvokeId()).mapDialog(response.getMAPDialog())
                    .imsi(imsi)
                    .build();
            for (var listener : listeners) {
                listener.onSendImsiResponse(content);
            }

        } catch (Exception e) {
            // TODO IMP this will catch Exceptions raised inside MapOamServiceListener. Fix
            var msg = "Failed to parse MAP response: " + e.getMessage();
            logger.error(msg, e);
            var error = ErrorEvent.builder().invokeId(response.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onMapMessageHandlingError(error);
            }
        }
    }
}
