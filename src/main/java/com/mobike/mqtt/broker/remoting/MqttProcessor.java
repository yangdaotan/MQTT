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
package com.mobike.mqtt.broker.remoting;

import com.mobike.mqtt.broker.config.BrokerConfig;
import com.mobike.mqtt.broker.log.MqttLoggerFactory;
import com.mobike.mqtt.broker.processor.UserProcessor;
import com.mobike.mqtt.broker.utils.NamedThreadFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

/**
 * @author mudun
 * @version $Id: MqttProcessor.java, v 0.1 2019/4/9 下午1:45 mudun Exp $
 */
public class MqttProcessor {
    private static final Logger logger = MqttLoggerFactory.getLogger("RpcRemoting");

    private ConcurrentHashMap<MqttMessageType, UserProcessor> userProcessors = new ConcurrentHashMap<>();

    private ExecutorService defaultExecutor;

    public MqttProcessor() {
        this.defaultExecutor = new ThreadPoolExecutor(BrokerConfig.MQTT_CORE_POOL_SIZE,
                BrokerConfig.MQTT_MAX_POOL_SIZE,
                60000,
                TimeUnit.MICROSECONDS,
                new LinkedBlockingQueue<Runnable>(BrokerConfig.MQTT_QUEUE_CAPACITY),
                new NamedThreadFactory("mqtt-processor-"
                        + "thread-pool")
        );
    }

    public void process(RemotingContext ctx, MqttMessage msg)
            throws Exception {
        UserProcessor userProcessor = userProcessors.get(ctx.getMqttMessageType());
        if (userProcessor == null) {
            logger.warn("find no userProcessor for mqtt type : {}", ctx.getMqttMessageType());
            return;
        }

        // get the biz logic thread pool
        Executor executor = userProcessor.getExecutor();
        if (executor == null) {
            executor = defaultExecutor;
        }

        executor.execute(new ProcessTask(ctx, msg, userProcessor));
    }


    public void registerProcessor(MqttMessageType mqttMessageType, UserProcessor userProcessor) {
        this.userProcessors.putIfAbsent(mqttMessageType, userProcessor);
    }

    private void sendResponse(final RemotingContext ctx, Object response) {
        ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
            @Override public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    logger.error("response send failed, cause: ", future.cause());
                }
            }
        });
    }

    class ProcessTask implements Runnable {
        private RemotingContext ctx;
        private MqttMessage message;
        private UserProcessor userProcessor;

        public ProcessTask(RemotingContext context, MqttMessage message, UserProcessor userProcessor) {
            this.ctx = context;
            this.message = message;
            this.userProcessor = userProcessor;
        }

        @Override public void run() {
            try {
                BizContext bizContext = userProcessor.preHandleRequest(ctx, message);
                if (userProcessor.oneway()) {
                    userProcessor.handleRequestOneway(bizContext, message);
                } else {
                    Object response = userProcessor.handleRequest(bizContext, message);
                    sendResponse(ctx, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
