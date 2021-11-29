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

package com.rodan.connectivity.ss7.adapter;

import com.rodan.connectivity.ss7.handler.CapListeners;
import com.rodan.connectivity.ss7.handler.MapListeners;
import com.rodan.connectivity.ss7.payloadwrapper.JSs7PayloadWrapper;
import com.rodan.connectivity.ss7.payloadwrapper.Jss7CapPayloadWrapper;
import com.rodan.library.model.Constants;
import com.rodan.library.model.config.node.config.NodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.error.ValidationException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.cap.api.CAPDialog;
import org.mobicents.protocols.ss7.cap.api.CAPDialogListener;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CAPServiceCircuitSwitchedCallListener;
import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPDialogListener;
import org.mobicents.protocols.ss7.map.api.service.callhandling.MAPServiceCallHandlingListener;
import org.mobicents.protocols.ss7.map.api.service.lsm.MAPServiceLsmListener;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPServiceMobilityListener;
import org.mobicents.protocols.ss7.map.api.service.oam.MAPServiceOamListener;
import org.mobicents.protocols.ss7.map.api.service.pdpContextActivation.MAPServicePdpContextActivationListener;
import org.mobicents.protocols.ss7.map.api.service.sms.MAPServiceSmsListener;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter @ToString
public class JSs7StackAdapter {
	final static Logger logger = LogManager.getLogger(JSs7StackAdapter.class);

	private NodeConfig nodeConfig;
	private SctpAdapter sctpAdapter;
	private M3uaAdapter m3uaAdapter;
	private SccpAdapter sccpAdapter;

	private Map<Integer, MapAdapter> mapAdapterCache;
	private Map<Integer, MapListeners> mapListeners;

	private Map<Integer, CapAdapter> capAdapterCache;
	private Map<Integer, CapListeners> capListeners;


	public JSs7StackAdapter(NodeConfig nodeConfig) {
		this.nodeConfig = nodeConfig;

		this.mapAdapterCache = new HashMap<>();
		this.mapListeners = new HashMap<>();

		this.capAdapterCache = new HashMap<>();
		this.capListeners = new HashMap<>();
	}

	public void addMapAdapter(int ssn, MapAdapter adapter) throws SystemException {
		var existingAdapter = this.mapAdapterCache.get(ssn);
		if (existingAdapter != null) {
			var msg = "A MAP adapter is already cached for SSN: " + ssn;
			throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
		}
		this.mapAdapterCache.put(ssn, adapter);
	}

	public MapAdapter getMapAdapter(int ssn) throws SystemException {
		if (Constants.SCCP_INTEGRATED_VLR && ssn == Constants.SCCP_VLR_SSN) {
			ssn = Constants.SCCP_MSC_SSN;
		}
		var adapter = this.mapAdapterCache.get(ssn);
		if (adapter == null) {
			var msg = "No cached MAP adapter for SSN: " + ssn;
			throw SystemException.builder().code(ErrorCode.MAP_INITIALIZATION).message(msg).build();
		}

		return adapter;
	}

	public void addCapAdapter(int ssn, CapAdapter adapter) throws SystemException {
		var existingAdapter = this.mapAdapterCache.get(ssn);
		if (existingAdapter != null) {
			var msg = "A CAP adapter is already cached for SSN: " + ssn;
			throw SystemException.builder().code(ErrorCode.CAP_INITIALIZATION).message(msg).build();
		}
		this.capAdapterCache.put(ssn, adapter);
	}

	public CapAdapter getCapAdapter(int ssn) throws SystemException {
		var adapter = this.capAdapterCache.get(ssn);
		if (adapter == null) {
			var msg = "No cached CAP adapter for SSN: " + ssn;
			throw SystemException.builder().code(ErrorCode.CAP_INITIALIZATION).message(msg).build();
		}

		return adapter;
	}

	public <PL extends JSs7PayloadWrapper> MAPDialog generateMapDialog(PL payload) throws SystemException {
		try {
			return (MAPDialog) payload.generateCarrier();

		} catch (ValidationException e) {
			// TODO SS7: Validation should be done on module options before constructing payloads
			throw SystemException.builder().message(e.getMessage()).code(e.getCode()).parent(e).build();
		}
	}

	public <PL extends Jss7CapPayloadWrapper> CAPDialog generateCapDialog(PL payload) throws SystemException {
		try {
			return (CAPDialog) payload.generateCarrier();

		} catch (ValidationException e) {
			// TODO SS7: Validation should be done on module options before constructing payloads
			throw SystemException.builder().message(e.getMessage()).code(e.getCode()).parent(e).build();
		}
	}

	public <PL extends JSs7PayloadWrapper,DLG extends MAPDialog> void addToDialog(PL payload, DLG dialog) throws SystemException {
		payload.addToCarrier(dialog);
	}

	public <PL extends Jss7CapPayloadWrapper,DLG extends CAPDialog> void addToDialog(PL payload, DLG dialog) throws SystemException {
		payload.addToCarrier(dialog);
	}

	public void addMapDialogEventListener(int ssn, MAPDialogListener listener) throws SystemException {
		getMapAdapter(ssn).setDialogEventListener(listener);
	}

	public void removeMapDialogEventListener(int ssn) throws SystemException {
		getMapAdapter(ssn).setDialogEventListener(null);
	}

	public void addCapDialogEventListener(int ssn, CAPDialogListener listener) throws SystemException {
		getCapAdapter(ssn).setDialogEventListener(listener);
	}

	public void removeCapDialogEventListener(int ssn) throws SystemException {
		getCapAdapter(ssn).setDialogEventListener(null);
	}

	public void addMobilityServiceListener(int ssn, MAPServiceMobilityListener listener) throws SystemException {
		getMapAdapter(ssn).setMobilityServiceListener(listener);
	}

	public void removeMobilityServiceListener(int ssn) throws SystemException {
		getMapAdapter(ssn).setMobilityServiceListener(null);
	}

	public void addSmsServiceListener(int ssn, MAPServiceSmsListener listener) throws SystemException {
		getMapAdapter(ssn).setSmsServiceListener(listener);
	}

	public void removeSmsServiceListener(int ssn) throws SystemException {
		getMapAdapter(ssn).setSmsServiceListener(null);
	}

	public void addOamServiceListener(int ssn, MAPServiceOamListener listener) throws SystemException {
		getMapAdapter(ssn).setOamServiceListener(listener);
	}

	public void removeOamServiceListener(int ssn) throws SystemException {
		getMapAdapter(ssn).setOamServiceListener(null);
	}

	public void addPdpServiceListener(int ssn, MAPServicePdpContextActivationListener listener) throws SystemException {
		getMapAdapter(ssn).setPdpServiceListener(listener);
	}

	public void removePdpServiceListener(int ssn) throws SystemException {
		getMapAdapter(ssn).setPdpServiceListener(null);
	}

	public void addLcsServiceListener(int ssn, MAPServiceLsmListener listener) throws SystemException {
		getMapAdapter(ssn).setLcsServiceListener(listener);
	}

	public void removeLcsServiceListener(int ssn) throws SystemException {
		getMapAdapter(ssn).setLcsServiceListener(null);
	}

	public void addCallHandlingServiceListener(int ssn, MAPServiceCallHandlingListener listener) throws SystemException {
		getMapAdapter(ssn).setCallHandlingServiceListener(listener);
	}

	public void removeCallHandlingServiceListener(int ssn) throws SystemException {
		getMapAdapter(ssn).setCallHandlingServiceListener(null);
	}

	public void addCapCsCallListener(int ssn, CAPServiceCircuitSwitchedCallListener listener) throws SystemException {
		getCapAdapter(ssn).setCapCsCallListener(listener);
	}

	public void removeCapCsCallListener(int ssn) throws SystemException {
		getCapAdapter(ssn).setCapCsCallListener(null);
	}
}
