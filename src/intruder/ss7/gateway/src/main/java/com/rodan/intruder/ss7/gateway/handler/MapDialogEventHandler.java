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

import com.rodan.intruder.ss7.gateway.dialog.Ss7MapDialogImpl;
import com.rodan.intruder.ss7.gateway.handler.model.error.DialogProviderAbortImpl;
import com.rodan.intruder.ss7.gateway.handler.model.error.DialogRejectImpl;
import com.rodan.intruder.ss7.gateway.handler.model.error.DialogUserAbortImpl;
import com.rodan.intruder.ss7.entities.event.dialog.MapDialogEventListener;
import com.rodan.intruder.ss7.entities.event.model.error.DialogProviderAbort;
import com.rodan.intruder.ss7.entities.event.model.error.DialogReject;
import com.rodan.intruder.ss7.entities.event.model.error.DialogUserAbort;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPDialogListener;
import org.mobicents.protocols.ss7.map.api.dialog.*;
import org.mobicents.protocols.ss7.map.api.primitives.AddressString;
import org.mobicents.protocols.ss7.map.api.primitives.MAPExtensionContainer;
import org.mobicents.protocols.ss7.tcap.asn.ApplicationContextName;
import org.mobicents.protocols.ss7.tcap.asn.ApplicationContextNameImpl;

import java.util.ArrayList;
import java.util.List;

public class MapDialogEventHandler implements MAPDialogListener {
    Logger logger = LogManager.getLogger(MapDialogEventHandler.class);

    private List<MapDialogEventListener> listeners;

    public MapDialogEventHandler() {
        listeners = new ArrayList<>();
    }

    public void addListener(MapDialogEventListener listener) {
        logger.debug("Registering MAP listener: " + listener);
        if (listeners.contains(listener)) {
            logger.warn("Registering MapDialogEventListener for already existing one");
            return;
        }

        listeners.add(listener);
    }

    public void removeListener(MapDialogEventListener listener) {
        logger.debug("Removing MAP listener: " + listener);
        if (!listeners.contains(listener)) {
            logger.warn("Removing a non-existing MapDialogEventListener");
            return;
        }

        listeners.remove(listener);
    }

    @Override
    public void onDialogAccept(MAPDialog mapDialog, MAPExtensionContainer extensionContainer) {
        logger.debug("[[[[[[[[[[    onDialogAccept      ]]]]]]]]]]");
        String msg = String.format("onDialogAccept received for dialogId: [%d], extensionContainer: [%s]",
                mapDialog.getLocalDialogId(), extensionContainer);
        logger.debug(msg);
    }

    @Override
	public void onDialogClose(MAPDialog mapDialog) {
        logger.debug("[[[[[[[[[[    onDialogClose      ]]]]]]]]]]");
        String msg = String.format("onDialogClose received for dialogId: [%d]", mapDialog.getLocalDialogId());
        logger.debug(msg);
    }

    @Override
	public void onDialogDelimiter(MAPDialog mapDialog) {
        logger.debug("[[[[[[[[[[    onDialogDelimiter      ]]]]]]]]]]");
        String msg = String.format("onDialogDelimiter received for dialogId: [%d]", mapDialog.getLocalDialogId());
        logger.debug(msg);
        var content = Ss7MapDialogImpl.builder()
                .jss7Dialog(mapDialog)
                .build();
        for (var listener : listeners) {
            listener.onDialogDelimiter(content);
        }
    }

    @Override
	public void onDialogNotice(MAPDialog mapDialog, MAPNoticeProblemDiagnostic noticeProblemDiagnostic) {
//		String msg = String.format("onDialogNotice received for dialogId: [%d], noticeProblemDiagnostic: [%s]",
//				mapDialog.getLocalDialogId(), noticeProblemDiagnostic);
//		logger.debug(msg);
    }

    @Override
	public void onDialogProviderAbort(MAPDialog mapDialog, MAPAbortProviderReason abortProviderReason,
                                       MAPAbortSource abortSource, MAPExtensionContainer extensionContainer) {
        logger.debug("[[[[[[[[[[    onDialogProviderAbort      ]]]]]]]]]]");
		String msg = String.format(
				"onDialogProviderAbort received for dialogId: [%d], abortProviderReason: [%s], abortSource: [%s], extensionContainer: [%s]",
				mapDialog.getLocalDialogId(), abortProviderReason, abortSource, extensionContainer);
		logger.error(msg);
		// TODO: Check for nulls
		var source = switch (abortSource) {
		    case MAPProblem -> DialogProviderAbort.MapAbortSource.MAPProblem;
            case TCProblem -> DialogProviderAbort.MapAbortSource.TCProblem;
            case NetworkServiceProblem -> DialogProviderAbort.MapAbortSource.NetworkServiceProblem;
        };

		var reason = switch (abortProviderReason) {
            case ProviderMalfunction -> DialogProviderAbort.MapAbortProviderReason.ProviderMalfunction;
            case SupportingDialogueTransactionReleased -> DialogProviderAbort.MapAbortProviderReason.SupportingDialogueTransactionReleased;
            case ResourceLimitation -> DialogProviderAbort.MapAbortProviderReason.ResourceLimitation;
            case MaintenanceActivity -> DialogProviderAbort.MapAbortProviderReason.MaintenanceActivity;
            case VersionIncompatibility -> DialogProviderAbort.MapAbortProviderReason.VersionIncompatibility;
            case VersionIncompatibilityTcap -> DialogProviderAbort.MapAbortProviderReason.VersionIncompatibilityTcap;
            case AbnormalMAPDialogueLocal -> DialogProviderAbort.MapAbortProviderReason.AbnormalMAPDialogueLocal;
            case AbnormalMAPDialogueFromPeer -> DialogProviderAbort.MapAbortProviderReason.AbnormalMAPDialogueFromPeer;
            case InvalidPDU -> DialogProviderAbort.MapAbortProviderReason.InvalidPDU;
        };

		var content = DialogProviderAbortImpl.builder()
                .abortSource(source).abortReason(reason)
                .build();
		for (var listener : listeners) {
		    listener.onDialogProviderAbort(content);
        }
    }

    @Override
	public void onDialogReject(MAPDialog mapDialog, MAPRefuseReason refuseReason,
                                ApplicationContextName alternativeApplicationContext, MAPExtensionContainer extensionContainer) {
        logger.debug("[[[[[[[[[[    onDialogReject      ]]]]]]]]]]");
		String msg = String.format(
				"onDialogReject received for dialogId: [%d], refuseReason: [%s], alternativeApplicationContext: [%s], extensionContainer: [%s]",
				mapDialog.getLocalDialogId(), refuseReason, alternativeApplicationContext, extensionContainer);
		logger.error(msg);
        DialogReject.MapRefuseReason reason = null;
        if (refuseReason != null) {
            reason = switch (refuseReason) {
                case ApplicationContextNotSupported -> DialogReject.MapRefuseReason.ApplicationContextNotSupported;
                case InvalidDestinationReference -> DialogReject.MapRefuseReason.InvalidDestinationReference;
                case InvalidOriginatingReference -> DialogReject.MapRefuseReason.InvalidOriginatingReference;
                case NoReasonGiven -> DialogReject.MapRefuseReason.NoReasonGiven;
                case RemoteNodeNotReachable -> DialogReject.MapRefuseReason.RemoteNodeNotReachable;
                case PotentialVersionIncompatibility -> DialogReject.MapRefuseReason.PotentialVersionIncompatibility;
                case PotentialVersionIncompatibilityTcap -> DialogReject.MapRefuseReason.PotentialVersionIncompatibilityTcap;
            };
        }
		var appCtx = (alternativeApplicationContext != null)?
                ((ApplicationContextNameImpl) alternativeApplicationContext).getStringValue() : "";
        var content = DialogRejectImpl.builder()
                .mapDialog(mapDialog)
                .refuseReason(reason).applicationContextName(appCtx)
                .build();
        for (var listener : listeners) {
            listener.onDialogReject(content);
        }
    }

    @Override
	public void onDialogRelease(MAPDialog mapDialog) {
        logger.debug("[[[[[[[[[[    onDialogRelease      ]]]]]]]]]]");
        String msg = String.format("onDialogRelease received for dialogId: [%d]", mapDialog.getLocalDialogId());
        logger.debug(msg);
    }

    @Override
	public void onDialogRequest(MAPDialog mapDialog, AddressString destReference, AddressString origReference,
                                MAPExtensionContainer extensionContainer) {
		String msg = String.format(
				"onDialogRequest received for dialogId: [%d], destReference: [%s], origReference: [%s], acn: [%s], extensionContainer: [%s]",
				mapDialog.getLocalDialogId(), destReference, origReference,
                mapDialog.getApplicationContext().getApplicationContextName().toString(), extensionContainer);
		logger.debug(msg);
    }

    @Override
	public void onDialogRequestEricsson(MAPDialog mapDialog, AddressString destReference, AddressString origReference,
                                        AddressString eriMsisdn, AddressString eriVlrNo) {
        logger.debug("[[[[[[[[[[    onDialogRequestEricsson      ]]]]]]]]]]");
        String msg = String.format(
                "onDialogRequestEricsson received for dialogId: [%d], destReference: [%s], origReference: [%s], acn: [%s], eriMsisdn: [%s], eriVlrNo: [%s]",
                mapDialog.getLocalDialogId(), destReference.toString(), origReference.toString(),
                mapDialog.getApplicationContext().getApplicationContextName().toString(), eriMsisdn, eriVlrNo);
        logger.debug(msg);
    }

    @Override
	public void onDialogTimeout(MAPDialog mapDialog) {
        logger.debug("[[[[[[[[[[    onDialogTimeout      ]]]]]]]]]]");
        String msg = String.format("onDialogTimeout received for dialogId: [%d]", mapDialog.getLocalDialogId());
        logger.debug(msg);
    }

    @Override
	public void onDialogUserAbort(MAPDialog mapDialog, MAPUserAbortChoice userReason,
                                   MAPExtensionContainer extensionContainer) {
        logger.debug("[[[[[[[[[[    onDialogUserAbort      ]]]]]]]]]]");
		String msg = String.format(
				"onDialogUserAbort received for dialogId: [%d], userReason: [%s], extensionContainer: [%s]",
				mapDialog.getLocalDialogId(), userReason.toString(), extensionContainer);
		logger.error(msg);

        DialogUserAbort.ResourceUnavailableReason resourceUnavailableReason = null;
        if (userReason.getResourceUnavailableReason() != null) {
            resourceUnavailableReason = switch (userReason.getResourceUnavailableReason()) {
                case shortTermResourceLimitation -> DialogUserAbort.ResourceUnavailableReason.shortTermResourceLimitation;
                case longTermResourceLimitation -> DialogUserAbort.ResourceUnavailableReason.longTermResourceLimitation;
            };
        }

        DialogUserAbort.ProcedureCancellationReason procedureCancellationReason = null;
        if (userReason.getProcedureCancellationReason() != null) {
            procedureCancellationReason = switch (userReason.getProcedureCancellationReason()) {
                case handoverCancellation -> DialogUserAbort.ProcedureCancellationReason.handoverCancellation;
                case radioChannelRelease -> DialogUserAbort.ProcedureCancellationReason.radioChannelRelease;
                case networkPathRelease -> DialogUserAbort.ProcedureCancellationReason.networkPathRelease;
                case callRelease -> DialogUserAbort.ProcedureCancellationReason.callRelease;
                case associatedProcedureFailure -> DialogUserAbort.ProcedureCancellationReason.associatedProcedureFailure;
                case tandemDialogueRelease -> DialogUserAbort.ProcedureCancellationReason.tandemDialogueRelease;
                case remoteOperationsFailure -> DialogUserAbort.ProcedureCancellationReason.remoteOperationsFailure;
            };
        }

        var content = DialogUserAbortImpl.builder()
                .isUserSpecificReason(userReason.isUserSpecificReason())
                .isUserResourceLimitation(userReason.isUserResourceLimitation())
                .isResourceUnavailableReason(userReason.isResourceUnavailableReason())
                .isProcedureCancellationReason(userReason.isProcedureCancellationReason())
                .resourceUnavailableReason(resourceUnavailableReason).procedureCancellationReason(procedureCancellationReason)

                .build();
        for (var listener : listeners) {
            listener.onDialogUserAbort(content);
        }
    }
}
