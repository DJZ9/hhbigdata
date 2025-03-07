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

package com.hhbigdata.api.master;

import com.hhbigdata.api.service.ClusterInfoService;
import com.hhbigdata.api.service.ClusterServiceCommandHostCommandService;
import com.hhbigdata.api.service.ClusterServiceCommandService;
import com.hhbigdata.api.service.FrameServiceRoleService;
import com.hhbigdata.api.service.FrameServiceService;
import com.hhbigdata.api.strategy.ServiceRoleStrategy;
import com.hhbigdata.api.strategy.ServiceRoleStrategyContext;
import com.hhbigdata.api.utils.SpringTool;
import com.hhbigdata.common.Constants;
import com.hhbigdata.common.command.StartExecuteCommandCommand;
import com.hhbigdata.common.command.SubmitActiveTaskNodeCommand;
import com.hhbigdata.common.enums.CommandType;
import com.hhbigdata.common.enums.ServiceExecuteState;
import com.hhbigdata.common.enums.ServiceRoleType;
import com.hhbigdata.common.model.DAGGraph;
import com.hhbigdata.common.model.ServiceNode;
import com.hhbigdata.common.model.ServiceRoleInfo;
import com.hhbigdata.dao.entity.ClusterInfoEntity;
import com.hhbigdata.dao.entity.ClusterServiceCommandEntity;
import com.hhbigdata.dao.entity.ClusterServiceCommandHostCommandEntity;
import com.hhbigdata.dao.entity.FrameServiceEntity;
import com.hhbigdata.dao.entity.FrameServiceRoleEntity;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import cn.hutool.core.util.ArrayUtil;

public class DAGBuildActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(DAGBuildActor.class);
    
    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof StartExecuteCommandCommand) {
            DAGGraph<String, ServiceNode, String> dag = new DAGGraph<>();
            
            StartExecuteCommandCommand executeCommandCommand = (StartExecuteCommandCommand) message;
            CommandType commandType = executeCommandCommand.getCommandType();
            logger.info("start execute command");
            
            ClusterServiceCommandService commandService =
                    SpringTool.getApplicationContext().getBean(ClusterServiceCommandService.class);
            ClusterServiceCommandHostCommandService hostCommandService =
                    SpringTool.getApplicationContext().getBean(ClusterServiceCommandHostCommandService.class);
            FrameServiceRoleService frameServiceRoleService =
                    SpringTool.getApplicationContext().getBean(FrameServiceRoleService.class);
            FrameServiceService frameService = SpringTool.getApplicationContext().getBean(FrameServiceService.class);
            ClusterInfoService clusterInfoService =
                    SpringTool.getApplicationContext().getBean(ClusterInfoService.class);
            
            ClusterInfoEntity clusterInfo = clusterInfoService.getById(executeCommandCommand.getClusterId());
            List<ClusterServiceCommandEntity> commandList = commandService.lambdaQuery()
                    .in(ClusterServiceCommandEntity::getCommandId, executeCommandCommand.getCommandIds()).list();
            
            ArrayList<FrameServiceEntity> frameServiceList = new ArrayList<>();
            if (ArrayUtil.isNotEmpty(commandList)) {
                for (ClusterServiceCommandEntity command : commandList) {
                    // build dag
                    List<ServiceRoleInfo> masterRoles = new ArrayList<>();
                    List<ServiceRoleInfo> elseRoles = new ArrayList<>();
                    ServiceNode serviceNode = new ServiceNode();
                    
                    List<ClusterServiceCommandHostCommandEntity> hostCommandList =
                            hostCommandService.getHostCommandListByCommandId(command.getCommandId());
                    
                    FrameServiceEntity serviceEntity = frameService.getServiceByFrameCodeAndServiceName(
                            clusterInfo.getClusterFrame(), command.getServiceName());
                    frameServiceList.add(serviceEntity);
                    
                    serviceNode.setCommandId(command.getCommandId());
                    for (ClusterServiceCommandHostCommandEntity hostCommand : hostCommandList) {
                        logger.info("service role is {}", hostCommand.getServiceRoleName());
                        FrameServiceRoleEntity frameServiceRoleEntity =
                                frameServiceRoleService.getServiceRoleByFrameCodeAndServiceRoleName(
                                        clusterInfo.getClusterFrame(), hostCommand.getServiceRoleName());
                        
                        ServiceRoleInfo serviceRoleInfo = JSONObject
                                .parseObject(frameServiceRoleEntity.getServiceRoleJson(), ServiceRoleInfo.class);
                        serviceRoleInfo.setHostname(hostCommand.getHostname());
                        serviceRoleInfo.setHostCommandId(hostCommand.getHostCommandId());
                        serviceRoleInfo.setClusterId(clusterInfo.getId());
                        serviceRoleInfo.setParentName(command.getServiceName());
                        serviceRoleInfo.setPackageName(serviceEntity.getPackageName());
                        serviceRoleInfo.setDecompressPackageName(serviceEntity.getDecompressPackageName());
                        serviceRoleInfo.setCommandType(commandType);
                        serviceRoleInfo.setServiceInstanceId(command.getServiceInstanceId());
                        serviceRoleInfo.setFrameCode(serviceEntity.getFrameCode());
                        
                        ServiceRoleStrategy serviceRoleHandler =
                                ServiceRoleStrategyContext.getServiceRoleHandler(serviceRoleInfo.getName());
                        if (Objects.nonNull(serviceRoleHandler)) {
                            serviceRoleHandler.handlerServiceRoleInfo(serviceRoleInfo, hostCommand.getHostname());
                        }
                        
                        if (ServiceRoleType.MASTER.equals(serviceRoleInfo.getRoleType())) {
                            masterRoles.add(serviceRoleInfo);
                        } else {
                            elseRoles.add(serviceRoleInfo);
                        }
                    }
                    serviceNode.setMasterRoles(masterRoles);
                    serviceNode.setElseRoles(elseRoles);
                    dag.addNode(command.getServiceName(), serviceNode);
                }
                // build edge
                for (FrameServiceEntity serviceEntity : frameServiceList) {
                    if (StringUtils.isNotBlank(serviceEntity.getDependencies())) {
                        for (String dependency : serviceEntity.getDependencies().split(Constants.COMMA)) {
                            if (dag.containsNode(dependency)) {
                                dag.addEdge(dependency, serviceEntity.getServiceName(), false);
                            }
                        }
                    }
                }
            }
            
            if (commandType == CommandType.STOP_SERVICE) {
                logger.info("reverse dag");
                dag = dag.getReverseDagGraph(dag);
            }
            
            Map<String, String> errorTaskList = new ConcurrentHashMap<>();
            Map<String, ServiceExecuteState> activeTaskList = new ConcurrentHashMap<>();
            Map<String, String> readyToSubmitTaskList = new ConcurrentHashMap<>();
            Map<String, String> completeTaskList = new ConcurrentHashMap<>();
            
            Collection<String> beginNode = dag.getBeginNode();
            logger.info("beginNode is {}", beginNode.toString());
            for (String node : beginNode) {
                readyToSubmitTaskList.put(node, "");
            }
            
            SubmitActiveTaskNodeCommand submitActiveTaskNodeCommand = new SubmitActiveTaskNodeCommand();
            submitActiveTaskNodeCommand.setCommandType(executeCommandCommand.getCommandType());
            submitActiveTaskNodeCommand.setDag(dag);
            submitActiveTaskNodeCommand.setClusterId(clusterInfo.getId());
            submitActiveTaskNodeCommand.setActiveTaskList(activeTaskList);
            submitActiveTaskNodeCommand.setErrorTaskList(errorTaskList);
            submitActiveTaskNodeCommand.setReadyToSubmitTaskList(readyToSubmitTaskList);
            submitActiveTaskNodeCommand.setCompleteTaskList(completeTaskList);
            submitActiveTaskNodeCommand.setClusterCode(clusterInfo.getClusterCode());
            
            ActorRef submitTaskNodeActor = ActorUtils.getLocalActor(SubmitTaskNodeActor.class,
                    ActorUtils.getActorRefName(SubmitTaskNodeActor.class));
            submitTaskNodeActor.tell(submitActiveTaskNodeCommand, getSelf());
        }
    }
}
