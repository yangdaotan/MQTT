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
package com.mobike.mqtt.broker.log;

import com.alipay.sofa.common.log.MultiAppLoggerSpaceManager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * @author mudun
 * @version $Id: MqttLoggerFactory.java, v 0.1 2019/4/8 下午4:51 mudun Exp $
 */
public class MqttLoggerFactory {
    private static final String MQTT_BROKER_LOG_SPACE = "com.mobike.mqtt.broker";

    private static final String LOG_PATH                  = "logging.path";
    private static final String LOG_PATH_DEFAULT          = System.getProperty("user.home")
            + File.separator + "logs";
    private static final String LOG_LEVEL          = "com.mobike.mqtt.broker.log.level";
    private static final String LOG_LEVEL_DEFAULT  = "INFO";
    private static final String LOG_ENCODE         = "com.mobike.mqtt.broker.log.encode";
    private static final String COMMON_ENCODE             = "file.encoding";
    private static final String LOG_ENCODE_DEFAULT = "UTF-8";

    static {
        //SpaceId init properties
        Map spaceIdProperties = new HashMap<String, String>();
        MultiAppLoggerSpaceManager.init(MQTT_BROKER_LOG_SPACE, spaceIdProperties);

        String logPath = System.getProperty(LOG_PATH);
        if (StringUtils.isBlank(logPath)) {
            System.setProperty(LOG_PATH, LOG_PATH_DEFAULT);
        }

        String logLevel = System.getProperty(LOG_LEVEL);
        if (StringUtils.isBlank(logLevel)) {
            System.setProperty(LOG_LEVEL, LOG_LEVEL_DEFAULT);
        }

        String commonEncode = System.getProperty(COMMON_ENCODE);
        if (StringUtils.isNotBlank(commonEncode)) {
            System.setProperty(LOG_ENCODE, commonEncode);
        } else {
            String logEncode = System.getProperty(LOG_ENCODE);
            if (StringUtils.isBlank(logEncode)) {
                System.setProperty(LOG_ENCODE, LOG_ENCODE_DEFAULT);
            }
        }
    }

    public static Logger getLogger(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return getLogger(clazz.getCanonicalName());
    }

    public static Logger getLogger(String name) {
        //From "com/mobike/mqtt/broker/log" get the xml configuration and init,then get the logger object
        return MultiAppLoggerSpaceManager.getLoggerBySpace(name, MQTT_BROKER_LOG_SPACE);
    }
}
