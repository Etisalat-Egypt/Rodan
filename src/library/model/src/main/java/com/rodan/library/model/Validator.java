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

package com.rodan.library.model;

import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

public class Validator {
    final static Logger logger = LogManager.getLogger(Validator.class);

    public static final Pattern E146_PATTERN = Pattern.compile("\\d{2,15}");
    public static final Pattern E212_PATTERN = Pattern.compile("\\d{14,15}");
    public static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+");
    public static final Pattern IMEI_PATTERN = Pattern.compile("\\d{15}");
    public static final Pattern TMSI_PATTERN = Pattern.compile("[a-fA-F0-9]{2,8}");
    public static final Pattern NODE_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9._]{1,20}");
    public static final Pattern IP4_PATTERN = Pattern.compile("(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])");
    public static final Pattern IP6_PATTERN = Pattern.compile("(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))"); // TODO Test
    public static final Pattern HOSTNAME_PATTERN = Pattern.compile("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$");
    public static final Pattern POINT_CODE_ITU_DECIMAL_PATTERN = Pattern.compile("[0-9]{1,5}");
    public static final Pattern POINT_CODE_ITU_383_PATTERN = Pattern.compile("^[0-7]-[0-9]{1,3}-[0-7]$");
    public static final Pattern NETWORK_INDICATOR_PATTERN = Pattern.compile("^[0-3]$");
    public static final Pattern MCC_PATTERN = Pattern.compile("^[0-9]{3}$");
    public static final Pattern MNC_PATTERN = Pattern.compile("^[0-9]{2,3}$");
    public static final Pattern CC_PATTERN = Pattern.compile("^[0-9]{1,3}$");
    public static final Pattern NDC_PATTERN = Pattern.compile("^[0-9]{1,13}$"); // len(CC+NDC+SN)=15
    public static final Pattern DIAMETER_REALM_PATTERN = Pattern.compile(""); // TODO Diameter add regex
    public static final Pattern BASIC_GLOBAL_TITLE_RANGE_PATTERN = Pattern.compile("^[0-9\\-, ]+$");
    public static final Pattern DIAMETER_HOST_PATTERN = HOSTNAME_PATTERN;
    public static final Pattern DIAMETER_BASIC_HOST_RANGE_PATTERN = Pattern.compile("^[a-zA-Z0-9., ]*$");
    public static final Pattern FILE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-]+[.]?[a-zA-Z]{1,3}$");
    public static final Pattern SM_TYPE_PATTERN = Pattern.compile("^(normal|replace|flash|silent)$");

    public static void validateMsisdn(String msisdn) throws ValidationException {
        validateMsisdn(msisdn, "MSISDN");
    }

    public static void validateMsisdn(String msisdn, String paramName) throws ValidationException {
        validateE146(msisdn, paramName);
    }

    public static void validateImsi(String imsi) throws ValidationException {
        validateE212(imsi, "IMSI");
    }

    public static void validateTmsi(String tmsi) throws ValidationException {
        if (StringUtils.isBlank(tmsi) || !TMSI_PATTERN.matcher(tmsi).matches()) {
            var msg = "Invalid TMSI: " + tmsi;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_IMSI).message(msg).build();
        }
    }

    public static void validateMsrn(String msrn) throws ValidationException {
        validateE146(msrn, "MSRN");
    }

    public static void validateHlr(String hlr) throws ValidationException {
        validateE146(hlr, "HLR");
    }

    public static void validateLocalGlobalTitle(String gt) throws ValidationException {
        validateE146(gt, "local GT");
    }

    public static void validatePeerGlobalTitle(String gt) throws ValidationException {
        validateE146(gt, "peer GT");
    }

    public static void validateVlr(String vlr) throws ValidationException {
        validateE146(vlr, "VLR");
    }

    public static void validateMsc(String msc) throws ValidationException {
        validateE146(msc, "MSC");
    }

    public static void validateSmsc(String smsc) throws ValidationException {
        validateE146(smsc, "SMSC");
    }

    public static void validateGsmScf(String gsmScf) throws ValidationException {
        validateE146(gsmScf, "gsmScf");
    }

    public static void validateGmlc(String gmlc) throws ValidationException {
        validateE146(gmlc, "gmlc");
    }

    public static void validateSgsn(String sgsn) throws ValidationException {
        validateE146(sgsn, "sgsn");
    }

    public static void validateGgsn(String ggsn) throws ValidationException {
        validateE146(ggsn, "ggsn");
    }

    public static void validateMapVersion(String value) throws ValidationException {
        validateInteger(value, "MAP version");
    }

    public static void validateAuthVectorNumber(String value) throws ValidationException {
        validateNumericRange(value, 1, 9, "av_num");
    }

    public static void validateDelayTime(String value) throws ValidationException {
        // JDiameter messages and scheduled at a fixed rate of 500ms, so min delay is 500 to insure sending
        // only 1 message at a time and to avoid buffer memory issues.
        // Ref: org.mobicents.protocols.ss7.m3ua.impl.M3UAManagementImpl.start
        //      fsmTicker.scheduleAtFixedRate(m3uaScheduler, delay: 500, period: 500, TimeUnit.MILLISECONDS);
        // Check sending rate for JSS7
        // Ref: org.mobicents.protocols.sctp.SelectorThread.run
        validateNumericRange(value, 500, 60000, "delay");
    }

    public static void validateAddressCode(String value) throws ValidationException {
        validateInteger(value, "address code");
    }

    public static void validateSmSender(String value) throws ValidationException {
        validateText(value, "sender");
    }

    public static void validateSmContent(String value) throws ValidationException {
        validateText(value, "content");
    }

    public static void validateDosDuration(String value) throws ValidationException {
        validateNumericRange(value,1, 60, "duration");
    }

    public static void validateImei(String imei) throws ValidationException {
        if (StringUtils.isBlank(imei) || !IMEI_PATTERN.matcher(imei).matches()) {
            var msg = "Invalid IMEI:"  + imei;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_IMEI).message(msg).build();
        }
    }

    public static void validateMandatoryField(Object field, String fieldName) throws ValidationException {
        if (field == null) {
            var msg = "Invalid "  + fieldName + " (null)";
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.MISSING_FIELD).message(msg).build();
        }
    }

    public static void validateMandatoryField(Optional field, String fieldName) throws ValidationException {
        if (field == null || field.isEmpty()) {
            var msg = "Invalid "  + fieldName + " (null)";
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.MISSING_FIELD).message(msg).build();
        }
    }

    public static void validateMandatoryCollection(Collection collection, String fieldName) throws ValidationException {
        if (collection == null || collection.size() == 0) {
            var msg = "Invalid "  + fieldName + " (empty)";
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.MISSING_FIELD).message(msg).build();
        }
    }

    public static void validateOneMandatory(String[] values, String msg) throws ValidationException {
        boolean empty = true;
        for (var value : values)
            if (value != null && !value.isBlank()) {
                empty = false;
                break;
            }

        if (empty) {
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_IMSI).message(msg).build();
        }
    }

    public static void validateDiameterRealm(String realm) throws ValidationException {
        // TODO: Add paramName to method
        // TODO Diameter IMP: uncomment after fixing DIAMETER_REALM_PATTERN
//        if (StringUtils.isBlank(realm) || !DIAMETER_REALM_PATTERN.matcher(realm).matches()) {
//            var msg = "Invalid realm: " + realm;
//            logger.error(msg);
//            throw ValidationException.builder().code(ErrorCode.INVALID_IMSI).message(msg).build();
//        }
    }

    private static void validateE212(String address, String paramName) throws ValidationException {
        if (StringUtils.isBlank(address) || !E212_PATTERN.matcher(address).matches()) {
            var msg = "Invalid " + paramName +": " + address;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_IMSI).message(msg).build();
        }
    }

    private static void validateE146(String address, String paramName) throws ValidationException {
        if (StringUtils.isBlank(address) || !E146_PATTERN.matcher(address).matches()) {
            var msg = "Invalid " + paramName + ": " + address;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_MSISDN).message(msg).build();
        }
    }

    public static void validateToggleOption(String value, String paramName) throws ValidationException {
        if (!"Yes".equalsIgnoreCase(value) && !"No".equalsIgnoreCase(value)) {
            var msg = "Invalid " + paramName + ": " + value;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_TOGGLE).message(msg).build();
        }
    }

    private static void validateInteger(String value, String paramName) throws ValidationException {
        try {
            Integer.valueOf(value);

        } catch (NumberFormatException e) {
            var msg = "Invalid " + paramName + ": " + value;
            logger.error(msg, e);
            throw ValidationException.builder().code(ErrorCode.INVALID_MSISDN).message(msg).build();
        }
    }

    private static void validateNumericRange(String value, int min, int max, String paramName) throws ValidationException {
        try {
            int intValue = Integer.valueOf(value);
            if (intValue < min || intValue > max) {
                var msg = "Invalid " + paramName + ": " + value;
                logger.error(msg);
                throw ValidationException.builder().code(ErrorCode.INVALID_MSISDN).message(msg).build();
            }

        } catch (NumberFormatException e) {
            var msg = "Invalid " + paramName + ": " + value;
            logger.error(msg, e);
            throw ValidationException.builder().code(ErrorCode.INVALID_MSISDN).message(msg).build();
        }
    }

    private static void validateText(String value, String paramName) throws ValidationException {
        if (StringUtils.isBlank(value)) {
            var msg = "Invalid " + paramName +": " + value;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_TEXT).message(msg).build();
        }
    }

    public static void validateNodeName(String name) throws ValidationException {
        if (StringUtils.isBlank(name) || !NODE_NAME_PATTERN.matcher(name).matches()) {
            var msg = "Invalid nodeName: " + name;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_NODE_NAME).message(msg).build();
        }
    }

    public static void validateNodeAddress(String address) throws ValidationException {
        if (StringUtils.isBlank(address) || (!IP4_PATTERN.matcher(address).matches()) && (!IP6_PATTERN.matcher(address).matches()) &&
                (!HOSTNAME_PATTERN.matcher(address).matches())) {
            var msg = "Invalid node address: " + address;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_NODE_ADDRESS).message(msg).build();
        }
    }

    public static void validatePortNumber(String port) throws ValidationException {
        var value = Validator.tryParseInt(port);
        if (value == null || value < 1 || value > 65535) {
            var msg = "Invalid node address: " + port;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_PORT_NUMBER).message(msg).build();
        }
    }

    public static void validatePointCode(String pc) throws ValidationException {
        if (StringUtils.isBlank(pc) || !POINT_CODE_ITU_DECIMAL_PATTERN.matcher(pc).matches()) {
            var msg = "Invalid point code: " + pc;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_POINT_CODE).message(msg).build();
        }
    }

    public static void validateRoutingContext(String ctx) throws ValidationException {
        var value = Validator.tryParseLong(ctx);
        // Routing context if 4 bytes, so MAX value should be 0xffffffff
        if (value == null || value < 1 || value > 0xffffffffL) {
            var msg = "Invalid routing context: " + ctx;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_ROUTING_CONTEXT).message(msg).build();
        }
    }

    public static void validateNetworkIndicator(String ni) throws ValidationException {
        if (StringUtils.isBlank(ni) || !NETWORK_INDICATOR_PATTERN.matcher(ni).matches()) {
            var msg = "Invalid network indicator: " + ni;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_NETWORK_INDICATOR).message(msg).build();
        }
    }

    public static void validateMcc(String mcc) throws ValidationException {
        if (StringUtils.isBlank(mcc) || !MCC_PATTERN.matcher(mcc).matches()) {
            var msg = "Invalid MCC: " + mcc;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_MCC).message(msg).build();
        }
    }

    public static void validateMnc(String mnc) throws ValidationException {
        if (StringUtils.isBlank(mnc) || !MNC_PATTERN.matcher(mnc).matches()) {
            var msg = "Invalid MCC: " + mnc;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_MNC).message(msg).build();
        }
    }

    public static void validateCc(String cc) throws ValidationException {
        if (StringUtils.isBlank(cc) || !CC_PATTERN.matcher(cc).matches()) {
            var msg = "Invalid CC: " + cc;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_CC).message(msg).build();
        }
    }

    public static void validateNdc(String ndc) throws ValidationException {
        if (StringUtils.isBlank(ndc) || !NDC_PATTERN.matcher(ndc).matches()) {
            var msg = "Invalid MCC: " + ndc;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_NDC).message(msg).build();
        }
    }

    public static void validateTrue(BooleanSupplier supplier, String errorMessage) throws ValidationException {
        if (!supplier.getAsBoolean()) {
            logger.error(errorMessage);
            throw ValidationException.builder().code(ErrorCode.INVALID_CONDITION).message(errorMessage).build();
        }
    }

    // TODO SS7 IMP: Test
    public static void validateGlobalTitleRange(String range) throws ValidationException {
        if (StringUtils.isBlank(range) || !BASIC_GLOBAL_TITLE_RANGE_PATTERN.matcher(range).matches()) {
            var msg = "Invalid GT range: " + range;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_GT_RANGE).message(msg).build();
        }
        for (var gtRange : range.split(","))
           for (var gt : gtRange.split("-"))
               validateE146(gt, "GT range");
    }

    public static void validateDiameterHostRange(String range) throws ValidationException {
        if (StringUtils.isBlank(range) || !DIAMETER_BASIC_HOST_RANGE_PATTERN.matcher(range).matches()) {
            var msg = "Invalid Diameter host range: " + range;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_GT_RANGE).message(msg).build();
        }
        var hostList = range.split(",");
        for (var host : hostList)
            validateDiameterHost(host, "");
    }

    public static void validateDiameterHost(String host, String paramName) throws ValidationException {
        if (StringUtils.isBlank(host) || !DIAMETER_HOST_PATTERN.matcher(host.trim()).matches()) {
            var msg = "Invalid " + paramName + ": " + host;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_GT_RANGE).message(msg).build();
        }
    }

    public static void validateWordListFileName(String fileName, String baseDir) throws ValidationException {
        if (StringUtils.isBlank(fileName) || !FILE_NAME_PATTERN.matcher(fileName).matches()) {
            var msg = "Invalid file fileName: " + fileName;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_WORDLIST_FILE_PATH).message(msg).build();
        }

        var filePath = Paths.get(baseDir + File.separator + fileName);
        try {
            var fileCanonicalPath = filePath.toFile().getCanonicalPath();
            var baseDirCanonicalPath = Paths.get(baseDir).toFile().getCanonicalPath();
            if (!fileCanonicalPath.startsWith(baseDirCanonicalPath) || Files.notExists(filePath)) {
                var msg = "Invalid file fileName: " + fileName;
                logger.error(msg);
                throw ValidationException.builder().code(ErrorCode.INVALID_WORDLIST_FILE_PATH).message(msg).build();
            }

        } catch (IOException e) {
            var msg = "Failed load file: " + fileName;
            logger.error(msg, e);
            throw ValidationException.builder().code(ErrorCode.IO_ERROR).message(msg).build(); // TODO: Should be SystemException
        }
    }

    public static void validateSmType(String type) throws ValidationException {
        if (StringUtils.isBlank(type) || !SM_TYPE_PATTERN.matcher(type.trim()).matches()) {
            var msg = "Invalid type: " + type;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_SM_TYPE).message(msg).build();
        }
    }

    public static void validateSsn(String ssn) throws ValidationException {
        var value = Validator.tryParseInt(ssn);
        if (value == null || value < 1 || value > 255) {
            var msg = "Invalid SSN: " + ssn;
            logger.error(msg);
            throw ValidationException.builder().code(ErrorCode.INVALID_PORT_NUMBER).message(msg).build();
        }
    }

    private static Integer tryParseInt(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            logger.error("Error parsing numerical value: " + value, e);
            return null;
        }
    }

    private static Long tryParseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            logger.error("Error parsing numerical value: " + value, e);
            return null;
        }
    }
}
