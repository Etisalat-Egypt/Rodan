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

package com.rodan.lab.ss7.stp.main.cli;

import com.rodan.intruder.main.cli.executor.AsyncModuleExecutor;
import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorConstants;
import com.rodan.lab.ss7.stp.usecases.model.Ss7SimulatorOptionsFactory;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.ss7.gateway.Ss7GatewayFactoryImpl;
import com.rodan.intruder.ss7.usecases.port.Ss7GatewayFactory;
import com.rodan.lab.ss7.stp.usecases.simulation.StpSimulator;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.config.node.config.StpNodeConfig;
import com.rodan.library.model.error.ApplicationException;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.util.Util;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ayman ElSherif
 */
public class Main {
    final static Logger logger = LogManager.getLogger(Main.class);

    public static final List<Class<?>> SS7_SIMULATORS = Util.getAvailableModules("com.rodan.lab.ss7.stp.usecases.simulation");

    private String configPath;
    @Setter @Getter private StpNodeConfig nodeConfig;
    private Ss7GatewayFactory ss7GatewayFactory;

    private Map<String, SignalingModule> ss7SimulatorsMap;

    @Builder
    public Main(String configPath, Ss7GatewayFactory ss7GatewayFactory) {
        this.configPath = configPath;
        this.ss7GatewayFactory = ss7GatewayFactory;
        this.ss7SimulatorsMap = new HashMap<>();
    }

    public static void main(String[] args) {
        try {
            var configPath = (args != null && args.length > 0) ? args[0] : Constants.DEFAULT_CONFIG_FILE_PATH;
            var ss7GatewayFactory = new Ss7GatewayFactoryImpl();
            var app = Main.builder()
                    .configPath(configPath).ss7GatewayFactory(ss7GatewayFactory)
                    .build();
            app.loadAvailableOptions();
            app.start();

        } catch (ApplicationException e) {
            logger.error("Exception thrown while running " + Constants.APP_NAME, e);
        }
    }

    private void loadAvailableOptions() throws ApplicationException {
        var nodeConfig = Util.<StpNodeConfig>loadConfigs(configPath);
        Util.cleanupStackConfigFiles(nodeConfig);
        this.setNodeConfig(nodeConfig);

        var ss7Factory = new Ss7SimulatorOptionsFactory();
        var ss7Gateway = ss7GatewayFactory.makeGateway(nodeConfig);

        for (var simulatorClass : SS7_SIMULATORS) {
            var simulatorName = simulatorClass.getAnnotation(Module.class).name();
            var moduleOps = ss7Factory.create(simulatorName, nodeConfig);
            SignalingModule simulator = switch (simulatorName) {
                case Ss7SimulatorConstants.STP_SIM_NAME -> StpSimulator.builder()
                        .moduleOptions(moduleOps)
                        .gateway(ss7Gateway)
                        .build();

                default -> {
                    String msg = "Invalid SS7 simulator name: " + simulatorName;
                    logger.error(msg);
                    throw SystemException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).build();
                }

            };

            ss7SimulatorsMap.put(simulatorName, simulator);
        }
    }

    public void start() throws ApplicationException {
        try {
            logger.info("Starting STP...");
            System.out.println("Starting STP...");
            appStartAction();

            var executor = new AsyncModuleExecutor();
            for (var entry : ss7SimulatorsMap.entrySet()) {
                executor.execute(entry.getValue());
                Thread.sleep(2000);
            }

            logger.info("STP is ready!");
            System.out.println("STP is ready!");


        } catch (InterruptedException e) {
            String msg = "[-] Error: " + e.getMessage();
            logger.error(msg, e);
        }
    }

    private void appStartAction() throws ApplicationException {
        var ss7Gateway = ss7GatewayFactory.makeGateway(getNodeConfig());
        if (!ss7Gateway.isConnected()) {
            ss7Gateway.connect();
        }
    }
}
