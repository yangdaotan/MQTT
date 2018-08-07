/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mobike.mqtt.remoting;

import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mudun
 * @version $Id: RemotingServer.java, v 0.1 2018/8/1 上午11:08 mudun Exp $
 */
public abstract class RemotingServer {
    private static final Logger logger = LoggerFactory.getLogger("Server");

    protected int port;

    private AtomicBoolean initialized = new AtomicBoolean(false);

    private AtomicBoolean started = new AtomicBoolean(false);

    public RemotingServer(int port) {
        this.port = port;
    }

    /**
     * init the server
     */
    public void init() {
        if (initialized.compareAndSet(false, true)) {
            logger.info("Initialize the service.");
            doInit();
        } else {
            logger.warn("service has been initialized");
        }
    }

    /**
     * actually init the server
     */
    public abstract void doInit();

    /**
     * init & start the server
     */
    public void start() {
        init();
        if (started.compareAndSet(false, true)) {
            logger.info("Start the service.");
            doStart();
        } else {
            logger.warn("service has been started");
        }
    }

    /**
     * actually start the server
     */
    public abstract void doStart();

    /**
     * stop and shutdown the server
     */
    public void stop() {
        if (started.compareAndSet(true, false)) {
            logger.info("shutdown the server");
            doStop();
        } else {
            logger.warn("server has not started yet");
        }
    }

    /**
     * actually stop the server
     */
    public abstract void doStop();
}
