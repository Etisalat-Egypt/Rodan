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
import com.rodan.library.model.config.node.info.SepNodeInfo;
import com.rodan.library.model.config.node.info.StpNodeInfo;
import com.rodan.library.model.error.ValidationException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter @ToString @NoArgsConstructor
public class StpAssociationInfo extends Ss7AssociationInfo<StpNodeInfo> {
    private List<SepNodeInfo> peerNodes;

    @Builder
    public StpAssociationInfo(String routingContext, String networkIndicator, StpNodeInfo localNode, List<SepNodeInfo> peerNodes) {
        super(routingContext, networkIndicator, localNode);
        this.peerNodes = peerNodes;
    }

    public void validate() throws ValidationException {
        super.validate();
        Validator.validateMandatoryCollection(peerNodes, "peerNodes");
        for (var node : peerNodes) {
            Validator.validateMandatoryField(node, "peerNode");
            node.validate();
        }
    }
}
