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
package com.mobike.mqtt.broker.config;

/**
 * @author mudun
 * @version $Id: SystemProperties.java, v 0.1 2018/8/1 下午2:25 mudun Exp $
 */
public class BrokerConfig {
    static public final int MQTT_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    static public final int MQTT_MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    static public final int MQTT_QUEUE_CAPACITY = 10000;

    static public final int SEND_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    static public final int SEND_MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    static public final int SEND_QUEUE_CAPACITY = 100000;
}
