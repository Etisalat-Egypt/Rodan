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

package com.rodan.connectivity.ss7.payloadwrapper;

import com.rodan.connectivity.ss7.adapter.MapAdapter;
import com.rodan.connectivity.ss7.adapter.SccpAdapter;
import com.rodan.connectivity.ss7.service.MapDialogGenerator;
import com.rodan.connectivity.ss7.service.MapService;
import com.rodan.library.model.Constants;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.util.Util;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.indicator.NatureOfAddress;
import org.mobicents.protocols.ss7.indicator.NumberingPlan;
import org.mobicents.protocols.ss7.map.MAPDialogImpl;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.sccp.parameter.EncodingScheme;

import java.util.Arrays;
import java.util.List;

@ToString
public abstract class JSs7PayloadWrapper<SVC extends MapService, DLG extends MAPDialog> implements Jss7Payload<DLG> {
    protected final static Logger logger = LogManager.getLogger(JSs7PayloadWrapper.class);

    @Getter(AccessLevel.PROTECTED) private String localGt;
    @Getter private int localSsn;
    @Getter(AccessLevel.PROTECTED) private int remoteSsn;

    @Getter(AccessLevel.PROTECTED) private NodeConfig nodeConfig;
    @Setter @Getter(AccessLevel.PROTECTED) private SccpAdapter sccpAdapter;
    @Setter @Getter(AccessLevel.PROTECTED) private MapAdapter mapAdapter;

    @Setter protected MapDialogGenerator<DLG> dialogGenerator;

    public static final int TRANSLATION_TYPE = Constants.SCCP_TRANSLATION_TYPE_NOT_USED;
    public static final EncodingScheme ENCODING_SCHEME = null; // use default encoding scheme (even/odd BCD)
    // ISDN_TELEPHONY (E.164): CC:NDC:SN
    // LAND_MOBILE (E.212): MCC:MNC:MSIN
    // ISDN_MOBILE (E.214): CC:NDC:MSIN
    public static final NumberingPlan ISDN_TELEPHONY_INDICATOR = NumberingPlan.ISDN_TELEPHONY;
    public static final NumberingPlan ISDN_MOBILE_INDICATOR = NumberingPlan.ISDN_MOBILE;
    public static final org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan ISDN_TELEPHONY_PLAN =
            org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN;

    public static final NatureOfAddress NATURE_OF_ADDRESS = NatureOfAddress.INTERNATIONAL;
    public static final AddressNature ADDRESS_NATURE = AddressNature.international_number;

    public JSs7PayloadWrapper(String localGt, int localSsn, int remoteSsn, NodeConfig nodeConfig, SccpAdapter sccpAdapter,
                              MapAdapter mapAdapter, MapDialogGenerator<DLG> dialogGenerator) {
        this.localGt = localGt;
        this.localSsn = localSsn;
        this.remoteSsn = remoteSsn;
        this.nodeConfig = nodeConfig;
        this.sccpAdapter = sccpAdapter;
        this.mapAdapter = mapAdapter;
        this.dialogGenerator = dialogGenerator;
    }

    public abstract void addToCarrier(DLG dialog) throws SystemException;

    protected void validate() throws SystemException {
        // TODO IMP: Validate payloadOptions
        if (nodeConfig == null || mapAdapter == null || sccpAdapter == null) {
            String msg = "Invalid payload parameters! mapAdapter: [" + mapAdapter + "], sccpAdapter: [" + sccpAdapter +
                    "], nodeConfig: [" + nodeConfig + "].";
            throw SystemException.builder().code(ErrorCode.INVALID_PAYLOAD_PARAMS).message(msg).build();
        }
    }

    public void send(DLG dialog) throws SystemException {
        try {
            dialog.send();

        } catch (MAPException e) {
            var msg = "Failed to send MAP dialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MAP_DIALOG_SEND_FAILED).message(msg).parent(e).build();
        }
    }

    public void sendMalformedAcn(MAPDialog dialog) throws SystemException {
        try {
            ((MAPDialogImpl) dialog).sendMalformedAcn();

        } catch (MAPException e) {
            var msg = "Failed to send MAP dialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MAP_DIALOG_SEND_FAILED).message(msg).parent(e).build();
        }
    }

    // TODO TRX: rename
    public void sendWithOriginalAcn(MAPDialog dialog) throws SystemException {
        try {
            // ctn is only sent during TCAP-Begin, subsequent messages in the same dialog
            // does not include ctn
            ((MAPDialogImpl) dialog).send(getApplicationContext());

        } catch (MAPException e) {
            var msg = "Failed to send MAP dialog";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.MAP_DIALOG_SEND_FAILED).message(msg).parent(e).build();
        }
    }

    protected abstract MAPApplicationContext getApplicationContext() throws SystemException;

    protected MAPApplicationContextVersion getAcVersion(int requiredVersion, Integer[] supportedVersions) throws SystemException {
        List<Integer> list = Arrays.asList(supportedVersions);
        var version = list.stream().filter(i -> i == requiredVersion).mapToInt(i -> i).findAny().orElse(0);
        var mapContextVersion = MAPApplicationContextVersion.getInstance(version);
        if (mapContextVersion == null) {
            String msg = "Unsupported MAP version: " + requiredVersion;
            logger.error(msg);
            throw SystemException.builder().code(ErrorCode.INVALID_PAYLOAD_PARAMS).message(msg).build();
        }
        return mapContextVersion;
    }

    protected MAPApplicationContextVersion getComptableAcVersion(int preferredVersion, Integer[] supportedVersions) throws SystemException {
        int version = Util.getLargest(supportedVersions, preferredVersion).orElse(0);
        var mapContextVersion = MAPApplicationContextVersion.getInstance(version);
        if (mapContextVersion == null) {
            String msg = "Unsupported MAP version: " + preferredVersion;
            logger.error(msg);
            throw SystemException.builder().code(ErrorCode.INVALID_PAYLOAD_PARAMS).message(msg).build();
        }
        return mapContextVersion;
    }
}
