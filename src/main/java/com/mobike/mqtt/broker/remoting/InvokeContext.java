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

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mudun
 * @version $Id: InvokeContext.java, v 0.1 2019/4/9 下午2:08 mudun Exp $
 */
public class InvokeContext {
    public final static int                   INITIAL_SIZE           = 8;

    /** context */
    private ConcurrentHashMap<String, Object> context;


    public InvokeContext() {
        this.context = new ConcurrentHashMap<>(INITIAL_SIZE);
    }

    public void put(String key, Object value) {
        this.context.put(key, value);
    }

    public Object get(String key) {
        return this.context.get(key);
    }
}
