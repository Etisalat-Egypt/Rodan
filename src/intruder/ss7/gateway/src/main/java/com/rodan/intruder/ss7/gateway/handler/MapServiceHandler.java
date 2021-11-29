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

import com.rodan.intruder.ss7.entities.event.model.error.MapProblem;
import com.rodan.intruder.ss7.entities.event.model.error.details.ReturnErrorProblemType;
import com.rodan.intruder.ss7.entities.event.model.error.details.AbsentSubscriberDiagnosticSM;
import com.rodan.intruder.ss7.entities.event.model.error.details.AbsentSubscriberReason;
import com.rodan.intruder.ss7.entities.event.model.error.details.AdditionalNetworkResource;
import com.rodan.intruder.ss7.entities.event.model.error.details.CallBarringCause;
import com.rodan.intruder.ss7.entities.event.model.error.details.PositionMethodFailureDiagnostic;
import com.rodan.intruder.ss7.entities.event.model.error.details.UnauthorizedLCSClientDiagnostic;
import com.rodan.intruder.ss7.entities.event.model.error.details.UnknownSubscriberDiagnostic;
import com.rodan.intruder.ss7.entities.event.service.MapServiceListener;
import com.rodan.intruder.ss7.gateway.handler.model.error.ErrorComponentImpl;
import com.rodan.intruder.ss7.gateway.handler.model.error.RejectComponentImpl;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPMessage;
import org.mobicents.protocols.ss7.map.api.MAPServiceListener;
import org.mobicents.protocols.ss7.map.api.errors.*;
import org.mobicents.protocols.ss7.tcap.asn.comp.Problem;

import java.util.ArrayList;
import java.util.List;

public class MapServiceHandler implements MAPServiceListener {
    Logger logger = LogManager.getLogger(MapServiceHandler.class);

    @Getter(AccessLevel.PROTECTED) private List<MapServiceListener> baseServiceListeners;

    public MapServiceHandler() {
        baseServiceListeners = new ArrayList<>();
    }

    public void addListener(MapServiceListener listener) {
        logger.debug("Registering MAP listener: " + listener);
        if (baseServiceListeners.contains(listener)) {
            logger.warn("Registering MapServiceListener for already existing one");
            return;
        }

        baseServiceListeners.add(listener);
    }

    public void removeListener(MapServiceListener listener) {
        logger.debug("Removing MAP listener: " + listener);
        if (!baseServiceListeners.contains(listener)) {
            logger.warn("Removing a non-existing MapServiceListener");
            return;
        }

        baseServiceListeners.remove(listener);
    }

    @Override
    public void onErrorComponent(MAPDialog mapDialog, Long invokeId, MAPErrorMessage mapErrorMessage) {
        logger.debug("[[[[[[[[[[    onErrorComponent      ]]]]]]]]]]");
        String msg = String.format("onErrorComponent received for dialogId: [%d], invokeId: [%d], mapErrorMessage: [%s]",
                mapDialog.getLocalDialogId(), invokeId, mapErrorMessage);
        logger.debug(msg);

        var content = parseMapErrorMessage(invokeId, mapDialog, mapErrorMessage);
        for (var listener : baseServiceListeners) {
            listener.onErrorComponent(content);
        }
    }


    @Override
    public void onRejectComponent(MAPDialog mapDialog, Long invokeId, Problem problem, boolean isLocalOriginated) {
        logger.debug("[[[[[[[[[[    onRejectComponent      ]]]]]]]]]]");
        String msg = String.format("onRejectComponent received for dialogId: [%d], invokeId: [%d], problem: [%s], isLocalOriginated: [%b]",
                mapDialog.getLocalDialogId(), invokeId, problem, isLocalOriginated);
        logger.debug(msg);

        var mapProblem = generateMapProblem(problem);
        var content = RejectComponentImpl.builder()
                .invokeId(invokeId).mapDialog(mapDialog)
                .isLocalOriginated(isLocalOriginated).problem(mapProblem)
                .build();
        for (var listener : baseServiceListeners) {
            listener.onRejectComponent(content);
        }
    }

    @Override
    public void onInvokeTimeout(MAPDialog mapDialog, Long invokeId) {
        // TODO IMP: Override in Ss7Template and return error
        logger.debug("[[[[[[[[[[    onInvokeTimeout      ]]]]]]]]]]");
        String msg = String.format("onInvokeTimeout received for dialogId: [%d], invokeId: [%d]",
                mapDialog.getLocalDialogId(), invokeId);
        logger.debug(msg);
    }


    @Override
    public void onMAPMessage(MAPMessage mapMessage) {
        logger.debug("[[[[[[[[[[    onMAPMessage      ]]]]]]]]]]");
        String msg = String.format("onMAPMessage received for mapMessage: [%s], invokeId: [%d]",
                mapMessage, mapMessage.getInvokeId());
        logger.debug(msg);
    }

    private ErrorComponentImpl parseMapErrorMessage(long invokeId, MAPDialog mapDialog, MAPErrorMessage mapErrorMessage) {
        // TODO check for nulls
        var remoteAddress = mapDialog.getRemoteAddress().getGlobalTitle().getDigits();
        var isEmParameterless = false;
        if (mapErrorMessage.isEmParameterless())
            isEmParameterless = true;

        var isEmExtensionContainer = false;
        if (mapErrorMessage.isEmExtensionContainer())
            isEmExtensionContainer = true;

        var isEmFacilityNotSup = false;
        if (mapErrorMessage.isEmFacilityNotSup())
            isEmFacilityNotSup = true;

        var isEmSMDeliveryFailure = false;
        if (mapErrorMessage.isEmSMDeliveryFailure())
            isEmSMDeliveryFailure = true;

        com.rodan.intruder.ss7.entities.event.model.error.details.NetworkResource networkResource = null;
        AdditionalNetworkResource additionalNetworkResource = null;
        var isEmSystemFailure = false;
        if (mapErrorMessage.isEmSystemFailure() && mapErrorMessage instanceof MAPErrorMessageSystemFailure e) {
            isEmSystemFailure = true;
            if (e.getNetworkResource() != null)
                networkResource = com.rodan.intruder.ss7.entities.event.model.error.details.NetworkResource.getInstance(e.getNetworkResource().getCode());
            if (e.getAdditionalNetworkResource() != null)
                additionalNetworkResource = AdditionalNetworkResource.getInstance(e.getAdditionalNetworkResource().getCode());
        }

        UnknownSubscriberDiagnostic unknownSubscriberDiagnostic = null;
        var isEmUnknownSubscriber = false;
        if (mapErrorMessage.isEmUnknownSubscriber() && mapErrorMessage instanceof MAPErrorMessageUnknownSubscriber e) {
            isEmUnknownSubscriber = true;
            if (e.getUnknownSubscriberDiagnostic() != null)
                unknownSubscriberDiagnostic = UnknownSubscriberDiagnostic.getInstance(e.getUnknownSubscriberDiagnostic().getCode());
        }

        AbsentSubscriberReason absentSubscriberReason = null;
        var isEmAbsentSubscriber = false;
        if (mapErrorMessage.isEmAbsentSubscriber() && mapErrorMessage instanceof MAPErrorMessageAbsentSubscriber e) {
            isEmAbsentSubscriber = true;
            if (e.getAbsentSubscriberReason() != null)
                absentSubscriberReason = AbsentSubscriberReason.getInstance(e.getAbsentSubscriberReason().getCode());
        }

        AbsentSubscriberDiagnosticSM absentSubscriberSMDiagnostic = null;
        AbsentSubscriberDiagnosticSM additionalAbsentSubscriberSMDiagnostic = null;
        var isEmAbsentSubscriberSM = false;
        if (mapErrorMessage.isEmAbsentSubscriberSM() && mapErrorMessage instanceof MAPErrorMessageAbsentSubscriberSM e) {
            isEmAbsentSubscriberSM = true;
            if (e.getAbsentSubscriberDiagnosticSM() != null)
                absentSubscriberSMDiagnostic = AbsentSubscriberDiagnosticSM.getInstance(e.getAbsentSubscriberDiagnosticSM().getCode());
            if (e.getAdditionalAbsentSubscriberDiagnosticSM() != null)
                additionalAbsentSubscriberSMDiagnostic = AbsentSubscriberDiagnosticSM.getInstance(e.getAdditionalAbsentSubscriberDiagnosticSM().getCode());
        }

        var isEmSubscriberBusyForMtSms = false;
        if (mapErrorMessage.isEmSubscriberBusyForMtSms())
            isEmSubscriberBusyForMtSms = true;

        CallBarringCause callBarringCause =null;
        var isEmCallBarred = false;
        if (mapErrorMessage.isEmCallBarred() && mapErrorMessage instanceof MAPErrorMessageCallBarred e) {
            isEmCallBarred = true;
            if (e.getCallBarringCause() != null)
                callBarringCause = CallBarringCause.getInstance(e.getCallBarringCause().getCode());
        }

        UnauthorizedLCSClientDiagnostic unauthorizedLCSClientDiagnostic = null;
        var isEmUnauthorizedLCSClient = false;
        if (mapErrorMessage.isEmUnauthorizedLCSClient() && mapErrorMessage instanceof MAPErrorMessageUnauthorizedLCSClient e) {
            isEmUnauthorizedLCSClient = true;
            if (e.getUnauthorizedLCSClientDiagnostic() != null)
                callBarringCause = CallBarringCause.getInstance(e.getUnauthorizedLCSClientDiagnostic().getCode());
        }

        PositionMethodFailureDiagnostic positionMethodFailureDiagnostic = null;
        var isEmPositionMethodFailure = false;
        if (mapErrorMessage.isEmPositionMethodFailure() && mapErrorMessage instanceof MAPErrorMessagePositionMethodFailure e) {
            isEmPositionMethodFailure = true;
            if (e.getPositionMethodFailureDiagnostic() != null)
                positionMethodFailureDiagnostic = PositionMethodFailureDiagnostic.getInstance(e.getPositionMethodFailureDiagnostic().getCode());
        }

        var errorComponent = ErrorComponentImpl.builder()
                .remoteAddress(remoteAddress)
                .invokeId(invokeId).mapDialog(mapDialog)
                .errorCode(mapErrorMessage.getErrorCode())
                .isEmParameterless(isEmParameterless)
                .isEmExtensionContainer(isEmExtensionContainer)
                .isEmFacilityNotSup(isEmFacilityNotSup)
                .isEmSMDeliveryFailure(isEmSMDeliveryFailure)
                .isEmSystemFailure(isEmSystemFailure).networkResource(networkResource).additionalNetworkResource(additionalNetworkResource)
                .isEmUnknownSubscriber(isEmUnknownSubscriber).unknownSubscriberDiagnostic(unknownSubscriberDiagnostic)
                .isEmAbsentSubscriber(isEmAbsentSubscriber).absentSubscriberReason(absentSubscriberReason)
                .isEmSubscriberBusyForMtSms(isEmSubscriberBusyForMtSms)
                .isEmAbsentSubscriberSM(isEmAbsentSubscriberSM).absentSubscriberDiagnosticSM(absentSubscriberSMDiagnostic).additionalAbsentSubscriberSMDiagnostic(additionalAbsentSubscriberSMDiagnostic)
                .isEmCallBarred(isEmCallBarred).callBarringCause(callBarringCause)
                .isEmUnauthorizedLCSClient(isEmUnauthorizedLCSClient).unauthorizedLCSClientDiagnostic(unauthorizedLCSClientDiagnostic)
                .isEmPositionMethodFailure(isEmPositionMethodFailure).positionMethodFailureDiagnostic(positionMethodFailureDiagnostic)
                .build();

        return errorComponent;
    }

    private MapProblem generateMapProblem(Problem problem) {
        com.rodan.intruder.ss7.entities.event.model.error.details.ProblemType problemType = null;
        if (problem.getType() != null)
            problemType = com.rodan.intruder.ss7.entities.event.model.error.details.ProblemType.getInstance((int) problem.getType().getTypeTag());

        com.rodan.intruder.ss7.entities.event.model.error.details.GeneralProblemType generalProblemType = null;
        if (problem.getGeneralProblemType() != null)
            generalProblemType = com.rodan.intruder.ss7.entities.event.model.error.details.GeneralProblemType.getInstance((int) problem.getGeneralProblemType().getType());


        com.rodan.intruder.ss7.entities.event.model.error.details.InvokeProblemType invokeProblemType = null;
        if (problem.getInvokeProblemType() != null)
            invokeProblemType = com.rodan.intruder.ss7.entities.event.model.error.details.InvokeProblemType.getInstance((int) problem.getInvokeProblemType().getType());

        com.rodan.intruder.ss7.entities.event.model.error.details.ReturnErrorProblemType returnErrorProblemType = null;
        if (problem.getReturnErrorProblemType() != null)
            returnErrorProblemType = ReturnErrorProblemType.getInstance((int) problem.getReturnErrorProblemType().getType());

        com.rodan.intruder.ss7.entities.event.model.error.details.ReturnResultProblemType returnResultProblemType = null;
        if (problem.getReturnResultProblemType() != null)
            returnResultProblemType = com.rodan.intruder.ss7.entities.event.model.error.details.ReturnResultProblemType.getInstance((int) problem.getReturnResultProblemType().getType());

        var mapProblem = MapProblem.builder()
                .problemType(problemType).generalProblemType(generalProblemType).invokeProblemType(invokeProblemType)
                .returnErrorProblemType(returnErrorProblemType).returnResultProblemType(returnResultProblemType)
                .build();

        return mapProblem;
    }
}
