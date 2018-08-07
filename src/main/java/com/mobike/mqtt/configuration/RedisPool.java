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
package com.mobike.mqtt.configuration;


import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author mudun
 * @version $Id: RedisPool.java, v 0.1 2018/8/3 下午2:39 mudun Exp $
 */
@Configuration
public class RedisPool {
    private static final Logger logger = LoggerFactory.getLogger("Server");

    @Value("${codis.host:127.0.0.1}")
    private String host;

    @Value("${codis.port:6379}")
    private int port;

    @Value("${codis.auth:}")
    private String auth;

    @Value("${codis.maxTotal:20}")
    private int maxTotal;

    @Value("${codis.maxIdle:20}")
    private int maxIdle;

    @Value("${codis.maxWaitMillis:5000}")
    private long maxWaitMillis;

    @Value("${codis.testOnBorrow:false}")
    private boolean tesOnBorrow;

    @Bean(value = "JedisPool", destroyMethod = "close")
    public JedisPool onCreate() {
        logger.info("Initializing Redis store: {}:{}:{}", this.host, this.port, this.auth);

        JedisPool jedisPool;
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMaxWaitMillis(maxWaitMillis);
        config.setTestOnBorrow(tesOnBorrow);

        if (StringUtils.isBlank(auth)) {
            jedisPool = new JedisPool(config, host, port, 5*1000);
        } else {
            jedisPool = new JedisPool(config, host, port, 5*1000, auth);
        }

        warmUp(jedisPool, config);

        return jedisPool;
    }

    private void warmUp(JedisPool jedisPool, JedisPoolConfig config) {
        List<Jedis> minIdleJedisList = new ArrayList<>(config.getMinIdle());
        for (int i = 0; i < config.getMaxIdle(); ++i) {
            try {
                Jedis jedis = jedisPool.getResource();
                minIdleJedisList.add(jedis);
                logger.info("ping redis resource - {}: {}", i, jedis.ping());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        for (int i = 0; i < config.getMaxIdle(); ++i) {
            try {
                Jedis jedis = minIdleJedisList.get(i);
                jedis.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        logger.info("hot the jedis pool complete.");
    }
}
