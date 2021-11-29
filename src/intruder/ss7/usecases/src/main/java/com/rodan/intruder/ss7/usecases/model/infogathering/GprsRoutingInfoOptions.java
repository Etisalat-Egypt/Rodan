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
public class GprsRoutingInfoOptions extends Ss7ModuleOptions<IntruderNodeConfig> {
    @Option(name = "imsi", description = "Target IMSI number", mandatory = true)
    private String imsi;

    @Option(name = "msisdn", description = "Target MSISDN (either MSISDN or HLR GT is required)")
    private String msisdn;

    @Option(name = "hlr", description = "Target HLR global title (either MSISDN or HLR GT is required)")
    private String targetHlrGt;

    @Option(name = "ggsn_number", description = "Attacker GGSN global title (empty means use local GT)")
    private String ggsnNumber;

    @Option(name = "acv", description = "MAP app context version (3, 4)", mandatory = true)
    private String mapVersion;

    @Builder
    public GprsRoutingInfoOptions(IntruderNodeConfig nodeConfig, String imsi, String msisdn, String targetHlrGt,
                                  String ggsnNumber, String mapVersion) {
        super(nodeConfig);
        this.imsi = Objects.requireNonNullElse(imsi, "");
        this.msisdn = Objects.requireNonNullElse(msisdn, "");
        this.targetHlrGt = Objects.requireNonNullElse(targetHlrGt, "");
        this.ggsnNumber = Objects.requireNonNullElse(ggsnNumber, "");
        this.mapVersion = Objects.requireNonNullElse(mapVersion, "3");
    }

    @Override
    public String getSpoofSender() {
        return "No";
    }

    @Override
    public void validate() throws ValidationException {
        Validator.validateImsi(imsi);
        Validator.validateMapVersion(mapVersion);
        String[] params = {msisdn,targetHlrGt};
        Validator.validateOneMandatory(params, "either MSISDN or HLR GT is required");

        if (!ggsnNumber.isBlank())
            Validator.validateGgsn(ggsnNumber);
    }
}
