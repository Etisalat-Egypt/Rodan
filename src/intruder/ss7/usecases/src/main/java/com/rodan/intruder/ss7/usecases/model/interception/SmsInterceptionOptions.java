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

package com.rodan.intruder.ss7.usecases.model.interception;

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
public class SmsInterceptionOptions extends Ss7ModuleOptions<IntruderNodeConfig> {
    @Option(name = "imsi", description = "Target IMSI", mandatory = true)
    private String imsi;

    @Option(name = "hlr", description = "Victim's current HLR (required for SMS forwarding)")
    private String hlrGt;

    @Option(name = "current_msc", description = "Victim's current VMSC global title (required for SMS forwarding)")
    private String currentMscGt;

    @Option(name = "current_vlr", description = "Victim's current VLR global title (required for SMS forwarding)")
    private String currentVlrGt;

    @Option(name = "forward", description = "Forward SMS to victim after interception", mandatory = true)
    private String forwardSmsToVictim;

    @Option(name = "spoof", description = "Spoof HLR address in SCCP CgPA (Yes or No)", mandatory = true)
    private String spoofHlr;

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
    public SmsInterceptionOptions(IntruderNodeConfig nodeConfig, String imsi, String hlrGt, String currentMscGt, String currentVlrGt,
                                  String spoofHlr, String forwardSmsToVictim, String mcc, String mnc, String cc, String ndc,
                                  String mapVersion) {
        super(nodeConfig);
        this.imsi = Objects.requireNonNullElse(imsi, "");
        this.forwardSmsToVictim = Objects.requireNonNullElse(forwardSmsToVictim, "No");
        this.hlrGt = Objects.requireNonNullElse(hlrGt, "");
        this.currentMscGt = Objects.requireNonNullElse(currentMscGt, "");
        this.currentVlrGt = Objects.requireNonNullElse(currentVlrGt, "");
        this.spoofHlr = Objects.requireNonNullElse(spoofHlr, "No");
        this.mapVersion = Objects.requireNonNullElse(mapVersion, "3");
        this.cc = Objects.requireNonNullElse(cc, "");
        this.ndc = Objects.requireNonNullElse(ndc, "");
        this.mcc = Objects.requireNonNullElse(mcc, "");
        this.mnc = Objects.requireNonNullElse(mnc, "");

    }

    @Override
    public String getSpoofSender() {
        return spoofHlr;
    }

    @Override
    public void validate() throws ValidationException {
        Validator.validateImsi(imsi);
        Validator.validateToggleOption(forwardSmsToVictim, "forwardSmsToVictim");
        Validator.validateToggleOption(spoofHlr, "spoof");
        Validator.validateMapVersion(mapVersion);
        Validator.validateAddressCode(cc);
        Validator.validateAddressCode(ndc);
        Validator.validateAddressCode(mcc);
        Validator.validateAddressCode(mnc);

        if (!currentMscGt.isBlank())
            Validator.validateMsc(currentMscGt);
        if (!currentVlrGt.isBlank())
            Validator.validateVlr(currentVlrGt);

        if ("Yes".equalsIgnoreCase(spoofHlr))
            Validator.validateHlr(hlrGt);
    }
}
