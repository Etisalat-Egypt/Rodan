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

package com.rodan.intruder.kernel.usecases.model;

import com.rodan.library.model.config.AbstractConfig;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.error.ValidationException;
import com.rodan.intruder.kernel.usecases.SignalingProtocol;
import lombok.Getter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Objects;

public abstract class ModuleOptions<T extends NodeConfig> extends AbstractConfig {
    final static Logger logger = LogManager.getLogger(ModuleOptions.class);

    @Getter protected T nodeConfig;

    public ModuleOptions(T nodeConfig) {
        this.nodeConfig = Objects.requireNonNull(nodeConfig, "nodeConfig cannot be null");
    }

    public static SignalingProtocol getProtocol(String moduleName) throws SystemException {
        SignalingProtocol protocol;
        var str = moduleName.split("/")[1];
        if (str.equalsIgnoreCase("ss7"))
            protocol = SignalingProtocol.SS7;
        else if (str.equalsIgnoreCase("diameter")) {
            protocol = SignalingProtocol.DIAMETER;
        } else {
            String msg = "Invalid module name: " + moduleName;
            logger.error(msg);
            throw SystemException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).build();
        }

        return protocol;
    }

    public abstract void validate() throws ValidationException;
}
