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

package com.rodan.intruder.ss7.gateway;

import com.rodan.connectivity.ss7.adapter.*;
import com.rodan.intruder.ss7.entities.dialog.Ss7CapDialog;
import com.rodan.intruder.ss7.entities.dialog.Ss7MapDialog;
import com.rodan.intruder.ss7.entities.event.dialog.CapDialogEventListener;
import com.rodan.intruder.ss7.entities.event.dialog.MapDialogEventListener;
import com.rodan.intruder.ss7.entities.event.model.MapMessage;
import com.rodan.intruder.ss7.entities.event.model.error.details.ReturnErrorProblemType;
import com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener;
import com.rodan.intruder.ss7.entities.payload.Ss7Payload;
import com.rodan.intruder.ss7.entities.payload.mobility.IsdForwarderPayload;
import com.rodan.intruder.ss7.entities.payload.sms.FsmForwarderPayload;
import com.rodan.intruder.ss7.gateway.dialog.Ss7CapDialogImpl;
import com.rodan.intruder.ss7.gateway.dialog.Ss7MapDialogImpl;
import com.rodan.intruder.ss7.gateway.fowrarderpayload.mobility.IsdForwarderPayloadImpl;
import com.rodan.intruder.ss7.gateway.fowrarderpayload.sms.FsmForwarderPayloadImpl;
import com.rodan.intruder.ss7.gateway.handler.*;
import com.rodan.intruder.ss7.gateway.handler.model.mobility.IsdRequestImpl;
import com.rodan.intruder.ss7.gateway.handler.model.sms.FsmRequestImpl;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.config.node.SctpMode;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.config.node.config.SepNodeConfig;
import com.rodan.library.model.config.node.config.StpNodeConfig;
import com.rodan.library.model.config.node.info.Ss7NodeInfo;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.library.util.LongRunningTask;
import com.rodan.library.util.Util;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Ss7GatewayImpl implements Ss7Gateway {
	private static final int TASK_CHECK_INTERVAL = 500;
	private static final int TASK_WAIT_TIME = 20000;
	private static final int SST_TASK_CHECK_INTERVAL = 500;
	private static final int SST_TASK_WAIT_TIME = 13000;

	final static Logger logger = LogManager.getLogger(Ss7GatewayImpl.class);

	private JSs7StackAdapter stack;
	private NodeConfig nodeConfig;
	private boolean connected;

	private Map<Integer, MapDialogEventHandler> mapDialogEventListeners;
	private Map<Integer, MapMobilityServiceHandler> mapMobilityServiceListeners;
	private Map<Integer, MapSmsServiceHandler> mapSmsServiceListeners;
	private Map<Integer, MapOamServiceHandler> mapOamServiceListeners;
	private Map<Integer, MapPdpServiceHandler> mapPdpServiceListeners;
	private Map<Integer, MapLcsServiceHandler> mapLcsServiceListeners;
	private Map<Integer, MapCallHandlingServiceHandler> mapCallHandlingServiceListeners;

	private Map<Integer, CapDialogEventHandler> capDialogEventListeners;
	private Map<Integer, CapCsCallHandler> capCsCallListeners;

	protected List<BiConsumer<String, NotificationType>> notificationListeners;

	@Builder
	public Ss7GatewayImpl(NodeConfig nodeConfig) {
		this.nodeConfig = nodeConfig;
		this.connected = false;

		this.mapDialogEventListeners = new HashMap<>();
		this.mapMobilityServiceListeners = new HashMap<>();
		this.mapSmsServiceListeners = new HashMap<>();
		this.mapOamServiceListeners = new HashMap<>();
		this.mapPdpServiceListeners = new HashMap<>();
		this.mapLcsServiceListeners = new HashMap<>();
		this.mapCallHandlingServiceListeners = new HashMap<>();

		this.capDialogEventListeners = new HashMap<>();
		this.capCsCallListeners = new HashMap<>();

		this.notificationListeners = new ArrayList<>();
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public void connect() throws SystemException {
		if (connected) {
			var msg = "Node is already connected";
			throw SystemException.builder().code(ErrorCode.SS7_CONNECTION_INITIALIZATION).message(msg).build();
		}

		if (nodeConfig instanceof SepNodeConfig cfg) {
			var peerAddress = cfg.getSs7Association().getPeerNode().getAddress();
			notify("Connecting to SS7 network via " + peerAddress + "...", NotificationType.PROGRESS);
		}

		initialize();
		startStack(); // MAP should be started after activating MAP services

		if (nodeConfig.getSctpMode() == SctpMode.CLIENT) {
			waitForSctpAssociation();
			waitForM3uaLink();
			waitForAspActivation();
			waitForSsnActivation();
		}
		connected = true;
		notify("Connected!", NotificationType.SUCCESS);
	}

	@Override
	public Ss7MapDialog generateMapDialog(Ss7Payload payload) throws SystemException {
		var payloadWrapper = PayloadMapper.mapMapPayload(payload, stack, nodeConfig);
		var jss7Dialog = stack.generateMapDialog(payloadWrapper);
		return Ss7MapDialogImpl.builder().jss7Dialog(jss7Dialog).build();
	}

	@Override
	public Ss7CapDialog generateCapDialog(Ss7Payload payload) throws SystemException {
		var payloadWrapper = PayloadMapper.mapCapPayload(payload, stack, nodeConfig);
		var jss7Dialog = stack.generateCapDialog(payloadWrapper);
		return Ss7CapDialogImpl.builder().jss7Dialog(jss7Dialog).build();
	}

	@Override
	public void addToDialog(Ss7Payload payload, Ss7MapDialog dialog) throws SystemException {
		var payloadWrapper = PayloadMapper.mapMapPayload(payload, stack, nodeConfig);
		var jss7Dialog = ((Ss7MapDialogImpl) dialog).getJss7Dialog();
		stack.addToDialog(payloadWrapper, jss7Dialog);
	}

	@Override
	public void addToDialog(Ss7Payload payload, Ss7CapDialog dialog) throws SystemException {
		var payloadWrapper = PayloadMapper.mapCapPayload(payload, stack, nodeConfig);
		var jss7Dialog = ((Ss7CapDialogImpl) dialog).getJss7Dialog();
		stack.addToDialog(payloadWrapper, jss7Dialog);
	}

	@Override
	public Ss7Payload generateForwarderPayload(MapMessage message, Ss7Payload payload) throws SystemException {
		Ss7Payload forwarderPayload = null;
		if (message instanceof IsdRequestImpl m) {
			var isd = m.getOriginalRequest();
			forwarderPayload = IsdForwarderPayloadImpl.forwarderBuilder().request(isd)
					.payload((IsdForwarderPayload) payload).build();

		} else if (message instanceof FsmRequestImpl m) {
			var fsm = m.getOriginalRequest();
			var mtFsm = m.getOriginalMtRequest();
			forwarderPayload = FsmForwarderPayloadImpl.forwarderBuilder().request(fsm).mtRequest(mtFsm)
					.payload((FsmForwarderPayload) payload).build();

		} else {
			var msg = "Invalid MAP message provided: " + message.getClass();
			logger.error(msg);
			throw SystemException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).build();
		}
		return forwarderPayload;
	}

	@Override
	public void send(Ss7MapDialog dialog) throws SystemException {
		((Ss7MapDialogImpl) dialog).send();
	}

	@Override
	public void sendMalformedAcn(Ss7MapDialog dialog) throws SystemException {
		((Ss7MapDialogImpl) dialog).sendMalformedAcn();
	}

	@Override
	public void sendRejectComponent(Ss7MapDialog dialog, long invokeId, ReturnErrorProblemType type) throws SystemException {
		((Ss7MapDialogImpl) dialog).sendRejectComponent(invokeId, type);
	}

	@Override
	public void close(Ss7MapDialog dialog) throws SystemException {
		((Ss7MapDialogImpl) dialog).close();
	}

	@Override
	public void send(Ss7CapDialog dialog) throws SystemException {
		((Ss7CapDialogImpl) dialog).send();
	}

	@Override
	public void addMapDialogEventListener(int ssn, MapDialogEventListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		mapDialogEventListeners.get(ssn).addListener(listener);
	}

	@Override
	public void removeMapDialogEventListener(int ssn, MapDialogEventListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		mapDialogEventListeners.get(ssn).removeListener(listener);
	}

	@Override
	public void addCapDialogEventListener(int ssn, CapDialogEventListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		capDialogEventListeners.get(ssn).addListener(listener);
	}

	@Override
	public void removeCapDialogEventListener(int ssn, CapDialogEventListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		capDialogEventListeners.get(ssn).removeListener(listener);
	}

	@Override
	public void addMobilityServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		mapMobilityServiceListeners.get(ssn).addListener(listener);
	}

	@Override
	public void removeMobilityServiceListener(int ssn, MapMobilityServiceListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		mapMobilityServiceListeners.get(ssn).removeListener(listener);
	}

	@Override
	public void addSmsServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapSmsServiceListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		mapSmsServiceListeners.get(ssn).addListener(listener);
	}

	@Override
	public void removeSmsServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapSmsServiceListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		mapSmsServiceListeners.get(ssn).removeListener(listener);
	}

	@Override
	public void addOamServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapOamServiceListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		mapOamServiceListeners.get(ssn).addListener(listener);
	}

	@Override
	public void removeOamServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapOamServiceListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		mapOamServiceListeners.get(ssn).removeListener(listener);
	}

	@Override
	public void addPdpServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapPdpServiceListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		mapPdpServiceListeners.get(ssn).addListener(listener);
	}

	@Override
	public void removePdpServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapPdpServiceListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		mapPdpServiceListeners.get(ssn).removeListener(listener);
	}

	@Override
	public void addLcsServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapLcsServiceListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		mapLcsServiceListeners.get(ssn).addListener(listener);
	}

	@Override
	public void removeLcsServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapLcsServiceListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		mapLcsServiceListeners.get(ssn).removeListener(listener);
	}

	@Override
	public void addCallHandlingServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapCallHandlingServiceListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		mapCallHandlingServiceListeners.get(ssn).addListener(listener);
	}

	@Override
	public void removeCallHandlingServiceListener(int ssn, com.rodan.intruder.ss7.entities.event.service.MapCallHandlingServiceListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		mapCallHandlingServiceListeners.get(ssn).removeListener(listener);
	}

	@Override
	public void addCapCsCallListener(int ssn, com.rodan.intruder.ss7.entities.event.service.CapCsCallListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		capCsCallListeners.get(ssn).addListener(listener);
	}

	@Override
	public void removeCapCsCallListener(int ssn, com.rodan.intruder.ss7.entities.event.service.CapCsCallListener listener) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN)
			ssn = Constants.SCCP_MSC_SSN;
		capCsCallListeners.get(ssn).removeListener(listener);
	}

	@Override
	public void addNotificationListener(BiConsumer<String, NotificationType> listener) {
		if (notificationListeners.contains(listener)) {
			logger.warn("Registering NotificationListener for already existing one");
			return;
		}

		notificationListeners.add(listener);
	}

	@Override
	public void removeNotificationListener(BiConsumer<String, NotificationType> listener) {
		if (!notificationListeners.contains(listener)) {
			logger.warn("Removing a non-existing NotificationListener");
			return;
		}

		notificationListeners.remove(listener);
	}

	public ArrayList<Ss7NodeInfo> getPeerNodes() {
		var peerNodes = new ArrayList<Ss7NodeInfo>();
		if (nodeConfig instanceof SepNodeConfig cfg) {
			peerNodes.add(cfg.getSs7Association().getPeerNode());

		} else {
			((StpNodeConfig) nodeConfig).getSs7Association().getPeerNodes().forEach(node -> peerNodes.add(node));
		}
		return peerNodes;
	}

	private void initialize() throws SystemException {
		createStack();
		activateMapServices();
		activateCapServices();
	}

	private void createStack() throws SystemException {
		stack = new JSs7StackAdapter(nodeConfig);
		logger.info("Creating SS7 stack...");
		initSctp();
		initM3ua();
		initSccp();
		if (nodeConfig instanceof SepNodeConfig) {
			initMap();
			initCap();
		}

		if (!stack.getM3uaAdapter().isAspUp()) {
			stack.getM3uaAdapter().startAsp();
		}

		logger.info("SS7 created successfully!");
	}

	private void activateMapServices() throws SystemException {
		if (nodeConfig instanceof SepNodeConfig cfg) {
			var supportedSsnList = cfg.getSs7Association().getLocalNode().getSupportedMapSsnList();
			// Activate all services, not just service relevant to current SSN, since bypass
			// payload may require additional service.
			for (var ssn : supportedSsnList) {
				stack.getMapAdapter(Integer.valueOf(ssn)).activateServices();
			}
		}
	}

	private void activateCapServices() throws SystemException {
		if (nodeConfig instanceof SepNodeConfig cfg) {
			var supportedSsnList = cfg.getSs7Association().getLocalNode().getSupportedCapSsnList();
			for (var ssn : supportedSsnList) {
				stack.getCapAdapter(Integer.valueOf(ssn)).activateServices();
			}
		}
	}

	private void startStack() throws SystemException {
		if (nodeConfig instanceof SepNodeConfig cfg) {
			var supportedSsnList = cfg.getSs7Association().getLocalNode().getSupportedMapSsnList();
			for (var ssn : supportedSsnList) {
				if (Constants.SCCP_INTEGRATED_VLR && (Integer.valueOf(ssn) == Constants.SCCP_VLR_SSN))
					continue;
				stack.getMapAdapter(Integer.valueOf(ssn)).start();
			}

			supportedSsnList = cfg.getSs7Association().getLocalNode().getSupportedCapSsnList();
			for (var ssn : supportedSsnList) {
				stack.getCapAdapter(Integer.valueOf(ssn)).start();
			}
		}
	}

	protected void waitForSctpAssociation() throws SystemException {
		var startStackTask = LongRunningTask.builder()
				.workStartMessage("Waiting for SCTP association...").workWaitMessage(null)
				.workDoneMessage("SCTP association is up.")
				.workFailedMessage("Failed to establish SCTP association.")
				.startWorkAction(null).workWaitAction(null)
				.workDoneCheck(m -> stack.getSctpAdapter().isAssociationUp())
				.waitTime(TASK_WAIT_TIME).checkInterval(TASK_CHECK_INTERVAL)
				.build();
		Util.startLongRunningTask(startStackTask);
	}

	private void waitForM3uaLink() throws SystemException {
		var m3uaAspTask = LongRunningTask.builder()
				.workStartMessage("Waiting for M3UA link...").workWaitMessage(null)
				.workDoneMessage("M3UA link connected successfully.")
				.workFailedMessage("Failed to connect M3UA link.")
				.workDoneCheck(m -> stack.getM3uaAdapter().isConnected())
				.startWorkAction(null)
				.workWaitAction(() -> {
					if (!stack.getM3uaAdapter().isStarted())
						stack.getM3uaAdapter().startAsp();
				})
				.waitTime(TASK_WAIT_TIME).checkInterval(TASK_CHECK_INTERVAL)
				.build();
		Util.startLongRunningTask(m3uaAspTask);
	}

	private void waitForAspActivation() throws SystemException {
		var m3uaAspTask = LongRunningTask.builder()
				.workStartMessage("Waiting for ASP activation...").workWaitMessage(null)
				.workDoneMessage("ASP activated successfully.")
				.workFailedMessage("Failed to activate ASP.")
				.workDoneCheck(m -> stack.getM3uaAdapter().isAspUp())
				.startWorkAction(null)
				.waitTime(TASK_WAIT_TIME).checkInterval(TASK_CHECK_INTERVAL)
				.build();
		Util.startLongRunningTask(m3uaAspTask);
	}

	private void waitForSsnActivation() {
		try {
			notify("Waiting for SSN activation...", NotificationType.PROGRESS);
			var sccpSstTask = LongRunningTask.builder()
					.workStartMessage("Waiting for SSN activation...").workWaitMessage(null)
					.workDoneMessage("Received SST for SSN successfully.")
					.workFailedMessage("Failed to receive SST for all supported SSNs.")
					.workFailedErrorCode(ErrorCode.SST_WAIT_FAILED)
					.workDoneCheck(m -> stack.getSccpAdapter().isAllSsnActivated())
					.startWorkAction(null)
					.waitTime(SST_TASK_WAIT_TIME).checkInterval(SST_TASK_CHECK_INTERVAL)
					.build();
			Util.startLongRunningTask(sccpSstTask);

		} catch (SystemException e) {
			var notActivatedSsnList = stack.getSccpAdapter().getNotActivatedSsnList().map(ssn -> String.valueOf(ssn))
					.collect(Collectors.joining(", ", "{", "}"));
			var msg = String.format("Failed to receive SST for: %s SSNs", notActivatedSsnList);
			notify(msg, NotificationType.WARNING);
		}
	}

	protected void notify(String msg, NotificationType type) {
		logger.info(msg);
		for (var listener : notificationListeners) {
			listener.accept(msg, type);
		}
	}

	// Init configs (SSNs, config dir)
	// Create Stack with configs
	// For client, create multiple MAP and TCAP layers for all local SSNs
	// Add GTT inside SCCP

	// Module has to init payloads and activate services and

	private void initSctp() throws SystemException {
		var sctp = SctpAdapter.getInstance(nodeConfig);
		stack.setSctpAdapter(sctp);
	}
	
	private void initM3ua() throws SystemException {
		var m3ua = M3uaAdapter.getInstance(nodeConfig, stack.getSctpAdapter());
		stack.setM3uaAdapter(m3ua);
	}
	
	private void initSccp() throws SystemException {
		var sccp = SccpAdapter.getInstance(nodeConfig, stack.getM3uaAdapter());
		stack.setSccpAdapter(sccp);
	}

	private void initMap() throws SystemException {
		if (nodeConfig instanceof SepNodeConfig cfg) {
			var supportedSsnList = cfg.getSs7Association().getLocalNode().getSupportedMapSsnList();
			for (var ssnEntry : supportedSsnList) {
				var ssn = Integer.valueOf(ssnEntry);
				if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN) {
					continue;
				}

				mapDialogEventListeners.put(ssn, new MapDialogEventHandler());
				mapMobilityServiceListeners.put(ssn, new MapMobilityServiceHandler());
				mapSmsServiceListeners.put(ssn, new MapSmsServiceHandler());
				mapOamServiceListeners.put(ssn, new MapOamServiceHandler());
				mapPdpServiceListeners.put(ssn, new MapPdpServiceHandler());
				mapLcsServiceListeners.put(ssn, new MapLcsServiceHandler());
				mapCallHandlingServiceListeners.put(ssn, new MapCallHandlingServiceHandler());
				var map = MapAdapter.getInstance(nodeConfig, stack.getSccpAdapter(), ssn,
						mapDialogEventListeners.get(ssn), mapMobilityServiceListeners.get(ssn), mapSmsServiceListeners.get(ssn),
						mapOamServiceListeners.get(ssn), mapPdpServiceListeners.get(ssn), mapLcsServiceListeners.get(ssn),
						mapCallHandlingServiceListeners.get(ssn));
				stack.addMapAdapter(ssn, map);
			}
		}
	}

	private void initCap() throws SystemException {
		if (nodeConfig instanceof SepNodeConfig cfg) {
			var supportedSsnList = cfg.getSs7Association().getLocalNode().getSupportedCapSsnList();
			for (var ssnEntry : supportedSsnList) {
				var ssn = Integer.valueOf(ssnEntry);

				capDialogEventListeners.put(ssn, new CapDialogEventHandler());
				capCsCallListeners.put(ssn, new CapCsCallHandler());
				var cap = CapAdapter.getInstance(nodeConfig, stack.getSccpAdapter(), ssn,
						capDialogEventListeners.get(ssn), capCsCallListeners.get(ssn));
				stack.addCapAdapter(ssn, cap);
			}
		}
	}
}
