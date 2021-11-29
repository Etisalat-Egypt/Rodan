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

package com.rodan.intruder.ss7.entities.event.model.error;

import com.rodan.intruder.ss7.entities.event.model.MapMessage;
import com.rodan.intruder.ss7.entities.event.model.error.details.NetworkResource;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public abstract class ErrorComponent implements MapMessage {
    private String remoteAddress;
    private long errorCode;
    private boolean isEmParameterless;
    private boolean isEmExtensionContainer;
    private boolean isEmFacilityNotSup;
    private boolean isEmSMDeliveryFailure;
    private boolean isEmSystemFailure;
    private com.rodan.intruder.ss7.entities.event.model.error.details.NetworkResource networkResource;
    private com.rodan.intruder.ss7.entities.event.model.error.details.AdditionalNetworkResource additionalNetworkResource;
    private boolean isEmUnknownSubscriber;
    private com.rodan.intruder.ss7.entities.event.model.error.details.UnknownSubscriberDiagnostic unknownSubscriberDiagnostic;
    private boolean isEmAbsentSubscriber;
    private com.rodan.intruder.ss7.entities.event.model.error.details.AbsentSubscriberReason absentSubscriberReason;
    private boolean isEmAbsentSubscriberSM;
    private com.rodan.intruder.ss7.entities.event.model.error.details.AbsentSubscriberDiagnosticSM absentSubscriberDiagnosticSM;
    private com.rodan.intruder.ss7.entities.event.model.error.details.AbsentSubscriberDiagnosticSM additionalAbsentSubscriberSMDiagnostic;
    private boolean isEmSubscriberBusyForMtSms;
    private boolean isEmCallBarred;
    private com.rodan.intruder.ss7.entities.event.model.error.details.CallBarringCause callBarringCause;
    private boolean isEmUnauthorizedLCSClient;
    private com.rodan.intruder.ss7.entities.event.model.error.details.UnauthorizedLCSClientDiagnostic unauthorizedLCSClientDiagnostic;
    private boolean isEmPositionMethodFailure;
    private com.rodan.intruder.ss7.entities.event.model.error.details.PositionMethodFailureDiagnostic positionMethodFailureDiagnostic;

    public ErrorComponent(String remoteAddress, long errorCode, boolean isEmParameterless, boolean isEmExtensionContainer,
                          boolean isEmFacilityNotSup, boolean isEmSMDeliveryFailure, boolean isEmSystemFailure,
                          NetworkResource networkResource, com.rodan.intruder.ss7.entities.event.model.error.details.AdditionalNetworkResource additionalNetworkResource,
                          boolean isEmUnknownSubscriber, com.rodan.intruder.ss7.entities.event.model.error.details.UnknownSubscriberDiagnostic unknownSubscriberDiagnostic,
                          boolean isEmAbsentSubscriber, com.rodan.intruder.ss7.entities.event.model.error.details.AbsentSubscriberReason absentSubscriberReason,
                          boolean isEmAbsentSubscriberSM, com.rodan.intruder.ss7.entities.event.model.error.details.AbsentSubscriberDiagnosticSM absentSubscriberDiagnosticSM,
                          com.rodan.intruder.ss7.entities.event.model.error.details.AbsentSubscriberDiagnosticSM additionalAbsentSubscriberSMDiagnostic,
                          boolean isEmSubscriberBusyForMtSms, boolean isEmCallBarred, com.rodan.intruder.ss7.entities.event.model.error.details.CallBarringCause callBarringCause,
                          boolean isEmUnauthorizedLCSClient, com.rodan.intruder.ss7.entities.event.model.error.details.UnauthorizedLCSClientDiagnostic unauthorizedLCSClientDiagnostic,
                          boolean isEmPositionMethodFailure, com.rodan.intruder.ss7.entities.event.model.error.details.PositionMethodFailureDiagnostic positionMethodFailureDiagnostic) {
        this.remoteAddress = remoteAddress;
        this.errorCode = errorCode;
        this.isEmParameterless = isEmParameterless;
        this.isEmExtensionContainer = isEmExtensionContainer;
        this.isEmFacilityNotSup = isEmFacilityNotSup;
        this.isEmSMDeliveryFailure = isEmSMDeliveryFailure;
        this.isEmSystemFailure = isEmSystemFailure;
        this.networkResource = networkResource;
        this.additionalNetworkResource = additionalNetworkResource;
        this.isEmUnknownSubscriber = isEmUnknownSubscriber;
        this.unknownSubscriberDiagnostic = unknownSubscriberDiagnostic;
        this.isEmAbsentSubscriber = isEmAbsentSubscriber;
        this.absentSubscriberReason = absentSubscriberReason;
        this.isEmAbsentSubscriberSM = isEmAbsentSubscriberSM;
        this.absentSubscriberDiagnosticSM = absentSubscriberDiagnosticSM;
        this.additionalAbsentSubscriberSMDiagnostic = additionalAbsentSubscriberSMDiagnostic;
        this.isEmSubscriberBusyForMtSms = isEmSubscriberBusyForMtSms;
        this.isEmCallBarred = isEmCallBarred;
        this.callBarringCause = callBarringCause;
        this.isEmUnauthorizedLCSClient = isEmUnauthorizedLCSClient;
        this.unauthorizedLCSClientDiagnostic = unauthorizedLCSClientDiagnostic;
        this.isEmPositionMethodFailure = isEmPositionMethodFailure;
        this.positionMethodFailureDiagnostic = positionMethodFailureDiagnostic;
    }

    public String getReadableError() {
        var msg = new StringBuilder();
        msg.append("Code: ").append(errorCode).append(", Type: ");
        if (isEmParameterless)
            msg.append("Parameterless ");
        if (isEmExtensionContainer)
            msg.append("ExtensionContainer ");
        if (isEmFacilityNotSup)
            msg.append("FacilityNotSup ");
        if (isEmSMDeliveryFailure)
            msg.append("SMDeliveryFailure ");
        if (isEmSystemFailure)
            msg.append("SystemFailure ").append("networkResource: ").append(networkResource)
                    .append(", additionalNetworkResource: ").append(additionalNetworkResource);
        if (isEmUnknownSubscriber)
            msg.append("UnknownSubscriber ").append("unknownSubscriberDiagnostic: ").append(unknownSubscriberDiagnostic);
        if (isEmAbsentSubscriberSM)
            msg.append("AbsentSubscriberSM ").append("absentSubscriberDiagnosticSM: ").append(absentSubscriberDiagnosticSM)
                    .append(", additionalAbsentSubscriberSMDiagnostic").append(additionalAbsentSubscriberSMDiagnostic);
        if (isEmAbsentSubscriber)
            msg.append("AbsentSubscriber ");
        if (isEmSubscriberBusyForMtSms)
            msg.append("SubscriberBusyForMtSms ");
        if (isEmCallBarred)
            msg.append("CallBarred ").append("callBarringCause: ").append(callBarringCause);
        if (isEmUnauthorizedLCSClient)
            msg.append("UnauthorizedLCSClient ").append("unauthorizedLCSClientDiagnostic: ").append(unauthorizedLCSClientDiagnostic);
        if (isEmPositionMethodFailure)
            msg.append("PositionMethodFailure ").append("positionMethodFailureDiagnostic: ").append(positionMethodFailureDiagnostic);

        return msg.toString();
    }
}
