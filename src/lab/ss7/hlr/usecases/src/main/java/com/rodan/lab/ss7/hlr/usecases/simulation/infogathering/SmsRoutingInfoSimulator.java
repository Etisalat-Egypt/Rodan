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
import com.rodan.intruder.ss7.entities.event.model.sms.SriSmRequest;
import com.rodan.intruder.ss7.entities.event.service.MapSmsServiceListener;
import com.rodan.intruder.ss7.entities.payload.Ss7Payload;
import com.rodan.intruder.ss7.entities.payload.sms.SriSmResponsePayload;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.lab.ss7.hlr.usecases.model.infogathering.SmsRoutingInfoSimOptions;
import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorConstants;
import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorTemplate;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.ApplicationException;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.library.util.Util;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Ayman ElSherif
 */
@Module(name = Ss7SimulatorConstants.SMS_ROUTING_INFO_SIM_NAME)
public class SmsRoutingInfoSimulator extends Ss7SimulatorTemplate implements SignalingModule, MapSmsServiceListener {
    final static Logger logger = LogManager.getLogger(SmsRoutingInfoSimulator.class);

    @Builder
    public SmsRoutingInfoSimulator(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);

        // Simulate SMS Home Routing
        var payload = generatePseudoPayload();
        setMainPayload(payload);
        setCurrentPayload(getMainPayload());
        logger.debug("Payload: " + payload);
    }

    @Override
    protected void addServiceListener() throws SystemException {
        logger.debug("Adding service listeners");
        super.addServiceListener();
        var localSsn = getMainPayload().getLocalSsn();
        getGateway().addSmsServiceListener(localSsn,this);
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
    public void onSendRoutingInfoForSMRequest(SriSmRequest request) {
        try {
            var msisdn = request.getMsisdn();
            var serviceCentre = request.getServiceCentreAddress();
            notify("Received SRI-SM request for MSISDN: " + msisdn + " from serviceCemtre: " + serviceCentre,
                    NotificationType.PROGRESS);

            var dialog = request.getDialog();
            var invokeId = request.getInvokeId();
            dialog.setUserObject(invokeId);

            // Simulate SMS Home Routing E.214 bypass
            var returnRealData = (!request.getUseSmsHomeRouter()) || isDoubleMapBypassUsed(request);
            logger.debug("returnRealData: " + returnRealData);
            var newPayload = returnRealData ? generateRealPayload() : generatePseudoPayload();
            var payload = (SriSmResponsePayload) newPayload;
            payload = payload.withInvokeId(invokeId);

            getGateway().addToDialog(payload, dialog);
            getGateway().send(dialog);

        } catch (ApplicationException e) {
            String msg = "Failed to handle SRI request";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }

    private Ss7Payload generatePseudoPayload() {
        var options = (SmsRoutingInfoSimOptions) moduleOptions;
        var mcc = moduleOptions.getNodeConfig().getTargetNetwork().getMcc();
        var mnc = moduleOptions.getNodeConfig().getTargetNetwork().getMnc();
        var pseudoImsi = Util.generateRandomImsi(mcc, mnc);
        var smsRouterGt = moduleOptions.getNodeConfig().getTargetNetwork().getSmscGt();
        var vmsc = smsRouterGt;

        return SriSmResponsePayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .imsi(pseudoImsi).vmscGt(vmsc)
                .build();
    }

    private Ss7Payload generateRealPayload() {
        var options = (SmsRoutingInfoSimOptions) moduleOptions;
        return SriSmResponsePayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .imsi(options.getImsi()).vmscGt(options.getVmscGt())
                .build();
    }
}
