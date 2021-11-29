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

package com.rodan.connectivity.ss7.adapter;

import com.rodan.connectivity.ss7.service.CapCsCallHandlingService;
import com.rodan.library.model.Constants;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.util.Util;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.cap.CAPStackImpl;
import org.mobicents.protocols.ss7.cap.api.CAPDialogListener;
import org.mobicents.protocols.ss7.cap.api.CAPParameterFactory;
import org.mobicents.protocols.ss7.cap.api.CAPStack;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CAPServiceCircuitSwitchedCallListener;
import org.mobicents.protocols.ss7.isup.ISUPParameterFactory;
import org.mobicents.protocols.ss7.tcap.TCAPStackImpl;
import org.mobicents.protocols.ss7.tcap.api.TCAPStack;

import java.util.HashMap;
import java.util.Map;

// Ref: org.restcomm.protocols.ss7.cap.CallScfExample
public class CapAdapter {
    final static Logger logger = LogManager.getLogger(CapAdapter.class);

    private static Map<Integer, CapAdapter> cache;

    private int supportedSsn;
    private CAPStack stack;
    private TCAPStack tcapStack;
    private NodeConfig nodeConfig;
    @Getter private boolean started;
    @Getter private CapCsCallHandlingService csCallHandlingService;

    @Setter private CAPDialogListener dialogEventListener;
    @Setter private CAPServiceCircuitSwitchedCallListener capCsCallListener;

    static {
        cache = new HashMap<>();
    }

    private CapAdapter(NodeConfig nodeConfig, int supportedSsn, CAPDialogListener dialogEventListener,
                       CAPServiceCircuitSwitchedCallListener capCsCallListener) {
        this.supportedSsn = supportedSsn;
        this.nodeConfig = nodeConfig;
        this.started = false;
        this.dialogEventListener = dialogEventListener;
        this.capCsCallListener = capCsCallListener;
    }

    public static CapAdapter getInstance(NodeConfig nodeConfig, SccpAdapter sccpAdapter, int supportedSsn,
                                         CAPDialogListener dialogEventListener, CAPServiceCircuitSwitchedCallListener capCsCallListener) throws SystemException {
        CapAdapter capAdapter;
        if (cache.containsKey(supportedSsn)) {
            logger.debug("Using cached CAP layer");
            capAdapter = cache.get(supportedSsn);

        } else {
            logger.debug("Creating a new CAP layer...");
            capAdapter = new CapAdapter(nodeConfig, supportedSsn, dialogEventListener, capCsCallListener);
            capAdapter.initialize(sccpAdapter);
            cache.put(supportedSsn, capAdapter);
        }

        return capAdapter;
    }

    private void initialize(SccpAdapter sccpAdapter) throws SystemException {
        try {
            logger.debug("Initializing TCAP & CAP layer ....");
            var nodeName = nodeConfig.getSs7Association().getLocalNode().getNodeName();
            var configDir = Util.getConfigDir(nodeName);
            var tcapMaxDialogs = 65535;

            stack = new CAPStackImpl(nodeName, sccpAdapter.getStack().getSccpProvider(), supportedSsn);
            tcapStack = this.stack.getTCAPStack();
            ((TCAPStackImpl) tcapStack).setPersistDir(configDir);
            tcapStack.start();
            tcapStack.setDialogIdleTimeout(Constants.CAP_DIALOG_IDLE_TIMEOUT);
            tcapStack.setInvokeTimeout(Constants.CAP_INVOKE_TIMEOUT);
            tcapStack.setMaxDialogs(tcapMaxDialogs);

            csCallHandlingService = CapCsCallHandlingService.builder().stack(stack).build();
            logger.debug("TCAP & CAP layers initialized successfully!");

        } catch (Exception e) {
            var msg = "Failed to initialize TCAP & CAP layers.";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.CAP_INITIALIZATION).message(msg).build();
        }
    }

    public void start() throws SystemException {
        try {
            logger.debug("Starting CAP for SSN: " + stack.getTCAPStack().getSubSystemNumber());
            if (!started) {
                stack.start();
                addServiceListeners();
                started = true;

            } else {
                logger.error("CAP layer already started.");
                throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
            }

        } catch (Exception e) {
            logger.error("Failed to start CAP layer.", e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
        }
    }

    public void stop() throws SystemException {
        try {
            if (started) {
                stack.stop();
                started = false;
            } else {
                logger.error("CAP layer is not started.");
                throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
            }

        } catch (Exception e) {
            logger.error("Failed to stop CAP layer.", e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
        }
    }

    public void activateServices() throws SystemException {
        if (started) {
            String msg = "Cannot activate CAP services after starting CAP layer.";
            logger.error(msg);
            throw SystemException.builder().code(ErrorCode.SS7_CONNECTION_INITIALIZATION).message(msg).build();
        }

        csCallHandlingService.activate();
    }

    public CAPParameterFactory getParamFactory() { return stack.getCAPProvider().getCAPParameterFactory(); }

    public ISUPParameterFactory getIsupParamFactory() { return stack.getCAPProvider().getISUPParameterFactory(); }

    private void addServiceListeners() {
        var provider = stack.getCAPProvider();
        provider.addCAPDialogListener(dialogEventListener);
        provider.getCAPServiceCircuitSwitchedCall().addCAPServiceListener(capCsCallListener);
        // Cleanup: stack.getCAPProvider().removeCAPDialogListener(dialogEventHandler);
    }
}
