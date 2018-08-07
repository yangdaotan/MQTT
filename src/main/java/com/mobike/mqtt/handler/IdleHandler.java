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
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mudun
 * @version $Id: IdleHandler.java, v 0.1 2018/8/1 下午2:32 mudun Exp $
 */
@ChannelHandler.Sharable
public class IdleHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger("handler");

    @Override public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        if (evt instanceof IdleStateEvent) {
            try {
                logger.warn("connection idle, close it from server, uniqueKey = {}", ctx.channel().attr(
                        ServerConfigs.MQTT_UNIQUE_KEY));
                ctx.close();
            } catch (Exception e) {
                logger.error("Exception caught when closing connection");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
