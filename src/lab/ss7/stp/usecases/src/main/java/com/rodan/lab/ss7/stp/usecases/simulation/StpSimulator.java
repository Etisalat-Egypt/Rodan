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

package com.rodan.lab.ss7.stp.usecases.simulation;

import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorTemplate;
import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorConstants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.SystemException;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Ayman ElSherif
 */
@Module(name = Ss7SimulatorConstants.STP_SIM_NAME)
public class StpSimulator extends Ss7SimulatorTemplate implements SignalingModule {

    final static Logger logger = LogManager.getLogger(StpSimulator.class);

    @Builder
    public StpSimulator(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("No payload to generate for STP");
    }

    @Override
    protected void addServiceListener() throws SystemException {
        logger.debug("No service listeners for STP");
        // Add SRI-SM to support SMS home routing
    }

    @Override
    protected void cleanup() throws SystemException {
        super.cleanup();
    }
}
