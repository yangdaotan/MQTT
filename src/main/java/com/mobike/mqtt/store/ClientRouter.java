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
package com.mobike.mqtt.store;

import redis.clients.jedis.JedisPool;

/**
 * get the clientId in which broker
 *
 * @author mudun
 * @version $Id: ClientRouter.java, v 0.1 2018/8/3 下午2:30 mudun Exp $
 */
public class ClientRouter extends RedisBase {

    private String KEY_PREFIX = "mqtt_client_route_";

    private String brokerIP;

    public ClientRouter(JedisPool jedisPool, String brokerIP) {
        super(jedisPool);
        this.brokerIP = brokerIP;
    }

    public void update(String clientId) {
        set(KEY_PREFIX + clientId, brokerIP);
    }

    public void del(String clientId) {
        delete(KEY_PREFIX + clientId);
    }
}
