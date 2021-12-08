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

package com.rodan.lab.ss7.hlr.usecases.model.infogathering;

import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorOptions;
import com.rodan.library.model.Validator;
import com.rodan.library.model.annotation.Option;
import com.rodan.library.model.config.node.config.LabNodeConfig;
import com.rodan.library.model.error.ValidationException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * @author Ayman ElSherif
 */
@Getter @ToString
public class UlResponderSimOptions extends Ss7SimulatorOptions<LabNodeConfig> {
    @Option(name = "hlr", description = "HLR of target", mandatory = true)
    private String hlrGt;

    @Option(name = "gsmScf", description = "Current gsmSCF of target", mandatory = true)
    private String gsmScfGt;

    @Builder
    public UlResponderSimOptions(LabNodeConfig nodeConfig, String hlrGt, String gsmScfGt) {
        super(nodeConfig);
        this.hlrGt = Objects.requireNonNull(hlrGt);
        this.gsmScfGt = Objects.requireNonNull(gsmScfGt);
    }

    @Override
    public void validate() throws ValidationException {
        Validator.validateHlr(hlrGt);
        Validator.validateGsmScf(gsmScfGt);
    }
}
