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

import com.hhbigdata.api.service.SessionService;
import com.hhbigdata.api.utils.HttpUtils;
import com.hhbigdata.common.Constants;
import com.hhbigdata.dao.entity.SessionEntity;
import com.hhbigdata.dao.entity.UserInfoEntity;
import com.hhbigdata.dao.mapper.SessionMapper;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("sessionService")
public class SessionServiceImpl extends ServiceImpl<SessionMapper, SessionEntity> implements SessionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
    
    @Autowired
    private SessionMapper sessionMapper;
    
    /**
     * get user session from request
     *
     * @param request request
     * @return session
     */
    @Override
    public SessionEntity getSession(HttpServletRequest request) {
        String sessionId = request.getHeader(Constants.SESSION_ID);
        
        if (StringUtils.isBlank(sessionId)) {
            Cookie cookie = WebUtils.getCookie(request, Constants.SESSION_ID);
            
            if (cookie != null) {
                sessionId = cookie.getValue();
            }
        }
        
        if (StringUtils.isBlank(sessionId)) {
            return null;
        }
        
        String ip = HttpUtils.getClientIpAddress(request);
        logger.debug("get session: {}, ip: {}", sessionId, ip);
        
        return sessionMapper.selectById(sessionId);
    }
    
    /**
     * create session
     *
     * @param user user
     * @param ip   ip
     * @return session string
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createSession(UserInfoEntity user, String ip) {
        SessionEntity session = null;
        
        // logined
        List<SessionEntity> sessionList = sessionMapper.queryByUserId(user.getId());
        
        Date now = new Date();
        
        /**
         * if you have logged in and are still valid, return directly
         */
        if (CollectionUtils.isNotEmpty(sessionList)) {
            // is session list greater 1 ， delete other ，get one
            if (sessionList.size() > 1) {
                for (int i = 1; i < sessionList.size(); i++) {
                    sessionMapper.deleteById(sessionList.get(i).getId());
                }
            }
            session = sessionList.get(0);
            if (now.getTime() - session.getLastLoginTime().getTime() <= Constants.SESSION_TIME_OUT * 1000) {
                /**
                 * updateProcessInstance the latest login time
                 */
                session.setLastLoginTime(now);
                sessionMapper.updateById(session);
                
                return session.getId();
                
            } else {
                /**
                 * session expired, then delete this session first
                 */
                sessionMapper.deleteById(session.getId());
            }
        }
        
        // assign new session
        session = new SessionEntity();
        
        session.setId(UUID.randomUUID().toString());
        session.setIp(ip);
        session.setUserId(user.getId());
        session.setLastLoginTime(now);
        
        sessionMapper.insertSession(session);
        
        return session.getId();
    }
    
    /**
     * sign out
     * remove ip restrictions
     *
     * @param ip        no use
     * @param loginUser login user
     */
    @Override
    public void signOut(String ip, UserInfoEntity loginUser) {
        try {
            /**
             * query session by user id and ip
             */
            SessionEntity session = sessionMapper.queryByUserIdAndIp(loginUser.getId(), ip);
            
            // delete session
            sessionMapper.deleteById(session.getId());
        } catch (Exception e) {
            logger.warn("userId : {} , ip : {} , find more one session", loginUser.getId(), ip);
        }
    }
    
}
