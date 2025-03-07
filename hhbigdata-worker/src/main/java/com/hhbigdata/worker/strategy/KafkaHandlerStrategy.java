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

import com.hhbigdata.common.Constants;
import com.hhbigdata.common.cache.CacheUtils;
import com.hhbigdata.common.command.ServiceRoleOperateCommand;
import com.hhbigdata.common.utils.ExecResult;
import com.hhbigdata.worker.handler.ServiceHandler;
import com.hhbigdata.worker.utils.KerberosUtils;

import cn.hutool.core.io.FileUtil;

public class KafkaHandlerStrategy extends AbstractHandlerStrategy implements ServiceRoleStrategy {
    
    public KafkaHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }
    
    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) {
        ExecResult startResult = new ExecResult();
        ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(), command.getServiceRoleName());
        if (command.getEnableKerberos()) {
            logger.info("start to get hive keytab file");
            String hostname = CacheUtils.getString(Constants.HOSTNAME);
            KerberosUtils.createKeytabDir();
            if (!FileUtil.exist("/etc/security/keytab/kafka.service.keytab")) {
                KerberosUtils.downloadKeytabFromMaster("kafka/" + hostname, "kafka.service.keytab");
            }
        }
        startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                command.getDecompressPackageName(), command.getRunAs());
        return startResult;
    }
}
