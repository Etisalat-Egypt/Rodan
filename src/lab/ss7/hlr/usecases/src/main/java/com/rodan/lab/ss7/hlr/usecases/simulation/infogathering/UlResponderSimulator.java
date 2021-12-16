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
import com.rodan.intruder.ss7.entities.event.model.mobility.IsdResponse;
import com.rodan.intruder.ss7.entities.event.model.mobility.UlRequest;
import com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener;
import com.rodan.intruder.ss7.entities.payload.callhandling.SriResponsePayload;
import com.rodan.intruder.ss7.entities.payload.mobility.IsdPayload;
import com.rodan.intruder.ss7.entities.payload.mobility.UlPayload;
import com.rodan.intruder.ss7.entities.payload.mobility.UlResponsePayload;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.dos.DosCallBarringOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.lab.ss7.hlr.usecases.model.infogathering.UlResponderSimOptions;
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
@Module(name = Ss7SimulatorConstants.UL_RESPONDER_NAME)
public class UlResponderSimulator extends Ss7SimulatorTemplate implements SignalingModule, MapMobilityServiceListener {
    final static Logger logger = LogManager.getLogger(UlResponderSimulator.class);

    @Builder
    public UlResponderSimulator(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (UlResponderSimOptions) moduleOptions;
        var payload = UlResponsePayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .hlrGt(options.getHlrGt())
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
    public void onUpdateLocationRequest(UlRequest request) throws SystemException {
        try {
            var imsi = request.getImsi();
            var msc = request.getMscGt();
            var vlr = request.getVlrGt();
            notify("Received UL request for IMSI: " + imsi + " from MSC/VLR: " + msc + "/" + vlr,
                    NotificationType.PROGRESS);

            var dialog = request.getDialog();
            var invokeId = request.getInvokeId();
            dialog.setUserObject(invokeId);
            var options = (UlResponderSimOptions) moduleOptions;
            var msisdn = options.getNodeConfig().getTargetSubscriberInfo().getMsisdn();
            var gsmScf = options.getNodeConfig().getTargetNetwork().getGsmScfGt();
            var isdPayload = IsdPayload.builder()
                    .usage(IsdPayload.Usage.REDIRECT_CAMEL)
                    .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                    .invokeId(invokeId)
                    .imsi(imsi).msisdn(msisdn).targetVlrGt(vlr).gsmScf(gsmScf)
                    .build();
            getGateway().addToDialog(isdPayload, dialog);
            getGateway().send(dialog);
            logger.debug("ISD request sent.");

        } catch (ApplicationException e) {
            String msg = "Failed to handle SRI request";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }

    @Override
    public void onInsertSubscriberDataResponse(IsdResponse response) {
        try {
            logger.debug("##### Received ISD response!");
            logger.debug(response);

            // TODO: Send CL to MSC. UL response should be sent after receiving CL response
            var dialog = response.getDialog();
            var invokeId = response.getInvokeId();
            dialog.setUserObject(invokeId);
            var payload = (UlResponsePayload) getMainPayload();
            payload = payload.withInvokeId(invokeId);
            getGateway().addToDialog(payload, dialog);
            getGateway().send(dialog);

        } catch (SystemException e) {
            String msg = "Failed to parse TCAP-END for ISD response";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }
}
