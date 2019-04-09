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

import com.mobike.mqtt.broker.config.BrokerConfig;
import com.mobike.mqtt.broker.log.MqttLoggerFactory;
import com.mobike.mqtt.broker.remoting.BizContext;
import com.mobike.mqtt.broker.remoting.Connection;
import com.mobike.mqtt.broker.remoting.ConnectionManager;
import com.mobike.mqtt.broker.utils.NamedThreadFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.ReferenceCountUtil;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

/**
 * @author mudun
 * @version $Id: MqttPublishProcessor.java, v 0.1 2019/4/9 下午2:18 mudun Exp $
 */
public class MqttPublishProcessor extends AbstractUserProcessor<MqttPublishMessage> {
    private static final Logger logger = MqttLoggerFactory.getLogger("UserProcessor");

    private ConnectionManager connectionManager;

    private ExecutorService sendExecutor;

    public MqttPublishProcessor(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.sendExecutor = new ThreadPoolExecutor(BrokerConfig.SEND_CORE_POOL_SIZE,
                BrokerConfig.SEND_MAX_POOL_SIZE, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(BrokerConfig.SEND_QUEUE_CAPACITY),
                new NamedThreadFactory("mqtt-send-" + "thread-pool"));
    }

    // topic: $groupId/locked/$uid
    // target clientId: $uid
    // groupId: topic.split("/")[0]
    @Override public void handleRequestOneway(BizContext bizCtx, MqttPublishMessage msg) {
        String topic = msg.variableHeader().topicName();
        String[] result = topic.split("/");
        String subClientId = result[result.length - 1];
        String groupId = result[0];

        Connection connection = connectionManager.getConnection(groupId, subClientId);

        if (connection != null && connection.isActive()) {
            sendExecutor.execute(new SendMessageTask(connection, topic, msg.payload()));
        }
    }

    @Override public Executor getExecutor() {
        return sendExecutor;
    }

    @Override public boolean oneway() {
        return true;
    }

    private static MqttPublishMessage makePublishMessage(String topic, MqttQoS qos, ByteBuf message,
            int messageId) {
        MqttFixedHeader fixedHeader =
                new MqttFixedHeader(MqttMessageType.PUBLISH, false, qos, false, 0);
        MqttPublishVariableHeader varHeader = new MqttPublishVariableHeader(topic, messageId);
        return new MqttPublishMessage(fixedHeader, varHeader, message);
    }

    class SendMessageTask implements Runnable {
        private Connection connection;
        private String topic;
        private ByteBuf payload;

        public SendMessageTask(Connection connection, String topic, ByteBuf payload) {
            this.connection = connection;
            this.topic = topic;
            this.payload = payload;
        }

        @Override public void run() {
            try {
                final ByteBuf payload = Unpooled.copiedBuffer(this.payload);
                MqttPublishMessage mqttPublishMessage =
                        makePublishMessage(topic, AT_MOST_ONCE, payload.retainedDuplicate(), 0);

                connection.getChannel().writeAndFlush(mqttPublishMessage)
                        .addListener(new ChannelFutureListener() {
                            @Override public void operationComplete(ChannelFuture future)
                                    throws Exception {
                                if (future.isSuccess()) {
                                    logger.info("send success");
                                } else {
                                    logger.error("send fail");
                                }
                            }
                        });
            } catch (Exception e) {
                logger.error(e.getMessage());
            } finally {
                ReferenceCountUtil.release(payload);
            }
        }
    }
}
