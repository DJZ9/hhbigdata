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

package com.hhbigdata.api.service;

import com.hhbigdata.dao.entity.ClusterServiceRoleGroupConfig;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 
 *
 * @author dygao2
 * @email gaodayu2022@163.com
 * @date 2022-08-16 16:56:01
 */
public interface ClusterServiceRoleGroupConfigService extends IService<ClusterServiceRoleGroupConfig> {
    
    ClusterServiceRoleGroupConfig getConfigByRoleGroupId(Integer roleGroupId);
    
    ClusterServiceRoleGroupConfig getConfigByRoleGroupIdAndVersion(Integer roleGroupId, Integer version);
    
    void removeAllByRoleGroupId(Integer roleGroupId);
    
    List<ClusterServiceRoleGroupConfig> listRoleGroupConfigsByRoleGroupIds(List<Integer> roleGroupIds);
}
