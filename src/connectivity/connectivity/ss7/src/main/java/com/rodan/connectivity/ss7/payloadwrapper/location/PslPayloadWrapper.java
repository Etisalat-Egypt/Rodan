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

package com.rodan.connectivity.ss7.payloadwrapper.location;

import com.rodan.connectivity.ss7.adapter.MapAdapter;
import com.rodan.connectivity.ss7.adapter.SccpAdapter;
import com.rodan.connectivity.ss7.payloadwrapper.JSs7PayloadWrapper;
import com.rodan.connectivity.ss7.service.MapDialogGenerator;
import com.rodan.connectivity.ss7.service.MapLcsService;
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
import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.service.lsm.*;
import org.mobicents.protocols.ss7.map.datacoding.CBSDataCodingSchemeImpl;
import org.mobicents.protocols.ss7.map.primitives.USSDStringImpl;
import org.mobicents.protocols.ss7.map.service.lsm.*;

import java.nio.charset.Charset;

@Payload(name = Constants.PSL_PAYLOAD_NAME)
@ToString(callSuper = true)
public class PslPayloadWrapper extends JSs7PayloadWrapper<MapLcsService, MAPDialogLsm> {
    @Getter(AccessLevel.PRIVATE) private String msisdn;
    @Getter(AccessLevel.PRIVATE) private String imsi;
    @Getter(AccessLevel.PRIVATE) private String targetMscGt;
    @Getter(AccessLevel.PRIVATE) private String gmlcGt;
    @Getter(AccessLevel.PRIVATE) private String abuseOpcodeTag;
    @Getter(AccessLevel.PRIVATE) private String mapVersion;

    @Builder
    public PslPayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig, SccpAdapter sccpAdapter,
                             MapAdapter mapAdapter, MapDialogGenerator<MAPDialogLsm> dialogGenerator, String msisdn,
                             String imsi, String targetMscGt, String gmlcGt, String abuseOpcodeTag, String mapVersion) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.msisdn = msisdn;
        this.imsi = imsi;
        this.targetMscGt = targetMscGt;
        this.gmlcGt = gmlcGt;
        this.abuseOpcodeTag = abuseOpcodeTag;
        this.mapVersion = mapVersion;
    }

    @Override
    public MAPDialogLsm generateCarrier() throws SystemException {
        validate();

        var sccpParamFactory = getSccpAdapter().getParamFactory();
        var callingGt = sccpParamFactory.createGlobalTitle(getLocalGt(), TRANSLATION_TYPE,
                ISDN_TELEPHONY_INDICATOR, ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var callingPc = Integer.valueOf(getNodeConfig().getSs7Association().getLocalNode().getPointCode());
        var callingParty = sccpParamFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                callingGt, callingPc, getLocalSsn());
        var calledGt = sccpParamFactory.createGlobalTitle(getTargetMscGt(), TRANSLATION_TYPE,
                ISDN_TELEPHONY_INDICATOR, ENCODING_SCHEME, NATURE_OF_ADDRESS);
        var calledPc = Integer.valueOf(((IntruderNodeConfig) getNodeConfig()).getSs7Association().getPeerNode().getPointCode());
        var calledParty = sccpParamFactory.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                calledGt, calledPc, getRemoteSsn());

        return dialogGenerator.generate(callingParty, calledParty, getApplicationContext());
    }

    @Override
    public void addToCarrier(MAPDialogLsm dialog) throws SystemException {
        try {
            validate();

            var mapParamFactory = getMapAdapter().getParamFactory();
            ISDNAddressString msisdn = null;
            IMSI imsi = null;

            if (StringUtils.isNotBlank(getImsi())) {
                imsi = mapParamFactory.createIMSI(getImsi());
            } else {
                msisdn = mapParamFactory.createISDNAddressString(AddressNature.international_number, NumberingPlan.ISDN,
                        getMsisdn());
            }

            var gmlcGt = StringUtils.isBlank(getGmlcGt()) ? getLocalGt() : getGmlcGt();
            var gmlcNumber = mapParamFactory.createISDNAddressString(AddressNature.international_number,
                    NumberingPlan.ISDN, gmlcGt);
            var locationType = mapParamFactory.createLocationType(LocationEstimateType.currentOrLastKnownLocation, null);

            // Ref: https://github.com/RestComm/gmlc/blob/master/test-suite/src/main/java/org/mobicents/protocols/ss7/gmlc/load/ClientServer.java
            // Ref: https://www.arib.or.jp/english/html/overview/doc/STD-T63V9_21/5_Appendix/Rel5/23/23271-5d0.pdf
            //      Page 77 Annex B (normative) & Page 71
            var codingScheme = new CBSDataCodingSchemeImpl(0x0F);
            var clientNameStr = ""; // Read from payload
            var nameString = new USSDStringImpl(clientNameStr, null, Charset.defaultCharset());
            var formatIndicator = LCSFormatIndicator.logicalName;
            var clientName = new LCSClientNameImpl(codingScheme, nameString, formatIndicator);
            var requesterIdStr = ""; // Read from payload
            var idString = new USSDStringImpl(requesterIdStr, null, Charset.defaultCharset());
            var requestorID = new LCSRequestorIDImpl(codingScheme, idString, formatIndicator);
            clientName = null; // Client Name is not required for lawfulInterceptServices
            requestorID = null; // Requester ID is not required for lawfulInterceptServices
            var lcsClientID = new LCSClientIDImpl(LCSClientType.lawfulInterceptServices, null, null,
                    clientName, null, null, requestorID);


            var privacyOverride = true;
            var callSessionUnrelated = PrivacyCheckRelatedAction.allowedWithoutNotification;
            var callSessionRelated = PrivacyCheckRelatedAction.allowedWithoutNotification;


            var responseTime = new ResponseTimeImpl(ResponseTimeCategory.delaytolerant);
            var qos = new LCSQoSImpl(6, 6, false, responseTime, null);
            var gadShapes = new SupportedGADShapesImpl(false, false, true, false, false, false, false);
            var codeword = new LCSCodewordImpl();
            codeword = null; // Not currently used, till testing (needed with privacyOverride?)
            var lcsPrivacyCheck = new LCSPrivacyCheckImpl(callSessionUnrelated, callSessionRelated);
            lcsPrivacyCheck = null; // Not currently used, till testing (needed with privacyOverride?)
            dialog.addProvideSubscriberLocationRequest(locationType, gmlcNumber, lcsClientID, privacyOverride, imsi,
                    msisdn, null, null, LCSPriority.highestPriority, qos, null, gadShapes,
                    null, null, codeword, lcsPrivacyCheck, null,
                    null, false, null, null);

        } catch (MAPException e) {
            var msg = "Failed to add PSL to dialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).parent(e).build();
        }
    }

    @Override
    protected MAPApplicationContext getApplicationContext() throws SystemException {
        Integer[] supportedVersions = {3};
        var mapContextVersion = getAcVersion(Integer.parseInt(getMapVersion()), supportedVersions);
        return MAPApplicationContext.getInstance(MAPApplicationContextName.locationSvcEnquiryContext, mapContextVersion);
    }
}
