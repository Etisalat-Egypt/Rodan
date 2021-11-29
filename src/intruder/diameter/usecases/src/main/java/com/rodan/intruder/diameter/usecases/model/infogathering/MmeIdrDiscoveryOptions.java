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

package com.rodan.intruder.diameter.usecases.model.infogathering;

import com.rodan.intruder.diameter.usecases.model.DiameterModuleOptions;
import com.rodan.library.model.Validator;
import com.rodan.library.model.annotation.Option;
import com.rodan.library.model.config.node.config.IntruderNodeConfig;
import com.rodan.library.model.error.ValidationException;
import com.rodan.library.util.Util;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter @ToString
public class MmeIdrDiscoveryOptions extends DiameterModuleOptions {
    @Option(name = "mme_file", description = "Target MME list file name", mandatory = true)
    private String targetMmeFileName;

    @Option(name = "realm", description = "Destination realm", mandatory = true)
    private String destinationRealm;

    @Option(name = "delay", description = "Delay in milli seconds", mandatory = true)
    private String delayMillis;

    @Option(name = "mcc", description = "Target MCC", display = false)
    String mcc;

    @Option(name = "mnc", description = "Target MNC", display = false)
    String mnc;

    @Builder
    public MmeIdrDiscoveryOptions(IntruderNodeConfig nodeConfig, String targetMmeFileName,
                                  String destinationRealm, String delayMillis, String mcc, String mnc) {
        super(nodeConfig);
        this.targetMmeFileName = Objects.requireNonNullElse(targetMmeFileName, "");
        this.destinationRealm = Objects.requireNonNullElse(destinationRealm, "");
        this.delayMillis = Objects.requireNonNullElse(delayMillis, "500");
        this.mcc = Objects.requireNonNullElse(mcc, "");
        this.mnc = Objects.requireNonNullElse(mnc, "");
    }

    @Override
    public void validate() throws ValidationException {
        Validator.validateWordListFileName(targetMmeFileName, Util.getWordListsDirectory());
        Validator.validateDiameterRealm(destinationRealm);
        Validator.validateDelayTime(delayMillis);
        Validator.validateAddressCode(mcc);
        Validator.validateAddressCode(mnc);
    }
}
