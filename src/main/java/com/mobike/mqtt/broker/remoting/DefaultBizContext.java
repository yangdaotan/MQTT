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

import com.mobike.mqtt.broker.utils.RemotingUtil;
import io.netty.channel.Channel;

/**
 * @author mudun
 * @version $Id: DefaultBizContext.java, v 0.1 2019/4/9 下午2:07 mudun Exp $
 */
public class DefaultBizContext implements BizContext {

    private RemotingContext remotingCtx;

    private InvokeContext invokeContext;

    public DefaultBizContext(RemotingContext remotingCtx) {
        this.invokeContext = new InvokeContext();
        this.remotingCtx = remotingCtx;
    }

    @Override public String getRemoteAddress() {
        return RemotingUtil.parseRemoteAddress(remotingCtx.channel());
    }

    @Override public Connection getConnection() {
        return remotingCtx.channel().attr(Connection.CONNECTION).get();
    }

    @Override public long getArriveTimestamp() {
        return remotingCtx.getArriveTimestamp();
    }

    @Override public void put(String key, String value) {
        this.invokeContext.put(key, value);
    }

    @Override public Object get(String key) {
        return this.invokeContext.get(key);
    }

    @Override public InvokeContext getInvokeContext() {
        return invokeContext;
    }

    @Override public Channel getChannel() {
        return remotingCtx.channel();
    }
}
