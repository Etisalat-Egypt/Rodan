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
public class LocalDiameterNodeInfo extends DiameterNodeInfo {
    private String hostname;
    private String realm;

    public LocalDiameterNodeInfo(String nodeName, String address, String port, DiameterAppInfo appInfo, String hostname, String realm) {
        super(nodeName, address, port, appInfo);
        this.hostname = hostname;
        this.realm = realm;
    }

    public void validate() throws ValidationException {
        super.validate();
        // TODO IMP: Implement
    }
}
