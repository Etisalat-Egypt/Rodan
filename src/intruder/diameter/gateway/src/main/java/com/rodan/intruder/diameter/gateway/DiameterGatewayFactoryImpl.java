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

package com.rodan.intruder.diameter.gateway;

import com.rodan.intruder.diameter.usecases.port.DiameterGatewayFactory;
import com.rodan.library.model.config.node.config.IntruderNodeConfig;
import com.rodan.library.model.error.SystemException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class DiameterGatewayFactoryImpl implements DiameterGatewayFactory {
    final static Logger logger = LogManager.getLogger(DiameterGatewayFactoryImpl.class);

    private static DiameterGatewayImpl instance;

    @Override
    public DiameterGatewayImpl makeGateway(IntruderNodeConfig nodeConfig) throws SystemException {
        logger.debug("Getting Diameter gateway...");
        if (instance == null) {
            logger.debug("Creating a new Diameter gateway...");
            instance = DiameterGatewayImpl.builder().nodeConfig(nodeConfig).build();
        }

        return instance;
    }
}
