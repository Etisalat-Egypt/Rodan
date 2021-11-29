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

package com.rodan.intruder.main.cli.menu;

import com.rodan.intruder.diameter.usecases.port.DiameterGatewayFactory;
import com.rodan.intruder.main.cli.Cli;
import com.rodan.intruder.main.cli.command.Command;
import com.rodan.intruder.main.cli.command.processor.CliCommandProcessor;
import com.rodan.intruder.main.cli.util.ConsoleColors;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7GatewayFactory;
import com.rodan.library.model.Constants;
import com.rodan.library.model.config.node.config.IntruderNodeConfig;
import com.rodan.library.model.error.ApplicationException;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.util.Util;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Map;

public class
MainMenu extends MenuTemplate {
	final static Logger logger = LogManager.getLogger(MainMenu.class);
	
	private String configPath;

	@Builder
	public MainMenu(Map<String, String> menuParams, Ss7ModuleOptions moduleOptions,
					IntruderNodeConfig nodeConfig, String configPath, Ss7GatewayFactory ss7GatewayFactory,
					DiameterGatewayFactory diameterGatewayFactory) {
		super(menuParams, moduleOptions, nodeConfig, ss7GatewayFactory, diameterGatewayFactory);
		this.configPath = configPath;
		var globalCommandProcessor = new GlobalCommandProcessor(null);
		this.commandProcessor = new MainCommandProcessor(globalCommandProcessor);
	}

	@Override
	public void validateParameters() {
	}
	
	@Override
	public void loadAvailableOptions() throws ApplicationException {
		var nodeConfig = Util.<IntruderNodeConfig>loadConfigs(configPath);
		Util.cleanupStackConfigFiles(nodeConfig);
		this.setNodeConfig(nodeConfig);
	}

	@Override
	protected void menuStartAction() throws ApplicationException {
//		connectSs7();
	}



	@Override
	protected String getCursorText() {
		return ConsoleColors.underline(Constants.APP_NAME) + " > ";
	}
	
	@Override
	public void displayHelp() {
		String[] availableCommands = {"list (ss7|diameter)", "connect (ss7|diameter)", "use", "info", "show options",
				"set", "unset", "run", "help", "exit"};
		System.out.print("\nAvailable Commands:\n");
		System.out.print("==================\n");
		for (var command : availableCommands) {
			System.out.print(Cli.PREFIX_INDENTATION + command + "\n");
		}
		System.out.print("\n");
	}
	
	public class MainCommandProcessor extends CliCommandProcessor {
		public MainCommandProcessor(CliCommandProcessor next) {
			super(next);
		}

		public boolean handle(Command command) throws SystemException {
			boolean commandHandled = false;
			switch (command.getCommandText()) {
				case BACK -> {
					command.setCommandText(Command.CommandText.INVALID); // Quick fix to disable exit using back
					displayError("Invalid command");
					commandHandled = true;
				}
			}

			return commandHandled;
		}
	}
}
