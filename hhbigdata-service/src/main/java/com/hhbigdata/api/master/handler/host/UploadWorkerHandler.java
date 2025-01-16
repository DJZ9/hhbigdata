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

package com.hhbigdata.api.master.handler.host;

import com.hhbigdata.api.utils.CommonUtils;
import com.hhbigdata.api.utils.MessageResolverUtils;
import com.hhbigdata.api.utils.MinaUtils;
import com.hhbigdata.common.Constants;
import com.hhbigdata.common.enums.InstallState;
import com.hhbigdata.common.model.HostInfo;

import org.apache.sshd.client.session.ClientSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadWorkerHandler implements DispatcherWorkerHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(StartWorkerHandler.class);
    
    @Override
    public boolean handle(ClientSession session, HostInfo hostInfo) {
        boolean uploadFile = MinaUtils.uploadFile(session,
                Constants.INSTALL_PATH,
                Constants.MASTER_MANAGE_PACKAGE_PATH +
                        Constants.SLASH +
                        Constants.WORKER_PACKAGE_NAME);
        if (uploadFile) {
            hostInfo.setMessage(
                    MessageResolverUtils.getMessage("distribution.successful.and.starts.md5.authentication"));
            hostInfo.setProgress(25);
        } else {
            hostInfo.setMessage(
                    MessageResolverUtils.getMessage("distributed.host.management.agent.installation.package.fail"));
            hostInfo.setErrMsg("dispatcher host agent to " + hostInfo.getHostname() + " failed");
            CommonUtils.updateInstallState(InstallState.FAILED, hostInfo);
        }
        return uploadFile;
    }
}
