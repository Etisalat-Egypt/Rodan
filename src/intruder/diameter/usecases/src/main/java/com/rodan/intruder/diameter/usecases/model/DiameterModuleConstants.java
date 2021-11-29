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

package com.rodan.intruder.diameter.usecases.model;

import com.rodan.library.model.Constants;

/**
 * @author Ayman ElSherif
 */
public interface DiameterModuleConstants {
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
    String SUBSCRIBER_INFO_NAME = INFO_GATHERING_CATEGORY + "/diameter/subscriber";
    String SUBSCRIBER_INFO_DISPLAY_NAME = "Update-Location-Request (ULR) message";
    String SUBSCRIBER_INFO_BRIEF = "Subscriber information disclose location using (ULR) message.";
    String SUBSCRIBER_INFO_DESCRIPTION = "Disclosing subscriber information (MSISDN, status, APN, access restriction, etc.) using Update-Location-Request (ULR) message.";
    String SUBSCRIBER_INFO_RANK = "great";

    String NEW_AUTH_PARAM_NAME = INFO_GATHERING_CATEGORY + "/diameter/new_auth";
    String NEW_AUTH_PARAM_DISPLAY_NAME = "Authentication-Information-Request (AIR) message";
    String NEW_AUTH_PARAM_BRIEF = "Disclose authentication parameters using (AIR) message.";
    String NEW_AUTH_PARAM_DESCRIPTION = "Disclose subscriber authentication parameters using Authentication-Information-Request (AIR) message. \nCan be used along with a fake eNodeB to intercept subscriber traffic.";
    String NEW_AUTH_PARAM_RANK = "great";

    String HSS_ADDRESS_AIR_NAME = INFO_GATHERING_CATEGORY + "/diameter/hss";
    String HSS_ADDRESS_AIR_DISPLAY_NAME = "Authentication-Information-Request (AIR) message";
    String HSS_ADDRESS_AIR_BRIEF = "Disclose HSS address using (AIR) message.";
    String HSS_ADDRESS_AIR_DESCRIPTION = "Disclose HSS address by sending Authentication-Information-Request (AIR) message. Topology hiding will result in pseudo address being returned.";
    String HSS_ADDRESS_AIR_RANK = "average";

    String MME_IDR_BF_NAME = INFO_GATHERING_CATEGORY + "/diameter/mme_bf";
    String MME_IDR_BF_DISPLAY_NAME = "Insert-Subscriber-Data-Request (IDR) message";
    String MME_IDR_BF_BRIEF = "Disclose serving MME address using (IDR) BF messages.";
    String MME_IDR_BF_DESCRIPTION = "Disclose subscriber serving MME address by sending IDR message to provided hosts list using target IMSI.\nMME address along with IMSI can be used to launch further attacks (location tracking, fraud, DoS, etc.).";
    String MME_IDR_BF_RANK = "good";

    String MME_IDR_DISCOVERY_NAME = INFO_GATHERING_CATEGORY + "/diameter/mme_discovery";
    String MME_IDR_DISCOVERY_DISPLAY_NAME = "Insert Subscriber Data Request (IDR) message";
    String MME_IDR_DISCOVERY_BRIEF = "Disclose all valid MME addresses using (IDR) BF messages.";
    String MME_IDR_DISCOVERY_DESCRIPTION = "Disclose all valid MME addresses by sending IDR message to provided provided hosts list.";
    String MME_IDR_DISCOVERY_RANK = "good";


    // Location Tracking
    String LOCATION_IDR_NAME = LOCATION_TRACKING_CATEGORY + "/diameter/approx";
    String LOCATION_IDR_DISPLAY_NAME = "Insert-Subscriber-Data-Request (IDR) message";
    String LOCATION_IDR_BRIEF = "Disclose location using (IDR) message.";
    String LOCATION_IDR_DESCRIPTION = "Disclosing subscriber location (Cell ID, MCC, MNC, TAC) using Insert-Subscriber-Data-Request (IDR) message.";
    String LOCATION_IDR_RANK = "good";


    // Denial of Service
    String DOS_ALL_ULR_NAME = DOS_CATEGORY + "/diameter/dos_ulr";
    String DOS_ALL_ULR_DISPLAY_NAME = "Update-Location-Request (ULR) message";
    String DOS_ALL_ULR_BRIEF = "Interrupt subscriber availability using (ULR) message.";
    String DOS_ALL_ULR_DESCRIPTION = "Interrupt subscriber availability by sending many Update-Location-Request (ULR) messages to HSS.";
    String DOS_ALL_ULR_RANK = "great";

    String DOS_MT_SMS_NAME = DOS_CATEGORY + "/diameter/mt_sms";
    String DOS_MT_SMS_DISPLAY_NAME = "Notify-Request (NOR) message";
    String DOS_MT_SMS_BRIEF = "Interrupt subscriber SMS availability using (NOR) message.";
    String DOS_MT_SMS_DESCRIPTION = "Interrupt subscriber MT-SMS availability by sending Notify-Request (NOR) message to HSS with 'Removal of MME registration for SMS' flag set.";
    String DOS_MT_SMS_RANK = "good";

    String DOS_MO_ALL_NAME = DOS_CATEGORY + "/diameter/mo_all";
    String DOS_MO_ALL_DISPLAY_NAME = "Insert-Subscriber-Data-Request (IDR) message";
    String DOS_MO_ALL_BRIEF = "Interrupt subscriber availability using (IDR ODB) message.";
    String DOS_MO_ALL_DESCRIPTION = "Interrupt subscriber availability using by sending Insert-Subscriber-Data-Request (IDR) message with 'Operator Determined Barring' flag set.";
    String DOS_MO_ALL_RANK = "good";

    String DOS_MO_ALL_RAT_NAME = DOS_CATEGORY + "/diameter/mo_all_rat";
    String DOS_MO_ALL_RAT_DISPLAY_NAME = "Insert-Subscriber-Data-Request (IDR) message";
    String DOS_MO_ALL_RAT_BRIEF = "Interrupt subscriber availability using (IDR Access Restriction) message.";
    String DOS_MO_ALL_RAT_DESCRIPTION = "Interrupt subscriber availability using by sending Insert-Subscriber-Data-Request (IDR) message with 'Access Restriction' set for all RATs.";
    String DOS_MO_ALL_RAT_RANK = "good";

    String DOS_MT_ALL_NAME = DOS_CATEGORY + "/diameter/mt_all";
    String DOS_MT_ALL_DISPLAY_NAME = "PurgeUE-Request (PUR) message";
    String DOS_MT_ALL_BRIEF = "Interrupt subscriber availability using (PUR) message.";
    String DOS_MT_ALL_DESCRIPTION = "Interrupt subscriber availability using by sending PurgeUE-Request (PUR) message to the HSS.";
    String DOS_MT_ALL_RANK = "good";

    String DOS_MT_ALL_CLR_NAME = DOS_CATEGORY + "/diameter/dos_clr";
    String DOS_MT_ALL_CLR_DISPLAY_NAME = "Cancel-Location-Request (CLR) message";
    String DOS_MT_ALL_CLR_BRIEF = "Interrupt subscriber availability using (CLR) message.";
    String DOS_MT_ALL_CLR_DESCRIPTION = "Interrupt subscriber availability using by sending Cancel-Location-Request (CLR) message to the serving MME.";
    String DOS_MT_ALL_CLR_RANK = "good";


    // Fraud
    String FRAUD_ODB_NAME = FRAUD_CATEGORY + "/diameter/odb";
    String FRAUD_ODB_DISPLAY_NAME = "Insert-Subscriber-Data-Request (IDR) message";
    String FRAUD_ODB_BRIEF = "Remove Operator Determined Barring using (IDR) message.";
    String FRAUD_ODB_DESCRIPTION = "Restore subscriber availability using by sending Insert-Subscriber-Data-Request (IDR) message with 'Operator Determined Barring' flag cleared.";
    String FRAUD_ODB_RANK = "good";

    String FRAUD_ACCESS_RESTRICTION_NAME = FRAUD_CATEGORY + "/diameter/ar";
    String FRAUD_ACCESS_RESTRICTION_DISPLAY_NAME = "Insert-Subscriber-Data-Request (IDR) message";
    String FRAUD_ACCESS_RESTRICTION_BRIEF = "Remove Operator Determined access restriction using (IDR) message.";
    String FRAUD_ACCESS_RESTRICTION_DESCRIPTION = "Restore subscriber availability using by sending Insert-Subscriber-Data-Request (IDR) message with 'Access Restriction' cleared for all RATs.";
    String FRAUD_ACCESS_RESTRICTION_RANK = "good";
}
