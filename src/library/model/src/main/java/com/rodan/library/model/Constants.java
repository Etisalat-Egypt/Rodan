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

package com.rodan.library.model;

public interface Constants {
	String APP_NAME = "rodan";

	String BASE_CONFIG_DIR = "config";
	String WORD_LIST_BASE_DIR_NAME = "wordlist";
	int SCTP_CONNECT_DELAY = 1000;
	int SCTP_MAX_IO_ERRORS = 30;
	int M3UA_DELIVERY_MESSAGE_THREAD_COUNT = 1;
	
	int MIN_ASP_ACTIVE_FOR_LOAD_BALANACE = 1;
	int M3UA_NO_POINT_CODE = 0;

	// ref: http://ndl.ru/~hammet/netw/SS7/SS7_Protocol_Architecture_and_Services/sigtran-training/ch07lev1sec2.html#ch07table01
	int M3UA_SCCP_SI = 3;
	int M3UA_ISUP_SI = 5;

	int M3UA_WILD_CARD_PC = -1; // ref: https://github.com/RestComm/jss7/blob/master/docs/userguide/sources-asciidoc/src/main/asciidoc/Chapter-M3UA.adoc#route
	int M3UA_WILD_CARD_SI = -1;

	// Ref: http://ndl.ru/~hammet/netw/SS7/SS7_Protocol_Architecture_and_Services/sigtran-training/ch09lev1sec4.html#ch09table12
	// Ref: https://en.wikipedia.org/wiki/Subsystem_number
	int SCCP_SSN_FLAG = 0;
	int SCCP_UNKNOWN_SSN = 0;
	int SCCP_SSN_NOT_PRESENT = 0;
	int SCCP_SPC_NOT_PRESENT = 0;
	int SCCP_HLR_SSN = 6;
	int SCCP_VLR_SSN = 7;
	int SCCP_MSC_SSN = 8;
	int SCCP_EIR_SSN = 9;
	int SCCP_AUC_SSN = 10;
	int SCCP_GMLC_SSN = 145;
	int SCCP_CAP_SSN = 146;
	int SCCP_GSMSCF_SSN  = 147;
	int SCCP_SMLC_SSN = 252;
	int SCCP_GGSN_SSN = 150;
	boolean SCCP_INTEGRATED_VLR = true;

	int SCCP_INTERNATIONAL_NI = 0;
	int SCCP_NATIONAL_NI = 2;
	int SCCP_ROUTING_ADDRESS_ID_NOT_USED = -1;

	int SCCP_TRANSLATION_TYPE_NOT_USED = 0;

	// SCCP Routing
	// Ref: https://github.com/RestComm/jss7/blob/master/docs/userguide/sources-asciidoc/src/main/asciidoc/Section-Managing-SCCP.adoc#using-cli-39
	String SCCP_MASK_IGNORE = "-";
	String SCCP_MASK_SEPARATOR = "/";
	String SCCP_MASK_KEEP = "K";
	String SCCP_MASK_REPLACE = "R";

	String MAP_STACK_NAME = "MAP-HLR";
	int MAP_DEFAULT_VERSION = 3;
	int MAP_DIALOG_IDLE_TIMEOUT = 60000;
	int MAP_INVOKE_TIMEOUT = 30000;
	int CAP_DIALOG_IDLE_TIMEOUT = 60000;
	int CAP_INVOKE_TIMEOUT = 30000;
	
	// SCCP Routing
    String SCCP_DIGITS_PATTERN_WILD_CARD_ALL = "*";
	String SCCP_DIGITS_PATTERN_EMPTY = ""; //TODO: rename
    String SCCP_DIGITS_PATTERN_WILD_CARD_SINGLE = "?";
	String SCCP_DIGITS_PATTERN_SEPARATOR = "/";
	String SCCP_DIGITS_PATTERN_PADDING = "-";

    String NETWORK_ID = "networkId";
    
    String DEFAULT_CONFIG_FILE_PATH = "app.yml";

    int TRANSLATION_TYPE_NOT_USED = 0;

	int DIAMETER_ERROR_USER_UNKNOWN = 5001;
	int DIAMETER_ERROR_UNABLE_TO_DELIVER = 3002;
	int DIAMETER_ERROR_TOO_BUSY = 3004;


	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	//  SS7 Payloads
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	String SAI_PAYLOAD_NAME = "sai";
	String SAI_RESPONSE_PAYLOAD_NAME = "sai_res";
	String SEND_IDENTIFICATION_PAYLOAD_NAME = "sid";
	String UL_PAYLOAD_NAME = "ul";
	String SIMSI_PAYLOAD_NAME = "simsi";
	String SRI_GPRS_PAYLOAD_NAME = "sri_gprs";
	String SRI_LCS_PAYLOAD_NAME = "sri_lcs";
	String SRI_SM_PAYLOAD_NAME = "sri_sm";
	String SRI_SM_RESPONSE_PAYLOAD_NAME = "sri_sm_res";
	String SRI_PAYLOAD_NAME = "sri";
	String SRI_RESPONSE_PAYLOAD_NAME = "sri_resp";
	String ATI_PAYLOAD_NAME = "ati";
	String ATI_RESPONSE_PAYLOAD_NAME = "ati_resp";
	String PSI_PAYLOAD_NAME = "psi";
	String PSI_RESPONSE_PAYLOAD_NAME = "psi_resp";
	String PSL_PAYLOAD_NAME = "psl";
	String REPORT_SM_DELIVERY_STATUS_PAYLOAD_NAME = "report_sm_delivery_status";
	String FSM_PAYLOAD_NAME = "fsm";
	String FSM_FORWARDER_PAYLOAD_NAME = "fsm_forwarder";
	String FSM_RESPONSE_PAYLOAD_NAME = "fsm_response";
	String MT_FSM_RESPONSE_PAYLOAD_NAME = "mt_fsm_response";
	String CL_PAYLOAD_NAME = "cl";
	String DSD_PAYLOAD_NAME = "dsd";
	String PURGE_MS_PAYLOAD_NAME = "purge_ms";
	String ISD_PAYLOAD_NAME = "isd";
	String ISD_FORWARDER_PAYLOAD_NAME = "isd_forwarder";
	String ISD_RESPONSE_PAYLOAD_NAME = "isd_response";
	String RESTORE_DATA_PAYLOAD_NAME = "restore_data";
	String PNR_RESPONSE_PAYLOAD_NAME = "pnr_response";

	String CAMEL_CONNECT_PAYLOAD_NAME = "conect";

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	//  Diameter Payloads
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	String ULR_PAYLOAD_NAME = "ulr";
	String AIR_PAYLOAD_NAME = "air";
	String IDR_PAYLOAD_NAME = "idr";
	String NOR_PAYLOAD_NAME = "nor";
	String PUR_PAYLOAD_NAME = "pur";
	String CLR_PAYLOAD_NAME = "clr";

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	//  SS7 Modules
	//////////////////////////////////////////////////////////////////////////////////////////////////////////

	// Module Categories
	String INFO_GATHERING_CATEGORY = "gather";
	String INFO_GATHERING_CATEGORY_DISPLAY_NAME = "Information Gathering";

	String LOCATION_TRACKING_CATEGORY = "locate";
	String LOCATION_TRACKING_CATEGORY_DISPLAY_NAME = "Location Tracking";

	String INTERCEPTION_CATEGORY = "intercept";
	String INTERCEPTION_CATEGORY_DISPLAY_NAME = "Interception (Calls and SMSs)";

	String FRAUD_CATEGORY = "fraud";
	String FRAUD_CATEGORY_DISPLAY_NAME = "Fraud";

	String DOS_CATEGORY = "dos";
	String DOS_CATEGORY_DISPLAY_NAME = "Denial of Service";

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	//  MAP Services
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	String MAP_CALL_HANDLING_SERVICE = "map_call_handling_service";
	String MAP_LOCATION_SERVICE = "map_location_service";
	String MAP_MOBILITY_SERVICE = "map_mobility_service";
	String MAP_OAM_SERVICE = "map_oam_service";
	String MAP_PACKET_SERVICE = "map_packet_service";
	String MAP_SMS_SERVICE = "map_sms_service";
	//////////////////////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	//  CAP Services
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	String CAP_CS_CALL_HANDLING_SERVICE = "cap_cs_call_handling_service";
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
}
