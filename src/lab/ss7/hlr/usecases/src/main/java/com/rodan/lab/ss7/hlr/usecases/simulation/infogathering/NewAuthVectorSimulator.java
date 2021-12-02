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

package com.rodan.lab.ss7.hlr.usecases.simulation.infogathering;

import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.ss7.entities.event.model.mobility.SaiRequest;
import com.rodan.intruder.ss7.entities.event.model.mobility.SaiResponse;
import com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener;
import com.rodan.intruder.ss7.entities.payload.callhandling.SriResponsePayload;
import com.rodan.intruder.ss7.entities.payload.mobility.SaiResponsePayload;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.lab.ss7.hlr.usecases.model.infogathering.NewAuthVectorSimOptions;
import com.rodan.lab.ss7.hlr.usecases.model.infogathering.RoutingInfoSimOptions;
import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorConstants;
import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorTemplate;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.ApplicationException;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Ayman ElSherif
 */
@Module(name = Ss7SimulatorConstants.NEW_AUTH_VECTOR_SIM_NAME)
public class NewAuthVectorSimulator extends Ss7SimulatorTemplate implements SignalingModule, MapMobilityServiceListener {
    final static Logger logger = LogManager.getLogger(NewAuthVectorSimulator.class);

    @Builder
    public NewAuthVectorSimulator(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (NewAuthVectorSimOptions) moduleOptions;
        var payload = SaiResponsePayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .imsi(options.getImsi()).rand("aab0414a41fba93a8521050ed613061b").sres("9ea0ed84").kc("9ffd8aee2a6fac9b")
                .xres("3f718392db295d65d08760a2fa4892fa").authPs("9f6f72fd63212a542e4296a0163ebdc8").kasme("fb1ac46f68f841c13d4662fac2835635343fed6086e78c91fee2c0bdd3d1cb44")
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
    public void onSendAuthenticationInfoRequest(SaiRequest request) {
        try {
            var imsi = request.getImsi();
            var vlr = request.getVlrGt();
            notify("Received SAI request for IMSI: " + imsi + " from VLR: " + vlr, NotificationType.PROGRESS);

            var dialog = request.getDialog();
            var invokeId = request.getInvokeId();
            dialog.setUserObject(invokeId);
            var payload = (SaiResponsePayload) getMainPayload();
            payload.setInvokeId(invokeId);
            payload.setInvokeId(invokeId);
            payload.setRequestingNodeType(request.getRequestingNodeType());
            getGateway().addToDialog(payload, dialog);
            getGateway().send(dialog);

        } catch (ApplicationException e) {
            String msg = "Failed to handle SAI request";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }
}
