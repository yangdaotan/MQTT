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
package com.mobike.mqtt.processor;

import com.mobike.mqtt.remoting.RemotingContext;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader.from;
import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

/**
 * @author mudun
 * @version $Id: MqttSubscribeProcessor.java, v 0.1 2018/8/1 下午9:59 mudun Exp $
 */
public class MqttSubscribeProcessor implements RequestCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger("SubscribeHandler");

    @Override public void handle(MqttMessage message, RemotingContext ctx) {
        MqttSubscribeMessage msg = (MqttSubscribeMessage) message;
        int msgId = msg.variableHeader().messageId();

        List<MqttTopicSubscription> topicSubscriptions = msg.payload().topicSubscriptions();
        MqttSubAckMessage mqttSubAckMessage = makeSubAckMessage(topicSubscriptions, msgId);

        ctx.channel().writeAndFlush(mqttSubAckMessage).addListener(new ChannelFutureListener() {
            @Override public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("subscribe success");
                } else {
                    logger.error("subscribe fail");
                }
            }
        });
    }

    private MqttSubAckMessage makeSubAckMessage(List<MqttTopicSubscription> topicFilters,
            int messageId) {
        List<Integer> grantedQoSLevels = new ArrayList<>();
        for (MqttTopicSubscription req : topicFilters) {
            grantedQoSLevels.add(req.qualityOfService().value());
        }

        MqttFixedHeader fixedHeader =
                new MqttFixedHeader(MqttMessageType.SUBACK, false, AT_MOST_ONCE, false, 0);
        MqttSubAckPayload payload = new MqttSubAckPayload(grantedQoSLevels);
        return new MqttSubAckMessage(fixedHeader, from(messageId), payload);
    }
}
