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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Objects;

@Getter @ToString
public class VlrBruteforceOptions extends Ss7ModuleOptions<IntruderNodeConfig> {
    final static Logger logger = LogManager.getLogger(VlrBruteforceOptions.class);

    @Option(name = "imsi", description = "Target IMSI number", mandatory = true)
    private String imsi;

    // TODO Add range description
    @Option(name = "range", description = "Target VLR global title range (\\d,-)", mandatory = true)
    private String targetVlrRange;

    @Option(name = "delay", description = "Delay in milli seconds", mandatory = true)
    private String delayMillis;

    @Option(name = "bypass_oct", description = "Bypass filtering using operational code tag abuse (yes/no)", mandatory = true)
    private String abuseOpcodeTag;

    @Option(name = "acv", description = "MAP app context version (3)", mandatory = true)
    private String mapVersion;

    @Builder
    public VlrBruteforceOptions(IntruderNodeConfig nodeConfig, String imsi, String targetVlrRange, String delayMillis,
                                String abuseOpcodeTag, String mapVersion) {
        super(nodeConfig);
        this.imsi = Objects.requireNonNullElse(imsi, "");
        this.targetVlrRange = Objects.requireNonNullElse(targetVlrRange, "");
        this.delayMillis = Objects.requireNonNullElse(delayMillis, "500");
        this.abuseOpcodeTag = Objects.requireNonNullElse(abuseOpcodeTag, "No");
        this.mapVersion = Objects.requireNonNullElse(mapVersion, "3");
    }

    @Override
    public String getSpoofSender() {
        return "No";
    }

    @Override
    public void validate() throws ValidationException {
        Validator.validateImsi(imsi);
        Validator.validateGlobalTitleRange(targetVlrRange);
        Validator.validateToggleOption(abuseOpcodeTag, "abuseOpcodeTag");
        Validator.validateMapVersion(mapVersion);
        Validator.validateDelayTime(delayMillis);
    }
}
