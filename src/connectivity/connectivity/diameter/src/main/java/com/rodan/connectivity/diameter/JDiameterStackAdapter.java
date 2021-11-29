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

package com.rodan.connectivity.diameter;

import com.rodan.library.model.config.node.config.IntruderNodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.util.Util;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdiameter.api.*;
import org.jdiameter.api.s6a.ClientS6aSession;
import org.jdiameter.api.s6a.ClientS6aSessionListener;
import org.jdiameter.api.s6a.ServerS6aSession;
import org.jdiameter.api.s6a.ServerS6aSessionListener;
import org.jdiameter.client.api.ISessionFactory;
import org.jdiameter.client.impl.helpers.AppConfiguration;
import org.jdiameter.common.impl.app.s6a.S6aSessionFactoryImpl;
import org.jdiameter.server.impl.helpers.EmptyConfiguration;
import org.jdiameter.server.impl.helpers.ExtensionPoint;
import org.jdiameter.server.impl.helpers.Parameters;
import org.mobicents.diameter.dictionary.AvpDictionary;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@ToString
public class JDiameterStackAdapter {
    private static final long APP_ID = 16777251; // TODO Diameter: check hardcoded value
    private static final String dictionaryFile = "com/rodan/connectivity/diameter/dictionary.xml";
    final static Logger logger = LogManager.getLogger(JDiameterStackAdapter.class);

    private IntruderNodeConfig nodeConfig;
    private AvpDictionary dictionary = AvpDictionary.INSTANCE;
    @Getter private ApplicationId authAppId;
    private ISessionFactory sessionFactory;
    @Getter private S6aSessionFactoryImpl s6aSessionFactory;
    private org.jdiameter.api.Stack stack;
    private boolean stackInitialized;
    private boolean stackStarted;

    private ClientS6aSessionListener s6aClientEventListener;
    private ServerS6aSessionListener s6aServerEventListener;

    public static final long VENDOR_ID_3GPP = 10415L;
    public static final int AVP_PROTECTED_FLAG = 0;
    public static final int AVP_NOT_PROTECTED_FLAG = 1;
    public static final int AVP_MANDATORY_FLAG = 0;
    public static final int AVP_NOT_MANDATORY_FLAG = 2;


    @Builder
    public JDiameterStackAdapter(IntruderNodeConfig nodeConfig, ClientS6aSessionListener s6aClientEventListener,
                                 ServerS6aSessionListener s6aServerEventListener) {
        this.nodeConfig = nodeConfig;
        this.s6aClientEventListener = s6aClientEventListener;
        this.s6aServerEventListener = s6aServerEventListener;
        this.authAppId = ApplicationId.createByAuthAppId(VENDOR_ID_3GPP, APP_ID);
        this.stackInitialized = false;
        this.stackStarted = false;
    }

    public void initStack() throws SystemException {
        try {
            if (stackInitialized) {
                var msg = "Diameter stack already initialized";
                logger.error(msg);
                throw SystemException.builder().code(ErrorCode.DIAMETER_INITIALIZATION).message(msg).build();
            }

            var stackConfig = JDiameterStackAdapter.generateStackConfig(nodeConfig);
            logger.debug("Parsing dictionary...");
            var is = this.getClass().getClassLoader().getResourceAsStream(dictionaryFile);
            if (is == null) {
                var msg = "Failed to load AVP Dictionary. File not found at: " + dictionaryFile;
                logger.error(msg);
                throw SystemException.builder().code(ErrorCode.DIAMETER_INITIALIZATION).message(msg).build();
            }

            dictionary.parseDictionary(is);
            logger.debug("AVP Dictionary successfully parsed.");
            this.stack = new org.jdiameter.server.impl.StackImpl();
            logger.debug("Initializing stack with loaded configurations...");
            sessionFactory = (ISessionFactory) stack.init(stackConfig);
            s6aSessionFactory = new S6aSessionFactoryImpl(sessionFactory);
            s6aSessionFactory.setClientSessionListener(s6aClientEventListener);
            s6aSessionFactory.setServerSessionListener(s6aServerEventListener);

            Set<ApplicationId> appIds = stack.getMetaData().getLocalPeer().getCommonApplications();
            logger.debug("Diameter Stack  :: Supporting " + appIds.size() + " applications.");
            for (org.jdiameter.api.ApplicationId x : appIds) {
                logger.debug("Diameter Stack  :: Common :: " + x);
            }

            //Register network req listener, even though we wont receive requests
            //this has to be done to inform stack that we support application
            // TODO IMP TRX Diameter: General event handling should be done in service classes
            Network network = stack.unwrap(Network.class);
            network.addNetworkReqListener(new NetworkReqListener() {
                @Override
                public Answer processRequest(Request request) {
                    //this wont be called.
                    return null;
                }
            }, this.authAppId); // passing example app id.

            // TODO IMP TRX Diameter: Check if required
            MetaData metaData = stack.getMetaData();
            if (metaData.getStackType() != StackType.TYPE_SERVER || metaData.getMinorVersion() <= 0) {
                stack.destroy();
                logger.error("Incorrect driver");
                return;
            }

            // Initialize all services
            stackInitialized = true;

        } catch (Exception e) {
            var msg = "Error while initializing Diameter stack";
            logger.error(msg, e);
            if (this.stack != null) {
                this.stack.destroy();
            }
            throw SystemException.builder().code(ErrorCode.DIAMETER_INITIALIZATION).message(msg).parent(e).build();
        }
    }

    public void startStack() throws SystemException {
        try {
            if (!stackInitialized) {
                var msg = "Diameter stack is not initialized";
                logger.error(msg);
                throw SystemException.builder().code(ErrorCode.DIAMETER_INITIALIZATION).message(msg).build();
            }

            if (stackStarted) {
                var msg = "Diameter stack already started";
                logger.error(msg);
                throw SystemException.builder().code(ErrorCode.DIAMETER_INITIALIZATION).message(msg).build();
            }

            logger.debug("Starting stack...");
//            stack.start(); // Async
            stack.start(Mode.ALL_PEERS, 10000, TimeUnit.MILLISECONDS); // Sync
            logger.debug("Stack successfully started");
            stackStarted = true;

            sessionFactory.registerAppFacory(ClientS6aSession.class, s6aSessionFactory);
            sessionFactory.registerAppFacory(ServerS6aSession.class, s6aSessionFactory);

        } catch (IllegalDiameterStateException | InternalException e) {
            var msg = "Error while starting Diameter stack";
            logger.error(msg, e);
            if (this.stack != null) {
                this.stack.destroy();
            }
            throw SystemException.builder().code(ErrorCode.DIAMETER_INITIALIZATION).message(msg).parent(e).build();
        }
    }

    public void stopStack() throws SystemException {
        try {
            if (!stackStarted) {
                var msg = "Diameter stack not started";
                logger.error(msg);
                throw SystemException.builder().code(ErrorCode.DIAMETER_INITIALIZATION).message(msg).build();
            }
            stack.stop(10l, TimeUnit.SECONDS,0);
            stackStarted = false;

        } catch (IllegalDiameterStateException | InternalException e) {
            var msg = "Error while stopping Diameter stack";
            logger.error(msg, e);
            if (this.stack != null) {
                this.stack.destroy();
            }
            throw SystemException.builder().code(ErrorCode.DIAMETER_INITIALIZATION).message(msg).parent(e).build();
        }

    }

    public Session generateSession() throws SystemException {
        try {
            return sessionFactory.getNewSession();

        } catch (InternalException e) {
            var msg = "Failed to generate Diameter Session";
            logger.error(msg);
            throw SystemException.builder().code(ErrorCode.DIAMETER_INITIALIZATION).message(msg).build();
        }
    }

    public ClientS6aSession generateS6aClientSession() throws SystemException {
        try {
            var session = sessionFactory.getNewSession();
            return sessionFactory.<ClientS6aSession>getNewAppSession(null, authAppId, ClientS6aSession.class, null);

        } catch (InternalException e) {
            var msg = "Failed to generate S6aClientSession";
            logger.error(msg);
            throw SystemException.builder().code(ErrorCode.DIAMETER_INITIALIZATION).message(msg).build();
        }
    }

    public ServerS6aSession generateS6aServerSession() throws SystemException {
        try {
            var session = sessionFactory.getNewSession();
            return sessionFactory.<ServerS6aSession>getNewAppSession(null, authAppId, ServerS6aSession.class, null);

        } catch (InternalException e) {
            var msg = "Failed to generate S6aServerSession";
            logger.error(msg);
            throw SystemException.builder().code(ErrorCode.DIAMETER_INITIALIZATION).message(msg).build();
        }
    }

    public String getPeerAddress() {
        return nodeConfig.getDiameterAssociationInfo().getPeerNode().getAddress();
    }

    private static EmptyConfiguration generateStackConfig(IntruderNodeConfig nodeConfig) throws SystemException {
        var localNode = nodeConfig.getDiameterAssociationInfo().getLocalNode();
        var ownUri = String.format("aaa://%s.%s:%s", localNode.getHostname(), localNode.getRealm(), localNode.getPort());
        var ownIp = Util.resolveIp(localNode.getAddress());
        var ownRealm = localNode.getRealm();
        var ownVendorId = Long.valueOf(localNode.getAppInfo().getVendorId());
        var ownAuthAppId = Long.valueOf(localNode.getAppInfo().getAuthAppId());
        var ownAccAppId = Long.valueOf(localNode.getAppInfo().getAccAppId());

        var stackConfig = new EmptyConfiguration(true);
        stackConfig.add(Parameters.Assembler, Parameters.Assembler.defValue());
        stackConfig.add(Parameters.OwnDiameterURI, ownUri);
        stackConfig.add(Parameters.OwnIPAddresses,
                EmptyConfiguration.getInstance()
                        .add(Parameters.OwnIPAddress,   ownIp)
        );
        stackConfig.add(Parameters.OwnRealm, ownRealm);
        stackConfig.add(Parameters.OwnVendorID, 0L); // TODO Diameter IMP: should use 3GPP vendor ID?
        stackConfig.add(Parameters.OwnProductName, "Rodan");
        stackConfig.add(Parameters.OwnFirmwareRevision, 1L);
        stackConfig.add(Parameters.OverloadMonitor,
                EmptyConfiguration.getInstance()
                        .add(Parameters.ApplicationId,
                                EmptyConfiguration.getInstance()
                                        .add(Parameters.VendorId,   ownVendorId)
                                        .add(Parameters.AuthApplId, ownAuthAppId)
                                        .add(Parameters.AcctApplId, ownAccAppId)
                        )
                        .add(Parameters.OverloadEntryIndex, 1)
                        .add(Parameters.OverloadEntrylowThreshold, 0.5)
                        .add(Parameters.OverloadEntryhighThreshold, 0.6)
        );
        stackConfig.add(Parameters.AcceptUndefinedPeer, false);
        stackConfig.add(Parameters.DuplicateProtection, true);
        stackConfig.add(Parameters.DuplicateTimer, 240000L);
        stackConfig.add(Parameters.UseUriAsFqdn, false);
        stackConfig.add(Parameters.QueueSize, 10000);
        stackConfig.add(Parameters.MessageTimeOut, 60000L);
        stackConfig.add(Parameters.StopTimeOut, 10000L);
        stackConfig.add(Parameters.CeaTimeOut, 10000L);
        stackConfig.add(Parameters.IacTimeOut, 30000L);
        stackConfig.add(Parameters.DwaTimeOut, 10000L);
        stackConfig.add(Parameters.DpaTimeOut, 5000L);
        stackConfig.add(Parameters.RecTimeOut, 10000L);

        var concurrentConfig = new AppConfiguration[] {
                EmptyConfiguration.getInstance()
                        .add(Parameters.ConcurrentEntityName, "ThreadGroup")
                        .add(Parameters.ConcurrentEntityPoolSize, 64),
                EmptyConfiguration.getInstance()
                        .add(Parameters.ConcurrentEntityName, "ProcessingMessageTimer")
                        .add(Parameters.ConcurrentEntityPoolSize, 1),
                EmptyConfiguration.getInstance()
                        .add(Parameters.ConcurrentEntityName, "DuplicationMessageTimer")
                        .add(Parameters.ConcurrentEntityPoolSize, 1),
                EmptyConfiguration.getInstance()
                        .add(Parameters.ConcurrentEntityName, "RedirectMessageTimer")
                        .add(Parameters.ConcurrentEntityPoolSize, 1),
                EmptyConfiguration.getInstance()
                        .add(Parameters.ConcurrentEntityName, "PeerOverloadTimer")
                        .add(Parameters.ConcurrentEntityPoolSize, 1),
                EmptyConfiguration.getInstance()
                        .add(Parameters.ConcurrentEntityName, "ConnectionTimer")
                        .add(Parameters.ConcurrentEntityPoolSize, 1),
                EmptyConfiguration.getInstance()
                        .add(Parameters.ConcurrentEntityName, "StatisticTimer")
                        .add(Parameters.ConcurrentEntityPoolSize, 1)
        };

        stackConfig.add(Parameters.Concurrent, concurrentConfig);

        var peerNode = nodeConfig.getDiameterAssociationInfo().getPeerNode();
        var peerRealms = peerNode.getRealms();
        var peerName = String.format("aaa://%s:%s",  Util.resolveIp(peerNode.getAddress()), peerNode.getPort());
        var localPortRange = peerNode.getLocalPortRange();
        var attemptConnection = Boolean.valueOf(peerNode.getAttemptConnect());
        var realmHosts = Util.resolveIp(peerNode.getAddress());
        var peerVendorId = Long.valueOf(peerNode.getAppInfo().getVendorId());
        var peerAuthAppId = Long.valueOf(peerNode.getAppInfo().getAuthAppId());
        var peerAccAppId = Long.valueOf(peerNode.getAppInfo().getAccAppId());

        stackConfig.add(Parameters.PeerTable,
                EmptyConfiguration.getInstance()
                        .add(Parameters.PeerRating, 1)
                        .add(Parameters.PeerName, peerName)
                        .add(Parameters.PeerLocalPortRange, localPortRange)
                        .add(Parameters.PeerAttemptConnection, attemptConnection)
        );


        var realmCount = peerRealms.size();
        var realmEntries = new AppConfiguration[realmCount];
        for (var i = 0; i < realmCount; i++) {
            var realm = peerRealms.get(i);
            realmEntries[i] = EmptyConfiguration.getInstance()
                    .add(Parameters.RealmName, realm)
                    .add(Parameters.RealmLocalAction, "LOCAL")
                    .add(Parameters.RealmEntryIsDynamic, false)
                    .add(Parameters.RealmEntryExpTime, 1L)
                    .add(Parameters.RealmHosts, realmHosts)
                    .add(Parameters.ApplicationId,
                            EmptyConfiguration.getInstance()
                                    .add(Parameters.VendorId,   peerVendorId)
                                    .add(Parameters.AuthApplId, peerAuthAppId)
                                    .add(Parameters.AcctApplId, peerAccAppId)
                    );
        }

        var realmTable = EmptyConfiguration.getInstance();
        realmTable.add(Parameters.RealmEntry, realmEntries);
        stackConfig.add(Parameters.RealmTable, realmTable);

        // Set SCTP Connection and Network Guard classes
        AppConfiguration internalExtensions =
                (AppConfiguration) stackConfig.getChildren(Parameters.Extensions.ordinal())[ExtensionPoint.Internal.id()];
        // TODO Diameter IMP: Should add org.jdiameter.server.impl
        internalExtensions.
                add(ExtensionPoint.InternalConnectionClass, "org.jdiameter.client.impl.transport.sctp.SCTPClientConnection").
                add(ExtensionPoint.InternalNetworkGuard, "org.jdiameter.server.impl.io.sctp.NetworkGuard");

        logger.debug("############ EmptyConfig: " + stackConfig);
        return stackConfig;
    }

}
