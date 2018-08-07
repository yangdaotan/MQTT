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
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mudun
 * @version $Id: ConnectionManager.java, v 0.1 2018/8/1 下午5:45 mudun Exp $
 */
public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger("Connection");

    private final ConcurrentHashMap<String/*groupId*/, ConnectionGroup/*group*/> groups;

    public ConnectionManager() {
        this.groups = new ConcurrentHashMap<>();
    }

    public void put(String groupKey, Connection connection) {
        ConnectionGroup group = groups.get(groupKey);
        if (group == null) {
            group = new ConnectionGroup(connection.getGroupId());
        }

        if (group.isEmpty()) {
            group.put(connection.getUniqueId(), connection);
            groups.put(groupKey, group);
        } else {
            Connection exist = group.putIfAbsent(connection.getUniqueId(), connection);
            if (exist != null) {
                logger.warn("clientId is used in connection, close the previous connection");
                exist.getChannel().attr(ServerConfigs.CHANNEL_STATUS).set(ServerConfigs.RECONNECT);
                exist.close();
                group.put(connection.getUniqueId(), connection);
            }
        }
    }

    public void remove(Connection connection) {
        ConnectionGroup group = groups.get(connection.getGroupId());
        if (group != null) {
            group.remove(connection.getUniqueId());
        }
    }

    /**
     * @param groupKey
     * @param uniqueKey
     * @return
     */
    public Connection getConnection(String groupKey, String uniqueKey) {
        ConnectionGroup group = groups.get(groupKey);
        return group != null ? group.get(uniqueKey) : null;
    }


    public ConcurrentHashMap<String, ConnectionGroup> getGroups() {
        return groups;
    }

    public int count() {
        return groups.size();
    }
}
