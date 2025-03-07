/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hhbigdata.worker.log;

import com.hhbigdata.worker.utils.TaskConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.AbstractDiscriminator;

/**
 * Task Log Discriminator
 */
public class TaskLogDiscriminator extends AbstractDiscriminator<ILoggingEvent> {
    
    private static Logger logger = LoggerFactory.getLogger(TaskLogDiscriminator.class);
    
    /**
     * key
     */
    private String key;
    
    /**
     * log base
     */
    private String logBase;
    
    /**
     * logger name should be like:
     * Task Logger name should be like: Task-{processDefinitionId}-{processInstanceId}-{taskInstanceId}
     */
    @Override
    public String getDiscriminatingValue(ILoggingEvent event) {
        String loggerName = event.getLoggerName();
        String prefix = TaskConstants.TASK_LOG_LOGGER_NAME + "-";
        if (loggerName.startsWith(prefix)) {
            return loggerName.substring(prefix.length(),
                    loggerName.length()).replace("-", "/");
        } else {
            return "unknown_task";
        }
    }
    
    @Override
    public void start() {
        started = true;
    }
    
    @Override
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getLogBase() {
        return logBase;
    }
    
    public void setLogBase(String logBase) {
        this.logBase = logBase;
    }
}
