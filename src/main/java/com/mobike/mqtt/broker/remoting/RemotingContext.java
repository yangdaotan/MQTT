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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessageType;

/**
 * @author mudun
 * @version $Id: RemotingContext.java, v 0.1 2019/4/8 下午6:07 mudun Exp $
 */
public class RemotingContext {

    private ChannelHandlerContext channelContext;
    private long arriveTimestamp;
    private MqttMessageType mqttMessageType;

    public RemotingContext(ChannelHandlerContext ctx, MqttMessageType messageType) {
        this.channelContext = ctx;
        this.mqttMessageType = messageType;
        this.arriveTimestamp = System.currentTimeMillis();
    }

    public void close() {
        channelContext.close();
    }

    public ChannelFuture writeAndFlush(Object msg) {
        return this.channelContext.writeAndFlush(msg);
    }

    public ChannelHandlerContext getChannelContext() {
        return channelContext;
    }

    public Channel channel() {
        return channelContext.channel();
    }

    public void setChannelContext(ChannelHandlerContext channelContext) {
        this.channelContext = channelContext;
    }

    public long getArriveTimestamp() {
        return arriveTimestamp;
    }

    public void setArriveTimestamp(long arriveTimestamp) {
        this.arriveTimestamp = arriveTimestamp;
    }

    public MqttMessageType getMqttMessageType() {
        return mqttMessageType;
    }

    public void setMqttMessageType(MqttMessageType mqttMessageType) {
        this.mqttMessageType = mqttMessageType;
    }
}
