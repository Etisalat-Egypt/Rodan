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

package com.rodan.intruder.ss7.entities.event.dialog;

import com.rodan.intruder.ss7.entities.dialog.Ss7MapDialog;
import com.rodan.intruder.ss7.entities.event.model.error.DialogProviderAbort;
import com.rodan.intruder.ss7.entities.event.model.error.DialogReject;
import com.rodan.intruder.ss7.entities.event.model.error.DialogUserAbort;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public interface MapDialogEventListener {
    Logger logger = LogManager.getLogger(MapDialogEventListener.class);

    default void onDialogDelimiter(Ss7MapDialog dialog) {
        logger.debug("[[[[[[[[[[    onDialogDelimiter      ]]]]]]]]]]");
        logger.debug(dialog);
    }

    default void onDialogProviderAbort(DialogProviderAbort abort) {
        logger.debug("[[[[[[[[[[    onDialogProviderAbort      ]]]]]]]]]]");
        logger.debug(abort);
    }

    default void onDialogUserAbort(DialogUserAbort abort) {
        logger.debug("[[[[[[[[[[    onDialogUserAbort      ]]]]]]]]]]");
        logger.debug(abort);
    }

    default void onDialogReject(DialogReject reject) {
        logger.debug("[[[[[[[[[[    onDialogReject      ]]]]]]]]]]");
        logger.debug(reject);
    }
}
