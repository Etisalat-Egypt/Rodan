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

package com.rodan.intruder.ss7.usecases.model.infogathering;

import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.library.model.Validator;
import com.rodan.library.model.annotation.Option;
import com.rodan.library.model.config.node.config.IntruderNodeConfig;
import com.rodan.library.model.error.ValidationException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter @ToString
public class SmsRoutingInfoOptions extends Ss7ModuleOptions<IntruderNodeConfig> {
    @Option(name = "msisdn", description = "Target MSISDN", mandatory = true)
    private String msisdn;

    @Option(name = "imsi", description = "Any valid IMSI for target network (required for SMS Home Routing bypass)")
    private String imsi;

    @Option(name = "hlr", description = "Target HLR global title (to address HLR directly)")
    private String targetHlrGt;

    @Option(name = "smsc", description = "Attacker's SMSC GT (empty means use local GT)")
    private String smscGt;

    @Option(name = "detect_hr", description = "Detect SMS Home Routing (yes/no)", mandatory = true)
    private String detectSmsHomeRouting;

    @Option(name = "bypass_nr", description = "Bypass filtering using E.214 numbering plan (yes/no)", mandatory = true)
    private String bypassSmsHomeRouting;

    @Option(name = "bypass_oct", description = "Bypass filtering using operational code tag abuse (yes/no)", mandatory = true)
    private String abuseOpcodeTag;

    @Option(name = "bypass_acn", description = "Bypass filtering using malformed ACN (yes/no)", mandatory = true)
    private String malformedAcn;

    @Option(name = "bypass_dm", description = "Bypass filtering using double MAP component (yes/no)", mandatory = true)
    private String doubleMap;

    @Option(name = "acv", description = "MAP app context version (1, 2, 3)", mandatory = true)
    private String mapVersion;

    @Option(name = "cc", description = "Target CC", display = false)
    String cc;

    @Option(name = "ndc", description = "Target NDC", display = false)
    String ndc;

    @Option(name = "mcc", description = "Target MCC", display = false)
    String mcc;

    @Option(name = "mnc", description = "Target MNC", display = false)
    String mnc;

    @Builder
    public SmsRoutingInfoOptions(IntruderNodeConfig nodeConfig, String msisdn, String imsi, String targetHlrGt, String smscGt, String detectSmsHomeRouting,
                                 String bypassSmsHomeRouting, String abuseOpcodeTag, String malformedAcn, String doubleMap,
                                 String mcc, String mnc, String cc, String ndc, String mapVersion) {
        super(nodeConfig);
        this.msisdn = Objects.requireNonNullElse(msisdn, "");
        this.imsi = Objects.requireNonNullElse(imsi, "");
        this.targetHlrGt = Objects.requireNonNullElse(targetHlrGt, "");
        this.smscGt = Objects.requireNonNullElse(smscGt, "");
        this.detectSmsHomeRouting = Objects.requireNonNullElse(detectSmsHomeRouting, "No");
        this.bypassSmsHomeRouting = Objects.requireNonNullElse(bypassSmsHomeRouting, "No");
        this.abuseOpcodeTag = Objects.requireNonNullElse(abuseOpcodeTag, "No");
        this.malformedAcn = Objects.requireNonNullElse(malformedAcn, "No");
        this.doubleMap = Objects.requireNonNullElse(doubleMap, "No");
        this.cc = Objects.requireNonNullElse(cc, "");
        this.ndc = Objects.requireNonNullElse(ndc, "");
        this.mcc = Objects.requireNonNullElse(mcc, "");
        this.mnc = Objects.requireNonNullElse(mnc, "");
        this.mapVersion = Objects.requireNonNullElse(mapVersion, "2");
    }

    @Override
    public String getSpoofSender() {
        return "No";
    }

    @Override
    public void validate() throws ValidationException {
        Validator.validateMsisdn(msisdn);
        Validator.validateToggleOption(detectSmsHomeRouting, "detectSmsHomeRouting");
        Validator.validateToggleOption(bypassSmsHomeRouting, "bypassSmsHomeRouting");
        Validator.validateToggleOption(abuseOpcodeTag, "abuseOpcodeTag");
        Validator.validateToggleOption(malformedAcn, "malformedAcn");
        Validator.validateToggleOption(doubleMap, "doubleMap");
        Validator.validateMapVersion(mapVersion);

        if (!targetHlrGt.isBlank())
            Validator.validateHlr(targetHlrGt);
        if (!smscGt.isBlank())
            Validator.validateSmsc(smscGt);
        if ("Yes".equalsIgnoreCase(bypassSmsHomeRouting)) {
            Validator.validateImsi(imsi);
            Validator.validateAddressCode(cc);
            Validator.validateAddressCode(ndc);
            Validator.validateAddressCode(mcc);
            Validator.validateAddressCode(mnc);
        }
    }
}
