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
import com.rodan.intruder.kernel.usecases.model.ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7GatewayFactory;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.config.AbstractConfig;
import com.rodan.library.model.config.node.config.IntruderNodeConfig;
import com.rodan.library.model.error.ApplicationException;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.error.ValidationException;
import com.rodan.library.model.notification.NotificationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.*;

public abstract class MenuTemplate implements Menu {
	final static Logger logger = LogManager.getLogger(MenuTemplate.class);

	public static final String MODULE_NAME_PARAM = "moduleName";
	public static final String PROTOCOL_NAME_PARAM = "protocol";

	// Convert to private and access it from subclasses using protected methods
	// ie: protected members are accessible from all classes in the same package
	private Map<String, String> menuParams;
	@Setter @Getter private IntruderNodeConfig nodeConfig;
	@Setter(AccessLevel.PROTECTED) @Getter(AccessLevel.PROTECTED)
	private ModuleOptions moduleOptions;
//	@Setter(AccessLevel.PROTECTED) @Getter(AccessLevel.PROTECTED)
//	private Ss7PayloadOptions bypassOptions; // TODO SS7 IMP: Implement bypass
@Getter(AccessLevel.PROTECTED) private Ss7GatewayFactory ss7GatewayFactory;
	@Getter(AccessLevel.PROTECTED) private DiameterGatewayFactory diameterGatewayFactory;

	protected CliCommandProcessor commandProcessor; // TODO avoid using protected fields.

	public MenuTemplate(Map<String, String> menuParams, Ss7ModuleOptions moduleOptions, IntruderNodeConfig nodeConfig,
                        Ss7GatewayFactory ss7GatewayFactory, DiameterGatewayFactory diameterGatewayFactory) {
		super();
		this.menuParams = (menuParams != null)? menuParams : new HashMap<>();
		this.moduleOptions = moduleOptions;
		this.nodeConfig = nodeConfig;
		this.diameterGatewayFactory = Objects.requireNonNull(diameterGatewayFactory, "diameterConnectionFactory cannot be null");
		this.ss7GatewayFactory = Objects.requireNonNull(ss7GatewayFactory, "ss7ConnectionFactory cannot be null");
	}

	@Override
	public void start() {
		try {
			validateParameters();
			loadAvailableOptions();
			menuStartAction();

			String cursorPrefix = getCursorText();
			Command command;
			boolean finish;

			do {
				System.out.print(cursorPrefix);
				command = readCommand();
				var commandHandled = commandProcessor.process(command);
				if (!commandHandled) {
					displayError("Invalid command");
				}
				finish = (Command.CommandText.EXIT.equals(command.getCommandText())
						|| Command.CommandText.BACK.equals(command.getCommandText()));
			} while (!finish);
			
		} catch (ApplicationException e) {
			logger.error(e);
			displayError(e.getMessage());

		} catch (Exception e) {
			logger.error(e);
			displayError(e.getMessage());
		}
	}

	protected void menuStartAction() throws ApplicationException {
		// Do nothing
	}

	public abstract void validateParameters() throws ValidationException;
	
	public abstract void loadAvailableOptions() throws ApplicationException;

	protected abstract String getCursorText();
	
	public Command readCommand() {
		String cmdText = null;
		while (cmdText == null) {
			try {
				Scanner scanner = new Scanner(System.in);
				cmdText = scanner.nextLine();

			} catch (NoSuchElementException e) {
				logger.debug("No user input provided: " + e.getMessage());
			}
		}
		return Command.parse(cmdText);
	}
	
	public static void displaySuccess(String msg) {
		System.out.printf("[%s] %s\n\n", ConsoleColors.greenBold("*"), msg);
	}

	public static void displayProgress(String msg) {
		System.out.printf("%s\n", ConsoleColors.italic(msg));
	}

	public static void displayWarning(String msg) {
		System.out.printf("[%s] Warning: %s\n\n", ConsoleColors.yellowBold("-"), ConsoleColors.yellow(msg));
	}

	public static void displayError(String msg) {
		System.out.printf("[%s] Error: %s\n\n", ConsoleColors.red("-"), ConsoleColors.red(msg));
	}

	public static void displayNotification(String msg, NotificationType type) {
		switch (type) {
			case SUCCESS -> displaySuccess(msg);
			case PROGRESS -> displayProgress(msg);
			case WARNING -> displayWarning(msg);
			case FAILURE -> displayError(msg);
		}
	}

	private void displaySs7Modules() {
		displayModules(ModuleMenu.SS7_MODULES);
	}

	private void displayDiameterModules() {
		displayModules(ModuleMenu.DIAMETER_MODULES);
	}

	private void displayModules(List<Class<?>> modules) {
		System.out.print("\nAvailable Modules: \n");
		System.out.print("=================\n");

		System.out.printf("%s %-4s %-35s %-11s %-50s\n", Cli.PREFIX_INDENTATION, "#", "Module", "Rank", "Description");
		System.out.printf("%s %-4s %-35s %-11s %-50s\n", Cli.PREFIX_INDENTATION, "-", "------", "----", "-----------");

		int index = 1;
		for (var module : modules) {
			var name = module.getAnnotation(Module.class).name();
			var brief = module.getAnnotation(Module.class).brief();
			var rank = module.getAnnotation(Module.class).rank();
			System.out.printf("%s %-4s %-35s %-11s %-50s\n", Cli.PREFIX_INDENTATION, index, name, rank, ConsoleColors.italic(brief));
			index++;
		}

		System.out.print("\n\n");
	}

	public abstract void displayHelp();

	public void connectDiameter() throws SystemException {
		var gateway = getDiameterGatewayFactory().makeGateway(getNodeConfig());
		gateway.addNotificationListener(MenuTemplate::displayNotification);
		if (gateway.isConnected())
			displayNotification("Node is already connected to Diameter network", NotificationType.FAILURE);
		else
			gateway.connect();
	}

	protected void addParam(String key, String value) {
		Objects.requireNonNull(key, "Parameter key cannot be null");
		Objects.requireNonNull(value, "Parameter value cannot be null");
		menuParams.put(key, value);
	}

	protected void displayConnection() throws SystemException {
//		System.out.print(ConsoleColors.bold("\nConnection options:\n\n"));
//		System.out.printf("%s%-20s  %-15s  %-10s  %-15s\n", Cli.PREFIX_INDENTATION, "Name", "Current Setting", "Required", "Description");
//		System.out.printf("%s%-20s  %-15s  %-10s  %-15s\n", Cli.PREFIX_INDENTATION, "----", "---------------", "--------", "-----------");
//
//		var associationInfo = nodeConfig.getAssociationInfo();
//
//		var fields = Arrays.stream(AssociationInfo.class.getDeclaredFields())
//				.sorted(Comparator.comparing(Field::getName)).toArray(Field[]::new);
//		for (Field field : fields) {
//			displayConfigEntry(associationInfo, field, false);
//		}
//
//		// TODO TRX: STP: fix to display node info
//		System.out.print(ConsoleColors.bold("\n\nCurrent Connection:\n\n"));
//		System.out.printf("%s%-12s  %-14s  %-8s\n", Cli.PREFIX_INDENTATION, "ID", "Peer Address", "Status");
//		System.out.printf("%s%-12s  %-14s  %-8s\n", Cli.PREFIX_INDENTATION, "--", "------------", "------");
//		var connections = Ss7Connection.getConnections();
//		for (var entry : connections.entrySet()) {
//			var id = entry.getKey();
//			// Quick fix to remove (-ve) sign TODO find a better way
//			if (id.startsWith("-"))
//				id = id.substring(1);
//			var peer = entry.getValue().getPeerNodes();
//			var status = entry.getValue().isConnected() ? ConsoleColors.greenBold("Connected") :
//					ConsoleColors.redBold("Disconnected");
//			System.out.printf("%s%-12s  %-14s  %-8s\n", Cli.PREFIX_INDENTATION, id, peer, status);
//		}
//		System.out.printf("\n");
	}

	protected void displayConfigEntry(AbstractConfig config, Field field, boolean isBypassConfig) throws SystemException {
		if (AbstractConfig.isOptionField(field) && AbstractConfig.isFieldDisplayable(field)) {
			var displayName = AbstractConfig.getFieldDisplayName(field);
			if (isBypassConfig) {
				displayName = "b_" + displayName;
			}
			var description = AbstractConfig.getFieldDescription(field);
			var isMandatory = AbstractConfig.isFieldMandatory(field);
			var value = config.getFieldValue(config, field);

			System.out.printf("%s%-20s  %-15s  %-10s  %-15s\n", Cli.PREFIX_INDENTATION, displayName, value,
					isMandatory ? "yes" : "no", ConsoleColors.italic(description));
		}
	}

	public void connectSs7() throws SystemException {
		var gateway = getSs7GatewayFactory().makeGateway(getNodeConfig());
		gateway.addNotificationListener(MenuTemplate::displayNotification);
		if (gateway.isConnected())
			displayNotification("Node is already connected to SS7 network", NotificationType.FAILURE);
		else
			gateway.connect();
	}

	protected String getParam(String key) {
		return menuParams.get(key);
	}

	protected class GlobalCommandProcessor extends CliCommandProcessor {
		public GlobalCommandProcessor(CliCommandProcessor next) {
			super(next);
		}

		public boolean handle(Command command) throws SystemException {
			boolean commandHandled = false;
			switch (command.getCommandText()) {
				case LIST_SS7 -> {
					displaySs7Modules();
					commandHandled = true;
				}

				case LIST_DIAMETER -> {
					displayDiameterModules();
					commandHandled = true;
				}

				case CONNECT_SS7 -> {
					connectSs7();
					commandHandled = true;
				}

				case CONNECT_DIAMETER -> {
					connectDiameter();
					commandHandled = true;
				}

				case CONNECTIONS -> {
					displayConnection();
					commandHandled = true;
				}

//				case SET -> {
//					for (Map.Entry<String, String> entry : command.getParams().entrySet()) {
//						var isSet = getNodeConfig().trySetFieldByName(entry.getKey(), entry.getValue());
//						if (isSet) {
//							commandHandled = true;
//
//						} else {
//							displayError("Invalid options!");
//							commandHandled = false;
//						}
//					}
//				}

				case USE -> {
					try {
						Menu useMenu = ModuleMenu.builder()
								.nodeConfig(nodeConfig).menuParams(command.getParams())
								.ss7GatewayFactory(ss7GatewayFactory)
								.diameterGatewayFactory(diameterGatewayFactory)
								.build();
						useMenu.start();


					} catch (ApplicationException e) {
						displayError(e.getMessage());
					}
					commandHandled = true;
				}

				case BACK -> {
					commandHandled = true;
				}

				case HELP -> {
					displayHelp();
					commandHandled = true;
				}

				case EXIT -> {
					commandHandled = true;
					System.exit(0);
				}
			}

			return commandHandled;
		}
	}
}
