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

package com.rodan.intruder.ss7.usecases.model.dos;

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
public class DosClOptions extends Ss7ModuleOptions<IntruderNodeConfig> {
    @Option(name = "imsi", description = "Target IMSI", mandatory = true)
    private String imsi;

    @Option(name = "vlr", description = "Target VLR global title", mandatory = true)
    private String targetVlrGt;

    @Option(name = "hlr", description = "Target HLR global title (required for spoofing HLR)")
    private String targetHlrGt;

    @Option(name = "spoof", description = "Spoof HLR address in SCCP CgPA (yes/no)", mandatory = true)
    private String spoofHlr;

    @Option(name = "acv", description = "MAP app context version (1, 2, 3)", mandatory = true)
    private String mapVersion;

    @Builder
    public DosClOptions(IntruderNodeConfig nodeConfig, String imsi, String targetVlrGt, String targetHlrGt, String spoofHlr, String mapVersion) {
        super(nodeConfig);
        this.imsi = Objects.requireNonNullElse(imsi, "");
        this.targetVlrGt = Objects.requireNonNullElse(targetVlrGt, "");
        this.targetHlrGt = Objects.requireNonNullElse(targetHlrGt, "");
        this.spoofHlr = Objects.requireNonNullElse(spoofHlr, "No");
        this.mapVersion = Objects.requireNonNullElse(mapVersion, "3");
    }

    @Override
    public String getSpoofSender() {
        return spoofHlr;
    }

    @Override
    public void validate() throws ValidationException {
        Validator.validateImsi(imsi);
        Validator.validateVlr(targetVlrGt);
        Validator.validateToggleOption(spoofHlr, "spoofHlr");
        Validator.validateMapVersion(mapVersion);

        if ("Yes".equalsIgnoreCase(spoofHlr))
            Validator.validateHlr(targetHlrGt);
    }
}
