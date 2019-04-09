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
package com.mobike.mqtt.broker.handler;

import com.mobike.mqtt.broker.config.ConnectionEventType;
import com.mobike.mqtt.broker.config.NettyConfig;
import com.mobike.mqtt.broker.log.MqttLoggerFactory;
import com.mobike.mqtt.broker.remoting.Connection;
import com.mobike.mqtt.broker.remoting.ConnectionEventListener;
import com.mobike.mqtt.broker.utils.NamedThreadFactory;
import com.mobike.mqtt.broker.utils.RemotingUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

/**
 * @author mudun
 * @version $Id: ConnectionEventHandler.java, v 0.1 2019/4/9 下午2:23 mudun Exp $
 */
@ChannelHandler.Sharable public class ConnectionEventHandler extends ChannelDuplexHandler {
    private static final Logger logger = MqttLoggerFactory.getLogger("ConnectionEventHandler");

    private ConnectionEventListener eventListener;

    // ConnectionEvent threadPool executor
    private ConnectionEventExecutor eventExecutor;

    public ConnectionEventHandler(ConnectionEventListener eventListener) {
        this.eventListener = eventListener;
        this.eventExecutor = new ConnectionEventExecutor();
    }

    public ConnectionEventHandler(ConnectionEventListener eventListener,
            ConnectionEventExecutor eventExecutor) {
        this.eventListener = eventListener;
        this.eventExecutor = eventExecutor;
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
        String remoteAddress = RemotingUtil.parseRemoteAddress(ctx.channel());
        logger.info("connection channel inactive: {}", remoteAddress);
        super.channelInactive(ctx);
        Connection connection = ctx.channel().attr(Connection.CONNECTION).get();
        if (connection != null) {
            onEvent(connection, remoteAddress, ConnectionEventType.CLOSE);
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
                ctx.channel().attr(NettyConfig.MQTT_UNIQUE_KEY), ctx.channel().remoteAddress());
        ctx.close();
    }

    private void onEvent(final Connection conn, final String remoteAddress,
            final ConnectionEventType type) {
        if (this.eventListener != null) {
            this.eventExecutor.onEvent(new Runnable() {
                @Override
                public void run() {
                    ConnectionEventHandler.this.eventListener.onEvent(type, remoteAddress, conn);
                }
            });
        }
    }

    public class ConnectionEventExecutor {
        Logger          logger   = MqttLoggerFactory.getLogger("ConnectionEvent");
        ExecutorService executor = new ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(10000),
                new NamedThreadFactory("Mqtt-conn-event-executor", true));

        /**
         * Process event.
         *
         * @param event
         */
        public void onEvent(Runnable event) {
            try {
                executor.execute(event);
            } catch (Throwable t) {
                logger.error("Exception caught when execute connection event!", t);
            }
        }
    }

}
