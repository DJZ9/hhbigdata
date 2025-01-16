/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.hhbigdata.worker.actor;

import com.hhbigdata.common.command.GenerateServiceConfigCommand;
import com.hhbigdata.common.utils.ExecResult;
import com.hhbigdata.worker.handler.ConfigureServiceHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

public class ConfigureServiceActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigureServiceActor.class);
    
    @Override
    public void onReceive(Object msg) throws Throwable {
        if (msg instanceof GenerateServiceConfigCommand) {
            
            GenerateServiceConfigCommand command = (GenerateServiceConfigCommand) msg;
            logger.info("start configure {}", command.getServiceName());
            ConfigureServiceHandler serviceHandler =
                    new ConfigureServiceHandler(command.getServiceName(), command.getServiceRoleName());
            ExecResult startResult = serviceHandler.configure(command.getConfigFileMap(),
                    command.getDecompressPackageName(),
                    command.getClusterId(),
                    command.getMyid(),
                    command.getServiceRoleName(),
                    command.getRunAs());
            getSender().tell(startResult, getSelf());
            
            logger.info("{} configure result {}", command.getServiceName(),
                    startResult.getExecResult() ? "success" : "failed");
        } else {
            unhandled(msg);
        }
    }
}