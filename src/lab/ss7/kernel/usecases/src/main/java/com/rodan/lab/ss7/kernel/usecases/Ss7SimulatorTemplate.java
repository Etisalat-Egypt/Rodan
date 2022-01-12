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

package com.rodan.lab.ss7.kernel.usecases;

import com.rodan.intruder.ss7.entities.event.model.MapMessage;
import com.rodan.intruder.ss7.entities.event.model.details.TcapMessageType;
import com.rodan.intruder.ss7.usecases.Ss7ModuleTemplate;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.ApplicationException;
import com.rodan.library.model.notification.NotificationType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class Ss7SimulatorTemplate extends Ss7ModuleTemplate {
    protected static final int SIMULATOR_SLEEP_TIME = 200;

    private final static Logger logger = LogManager.getLogger(Ss7SimulatorTemplate.class);

    public Ss7SimulatorTemplate(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void execute() throws ApplicationException {
        var msgName = getClass().getAnnotation(Module.class).name();
        msgName = msgName.substring(msgName.lastIndexOf("/") + 1).toUpperCase();
        notify("Simulating responses for " + msgName + " messages...", NotificationType.PROGRESS);
        try {
            while (true) {
                if (getMainPayload() != null) {

                }
                Thread.sleep(SIMULATOR_SLEEP_TIME);
            }
        } catch (Exception e) {
            logger.error("Failed to simulate " + msgName + " response!", e);
        }

        logger.debug("Simulating completed successfully!");
    }

    protected boolean isDoubleMapBypassUsed(MapMessage message) {
        return message.getDialog().getTcapMessageType().equals(TcapMessageType.Continue);
    }
}
