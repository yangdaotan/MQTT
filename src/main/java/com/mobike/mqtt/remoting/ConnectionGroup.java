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

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mudun
 * @version $Id: ConnectionGroup.java, v 0.1 2018/8/1 下午5:45 mudun Exp $
 */
public class ConnectionGroup {
    /**
     * the group: [uniqueId : connection]
     */
    private final ConcurrentHashMap<String, Connection> group = new ConcurrentHashMap<>();

    /**
     * the groupId
     */
    private String groupId;

    public ConnectionGroup(String groupId) {
        this.groupId = groupId;
    }

    public Connection putIfAbsent(String uniqueKey, Connection connection) {
        return group.putIfAbsent(uniqueKey, connection);
    }

    public void put(String uniqueKey, Connection connection) {
        group.put(uniqueKey, connection);
    }

    public void remove(String uniqueKey) {
        group.remove(uniqueKey);
    }

    public Connection get(String uniqueKey) {
        return group.get(uniqueKey);
    }

    public int size() {
        return group.size();
    }

    public boolean isEmpty() {
        return group.isEmpty();
    }

    public String getGroupId() {
        return groupId;
    }
}
