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

import com.hhbigdata.common.utils.Result;
import com.hhbigdata.dao.entity.ClusterAlertHistory;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 集群告警历史表 
 *
 * @author gaodayu
 * @email gaodayu2022@163.com
 * @date 2022-06-07 12:04:38
 */
public interface ClusterAlertHistoryService extends IService<ClusterAlertHistory> {
    
    void saveAlertHistory(String alertMessage);
    
    Result getAlertList(Integer serviceInstanceId);
    
    Result getAllAlertList(Integer clusterId, Integer page, Integer pageSize);
    
    void removeAlertByRoleInstanceIds(List<Integer> ids);
}
