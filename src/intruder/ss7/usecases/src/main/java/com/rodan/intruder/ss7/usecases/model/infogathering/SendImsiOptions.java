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
public class SendImsiOptions extends Ss7ModuleOptions<IntruderNodeConfig> {
    @Option(name = "msisdn", description = "Target MSISDN", mandatory = true)
    private String msisdn;

    @Option(name = "hlr", description = "Target HLR global title")
    private String targetHlrGt;

    @Option(name = "bypass_oct", description = "Bypass filtering using operational code tag abuse (yes/no)", mandatory = true)
    private String abuseOpcodeTag;

    @Option(name = "bypass_acn", description = "Bypass filtering using malformed ACN (yes/no)", mandatory = true)
    private String malformedAcn;

    @Option(name = "acv", description = "MAP app context version (2)", mandatory = true)
    private String mapVersion;

    @Builder
    public SendImsiOptions(IntruderNodeConfig nodeConfig, String msisdn, String targetHlrGt, String abuseOpcodeTag,
                           String malformedAcn, String mapVersion) {
        super(nodeConfig);
        this.msisdn = Objects.requireNonNullElse(msisdn, "");
        this.targetHlrGt = Objects.requireNonNullElse(targetHlrGt, "");
        this.abuseOpcodeTag = Objects.requireNonNullElse(abuseOpcodeTag, "No");
        this.malformedAcn = Objects.requireNonNullElse(malformedAcn, "No");
        this.mapVersion = Objects.requireNonNullElse(mapVersion, "2");
    }

    @Override
    public String getSpoofSender() {
        return "No";
    }

    @Override
    public void validate() throws ValidationException {
        Validator.validateMsisdn(msisdn);
        Validator.validateToggleOption(abuseOpcodeTag, "abuseOpcodeTag");
        Validator.validateToggleOption(malformedAcn, "malformedAcn");
        Validator.validateMapVersion(mapVersion);

        if (!targetHlrGt.isBlank())
            Validator.validateHlr(targetHlrGt);
    }
}
