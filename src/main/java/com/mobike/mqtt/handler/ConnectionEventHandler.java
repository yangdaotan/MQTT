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
package com.mobike.mqtt.handler;

import com.mobike.mqtt.config.ServerConfigs;
import com.mobike.mqtt.remoting.Connection;
import com.mobike.mqtt.remoting.ConnectionManager;
import com.mobike.mqtt.store.RedisStore;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * log the channel status event
 *
 * notes:
 * 1. if call ctx.close()  -> ConnectionEventHandler.close()->ConnectionEventHandler.channelInactive()
 * 2. if call ctx.disconnect -> ConnectionEventHandler.close() -> ConnectionEventHandler.channelInactive()
 *
 *
 * channel.close will trigger every handler.close() from the tail of pipeline
 * ctx.close will trigger every handler.close() before the ctx
 *
 * @author mudun
 * @version $Id: ConnectionEventHandler.java, v 0.1 2018/8/1 下午4:06 mudun Exp $
 */
@ChannelHandler.Sharable public class ConnectionEventHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger("Connection");

    /** the connectionManager*/
    private final ConnectionManager connectionManger;

    /** store of connection status*/
    private final RedisStore redisStore;

    public ConnectionEventHandler(ConnectionManager connectionManger, RedisStore redisStore) {
        this.connectionManger = connectionManger;
        this.redisStore = redisStore;
    }

    /**
     * server close the channel when clientId connection has existed, then call the close
     *
     * @param ctx
     * @param promise
     * @throws Exception
     */
    @Override public void close(ChannelHandlerContext ctx, ChannelPromise promise)
            throws Exception {
        super.close(ctx, promise);
    }

    /**
     * if channel is closed or peer connection lost call the channelInaction remove the connection
     * from the connectionManager
     *
     * @param ctx
     * @throws Exception
     */
    @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("connection channel inactive: {}", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
        Connection connection = ctx.channel().attr(Connection.CONNECTION).get();
        if (connection != null) {
            String status = ctx.channel().attr(ServerConfigs.CHANNEL_STATUS).get();
            //  case 1: connection is lost
            //  case 2: reconnect the same broker, the status is disconnect, we process in connect MqttConnectProcessor.handle()
            if (status == null || !ServerConfigs.RECONNECT.equals(status)) {
                connectionManger.remove(connection);
                redisStore.getClientRouter().del(connection.getUniqueId());
            }
        }
    }

    /**
     * channel get the exception
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.error("netty error : {}, close channel, clientId={}, address={}", cause.getMessage(),
               ctx.channel().attr(ServerConfigs.MQTT_UNIQUE_KEY), ctx.channel().remoteAddress());
        ctx.close();
    }
}
