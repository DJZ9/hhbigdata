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

package com.hhbigdata.domain.host.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum MANAGED {
    
    YES(1, true),
    NO(2, false);
    
    @EnumValue
    private int value;
    
    private boolean desc;
    
    MANAGED(int value, boolean desc) {
        this.value = value;
        this.desc = desc;
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    public boolean isDesc() {
        return desc;
    }
    
    public void setDesc(boolean desc) {
        this.desc = desc;
    }
    
}