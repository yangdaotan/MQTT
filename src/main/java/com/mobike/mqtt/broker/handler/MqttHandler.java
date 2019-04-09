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
package com.mobike.mqtt.broker.handler;

import com.mobike.mqtt.broker.log.MqttLoggerFactory;
import com.mobike.mqtt.broker.remoting.MqttProcessor;
import com.mobike.mqtt.broker.remoting.RemotingContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.slf4j.Logger;

/**
 * @author mudun
 * @version $Id: MqttHandler.java, v 0.1 2019/4/9 下午2:29 mudun Exp $
 */
@ChannelHandler.Sharable
public class MqttHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = MqttLoggerFactory.getLogger("MqttHandler");

    private MqttProcessor mqttProcessor;

    public MqttHandler(MqttProcessor mqttProcessor) {
        this.mqttProcessor = mqttProcessor;
    }

    @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MqttMessage mqttMessage = (MqttMessage)msg;
        if (mqttMessage == null || mqttMessage.fixedHeader() == null) {
            return;
        }
        MqttMessageType messageType = mqttMessage.fixedHeader().messageType();
        if (messageType == null) {
            logger.warn("found no mqtt message type");
            return;
        }
        mqttProcessor.process(new RemotingContext(ctx, messageType), mqttMessage);
    }
}

