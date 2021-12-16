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

package com.rodan.lab.ss7.hlr.usecases.simulation.location;

import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.ss7.entities.event.model.LocationInfo;
import com.rodan.intruder.ss7.entities.event.model.SubscriberInfo;
import com.rodan.intruder.ss7.entities.event.model.error.details.ReturnErrorProblemType;
import com.rodan.intruder.ss7.entities.event.model.mobility.AtiRequest;
import com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener;
import com.rodan.intruder.ss7.entities.payload.mobility.AtiResponsePayload;
import com.rodan.intruder.ss7.entities.payload.mobility.PsiResponsePayload;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.lab.ss7.hlr.usecases.model.location.LocationAtiSimOptions;
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
@Module(name = Ss7SimulatorConstants.LOCATION_ATI_SIM_NAME)
public class LocationAtiSimulator extends Ss7SimulatorTemplate implements SignalingModule, MapMobilityServiceListener {
    final static Logger logger = LogManager.getLogger(LocationAtiSimulator.class);

    @Builder
    public LocationAtiSimulator(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (LocationAtiSimOptions) moduleOptions;
        var networkInfo = options.getNodeConfig().getTargetNetwork();
        var locationInfo = LocationInfo.builder()
                .mcc(Integer.valueOf(networkInfo.getMcc())).mnc(Integer.valueOf(networkInfo.getMnc()))
                .lac(1234).cellId(5678).ageOfLocation(0)
                .build();
        var subscriberInfo = SubscriberInfo.builder()
                .imei(options.getImei()).state("assumedIdle")
                .build();

        var payload = AtiResponsePayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .subscriberInfo(subscriberInfo).locationInfo(locationInfo)
                .vlrGt(options.getVlrGt()).vmscGt(options.getVmscGt())
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
    public void onAnyTimeInterrogationRequest(AtiRequest request) throws SystemException {
        try {
            var msisdn = request.getMsisdn();
            var requestingGsmScf = request.getGsmScf();
            notify("Received ATI request for MSISDN: " + msisdn + ", from gsmSCF: " + requestingGsmScf, NotificationType.PROGRESS);

            var dialog = request.getDialog();
            var invokeId = request.getInvokeId();
            dialog.setUserObject(invokeId);
            var homeGsmScfGt = moduleOptions.getNodeConfig().getTargetNetwork().getGsmScfGt();
            if (homeGsmScfGt.equals(requestingGsmScf)) {
                var payload = (AtiResponsePayload) getMainPayload();
                payload = payload.withInvokeId(invokeId);
                getGateway().addToDialog(payload, dialog);
                getGateway().send(dialog);

            } else {
                // TODO send MAP returnError with ati-NotAllowed (49) code instead of ReturnErrorUnexpected
                getGateway().sendRejectComponent(dialog, invokeId, ReturnErrorProblemType.ReturnErrorUnexpected);
            }


        } catch (ApplicationException e) {
            String msg = "Failed to handle ATI request";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }
}
