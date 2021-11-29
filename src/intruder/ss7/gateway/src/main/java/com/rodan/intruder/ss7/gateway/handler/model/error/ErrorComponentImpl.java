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

package com.rodan.intruder.ss7.gateway.handler.model.error;

import com.rodan.intruder.ss7.entities.dialog.Ss7MapDialog;
import com.rodan.intruder.ss7.entities.event.model.error.ErrorComponent;
import com.rodan.intruder.ss7.entities.event.model.error.details.NetworkResource;
import com.rodan.intruder.ss7.gateway.dialog.Ss7MapDialogImpl;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.mobicents.protocols.ss7.map.api.MAPDialog;

@Getter @ToString(callSuper = true)
public class ErrorComponentImpl extends ErrorComponent {
    private Ss7MapDialogImpl ss7Dialog;
    private long invokeId;

    @Builder
    public ErrorComponentImpl(String remoteAddress, long errorCode, boolean isEmParameterless, boolean isEmExtensionContainer,
                              boolean isEmFacilityNotSup, boolean isEmSMDeliveryFailure, boolean isEmSystemFailure,
                              NetworkResource networkResource, com.rodan.intruder.ss7.entities.event.model.error.details.AdditionalNetworkResource additionalNetworkResource,
                              boolean isEmUnknownSubscriber, com.rodan.intruder.ss7.entities.event.model.error.details.UnknownSubscriberDiagnostic unknownSubscriberDiagnostic,
                              boolean isEmAbsentSubscriber, com.rodan.intruder.ss7.entities.event.model.error.details.AbsentSubscriberReason absentSubscriberReason,
                              boolean isEmAbsentSubscriberSM, com.rodan.intruder.ss7.entities.event.model.error.details.AbsentSubscriberDiagnosticSM absentSubscriberDiagnosticSM,
                              com.rodan.intruder.ss7.entities.event.model.error.details.AbsentSubscriberDiagnosticSM additionalAbsentSubscriberSMDiagnostic,
                              boolean isEmSubscriberBusyForMtSms, boolean isEmCallBarred, com.rodan.intruder.ss7.entities.event.model.error.details.CallBarringCause callBarringCause,
                              boolean isEmUnauthorizedLCSClient, com.rodan.intruder.ss7.entities.event.model.error.details.UnauthorizedLCSClientDiagnostic unauthorizedLCSClientDiagnostic,
                              boolean isEmPositionMethodFailure, com.rodan.intruder.ss7.entities.event.model.error.details.PositionMethodFailureDiagnostic positionMethodFailureDiagnostic,
                              MAPDialog mapDialog, long invokeId) {
        super(remoteAddress, errorCode, isEmParameterless, isEmExtensionContainer, isEmFacilityNotSup, isEmSMDeliveryFailure, isEmSystemFailure, networkResource, additionalNetworkResource, isEmUnknownSubscriber, unknownSubscriberDiagnostic, isEmAbsentSubscriber, absentSubscriberReason, isEmAbsentSubscriberSM, absentSubscriberDiagnosticSM, additionalAbsentSubscriberSMDiagnostic, isEmSubscriberBusyForMtSms, isEmCallBarred, callBarringCause, isEmUnauthorizedLCSClient, unauthorizedLCSClientDiagnostic, isEmPositionMethodFailure, positionMethodFailureDiagnostic);
        this.ss7Dialog = Ss7MapDialogImpl.builder().jss7Dialog(mapDialog).build();
        this.invokeId = invokeId;
    }

    @Override
    public Ss7MapDialog getDialog() {
        return ss7Dialog;
    }

    @Override
    public long getInvokeId() {
        return invokeId;
    }
}
