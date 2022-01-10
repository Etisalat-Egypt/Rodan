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

import com.rodan.intruder.diameter.usecases.port.DiameterGatewayFactory;
import com.rodan.intruder.main.cli.menu.MainMenu;
import com.rodan.intruder.main.cli.util.ConsoleColors;
import com.rodan.intruder.ss7.usecases.port.Ss7GatewayFactory;
import com.rodan.library.model.Constants;
import com.rodan.library.model.error.ApplicationException;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class Cli {
	public static final String PREFIX_INDENTATION = "   "; // 3 spaces

	final static Logger logger = LogManager.getLogger(Cli.class);

	private String[] args;
	private Ss7GatewayFactory ss7GatewayFactory;
	private DiameterGatewayFactory diameterGatewayFactory;

	@Builder
	public Cli(String[] args, Ss7GatewayFactory ss7GatewayFactory, DiameterGatewayFactory diameterGatewayFactory) {
		this.args = args;
		this.ss7GatewayFactory = ss7GatewayFactory;
		this.diameterGatewayFactory = diameterGatewayFactory;
	}

	public void start() throws ApplicationException {
		Cli.disableWarning();
		System.out.print(ConsoleColors.clear());
		this.printBanner();

		var configPath = (args != null && args.length > 0) ? args[0] : Constants.DEFAULT_CONFIG_FILE_PATH;
		MainMenu.builder().configPath(configPath)
				.ss7GatewayFactory(ss7GatewayFactory).diameterGatewayFactory(diameterGatewayFactory)
				.build().start();
	}

	public void printBanner() {
		// ref: http://www.asciiworld.com/-Winged-.html
		var logo = new String[] { "                                                ,d888*`             ",
				"                                              ,d888`                ",
				"                                            ,d888`                  ",
				"                                           ,d88`                    ",
				"                                         ,d88`                      ",
				"                                        ,d8`                        ",
				"                                      ,d8*                 ..d**    ",
				"                                    ,d88*             ..d**`        ",
				"                                  ,d88`         ..d8*`              ",
				"                                ,d888`    ..d8P*`                   ",
				"                        .     ,d8888*8888*`                         ",
				"                      ,*     ,88888*8P*                             ",
				"                    ,*      d888888*8b.                             ",
				"                  ,P       dP  *888.*888b.                          ",
				"                ,8*        8    *888  `**88888b.                    ",
				"              ,dP                *88           *88b.                ",
				"             d8`                  *8b               *8b.            ",
				"           ,d8`                    *8.                  *88b.       ",
				"          d8P                       88.                    *88b     ",
				"        ,88P                        888                             ",
				"       d888*       .d88P            888                             ",
				"      d8888b..d888888*              888		", "    ,888888888888888b.              888        ",
				"   ,8*;88888P*****788888888ba.      888        ", "  ,8;,8888*        `88888*          d88*       ",
				"  )8e888*          ,88888be.        888        ", " ,d888`           ,8888888***     d888         ",
				",d88P`           ,8888888Pb.     d888`         ", "888*            ,88888888**   .d8888*          ",
				"`88            ,888888888    .d88888b          ", " `P           ,8888888888bd888888*             ",
				"              d888888888888d888*               ", "              8888888888888888b.               ",
				"              88*. *88888888888b.        .db   ",
				"              `888b.`8888888888888b. .d8888P                        ",
				"               **88b.`*8888888888888888888888b...                   ",
				"                *888b.`*8888888888P***7888888888888e.               ",
				"                 88888b.`********.d8888b**`88888P*                  ",
				"                 `888888b     .d88888888888**`8888.				 ",
				"                  )888888.   d888888888888P   `8888888b.			 ",
				"                 ,88888*    d88888888888**`    `8888b				 ",
				"                ,8888*    .8888888888P`          `888b.			 ",
				"               ,888*      888888888b...            `888P88b.	 	 ",
				"      .db.   ,d88*        88888888888888b          `8888			 ",
				"  ,d888888b.8888`         `*888888888888888888P`   `888b.			 ",
				" /*****8888b**`              `***8888P*``8888`       `8888b.		 ",
				"      /**88`                 .ed8b..  .d888P`            `88888	 ",
				"                           d8**888888888P*               `88b		 ",
				"                          (*``,d8888***`                    `88	 ",
				"                             (*`                             `88	 ",
				"                                                              88	 ",
				"                                                              88	 ",
				"                                                             `8	 ",
				"                                                             d8	 " };

		var header = new String[] { "01010010 01101111 01100100 01100001 01101110" };
		// ref: http://patorjk.com/software/taag/#p=testall&f=Graffiti&t=Rodan
		var appName = new String[] { "             __                             ",
				"             )_) _   _ ) _   _              ", "            / \\ (_) (_( (_( ) )            ",
				"                                            " };

		var footer = new String[] { "                   Rodan                    ",
				"                    R.X                     ", "                     X.o                    ",
				"                    d.X                     ", "                     X.a                    ",
				"                    n.X                     ", "                     X                      " };

		var versionString = new String[] { "     Rodan Exploitation Framework v1.2.2!     " };

		int logoIndex = 0, headerIndex = 0, appNameIndex = 0, footerIndex = 0, versionStringIndex = 0;
		// header 21
		// appName 22 to 24
		// footerIndex 26 to 32
		// versionStringIndex 33
		for (logoIndex = 0; logoIndex < logo.length; logoIndex++) {
			System.out.printf("%s", ConsoleColors.greenBold(logo[logoIndex]));
			if (logoIndex == 21) {
				System.out.printf("%s", ConsoleColors.redBold(header[headerIndex]));
				headerIndex++;
			}

			if (logoIndex >= 22 && logoIndex <= 24) {
				System.out.printf("%s", ConsoleColors.cyanBold(appName[appNameIndex]));
				appNameIndex++;
			}

			if (logoIndex >= 26 && logoIndex <= 32) {
				System.out.printf("%s", footer[footerIndex]);
				footerIndex++;
			}

			if (logoIndex == 33) {
				System.out.printf("%s", ConsoleColors.yellowBold(versionString[versionStringIndex]));
				versionStringIndex++;
			}

			System.out.print("\n");
		}
	}

	public static void disableWarning() {
		// TODO use a better approach
		System.err.close();
		System.setErr(System.out);
	}
}
