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

package com.rodan.lab.ss7.msc.usecases.simulation.location;

import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.ss7.entities.event.model.LocationInfo;
import com.rodan.intruder.ss7.entities.event.model.error.details.ReturnErrorProblemType;
import com.rodan.intruder.ss7.entities.event.model.lcs.PslRequest;
import com.rodan.intruder.ss7.entities.event.service.MapLcsServiceListener;
import com.rodan.intruder.ss7.entities.payload.location.PslResponsePayload;
import com.rodan.intruder.ss7.entities.payload.mobility.AtiResponsePayload;
import com.rodan.intruder.ss7.entities.payload.mobility.PsiResponsePayload;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorConstants;
import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorTemplate;
import com.rodan.lab.ss7.msc.usecases.model.LocationPslSimOptions;
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
@Module(name = Ss7SimulatorConstants.LOCATION_PSL_SIM_NAME)
public class LocationPslSimulator extends Ss7SimulatorTemplate implements SignalingModule, MapLcsServiceListener {
    final static Logger logger = LogManager.getLogger(LocationPslSimulator.class);

    @Builder
    public LocationPslSimulator(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (LocationPslSimOptions) moduleOptions;
        var networkInfo = options.getNodeConfig().getTargetNetwork();
        var locationInfo = LocationInfo.builder()
                .mcc(Integer.valueOf(networkInfo.getMcc())).mnc(Integer.valueOf(networkInfo.getMnc()))
                .latitude(35.1515551).longitude(33.3987815).uncertainty(0.0).ageOfLocation(0)
                .build();

        var payload = PslResponsePayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .locationInfo(locationInfo)
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
        getGateway().addLcsServiceListener(localSsn,this);
    }

    @Override
    protected void cleanup() throws SystemException {
        super.cleanup();
        if (getGateway() != null && getMainPayload() != null) {
            var localSsn = getMainPayload().getLocalSsn();
            getGateway().removeLcsServiceListener(localSsn, this);
        }
    }

    @Override
    public void onProvideSubscriberLocationRequest(PslRequest request) {
        try {
            var imsi = request.getImsi();
            var msisdn = request.getMsisdn();
            var requestingMlcGt = request.getMlcNumber();
            notify("Received PSL request for IMSI: " + imsi + ", MSISDN: " + msisdn + " from MLC: " +
                    requestingMlcGt, NotificationType.PROGRESS);

            var dialog = request.getDialog();
            var invokeId = request.getInvokeId();
            dialog.setUserObject(invokeId);
            var homeGmlcGt = moduleOptions.getNodeConfig().getTargetNetwork().getGmlcGt();
            if (homeGmlcGt.equals(requestingMlcGt)) {
                var payload = (PslResponsePayload) getMainPayload();
                payload = payload.withInvokeId(invokeId);
                getGateway().addToDialog(payload, dialog);
                getGateway().send(dialog);

            } else {
                // TODO send MAP returnError with unauthorizedRequestingNetwork (52) code instead of ReturnErrorUnexpected
                getGateway().sendRejectComponent(dialog, invokeId, ReturnErrorProblemType.ReturnErrorUnexpected);
            }

        } catch (ApplicationException e) {
            String msg = "Failed to handle PSI request";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }
}
