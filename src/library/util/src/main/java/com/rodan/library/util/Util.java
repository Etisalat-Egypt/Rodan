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

package com.rodan.library.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.rodan.library.model.Constants;
import com.rodan.library.model.PayloadStream;
import com.rodan.library.model.Validator;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.error.ValidationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Util {
	final static Logger logger = LogManager.getLogger(Util.class);

	public static <T extends NodeConfig> T loadConfigs(String path) throws SystemException, ValidationException {
		try {
			logger.debug("Loading config file: " + path);
			var mapper = new ObjectMapper(new YAMLFactory());
			mapper.findAndRegisterModules();
			var config = mapper.readValue(new File(path), NodeConfig.class);
			logger.debug("Config file loaded successfully: " + config);
			config.validate();
			return (T) config;

		} catch (IOException | ClassCastException e) {
			var msg = String.format("Failed to load config file: %s!", path);
			logger.error(msg, e);
			throw SystemException.builder().code(ErrorCode.INVALID_FILE).message(msg).parent(e).build();
		}
	}

	public static List<String> loadWordList(String path) throws SystemException {
		try {
			logger.debug("Loading wordlist from file: " + path);
			var filePath = Paths.get(path);
			return Files.readAllLines(filePath);

		} catch (IOException e) {
			var msg = String.format("Failed to load wordlist file: %s!", path);
			logger.error(msg, e);
			throw SystemException.builder().code(ErrorCode.INVALID_FILE).message(msg).parent(e).build();
		}
	}

	public static PayloadStream loadWordListLazy(String path) throws SystemException {
		try {
			logger.debug("Loading wordlist from file: " + path);
			var filePath = Paths.get(path);
			var stream = Files.lines(filePath);
			var streamSize = Files.size(filePath);;
			return PayloadStream.builder().size(streamSize).stream(stream).build();

		} catch (IOException e) {
			var msg = String.format("Failed to load wordlist file: %s!", path);
			logger.error(msg, e);
			throw SystemException.builder().code(ErrorCode.INVALID_FILE).message(msg).parent(e).build();
		}
	}

	public static String getWordListsDirectory(){
		var cwd = FileSystems.getDefault().getPath("").toAbsolutePath().toString();
		return cwd + File.separator + Constants.WORD_LIST_BASE_DIR_NAME;
	}

	public static void cleanupStackConfigFiles(NodeConfig nodeConfig) throws SystemException {
		try {
			logger.debug("Deleting old config files...");
			FileUtils.deleteDirectory(new File(Constants.BASE_CONFIG_DIR));

			logger.debug("Creating config directory tree...");
			Files.createDirectories(Paths.get(Constants.BASE_CONFIG_DIR + File.separator +
					nodeConfig.getSs7Association().getLocalNode().getNodeName()));

			logger.debug("Old config dir cleaned successfully: " + Constants.BASE_CONFIG_DIR);

		} catch (IOException e) {
			var msg = "Failed to clean config dir.";
			logger.error(msg, e);
			throw SystemException.builder().code(ErrorCode.IO_ERROR).message(msg).parent(e).build();
		}
	}

	public static boolean isIPv4Address(String address) {
		return Validator.IP4_PATTERN.matcher(address).find();
	}

	public static boolean isItu383Pc(String pc) {
		return Validator.POINT_CODE_ITU_383_PATTERN.matcher(pc).find();
	}

	public static boolean isItuDecimalPc(String pc) {
		return Validator.POINT_CODE_ITU_DECIMAL_PATTERN.matcher(pc).find();
	}

	public static int convertPointCode(String pc) throws ValidationException {
		var decimalPc = 0;
		logger.debug(String.format("Converting point code: %s", pc));
		if (Util.isItuDecimalPc(pc)) {
			decimalPc = Integer.valueOf(pc);

		} else if (Util.isItu383Pc(pc)) {
			// ref: https://www.modulo.co.il/tools/ss7-point-code-converter/
			// ref: https://tools.valid8.com/#ss7
			var pc1 = Integer.valueOf(pc.split("-")[0]);
			var pc2 = Integer.valueOf(pc.split("-")[1]);
			var pc3 = Integer.valueOf(pc.split("-")[2]);
			if (pc1 > 7 || pc2 > 255 || pc3 > 7 ) {
				String msg = "Invalid point code: " + pc;
				logger.error(msg);
				throw ValidationException.builder().code(ErrorCode.INVALID_POINT_CODE).message(msg).build();
			}
			pc1 = pc1 << 11; // 3-8-3, so shift with 8 + 3
			pc2 = pc2 << 3; // 3-8-3, so shift with 3
			pc3 = pc3 << 0; // 3-8-3, so shift with 0
			decimalPc = pc1 | pc2 | pc3;

		} else {
			String msg = "Invalid point code: " + pc;
			logger.error(msg);
			throw ValidationException.builder().code(ErrorCode.INVALID_POINT_CODE).message(msg).build();
		}

		logger.debug(String.format("Converted point code: %s", decimalPc));

		return decimalPc;
	}

	public static String resolveIp(String hostname) throws SystemException {
		String ipAddress;
		if (Util.isIPv4Address(hostname)) {
			ipAddress = hostname;
		} else {
			try {
				logger.debug(String.format("Resolving %s to IP address", hostname));
				var address = InetAddress.getByName(hostname);
				ipAddress = address.getHostAddress();
				logger.debug(String.format("Got IP address: %s", ipAddress));

			} catch (UnknownHostException e) {
				var msg = String.format("Failed to resolve hostname: %s!", hostname);
				logger.error(msg, e);
				throw SystemException.builder().code(ErrorCode.INVALID_ADDRESS).message(msg).parent(e).build();
			}
		}

		return ipAddress;
	}

	// Ref: org.mobicents.protocols.ss7.map.primitives.GSNAddressImpl.printDataArr
	public static String toIpString(byte[] addressBytes) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		if (addressBytes != null) {
			for (byte b : addressBytes) {
				if (first)
					first = false;
				else
					sb.append(".");
				sb.append(b & 0xFF); // Equivalent to Byte.toUnsignedInt(b)
			}
		}
		return sb.toString();
	}

	public static <T,U> U getValueOrElse(T obj, Function<T,U> function, U fallback) {
		if(obj == null) {
			return fallback;
		} else {
			return function.apply(obj);
		}
	}

	public static <T,U> U getValueOrElseNull(T obj, Function<T,U> function) {
		if(obj == null) {
			return null;
		} else {
			return function.apply(obj);
		}
	}

	public static void startLongRunningTask(LongRunningTask task) throws SystemException {
		try {
			if (StringUtils.isNotBlank(task.workStartMessage))
				logger.debug(task.workStartMessage);

			if (!task.workDoneCheck.apply(null) && task.startWorkAction != null)
				task.startWorkAction.call();

			var shouldWait = true;
			var isWorkDone = false;
			var endTime = System.currentTimeMillis() + task.waitTime;

			while (shouldWait) {
				if (task.workDoneCheck.apply(null)) {
					if (StringUtils.isNotBlank(task.workDoneMessage))
						logger.debug(task.workDoneMessage);
					shouldWait = false;
					isWorkDone = true;

				} else {
					if (StringUtils.isNotBlank(task.workWaitMessage))
						logger.debug(task.workWaitMessage);
					if (task.workWaitAction != null)
						task.workWaitAction.call();
					shouldWait = System.currentTimeMillis() < endTime;
					Thread.sleep(task.checkInterval);
				}
			}

			if (!isWorkDone && task.throwExceptionOnFailure) {
				if (StringUtils.isNotBlank(task.workFailedMessage))
					logger.error(task.workFailedMessage);
				throw SystemException.builder().code(ErrorCode.GENERAL_ERROR).message(task.workFailedMessage).build();
			}

		} catch (InterruptedException e) {
			var msg = String.format("%s. Cause: %s", task.workFailedMessage, e.getMessage());
			logger.error(msg, e);
			var errorCode = task.workFailedErrorCode != 0 ? task.workFailedErrorCode : ErrorCode.GENERAL_ERROR;
			throw SystemException.builder().code(errorCode).message(msg).build();
		}
	}

	public static String encodeHexString(byte[] byteArray) {
		var hexStringBuffer = new StringBuilder();
		for (var b : byteArray) {
			hexStringBuffer.append(byteToHex(b));
		}
		return hexStringBuffer.toString();
	}

	public static byte[] decodeHexString(String hexString) {
		if (hexString.length() % 2 == 1) {
			throw new IllegalArgumentException("Invalid hexadecimal String supplied.");
		}

		byte[] bytes = new byte[hexString.length() / 2];
		for (int i = 0; i < hexString.length(); i += 2) {
			bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
		}
		return bytes;
	}

	public static String getConfigDir(String nodeName) {
		return Constants.BASE_CONFIG_DIR + File.separator + nodeName;
	}

	private static String byteToHex(byte num) {
		char[] hexDigits = new char[2];
		hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
		hexDigits[1] = Character.forDigit((num & 0xF), 16);
		return new String(hexDigits);
	}

	public static byte hexToByte(String hexString) {
		int firstDigit = toDigit(hexString.charAt(0));
		int secondDigit = toDigit(hexString.charAt(1));
		return (byte) ((firstDigit << 4) + secondDigit);
	}

	private static int toDigit(char hexChar) {
		int digit = Character.digit(hexChar, 16);
		if(digit == -1) {
			throw new IllegalArgumentException(
					"Invalid Hexadecimal Character: "+ hexChar);
		}
		return digit;
	}

	public static OptionalInt getLargest(Integer[] arr, int max) {
		List<Integer> list = Arrays.asList(arr);
		return list.stream().filter(i -> i <= max).mapToInt(i -> i).max();
	}

	public static String generateRandomImsi(String mcc, String mnc) {
		// TODO IMP: msin length depend on length of mcc + length of mnc
		var msin = ThreadLocalRandom.current().nextLong(1000000000, 9999999999L);
		return mcc + mnc + msin;
	}

	public static String generateRandomE214Address(String cc, String ndc) {
		var msin = ThreadLocalRandom.current().nextLong(1000000000, 9999999999L);
		// TODO IMP: msin length depend on length of mcc + length of mnc
		return cc + ndc + msin;
	}

	public static String generateE214Address(String imsi, String mcc, String mnc, String cc, String ndc) throws ValidationException {
		var msin = imsi;
		if (msin.startsWith(mcc)) {
			msin = msin.substring(mcc.length());
		} else {
			var msg = "Invalid IMSI format: " + msin;
			logger.error(msg);
			throw ValidationException.builder().code(ErrorCode.INVALID_IMSI).message(msg).build();
		}

		if (msin.startsWith(mnc)) {
			msin = msin.substring(mnc.length());
		} else {
			var msg = "Invalid IMSI format: " + msin;
			logger.error(msg);
			throw ValidationException.builder().code(ErrorCode.INVALID_IMSI).message(msg).build();
		}

		return cc + ndc + msin;
	}

	public static byte[] generateVisitedPlmnId(int mcc, int mnc) {
		// Should be
		/*
		 * Ref: ts_129272v160600p, Table 7.3.9/1: Encoding format for Visited-PLMN-Id AVP
		 *   -------------------------------------
		 *   |   MCC Digit 2   |   MCC Digit 1   |   Octet 1
		 *   |   MNC Digit 3   |   MCC Digit 3   |   Octet 2 (if MNC Digit 3 replace with 1111b)
		 *   |   MNC Digit 2   |   MNC Digit 1   |   Octet 3
		 *   -------------------------------------
		 *
		 * After Trials, found a different encoding (maybe something related to endianness
		 *   -------------------------------------
		 *   |   MCC Digit 2   |   MCC Digit 3   |   Octet 1
		 *   |   MNC Digit 3   |   MCC Digit 1   |   Octet 2 (if MNC Digit 3 replace with 1111b)
		 *   |   MNC Digit 2   |   MNC Digit 1   |   Octet 3
		 *   -------------------------------------
		 * */

		byte mccDigit1 = Util.getDigit(mcc, 1);
		byte mccDigit2 = Util.getDigit(mcc, 2);
		byte mccDigit3 = Util.getDigit(mcc, 3);

		byte mncDigit1 = Util.getDigit(mnc, 1);
		byte mncDigit2 = Util.getDigit(mnc, 2);
		byte mncDigit3 = Util.getDigit(mnc, 3);
		if (mncDigit3 == 0) {
			mncDigit3 = 0xF;
		}

		byte octet1 = (byte) ((mccDigit2 << 4) | mccDigit3);
		byte octet2 = (byte) ((mncDigit3 << 4) | mccDigit1);
		byte octet3 = (byte) ((mncDigit1 << 4) | mncDigit2);

		return new byte[] {octet1, octet2, octet3};
	}

	public static byte getDigit(int value, int position) {
		return (byte) (value / (int)Math.pow(10, position - 1) % 10);
	}

	public static PayloadStream generateGtRange(String rangeDefinition) {
		logger.debug("Generating GT range for: " + rangeDefinition);
		List<String> rangeList = new ArrayList<>();
		var parts = rangeDefinition.split(",");
		for (var part : parts) {
			if (part.contains("-")) {
				var start = Long.valueOf(part.split("-")[0].trim());
				var end = Long.valueOf(part.split("-")[1].trim());
				logger.debug("Adding from GT: " + start + " to GT: " + end + " to range");
				LongStream.rangeClosed(start, end).boxed().forEach(v -> rangeList.add(String.valueOf(v)));

			} else {
				logger.debug("Adding GT: " + part.trim() + " to range");
				rangeList.add(part.trim());
			}
		}

		var gtSize = rangeList.get(0).getBytes().length;
		var gtStream = rangeList.stream().distinct().sorted();
		var streamSize = (long) gtSize * rangeList.size(); // Not accurate, should count after distinct, but used to avoid closing the stream.
		return PayloadStream.builder().size(streamSize).stream(gtStream).build();
	}

	public static List<String> generateHostRange(String rangeDefinition) {
		List<String> range = new ArrayList<>();
		try {
			List<String> rangeList = new ArrayList<>();
			var parts = rangeDefinition.split(",");
			for (var part : parts) {
				rangeList.add(part.trim());
			}

			range = rangeList.stream().distinct().sorted().collect(Collectors.toList());

		} catch (Exception e) {
			range.clear();
		}

		return range;
	}

	public static String tbcdToString(byte[] value) {
		var radix = 16;
		int digits = 2;
		var buffer = new StringBuilder(value.length * (digits + 1));

		for (var b : value) {
			int byteValue = b & 255;
			String strValue = Integer.toString(byteValue, radix);
			for (var j = 0; j < digits - strValue.length(); j++) {
				strValue = "0" + strValue;
			}

			buffer.append(new StringBuffer(strValue).reverse());
		}

		return buffer.toString();
	}

	public static String octetStringToString(byte[] value) {
		var radix = 16;
		int digits = 2;
		var buffer = new StringBuilder(value.length * (digits + 1));

		for (var b : value) {
			int byteValue = b & 255;
			String strValue = Integer.toString(byteValue, radix);
			for (var j = 0; j < digits - strValue.length(); j++) {
				strValue = "0" + strValue;
			}

			buffer.append(strValue);
		}

		return buffer.toString();
	}

	public static List<Class<?>> getAvailableModules(String packageName) {
		// Reflections version 0.9.12 will crash if no classes were found,
		// revert to 0.9.11 or wait for a fix in 0.9.13
		var reflections = new Reflections(packageName);
		return reflections.getTypesAnnotatedWith(Module.class).stream()
				.filter( m -> m.getAnnotation(Module.class).display())
				.sorted(Comparator.<Class<?>, String>comparing(m -> m.getAnnotation(Module.class).category())
						.thenComparing(m -> m.getAnnotation(Module.class).name())
				)
				.collect(Collectors.toList());
	}

	public static String hexToDecimal(String hex) {
		var value = Integer.parseInt(hex, 16);
		return String.valueOf(value);
	}

	public static void printStackTrace() {
		logger.debug("###### Printing stack trace...");
		var stackTrace = Thread.currentThread().getStackTrace();
		for (var trace : stackTrace) {
			logger.debug("\t"+ trace);
		}
		logger.debug("###### Done!");
	}
}
