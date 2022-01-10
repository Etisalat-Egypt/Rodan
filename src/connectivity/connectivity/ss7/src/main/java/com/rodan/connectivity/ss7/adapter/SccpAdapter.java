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
import com.rodan.library.model.config.node.config.LabNodeConfig;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.config.node.config.SepNodeConfig;
import com.rodan.library.model.config.node.config.StpNodeConfig;
import com.rodan.library.model.config.node.info.SepNodeInfo;
import com.rodan.library.model.config.node.info.Ss7NodeInfo;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.util.Util;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.indicator.NatureOfAddress;
import org.mobicents.protocols.ss7.indicator.NumberingPlan;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.m3ua.impl.M3UAManagementImpl;
import org.mobicents.protocols.ss7.mtp.FastHDLC;
import org.mobicents.protocols.ss7.sccp.OriginationType;
import org.mobicents.protocols.ss7.sccp.Router;
import org.mobicents.protocols.ss7.sccp.RuleType;
import org.mobicents.protocols.ss7.sccp.SccpStack;
import org.mobicents.protocols.ss7.sccp.impl.SccpProviderImpl;
import org.mobicents.protocols.ss7.sccp.impl.SccpSstListener;
import org.mobicents.protocols.ss7.sccp.impl.SccpStackImpl;
import org.mobicents.protocols.ss7.sccp.parameter.EncodingScheme;
import org.mobicents.protocols.ss7.sccp.parameter.ParameterFactory;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SccpAdapter implements SccpSstListener {
    /*
        SCCP carries non-circuit signalling information (serves TCAP like: toll-free calling,
        local number portability, CCBS in IN, SMS, ...). It also responsible for routing mechanisms, msg segmentation
        connection and connectionless data transfer
        SCCP along with MPT3(M3UA) corresponds to OSI layer 3

        SCCP can route messages based on GT using Global Title Translation (GTT)
        SLS SCCP can ask MTP3 to stop link rotation in linkset
    */
    private static final Logger logger = LogManager.getLogger(SccpAdapter.class);
    private static SccpAdapter instance;
    private List<Integer> activatedSsns;

    @Getter(AccessLevel.PACKAGE) private SccpStack stack;
    private NodeConfig nodeConfig;
    private static final int MTP3_USER_PART_ID = 1;

    private SccpAdapter(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
        activatedSsns = new ArrayList<>();
    }

    public static SccpAdapter getInstance(NodeConfig nodeConfig, M3uaAdapter m3UaAdapter) throws SystemException {
        logger.debug("Getting SCTP layer...");
        if (instance == null) {
            logger.debug("Creating a new SCTP layer...");
            instance = new SccpAdapter(nodeConfig);
            instance.initialize(m3UaAdapter);
        }

        return instance;
    }

    private void initialize(M3uaAdapter m3UaAdapter) throws SystemException {
        try {
            logger.debug("Initializing SCCP Stack ....");
            var nodeName = nodeConfig.getSs7Association().getLocalNode().getNodeName();
            stack = new SccpStackImpl(nodeName);
            ((SccpStackImpl) stack).setMtp3UserPart(MTP3_USER_PART_ID, (M3UAManagementImpl) m3UaAdapter.getManagement());
            var configDir = Util.getConfigDir(nodeConfig.getSs7Association().getLocalNode().getNodeName());
            stack.setPersistDir(configDir);

            stack.start();
            var provider = (SccpProviderImpl) stack.getSccpProvider();
            provider.registerSccpSstListener(this);

            ((SccpStackImpl) stack).removeAllResourses();
            addRemoteEndpoints();
            addGttRules();
            logger.debug("SCCP layer initialized successfully!");

        } catch (Exception e) {
            logger.error("Failed to initialize SCCP layer.", e);
            throw SystemException.builder().code(ErrorCode.SCCP_INITIALIZATION).build();
        }
    }

    private void addRemoteEndpoints() throws SystemException {
        int networkIndicator;
        var peerNodeList = new ArrayList<Ss7NodeInfo>();
        networkIndicator = Integer.valueOf(nodeConfig.getSs7Association().getNetworkIndicator());
        if (nodeConfig instanceof SepNodeConfig cfg) {
            peerNodeList.add(cfg.getSs7Association().getPeerNode());

        } else {
            ((StpNodeConfig) nodeConfig).getSs7Association().getPeerNodes().forEach(node -> peerNodeList.add(node));
        }

        for (var peerNode : peerNodeList) {
            var spc = Integer.valueOf(peerNode.getPointCode());
            var peerPointCode = PointCode.builder().value(spc).flag(0).build();
            addPcIfNotExist(peerPointCode);

            var ssnStrList = new ArrayList<String>();
            if (peerNode instanceof SepNodeInfo sepNodeInfo) {
                ssnStrList.addAll(sepNodeInfo.getSupportedMapSsnList());
                ssnStrList.addAll(sepNodeInfo.getSupportedCapSsnList());
                var ssnList = ssnStrList.stream().map(e -> Integer.valueOf(e)).collect(Collectors.toList());
                for (var ssn : ssnList) {
                    addSsnIfNotExist(peerPointCode, ssn);
                }
            }

            var localPc = PointCode.builder()
                    .value(Integer.valueOf(nodeConfig.getSs7Association().getLocalNode().getPointCode())).flag(0).build();
            var sapIndex = addSapIfNotExist(localPc, networkIndicator);
            addMtp3DestinationIfNotExist(sapIndex, peerPointCode);
        }
    }

    private int addPcIfNotExist(PointCode pc) throws SystemException {
        try {
            int index = -1;
            for (var entry : stack.getSccpResource().getRemoteSpcs().entrySet()) {
                var value = entry.getValue();
                if (value.getRemoteSpc() == pc.getValue() && value.getRemoteSpcFlag() == pc.getFlag()) {
                    index = entry.getKey();
                    logger.debug("Found remote SPC index: " + index);
                    break;
                }
            }

            if (index == -1) {
                index = stack.getSccpResource().getRemoteSpcs().size();
                logger.debug("Adding remote SPC index: " + index + ", PC: " + pc.getValue());
                var mask = 0;
                stack.getSccpResource().addRemoteSpc(index, pc.getValue(), pc.getFlag(), mask);
            }
            return index;

        } catch (Exception e) {
            logger.error("Failed to add SCCP remote endpoints SPCs.", e);
            throw SystemException.builder().code(ErrorCode.SCCP_INITIALIZATION).build();
        }

    }

    private int addSsnIfNotExist(PointCode pc, int ssn) throws SystemException {
        try {
            int index = -1;
            for (var entry : stack.getSccpResource().getRemoteSsns().entrySet()) {
                var value = entry.getValue();
                if (value.getRemoteSpc() == pc.getValue() && value.getRemoteSsn() == ssn) {
                    index = entry.getKey();
                    logger.debug("Found remote SSN index: " + index);
                    break;
                }
            }

            if (index == -1) {
                index = stack.getSccpResource().getRemoteSsns().size();
                logger.debug("Adding remote SSN index: " + index + ", PC: " + pc.getValue() +
                        ", SSN: " + ssn);
                var markProhibitedWhenSpcResuming = false;
                stack.getSccpResource().addRemoteSsn(index, pc.getValue(), ssn, Constants.SCCP_SSN_FLAG, markProhibitedWhenSpcResuming);
            }
            return index;

        } catch (Exception e) {
            logger.error("Failed to add SCCP remote endpoints SSNs.", e);
            throw SystemException.builder().code(ErrorCode.SCCP_INITIALIZATION).build();
        }
    }

    private int addSapIfNotExist(PointCode pc, int networkIndicator) throws SystemException {
        try {
            int index = -1;
            for (var entry : stack.getRouter().getMtp3ServiceAccessPoints().entrySet()) {
                var value = entry.getValue();
                if (value.getOpc() == pc.getValue() && value.getNi() == networkIndicator) {
                    index = entry.getKey();
                    logger.debug("Found remote ServiceAccessPoint index: " + index);
                    break;
                }
            }

            if (index == -1) {
                index = stack.getRouter().getMtp3ServiceAccessPoints().size();
                logger.debug("Adding remote SAP index: " + index + ", PC: " + pc.getValue() +
                        ", networkIndicator: " + networkIndicator);
                stack.getRouter().addMtp3ServiceAccessPoint(index, MTP3_USER_PART_ID, pc.getValue(),
                        networkIndicator, 0);
            }
            return index;

        } catch (Exception e) {
            logger.error("Failed to add SCCP remote endpoints ServiceAccessPoint.", e);
            throw SystemException.builder().code(ErrorCode.SCCP_INITIALIZATION).build();
        }

    }

    private int addMtp3DestinationIfNotExist(int sapId, PointCode pc) throws SystemException {
        try {
            int index = -1;
            for (var entry : stack.getRouter().getMtp3ServiceAccessPoint(sapId).getMtp3Destinations().entrySet()) {
                var value = entry.getValue();
                if (value.getFirstDpc() == pc.getValue() && value.getLastDpc() == pc.getValue()) {
                    index = entry.getKey();
                    logger.debug("Found remote MTP3 destination index: " + index);
                    break;
                }
            }
            if (index == -1) {
                index = stack.getRouter().getMtp3ServiceAccessPoint(sapId).getMtp3Destinations().size();
                logger.debug("Adding remote MTP3 Dest index: " + index + ", PC: " + pc.getValue());
                var firstSls = 0;
                var lastSls = 255;
                var slsMask = FastHDLC.DATA_MASK;
                stack.getRouter().addMtp3Destination(sapId, index, pc.getValue(), pc.getValue(), firstSls, lastSls, slsMask);
            }

            return index;

        } catch (Exception e) {
            logger.error("Failed to add SCCP remote endpoints SPCs.", e);
            throw SystemException.builder().code(ErrorCode.SCCP_INITIALIZATION).build();
        }
    }

    public ParameterFactory getParamFactory() {
        return stack.getSccpProvider().getParameterFactory();
    }

    protected Router getRouter() {
        return stack.getRouter();
    }

    protected void addGttRules() throws SystemException {
        try {
            logger.info("Adding SCCP GTT...");
            var factory = getParamFactory();
            var router = getRouter();

            var defaultTt = Constants.TRANSLATION_TYPE_NOT_USED;
            var trustedNodeTt = Constants.TRANSLATION_TYPE_TRUSTED_NODE;
            var numberingPlan = NumberingPlan.ISDN_TELEPHONY;
            var e214NumberingPlan = NumberingPlan.ISDN_MOBILE;
            EncodingScheme encodingScheme = null; // use default encoding scheme (even/odd BCD)
            var natureOfAddress = NatureOfAddress.INTERNATIONAL;
            int translationAddressId;
            SccpAddress translationAddress;
            SccpAddress matchPattern;
            var rulesIndex = router.getRules().size() ;

            var localPc = Integer.valueOf(nodeConfig.getSs7Association().getLocalNode().getPointCode());
            // TODO IMP TRX: STP: Define subscriber's MSISDN prefix instead of wildcard
            var wildcardGt = factory.createGlobalTitle(Constants.SCCP_DIGITS_PATTERN_WILD_CARD_ALL,
                    defaultTt, numberingPlan, encodingScheme, natureOfAddress);
            var wildcardGtE214 = factory.createGlobalTitle(Constants.SCCP_DIGITS_PATTERN_WILD_CARD_ALL,
                    defaultTt, e214NumberingPlan, encodingScheme, natureOfAddress);

            // Adding GTT rules for STP node
            if (nodeConfig instanceof StpNodeConfig cfg) {
                // Translate all incoming messages with peer GT to peer nodes.
                for (var peerNode : cfg.getSs7Association().getPeerNodes()) {
                    var peerGt = factory.createGlobalTitle(peerNode.getGlobalTitle(),
                            defaultTt, numberingPlan, encodingScheme, natureOfAddress);
                    var peerPc = Integer.valueOf(peerNode.getPointCode());
                    matchPattern = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                            peerGt, Constants.SCCP_SPC_NOT_PRESENT, Constants.SCCP_SSN_NOT_PRESENT);
                    translationAddress = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                            peerGt, peerPc, Constants.SCCP_SSN_NOT_PRESENT);
                    translationAddressId = addIfNotExist(translationAddress);
                    logger.debug("Adding GTT matchPattern: [" + matchPattern + "], translationAddress: [" + translationAddress + "]");
                    // OriginationType.LOCAL means match rules only on messages generated locally by this node
                    // OriginationType.REMOTE means match rules only on messages received from a remote node
                    // RuleType.SOLITARY means that translation depends on primary address only, not secondary
                    // newCallingPartyAddressAddressId means don't change calling party address.
                    // PC and SSN are not used in ROUTING_BASED_ON_DPC_AND_SSN, they are retrieved using GTT
                    // Constants.SCCP_MASK_KEEP means don't replace GT during translation
                    // Constants.SCCP_MASK_REPLACE means replace GT during translation (according to the pattern)
                    router.addRule(rulesIndex, RuleType.SOLITARY, null, OriginationType.REMOTE, matchPattern,
                            Constants.SCCP_MASK_REPLACE, translationAddressId, Constants.SCCP_ROUTING_ADDRESS_ID_NOT_USED,
                            null, 0, null);
                    rulesIndex++;

                    // Translate all incoming messages with peer GT and TRANSLATION_TYPE_TRUSTED_NODE to peer nodes.
                    var peerGtTrustedNode = factory.createGlobalTitle(peerNode.getGlobalTitle(),
                            trustedNodeTt, numberingPlan, encodingScheme, natureOfAddress);
                    matchPattern = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                            peerGtTrustedNode, Constants.SCCP_SPC_NOT_PRESENT, Constants.SCCP_SSN_NOT_PRESENT);
                    translationAddress = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                            peerGt, peerPc, Constants.SCCP_SSN_NOT_PRESENT); // Only STP should add TRANSLATION_TYPE_TRUSTED_NODE
                    translationAddressId = addIfNotExist(translationAddress);
                    logger.debug("Adding GTT matchPattern: [" + matchPattern + "], translationAddress: [" + translationAddress + "]");
                    router.addRule(rulesIndex, RuleType.SOLITARY, null, OriginationType.REMOTE, matchPattern,
                            Constants.SCCP_MASK_REPLACE, translationAddressId, Constants.SCCP_ROUTING_ADDRESS_ID_NOT_USED,
                            null, 0, null);
                    rulesIndex++;
                }

                // Translate all incoming messages targeted to subscriber's MSISDN range to HLR
                var hlrGtStr = nodeConfig.getTargetNetwork().getHlrGt();
                var hlrNode = ((StpNodeConfig) nodeConfig).getSs7Association().getPeerNodes().stream()
                        .filter(node -> node.getGlobalTitle().equals(hlrGtStr)).findFirst().get();
                var hlrGt = factory.createGlobalTitle(hlrNode.getGlobalTitle(),
                        defaultTt, numberingPlan, encodingScheme, natureOfAddress);
                var hlrPc = Integer.valueOf(hlrNode.getPointCode());
                matchPattern = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                        wildcardGt, Constants.SCCP_SPC_NOT_PRESENT, Constants.SCCP_SSN_NOT_PRESENT); // DCP is not used in matching
                translationAddress = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                        hlrGt, hlrPc, Constants.SCCP_SSN_NOT_PRESENT);
                translationAddressId = addIfNotExist(translationAddress);
                logger.debug("Adding GTT matchPattern: [" + matchPattern + "], translationAddress: [" + translationAddress + "]");
                router.addRule(rulesIndex, RuleType.SOLITARY, null, OriginationType.REMOTE, matchPattern,
                        Constants.SCCP_MASK_REPLACE, translationAddressId, Constants.SCCP_ROUTING_ADDRESS_ID_NOT_USED,
                        null, 0, null);
                rulesIndex++;

                // Translate all incoming messages targeted to subscriber's IMSI (E.214) to HLR changing TT to
                // TRANSLATION_TYPE_TRUSTED_NODE to simulate Home Routing bypass
                var hlrGtTrustedNode = factory.createGlobalTitle(hlrNode.getGlobalTitle(), trustedNodeTt,
                        numberingPlan, encodingScheme, natureOfAddress);
                matchPattern = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                        wildcardGtE214, Constants.SCCP_SPC_NOT_PRESENT, Constants.SCCP_SSN_NOT_PRESENT);
                translationAddress = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                        hlrGtTrustedNode, hlrPc, Constants.SCCP_SSN_NOT_PRESENT);
                translationAddressId = addIfNotExist(translationAddress);
                logger.debug("Adding GTT matchPattern: [" + matchPattern + "], translationAddress: [" + translationAddress + "]");
                router.addRule(rulesIndex, RuleType.SOLITARY, null, OriginationType.REMOTE, matchPattern,
                        Constants.SCCP_MASK_REPLACE, translationAddressId, Constants.SCCP_ROUTING_ADDRESS_ID_NOT_USED,
                        null, 0, null);
                rulesIndex++;

            } else {
                // Adding GTT rules for Intruder and other Lab nodes (HLR, VLR/MSC)
                // Translate all outgoing messages to peer node SPC (STP)
                var localGt = factory.createGlobalTitle(((SepNodeConfig) nodeConfig).getSs7Association()
                                .getLocalNode().getGlobalTitle(), defaultTt, numberingPlan, encodingScheme, natureOfAddress);
                var peerNode = ((SepNodeConfig) nodeConfig).getSs7Association().getPeerNode();
                var peerPc = Integer.valueOf(peerNode.getPointCode());
                matchPattern = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                        wildcardGt, Constants.SCCP_SPC_NOT_PRESENT, Constants.SCCP_SSN_NOT_PRESENT);
                translationAddress = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                        wildcardGt, peerPc, Constants.SCCP_SSN_NOT_PRESENT);
                translationAddressId = addIfNotExist(translationAddress);
                logger.debug("Adding GTT matchPattern: [" + matchPattern + "], translationAddress: [" + translationAddress + "]");
                router.addRule(rulesIndex, RuleType.SOLITARY, null, OriginationType.LOCAL, matchPattern,
                        Constants.SCCP_MASK_KEEP, translationAddressId, Constants.SCCP_ROUTING_ADDRESS_ID_NOT_USED,
                        null, 0, null);
                rulesIndex++;

                // Translate all outgoing messages with TRANSLATION_TYPE_TRUSTED_NODE to peer node SPC (STP)
                var wildcardGtTrustedNode = factory.createGlobalTitle(Constants.SCCP_DIGITS_PATTERN_WILD_CARD_ALL,
                        trustedNodeTt, numberingPlan, encodingScheme, natureOfAddress);
                matchPattern = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                        wildcardGtTrustedNode, Constants.SCCP_SPC_NOT_PRESENT, Constants.SCCP_SSN_NOT_PRESENT);
                translationAddress = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                        wildcardGt, peerPc, Constants.SCCP_SSN_NOT_PRESENT);
                translationAddressId = addIfNotExist(translationAddress);
                logger.debug("Adding GTT matchPattern: [" + matchPattern + "], translationAddress: [" + translationAddress + "]");
                router.addRule(rulesIndex, RuleType.SOLITARY, null, OriginationType.LOCAL, matchPattern,
                        Constants.SCCP_MASK_KEEP, translationAddressId, Constants.SCCP_ROUTING_ADDRESS_ID_NOT_USED,
                        null, 0, null);
                rulesIndex++;

                // Translate incoming messages with local GT to local node SPC
                matchPattern = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                        localGt, Constants.SCCP_SPC_NOT_PRESENT, Constants.SCCP_SSN_NOT_PRESENT);
                translationAddress = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                        localGt, localPc, Constants.SCCP_SSN_NOT_PRESENT);
                translationAddressId = addIfNotExist(translationAddress);
                logger.debug("Adding GTT matchPattern: [" + matchPattern + "], translationAddress: [" + translationAddress + "]");
                router.addRule(rulesIndex, RuleType.SOLITARY, null, OriginationType.REMOTE, matchPattern,
                        Constants.SCCP_MASK_REPLACE, translationAddressId, Constants.SCCP_ROUTING_ADDRESS_ID_NOT_USED,
                        null, 0, null);
                rulesIndex++;

                if (nodeConfig instanceof LabNodeConfig cfg) {
                    // Translate incoming messages with local GT and TRANSLATION_TYPE_TRUSTED_NODE to local node SPC
                    var localGtTrustedNode = factory.createGlobalTitle(((SepNodeConfig) nodeConfig)
                                    .getSs7Association().getLocalNode().getGlobalTitle(), trustedNodeTt, numberingPlan,
                            encodingScheme, natureOfAddress);
                    matchPattern = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                            localGtTrustedNode, Constants.SCCP_SPC_NOT_PRESENT, Constants.SCCP_SSN_NOT_PRESENT);
                    translationAddress = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                            localGtTrustedNode, localPc, Constants.SCCP_SSN_NOT_PRESENT);
                    translationAddressId = addIfNotExist(translationAddress);
                    logger.debug("Adding GTT matchPattern: [" + matchPattern + "], translationAddress: [" + translationAddress + "]");
                    router.addRule(rulesIndex, RuleType.SOLITARY, null, OriginationType.REMOTE, matchPattern,
                            Constants.SCCP_MASK_REPLACE, translationAddressId, Constants.SCCP_ROUTING_ADDRESS_ID_NOT_USED,
                            null, 0, null);
                    rulesIndex++;

                    // Translate incoming messages with local GT E.214 to local node SPC
                    var e214LocalGt = factory.createGlobalTitle(((SepNodeConfig) nodeConfig).getSs7Association()
                            .getLocalNode().getGlobalTitle(), defaultTt, e214NumberingPlan, encodingScheme, natureOfAddress);
                    matchPattern = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                            e214LocalGt, Constants.SCCP_SPC_NOT_PRESENT, Constants.SCCP_SSN_NOT_PRESENT);
                    translationAddress = factory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                            e214LocalGt, localPc, Constants.SCCP_SSN_NOT_PRESENT);
                    translationAddressId = addIfNotExist(translationAddress);
                    logger.debug("Adding GTT matchPattern: [" + matchPattern + "], translationAddress: [" + translationAddress + "]");
                    router.addRule(rulesIndex, RuleType.SOLITARY, null, OriginationType.REMOTE, matchPattern,
                            Constants.SCCP_MASK_REPLACE, translationAddressId, Constants.SCCP_ROUTING_ADDRESS_ID_NOT_USED,
                            null, 0, null);
                }
            }
            logger.debug("GTT rules added successfully.");

        } catch (Exception e) {
            String msg = "Failed to add GTT rules";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.M3UA_INITIALIZATION).message(msg).parent(e).build();
        }
    }

    protected int addIfNotExist(SccpAddress translationAddress) throws Exception {
        int index = -1;
        var router = getRouter();
        for (var addressSet : router.getRoutingAddresses().entrySet()) {
            var address = addressSet.getValue();
            if (translationAddress.equals(address)) {
                index = addressSet.getKey();
                logger.debug("Found translation address index: " + index);
                break;
            }
        }

        if (index == -1) {
            index = router.getRoutingAddresses().size();
            router.addRoutingAddress(index, translationAddress);
        }

        return index;
    }

    public Stream<Integer> getNotActivatedSsnList() {
        var supportedSsnList = new ArrayList<String>();
        supportedSsnList.addAll(((SepNodeConfig) nodeConfig).getSs7Association().getLocalNode().getSupportedMapSsnList());
        supportedSsnList.addAll(((SepNodeConfig) nodeConfig).getSs7Association().getLocalNode().getSupportedCapSsnList());
        var notActivatedSsnList = supportedSsnList.stream().map(e -> Integer.valueOf(e))
                .filter(ssn -> !activatedSsns.contains(ssn));//.collect(Collectors.toList());
        return notActivatedSsnList.sorted();
    }

    public boolean isAllSsnActivated() {
        return getNotActivatedSsnList().count() == 0;
    }

    @Override
    public void onSst(int affectedSsn) {
        logger.info("ReceivedSST for SSN: " + affectedSsn);
        activatedSsns.add(affectedSsn);
    }

    @Getter @Builder
    private static class PointCode {
        // Point codes are represented in 14-bits ITU decimal or 3-8-3 format
        // 3-8-3 format is commonly used for international PC (ISPC), while decimal is used for national
        // In decimal format, the X most significant digits represents the network ID, while the remaining
        // part represents the node id
        // Decimal format can start with nature of address identifier (0- for international, and 2- for national)
        // Ref: https://www.citc.gov.sa/ar/RulesandSystems/RegulatoryDocuments/Numbering/Documents/TA110E.PDF
        // Ref: https://docs.oracle.com/cd/E93309_01/docs.466/SS7/GUID-03974BB1-C30D-41A4-8FC5-C99DA6DC46D2.htm
        // List of PCs: https://www.itu.int/dms_pub/itu-t/opb/sp/T-SP-Q.708B-2020-PDF-E.pdf
        private int value;
        private int flag; // TRX: 0
    }
}