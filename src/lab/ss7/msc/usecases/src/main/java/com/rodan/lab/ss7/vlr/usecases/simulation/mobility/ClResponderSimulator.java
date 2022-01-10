/*
 * Etisalat Egypt, Open Source
 * Copyright 2022, Etisalat Egypt and individual contributors
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

package com.rodan.lab.ss7.vlr.usecases.simulation.mobility;

import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.ss7.entities.event.model.mobility.ClRequest;
import com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener;
import com.rodan.intruder.ss7.entities.payload.sms.FsmPayload;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorConstants;
import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorTemplate;
import com.rodan.lab.ss7.msc.usecases.model.mobility.ClResponderSimOptions;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.library.util.Util;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Ayman ElSherif
 */
@Module(name = Ss7SimulatorConstants.CL_RESPONDER_NAME)
public class ClResponderSimulator extends Ss7SimulatorTemplate implements SignalingModule, MapMobilityServiceListener {
    final static Logger logger = LogManager.getLogger(ClResponderSimulator.class);

    @Builder
    public ClResponderSimulator(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (ClResponderSimOptions) moduleOptions;
        var smscGt = options.getNodeConfig().getTargetNetwork().getSmscGt();
        var payload = FsmPayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .sender("Google").messageType("normal").smscGt(smscGt).spoofSmsc("No")
                .mapVersion("2")
                .build();
        setMainPayload(payload);
        setCurrentPayload(getMainPayload());
        logger.debug("Payload: " + payload);
    }

    @Override
    protected void addServiceListener() throws SystemException {
        logger.debug("Adding service listeners");
        super.addServiceListener();
        var localSsn = getMainPayload().getLocalSsn(); // TODO IMP: Check should listen for both MSC & VLR
        getGateway().addMobilityServiceListener(localSsn,this);
    }

    @Override
    protected void cleanup() throws SystemException {
        super.cleanup();
        if (getGateway() != null && getMainPayload() != null) {
            var localSsn = getMainPayload().getLocalSsn();
            getGateway().removeMobilityServiceListener(localSsn, this);
        }
    }

    @Override
    public void onCancelLocationRequest(ClRequest request) {
        try {
            logger.debug("Received CL:" + request);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }

            var payload = (FsmPayload) getMainPayload();
            var content = "G-" + Util.generateRandomNumber(100000, 999999) + " is your Google verification code.";
            payload = payload.withImsi(request.getImsi()).withTargetMscGt(request.getNewMscGt()).withContent(content);
            var dialog = getGateway().generateMapDialog(payload);
            getGateway().addToDialog(payload, dialog);
            getGateway().send(dialog);

        } catch (SystemException e) {
            String msg = "Failed to send SMS to serving node";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }
}
