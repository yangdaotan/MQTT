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
package com.mobike.mqtt.broker.remoting;

import com.mobike.mqtt.broker.config.ConnectionEventType;
import com.mobike.mqtt.broker.config.NettyConfig;
import com.mobike.mqtt.broker.handler.ConnectionEventHandler;
import com.mobike.mqtt.broker.handler.MqttHandler;
import com.mobike.mqtt.broker.handler.ServerIdleHandler;
import com.mobike.mqtt.broker.processor.MqttConnectProcessor;
import com.mobike.mqtt.broker.processor.MqttDisconnectProcessor;
import com.mobike.mqtt.broker.processor.MqttPingReqProcessor;
import com.mobike.mqtt.broker.processor.MqttPublishProcessor;
import com.mobike.mqtt.broker.processor.MqttSubscribeProcessor;
import com.mobike.mqtt.broker.processor.MqttUnSubscribeProcessor;
import com.mobike.mqtt.broker.processor.UserProcessor;
import com.mobike.mqtt.broker.store.Store;
import com.mobike.mqtt.broker.utils.NamedThreadFactory;
import com.mobike.mqtt.broker.utils.NettyEventLoopUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mudun
 * @version $Id: MqttServer.java, v 0.1 2019/4/9 下午2:33 mudun Exp $
 */
public class MqttServer extends AbstractRemotingServer {
    private static final Logger logger = LoggerFactory.getLogger("RpcRemoting");

    /**
     * mqtt netty serverBootstrap
     */
    private ServerBootstrap bootstrap;

    /** boss event loop group, boss group should not be daemon, need shutdown manually */
    private final EventLoopGroup bossGroup = NettyEventLoopUtil
            .newEventLoopGroup(1, new NamedThreadFactory("mqtt-tcp-netty-server-boss", false));

    /** worker event loop group. Reuse I/O worker threads between rpc servers. */
    private final static EventLoopGroup workerGroup = NettyEventLoopUtil
            .newEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2,
                    new NamedThreadFactory("mqtt-tcp-netty-server-worker", true));

    /**
     * channel of server bootstrap
     */
    private ChannelFuture channelFuture;

    /** connection event handler */
    private ConnectionEventHandler connectionEventHandler;

    /** connection event listener */
    private ConnectionEventListener connectionEventListener = new ConnectionEventListener();

    /**
     * mqtt handler of mqtt protocol
     */
    private MqttHandler mqttHandler;

    private MqttProcessor mqttProcessor;

    private Store store;

    /** mqtt tcp connection manager */
    private ConnectionManager connectionManager = new ConnectionManager();

    static {
        if (workerGroup instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) workerGroup).setIoRatio(NettyConfig.netty_io_ratio());
        } else if (workerGroup instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) workerGroup).setIoRatio(NettyConfig.netty_io_ratio());
        }
    }

    public MqttServer(int port) {
        super(port);
        mqttProcessor = new MqttProcessor();
    }

    public MqttServer(String ip, int port) {
        super(ip, port);
        mqttProcessor = new MqttProcessor();
    }

    @Override protected void doInit() {
        this.bootstrap = new ServerBootstrap();
        this.bootstrap.group(bossGroup, workerGroup)
                .channel(NettyEventLoopUtil.getServerSocketChannelClass())
                .option(ChannelOption.SO_BACKLOG, NettyConfig.tcp_so_backlog())
                .option(ChannelOption.SO_REUSEADDR, NettyConfig.tcp_so_reuseaddr())
                .childOption(ChannelOption.TCP_NODELAY, NettyConfig.tcp_nodely())
                .childOption(ChannelOption.SO_KEEPALIVE, NettyConfig.tcp_so_keepalive());

        if (NettyConfig.netty_buffer_pooled()) {
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        } else {
            bootstrap.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);
        }

        mqttHandler = new MqttHandler(mqttProcessor);
        connectionEventListener.addConnectionEventProcessor(ConnectionEventType.CLOSE,
                new ConnectionCloseEventProcessor(connectionManager, store));
        connectionEventListener.addConnectionEventProcessor(ConnectionEventType.CONNECT,
                new ConnectionConnectEventProcessor());

        connectionEventHandler = new ConnectionEventHandler(connectionEventListener);

        //store = new RedisStore(null, "");

        registerProcessor(MqttMessageType.CONNECT, new MqttConnectProcessor(store, connectionManager));
        registerProcessor(MqttMessageType.PINGREQ, new MqttPingReqProcessor());
        registerProcessor(MqttMessageType.SUBSCRIBE, new MqttSubscribeProcessor());
        registerProcessor(MqttMessageType.UNSUBSCRIBE, new MqttUnSubscribeProcessor());
        registerProcessor(MqttMessageType.DISCONNECT, new MqttDisconnectProcessor());
        registerProcessor(MqttMessageType.PUBLISH, new MqttPublishProcessor(connectionManager));

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("decoder", new MqttDecoder());
                pipeline.addLast("encoder", MqttEncoder.INSTANCE);
                pipeline.addLast("idleStateHandler",
                        new IdleStateHandler(NettyConfig.mqtt_tcp_idle_time(), 0, 0));
                pipeline.addLast("serverIdleHandler", new ServerIdleHandler());
                pipeline.addLast("connectionEventHandler", connectionEventHandler);
                pipeline.addLast("handler", mqttHandler);
            }
        });

        // enable trigger mode for epoll if need
        NettyEventLoopUtil.enableTriggeredMode(bootstrap);
    }

    @Override protected boolean doStart() throws InterruptedException {
        this.channelFuture = this.bootstrap.bind(new InetSocketAddress(ip(), port())).sync();
        return this.channelFuture.isSuccess();
    }

    @Override protected boolean doStop() {
        if (null != this.channelFuture) {
            this.channelFuture.channel().close();
        }

        this.bossGroup.shutdownGracefully().awaitUninterruptibly();

        this.connectionManager.clear();

        logger.warn("Mqtt Server stopped!");
        return true;
    }

    @Override
    public void registerProcessor(MqttMessageType mqttMessageType, UserProcessor processor) {
        this.mqttProcessor.registerProcessor(mqttMessageType, processor);
    }

    @Override public void registerDefaultExecutor(ExecutorService executor) {

    }
}

