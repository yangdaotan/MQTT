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
package com.mobike.mqtt.config;

/**
 * @author mudun
 * @version $Id: SystemProperties.java, v 0.1 2018/8/1 下午2:25 mudun Exp $
 */
public class SystemProperties {
    public static boolean tcp_nodely() {
        return getBool(ServerConfigs.TCP_NODELAY, ServerConfigs.TCP_NODELAY_DEFAULT);
    }

    public static int tcp_so_backlog() {
        return getInt(ServerConfigs.TCP_SO_BACKLOG, ServerConfigs.TCP_SO_BACKLOG_DEFAULT);
    }

    public static boolean tcp_so_reuseaddr() {
        return getBool(ServerConfigs.TCP_SO_REUSEADDR, ServerConfigs.TCP_SO_REUSEADDR_DEFAULT);
    }

    public static boolean tcp_so_keepalive() {
        return getBool(ServerConfigs.TCP_SO_KEEPALIVE, ServerConfigs.TCP_SO_KEEPALIVE_DEFAULT);
    }

    public static int netty_io_ratio() {
        return getInt(ServerConfigs.NETTY_IO_RATIO, ServerConfigs.NETTY_IO_RATIO_DEFAULT);
    }

    public static boolean netty_buffer_pooled() {
        return getBool(ServerConfigs.NETTY_BUFFER_POOLED, ServerConfigs.NETTY_BUFFER_POOLED_DEFAULT);
    }

    public static int mqtt_tcp_idle_time() {
        return getInt(ServerConfigs.MQTT_TCP_IDLE_TIME, ServerConfigs.MQTT_TCP_IDLE_TIME_DEFAULT);
    }


    // ~~~ private methods
    protected static boolean getBool(String key, String defaultValue) {
        return Boolean.parseBoolean(System.getProperty(key, defaultValue));
    }

    protected static int getInt(String key, String defaultValue) {
        return Integer.parseInt(System.getProperty(key, defaultValue));
    }

    protected static byte getByte(String key, String defaultValue) {
        return Byte.parseByte(System.getProperty(key, defaultValue));
    }

    protected static long getLong(String key, String defaultValue) {
        return Long.parseLong(System.getProperty(key, defaultValue));
    }
}
