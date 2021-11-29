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

package com.rodan.intruder.main.cli.command;

import com.rodan.intruder.main.cli.menu.MenuTemplate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter @ToString @Builder
public class Command {
	private static final String LIST_COMMAND = "list";
	private static final String USE_COMMAND = "use";
	private static final String INFO_COMMAND = "info";
	private static final String CONNECTIONS_COMMAND = "connections";
	private static final String CONNECT_COMMAND = "connect";
	private static final String SHOW_COMMAND = "show";
	private static final String SHOW_OPTIONS_SUBCOMMAND = "options";
	private static final String SHOW_BYPASS_PAYLOADS_SUBCOMMAND = "bypass";
	private static final String SET_COMMAND = "set";
	private static final String SET_BYPASS_SUBCOMMAND = "bypass";
	private static final String UNSET_COMMAND = "unset";
	private static final String RUN_COMMAND = "run";
	private static final String HELP_COMMAND = "help";
	private static final String BACK_COMMAND = "back";
	private static final String EXIT_COMMAND = "exit";

	public enum CommandText {
		USE, EXIT, BACK, INVALID, SET, SET_BYPASS, UNSET, RUN, INFO, CONNECTIONS, CONNECT_SS7, CONNECT_DIAMETER,
		SHOW_OPTIONS, SHOW_BYPASS_PAYLOADS, HELP, LIST_SS7, LIST_DIAMETER
	}

	public static Command parse(String cmdText) {
		Command command = Command.builder().commandText(CommandText.INVALID).build();
		if (StringUtils.isNotBlank(cmdText)) {
			cmdText = cmdText.trim();
			String[] parts = cmdText.split("\\s+");
			String commandText = parts[0];

			switch (commandText) {
				case INFO_COMMAND:
					command = Command.builder().commandText(CommandText.INFO).build();
					break;

				case CONNECTIONS_COMMAND:
					command = Command.builder().commandText(CommandText.CONNECTIONS).build();
					break;

				case CONNECT_COMMAND:
					if (parts.length == 2 && ("SS7".equalsIgnoreCase(parts[1]) || "DIAMETER".equalsIgnoreCase(parts[1]))) {
						var cmd = "SS7".equalsIgnoreCase(parts[1]) ?
								CommandText.CONNECT_SS7 : CommandText.CONNECT_DIAMETER;
						command = Command.builder().commandText(cmd).build();

					} else {
						command = Command.builder().commandText(CommandText.INVALID).build();
					}
					break;

				case SHOW_COMMAND:
					if (parts.length == 2 && SHOW_OPTIONS_SUBCOMMAND.equals(parts[1])) {
						command = Command.builder().commandText(CommandText.SHOW_OPTIONS).build();

					} else if (parts.length == 2 && SHOW_BYPASS_PAYLOADS_SUBCOMMAND.equals(parts[1])) {
						command = Command.builder().commandText(CommandText.SHOW_BYPASS_PAYLOADS).build();

					}	else {
						command = Command.builder().commandText(CommandText.INVALID).build();
					}
					break;

				case SET_COMMAND:
					if (parts.length == 3) {
						// TODO only a singly entry is used. Create a NameValuePair class instead of using MAP
						Map<String, String> params = new HashMap<>();
						params.put(parts[1], parts[2]);
						var commandValue = (SET_BYPASS_SUBCOMMAND.equals(parts[1]))?
								CommandText.SET_BYPASS : CommandText.SET;
						command = Command.builder().commandText(commandValue).params(params).build();

					} else {
						command = Command.builder().commandText(CommandText.INVALID).build();
					}
					break;

				case UNSET_COMMAND:
					if (parts.length == 2) {
						// TODO only a singly entry is used. Create a NameValuePair class instead of using MAP
						Map<String, String> params = new HashMap<>();
						params.put(parts[1], "");
						command = Command.builder().commandText(CommandText.UNSET).params(params).build();

					} else {
						command = Command.builder().commandText(CommandText.INVALID).build();
					}
					break;

				case USE_COMMAND:
					if (parts.length == 2) {
						Map<String, String> params = new HashMap<>();
						params.put(MenuTemplate.MODULE_NAME_PARAM, parts[1]);
						command = Command.builder().commandText(CommandText.USE).params(params).build();

					} else {
						// TODO offer more error details for all menus
						command = Command.builder().commandText(CommandText.INVALID).build();
					}

					break;

				case RUN_COMMAND:
					command = Command.builder().commandText(CommandText.RUN).build();
					break;

				case LIST_COMMAND:
					if (parts.length == 2) {
						if (parts[1].equals("ss7"))
							command = Command.builder().commandText(CommandText.LIST_SS7).build();
						else if (parts[1].equals("diameter"))
							command = Command.builder().commandText(CommandText.LIST_DIAMETER).build();
						else
							command = Command.builder().commandText(CommandText.INVALID).build();
					} else {
						command = Command.builder().commandText(CommandText.INVALID).build();
					}

					break;

				case HELP_COMMAND:
					command = Command.builder().commandText(CommandText.HELP).build();
					break;

				case BACK_COMMAND:
					command = Command.builder().commandText(CommandText.BACK).build();
					break;

				case EXIT_COMMAND:
					command = Command.builder().commandText(CommandText.EXIT).build();
					break;
			}
		}
		return command;
	}
	
	private CommandText commandText;
	private Map<String, String> params;
	
	public Command() {
		
	}
	
	public Command(CommandText commandText, Map<String, String> params) {
		super();
		this.commandText = commandText;
		this.params = params;
	}

}
