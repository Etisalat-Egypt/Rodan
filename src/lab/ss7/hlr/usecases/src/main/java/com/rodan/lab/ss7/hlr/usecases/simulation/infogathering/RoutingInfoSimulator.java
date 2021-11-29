package com.rodan.lab.ss7.hlr.usecases.simulation.infogathering;

import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.ss7.entities.event.model.call.SriRequest;
import com.rodan.intruder.ss7.entities.event.service.MapCallHandlingServiceListener;
import com.rodan.intruder.ss7.entities.payload.callhandling.SriResponsePayload;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7Gateway;
import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorTemplate;
import com.rodan.lab.ss7.kernel.usecases.Ss7SimulatorConstants;
import com.rodan.lab.ss7.hlr.usecases.model.infogathering.RoutingInfoSimOptions;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.error.ApplicationException;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.notification.NotificationType;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Module(name = Ss7SimulatorConstants.ROUTING_INFO_SIM_NAME)
public class RoutingInfoSimulator extends Ss7SimulatorTemplate implements SignalingModule, MapCallHandlingServiceListener {
    final static Logger logger = LogManager.getLogger(RoutingInfoSimulator.class);

    @Builder
    public RoutingInfoSimulator(Ss7Gateway gateway, Ss7ModuleOptions moduleOptions) {
        super(gateway, moduleOptions);
    }

    @Override
    protected void generatePayload() {
        logger.debug("Generating payload");
        logger.debug("Module Options: " + moduleOptions);
        var options = (RoutingInfoSimOptions) moduleOptions;
        var payload = SriResponsePayload.builder()
                .localGt(options.getNodeConfig().getSs7Association().getLocalNode().getGlobalTitle())
                .imsi(options.getImsi()).msrn(options.getMsrn()).vmscGt(options.getVmscGt())
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
        getGateway().addCallHandlingServiceListener(localSsn,this);
    }

    @Override
    protected void cleanup() throws SystemException {
        super.cleanup();
        if (getGateway() != null && getMainPayload() != null) {
            var localSsn = getMainPayload().getLocalSsn();
            getGateway().removeCallHandlingServiceListener(localSsn, this);
        }
    }

    @Override
    public void onSendRoutingInformationRequest(SriRequest request) {
        try {
            var msisdn = request.getMsisdn();
            var gmscOrGsmScf = request.getGmscOrGsmScfAddress();
            notify("Received SRI request for MSISDN: " + msisdn + " from GMSC: " + gmscOrGsmScf,
                    NotificationType.PROGRESS);

            var dialog = request.getDialog();
            var invokeId = request.getInvokeId();
            dialog.setUserObject(invokeId);
            var payload = (SriResponsePayload) getMainPayload();
            payload.setInvokeId(invokeId);
            getGateway().addToDialog(payload, dialog);
            getGateway().send(dialog);

        } catch (ApplicationException e) {
            String msg = "Failed to handle SRI request";
            logger.error(msg, e);
            notify(msg, NotificationType.FAILURE);
            setExecutionError(true);
        }
    }
}
