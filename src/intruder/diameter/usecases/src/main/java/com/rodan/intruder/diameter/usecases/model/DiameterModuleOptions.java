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

package com.rodan.intruder.diameter.usecases.model;

import com.rodan.intruder.diameter.usecases.model.dos.*;
import com.rodan.intruder.diameter.usecases.model.fraud.FraudAccessRestrictionOptions;
import com.rodan.intruder.diameter.usecases.model.fraud.FraudOdbOptions;
import com.rodan.intruder.diameter.usecases.model.infogathering.*;
import com.rodan.intruder.diameter.usecases.model.location.LocationIdrOptions;
import com.rodan.intruder.kernel.usecases.model.ModuleOptions;
import com.rodan.library.model.config.node.config.IntruderNodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.ValidationException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class DiameterModuleOptions extends ModuleOptions<IntruderNodeConfig> {
    final static Logger logger = LogManager.getLogger(DiameterModuleOptions.class);

    public DiameterModuleOptions(IntruderNodeConfig nodeConfig) {
        super(nodeConfig);
    }

    public static DiameterModuleOptions create(String moduleName, IntruderNodeConfig nodeConfig) throws ValidationException {
        var targetNetworkInfo = nodeConfig.getTargetNetwork();
        var targetSubscriberInfo = nodeConfig.getTargetSubscriberInfo();

        var options = switch (moduleName) {
            // Information Gathering
            case DiameterModuleConstants.SUBSCRIBER_INFO_NAME -> SubscriberInfoOptions.builder()
                    .nodeConfig(nodeConfig)
                    .destinationRealm(targetNetworkInfo.getRealm()).imsi(targetSubscriberInfo.getImsi())
                    .mcc(targetNetworkInfo.getMcc()).mnc(targetNetworkInfo.getMnc())
                    .build();
            case DiameterModuleConstants.NEW_AUTH_PARAM_NAME -> NewAuthParameterOptions.builder()
                    .nodeConfig(nodeConfig)
                    .destinationRealm(targetNetworkInfo.getRealm()).imsi(targetSubscriberInfo.getImsi())
                    .mcc(targetNetworkInfo.getMcc()).mnc(targetNetworkInfo.getMnc())
                    .build();
            case DiameterModuleConstants.HSS_ADDRESS_AIR_NAME -> HssAddressAirOptions.builder()
                    .nodeConfig(nodeConfig)
                    .destinationRealm(targetNetworkInfo.getRealm()).imsi(targetSubscriberInfo.getImsi())
                    .mcc(targetNetworkInfo.getMcc()).mnc(targetNetworkInfo.getMnc())
                    .build();
            case DiameterModuleConstants.MME_IDR_BF_NAME -> MmeIdrBruteforceOptions.builder()
                    .nodeConfig(nodeConfig)
                    .destinationRealm(targetNetworkInfo.getRealm()).imsi(targetSubscriberInfo.getImsi())
                    .build();
            case DiameterModuleConstants.MME_IDR_DISCOVERY_NAME -> MmeIdrDiscoveryOptions.builder()
                    .nodeConfig(nodeConfig)
                    .destinationRealm(targetNetworkInfo.getRealm()).mcc(targetNetworkInfo.getMcc()).mnc(targetNetworkInfo.getMnc())
                    .build();


            // Location Tracking
            case DiameterModuleConstants.LOCATION_IDR_NAME -> LocationIdrOptions.builder()
                    .nodeConfig(nodeConfig)
                    .destinationRealm(targetNetworkInfo.getRealm())
                    .targetMme(targetNetworkInfo.getMmeHostname()).imsi(targetSubscriberInfo.getImsi())
                    .build();

            // Denial of Service
            case DiameterModuleConstants.DOS_ALL_ULR_NAME -> DosAllUlrOptions.builder()
                    .nodeConfig(nodeConfig)
                    .destinationRealm(targetNetworkInfo.getRealm()).imsi(targetSubscriberInfo.getImsi())
                    .mcc(targetNetworkInfo.getMcc()).mnc(targetNetworkInfo.getMnc())
                    .build();
            case DiameterModuleConstants.DOS_MT_SMS_NAME -> DosMtSmsOptions.builder()
                    .nodeConfig(nodeConfig)
                    .destinationRealm(targetNetworkInfo.getRealm()).imsi(targetSubscriberInfo.getImsi())
                    .build();
            case DiameterModuleConstants.DOS_MO_ALL_NAME -> DosMoAllOptions.builder()
                    .nodeConfig(nodeConfig)
                    .destinationRealm(targetNetworkInfo.getRealm())
                    .targetMme(targetNetworkInfo.getMmeHostname()).imsi(targetSubscriberInfo.getImsi())
                    .build();
            case DiameterModuleConstants.DOS_MO_ALL_RAT_NAME -> DosMoAllRatOptions.builder()
                    .nodeConfig(nodeConfig)
                    .destinationRealm(targetNetworkInfo.getRealm()).targetMme(targetNetworkInfo.getMmeHostname())
                    .imsi(targetSubscriberInfo.getImsi())
                    .build();
            case DiameterModuleConstants.DOS_MT_ALL_NAME -> DosMtAllOptions.builder()
                    .nodeConfig(nodeConfig)
                    .destinationRealm(targetNetworkInfo.getRealm())
                    .imsi(targetSubscriberInfo.getImsi())
                    .build();
            case DiameterModuleConstants.DOS_MT_ALL_CLR_NAME -> DosMtAllClrOptions.builder()
                    .nodeConfig(nodeConfig)
                    .destinationRealm(targetNetworkInfo.getRealm()).targetMme(targetNetworkInfo.getMmeHostname())
                    .imsi(targetSubscriberInfo.getImsi())
                    .build();

            // Fraud
            case DiameterModuleConstants.FRAUD_ODB_NAME -> FraudOdbOptions.builder()
                    .nodeConfig(nodeConfig)
                    .destinationRealm(targetNetworkInfo.getRealm())
                    .targetMme(targetNetworkInfo.getMmeHostname()).imsi(targetSubscriberInfo.getImsi())
                    .build();
            case DiameterModuleConstants.FRAUD_ACCESS_RESTRICTION_NAME -> FraudAccessRestrictionOptions.builder()
                    .nodeConfig(nodeConfig)
                    .destinationRealm(targetNetworkInfo.getRealm())
                    .targetMme(targetNetworkInfo.getMmeHostname()).imsi(targetSubscriberInfo.getImsi())
                    .build();

            default -> {
                String msg = "Invalid Diameter module name: " + moduleName;
                logger.error(msg);
                throw ValidationException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).build();
            }
        };

        return options;
    }
}
