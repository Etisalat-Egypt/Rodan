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

import com.rodan.connectivity.ss7.handler.AssociationEventHandler;
import com.rodan.connectivity.ss7.handler.SctpEventHandler;
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
import org.mobicents.protocols.api.Association;
import org.mobicents.protocols.api.Management;
import org.mobicents.protocols.sctp.ManagementImpl;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class SctpAdapter {
    /*
        A stream is a unidirectional channel within an association
        SCTP provides the ability to have multiple streams within a single association
        L2 in ISO

        PS: Each SCTP endpoint can have multiple IPs, an association is is the combination of the
        2 endpoint (with all IPs) "each transport address is equivalent to a different path to send and receive
        data within an association. One path is selected as the primary path, other paths are alternatives.
    * */
    private static final int MAX_CONNECTION_LOSS = 5;

    private static final Logger logger = LogManager.getLogger(SctpAdapter.class);

    private static SctpAdapter instance;

    @Getter(AccessLevel.PACKAGE) private Management management;
    private NodeConfig nodeConfig;
    private boolean started;
    private boolean up;
    private int associationFailureCount;
    private SctpEventHandlerImpl2 eventHandler;

    private SctpAdapter(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
        this.started = false;
        this.up = false;
        this.associationFailureCount = 0;
        this.eventHandler = new SctpEventHandlerImpl2();
    }

    public static SctpAdapter getInstance(NodeConfig nodeConfig) throws SystemException {
        logger.debug("Getting SCTP layer...");
        if (instance == null) {
            logger.debug("Creating a new SCTP layer...");
            instance = new SctpAdapter(nodeConfig);
            instance.initialize();
        }

        return instance;
    }

    private void initialize() throws SystemException {
        try {
            logger.info("Initializing SCTP layer ....");
            management = new ManagementImpl(nodeConfig.getSs7Association().getLocalNode().getNodeName());
            management.setSingleThread(false);
            var configDir = Util.getConfigDir(nodeConfig.getSs7Association().getLocalNode().getNodeName());
            management.setPersistDir(configDir);
            management.start();
            management.setConnectDelay(Constants.SCTP_CONNECT_DELAY);
            ((ManagementImpl) management).setMaxIOErrors(Constants.SCTP_MAX_IO_ERRORS);
            management.removeAllResourses();
            management.addManagementEventListener(eventHandler);
//            association.setAssociationListener(eventHandler); // TODO IMP: uncomment when fixing listener

            Association association;
            var localIp = Util.resolveIp(nodeConfig.getSs7Association().getLocalNode().getAddress());
            var localPort = Integer.valueOf(nodeConfig.getSs7Association().getLocalNode().getPort());

            var peerNodeList = new ArrayList<Ss7NodeInfo>();
            if (nodeConfig instanceof SepNodeConfig cfg) {
                peerNodeList.add(cfg.getSs7Association().getPeerNode());

            } else {
                ((StpNodeConfig) nodeConfig).getSs7Association().getPeerNodes().forEach(node -> peerNodeList.add(node));
            }


            var serverName = nodeConfig.getSs7Association().getLocalNode().getNodeName();
            if (nodeConfig.getSctpMode() == SctpMode.SERVER) {
                // Use extraHostAddresses for multi-homing
                management.addServer(serverName, localIp, localPort);
                for (var peerNode : peerNodeList) {
                    var associationName = peerNode.getNodeName() + "_ASCN";
                    var peerIp = Util.resolveIp(peerNode.getAddress());
                    var peerPort = Integer.valueOf(peerNode.getPort());
                    association = management.addServerAssociation(peerIp, peerPort, serverName, associationName);
                }
                management.startServer(serverName);

            } else {
                var associationName = nodeConfig.getSs7Association().getLocalNode().getNodeName() + "_ASCN";
                for (var node : peerNodeList) {
                    var peerIp =  Util.resolveIp(node.getAddress());
                    var peerPort = Integer.valueOf(node.getPort());
                    // Use extraHostAddresses for multi-homing
                    association = management.addAssociation(localIp, localPort, peerIp, peerPort, associationName);
                }
            }

            logger.debug("SCTP layer initialized successfully!");
        } catch (Exception e) {
            logger.error("Failed to initialize SCTP layer.", e);
            throw SystemException.builder().code(ErrorCode.SCTP_INITIALIZATION).build();
        }
    }

    public boolean isAssociationUp() {
        // TODO IMP TRX: STP: check if SctpEventHandlerImpl2 is required
        var notConnectedAscnList = management.getAssociations().entrySet().stream()
                .filter( ascn -> !ascn.getValue().isConnected()).collect(Collectors.toList());
        return notConnectedAscnList.isEmpty();

//        return started && up;
    }

    public void stop() throws SystemException {
        try {
            if (!isAssociationUp()) {
                management.stop();
            } else {
                logger.error("SCTP layer is not started.");
                throw SystemException.builder().code(ErrorCode.SCTP_INITIALIZATION).build();
            }


        } catch (Exception e) {
            logger.error("Failed to stop SCTP layer.", e);
            throw SystemException.builder().code(ErrorCode.SCTP_INITIALIZATION).build();
        }
    }

    private class SctpEventHandlerImpl2 implements SctpEventHandler, AssociationEventHandler {
        @Override
        public void onAssociationStarted(Association asctn) {
            started = true;
            SctpEventHandler.logger.debug("[[[[[[[[[[    onAssociationStarted      ]]]]]]]]]]");
            if (asctn != null) {
                SctpEventHandler.logger.debug(String.format("SCTP AssociationStarted name=%s peer=%s", asctn.getName(), asctn.getPeerAddress()));
            }
        }

        @Override
        public void onAssociationStopped(Association association) {
            started = false;
            SctpEventHandler.logger.debug("[[[[[[[[[[    onAssociationStopped      ]]]]]]]]]]");
            if (association != null) {
                SctpEventHandler.logger.debug(String.format("SCTP AssociationUp name=%s peer=%s", association.getName(), association.getPeerAddress()));
            }
        }

        public void onAssociationUp(Association association) {
            up = true;
            SctpEventHandler.logger.debug("[[[[[[[[[[    onAssociationUp      ]]]]]]]]]]");
            if (association != null) {
                SctpEventHandler.logger.debug(String.format("SCTP AssociationUp name=%s peer=%s", association.getName(), association.getPeerAddress()));
            }
        }

        @Override
        public void onAssociationDown(Association association) {
            up = false;
            SctpEventHandler.logger.debug("[[[[[[[[[[    onAssociationDown      ]]]]]]]]]]");
            if (association != null) {
                SctpEventHandler.logger.debug(String.format("SCTP AssociationDown name=%s peer=%s", association.getName(), association.getPeerAddress()));
            }
        }

        @Override
        public void onCommunicationLost(Association association) {
            SctpEventHandler.logger.debug("[[[[[[[[[[    onCommunicationLost      ]]]]]]]]]]");
            SctpEventHandler.logger.debug(String.format("SCTP Association name=%s peer=%s", association.getName(), association.getPeerAddress()));
            associationFailureCount++;
            // TODO shutdown should be based on time as well (# of losses in certain time)
            // TODO should update sctpCache to avoid using this instance again.
            if (associationFailureCount > MAX_CONNECTION_LOSS /* && !nodeType.equals(StackConfig.NodeType.CLIENT)*/) {
                try {
                    SctpEventHandler.logger.error("Max association failure count reached. Shutting down association...");
                    management.stopAssociation(association.getName());

                } catch (Exception e) {
                    SctpEventHandler.logger.error("Failed to shutdown association after connection loss", e);
                }
            }
        }
    }
}
