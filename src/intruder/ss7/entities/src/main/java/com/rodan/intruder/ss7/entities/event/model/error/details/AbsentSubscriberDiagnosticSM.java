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

package com.rodan.intruder.ss7.entities.event.model.error.details;

public enum AbsentSubscriberDiagnosticSM {
    NoPagingResponseViaTheMSC(0), IMSIDetached(1), RoamingRestriction(2), DeregisteredInTheHLRForNonGPRS(3), MSPurgedForNonGPRS(
            4), NoPagingResponseViaTheSGSN(5), GPRSDetached(6), DeregisteredInTheHLRForGPRS(7), MSPurgedForGPRS(8), UnidentifiedSubscriberViaTheMSC(
            9), UnidentifiedSubscriberViaTheSGSN(10), DeregisteredInTheHSS_HLRForIMS(11), NoResponseViaTheIP_SM_GW(12);

    private int code;

    private AbsentSubscriberDiagnosticSM(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AbsentSubscriberDiagnosticSM getInstance(int code) {
        switch (code) {
            case 0:
                return NoPagingResponseViaTheMSC;
            case 1:
                return IMSIDetached;
            case 2:
                return RoamingRestriction;
            case 3:
                return DeregisteredInTheHLRForNonGPRS;
            case 4:
                return MSPurgedForNonGPRS;
            case 5:
                return NoPagingResponseViaTheSGSN;
            case 6:
                return GPRSDetached;
            case 7:
                return DeregisteredInTheHLRForGPRS;
            case 8:
                return MSPurgedForGPRS;
            case 9:
                return UnidentifiedSubscriberViaTheMSC;
            case 10:
                return UnidentifiedSubscriberViaTheSGSN;
            case 11:
                return DeregisteredInTheHSS_HLRForIMS;
            case 12:
                return NoResponseViaTheIP_SM_GW;
            default:
                return null;
        }
    }
}
