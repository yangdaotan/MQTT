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

import com.mobike.mqtt.remoting.Connection;
import com.mobike.mqtt.remoting.ConnectionManager;
import com.mobike.mqtt.remoting.RemotingContext;
import com.mobike.mqtt.store.RedisStore;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mudun
 * @version $Id: MqttConnectProcessor.java, v 0.1 2018/8/1 下午3:12 mudun Exp $
 */
public class MqttConnectProcessor implements RequestCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger("Connection");

    private final ConnectionManager connectionManager;

    private final RedisStore redisStore;

    public MqttConnectProcessor(ConnectionManager connectionManager, RedisStore redisStore) {
        this.connectionManager = connectionManager;
        this.redisStore = redisStore;
    }

    @Override public void handle(MqttMessage message, RemotingContext ctx) {
        MqttConnectMessage msg = (MqttConnectMessage) message;
        String clientId = msg.payload().clientIdentifier();
        String groupKey = parseGroupKey(clientId);
        String username = msg.payload().userName();
        String password = new String(msg.payload().passwordInBytes(), CharsetUtil.UTF_8);
        int keepalive = Math.round(1.5f * msg.variableHeader().keepAliveTimeSeconds());
        //todo add auth
        MqttConnAckMessage connAckMessage =
                connAck(MqttConnectReturnCode.CONNECTION_ACCEPTED, false);
        ctx.writeAndFlush(connAckMessage).addListener(new ChannelFutureListener() {
            @Override public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    connectionManager.put(groupKey, new Connection(ctx.channel(), groupKey, username));
                    logger.info("add the new connection success, clientId={}", clientId);
                } else {
                    logger.error("add the new connection failed, clientId={}", clientId);
                }
            }
        });
        // replace client handler for the keepalive handler
        ctx.channel().pipeline().replace("idleStateHandler", "idleStateHandler",
                new IdleStateHandler(keepalive, 0, 0));

        redisStore.getClientRouter().update(username);
    }

    private MqttConnAckMessage connAck(MqttConnectReturnCode returnCode, boolean sessionPresent) {
        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttConnAckVariableHeader mqttConnAckVariableHeader =
                new MqttConnAckVariableHeader(returnCode, sessionPresent);
        return new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
    }

    public String parseGroupKey(String clientId) {
        return clientId.split("-")[0];
    }

}
