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

package com.rodan.connectivity.ss7.payloadwrapper.sms;

import com.rodan.connectivity.ss7.adapter.MapAdapter;
import com.rodan.connectivity.ss7.adapter.SccpAdapter;
import com.rodan.connectivity.ss7.payloadwrapper.JSs7PayloadWrapper;
import com.rodan.connectivity.ss7.service.MapCallHandlingService;
import com.rodan.connectivity.ss7.service.MapDialogGenerator;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Payload;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.LMSI;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.service.callhandling.MAPDialogCallHandling;
import org.mobicents.protocols.ss7.map.api.service.sms.MAPDialogSms;

/**
 * @author Ayman ElSherif
 */
@Payload(name = Constants.SRI_SM_RESPONSE_PAYLOAD_NAME)
@ToString(callSuper = true)
public class SriSmResponsePayloadWrapper extends JSs7PayloadWrapper<MapCallHandlingService, MAPDialogSms> {
    @Getter(AccessLevel.PRIVATE) private long invokeId;
    @Getter(AccessLevel.PRIVATE) private String imsi;
    @Getter(AccessLevel.PRIVATE) private String vmscGt;

    @Builder
    public SriSmResponsePayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig,
                                     SccpAdapter sccpAdapter, MapAdapter mapAdapter,
                                     MapDialogGenerator<MAPDialogSms> dialogGenerator,
                                     long invokeId, String imsi, String vmscGt) {
        super(localGt, localSsn, remoteSsn, nodeConfig, sccpAdapter, mapAdapter, dialogGenerator);
        this.invokeId = invokeId;
        this.imsi = imsi;
        this.vmscGt = vmscGt;
    }

    @Override
    public MAPDialogSms generateCarrier() throws SystemException {
        var msg = "ResponsePayload shall NOT generate new dialogs";
        logger.error(msg);
        throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
    }

    @Override
    public void addToCarrier(MAPDialogSms dialog) throws SystemException {
        try {
            validate();

            var mapFactory = getMapAdapter().getParamFactory();
            var vmsc = mapFactory.createISDNAddressString
                    (AddressNature.international_number, NumberingPlan.ISDN, getVmscGt());
            // Uncomment to trigger SMS home routing detection in Intruder
//            var modifiedImsi = options.getImsi().substring(0, options.getImsi().length() - 1);
//            modifiedImsi = modifiedImsi + new Random().nextInt(9);
//            var imsi = mapFactory.createIMSI(modifiedImsi);
            var imsi = mapFactory.createIMSI(getImsi());
//            var lmsi = mapFactory.createLMSI(new byte[]{(byte)0x01,(byte)0x02,(byte)0x03,(byte)0xf4});
            LMSI lmsi = null;
            var location = mapFactory.createLocationInfoWithLMSI(vmsc, lmsi, null, false, null);
            dialog.addSendRoutingInfoForSMResponse(getInvokeId(), imsi, location, null, false,null);

        } catch (MAPException e) {
            var msg = "Failed to add SRI-RESP to dialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).parent(e).build();
        }
    }

    @Override
    protected MAPApplicationContext getApplicationContext() throws SystemException {
        var msg = "ResponsePayload shall NOT generate new dialogs";
        logger.error(msg);
        throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
    }
}
