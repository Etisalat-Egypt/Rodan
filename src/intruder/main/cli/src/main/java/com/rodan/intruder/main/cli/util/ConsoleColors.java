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

package com.rodan.intruder.main.cli.util;

public class ConsoleColors {
	// Ref: https://en.wikipedia.org/wiki/ANSI_escape_code#Escape_sequences

	// Reset
	public static final String RESET = "\033[0m"; // Text Reset
	
//	echo -e "\e[1mbold\e[0m"
//	echo -e "\e[3mitalic\e[0m"
//	echo -e "\e[4munderline\e[0m"
//	echo -e "\e[9mstrikethrough\e[0m"
//	echo -e "\e[31mHello World\e[0m"
//	echo -e "\x1B[31mHello World\e[0m"
	public static final String CLEAR = "\033[H\033[2J";
	public static final String BOLD = "\033[1m"; // Bold
	public static final String ITALIC = "\033[3m"; // Italic
	public static final String UNDERLINE = "\033[4m"; // Underline
	public static final String STRIKE_THROUGH = "\033[9m"; // Strike through

	// Regular Colors
	public static final String BLACK = "\033[0;30m"; // BLACK
	public static final String RED = "\033[0;31m"; // RED
	public static final String GREEN = "\033[0;32m"; // GREEN
	public static final String YELLOW = "\033[0;33m"; // YELLOW
	public static final String BLUE = "\033[0;34m"; // BLUE
	public static final String PURPLE = "\033[0;35m"; // PURPLE
	public static final String CYAN = "\033[0;36m"; // CYAN
	public static final String WHITE = "\033[0;37m"; // WHITE
	public static final String ORANGE = "\033[38;5;208m"; // ORANGE

	// Bold
	public static final String BLACK_BOLD = "\033[1;30m"; // BLACK
	public static final String RED_BOLD = "\033[1;31m"; // RED
	public static final String GREEN_BOLD = "\033[1;32m"; // GREEN
	public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
	public static final String YELLOW_ITALIC = "\033[3;33m"; // YELLOW
	public static final String BLUE_BOLD = "\033[1;34m"; // BLUE
	public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
	public static final String CYAN_BOLD = "\033[1;36m"; // CYAN
	public static final String WHITE_BOLD = "\033[1;37m"; // WHITE
	public static final String ORANGE_BOLD = "\033[38;5;208m"; // ORANGE

	// Underline
	public static final String BLACK_UNDERLINED = "\033[4;30m"; // BLACK
	public static final String RED_UNDERLINED = "\033[4;31m"; // RED
	public static final String GREEN_UNDERLINED = "\033[4;32m"; // GREEN
	public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
	public static final String BLUE_UNDERLINED = "\033[4;34m"; // BLUE
	public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
	public static final String CYAN_UNDERLINED = "\033[4;36m"; // CYAN
	public static final String WHITE_UNDERLINED = "\033[4;37m"; // WHITE

	// Background
	public static final String BLACK_BACKGROUND = "\033[40m"; // BLACK
	public static final String RED_BACKGROUND = "\033[41m"; // RED
	public static final String GREEN_BACKGROUND = "\033[42m"; // GREEN
	public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
	public static final String BLUE_BACKGROUND = "\033[44m"; // BLUE
	public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
	public static final String CYAN_BACKGROUND = "\033[46m"; // CYAN
	public static final String WHITE_BACKGROUND = "\033[47m"; // WHITE

	// High Intensity
	public static final String BLACK_BRIGHT = "\033[0;90m"; // BLACK
	public static final String RED_BRIGHT = "\033[0;91m"; // RED
	public static final String GREEN_BRIGHT = "\033[0;92m"; // GREEN
	public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
	public static final String BLUE_BRIGHT = "\033[0;94m"; // BLUE
	public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
	public static final String CYAN_BRIGHT = "\033[0;96m"; // CYAN
	public static final String WHITE_BRIGHT = "\033[0;97m"; // WHITE

	// Bold High Intensity
	public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
	public static final String RED_BOLD_BRIGHT = "\033[1;91m"; // RED
	public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
	public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
	public static final String BLUE_BOLD_BRIGHT = "\033[1;94m"; // BLUE
	public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
	public static final String CYAN_BOLD_BRIGHT = "\033[1;96m"; // CYAN
	public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

	// High Intensity backgrounds
	public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
	public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
	public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
	public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
	public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
	public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
	public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m"; // CYAN
	public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m"; // WHITE

	public static String clear() {
		return CLEAR;
	}
	
	public static String underline(String st) {
		return UNDERLINE + st + RESET;
	}
	
	public static String bold(String st) {
		return BOLD + st + RESET;
	}

	public static String italic(String st) { return ITALIC + st + RESET; }
	
	public static String black(String st) {
		return BLACK + st + RESET;
	}

	public static String blackBold(String st) {
		return BLACK_BOLD + st + RESET;
	}
	
	public static String blackUnderline(String st) {
    	return BLACK_UNDERLINED + st + RESET;
    }

	public static String red(String st) {
		return RED + st + RESET;
	}

	public static String redBold(String st) {
		return RED_BOLD + st + RESET;
	}
	
	public static String redUnderline(String st) {
		return RED_UNDERLINED+ st + RESET;
	}


	public static String green(String st) {
		return GREEN + st + RESET;
	}

	public static String greenBold(String st) {
		return GREEN_BOLD + st + RESET;
	}

	public static String yellow(String st) {
		return YELLOW + st + RESET;
	}

	public static String yellowItalic(String st) {
		return YELLOW_ITALIC + st + RESET;
	}

	public static String yellowBold(String st) {
		return YELLOW_BOLD + st + RESET;
	}
	
	public static String cyan(String st) {
		return CYAN + st + RESET;
	}

	public static String cyanBold(String st) {
		return CYAN_BOLD + st + RESET;
	}
	
	public static String orange(String st) {
		return ORANGE + st + RESET;
	}

	public static String orangeBold(String st) {
		return ORANGE_BOLD + st + RESET;
	}
	
	

}
