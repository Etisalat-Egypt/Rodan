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

import com.rodan.library.model.Constants;
import com.rodan.library.model.config.node.SctpMode;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.config.node.config.SepNodeConfig;
import com.rodan.library.model.config.node.config.StpNodeConfig;
import com.rodan.library.model.config.node.info.Ss7NodeInfo;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.util.Util;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.m3ua.ExchangeType;
import org.mobicents.protocols.ss7.m3ua.Functionality;
import org.mobicents.protocols.ss7.m3ua.IPSPType;
import org.mobicents.protocols.ss7.m3ua.M3UAManagement;
import org.mobicents.protocols.ss7.m3ua.impl.M3UAManagementImpl;
import org.mobicents.protocols.ss7.m3ua.impl.parameter.ParameterFactoryImpl;
import org.mobicents.protocols.ss7.m3ua.parameter.TrafficModeType;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class M3uaAdapter {
    /*
        MTP3 carried circuit signalling information (serves ISUP, TUP)
        SCCP along with MPT3(M3UA) corresponds to OSI layer 3

        Application server (AS) is a logical entity that handldes a specific routing key
        ex: virtual switch, MGC, HLR node, ...

        Application server process (ASP) is a process instance of the AS. AS can have multiple ASPs (active, backups)
        ASP contains SCTP endpoint

        In order to distribute SS7 messages among ASs, Routing Keys are defined (static or dynamic routing)
        A Routing Key is a set of SS7 parameters that define the range of signalling traffic to
        be handled by AS. Routing key has a 1-to-1 relation with AS and is identified by a routing context
        Routing Key can be any combination of the following: NI, SI, DPC, OPC, SSN
        I think this routing is mainly required for SGs only

        ServiceInformationOctet
        =======================
        SI included in Service Information Octet (SIO) in MTP3 messages
        Helps MTP3 distribute messages among different MPT3 users (usually upper layers)
        it acts like an identifier for upper layer protocols (sccp, ISUP, ...)
        0011: SCCP, 0101: ISUP, ...

        Network Indicator (NI) field in Subservice Field (SSF) in Service Information Octet (SIO)
        Is used for load sharing across links and linksets
        it helps in routing MTP3 messages (national vs international)
        0000: international, 0001: international spare, 0010: national, 0011: national spare
        for national PCs, the whole bits are interpreted as a single identifier, for international PCs
        PC is considered ISPC and subdivided into hierarchical fields
        ITU-T: (zone, area/network, signalling point)
        ANSI: (network, cluster, member)
        networkIndicator: 0 for international, 10 for national

        SignalingInformationField
        =========================
        Destination Point Code (DPC), like destination IP in IP protocol
        routing is based on PC and SSN. SSN and PC are L3 fields
        This should be the PC of the node performing GT translation (GTT) usually STP
        (STP is like router in IP networks, but only uses static routes)

        Signalling Link Selector (SLS): routing information included in the ITU-T Routing Label which if the
        beginning portion of Signalling Information Field (SIF) in MTP3 messages.
        SIF has Originating/Destination Point Code (OPC/DPC) and SLS

        SignalingLinkSelection
        ======================

    */

    final static Logger logger = LogManager.getLogger(M3uaAdapter.class);

    private static M3uaAdapter instance;

    @Getter(AccessLevel.PACKAGE) private M3UAManagement management;
    private NodeConfig nodeConfig;

    private M3uaAdapter(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    public static M3uaAdapter getInstance(NodeConfig nodeConfig, SctpAdapter sctpAdapter) throws SystemException {
        logger.debug("Getting M3UA layer...");
        if (instance == null) {
            logger.debug("Creating a new M3UA layer...");
            instance = new M3uaAdapter(nodeConfig);
            instance.initialize(sctpAdapter);
        }

        return instance;
    }

    private void initialize(SctpAdapter sctpAdapter) throws SystemException {
        try {
            logger.info("Initializing M3UA layer ....");
            management = new M3UAManagementImpl(nodeConfig.getSs7Association().getLocalNode().getNodeName(), Constants.APP_NAME);
            var configDir = Util.getConfigDir(nodeConfig.getSs7Association().getLocalNode().getNodeName());
            management.setPersistDir(configDir);
            ((M3UAManagementImpl) management).setTransportManagement(sctpAdapter.getManagement());
            ((M3UAManagementImpl) management).setDeliveryMessageThreadCount(Constants.M3UA_DELIVERY_MESSAGE_THREAD_COUNT);
            management.start();
            management.removeAllResourses();

            var factory = new ParameterFactoryImpl();
            long defaultRoutingContext = Long.valueOf(nodeConfig.getSs7Association().getRoutingContext());
            var peerNodeList = new ArrayList<Ss7NodeInfo>();
            if (nodeConfig instanceof SepNodeConfig cfg) {
                peerNodeList.add(((SepNodeConfig) nodeConfig).getSs7Association().getPeerNode());
            } else {
                ((StpNodeConfig) nodeConfig).getSs7Association().getPeerNodes().forEach(node -> peerNodeList.add(node));
            }

            var routingContext = factory.createRoutingContext(new long[] {defaultRoutingContext});
            var trafficModeType = factory.createTrafficModeType(TrafficModeType.Loadshare);

            // IP Server Process (IPSP) is same as ASP but uses M3UA in a point-to-point
            // fashion (does not use signalling gateway SG)
            var functionality = Functionality.IPSP;
            var ipspType = (nodeConfig.getSctpMode() == SctpMode.SERVER) ? IPSPType.SERVER : IPSPType.CLIENT;

            for (var peerNode : peerNodeList) {
                var asName = peerNode.getNodeName() + "_AS";
                var aspName = peerNode.getNodeName() + "_ASP";
                var ascnName = (nodeConfig.getSctpMode() == SctpMode.SERVER ?
                        peerNode.getNodeName() : nodeConfig.getSs7Association().getLocalNode().getNodeName()) + "_ASCN";
                var peerPointCode = Integer.valueOf(peerNode.getPointCode());
                management.createAs(asName, functionality, ExchangeType.SE, ipspType,
                        routingContext, trafficModeType, Constants.MIN_ASP_ACTIVE_FOR_LOAD_BALANACE, null);
                management.createAspFactory(aspName, ascnName);
                management.assignAspToAs(asName, aspName);

                // Add route: AS name <-> Point Code
                management.addRoute(peerPointCode, Constants.M3UA_WILD_CARD_PC, Constants.M3UA_WILD_CARD_SI,
                        asName, TrafficModeType.Loadshare);
            }

            // TODO IMP: If server is using SGP mode, add another route based on Opc in server mode
            logger.debug("M3UA layer initialized successfully!");

        } catch (Exception e) {
            logger.error("Failed to initialize M3UA layer.", e);
            throw SystemException.builder().code(ErrorCode.M3UA_INITIALIZATION).parent(e).build();
        }
    }

    public void startAsp() throws SystemException {
        if (isAspUp()) { {
            logger.error("All ASPs already started.");
            throw SystemException.builder().code(ErrorCode.M3UA_INITIALIZATION).build();
        }}

        try {
            var notStartedAspList = management.getAspfactories()
                    .stream().filter(asp -> !asp.getStatus())
                    .collect(Collectors.toList());
            for (var asp : notStartedAspList) {
                logger.debug("######## Starting ASP: " + asp.getName());
                management.startAsp(asp.getName());
            }

        } catch (Exception e) {
            logger.error("Failed to start ASP", e);
            throw SystemException.builder().code(ErrorCode.M3UA_INITIALIZATION).build();
        }
    }

    public void start() throws SystemException {
        try {
            if (!isStarted()) {
                management.start();
            } else {
                logger.debug("Using cached M3UA layer");
                throw SystemException.builder().code(ErrorCode.M3UA_INITIALIZATION).build();
            }
        } catch (Exception e) {
            logger.error("Failed to start M3UA layer.", e);
            throw SystemException.builder().code(ErrorCode.M3UA_INITIALIZATION).build();
        }
    }

    public void stop() throws SystemException {
        try {
            if (isStarted()) {
                management.stop();
            } else {
                logger.error("M3UA layer is not started.");
                throw SystemException.builder().code(ErrorCode.M3UA_INITIALIZATION).build();
            }
        } catch (Exception e) {
            logger.error("Failed to stop M3UA layer.", e);
            throw SystemException.builder().code(ErrorCode.M3UA_INITIALIZATION).build();
        }
    }

    public boolean isStarted() {
        return management.isStarted();
    }

    public boolean isAspUp() {
        var notStartedAspList = management.getAspfactories().stream().filter(asp -> !asp.getStatus()).collect(Collectors.toList());

        return notStartedAspList.isEmpty();

//        var asp = management.getAspfactories().stream().filter(p ->
//                p.getName().equals(stackConfig.getM3uaConfig().getAspName())
//        ).findFirst();
//        return asp.isPresent() && asp.get().getStatus();
    }

    public boolean isConnected() {
        var notConnectedAsList = management.getAppServers().stream().filter(as -> !as.isConnected()).collect(Collectors.toList());

        return notConnectedAsList.isEmpty();

//        var as = management.getAppServers().stream().filter(s ->
//                s.getName().equals(stackConfig.getM3uaConfig().getAsName())
//        ).findFirst();
//        return as.isPresent() && as.get().isConnected() && as.get().isUp();
    }
}
