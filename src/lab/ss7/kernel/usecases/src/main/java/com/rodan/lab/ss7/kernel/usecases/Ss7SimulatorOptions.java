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

package com.rodan.lab.ss7.kernel.usecases;

import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.library.model.config.node.config.NodeConfig;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Ayman ElSherif
 */
public abstract class Ss7SimulatorOptions<T extends NodeConfig> extends Ss7ModuleOptions<T> {
    static final Logger logger = LogManager.getLogger(Ss7SimulatorOptions.class);

    public Ss7SimulatorOptions(T nodeConfig) {
        super(nodeConfig);
    }

    @Override
    public String getSpoofSender()  {
        return "No";
    }
}
