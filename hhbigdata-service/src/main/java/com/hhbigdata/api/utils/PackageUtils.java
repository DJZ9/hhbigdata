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

package com.hhbigdata.api.utils;

import com.hhbigdata.common.Constants;

import java.util.HashMap;

public class PackageUtils {
    
    static HashMap<String, String> map = new HashMap<String, String>();
    
    public static void putServicePackageName(String frameCode, String serviceName, String dcPackageName) {
        map.put(frameCode + Constants.UNDERLINE + serviceName, dcPackageName);
    }
    
    public static String getServiceDcPackageName(String frameCode, String serviceName) {
        return map.get(frameCode + Constants.UNDERLINE + serviceName);
    }
}
