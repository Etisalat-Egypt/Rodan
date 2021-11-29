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

package com.rodan.connectivity.ss7.payloadwrapper.mobility;

import com.rodan.connectivity.ss7.adapter.MapAdapter;
import com.rodan.connectivity.ss7.adapter.SccpAdapter;
import com.rodan.connectivity.ss7.payloadwrapper.JSs7PayloadWrapper;
import com.rodan.connectivity.ss7.service.MapDialogGenerator;
import com.rodan.connectivity.ss7.service.MapMobilityService;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Payload;
import com.rodan.library.model.config.node.config.IntruderNodeConfig;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextName;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPDialogMobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.*;

import java.util.ArrayList;

@Payload(name = Constants.ISD_PAYLOAD_NAME)
@ToString(callSuper = true)
    public class IsdPayloadWrapper extends JSs7PayloadWrapper<MapMobilityService, MAPDialogMobility> {
    public enum Usage {
        BAR(0), REDIRECT_CAMEL(1), FORWARD_SS(2), LINE_IDENTIFICATION(3);

        private int code;

        private Usage(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Usage getInstance(int code) {
            switch (code) {
                case 0:
                    return BAR;
                case 1:
                    return REDIRECT_CAMEL;
                case 2:
                    return FORWARD_SS;
                case 3:
                    return LINE_IDENTIFICATION;
                default:
                    return null;
            }
        }
    }

    @Getter(AccessLevel.PRIVATE) private Usage usage;
    @Getter(AccessLevel.PRIVATE) private String imsi;
    @Getter(AccessLevel.PRIVATE) private String msisdn;
    @Getter(AccessLevel.PRIVATE) private String forwardMsisdn;
    @Getter(AccessLevel.PRIVATE) private String gsmScf;
    @Getter(AccessLevel.PRIVATE) private String targetVlrGt;
    @Getter(AccessLevel.PRIVATE) private String barred;
    @Getter(AccessLevel.PRIVATE) private String spoofHlr;
    @Getter(AccessLevel.PRIVATE) private String targetHlrGt;
    @Getter(AccessLevel.PRIVATE) private String mapVersion;

    @Builder
    public IsdPayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig, SccpAdapter sccpAdapter,
                             MapAdapter mapAdapter, MapDialogGenerator<MAPDialogMobility> dialogGenerator, Usage usage,
                             String imsi, String msisdn, String forwardMsisdn, String gsmScf, String targetVlrGt,
                             String barred, String spoofHlr, String targetHlrGt, String mapVersion) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.usage = usage;
        this.imsi = imsi;
        this.msisdn = msisdn;
        this.forwardMsisdn = forwardMsisdn;
        this.gsmScf = gsmScf;
        this.targetVlrGt = targetVlrGt;
        this.barred = barred;
        this.spoofHlr = spoofHlr;
        this.targetHlrGt = targetHlrGt;
        this.mapVersion = mapVersion;
    }

    @Override
    public MAPDialogMobility generateCarrier() throws SystemException {
        validate();

        var sccpFactory = getSccpAdapter().getParamFactory();
        var spoofHlr = "Yes".equalsIgnoreCase(getSpoofHlr());
        var callingStr = spoofHlr ? getTargetHlrGt() : getLocalGt();
        var callingGt = sccpFactory.createGlobalTitle(callingStr,
                TRANSLATION_TYPE, ISDN_TELEPHONY_INDICATOR, ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var callingPc = Integer.valueOf(getNodeConfig().getSs7Association().getLocalNode().getPointCode());
        var callingParty = sccpFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, callingGt,
                callingPc, getLocalSsn());
        var calledGt = sccpFactory.createGlobalTitle(getTargetVlrGt(),
                TRANSLATION_TYPE, ISDN_TELEPHONY_INDICATOR, ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var calledPc = Integer.valueOf(((IntruderNodeConfig) getNodeConfig()).getSs7Association().getPeerNode().getPointCode());
        var calledParty = sccpFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, calledGt,
                calledPc, getRemoteSsn());

        return dialogGenerator.generate(callingParty, calledParty, getApplicationContext());
    }

    @Override
    public void addToCarrier(MAPDialogMobility dialog) throws SystemException {
        try {
            validate();

            var mapFactory = getMapAdapter().getParamFactory();
            // Ref: https://www.etsi.org/deliver/etsi_ts/129000_129099/129002/03.07.01_60/ts_129002v030701p.pdf
            // IMSI is used instead of MSISDN if the service is not used in an ongoing transaction.
            var imsi = mapFactory.createIMSI(getImsi());
            // TODO IMP: try setting MSISDN to null and remove it from IsdOptions
            ISDNAddressString msisdn;
            if (StringUtils.isNotBlank(getMsisdn())) {
                msisdn = mapFactory.createISDNAddressString(AddressNature.international_number,
                        NumberingPlan.ISDN, getMsisdn());
            } else {
                msisdn = null;
            }

            SubscriberStatus subscriberStatus;
            ODBData odbData = null;
            ArrayList<ExtSSInfo> provisionedSsList = null;
            VlrCamelSubscriptionInfo vlrCamelSubscriptionInfo = null;

            switch (getUsage()) {
                case BAR -> {
                    var barCalls = "Yes".equalsIgnoreCase(getBarred());
                    subscriberStatus = barCalls ? SubscriberStatus.operatorDeterminedBarring : SubscriberStatus.serviceGranted;
                    // serviceGranted will remove all operator determined barring
                    var odbGeneralData = mapFactory.createODBGeneralData(barCalls, false, false, false, false, false, false, false, false,
                            false, false, false, false, false, false, false, false, false,
                            false, false, false,
                            false, false, false, false, false, false, false, false);
                    odbData = mapFactory.createODBData(odbGeneralData, null, null);
                }

                case REDIRECT_CAMEL -> {
                    subscriberStatus = null;

                    var gsmscfStr = StringUtils.isBlank(getGsmScf()) ? getLocalGt() : getGsmScf();
                    var gsmscf = mapFactory.createISDNAddressString(AddressNature.international_number,
                            NumberingPlan.ISDN, gsmscfStr);

                    var oBcsmCamelTDPDatas = new ArrayList<OBcsmCamelTDPData>();
                    // collectedInfo: Dialed digits collected from originating party (subscriber)
                    // Can I use TBcsmTriggerDetectionPoint.termAttemptAuthorized an TBcsmCamelTDPData for MT calls?
                    var tdpData = mapFactory.createOBcsmCamelTDPData(OBcsmTriggerDetectionPoint.collectedInfo, 10, gsmscf, DefaultCallHandling.releaseCall, null);
                    oBcsmCamelTDPDatas.add(tdpData);
                    var ocsi = mapFactory.createOCSI(oBcsmCamelTDPDatas, null, 2, false, false);
                    vlrCamelSubscriptionInfo = mapFactory.createVlrCamelSubscriptionInfo(ocsi, null, null, null, false, null, null, null, null, null, null, null);
                }

//                case LINE_IDENTIFICATION -> {
//                subscriberStatus = null;
//                    var sssStatus = mapFactory.createExtSSStatus(false, true, false, true);
//                    mapFactory.createClirData(sssStatus, CliRestrictionOption.permanent, false);
////                    mapFactory.createClipData(sssStatus, OverrideCategory.overrideEnabled, false);
//                    var clipSs= mapFactory.createSSCode(SupplementaryCodeValue.clip);
//                    var ssData = mapFactory.createExtSSData(clipSs, sssStatus, null, null, null);
//                    var ssInfo = mapFactory.createExtSSInfo(ssData);
//                    provisionedSsList = new ArrayList<>();
//                    provisionedSsList.add(ssInfo);
//                }

//                case FORWARD_SS -> {
//                    var ssCode = mapFactory.createSSCode(SupplementaryCodeValue.barringOfOutgoingCalls);
//                    var telServiceCode = mapFactory.createExtTeleserviceCode(TeleserviceCodeValue.allTeleservices);
//                    var baseServiceCode = mapFactory.createExtBasicServiceCode(telServiceCode);
//                    var serviceStatus = "Yes".equalsIgnoreCase(getBarred()) ?
//                            mapFactory.createExtSSStatus(false, true, false, true) :
//                            mapFactory.createExtSSStatus(false, false, false, false);
//                    var feature = mapFactory.createExtCallBarringFeature(baseServiceCode, serviceStatus, null);
//                    var featureList = new ArrayList<ExtCallBarringFeature>();
//                    featureList.add(feature);
//                    var barringInfo = mapFactory.createExtCallBarInfo(ssCode, featureList, null);
//                    var ssInfo = mapFactory.createExtSSInfo(barringInfo);
//                    provisionedSsList = new ArrayList<ExtSSInfo>();
//                    provisionedSsList.add(ssInfo);
//                }

                default -> {
                    var msg = "Invalid ISD usage: " + getUsage();
                    logger.error(msg);
                    throw SystemException.builder().code(ErrorCode.INVALID_PAYLOAD_PARAMS).message(msg).build();
                }
            }

            dialog.addInsertSubscriberDataRequest(imsi, msisdn, null, subscriberStatus, null,
                    null, provisionedSsList, odbData, false,
                    null, null, null, vlrCamelSubscriptionInfo,
                    null, null, null, false,
                    null, null, false, null, null,
                    null, null, null, null,
                    null, null, null, null, null,
                    false, null, null, null,
                    false, null, null);

        } catch (MAPException e) {
            logger.error("Failed to add CL to dialog", e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
        }
    }

    @Override
    protected MAPApplicationContext getApplicationContext() throws SystemException {
        Integer[] supportedVersions = {1, 2, 3};
        var mapContextVersion = getAcVersion(Integer.parseInt(getMapVersion()), supportedVersions);
        return MAPApplicationContext.getInstance(MAPApplicationContextName.subscriberDataMngtContext, mapContextVersion);
    }
}
