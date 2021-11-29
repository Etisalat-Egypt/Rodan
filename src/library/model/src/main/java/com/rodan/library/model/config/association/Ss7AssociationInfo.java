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

package com.rodan.library.model.config.association;

import com.rodan.library.model.Validator;
import com.rodan.library.model.config.AbstractConfig;
import com.rodan.library.model.config.node.info.Ss7NodeInfo;
import com.rodan.library.model.error.ValidationException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter @ToString @NoArgsConstructor
public abstract class Ss7AssociationInfo<T extends Ss7NodeInfo> extends AbstractConfig {
    private String routingContext;
    private String networkIndicator;
    private T localNode;

    public Ss7AssociationInfo(String routingContext, String networkIndicator, T localNode) {
        this.routingContext = routingContext;
        this.networkIndicator = networkIndicator;
        this.localNode = localNode;
    }

    public void validate() throws ValidationException {
        Validator.validateRoutingContext(routingContext);
        Validator.validateNetworkIndicator(networkIndicator);
        Validator.validateMandatoryField(localNode, "localNode");
        localNode.validate();
    }
}
