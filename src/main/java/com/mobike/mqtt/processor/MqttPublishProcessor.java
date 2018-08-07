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
package com.mobike.mqtt.processor;

import com.mobike.mqtt.config.ServerConfigs;
import com.mobike.mqtt.remoting.Connection;
import com.mobike.mqtt.remoting.ConnectionManager;
import com.mobike.mqtt.remoting.RemotingContext;
import com.mobike.mqtt.store.RedisStore;
import com.mobike.mqtt.utils.FormatUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

/**
 * process publish message
 *
 * 1. client->broker->mq 2. micro service->mq->dispatcher->broker->client 3.
 * client->broker->mq->dispatcher->broker->client
 *
 * @author mudun
 * @version $Id: MqttPublishProcessor.java, v 0.1 2018/8/3 上午10:21 mudun Exp $
 */
public class MqttPublishProcessor implements RequestCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger("PublishHandler");

    private final ConnectionManager connectionManager;

    private final RedisStore redisStore;

    public MqttPublishProcessor(ConnectionManager connectionManager, RedisStore redisStore) {
        this.connectionManager = connectionManager;
        this.redisStore = redisStore;
    }

    @Override public void handle(MqttMessage message, RemotingContext ctx) {
        MqttPublishMessage msg = (MqttPublishMessage) message;
        final String topic = msg.variableHeader().topicName();

        String[] result = topic.split("/");
        String subClientId = result[result.length - 1];
        String groupKey = result[0];

        String payloadStr = FormatUtil.get(msg.payload());

        Connection connection = connectionManager.getConnection(groupKey, subClientId);

        if (connection != null && connection.isActive()) {
            final ByteBuf payload = Unpooled.copiedBuffer(msg.payload());
            MqttPublishMessage mqttPublishMessage = makePublishMessage(topic, AT_MOST_ONCE, payload.retainedDuplicate(), 0);

            connection.getChannel().writeAndFlush(mqttPublishMessage)
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                logger.info("send the msg success.clientId={},topic={},payload={}", subClientId, topic, payloadStr);
                            } else {
                                logger.error("send the msg fail.clientId={},topic={},payload={}", subClientId, topic, payloadStr);
                            }
                        }
                    });
        } else {
            logger.warn("send the msg error cause of inactive.clientId={},topic={},payload={}", subClientId, topic, payloadStr);
        }
    }


    private static MqttPublishMessage makePublishMessage(String topic, MqttQoS qos, ByteBuf message,
            int messageId) {
        MqttFixedHeader fixedHeader =
                new MqttFixedHeader(MqttMessageType.PUBLISH, false, qos, false, 0);
        MqttPublishVariableHeader varHeader = new MqttPublishVariableHeader(topic, messageId);
        return new MqttPublishMessage(fixedHeader, varHeader, message);
    }

}
