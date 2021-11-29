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

package com.rodan.intruder.main.cli;

import com.rodan.intruder.diameter.gateway.DiameterGatewayFactoryImpl;
import com.rodan.intruder.ss7.gateway.Ss7GatewayFactoryImpl;
import com.rodan.library.model.Constants;
import com.rodan.library.model.error.ApplicationException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Main {
    final static Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            var ss7GatewayFactory = new Ss7GatewayFactoryImpl();
            var diameterGatewayFactory = new DiameterGatewayFactoryImpl();

            var cli = Cli.builder().args(args)
                    .ss7GatewayFactory(ss7GatewayFactory)
                    .diameterGatewayFactory(diameterGatewayFactory)
                    .build();
            cli.start();
            System.exit(0);

        } catch (ApplicationException e) {
            logger.error("Exception thrown while running " + Constants.APP_NAME, e);
        }
    }
}
