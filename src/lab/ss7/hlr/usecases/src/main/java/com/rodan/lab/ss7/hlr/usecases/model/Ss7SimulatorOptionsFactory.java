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

package com.rodan.lab.ss7.hlr.usecases.model;

import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptionsFactory;
import com.rodan.lab.ss7.hlr.usecases.model.infogathering.NewAuthVectorSimOptions;
import com.rodan.lab.ss7.hlr.usecases.model.infogathering.SmsRoutingInfoSimOptions;
import com.rodan.lab.ss7.hlr.usecases.model.location.LocationAtiSimOptions;
import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorConstants;
import com.rodan.lab.ss7.hlr.usecases.model.infogathering.RoutingInfoSimOptions;
import com.rodan.library.model.config.node.config.LabNodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.ValidationException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Ayman ElSherif
 */
public class Ss7SimulatorOptionsFactory implements Ss7ModuleOptionsFactory<LabNodeConfig> {
    // TODO IMP TRX: implement factory as DiameterGatewayFactory
    final static Logger logger = LogManager.getLogger(Ss7SimulatorOptionsFactory.class);

    @Override
    public Ss7ModuleOptions create(String moduleName, LabNodeConfig nodeConfig) throws ValidationException {
        var targetNetworkInfo = nodeConfig.getTargetNetwork();
        var targetSubscriberInfo = nodeConfig.getTargetSubscriberInfo();

        var options = switch (moduleName) {
            // Information Gathering
            case Ss7SimulatorConstants.ROUTING_INFO_SIM_NAME -> RoutingInfoSimOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi()).msrn(targetSubscriberInfo.getMsrn())
                    .vmscGt(targetNetworkInfo.getMscGt())
                    .build();
            case Ss7SimulatorConstants.SMS_ROUTING_INFO_SIM_NAME -> SmsRoutingInfoSimOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi()).vmscGt(targetNetworkInfo.getMscGt())
                    .build();
            case Ss7SimulatorConstants.NEW_AUTH_VECTOR_SIM_NAME -> NewAuthVectorSimOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi())
                    .build();
            case Ss7SimulatorConstants.LOCATION_ATI_SIM_NAME -> LocationAtiSimOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imei(targetSubscriberInfo.getImei()).vlrGt(targetNetworkInfo.getVlrGt()).vmscGt(targetNetworkInfo.getMscGt())
                    .build();

            default -> {
                String msg = "Invalid SS7 module name: " + moduleName;
                logger.error(msg);
                throw ValidationException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).build();
            }
        };

        return options;
    }
}
