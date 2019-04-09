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
package com.mobike.mqtt.broker.processor;

import com.mobike.mqtt.broker.log.MqttLoggerFactory;
import com.mobike.mqtt.broker.remoting.BizContext;
import com.mobike.mqtt.broker.remoting.DefaultBizContext;
import com.mobike.mqtt.broker.remoting.RemotingContext;
import java.util.concurrent.Executor;
import org.slf4j.Logger;

/**
 * @author mudun
 * @version $Id: AbstractUserProcessor.java, v 0.1 2019/4/9 下午2:06 mudun Exp $
 */
public abstract class AbstractUserProcessor<T> implements UserProcessor<T> {

    private static final Logger logger = MqttLoggerFactory.getLogger("CommonDefault");

    @Override
    public BizContext preHandleRequest(RemotingContext remotingCtx, T request) {
        DefaultBizContext bizCtx =  new DefaultBizContext(remotingCtx);

        logger.info("receive msg from {}, type: {}", bizCtx.getRemoteAddress(), remotingCtx.getMqttMessageType());
        return bizCtx;
    }

    @Override public Object handleRequest(BizContext bizContext, T msg) throws Exception {
        return null;
    }

    @Override public void handleRequestOneway(BizContext bizContext, T msg) throws Exception {

    }

    @Override public boolean oneway() {
        return false;
    }

    @Override
    public Executor getExecutor() {
        return null;
    }
}
