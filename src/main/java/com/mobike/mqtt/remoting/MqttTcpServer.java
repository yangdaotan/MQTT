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
package com.mobike.mqtt.remoting;

import com.mobike.mqtt.config.ServerConfigs;
import com.mobike.mqtt.config.SystemProperties;
import com.mobike.mqtt.handler.ConnectionEventHandler;
import com.mobike.mqtt.handler.IdleHandler;
import com.mobike.mqtt.handler.MqttHandler;
import com.mobike.mqtt.processor.MqttConnectProcessor;
import com.mobike.mqtt.processor.MqttDisconnectProcessor;
import com.mobike.mqtt.processor.MqttPingReqProcessor;
import com.mobike.mqtt.processor.MqttPublishProcessor;
import com.mobike.mqtt.processor.MqttSubscribeProcessor;
import com.mobike.mqtt.processor.MqttUnSubscribeProcessor;
import com.mobike.mqtt.processor.RequestCommandProcessor;
import com.mobike.mqtt.store.RedisStore;
import com.mobike.mqtt.utils.NamedThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

/**
 * mqtt tcp server
 *
 * @author mudun
 * @version $Id: MqttTCPServer.java, v 0.1 2018/8/1 上午11:03 mudun Exp $
 */
public class MqttTcpServer extends RemotingServer {
    private static final Logger logger = LoggerFactory.getLogger("Server");

    /**
     * mqtt netty serverBootstrap
     */
    private final ServerBootstrap bootstrap;

    /** boss event loop group, boss group should not be daemon, need shutdown manually */
    private final EventLoopGroup bossGroup =
            new NioEventLoopGroup(1, new NamedThreadFactory("mqtt-tcp-netty-server-boss", false));

    /** worker event loop group. Reuse I/O worker threads between rpc servers. */
    private final static NioEventLoopGroup workerGroup =
            new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2,
                    new NamedThreadFactory("mqtt-tcp-netty-server-worker", true));

    /**
     * channel of server bootstrap
     */
    private ChannelFuture channelFuture;

    /**
     * mqtt handler of mqtt protocol
     */
    private final MqttHandler mqttHandler;

    /**
     * mqtt tcp connection manager
     */
    private final ConnectionManager connectionManager;

    /**
     * the store of client status
     */
    private final RedisStore redisStore;

    /**
     * mqtt tcp command handler
     */
    private ConcurrentHashMap<MqttMessageType, RequestCommandProcessor> processors;

    public MqttTcpServer(int port, JedisPool jedisPool, String brokerIP,
            ConnectionManager connectionManager) {
        super(port);
        bootstrap = new ServerBootstrap();
        this.connectionManager = connectionManager;
        mqttHandler = new MqttHandler();
        processors = new ConcurrentHashMap<>();
        this.redisStore = new RedisStore(jedisPool, brokerIP);
    }

    @Override public void doInit() {
        // init processors
        processors.putIfAbsent(MqttMessageType.CONNECT,
                new MqttConnectProcessor(connectionManager, redisStore));
        processors.putIfAbsent(MqttMessageType.PUBLISH,
                new MqttPublishProcessor(connectionManager, redisStore));
        processors.putIfAbsent(MqttMessageType.PINGREQ, new MqttPingReqProcessor());
        processors.putIfAbsent(MqttMessageType.DISCONNECT, new MqttDisconnectProcessor());
        processors.putIfAbsent(MqttMessageType.SUBSCRIBE, new MqttSubscribeProcessor());
        processors.putIfAbsent(MqttMessageType.UNSUBSCRIBE, new MqttUnSubscribeProcessor());

        mqttHandler.setProcessors(processors);
        // init bootstrap
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, SystemProperties.tcp_so_backlog())
                .option(ChannelOption.SO_REUSEADDR, SystemProperties.tcp_so_reuseaddr())
                .childOption(ChannelOption.TCP_NODELAY, SystemProperties.tcp_nodely())
                .childOption(ChannelOption.SO_KEEPALIVE, SystemProperties.tcp_so_keepalive());

        if (SystemProperties.netty_buffer_pooled()) {
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        } else {
            bootstrap.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);
        }

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("decoder", new MqttDecoder());
                pipeline.addLast("encoder", MqttEncoder.INSTANCE);
                pipeline.addLast("idleStateHandler",
                        new IdleStateHandler(SystemProperties.mqtt_tcp_idle_time(), 0, 0));
                pipeline.addLast("serverIdleHandler", new IdleHandler());
                pipeline.addLast("connectionManager",
                        new ConnectionEventHandler(connectionManager, redisStore));
                pipeline.addLast("handler", mqttHandler);
            }
        });
    }

    @Override public void doStart() {
        try {
            channelFuture = bootstrap.bind(ServerConfigs.MQTT_TCP_HOST, port);
            channelFuture.sync();
        } catch (InterruptedException e) {
            logger.error("An interruptedException was caught while initializing mqtt tcp server.",
                    e);
        }
    }

    @Override public void doStop() {
        logger.info("closing the mqtt tcp netty server...");
        if (bossGroup == null | workerGroup == null) {
            logger.error("server is not initialized");
            throw new IllegalStateException("Invoked close on an Acceptor that wasn't initialized");
        }

        Future<?> workerWaiter = workerGroup.shutdownGracefully();
        Future<?> bossWaiter = bossGroup.shutdownGracefully();

        logger.info("Waiting for worker and boss event loop groups to terminate...");
        try {
            workerWaiter.await(10, TimeUnit.SECONDS);
            bossWaiter.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn(
                    "An InterruptedException was caught while waiting for event loops to terminate...");
        }

        if (!workerGroup.isTerminated()) {
            logger.warn("Forcing shutdown of worker event loop...");
            workerGroup.shutdownGracefully(0L, 0L, TimeUnit.MILLISECONDS);
        }

        if (!bossGroup.isTerminated()) {
            logger.warn("Forcing shutdown of boss event loop...");
            bossGroup.shutdownGracefully(0L, 0L, TimeUnit.MILLISECONDS);
        }
    }
}
