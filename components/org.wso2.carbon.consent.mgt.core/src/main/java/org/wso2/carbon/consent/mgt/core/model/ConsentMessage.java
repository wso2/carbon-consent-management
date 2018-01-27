/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.consent.mgt.core.model;

import java.util.Map;

/**
 * Model class to pass to the interceptors.
 */
public class ConsentMessage {

    private String operation;
    private Map<String, Object> operationProperties;
    private ConsentMessageContext context;

    public ConsentMessage(String operation, Map<String, Object> operationProperties, ConsentMessageContext context) {

        this.operation = operation;
        this.operationProperties = operationProperties;
        this.context = context;
    }

    /**
     * Get Operation Name.
     *
     * @return Name of the operation.
     */
    public String getOperation() {

        return operation;
    }

    /**
     * Get operation attributes.
     *
     * @return map of operation properties.
     */
    public Map<String, Object> getOperationProperties() {

        return operationProperties;
    }

    /**
     * Return a common context across interceptors.
     *
     * @return context.
     */
    public ConsentMessageContext getContext() {

        return context;
    }
}
