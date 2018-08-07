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
package com.mobike.mqtt.utils;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;

/**
 * @author mudun
 * @version $Id: FormatUtil.java, v 0.1 2018/8/3 下午6:21 mudun Exp $
 */
public class FormatUtil {

    public static String get(ByteBuffer byteBuffer) {
        return Charsets.UTF_8.decode(byteBuffer).toString();
    }

    public static String get(ByteBuf byteBuf) {
        return Charsets.UTF_8.decode(byteBuf.nioBuffer()).toString();
    }

}
