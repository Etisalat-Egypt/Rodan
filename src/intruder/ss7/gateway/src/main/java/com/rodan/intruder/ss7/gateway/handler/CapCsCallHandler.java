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

import com.rodan.connectivity.ss7.handler.CapServiceHandler;
import com.rodan.intruder.ss7.entities.event.model.ErrorEvent;
import com.rodan.intruder.ss7.entities.event.service.CapCsCallListener;
import com.rodan.intruder.ss7.gateway.handler.model.camel.IdpRequestImpl;
import com.rodan.library.util.Util;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.cap.api.primitives.CalledPartyBCDNumber;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.*;

import java.util.ArrayList;
import java.util.List;

public class CapCsCallHandler implements CAPServiceCircuitSwitchedCallListener, CapServiceHandler {
    Logger logger = LogManager.getLogger(CapCsCallHandler.class);

    private List<CapCsCallListener> listeners;

    public CapCsCallHandler() {
        listeners = new ArrayList<>();
    }

    public void addListener(CapCsCallListener listener) {
        logger.debug("Registering CAP listener: " + listener);
        if (listeners.contains(listener)) {
            logger.warn("Registering CapCsCallListener for already existing one");
            return;
        }

        listeners.add(listener);
    }

    public void removeListener(CapCsCallListener listener) {
        logger.debug("Removing CAP listener: " + listener);
        if (!listeners.contains(listener)) {
            logger.warn("Removing a non-existing CapCsCallListener");
            return;
        }

        listeners.remove(listener);
    }

    @Override
	public void onInitialDPRequest(InitialDPRequest request) {
        try {
            logger.debug("[[[[[[[[[[    onInitialDPRequest      ]]]]]]]]]]");
            logger.debug(request);
            var calledNumber = Util.getValueOrElse(request.getCalledPartyBCDNumber(), CalledPartyBCDNumber::getAddress, "");
            var content = IdpRequestImpl.builder()
                    .invokeId(request.getInvokeId()).capDialog(request.getCAPDialog())
                    .calledNumber(calledNumber)
                    .build();
            for (var listener : listeners) {
                listener.onInitialDPRequest(content);
            }

        } catch (Exception e) {
            var msg = "Failed to parse MAP response: " + e.getMessage();
            logger.error(msg, e);
            // TODO SS7: Check invoke ID on all modules to make sure that event is sent to correct listener
            var error = ErrorEvent.builder().invokeId(request.getInvokeId()).message(msg).build();
            for (var listener : listeners) {
                listener.onCapMessageHandlingError(error);
            }
        }
    }

    @Override
	public void onRequestReportBCSMEventRequest(RequestReportBCSMEventRequest request) {

    }

    @Override
	public void onApplyChargingRequest(ApplyChargingRequest request) {

    }

    @Override
	public void onEventReportBCSMRequest(EventReportBCSMRequest request) {

    }

    @Override
	public void onContinueRequest(ContinueRequest request) {

    }

    @Override
	public void onContinueWithArgumentRequest(ContinueWithArgumentRequest request) {

    }

    @Override
	public void onApplyChargingReportRequest(ApplyChargingReportRequest request) {

    }

    @Override
	public void onReleaseCallRequest(ReleaseCallRequest request) {

    }

    @Override
	public void onConnectRequest(ConnectRequest request) {

    }

    @Override
	public void onCallInformationRequestRequest(CallInformationRequestRequest request) {

    }

    @Override
	public void onCallInformationReportRequest(CallInformationReportRequest request) {

    }

    @Override
	public void onActivityTestRequest(ActivityTestRequest request) {

    }

    @Override
	public void onActivityTestResponse(ActivityTestResponse response) {

    }

    @Override
	public void onAssistRequestInstructionsRequest(AssistRequestInstructionsRequest request) {

    }

    @Override
	public void onEstablishTemporaryConnectionRequest(EstablishTemporaryConnectionRequest request) {

    }

    @Override
	public void onDisconnectForwardConnectionRequest(DisconnectForwardConnectionRequest request) {

    }

    @Override
	public void onDisconnectLegRequest(DisconnectLegRequest request) {

    }

    @Override
	public void onDisconnectLegResponse(DisconnectLegResponse response) {

    }

    @Override
	public void onDisconnectForwardConnectionWithArgumentRequest(DisconnectForwardConnectionWithArgumentRequest request) {

    }

    @Override
	public void onConnectToResourceRequest(ConnectToResourceRequest request) {

    }

    @Override
	public void onResetTimerRequest(ResetTimerRequest request) {

    }

    @Override
	public void onFurnishChargingInformationRequest(FurnishChargingInformationRequest request) {

    }

    @Override
	public void onSendChargingInformationRequest(SendChargingInformationRequest request) {

    }

    @Override
	public void onSpecializedResourceReportRequest(SpecializedResourceReportRequest request) {

    }

    @Override
	public void onPlayAnnouncementRequest(PlayAnnouncementRequest request) {

    }

    @Override
	public void onPromptAndCollectUserInformationRequest(PromptAndCollectUserInformationRequest request) {

    }

    @Override
	public void onPromptAndCollectUserInformationResponse(PromptAndCollectUserInformationResponse response) {

    }

    @Override
	public void onCancelRequest(CancelRequest request) {

    }

    @Override
	public void onInitiateCallAttemptRequest(InitiateCallAttemptRequest request) {

    }

    @Override
	public void onInitiateCallAttemptResponse(InitiateCallAttemptResponse response) {

    }

    @Override
	public void onMoveLegRequest(MoveLegRequest request) {

    }

    @Override
	public void onMoveLegResponse(MoveLegResponse response) {

    }

    @Override
	public void onCollectInformationRequest(CollectInformationRequest request) {

    }

    @Override
	public void onSplitLegRequest(SplitLegRequest request) {

    }

    @Override
	public void onSplitLegResponse(SplitLegResponse response) {

    }

    @Override
	public void onCallGapRequest(CallGapRequest request) {

    }
}
