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

package com.rodan.intruder.ss7.gateway;

import com.rodan.connectivity.ss7.adapter.JSs7StackAdapter;
import com.rodan.connectivity.ss7.payloadwrapper.JSs7PayloadWrapper;
import com.rodan.connectivity.ss7.payloadwrapper.Jss7CapPayloadWrapper;
import com.rodan.connectivity.ss7.payloadwrapper.callhandling.PrnResponsePayloadWrapper;
import com.rodan.connectivity.ss7.payloadwrapper.callhandling.SriPayloadWrapper;
import com.rodan.connectivity.ss7.payloadwrapper.callhandling.SriResponsePayloadWrapper;
import com.rodan.connectivity.ss7.payloadwrapper.camel.CamelConnectPayloadWrapper;
import com.rodan.connectivity.ss7.payloadwrapper.location.PslPayloadWrapper;
import com.rodan.connectivity.ss7.payloadwrapper.location.PslResponsePayloadWrapper;
import com.rodan.connectivity.ss7.payloadwrapper.location.SriLcsPayloadWrapper;
import com.rodan.connectivity.ss7.payloadwrapper.mobility.*;
import com.rodan.connectivity.ss7.payloadwrapper.oam.SendImsiPayloadWrapper;
import com.rodan.connectivity.ss7.payloadwrapper.packet.SriGprsPayloadWrapper;
import com.rodan.connectivity.ss7.payloadwrapper.sms.*;
import com.rodan.intruder.ss7.entities.payload.Ss7Payload;
import com.rodan.intruder.ss7.entities.payload.callhandling.PnrResponsePayload;
import com.rodan.intruder.ss7.entities.payload.callhandling.SriPayload;
import com.rodan.intruder.ss7.entities.payload.callhandling.SriResponsePayload;
import com.rodan.intruder.ss7.entities.payload.camel.CamelConnectPayload;
import com.rodan.intruder.ss7.entities.payload.location.PslPayload;
import com.rodan.intruder.ss7.entities.payload.location.PslResponsePayload;
import com.rodan.intruder.ss7.entities.payload.location.SriLcsPayload;
import com.rodan.intruder.ss7.entities.payload.mobility.*;
import com.rodan.intruder.ss7.entities.payload.oam.SendImsiPayload;
import com.rodan.intruder.ss7.entities.payload.packet.SriGprsPayload;
import com.rodan.intruder.ss7.entities.payload.sms.*;
import com.rodan.intruder.ss7.gateway.fowrarderpayload.mobility.IsdForwarderPayloadImpl;
import com.rodan.intruder.ss7.gateway.fowrarderpayload.sms.FsmForwarderPayloadImpl;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class PayloadMapper {
    final static Logger logger = LogManager.getLogger(PayloadMapper.class);

    public static JSs7PayloadWrapper mapMapPayload(Ss7Payload payload, JSs7StackAdapter stack, NodeConfig nodeConfig) throws SystemException {
        logger.debug("Mapping Payload to PayloadWrapper");
        logger.debug("Payload: " + payload);
        var mapAdapter = stack.getMapAdapter(payload.getLocalSsn());
        var sccpAdapter = stack.getSccpAdapter();
        JSs7PayloadWrapper jSs7Payload;

        if (payload.getClass() == PnrResponsePayload.class) {
            var pl = (PnrResponsePayload) payload;
            var mainService = mapAdapter.getCallHandlingService();
            jSs7Payload = PrnResponsePayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .invokeId(pl.getInvokeId()).msrn(pl.getMsrn()).vmsc(pl.getVmsc())
                    .build();

        } else if (payload.getClass() == SriPayload.class) {
            var pl = (SriPayload) payload;
            var mainService = mapAdapter.getCallHandlingService();
            jSs7Payload = SriPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .msisdn(pl.getMsisdn()).targetHlrGt(pl.getTargetHlrGt()).mapVersion(pl.getMapVersion())
                    .dialogGenerator(mainService::generateDialog)
                    .build();

        } else if (payload.getClass() == SriResponsePayload.class) {
            var pl = (SriResponsePayload) payload;
            var mainService = mapAdapter.getCallHandlingService();
            jSs7Payload = SriResponsePayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .invokeId(pl.getInvokeId()).imsi(pl.getImsi()).msrn(pl.getMsrn()).vmscGt(pl.getVmscGt())
                    .build();

        } else if (payload.getClass() == PslPayload.class) {
            var pl = (PslPayload) payload;
            var mainService = mapAdapter.getLcsService();
            jSs7Payload = PslPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .msisdn(pl.getMsisdn()).imsi(pl.getImsi()).targetMscGt(pl.getTargetMscGt())
                    .gmlcGt(pl.getGmlcGt()).abuseOpcodeTag(pl.getAbuseOpcodeTag())
                    .mapVersion(pl.getMapVersion())
                    .build();

        } else if (payload.getClass() == PslResponsePayload.class) {
            var pl = (PslResponsePayload) payload;
            var mainService = mapAdapter.getLcsService();
            jSs7Payload = PslResponsePayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .invokeId(pl.getInvokeId())
                    .longitude(pl.getLocationInfo().getLongitude()).latitude(pl.getLocationInfo().getLatitude())
                    .uncertainty(pl.getLocationInfo().getUncertainty()).ageOfLocation(pl.getLocationInfo().getAgeOfLocation())
                    .build();

        } else if (payload.getClass() == SriLcsPayload.class) {
            var pl = (SriLcsPayload) payload;
            var mainService = mapAdapter.getLcsService();
            jSs7Payload = SriLcsPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .msisdn(pl.getMsisdn()).imsi(pl.getImsi()).targetHlrGt(pl.getTargetHlrGt())
                    .gmlcNumber(pl.getGmlcNumber()).mapVersion(pl.getMapVersion())
                    .build();

        } else if (payload.getClass() == AtiPayload.class) {
            var pl = (AtiPayload) payload;
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = AtiPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .msisdn(pl.getMsisdn()).imsi(pl.getImsi()).targetHlrGt(pl.getTargetHlrGt())
                    .gsmSCFGt(pl.getGsmSCFGt()).abuseOpcodeTag(pl.getAbuseOpcodeTag()).malformedAcn(pl.getMalformedAcn())
                    .mapVersion(pl.getMapVersion())

                    .build();

        } else if (payload.getClass() == AtiResponsePayload.class) {
            var pl = (AtiResponsePayload) payload;
            var locationInfo = pl.getLocationInfo();
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = AtiResponsePayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .invokeId(pl.getInvokeId())
                    .imei(pl.getSubscriberInfo().getImei()).subscriberState(pl.getSubscriberInfo().getState())
                    .vlrGt(pl.getVlrGt()).vmscGt(pl.getVmscGt()).mcc(locationInfo.getMcc())
                    .mnc(locationInfo.getMnc()).lac(locationInfo.getLac()).cellId(locationInfo.getCellId())
                    .ageOfLocation(locationInfo.getAgeOfLocation())
                    .build();

        } else if (payload.getClass() == ClPayload.class) {
            var pl = (ClPayload) payload;
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = CLPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .imsi(pl.getImsi()).targetVlrGt(pl.getTargetVlrGt()).spoofHlr(pl.getSpoofHlr())
                    .targetHlrGt(pl.getTargetHlrGt()).mapVersion(pl.getMapVersion())
                    .build();

        } else if (payload.getClass() == DsdPayload.class) {
            var pl = (DsdPayload) payload;
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = DsdPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .imsi(pl.getImsi()).targetVlrGt(pl.getTargetVlrGt()).spoofHlr(pl.getSpoofHlr())
                    .targetHlrGt(pl.getTargetHlrGt()).mapVersion(pl.getMapVersion())
                    .build();

        } else if (payload.getClass() == IsdForwarderPayloadImpl.class) {
            var pl = (IsdForwarderPayloadImpl) payload;
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = IsdForwarderPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .request(pl.getRequest())
                    .build();

        } else if (payload.getClass() == IsdPayload.class) {
            var pl = (IsdPayload) payload;
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = IsdPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .invokeId(pl.getInvokeId()).usage(IsdPayloadWrapper.Usage.getInstance(pl.getUsage().getCode()))
                    .imsi(pl.getImsi()).msisdn(pl.getMsisdn()).gsmScf(pl.getGsmScf()).targetVlrGt(pl.getTargetVlrGt())
                    .barred(pl.getBarred()).spoofHlr(pl.getSpoofHlr()).targetHlrGt(pl.getTargetHlrGt())
                    .mapVersion(pl.getMapVersion())
                    .build();

        } else if (payload.getClass() == IsdResponsePayload.class) {
            var pl = (IsdResponsePayload) payload;
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = IsdResponsePayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .invokeId(pl.getInvokeId()).supportedCamelPhases(pl.getSupportedCamelPhases())
                    .build();

        } else if (payload.getClass() == PsiPayload.class) {
            var pl = (PsiPayload) payload;
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = PsiPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .imsi(pl.getImsi()).targetVlrGt(pl.getTargetVlrGt()).abuseOpcodeTag(pl.getAbuseOpcodeTag())
                    .mapVersion(pl.getMapVersion())
                    .build();

        } else if (payload.getClass() == PsiResponsePayload.class) {
            var pl = (PsiResponsePayload) payload;
            var locationInfo = pl.getLocationInfo();
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = PsiResponsePayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .invokeId(pl.getInvokeId()).imei(pl.getSubscriberInfo().getImei())
                    .subscriberState(pl.getSubscriberInfo().getState()).vlrGt(pl.getVlrGt()).vmscGt(pl.getVmscGt())
                    .mcc(locationInfo.getMcc()).mnc(locationInfo.getMnc()).lac(locationInfo.getLac()).cellId(locationInfo.getCellId())
                    .ageOfLocation(locationInfo.getAgeOfLocation())
                    .build();

        } else if (payload.getClass() == PurgeMsPayload.class) {
            var pl = (PurgeMsPayload) payload;
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = PurgeMsPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .imsi(pl.getImsi()).msisdn(pl.getMsisdn()).targetHlrGt(pl.getTargetHlrGt())
                    .spoofVlr(pl.getSpoofVlr()).targetVlrGt(pl.getTargetVlrGt()).mapVersion(pl.getMapVersion())
                    .build();

        } else if (payload.getClass() == RestoreDataPayload.class) {
            var pl = (RestoreDataPayload) payload;
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = RestoreDataPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .imsi(pl.getImsi()).mapVersion(pl.getMapVersion())
                    .cc(pl.getCc()).ndc(pl.getNdc()).mcc(pl.getMcc()).mnc(pl.getMnc())
                    .build();

        } else if (payload.getClass() == SaiPayload.class) {
            var pl = (SaiPayload) payload;
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = SaiPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .imsi(pl.getImsi()).msisdn(pl.getMsisdn()).targetHlrGt(pl.getTargetHlrGt())
                    .avNumber(pl.getAvNumber()).mapVersion(pl.getMapVersion())
                    .build();

        } else if (payload.getClass() == SaiResponsePayload.class) {
            var pl = (SaiResponsePayload) payload;
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = SaiResponsePayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .invokeId(pl.getInvokeId()).imsi(pl.getImsi()).requestingNodeType(pl.getRequestingNodeType())
                    .rand(pl.getRand()).sres(pl.getSres()).kc(pl.getKc()).xres(pl.getXres()).authPs(pl.getAuthPs()).kasme(pl.getKasme())
                    .build();

        } else if (payload.getClass() == SendIdentificationPayload.class) {
            var pl = (SendIdentificationPayload) payload;
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = SendIdentificationPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .tmsi(pl.getTmsi()).targetVlrGt(pl.getTargetVlrGt()).avNumber(pl.getAvNumber())
                    .mapVersion(pl.getMapVersion())
                    .build();

        } else if (payload.getClass() == UlPayload.class) {
            var pl = (UlPayload) payload;
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = UlPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .imsi(pl.getImsi()).currentMscGt(pl.getCurrentMscGt()).currentVlrGt(pl.getCurrentVlrGt())
                    .newMscGt(pl.getNewMscGt()).newVlrGt(pl.getNewVlrGt()).msrn(pl.getMsrn())
                    .forwardSmsToVictim(pl.getForwardSmsToVictim()).hlrGt(pl.getHlrGt()).mapVersion(pl.getMapVersion())
                    .cc(pl.getCc()).ndc(pl.getNdc()).mcc(pl.getMcc()).mnc(pl.getMnc())
                    .build();

        } else if (payload.getClass() == UlResponsePayload.class) {
            var pl = (UlResponsePayload) payload;
            var mainService = mapAdapter.getMobilityService();
            jSs7Payload = UlResponsePayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .invokeId(pl.getInvokeId())
                    .hlrGt(pl.getHlrGt())
                    .build();

        } else if (payload.getClass() == SendImsiPayload.class) {
            var pl = (SendImsiPayload) payload;
            var mainService = mapAdapter.getOamService();
            jSs7Payload = SendImsiPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .msisdn(pl.getMsisdn()).targetHlrGt(pl.getTargetHlrGt()).abuseOpcodeTag(pl.getAbuseOpcodeTag())
                    .malformedAcn(pl.getMalformedAcn()).mapVersion(pl.getMapVersion())
                    .build();

        } else if (payload.getClass() == SriGprsPayload.class) {
            var pl = (SriGprsPayload) payload;
            var mainService = mapAdapter.getPdpService();
            jSs7Payload = SriGprsPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .imsi(pl.getImsi()).msisdn(pl.getMsisdn()).targetHlrGt(pl.getTargetHlrGt())
                    .ggsnNumber(pl.getGgsnNumber()).mapVersion(pl.getMapVersion())
                    .build();

        } else if (payload.getClass() == FsmForwarderPayloadImpl.class) {
            var pl = (FsmForwarderPayloadImpl) payload;
            var mainService = mapAdapter.getSmsService();
            jSs7Payload = FsmForwarderPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .request(pl.getRequest())
                    .build();

        } else if (payload.getClass() == FsmPayload.class) {
            var pl = (FsmPayload) payload;
            var mainService = mapAdapter.getSmsService();
            jSs7Payload = FsmPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .imsi(pl.getImsi()).sender(pl.getSender()).targetMscGt(pl.getTargetMscGt())
                    .content(pl.getContent()).messageType(pl.getMessageType()).spoofSmsc(pl.getSpoofSmsc())
                    .smscGt(pl.getSmscGt()).mapVersion(pl.getMapVersion())
                    .build();

        } else if (payload.getClass() == FsmResponsePayload.class) {
            var pl = (FsmResponsePayload) payload;
            var mainService = mapAdapter.getSmsService();
            jSs7Payload = FsmResponsePayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .invokeId(pl.getInvokeId())
                    .build();

        } else if (payload.getClass() == MtFsmResponsePayload.class) {
            var pl = (MtFsmResponsePayload) payload;
            var mainService = mapAdapter.getSmsService();
            jSs7Payload = MtFsmResponsePayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .invokeId(pl.getInvokeId())
                    .build();

        } else if (payload.getClass() == ReportSmDeliveryStatusPayload.class) {
            var pl = (ReportSmDeliveryStatusPayload) payload;
            var mainService = mapAdapter.getSmsService();
            jSs7Payload = ReportSmDeliveryStatusPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .msisdn(pl.getMsisdn()).targetHlrGt(pl.getTargetHlrGt()).smscGt(pl.getSmscGt())
                    .mapVersion(pl.getMapVersion())
                    .build();

        } else if (payload.getClass() == SriSmPayload.class) {
            var pl = (SriSmPayload) payload;
            var mainService = mapAdapter.getSmsService();
            jSs7Payload = SriSmPayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .msisdn(pl.getMsisdn()).imsi(pl.getImsi()).targetHlrGt(pl.getTargetHlrGt())
                    .smscGt(pl.getSmscGt()).detectSmsHomeRouting(pl.getDetectSmsHomeRouting())
                    .bypassSmsHomeRouting(pl.getBypassSmsHomeRouting()).abuseOpcodeTag(pl.getAbuseOpcodeTag())
                    .malformedAcn(pl.getMalformedAcn()).malformedAcn(pl.getMalformedAcn()).mapVersion(pl.getMapVersion())
                    .cc(pl.getCc()).ndc(pl.getNdc()).mcc(pl.getMcc()).mnc(pl.getMnc())
                    .build();

        } else if (payload.getClass() == SriSmResponsePayload.class) {
            var pl = (SriSmResponsePayload) payload;
            var mainService = mapAdapter.getSmsService();
            jSs7Payload = SriSmResponsePayloadWrapper.builder()
                    .mapAdapter(mapAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .dialogGenerator(mainService::generateDialog).invokeId(pl.getInvokeId())
                    .imsi(pl.getImsi()).vmscGt(pl.getVmscGt())
                    .build();

        } else {
            String msg = "Invalid payload class: " + payload.getClass();
            logger.error(msg);
            throw SystemException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).build();
        }

        logger.debug("PayloadWrapper: " + jSs7Payload);
        return jSs7Payload;
    }

    public static Jss7CapPayloadWrapper mapCapPayload(Ss7Payload payload, JSs7StackAdapter stack,
                                                               NodeConfig nodeConfig) throws SystemException {
        logger.debug("Mapping Payload to PayloadWrapper");
        logger.debug("Payload: " + payload);
        var capAdapter = stack.getCapAdapter(payload.getLocalSsn());
        var sccpAdapter = stack.getSccpAdapter();
        CamelConnectPayloadWrapper jSs7Payload;

        if (payload.getClass() == CamelConnectPayload.class) {
            var pl = (CamelConnectPayload) payload;
            var mainService = capAdapter.getCsCallHandlingService();
            jSs7Payload = CamelConnectPayloadWrapper.builder()
                    .capAdapter(capAdapter).sccpAdapter(sccpAdapter).nodeConfig(nodeConfig)
                    .dialogGenerator(mainService::generateDialog)
                    .localGt(pl.getLocalGt()).localSsn(pl.getLocalSsn()).remoteSsn(pl.getRemoteSsn())
                    .msisdn(pl.getMsisdn()).targetMscGt(pl.getTargetMscGt()).capVersion(pl.getCapVersion())
                    .build();

        } else {
            String msg = "Invalid payload class: " + payload.getClass();
            logger.error(msg);
            throw SystemException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).build();
        }

        logger.debug("PayloadWrapper: " + jSs7Payload);
        return jSs7Payload;
    }
}
