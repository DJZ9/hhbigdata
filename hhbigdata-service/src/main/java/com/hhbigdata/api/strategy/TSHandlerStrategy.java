/*
 *
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
 *
 */

package com.hhbigdata.api.strategy;

import com.hhbigdata.api.load.GlobalVariables;
import com.hhbigdata.api.utils.ProcessUtils;
import com.hhbigdata.common.model.ServiceConfig;
import com.hhbigdata.common.model.ServiceRoleInfo;
import com.hhbigdata.dao.entity.ClusterServiceRoleInstanceEntity;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * Yarn Timeline Server
 *
 * @author zhenqin
 *
 */
@Slf4j
public class TSHandlerStrategy implements ServiceRoleStrategy {
    
    @Override
    public void handler(Integer clusterId, List<String> hosts, String serviceName) {
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        if (hosts.size() > 0) {
            ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName, "${yarn_timeline_server}",
                    hosts.get(0));
        }
    }
    
    @Override
    public void handlerConfig(Integer clusterId, List<ServiceConfig> list, String serviceName) {
        
    }
    
    @Override
    public void getConfig(Integer clusterId, List<ServiceConfig> list) {
    }
    
    @Override
    public void handlerServiceRoleInfo(ServiceRoleInfo serviceRoleInfo, String hostname) {
    }
    
    @Override
    public void handlerServiceRoleCheck(
                                        ClusterServiceRoleInstanceEntity roleInstanceEntity,
                                        Map<String, ClusterServiceRoleInstanceEntity> map) {
    }
}
