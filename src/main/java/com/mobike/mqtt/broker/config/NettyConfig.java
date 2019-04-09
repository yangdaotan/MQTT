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

import io.netty.util.AttributeKey;

/**
 * @author mudun
 * @version $Id: ServerConfigs.java, v 0.1 2018/8/1 下午2:20 mudun Exp $
 */
public class NettyConfig {

    /** TCP_NODELAY option */
    public static final String TCP_NODELAY                           = "mot.tcp.nodelay";
    public static final String TCP_NODELAY_DEFAULT                   = "true";

    /** TCP SO_REUSEADDR option */
    public static final String TCP_SO_REUSEADDR                      = "mot.tcp.so.reuseaddr";
    public static final String TCP_SO_REUSEADDR_DEFAULT              = "true";

    /** TCP SO_BACKLOG option */
    public static final String TCP_SO_BACKLOG                        = "mot.tcp.so.backlog";
    public static final String TCP_SO_BACKLOG_DEFAULT                = "65535";

    /** TCP SO_KEEPALIVE option */
    public static final String TCP_SO_KEEPALIVE                      = "mot.tcp.so.keepalive";
    public static final String TCP_SO_KEEPALIVE_DEFAULT              = "true";

    /** Netty ioRatio option*/
    public static final String NETTY_IO_RATIO                        = "mot.netty.io.ratio";
    public static final String NETTY_IO_RATIO_DEFAULT                = "70";

    /** Netty buffer allocator */
    public static final String NETTY_BUFFER_POOLED                   = "mot.netty.buffer.pooled";
    public static final String NETTY_BUFFER_POOLED_DEFAULT           = "true";

    /** Netty epoll switch */
    public static final String NETTY_EPOLL_SWITCH                    = "MQTT.netty.epoll.switch";
    public static final String NETTY_EPOLL_SWITCH_DEFAULT            = "true";

    /** Netty epoll level trigger enabled */
    public static final String NETTY_EPOLL_LT                        = "MQTT.netty.epoll.lt";
    public static final String NETTY_EPOLL_LT_DEFAULT                = "true";

    public static final String MQTT_TCP_IDLE_TIME = "mqtt.tcp.idle.time";
    public static final String MQTT_TCP_IDLE_TIME_DEFAULT = "60";

    public static final String ATTR_CLIENT_ID = "clientID";
    public static final AttributeKey<String> MQTT_UNIQUE_KEY = AttributeKey.valueOf(ATTR_CLIENT_ID);

    public static final String ATTR_CLIENT_STATUS = "status";
    public static final AttributeKey<String> CHANNEL_STATUS = AttributeKey.valueOf(ATTR_CLIENT_STATUS);

    public static final String MQTT_TCP_HOST = "0.0.0.0";
    public static final int MQTT_TCP_PORT = 1883;

    public static final String MQTT_WEBSOCKET_TCP_HOST = "0.0.0.0";
    public static final int MQTT_WEBSOCKET_TCP_PORT = 8080;

    public static final String CONNECT = "connect";
    public static final String CONNECT_LOST = "connectionLost";
    public static final String DISCONNECT = "disconnect";
    public static final String RECONNECT = "reconnect";
    public static final String SUBSCRIBE = "subscribe";
    public static final String UNSUBSCRIBE = "unsubscribe";

    public static final String RECEIVED_MSG = "msgReceived";
    public static final String SEND_MSG = "msgSent";
    public static final String SEND_IN_ACTIVE = "inactive";


    public static boolean tcp_nodely() {
        return getBool(NettyConfig.TCP_NODELAY, NettyConfig.TCP_NODELAY_DEFAULT);
    }

    public static int tcp_so_backlog() {
        return getInt(NettyConfig.TCP_SO_BACKLOG, NettyConfig.TCP_SO_BACKLOG_DEFAULT);
    }

    public static boolean tcp_so_reuseaddr() {
        return getBool(NettyConfig.TCP_SO_REUSEADDR, NettyConfig.TCP_SO_REUSEADDR_DEFAULT);
    }

    public static boolean tcp_so_keepalive() {
        return getBool(NettyConfig.TCP_SO_KEEPALIVE, NettyConfig.TCP_SO_KEEPALIVE_DEFAULT);
    }

    public static int netty_io_ratio() {
        return getInt(NettyConfig.NETTY_IO_RATIO, NettyConfig.NETTY_IO_RATIO_DEFAULT);
    }

    public static boolean netty_buffer_pooled() {
        return getBool(NettyConfig.NETTY_BUFFER_POOLED, NettyConfig.NETTY_BUFFER_POOLED_DEFAULT);
    }

    public static int mqtt_tcp_idle_time() {
        return getInt(NettyConfig.MQTT_TCP_IDLE_TIME, NettyConfig.MQTT_TCP_IDLE_TIME_DEFAULT);
    }

    public static boolean netty_epoll() {
        return getBool(NettyConfig.NETTY_EPOLL_SWITCH, NettyConfig.NETTY_EPOLL_SWITCH_DEFAULT);
    }

    public static boolean netty_epoll_lt_enabled() {
        return getBool(NettyConfig.NETTY_EPOLL_LT, NettyConfig.NETTY_EPOLL_LT_DEFAULT);
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
