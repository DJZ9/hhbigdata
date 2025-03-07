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

import com.hhbigdata.common.Constants;
import com.hhbigdata.common.command.ExecuteCmdCommand;
import com.hhbigdata.common.utils.ExecResult;
import com.hhbigdata.common.utils.ShellUtils;

import akka.actor.UntypedActor;

public class ExecuteCmdActor extends UntypedActor {
    
    @Override
    public void onReceive(Object msg) throws Throwable {
        if (msg instanceof ExecuteCmdCommand) {
            ExecuteCmdCommand command = (ExecuteCmdCommand) msg;
            ExecResult execResult = ShellUtils.execWithStatus(Constants.INSTALL_PATH, command.getCommands(), 60L);
            getSender().tell(execResult, getSelf());
        } else {
            unhandled(msg);
        }
    }
}
