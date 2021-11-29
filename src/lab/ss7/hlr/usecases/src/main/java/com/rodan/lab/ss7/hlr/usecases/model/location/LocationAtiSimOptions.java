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

package com.rodan.lab.ss7.hlr.usecases.model.location;

import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorOptions;
import com.rodan.library.model.Validator;
import com.rodan.library.model.annotation.Option;
import com.rodan.library.model.config.node.config.LabNodeConfig;
import com.rodan.library.model.error.ValidationException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Ayman ElSherif
 */
@Getter @ToString
public class LocationAtiSimOptions extends Ss7SimulatorOptions<LabNodeConfig> {
    @Option(name = "imei", description = "Current IMEI of target", mandatory = true)
    private String imei;

    @Option(name = "vmsc", description = "Current VMSC of target", mandatory = true)
    private String vmscGt;

    @Option(name = "vlr", description = "Current VLR of target", mandatory = true)
    private String vlrGt;

    @Builder
    public LocationAtiSimOptions(LabNodeConfig nodeConfig, String imei, String vmscGt, String vlrGt) {
        super(nodeConfig);
        this.imei = imei;
        this.vmscGt = vmscGt;
        this.vlrGt = vlrGt;
    }

    @Override
    public void validate() throws ValidationException {
        Validator.validateImei(imei);
        Validator.validateMsc(vmscGt);
        Validator.validateVlr(vlrGt);
    }
}
