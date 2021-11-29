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

package com.rodan.library.model.error;

public interface ErrorCode {
	// TODO separate system(5xxxx) from validation (4xxxx) errors.
	int INVALID_MENU_PARAMETERS = 40001;
	int INVALID_FILE = 40002;
	int INVALID_ADDRESS = 40003;
	int INVALID_POINT_CODE = 40004;
	int INVALID_IMSI = 40005;
	int INVALID_MSISDN = 40006;
	int INVALID_TOGGLE = 40007;
	int INVALID_TEXT = 40008;
	int INVALID_IMEI = 40009;
	int INVALID_MODULE = 40010;
	int INVALID_NODE_TYPE = 40011;
	int INVALID_NODE_NAME = 40012;
	int INVALID_NODE_ADDRESS = 40013;
	int INVALID_PORT_NUMBER = 40014;
	int INVALID_ROUTING_CONTEXT = 40015;
	int INVALID_NETWORK_INDICATOR = 40016;
	int INVALID_MCC = 40017;
	int INVALID_MNC = 40018;
	int INVALID_CC = 40019;
	int INVALID_NDC = 40020;
	int MISSING_FIELD = 40021;
	int INVALID_CONDITION = 40022;
	int INVALID_SSN = 40023;
	int INVALID_GT_RANGE = 40024;
	int INVALID_WORDLIST_FILE_PATH = 40025;


	int GENERAL_ERROR = 50000;
	int SCTP_INITIALIZATION = 50001;
	int M3UA_INITIALIZATION = 50002;
	int SCCP_INITIALIZATION = 50003;
	int TCAP_INITIALIZATION = 50004;
	int MAP_INITIALIZATION = 50005;
	int CAP_INITIALIZATION = 50015;
	int DIAMETER_INITIALIZATION = 50005;
	int MODULE_INITIALIZATION = 50024;
	int GTT_INITIALIZATION = 50006;
	int STACK_START = 50007;
	int SCTP_ASSOCIATION_FAILED = 50008;
	int IO_ERROR = 50009;
	int MODULE_NOT_INITIALIZED = 50010;
	int MODULE_REQUEST_ERROR = 50011;
	int MODULE_RESPONSE_ERROR = 50012;
	int FIELD_REFLECTION_ERROR = 50013;
	int INVALID_PAYLOAD_PARAMS = 50014;
	int MAP_DIALOG_SEND_FAILED = 50015;
	int MAP_SERVICE_INVALID = 50016;
	int INCOMPATIBLE_MODULE_PAYLOADS = 50017;
	int SS7_CONNECTION_INITIALIZATION = 50018;
	int MAP_COMPONENT_ERROR = 50019;
	int MAP_DIALOG_ERROR = 50020;
	int CAP_DIALOG_SEND_FAILED = 50021;
	int INVALID_NODE_FUNCTION = 50022;
	int SST_WAIT_FAILED = 50023;
	int DIAMETER_REQUEST_SEND_FAILED = 50024;
	int DIAMETER_CONNECTION_INITIALIZATION = 50025;
	int INVALID_CLASS_TYPE = 50025;
	int INVALID_MAP_DIALOG_TYPE = 50026;
	int MISSING_PAYLOAD = 50027;
}

