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
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.error.ValidationException;
import lombok.Builder;
import lombok.ToString;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPDialogMobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.InsertSubscriberDataRequest;

@Payload(name = Constants.ISD_PAYLOAD_NAME)
@ToString(callSuper = true)
public class IsdForwarderPayloadWrapper extends JSs7PayloadWrapper<MapMobilityService, MAPDialogMobility> {
    private InsertSubscriberDataRequest request;

    @Builder
    public IsdForwarderPayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig,
                                      SccpAdapter sccpAdapter, MapAdapter mapAdapter,
                                      MapDialogGenerator<MAPDialogMobility> dialogGenerator,
                                      InsertSubscriberDataRequest request) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.request = request;
    }

    @Override
    public MAPDialogMobility generateCarrier() throws SystemException, ValidationException {
        var msg = "ForwarderPayload shall NOT generate new dialogs";
        logger.error(msg);
        throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
    }

    @Override
    public void addToCarrier(MAPDialogMobility dialog) throws SystemException {
        try {
            validate();

            dialog.addInsertSubscriberDataRequest(request.getImsi(), request.getMsisdn(), request.getCategory(),
                    request.getSubscriberStatus(), request.getBearerServiceList(),
                    request.getTeleserviceList(), request.getProvisionedSS(), request.getODBData(), request.getRoamingRestrictionDueToUnsupportedFeature(),
                    request.getRegionalSubscriptionData(), request.getVbsSubscriptionData(), request.getVgcsSubscriptionData(),
                    request.getVlrCamelSubscriptionInfo(), request.getExtensionContainer(), request.getNAEAPreferredCI(),
                    request.getGPRSSubscriptionData(), request.getRoamingRestrictedInSgsnDueToUnsupportedFeature(),
                    request.getNetworkAccessMode(), request.getLSAInformation(), request.getLmuIndicator(), request.getLCSInformation(),
                    request.getIstAlertTimer(),
                    request.getSuperChargerSupportedInHLR(), request.getMcSsInfo(), request.getCSAllocationRetentionPriority(),
                    request.getSgsnCamelSubscriptionInfo(),
                    request.getChargingCharacteristics(), request.getAccessRestrictionData(), request.getIcsIndicator(),
                    request.getEpsSubscriptionData(), request.getCsgSubscriptionDataList(),
                    request.getUeReachabilityRequestIndicator(), request.getSgsnNumber(), request.getMmeName(), request.getSubscribedPeriodicRAUTAUtimer(),
                    request.getVplmnLIPAAllowed(), request.getMdtUserConsent(), request.getSubscribedPeriodicLAUtimer());

        } catch (MAPException e) {
            logger.error("Failed to add ISD to dialog", e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).build();
        }
    }

    @Override
    protected MAPApplicationContext getApplicationContext() throws SystemException {
        var msg = "ForwarderPayload shall NOT generate new dialogs";
        logger.error(msg);
        throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
    }
}
