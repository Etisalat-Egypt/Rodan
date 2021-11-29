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

package com.rodan.intruder.diameter.gateway.service;

import com.rodan.intruder.diameter.gateway.adapter.JDiameterS6aSession;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import lombok.Builder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.s6a.ClientS6aSession;
import org.jdiameter.api.s6a.ServerS6aSession;
import org.jdiameter.client.api.ISessionFactory;
import org.jdiameter.common.impl.app.s6a.S6aSessionFactoryImpl;

public class S6aAppService implements AppService<JDiameterS6aSession> {
    final static Logger logger = LogManager.getLogger(S6aAppService.class);

    private ApplicationId authAppId;
    private ISessionFactory sessionFactory;
    private S6aSessionFactoryImpl s6aSessionFactory;

    @Builder
    public S6aAppService(ApplicationId authAppId, ISessionFactory sessionFactory, S6aSessionFactoryImpl s6aSessionFactory) {
        this.authAppId = authAppId;
        this.sessionFactory = sessionFactory;
        this.s6aSessionFactory = s6aSessionFactory;
    }

    public JDiameterS6aSession generateSession() throws SystemException {
        try {
            var session = sessionFactory.getNewSession();
            var clientAppSession = sessionFactory.<ClientS6aSession>getNewAppSession(null, authAppId,
                    ClientS6aSession.class, null);
            var serverAppSession = sessionFactory.<ServerS6aSession>getNewAppSession(null, authAppId,
                    ServerS6aSession.class, null);
            return JDiameterS6aSession.<ClientS6aSession>builder().appId(authAppId)
                    .messageFactory(s6aSessionFactory.getMessageFactory()).session(session)
                    .clientAppSession(clientAppSession).serverAppSession(serverAppSession)
                    .build();

        } catch (InternalException e) {
            var msg = "Failed to generate ClientS6aSession carrier.";
            logger.error(msg, e);
            throw SystemException.builder().code(ErrorCode.DIAMETER_REQUEST_SEND_FAILED).message(msg).parent(e).build();
        }
    }
}
