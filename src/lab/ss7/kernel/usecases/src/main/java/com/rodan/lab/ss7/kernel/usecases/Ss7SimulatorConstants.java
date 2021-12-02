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

package com.rodan.lab.ss7.kernel.usecases;

import com.rodan.intruder.ss7.usecases.model.Ss7ModuleConstants;

/**
 * @author Ayman ElSherif
 */
public interface Ss7SimulatorConstants {
    String SIMULATOR_NAME_PREFIX = "simulate/";
    String ROUTING_INFO_SIM_NAME = SIMULATOR_NAME_PREFIX + Ss7ModuleConstants.ROUTING_INFO_NAME;
    String SMS_ROUTING_INFO_SIM_NAME = SIMULATOR_NAME_PREFIX + Ss7ModuleConstants.SMS_ROUTING_INFO_NAME;
    String LOCATION_PSI_SIM_NAME = SIMULATOR_NAME_PREFIX + Ss7ModuleConstants.LOCATION_PSI_NAME;
    String LOCATION_ATI_SIM_NAME = SIMULATOR_NAME_PREFIX + Ss7ModuleConstants.LOCATION_ATI_NAME;
    String NEW_AUTH_VECTOR_SIM_NAME = SIMULATOR_NAME_PREFIX + Ss7ModuleConstants.NEW_AUTH_VECTOR_NAME;

    String SIMSI_SIM_NAME = SIMULATOR_NAME_PREFIX + Ss7ModuleConstants.SEND_IMSI_NAME;
    String SRI_GPRS_SIM_NAME = SIMULATOR_NAME_PREFIX + Ss7ModuleConstants.GPRS_ROUTING_INFO_NAME;
    String SRI_LCS_SIM_NAME = SIMULATOR_NAME_PREFIX + Ss7ModuleConstants.LCS_ROUTING_INFO_NAME;
    String SRI_SM_SIM_NAME = SIMULATOR_NAME_PREFIX + Ss7ModuleConstants.SMS_ROUTING_INFO_NAME;
    String PSL_SIM_NAME = SIMULATOR_NAME_PREFIX + Ss7ModuleConstants.LOCATION_PSL_NAME;
    String UL_SIM_NAME = SIMULATOR_NAME_PREFIX + Ss7ModuleConstants.SMS_INTERCEPTION_NAME;
    String STP_SIM_NAME = SIMULATOR_NAME_PREFIX + "stp";
}
