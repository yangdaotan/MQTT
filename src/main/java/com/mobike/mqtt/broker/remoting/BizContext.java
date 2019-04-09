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

/**
 * @author mudun
 * @version $Id: BizContext.java, v 0.1 2019/4/9 下午1:47 mudun Exp $
 */
public interface BizContext {
    /**
     * Gets remote address.
     *
     * @return the remote address
     */
    String getRemoteAddress();

    /**
     * Gets connection.
     *
     * @return the connection
     */
    Connection getConnection();

    /**
     * Gets arrive timestamp.
     *
     * @return the arrive timestamp
     */
    long getArriveTimestamp();

    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     */
    void put(String key, String value);

    /**
     * Get string.
     *
     * @param key the key
     * @return the string
     */
    Object get(String key);

    /**
     * get invoke context.
     *
     * @return invoke context
     */
    InvokeContext getInvokeContext();

    Channel getChannel();
}
