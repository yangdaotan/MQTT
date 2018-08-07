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
package com.mobike.mqtt.handler;

import com.mobike.mqtt.processor.RequestCommandProcessor;
import com.mobike.mqtt.remoting.RemotingContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.util.ReferenceCountUtil;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mudun
 * @version $Id: MqttHandler.java, v 0.1 2018/8/1 下午2:34 mudun Exp $
 */
@ChannelHandler.Sharable public class MqttHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger("handler");

    private ConcurrentHashMap<MqttMessageType, RequestCommandProcessor> processors;

    public MqttHandler() {
    }

    public MqttHandler(ConcurrentHashMap<MqttMessageType, RequestCommandProcessor> processors) {
        this.processors = processors;
    }

    @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MqttMessage mqttMessage = (MqttMessage) msg;
        MqttMessageType type = mqttMessage.fixedHeader().messageType();
        try {
            if (processors.get(type) != null) {
                processors.get(type).handle(mqttMessage, new RemotingContext(ctx));
            }
        } catch (Throwable e) {
            logger.error("exception caught,e={}", e.getCause());
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    public void setProcessors(
            ConcurrentHashMap<MqttMessageType, RequestCommandProcessor> processors) {
        this.processors = processors;
    }

}
