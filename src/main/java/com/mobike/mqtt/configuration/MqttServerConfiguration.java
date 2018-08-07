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
package com.mobike.mqtt.configuration;

import com.mobike.mqtt.config.ServerConfigs;
import com.mobike.mqtt.remoting.ConnectionManager;
import com.mobike.mqtt.remoting.MqttTcpServer;
import com.mobike.mqtt.remoting.MqttWebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

/**
 * @author mudun
 * @version $Id: MqttTcpServerConfiguration.java, v 0.1 2018/8/1 下午5:19 mudun Exp $
 */
@Configuration public class MqttServerConfiguration {

    @Value("${spring.cloud.client.ipAddress}") String brokerIP;

    @Autowired JedisPool jedisPool;

    @Bean(value = "MqttTcpServer", destroyMethod = "stop") MqttTcpServer onCreate() {

        ConnectionManager connectionManager = new ConnectionManager();

        MqttTcpServer mqttTcpServer =
                new MqttTcpServer(ServerConfigs.MQTT_TCP_PORT, jedisPool, brokerIP,
                        connectionManager);
        mqttTcpServer.start();

        MqttWebSocketServer mqttWebSocketServer =
                new MqttWebSocketServer(ServerConfigs.MQTT_WEBSOCKET_TCP_PORT, jedisPool, brokerIP,
                        connectionManager);
        mqttWebSocketServer.start();

        return mqttTcpServer;
    }
}
