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

package com.rodan.library.model.config.node.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rodan.library.model.Validator;
import com.rodan.library.model.config.AbstractConfig;
import com.rodan.library.model.config.TargetNetworkInfo;
import com.rodan.library.model.config.association.Ss7AssociationInfo;
import com.rodan.library.model.config.node.SctpMode;
import com.rodan.library.model.error.ValidationException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter @ToString @NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "function")
@JsonSubTypes({
        @Type(value = StpNodeConfig.class, name = "STP"),
        @Type(value = LabNodeConfig.class, name = "Lab"),
        @Type(value = IntruderNodeConfig.class, name = "Intruder")
})
public abstract class NodeConfig<ASCN extends Ss7AssociationInfo> extends AbstractConfig {
    // TODO IMP: check if "extends AbstractConfig" can be removed from class definition
    private SctpMode sctpMode; // TODO move to ss7Association (each asc should have it's mode)
    private ASCN ss7Association;
    private TargetNetworkInfo targetNetwork;

    public NodeConfig(SctpMode sctpMode, ASCN ss7Association, TargetNetworkInfo targetNetwork) {
        this.sctpMode = sctpMode;
        this.ss7Association = ss7Association;
        this.targetNetwork = targetNetwork;
    }

    public void validate() throws ValidationException {
        Validator.validateMandatoryField(sctpMode, "sctpMode");
        Validator.validateMandatoryField(ss7Association, "associationInfo");
        ss7Association.validate();
        if (targetNetwork != null)
            targetNetwork.validate();
    }
}
