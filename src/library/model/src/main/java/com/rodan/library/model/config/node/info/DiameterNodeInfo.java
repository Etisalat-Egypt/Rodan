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

package com.rodan.library.model.config.node.info;

import com.rodan.library.model.error.ValidationException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter @ToString @NoArgsConstructor
public abstract class DiameterNodeInfo extends NodeInfo {
    private DiameterAppInfo appInfo;
    public DiameterNodeInfo(String nodeName, String address, String port, DiameterAppInfo appInfo) {
        super(nodeName, address, port);
        this.appInfo = appInfo;
    }

    public void validate() throws ValidationException {
        super.validate();
        // TODO IMP: Implement
    }
}
