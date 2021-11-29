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

package com.rodan.intruder.diameter.gateway.adapter;

import com.rodan.intruder.diameter.entities.payload.DiameterPayload;
import com.rodan.intruder.diameter.entities.session.DiameterSession;
import com.rodan.library.model.error.SystemException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.Session;
import org.jdiameter.api.app.AppSession;

@ToString
public abstract class JDiameterSession<CLT_SESS extends AppSession, SRV_SESS extends AppSession, FCTY>
        implements DiameterSession {
    // TODO check if 'implements DiameterSession' is needed
    protected static final long IDR_LOCATION_FLAGS = 30L; // Current location, EPS location info, EPS user state, T-ADS data
    protected static final int ULR_COMMAND_CODE = 316;
    protected static final int CLR_COMMAND_CODE = 317;
    protected static final int AIR_COMMAND_CODE = 318;
    protected static final int IDR_COMMAND_CODE = 319;
    protected static final int PUR_COMMAND_CODE = 321;
    protected static final int NOR_COMMAND_CODE = 323;

    protected static final long IDR_T_ADS_FLAG = 0b10L;
    protected static final long IDR_EPS_USER_STATE_FLAG = 0b100L;
    protected static final long IDR_EPS_LOCATION_INFO_REQUEST_FLAG = 0b1000L;
    protected static final long IDR_CURRENT_LOCATION_REQUEST_FLAG = 0b10000L;

    protected static final int IDR_FEATURE_LIST_AVP_CODE = 628;
    protected static final long IDR_FEATURE_LIST_ID_1_VALUE = 1L;
    protected static final long IDR_FEATURE_LIST_1_VALUE = 0xFFFFFFFF;
    protected static final long IDR_FEATURE_LIST_ID_2_VALUE = 2L;
    protected static final long IDR_FEATURE_LIST_2_VALUE = 0x003FFFFF;
    protected static final long IDR_ODB_SET_FLAG = 1L;
    protected static final long IDR_ODB_CLEAR_FLAG = 0L;
    protected static final long IDR_HPLMN_ODB_CLEAR_FLAG = 0L;
    protected static final long IDR_ACCESS_RESTRICTION_SET_FLAG = 0b11111111111L; // Ref: ETSI TS 129 272 7.3.31/1
    protected static final long IDR_ACCESS_RESTRICTION_CLEAR_FLAG = 0L;



    final static Logger logger = LogManager.getLogger(JDiameterSession.class);

    @Getter(AccessLevel.PROTECTED) private ApplicationId appId;
    @Getter(AccessLevel.PROTECTED) private org.jdiameter.api.Session session;
    @Getter(AccessLevel.PROTECTED) private CLT_SESS clientAppSession;
    @Getter(AccessLevel.PROTECTED) private SRV_SESS serverAppSession;
    @Getter(AccessLevel.PROTECTED) private FCTY messageFactory;

    public JDiameterSession(ApplicationId appId, Session session, CLT_SESS clientAppSession, SRV_SESS serverAppSession,
                            FCTY messageFactory) {
        this.appId = appId;
        this.session = session;
        this.clientAppSession = clientAppSession;
        this.serverAppSession = serverAppSession;
        this.messageFactory = messageFactory;
    }

    public abstract void send(DiameterPayload payload) throws SystemException;

    @Override
    public String getSessionId() {
        return session.getSessionId();
    }
}
