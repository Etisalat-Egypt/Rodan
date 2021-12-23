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

package com.rodan.intruder.ss7.usecases;

import com.rodan.intruder.ss7.entities.event.model.error.DialogUserAbort;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.kernel.usecases.SignalingModuleTemplate;
import com.rodan.intruder.ss7.entities.dialog.Ss7MapDialog;
import com.rodan.intruder.ss7.entities.event.dialog.CapDialogEventListener;
import com.rodan.intruder.ss7.entities.event.dialog.MapDialogEventListener;
import com.rodan.intruder.ss7.entities.event.model.ErrorEvent;
import com.rodan.intruder.ss7.entities.event.service.CapServiceListener;
import com.rodan.intruder.ss7.entities.event.service.MapServiceListener;
import com.rodan.intruder.ss7.entities.payload.Ss7Payload;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.error.ApplicationException;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.library.util.LongRunningTask;
import com.rodan.library.util.Util;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class Ss7ModuleTemplate extends SignalingModuleTemplate<Ss7ModuleOptions, Ss7Payload>
		implements SignalingModule, MapServiceListener, CapServiceListener, MapDialogEventListener, CapDialogEventListener {
	final static Logger logger = LogManager.getLogger(Ss7ModuleTemplate.class);

	@Getter(AccessLevel.PROTECTED) private Ss7Gateway gateway;
	@Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) private Ss7MapDialog dialog;
	@Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) private Ss7Payload bypassPayload;

	protected boolean mapComponentRejectReceived;

	public Ss7ModuleTemplate(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
		super(moduleOptions, true);
		this.gateway = gateway;
	}

	protected void prepareStack() throws ApplicationException {
		logger.debug("Preparing stack");
		if (!gateway.isConnected()) {
			for (var listener : getNotificationListeners()) {
				gateway.addNotificationListener(listener);
			}
			gateway.connect();
		}
	}

	@Override
	protected void addServiceListener() throws SystemException {
		logger.debug("Adding service listeners");
		var localSsn = getMainPayload().getLocalSsn();
		getGateway().addMapDialogEventListener(localSsn, this);
	}

	@Override
	protected void execute() throws ApplicationException {
		logger.debug("currentPayload: " + getCurrentPayload());
		var payloadName = getCurrentPayload().getPayloadName();
		var shouldDisplayNotifications = !(this instanceof Ss7BruteforceModuleTemplate);
		if (shouldDisplayNotifications)
			notify("Sending " + payloadName + " message.", NotificationType.PROGRESS);

		var dialog = gateway.generateMapDialog(getCurrentPayload());
		setDialog(dialog);
		if (bypassPayload != null) {
			setTaskWaitTime(getTaskWaitTime() * 2);
			// Abuse OpCode Tag is for each MAP component (payload)
			var abuseOpcodeTag = bypassPayload.isAbuseOpcodeTagForBypass();
			var malformedAcn = bypassPayload.isMalformedAcnForBypass();
			dialog.setAbuseOpcodeTag(abuseOpcodeTag);
			var bypassName = bypassPayload.getPayloadName().toUpperCase();
			if (shouldDisplayNotifications)
				notify("Hiding " + payloadName + " after " + bypassName + " message.", NotificationType.PROGRESS);
			gateway.addToDialog(bypassPayload, dialog);

			abuseOpcodeTag = getCurrentPayload().isAbuseOpcodeTagForBypass();
			dialog.setAbuseOpcodeTag(abuseOpcodeTag);
			gateway.addToDialog(getCurrentPayload(), dialog);

			if (malformedAcn) {
				gateway.sendMalformedAcn(dialog);
			} else {
				gateway.send(dialog);
			}

			waitForMapComponentReject();
			if (shouldDisplayNotifications)
				notify("Resending " + payloadName + " using TCAP continue", NotificationType.PROGRESS);
			gateway.addToDialog(getCurrentPayload(), dialog);
			if (malformedAcn) {
				gateway.sendMalformedAcn(dialog);
			} else {
				gateway.send(dialog);
			}

		}  else {
			var abuseOpcodeTag = getCurrentPayload().isAbuseOpcodeTagForBypass();
			var malformedAcn = getCurrentPayload().isMalformedAcnForBypass();
			dialog.setAbuseOpcodeTag(abuseOpcodeTag);
			gateway.addToDialog(getCurrentPayload(), dialog);
			if (malformedAcn) {
				gateway.sendMalformedAcn(dialog);
			} else {
				gateway.send(dialog);
			}
		}

		logger.debug(payloadName + " message send successfully!");
	}

	@Override
	protected void waitForResponse() throws SystemException {
		var isSpoofedSender = "Yes".equalsIgnoreCase(getModuleOptions().getSpoofSender());
		var responseWaitTask = LongRunningTask.builder()
				.workStartMessage("Waiting for server response...").workWaitMessage(null)
				.workDoneMessage("Response received successfully")
				.workFailedMessage(getWaitForResponseFailedMessage())
				.workDoneCheck(m -> isResultReceived() || isExecutionError() || isSpoofedSender)
				.startWorkAction(null)
				.waitTime(getTaskWaitTime()).checkInterval(getTaskCheckInterval())
				.build();
		Util.startLongRunningTask(responseWaitTask);
	}

	@Override
	protected void cleanup() throws SystemException {
		if (getGateway() != null && getMainPayload() != null) {
			var localSsn = getMainPayload().getLocalSsn();
			getGateway().removeMapDialogEventListener(localSsn, this);
		}
	}

	private void waitForMapComponentReject() throws SystemException {
		var m3uaAspTask = LongRunningTask.builder()
				.workStartMessage("Waiting for TCAP reject...").workWaitMessage("Waiting for TCAP reject...")
				.workDoneMessage("TCAP reject received successfully")
				.workFailedMessage("Failed to receive TCAP reject.")
				.workDoneCheck(m -> mapComponentRejectReceived)
				.startWorkAction(null)
				.waitTime(getTaskWaitTime()).checkInterval(getTaskCheckInterval())
				.build();
		Util.startLongRunningTask(m3uaAspTask);
	}

	@Override
	public void onMapMessageHandlingError(ErrorEvent errorEvent) {
		logger.error(errorEvent.getMessage());
		notify(errorEvent.getMessage(), NotificationType.FAILURE);
		setExecutionError(true);
	}

	@Override
	public void onCapMessageHandlingError(ErrorEvent errorEvent) {
		logger.error(errorEvent.getMessage());
		notify(errorEvent.getMessage(), NotificationType.FAILURE);
		setExecutionError(true);
	}

	@Override
	public void onRejectComponent(com.rodan.intruder.ss7.entities.event.model.error.RejectComponent rejectComponent) {
		logger.debug("[[[[[[[[[[    onRejectComponent      ]]]]]]]]]]");
		if (bypassPayload != null) {
			notify("MAP component reject received", NotificationType.PROGRESS);
			mapComponentRejectReceived = true;

		} else {
			var msg = String.format("rejectComponent: [%s]", rejectComponent);
			logger.error(msg);
			msg = "MAP component reject received. Problem: " + rejectComponent.getProblem();
			setExecutionError(true);
			notify(msg, NotificationType.FAILURE);
		}
	}

	@Override
	public void onErrorComponent(com.rodan.intruder.ss7.entities.event.model.error.ErrorComponent errorComponent) {
		logger.debug("[[[[[[[[[[    onErrorComponent      ]]]]]]]]]]");
		String msg = String.format("errorComponent: [%s]", errorComponent);
		logger.error(msg);
		msg = "MAP component error received: " + errorComponent.getReadableError();
		setExecutionError(true);
		notify(msg, NotificationType.FAILURE);
	}

	@Override
	public void onDialogProviderAbort(com.rodan.intruder.ss7.entities.event.model.error.DialogProviderAbort abort) {
		logger.debug("[[[[[[[[[[    onDialogProviderAbort      ]]]]]]]]]]");
		String msg = String.format("abort: [%s]", abort);
		logger.error(msg);
		setExecutionError(true);
		msg = String.format("MAP dialog provider abort received: [%s]", abort.getReadableError());
		notify(msg, NotificationType.FAILURE);
	}

	@Override
	public void onDialogUserAbort(DialogUserAbort abort) {
		logger.debug("[[[[[[[[[[    onDialogUserAbort      ]]]]]]]]]]");
		String msg = String.format("onDialogUserAbort: [%s]", abort);
		logger.error(msg);
		setExecutionError(true);
		msg = String.format("MAP dialog user abort received. Reason: [%s]", abort.getReadableError());
		notify(msg, NotificationType.FAILURE);
	}

	@Override
	public void onDialogReject(com.rodan.intruder.ss7.entities.event.model.error.DialogReject reject) {
		logger.debug("[[[[[[[[[[    onDialogReject      ]]]]]]]]]]");
		String msg = String.format("reject: [%s]", reject);
		logger.error(msg);
		setExecutionError(true);
		msg = "MAP dialog reject received. RefuseReason: " + reject.getRefuseReason() + ", returnCause: " + reject.getTcapReturnCauseValue();
		notify(msg, NotificationType.FAILURE);
	}
}
