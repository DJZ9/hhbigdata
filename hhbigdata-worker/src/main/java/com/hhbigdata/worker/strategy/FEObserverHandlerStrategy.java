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

package com.hhbigdata.worker.strategy;

import com.hhbigdata.common.command.OlapOpsType;
import com.hhbigdata.common.command.OlapSqlExecCommand;
import com.hhbigdata.common.command.ServiceRoleOperateCommand;
import com.hhbigdata.common.enums.CommandType;
import com.hhbigdata.common.model.ServiceRoleRunner;
import com.hhbigdata.common.utils.ExecResult;
import com.hhbigdata.common.utils.ThrowableUtils;
import com.hhbigdata.worker.handler.ServiceHandler;
import com.hhbigdata.worker.utils.ActorUtils;

import java.util.ArrayList;

import akka.actor.ActorRef;

import cn.hutool.core.net.NetUtil;
import cn.hutool.json.JSONUtil;

public class FEObserverHandlerStrategy extends AbstractHandlerStrategy implements ServiceRoleStrategy {
    
    public FEObserverHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }
    
    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) {
        ExecResult startResult = new ExecResult();
        logger.info("FEObserverHandlerStrategy start fe observer" + JSONUtil.toJsonStr(command));
        ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(), command.getServiceRoleName());
        if (command.getCommandType() == CommandType.INSTALL_SERVICE) {
            logger.info("first start  fe observer");
            ArrayList<String> commands = new ArrayList<>();
            commands.add("--helper");
            commands.add(command.getMasterHost() + ":9010");
            commands.add("--daemon");
            
            ServiceRoleRunner startRunner = new ServiceRoleRunner();
            startRunner.setProgram(command.getStartRunner().getProgram());
            startRunner.setArgs(commands);
            startRunner.setTimeout("60");
            startResult = serviceHandler.start(startRunner, command.getStatusRunner(),
                    command.getDecompressPackageName(), command.getRunAs());
            if (startResult.getExecResult()) {
                // add observer
                try {
                    OlapSqlExecCommand sqlExecCommand = new OlapSqlExecCommand();
                    sqlExecCommand.setFeMaster(command.getMasterHost());
                    sqlExecCommand.setHostName(NetUtil.getLocalhostStr());
                    sqlExecCommand.setOpsType(OlapOpsType.ADD_FE_OBSERVER);
                    ActorUtils.getRemoteActor(command.getManagerHost(), "masterNodeProcessingActor")
                            .tell(sqlExecCommand, ActorRef.noSender());
                } catch (Exception e) {
                    logger.error("add fe observer failed {}", ThrowableUtils.getStackTrace(e));
                }
                logger.info("fe observer start success");
            } else {
                logger.error("fe observer start failed");
            }
        } else {
            startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                    command.getDecompressPackageName(), command.getRunAs());
        }
        return startResult;
    }
}