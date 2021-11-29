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
public class MoCallInterceptionMsrnOptions extends Ss7ModuleOptions<IntruderNodeConfig> {
    @Option(name = "imsi", description = "Target IMSI", mandatory = true)
    private String imsi;

    @Option(name = "msrn", description = "Attacker's MSRN for call forwarding", mandatory = true)
    private String msrn;

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
    public MoCallInterceptionMsrnOptions(IntruderNodeConfig nodeConfig, String imsi, String msrn, String mcc, String mnc,
                                         String cc, String ndc, String mapVersion) {
        super(nodeConfig);
        this.imsi = Objects.requireNonNullElse(imsi, "");
        this.msrn = Objects.requireNonNullElse(msrn, "");
        this.cc = Objects.requireNonNullElse(cc, "");
        this.ndc = Objects.requireNonNullElse(ndc, "");
        this.mcc = Objects.requireNonNullElse(mcc, "");
        this.mnc = Objects.requireNonNullElse(mnc, "");
        this.mapVersion = Objects.requireNonNullElse(mapVersion, "3");
    }

    @Override
    public String getSpoofSender() {
        return "No";
    }

    @Override
    public void validate() throws ValidationException {
        Validator.validateImsi(imsi);
        Validator.validateMsrn(msrn);
        Validator.validateMapVersion(mapVersion);
        Validator.validateAddressCode(cc);
        Validator.validateAddressCode(ndc);
        Validator.validateAddressCode(mcc);
        Validator.validateAddressCode(mnc);
    }
}
