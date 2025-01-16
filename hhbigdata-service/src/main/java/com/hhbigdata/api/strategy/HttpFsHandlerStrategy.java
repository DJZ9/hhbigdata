package com.hhbigdata.api.strategy;

import com.hhbigdata.api.load.GlobalVariables;
import com.hhbigdata.api.utils.ProcessUtils;
import com.hhbigdata.common.model.ServiceConfig;
import com.hhbigdata.common.model.ServiceRoleInfo;
import com.hhbigdata.dao.entity.ClusterServiceRoleInstanceEntity;

import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollUtil;

public class HttpFsHandlerStrategy implements ServiceRoleStrategy {
    
    @Override
    public void handler(Integer clusterId, List<String> hosts, String serviceName) {
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        if (CollUtil.isNotEmpty(hosts)) {
            ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName, "${httpFs}", hosts.get(0));
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
    public void handlerServiceRoleCheck(ClusterServiceRoleInstanceEntity roleInstanceEntity,
                                        Map<String, ClusterServiceRoleInstanceEntity> map) {
        
    }
}