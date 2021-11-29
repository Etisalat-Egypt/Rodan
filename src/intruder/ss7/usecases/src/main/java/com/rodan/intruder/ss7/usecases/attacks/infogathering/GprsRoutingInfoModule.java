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

package com.rodan.intruder.ss7.usecases.attacks.infogathering;

import com.rodan.intruder.ss7.entities.event.model.pdp.SriGprsResponse;
import com.rodan.intruder.ss7.entities.event.service.MapPdpServiceListener;
import com.rodan.intruder.ss7.entities.payload.packet.SriGprsPayload;
import com.rodan.intruder.ss7.usecases.Ss7ModuleTemplate;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleConstants;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.infogathering.GprsRoutingInfoOptions;
import com.rodan.intruder.ss7.usecases.model.infogathering.GprsRoutingInfoResponse;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = Ss7ModuleConstants.GPRS_ROUTING_INFO_NAME, category = Ss7ModuleConstants.INFO_GATHERING_CATEGORY_DISPLAY_NAME,
        displayName = Ss7ModuleConstants.GPRS_ROUTING_INFO_DISPLAY_NAME, brief = Ss7ModuleConstants.GPRS_ROUTING_INFO_BRIEF,
        description = Ss7ModuleConstants.GPRS_ROUTING_INFO_DESCRIPTION, rank = Ss7ModuleConstants.GPRS_ROUTING_INFO_RANK,
        mainPayload = Constants.SRI_GPRS_PAYLOAD_NAME)
public class GprsRoutingInfoModule extends Ss7ModuleTemplate implements SignalingModule, MapPdpServiceListener {
    final static Logger logger = LogManager.getLogger(GprsRoutingInfoModule.class);

    @Builder
    public GprsRoutingInfoModule(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (GprsRoutingInfoOptions) moduleOptions;
        var payload = SriGprsPayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .imsi(options.getImsi()).msisdn(options.getMsisdn())
                .targetHlrGt(options.getTargetHlrGt()).ggsnNumber(options.getGgsnNumber())
                .mapVersion(options.getMapVersion())
                .build();
        setMainPayload(payload);
        setCurrentPayload(getMainPayload());
        logger.debug("Payload: " + payload);
    }

    @Override
    protected void addServiceListener() throws SystemException {
        logger.debug("Adding service listeners");
        super.addServiceListener();
        var localSsn = getMainPayload().getLocalSsn();
        getGateway().addPdpServiceListener(localSsn, this);
    }

    @Override
    protected void cleanup() throws SystemException {
        super.cleanup();
        if (getGateway() != null && getMainPayload() != null) {
            var localSsn = getMainPayload().getLocalSsn();
            getGateway().removePdpServiceListener(localSsn, this);
        }
    }

    @Override
    public void onSendRoutingInfoForGprsResponse(SriGprsResponse response) {
        logger.debug("##### Received SRI-GPRS response!");
        logger.debug(response);
        logger.debug("Parsing response.");

        moduleResponse = GprsRoutingInfoResponse.builder()
                .notReachable(response.isNotReachable()).notReachableReason(response.getNotReachableReason())
                .ggsnAddressType(response.getGgsnAddressType()).ggsnAddress(response.getGgsnAddress())
                .sgsnAddressType(response.getSgsnAddressType()).sgsnAddress(response.getSgsnAddress())
                .build();
        setResultReceived(true);
        logger.debug("SRI-GPRS completed!");
    }
}
