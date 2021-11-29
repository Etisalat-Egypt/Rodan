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

package com.rodan.intruder.main.cli.executor;

import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.kernel.usecases.model.ModuleResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncModuleExecutor implements ModuleExecutor {
    // TODO move com.rodan.intruder.main.cli.executor package to another
    // Intruder module as Lab.Main modules depends on it
    final static Logger logger = LogManager.getLogger(AsyncModuleExecutor.class);

    private static final int INITIAL_ASSOCIATION_CAPACITY = 10;

    private ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public Future<? extends ModuleResponse> execute(SignalingModule ss7Module) {
        return executor.submit(ss7Module::run);
    }
}
