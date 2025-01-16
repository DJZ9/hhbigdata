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

package com.hhbigdata.api.service.impl;

import com.hhbigdata.api.enums.Status;
import com.hhbigdata.api.service.ClusterQueueCapacityService;
import com.hhbigdata.api.service.ClusterServiceRoleInstanceService;
import com.hhbigdata.api.utils.HadoopUtils;
import com.hhbigdata.api.utils.ProcessUtils;
import com.hhbigdata.common.Constants;
import com.hhbigdata.common.model.Generators;
import com.hhbigdata.common.model.ServiceConfig;
import com.hhbigdata.common.utils.ExecResult;
import com.hhbigdata.common.utils.Result;
import com.hhbigdata.dao.entity.ClusterInfoEntity;
import com.hhbigdata.dao.entity.ClusterQueueCapacity;
import com.hhbigdata.dao.entity.ClusterServiceRoleInstanceEntity;
import com.hhbigdata.dao.mapper.ClusterQueueCapacityMapper;
import com.hhbigdata.dao.model.ClusterQueueCapacityList;
import com.hhbigdata.dao.model.Links;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.bean.BeanUtil;

@Service("clusterQueueCapacityService")
public class ClusterQueueCapacityServiceImpl extends ServiceImpl<ClusterQueueCapacityMapper, ClusterQueueCapacity>
        implements
            ClusterQueueCapacityService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterQueueCapacityServiceImpl.class);
    
    @Autowired
    private ClusterServiceRoleInstanceService roleInstanceService;
    
    @Override
    public Result refreshToYarn(Integer clusterId) throws Exception {
        List<ClusterQueueCapacity> list = this.list(new QueryWrapper<ClusterQueueCapacity>()
                .eq(Constants.CLUSTER_ID, clusterId));
        ClusterInfoEntity clusterInfo = ProcessUtils.getClusterInfo(clusterId);
        List<ClusterServiceRoleInstanceEntity> roleList =
                roleInstanceService.getServiceRoleInstanceListByClusterIdAndRoleName(clusterId, "ResourceManager");
        
        // build configfilemap
        HashMap<Generators, List<ServiceConfig>> configFileMap = new HashMap<>();
        Generators generators = new Generators();
        generators.setFilename("capacity-scheduler.xml");
        generators.setOutputDirectory("etc/hadoop");
        generators.setConfigFormat("custom");
        generators.setTemplateName("capacity-scheduler.ftl");
        
        ArrayList<ServiceConfig> serviceConfigs = new ArrayList<>();
        ServiceConfig config = new ServiceConfig();
        ArrayList<JSONObject> queueList = new ArrayList<>();
        
        for (ClusterQueueCapacity clusterYarnQueue : list) {
            JSONObject queue = new JSONObject();
            BeanUtil.copyProperties(clusterYarnQueue, queue, false);
            queueList.add(queue);
        }
        
        config.setName("queueList");
        config.setValue(queueList);
        config.setConfigType("map");
        config.setRequired(true);
        
        serviceConfigs.add(config);
        
        configFileMap.put(generators, serviceConfigs);
        String hostname = "";
        for (ClusterServiceRoleInstanceEntity roleInstanceEntity : roleList) {
            ExecResult execResult = HadoopUtils.configQueueProp(clusterInfo, configFileMap, roleInstanceEntity);
            if (!execResult.getExecResult()) {
                return Result.error("config capacity-scheduler.xml failed");
            }
            if (StringUtils.isBlank(hostname)) {
                hostname = roleInstanceEntity.getHostname();
            }
        }
        ExecResult execResult = HadoopUtils.refreshQueuePropToYarn(clusterInfo, hostname);
        if (execResult.getExecResult()) {
            logger.info("yarn dfsadmin -refreshQueues success at {}", hostname);
        } else {
            logger.info(execResult.getExecOut());
            return Result.error(Status.FAILED_REFRESH_THE_QUEUE_TO_YARN.getMsg());
        }
        return Result.success();
    }
    
    @Override
    public void createDefaultQueue(Integer clusterId) {
        ClusterQueueCapacity queueCapacity = new ClusterQueueCapacity();
        queueCapacity.setCapacity("100");
        queueCapacity.setClusterId(clusterId);
        queueCapacity.setQueueName("default");
        queueCapacity.setNodeLabel("*");
        queueCapacity.setAclUsers("*");
        queueCapacity.setParent("root");
        this.save(queueCapacity);
    }
    
    @Override
    public Result listCapacityQueue(Integer clusterId) {
        List<ClusterQueueCapacity> list = this.list(new QueryWrapper<ClusterQueueCapacity>()
                .eq(Constants.CLUSTER_ID, clusterId));
        
        ClusterQueueCapacityList clusterQueueCapacityList = new ClusterQueueCapacityList();
        clusterQueueCapacityList.setRootId("root");
        clusterQueueCapacityList.setNodes(list);
        
        ArrayList<Links> linksList = new ArrayList<>();
        for (ClusterQueueCapacity clusterQueueCapacity : list) {
            Links links = new Links();
            links.setFrom(clusterQueueCapacity.getParent());
            links.setTo(clusterQueueCapacity.getQueueName());
            linksList.add(links);
        }
        clusterQueueCapacityList.setLinks(linksList);
        return Result.success(clusterQueueCapacityList);
    }
    
}
