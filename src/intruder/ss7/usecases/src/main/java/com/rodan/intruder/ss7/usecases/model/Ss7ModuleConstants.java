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

package com.rodan.intruder.ss7.usecases.model;

import com.rodan.library.model.Constants;

/**
 * @author Ayman ElSherif
 */
public interface Ss7ModuleConstants {
    String INFO_GATHERING_CATEGORY = Constants.INFO_GATHERING_CATEGORY;
    String INFO_GATHERING_CATEGORY_DISPLAY_NAME = Constants.INFO_GATHERING_CATEGORY_DISPLAY_NAME;
    String LOCATION_TRACKING_CATEGORY = Constants.LOCATION_TRACKING_CATEGORY;
    String LOCATION_TRACKING_CATEGORY_DISPLAY_NAME = Constants.LOCATION_TRACKING_CATEGORY_DISPLAY_NAME;
    String INTERCEPTION_CATEGORY = Constants.INTERCEPTION_CATEGORY;
    String INTERCEPTION_CATEGORY_DISPLAY_NAME = Constants.INTERCEPTION_CATEGORY_DISPLAY_NAME;
    String FRAUD_CATEGORY = Constants.FRAUD_CATEGORY;
    String FRAUD_CATEGORY_DISPLAY_NAME = Constants.FRAUD_CATEGORY_DISPLAY_NAME;
    String DOS_CATEGORY = Constants.DOS_CATEGORY;
    String DOS_CATEGORY_DISPLAY_NAME = Constants.DOS_CATEGORY_DISPLAY_NAME;

    // Information Gathering
    String ROUTING_INFO_NAME = INFO_GATHERING_CATEGORY + "/ss7/basic";
    String ROUTING_INFO_DISPLAY_NAME = "Send-Routing-Info (SRI) message";
    String ROUTING_INFO_BRIEF = "Disclose IMSI, HLR, and VLR";
    String ROUTING_INFO_DESCRIPTION = "Disclose subscriber IMSI and address of current VLR using MAP Send-Routing-Info (SRI) message.";
    String ROUTING_INFO_RANK = "low";

    String SMS_ROUTING_INFO_NAME = INFO_GATHERING_CATEGORY + "/ss7/basic_sm";
    String SMS_ROUTING_INFO_DISPLAY_NAME = "Send-Routing-Info-For-Sm (SRI-SM) message";
    String SMS_ROUTING_INFO_BRIEF = "Disclose IMSI, HLR, and vMSC (SMS)";
    String SMS_ROUTING_INFO_DESCRIPTION = "Disclose subscriber IMSI and address of HLR & current VLR using MAP Send-Routing-Info-For-Sm (SRI-SM) message.\nCompare 2 results of the same MSISDN to detect SMS home routing.";
    String SMS_ROUTING_INFO_RANK = "good";

    String LCS_ROUTING_INFO_NAME = INFO_GATHERING_CATEGORY + "/ss7/basic_lcs";
    String LCS_ROUTING_INFO_DISPLAY_NAME = "Send-Routing-Info-For-Lcs (SRI-LCS) message";
    String LCS_ROUTING_INFO_BRIEF = "Disclose IMSI, VMSC and SGSN (LCS)";
    String LCS_ROUTING_INFO_DESCRIPTION = "Disclose subscriber IMSI and address of VMSC & SGSN using MAP Send-Routing-Info-For-Lcs (SRI-LCS) message.\nProbably blocked as Location Based Services (LBS) is not very common on inter-connect links.";
    String LCS_ROUTING_INFO_RANK = "low";

    String GPRS_ROUTING_INFO_NAME = INFO_GATHERING_CATEGORY + "/ss7/basic_gprs";
    String GPRS_ROUTING_INFO_DISPLAY_NAME = "Send-Routing-Info-For-Gprs (SRI-GPRS) message";
    String GPRS_ROUTING_INFO_BRIEF = "Disclose GGSN & SGSN using (GPRS)";
    String GPRS_ROUTING_INFO_DESCRIPTION = "Disclose subscriber current GGSN & SGSN addresses using MAP Send-Routing-Info-For-Gprs (SRI-GPRS) message.";
    String GPRS_ROUTING_INFO_RANK = "low";

    String SEND_IMSI_NAME = INFO_GATHERING_CATEGORY + "/ss7/imsi";
    String SEND_IMSI_DISPLAY_NAME = "Send-IMSI message";
    String SEND_IMSI_BRIEF = "Disclose subscriber's IMSI";
    String SEND_IMSI_DESCRIPTION = "Disclose subscriber IMSI using MAP Send-IMSI message.";
    String SEND_IMSI_RANK = "low";

    String NEW_AUTH_VECTOR_NAME = INFO_GATHERING_CATEGORY + "/ss7/new_auth";
    String NEW_AUTH_VECTOR_DISPLAY_NAME = "Send-Authentication-Info (SAI) message";
    String NEW_AUTH_VECTOR_BRIEF = "Disclose subscriber's new authentication vectors";
    String NEW_AUTH_VECTOR_DESCRIPTION = "Disclose subscriber's authentication triplets using MAP Send-Authentication-Info (SAI) message. \nCan be used along with a fake BTS to intercept subscriber traffic.";
    String NEW_AUTH_VECTOR_RANK = "great";

    String CURRENT_AUTH_VECTOR_VECTOR_NAME = INFO_GATHERING_CATEGORY + "/ss7/auth";
    String CURRENT_AUTH_VECTOR_DISPLAY_NAME = "Send-Identification message";
    String CURRENT_AUTH_VECTOR_BRIEF = "Disclose subscriber's current authentication vectors";
    String CURRENT_AUTH_VECTOR_DESCRIPTION = "Disclose subscriber's current authentication triplets using MAP Send-Identification message. \nCan be used to decrypt sniffed subscriber traffic.";
    String CURRENT_AUTH_VECTOR_RANK = "low";

    String HLR_ADDRESS_SM_NAME = INFO_GATHERING_CATEGORY + "/ss7/hlr";
    String HLR_ADDRESS_SM_DISPLAY_NAME = "Report-SM-Delivery-Status message";
    String HLR_ADDRESS_SM_BRIEF = "Disclose HLR address (SMS)";
    String HLR_ADDRESS_SM_DESCRIPTION = "Disclose subscriber's HLR address using Report-SM-Delivery-Status message. Address can be used to target HLR directly";
    String HLR_ADDRESS_SM_RANK = "good";

    String CAMEL_INFO_NAME = INFO_GATHERING_CATEGORY + "/ss7/camel";
    String CAMEL_INFO_DISPLAY_NAME = "Update Location (UL) Message";
    String CAMEL_INFO_BRIEF = "Disclose gsmSCF address";
    String CAMEL_INFO_DESCRIPTION = "Disclose gsmSCF address using Update-Location (UL) message. Address can be used to spoof messages (ex: ATI) \nto bypass basic SS7 message filtering. Module does cancels the UL procedure after disclosing gsmSCF address to avoid by subscriber DoS.";
    String CAMEL_INFO_RANK = "great";

    String VLR_BF_NAME = INFO_GATHERING_CATEGORY + "/ss7/vlr_bf";
    String VLR_BF_DISPLAY_NAME = "Provide-Subscriber-Info (PSI) message";
    String VLR_BF_BRIEF = "Brute force VLR address";
    String VLR_BF_DESCRIPTION = "Brute force VLR address using Provide-Subscriber-Info (PSI) message.";
    String VLR_BF_RANK = "good";


    // Location Tracking
    String LOCATION_ATI_NAME = LOCATION_TRACKING_CATEGORY + "/ss7/approx_ati";
    String LOCATION_ATI_DISPLAY_NAME = "Any-Time-Interrogation (ATI) message";
    String LOCATION_ATI_BRIEF = "Disclose subscriber's approximate location (MSISDN)";
    String LOCATION_ATI_DESCRIPTION = "Disclose subscriber location (Cell ID, MCC, MNC, LAC) using MAP Any-Time-Interrogation (ATI) message.\nIt's a direct method to track subscribers location using their MSISDNs or IMSIs, but can be easily blocked\nusing operator's legacy equipments.";
    String LOCATION_ATI_RANK = "low";

    String LOCATION_PSI_NAME = LOCATION_TRACKING_CATEGORY + "/ss7/approx_psi";
    String LOCATION_PSI_DISPLAY_NAME = "Provide-Subscriber-Info (PSI) message";
    String LOCATION_PSI_BRIEF = "Disclose subscriber's approximate location (IMSI)";
    String LOCATION_PSI_DESCRIPTION = "Disclose subscriber location (Cell ID, MCC, MNC, LAC) using MAP Provide-Subscriber-Info (PSI) message.\nIt requires knowledge of target IMSI and serving VLR, but is difficult to block using operator's legacy equipments.";
    String LOCATION_PSI_RANK = "good";

    String LOCATION_PSL_NAME = LOCATION_TRACKING_CATEGORY + "/ss7/accurate";
    String LOCATION_PSL_DISPLAY_NAME = "Provide-Subscriber-Location (PSL) message";
    String LOCATION_PSL_BRIEF = "Disclose subscriber's accurate location using IMSI and MSC address";
    String LOCATION_PSL_DESCRIPTION = "Disclose subscriber location (Cell ID, MCC, MNC, LAC) using MAP Provide-Subscriber-Location (PSL) message.\nIt can be used to track subscribers location using their MSISDNs or IMSIs. It gets the most precise location,\nbut requires the knowledge of serving MSC, may not be supported by target operator, and can be easily blocked\nusing operator's legacy equipments.";
    String LOCATION_PSL_RANK = "average";


    // Call and SMS Interception
    String SMS_INTERCEPTION_NAME = INTERCEPTION_CATEGORY + "/ss7/sms";
    String SMS_INTERCEPTION_DISPLAY_NAME = "Upload-Location (UL) message";
    String SMS_INTERCEPTION_BRIEF = "Intercept subscriber's SMS";
    String SMS_INTERCEPTION_DESCRIPTION = "Intercept subscriber's mobile terminated SMS messages by updating his location to attacker's node (MSC)";
    String SMS_INTERCEPTION_RANK = "good";

    String MO_CALL_INTERCEPTION_NAME = INTERCEPTION_CATEGORY + "/ss7/call_mo";
    String MO_CALL_INTERCEPTION_DISPLAY_NAME = "Insert-Subscriber-Data (ISD) message";
    String MO_CALL_INTERCEPTION_BRIEF = "Redirect subscriber's outgoing calls using (CAMEL)";
    String MO_CALL_INTERCEPTION_DESCRIPTION = "Redirect subscriber's outgoing calls to a specific MSISDN changing the gsmSCF address in subscriber profile\n to attacker node, and replying with Connect to CAMEL IDP message.";
    String MO_CALL_INTERCEPTION_RANK = "good";

    String MO_CALL_INTERCEPTION_MSRN_NAME = INTERCEPTION_CATEGORY + "/ss7/call_msrn";
    String MO_CALL_INTERCEPTION_MSRN_DISPLAY_NAME = "Upload-Location (UL) message";
    String MO_CALL_INTERCEPTION_MSRN_BRIEF = "Redirect subscriber's incoming calls";
    String MO_CALL_INTERCEPTION_MSRN_DESCRIPTION = "Redirect subscriber's incoming calls by updating his location to attacker's node VLR (keeping vMSC as is),\nand replying to PNR requests with attacker's MSRN.";
    String MO_CALL_INTERCEPTION_MSRN_RANK = "good";


    // Fraud
    String SMS_FRAUD_NAME = FRAUD_CATEGORY + "/ss7/sms";
    String SMS_FRAUD_DISPLAY_NAME = "Forward-Short-Message (FSM) message";
    String SMS_FRAUD_BRIEF = "Send spam SMS bypassing SMS firewalls";
    String SMS_FRAUD_DESCRIPTION = "Send spam SMS directly to subscriber bypassing SMS firewall and spoofing any sender ID.";
    String SMS_FRAUD_RANK = "good";


    // Denial of Service
    String DOS_CL_NAME = DOS_CATEGORY + "/ss7/cl";
    String DOS_CL_DISPLAY_NAME = "Cancel-Location (CL) message";
    String DOS_CL_BRIEF = "Interrupt subscriber's availability (CL)";
    String DOS_CL_DESCRIPTION = "Interrupt subscriber availability by sending Cancel-Location (CL) message to vMSC node.";
    String DOS_CL_RANK = "good";

    String DOS_DSD_NAME = DOS_CATEGORY + "/ss7/dsd";
    String DOS_DSD_DISPLAY_NAME = "Delete-Subscriber-Data (DSD) message";
    String DOS_DSD_BRIEF = "Interrupt subscriber's availability (DSD)";
    String DOS_DSD_DESCRIPTION = "Interrupt subscriber availability by sending Delete-Subscriber-Data (DSD) message to vMSC node.";
    String DOS_DSD_RANK = "average";

    String DOS_PURGE_NAME = DOS_CATEGORY + "/ss7/purge";
    String DOS_PURGE_DISPLAY_NAME = "Purge-MS message";
    String DOS_PURGE_BRIEF = "Interrupt subscriber's availability (Purge)";
    String DOS_PURGE_DESCRIPTION = "Interrupt subscriber availability by sending Purge-MS message to HLR node.";
    String DOS_PURGE_RANK = "good";

    String DOS_CALL_BARRING_NAME = DOS_CATEGORY + "/ss7/barring";
    String DOS_CALL_BARRING_DISPLAY_NAME = "Insert-Subscriber-Data (ISD) message";
    String DOS_CALL_BARRING_BRIEF = "Interrupt subscriber's availability (MO calls and SMSs)";
    String DOS_CALL_BARRING_DESCRIPTION = "Interrupt subscriber availability 'MO calls and SMSs' by sending (ISD) message to VLR with barring flags set.";
    String DOS_CALL_BARRING_RANK = "average";
}
