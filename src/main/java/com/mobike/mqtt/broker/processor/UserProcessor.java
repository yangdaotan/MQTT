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
package com.mobike.mqtt.broker.processor;

import com.mobike.mqtt.broker.remoting.BizContext;
import com.mobike.mqtt.broker.remoting.RemotingContext;
import java.util.concurrent.Executor;

/**
 * @author mudun
 * @version $Id: UserProcessor.java, v 0.1 2019/4/9 下午1:46 mudun Exp $
 */
public interface UserProcessor<T> {

    /**
     * Pre handle request biz context.
     *
     * @param remotingCtx the remoting ctx
     * @param request the request
     * @return the biz context
     */
    BizContext preHandleRequest(RemotingContext remotingCtx, T request);

    /**
     * Handle request object.
     *
     * @param bizContext the remoting ctx
     * @param msg the msg
     * @return the object
     */
    Object handleRequest(BizContext bizContext, T msg) throws Exception;

    void handleRequestOneway(BizContext bizContext, T msg) throws Exception;

    boolean oneway();

    /**
     * Gets executor.
     *
     * @return the executor
     */
    Executor getExecutor();

}
