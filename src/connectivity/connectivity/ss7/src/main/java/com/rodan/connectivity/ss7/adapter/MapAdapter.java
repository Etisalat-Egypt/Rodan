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

import com.rodan.connectivity.ss7.service.*;
import com.rodan.library.model.Constants;
import com.rodan.library.model.config.node.config.LabNodeConfig;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.util.Util;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.map.MAPProviderImpl;
import org.mobicents.protocols.ss7.map.MAPStackImpl;
import org.mobicents.protocols.ss7.map.api.MAPDialogListener;
import org.mobicents.protocols.ss7.map.api.MAPParameterFactory;
import org.mobicents.protocols.ss7.map.api.MAPSmsTpduParameterFactory;
import org.mobicents.protocols.ss7.map.api.MAPStack;
import org.mobicents.protocols.ss7.map.api.service.callhandling.MAPServiceCallHandlingListener;
import org.mobicents.protocols.ss7.map.api.service.lsm.MAPServiceLsmListener;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPServiceMobilityListener;
import org.mobicents.protocols.ss7.map.api.service.oam.MAPServiceOamListener;
import org.mobicents.protocols.ss7.map.api.service.pdpContextActivation.MAPServicePdpContextActivationListener;
import org.mobicents.protocols.ss7.map.api.service.sms.MAPServiceSmsListener;
import org.mobicents.protocols.ss7.tcap.TCAPStackImpl;
import org.mobicents.protocols.ss7.tcap.api.TCAPStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapAdapter {
    /*
        MAP is a Network Switching Subsystem (NSS) protocol used that is used inside core network only
    */
    final static Logger logger = LogManager.getLogger(MapAdapter.class);

    private static Map<Integer, MapAdapter> cache;

    private int supportedSsn;
    private MAPStack stack;
    private TCAPStack tcapStack;
    private NodeConfig nodeConfig;
    @Getter private boolean started;
    @Getter private MapCallHandlingService callHandlingService;
    @Getter private MapSmsService smsService;
    @Getter private MapPdpService pdpService;
    @Getter private MapLcsService lcsService;
    @Getter private MapMobilityService mobilityService;
    @Getter private MapOamService oamService;

    @Setter private MAPDialogListener dialogEventListener;
    @Setter private MAPServiceMobilityListener mobilityServiceListener;
    @Setter private MAPServiceSmsListener smsServiceListener;
    @Setter private MAPServiceOamListener oamServiceListener;
    @Setter private MAPServicePdpContextActivationListener pdpServiceListener;
    @Setter private MAPServiceLsmListener lcsServiceListener;
    @Setter private MAPServiceCallHandlingListener callHandlingServiceListener;

    static {
        cache = new HashMap<>();
    }


    public MapAdapter(NodeConfig nodeConfig, int supportedSsn,  MAPDialogListener dialogEventListener,
                      MAPServiceMobilityListener mobilityServiceListener, MAPServiceSmsListener smsServiceListener,
                      MAPServiceOamListener oamServiceListener, MAPServicePdpContextActivationListener pdpServiceListener,
                      MAPServiceLsmListener lcsServiceListener, MAPServiceCallHandlingListener callHandlingServiceListener) {
        this.nodeConfig = nodeConfig;
        this.supportedSsn = supportedSsn;
        this.started = false;
        this.dialogEventListener = dialogEventListener;
        this.mobilityServiceListener = mobilityServiceListener;
        this.smsServiceListener = smsServiceListener;
        this.oamServiceListener = oamServiceListener;
        this.pdpServiceListener = pdpServiceListener;
        this.lcsServiceListener = lcsServiceListener;
        this.callHandlingServiceListener = callHandlingServiceListener;
    }

    public static MapAdapter getInstance(NodeConfig nodeConfig, SccpAdapter sccpAdapter, int supportedSsn,
                                         MAPDialogListener dialogEventListener,
                                         MAPServiceMobilityListener mobilityServiceListener, MAPServiceSmsListener smsServiceListener,
                                         MAPServiceOamListener oamServiceListener, MAPServicePdpContextActivationListener pdpServiceListener,
                                         MAPServiceLsmListener lcsServiceListener, MAPServiceCallHandlingListener callHandlingServiceListener) throws SystemException {
        MapAdapter mapAdapter;
        if (cache.containsKey(supportedSsn)) {
            logger.debug("Using cached MAP layer");
            mapAdapter = cache.get(supportedSsn);

        } else {
            logger.debug("Creating a new MAP layer...");
            mapAdapter = new MapAdapter(nodeConfig, supportedSsn, dialogEventListener, mobilityServiceListener,
                    smsServiceListener, oamServiceListener, pdpServiceListener, lcsServiceListener, callHandlingServiceListener);
            mapAdapter.initialize(sccpAdapter);
            cache.put(supportedSsn, mapAdapter);
        }

        return mapAdapter;
    }

    private void initialize(SccpAdapter sccpAdapter) throws SystemException {
        try {
            logger.debug("Initializing TCAP & MAP layer ....");
            var nodeName = nodeConfig.getSs7Association().getLocalNode().getNodeName();
            var configDir = Util.getConfigDir(nodeName);
            var tcapMaxDialogs = 65535;
            stack = new MAPStackImpl(nodeName, sccpAdapter.getStack().getSccpProvider(), supportedSsn);
            tcapStack = this.stack.getTCAPStack();
            if (Constants.SCCP_INTEGRATED_VLR && supportedSsn == Constants.SCCP_MSC_SSN) {
                var extraSsnList = new ArrayList<Integer>();
                extraSsnList.add(Constants.SCCP_VLR_SSN);
                tcapStack.setExtraSsns(extraSsnList);
            }
            ((TCAPStackImpl) tcapStack).setPersistDir(configDir);
            tcapStack.start();
            tcapStack.setDialogIdleTimeout(Constants.MAP_DIALOG_IDLE_TIMEOUT);
            tcapStack.setInvokeTimeout(Constants.MAP_INVOKE_TIMEOUT);
            tcapStack.setMaxDialogs(tcapMaxDialogs);

            if (nodeConfig instanceof LabNodeConfig) {
                ((MAPProviderImpl) stack.getMAPProvider()).setIsLabMode(true);
            }

            callHandlingService = MapCallHandlingService.builder().stack(stack).build();
            smsService = MapSmsService.builder().stack(stack).build();
            pdpService = MapPdpService.builder().stack(stack).build();
            lcsService = MapLcsService.builder().stack(stack).build();
            mobilityService = MapMobilityService.builder().stack(stack).build();
            oamService = MapOamService.builder().stack(stack).build();

            logger.debug("TCAP & MAP layers initialized successfully!");

        } catch (Exception e) {
            var msg = "Failed to initialize TCAP & MAP layers.";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
        }
    }

    public void start() throws SystemException {
        try {
            logger.debug("Starting MAP for SSN: " + stack.getTCAPStack().getSubSystemNumber());
            if (!started) {
                stack.start();
                addServiceListeners();
                started = true;

            } else {
                var msg = "MAP layer already started for SSN: " + supportedSsn;
                logger.error(msg);
                throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
            }

        } catch (Exception e) {
            logger.error("Failed to start MAP layer.", e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
        }
    }

    public void stop() throws SystemException {
        try {
            if (started) {
                stack.stop();
                started = false;
            } else {
                logger.error("MAP layer is not started.");
                throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
            }

        } catch (Exception e) {
            logger.error("Failed to stop MAP layer.", e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
        }
    }

    public void activateServices() throws SystemException {
        if (started) {
            String msg = "Cannot activate MAP services after starting MAP layer.";
            logger.error(msg);
            throw SystemException.builder().code(ErrorCode.SS7_CONNECTION_INITIALIZATION).message(msg).build();
        }

        callHandlingService.activate();
        smsService.activate();
        pdpService.activate();
        lcsService.activate();
        mobilityService.activate();
        oamService.activate();
    }

    public MAPParameterFactory getParamFactory() {
        return stack.getMAPProvider().getMAPParameterFactory();
    }

    public MAPSmsTpduParameterFactory getSmsParamFactory() {
        return stack.getMAPProvider().getMAPSmsTpduParameterFactory();
    }

    private void addServiceListeners() {
        var provider = stack.getMAPProvider();
        provider.addMAPDialogListener(dialogEventListener);
        provider.getMAPServiceMobility().addMAPServiceListener(mobilityServiceListener);
        provider.getMAPServiceSms().addMAPServiceListener(smsServiceListener);
        provider.getMAPServiceOam().addMAPServiceListener(oamServiceListener);
        provider.getMAPServicePdpContextActivation().addMAPServiceListener(pdpServiceListener);
        provider.getMAPServiceLsm().addMAPServiceListener(lcsServiceListener);
        provider.getMAPServiceCallHandling().addMAPServiceListener(callHandlingServiceListener);
    }
}
