/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.consent.mgt.core.model;

import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ConsentMessageContext<T1, T2> implements Serializable {

    private static final long serialVersionUID = 4937506590904486749L;
    protected Map<T1, T2> parameters = new HashMap<>();

    public ConsentMessageContext(Map<T1, T2> parameters) {

    }

    public ConsentMessageContext() {

    }

    public void addParameter(T1 key, T2 value) throws ConsentManagementClientException {

        if (this.parameters.containsKey(key)) {
            throw new ConsentManagementClientException("Parameters map trying to override existing key " + key, "");
        } else {
            this.parameters.put(key, value);
        }
    }

    public void addParameters(Map<T1, T2> parameters) throws ConsentManagementClientException {

        for (Entry<T1, T2> parameter : parameters.entrySet()) {
            if (this.parameters.containsKey(parameter.getKey())) {
                throw new ConsentManagementClientException("Parameters map trying to override existing key " +
                        parameter.getKey(), "");
            }
            parameters.put(parameter.getKey(), parameter.getValue());
        }

    }

    public Map<T1, T2> getParameters() {

        return Collections.unmodifiableMap(this.parameters);
    }

    public T2 getParameter(T1 key) {

        return this.parameters.get(key);
    }
}
