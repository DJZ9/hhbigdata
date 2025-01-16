package com.hhbigdata.worker.strategy;

import com.hhbigdata.common.Constants;
import com.hhbigdata.common.cache.CacheUtils;
import com.hhbigdata.common.command.ServiceRoleOperateCommand;
import com.hhbigdata.common.utils.ExecResult;
import com.hhbigdata.worker.utils.KerberosUtils;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.io.FileUtil;

/**
 * flink
 * 为flink on yarn在kerberos环境下创建keytab文件
 * @author zhangkeyu
 * @since 2024-02-02 22:30
 */
public class FlinkHandlerStrategy extends AbstractHandlerStrategy implements ServiceRoleStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(FlinkHandlerStrategy.class);
    
    public FlinkHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }
    
    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) throws SQLException, ClassNotFoundException {
        
        if (command.getEnableKerberos()) {
            logger.info("start to get flink keytab file");
            String hostname = CacheUtils.getString(Constants.HOSTNAME);
            KerberosUtils.createKeytabDir();
            if (!FileUtil.exist("/etc/security/keytab/flink.keytab")) {
                KerberosUtils.downloadKeytabFromMaster("flink/" + hostname, "flink.keytab");
            }
            
        }
        ExecResult startResult = new ExecResult();
        startResult.setExecResult(true);
        return startResult;
    }
}
