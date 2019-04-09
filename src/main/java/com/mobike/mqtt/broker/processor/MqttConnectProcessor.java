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

import com.mobike.mqtt.broker.remoting.BizContext;
import com.mobike.mqtt.broker.remoting.Connection;
import com.mobike.mqtt.broker.remoting.ConnectionManager;
import com.mobike.mqtt.broker.store.Store;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import java.nio.charset.Charset;

/**
 * @author mudun
 * @version $Id: MqttConnectProcessor.java, v 0.1 2019/4/9 下午2:12 mudun Exp $
 */
public class MqttConnectProcessor extends AbstractUserProcessor<MqttConnectMessage> {

    private ConnectionManager connectionManager;

    private Store store;

    public MqttConnectProcessor(Store store, ConnectionManager connectionManager) {
        this.store = store;
        this.connectionManager = connectionManager;
    }

    // check the connection valid , add to connection manager, update route info
    // clientId:  groupId-$uid
    // groupId:   split(clientId, '-')[0]
    // username:  $uid
    @Override public Object handleRequest(BizContext bizCtx, MqttConnectMessage msg) throws Exception {
        String clientId = msg.payload().clientIdentifier();
        String username = msg.payload().userName();
        String password = new String(msg.payload().passwordInBytes(), Charset.defaultCharset());
        String groupId = parseGroupId(clientId);

        checkConnection(clientId, username, password);

        // if clientId connection is already exists, keep precious connection and close the new connection
        if (connectionManager.exists(username, groupId)) {
            bizCtx.getChannel().close();
            throw new Exception("connection is already exists");
        }

        connectionManager.put(groupId, new Connection(bizCtx.getChannel(), groupId, username));

        //store.updateClientRoute(bizCtx);

        return connAck(MqttConnectReturnCode.CONNECTION_ACCEPTED, false);
    }

    private MqttConnAckMessage connAck(MqttConnectReturnCode returnCode, boolean sessionPresent) {
        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttConnAckVariableHeader mqttConnAckVariableHeader =
                new MqttConnAckVariableHeader(returnCode, sessionPresent);
        return new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
    }

    private boolean checkConnection(String clientId, String username, String password) {
        // do something...
        return true;
    }

    public String parseGroupId(String clientId) {
        return clientId.split("-")[0];
    }

}
