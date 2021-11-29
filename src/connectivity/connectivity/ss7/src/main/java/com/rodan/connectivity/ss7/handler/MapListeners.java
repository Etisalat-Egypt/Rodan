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

package com.rodan.connectivity.ss7.handler;

import lombok.Builder;
import lombok.Getter;
import org.mobicents.protocols.ss7.map.api.MAPDialogListener;
import org.mobicents.protocols.ss7.map.api.service.callhandling.MAPServiceCallHandlingListener;
import org.mobicents.protocols.ss7.map.api.service.lsm.MAPServiceLsmListener;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPServiceMobilityListener;
import org.mobicents.protocols.ss7.map.api.service.oam.MAPServiceOamListener;
import org.mobicents.protocols.ss7.map.api.service.pdpContextActivation.MAPServicePdpContextActivationListener;
import org.mobicents.protocols.ss7.map.api.service.sms.MAPServiceSmsListener;

public class MapListeners {
    @Getter private MAPDialogListener dialogEventListener;
    @Getter private MAPServiceMobilityListener mobilityServiceListener;
    @Getter private MAPServiceSmsListener smsServiceListener;
    @Getter private MAPServiceOamListener oamServiceListener;
    @Getter private MAPServicePdpContextActivationListener pdpServiceListener;
    @Getter private MAPServiceLsmListener lcsServiceListener;
    @Getter private MAPServiceCallHandlingListener callHandlingServiceListener;

    @Builder
    public MapListeners(MAPDialogListener dialogEventListener, MAPServiceMobilityListener mobilityServiceListener,
                        MAPServiceSmsListener smsServiceListener, MAPServiceOamListener oamServiceListener,
                        MAPServicePdpContextActivationListener pdpServiceListener, MAPServiceLsmListener lcsServiceListener,
                        MAPServiceCallHandlingListener callHandlingServiceListener) {
        this.dialogEventListener = dialogEventListener;
        this.mobilityServiceListener = mobilityServiceListener;
        this.smsServiceListener = smsServiceListener;
        this.oamServiceListener = oamServiceListener;
        this.pdpServiceListener = pdpServiceListener;
        this.lcsServiceListener = lcsServiceListener;
        this.callHandlingServiceListener = callHandlingServiceListener;
    }
}
