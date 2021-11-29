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

package com.rodan.intruder.ss7.usecases.model;

import com.rodan.intruder.ss7.usecases.model.dos.DosCallBarringOptions;
import com.rodan.intruder.ss7.usecases.model.dos.DosClOptions;
import com.rodan.intruder.ss7.usecases.model.dos.DosDsdOptions;
import com.rodan.intruder.ss7.usecases.model.dos.DosPurgeOptions;
import com.rodan.intruder.ss7.usecases.model.fraud.SmsFraudOptions;
import com.rodan.intruder.ss7.usecases.model.infogathering.*;
import com.rodan.intruder.ss7.usecases.model.interception.MoCallInterceptionMsrnOptions;
import com.rodan.intruder.ss7.usecases.model.interception.MoCallInterceptionOptions;
import com.rodan.intruder.ss7.usecases.model.interception.SmsInterceptionOptions;
import com.rodan.intruder.ss7.usecases.model.location.LocationAtiOptions;
import com.rodan.intruder.ss7.usecases.model.location.LocationPsiOptions;
import com.rodan.intruder.ss7.usecases.model.location.LocationPslOptions;
import com.rodan.library.model.config.node.config.IntruderNodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.ValidationException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Ss7ModuleOptionsFactoryImpl implements Ss7ModuleOptionsFactory<IntruderNodeConfig> {
    // TODO IMP TRX: implement factory as DiameterGatewayFactory
    final static Logger logger = LogManager.getLogger(Ss7ModuleOptionsFactoryImpl.class);

    @Override
    public Ss7ModuleOptions create(String moduleName, IntruderNodeConfig nodeConfig) throws ValidationException {
        var targetNetworkInfo = nodeConfig.getTargetNetwork();
        var targetSubscriberInfo = nodeConfig.getTargetSubscriberInfo();

        var options = switch (moduleName) {
            // Information Gathering
            case Ss7ModuleConstants.ROUTING_INFO_NAME -> RoutingInfoOptions.builder()
                    .nodeConfig(nodeConfig)
                    .msisdn(targetSubscriberInfo.getMsisdn()).targetHlrGt(targetNetworkInfo.getHlrGt())
                    .build();
            case Ss7ModuleConstants.SMS_ROUTING_INFO_NAME -> SmsRoutingInfoOptions.builder()
                    .nodeConfig(nodeConfig)
                    .msisdn(targetSubscriberInfo.getMsisdn()).imsi(targetSubscriberInfo.getImsi())
                    .targetHlrGt(targetNetworkInfo.getHlrGt())
                    .cc(targetNetworkInfo.getCc()).ndc(targetNetworkInfo.getNdc())
                    .mcc(targetNetworkInfo.getMcc()).mnc(targetNetworkInfo.getMnc())
                    .build();
            case Ss7ModuleConstants.LCS_ROUTING_INFO_NAME -> LcsRoutingInfoOptions.builder()
                    .nodeConfig(nodeConfig)
                    .msisdn(targetSubscriberInfo.getMsisdn()).imsi(targetSubscriberInfo.getImsi())
                    .targetHlrGt(targetNetworkInfo.getHlrGt())
                    .build();
            case Ss7ModuleConstants.GPRS_ROUTING_INFO_NAME -> GprsRoutingInfoOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi()).msisdn(targetSubscriberInfo.getMsisdn())
                    .targetHlrGt(targetNetworkInfo.getHlrGt())
                    .build();
            case Ss7ModuleConstants.SEND_IMSI_NAME -> SendImsiOptions.builder()
                    .nodeConfig(nodeConfig)
                    .msisdn(targetSubscriberInfo.getMsisdn()).targetHlrGt(targetNetworkInfo.getHlrGt())
                    .build();
            case Ss7ModuleConstants.NEW_AUTH_VECTOR_NAME -> NewAuthVectorOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi()).msisdn(targetSubscriberInfo.getMsisdn())
                    .targetHlrGt(targetNetworkInfo.getHlrGt())
                    .build();
            case Ss7ModuleConstants.CURRENT_AUTH_VECTOR_VECTOR_NAME -> CurrentAuthVectorOptions.builder()
                    .nodeConfig(nodeConfig)
                    .tmsi(targetSubscriberInfo.getTmsi())
                    .targetVlrGt(targetNetworkInfo.getVlrGt())
                    .build();
            case Ss7ModuleConstants.HLR_ADDRESS_SM_NAME -> HlrAddressSmOptions.builder()
                    .nodeConfig(nodeConfig)
                    .msisdn(targetSubscriberInfo.getMsisdn())
                    .build();
            case Ss7ModuleConstants.CAMEL_INFO_NAME -> CamelInfoOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi())
                    .cc(targetNetworkInfo.getCc()).ndc(targetNetworkInfo.getNdc())
                    .mcc(targetNetworkInfo.getMcc()).mnc(targetNetworkInfo.getMnc())
                    .build();
            case Ss7ModuleConstants.VLR_BF_NAME -> VlrBruteforceOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi())
                    .build();

            // Location Tracking
            case Ss7ModuleConstants.LOCATION_ATI_NAME -> LocationAtiOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi()).msisdn(targetSubscriberInfo.getMsisdn())
                    .targetHlrGt(targetNetworkInfo.getHlrGt())
                    .build();
            case Ss7ModuleConstants.LOCATION_PSI_NAME -> LocationPsiOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi()).targetVlrGt(targetNetworkInfo.getVlrGt())
                    .build();
            case Ss7ModuleConstants.LOCATION_PSL_NAME -> LocationPslOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi()).msisdn(targetSubscriberInfo.getMsisdn())
                    .targetMscGt(targetNetworkInfo.getMscGt())
                    .build();

            // Call and SMS Interception
            case Ss7ModuleConstants.SMS_INTERCEPTION_NAME -> SmsInterceptionOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi()).hlrGt(targetNetworkInfo.getHlrGt())
                    .currentMscGt(targetNetworkInfo.getMscGt()).currentVlrGt(targetNetworkInfo.getVlrGt())
                    .cc(targetNetworkInfo.getCc()).ndc(targetNetworkInfo.getNdc())
                    .mcc(targetNetworkInfo.getMcc()).mnc(targetNetworkInfo.getMnc())
                    .build();
            case Ss7ModuleConstants.MO_CALL_INTERCEPTION_NAME -> MoCallInterceptionOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi())
                    .gsmScf(targetNetworkInfo.getGsmScfGt())
                    .targetHlrGt(targetNetworkInfo.getHlrGt())
                    .targetVlrGt(targetNetworkInfo.getVlrGt())
                    .build();
            case Ss7ModuleConstants.MO_CALL_INTERCEPTION_MSRN_NAME -> MoCallInterceptionMsrnOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi())
                    .cc(targetNetworkInfo.getCc()).ndc(targetNetworkInfo.getNdc())
                    .mcc(targetNetworkInfo.getMcc()).mnc(targetNetworkInfo.getMnc())
                    .build();

            // Fraud
            case Ss7ModuleConstants.SMS_FRAUD_NAME -> SmsFraudOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi()).targetMscGt(targetNetworkInfo.getMscGt())
                    .build();

            // Denial of Service
            case Ss7ModuleConstants.DOS_CL_NAME -> DosClOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi()).targetVlrGt(targetNetworkInfo.getVlrGt())
                    .targetHlrGt(targetNetworkInfo.getHlrGt())
                    .build();
            case Ss7ModuleConstants.DOS_DSD_NAME -> DosDsdOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi()).targetVlrGt(targetNetworkInfo.getVlrGt())
                    .targetHlrGt(targetNetworkInfo.getHlrGt())
                    .build();
            case Ss7ModuleConstants.DOS_PURGE_NAME -> DosPurgeOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi()).msisdn(targetSubscriberInfo.getMsisdn())
                    .targetHlrGt(targetNetworkInfo.getHlrGt()).targetVlrGt(targetNetworkInfo.getVlrGt())
                    .build();
            case Ss7ModuleConstants.DOS_CALL_BARRING_NAME -> DosCallBarringOptions.builder()
                    .nodeConfig(nodeConfig)
                    .imsi(targetSubscriberInfo.getImsi()).msisdn(targetSubscriberInfo.getMsisdn())
                    .targetVlrGt(targetNetworkInfo.getVlrGt()).targetHlrGt(targetNetworkInfo.getHlrGt())
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
