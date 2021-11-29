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

package com.rodan.intruder.ss7.entities.payload;

import com.rodan.intruder.kernel.entities.payload.SignalingPayload;
import com.rodan.library.model.config.node.config.SepNodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import lombok.Getter;
import lombok.ToString;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Getter @ToString
public abstract class Ss7Payload extends SignalingPayload {
    protected final static Logger logger = LogManager.getLogger(Ss7Payload.class);

    private String localGt;
    private int localSsn;
    private int remoteSsn;

    public Ss7Payload(String localGt, int localSsn, int remoteSsn) {
        this.localGt = localGt;
        this.localSsn = localSsn;
        this.remoteSsn = remoteSsn;
    }

    public abstract boolean isAbuseOpcodeTagForBypass();

    public abstract boolean isMalformedAcnForBypass();

    public abstract String getPayloadName();

    public static Ss7Payload create(String payloadName, SepNodeConfig appOptions) throws SystemException {
        if (!(appOptions instanceof SepNodeConfig)) {
            String msg = "STP Node cannot have MAP payloads";
            logger.error(msg);
            throw SystemException.builder().code(ErrorCode.INVALID_NODE_FUNCTION).message(msg).build();
        }
        var targetNetworkInfo = appOptions.getTargetNetwork();
        var localGt = appOptions.getSs7Association().getLocalNode().getGlobalTitle();
        var targetSubscriberInfo = appOptions.getTargetSubscriberInfo();

//        Ss7PayloadOptions options = switch (payloadName) {
//            case Constants.SRI_PAYLOAD_NAME -> SriPayloadOptions.builder()
//                    .msisdn(targetSubscriberInfo.getMsisdn())
//                    .targetHlrGt(targetNetworkInfo.getHlrGt())
//                    .build();
//            case Constants.SRI_SM_PAYLOAD_NAME -> SriSmPayloadOptions.builder()
//                    .msisdn(targetSubscriberInfo.getMsisdn())
//                    .imsi(targetSubscriberInfo.getImsi())
//                    .targetHlrGt(targetNetworkInfo.getHlrGt())
//                    .cc(targetNetworkInfo.getCc())
//                    .ndc(targetNetworkInfo.getNdc())
//                    .mcc(targetNetworkInfo.getMcc())
//                    .mnc(targetNetworkInfo.getMnc())
//                    .build();
//
//            default -> {
//                String msg = "Invalid SS7 payload name: " + payloadName;
//                logger.error(msg);
//                throw SystemException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).build();
//            }
//        };
//
//        options.setLocalGt(localGt);
//
//        return options;

        return null;
    }
}
