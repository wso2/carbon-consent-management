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

import org.wso2.carbon.consent.mgt.core.connector.ConsentMgtInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.RESULT;
import static org.wso2.carbon.consent.mgt.core.util.LambdaExceptionUtils.rethrowConsumer;

/**
 * This template is used to trigger pre interceptor , main logic and post interceptors of an operation.
 *
 * @param <T> The type to be returned from the intercepted method.
 * @param <X> Generic exception thrown. If any.
 */
public class ConsentInterceptorTemplate<T extends Object, X extends Exception> {

    private List<ConsentMgtInterceptor> consentMgtInterceptors;
    private ConsentMessageContext context;
    private T result;

    public ConsentInterceptorTemplate(List<ConsentMgtInterceptor> consentMgtInterceptors, ConsentMessageContext context) {

        this.consentMgtInterceptors = consentMgtInterceptors;
        this.context = context;
    }

    /**
     * Execute the main logic.
     *
     * @param delegate Functional delegate interface.
     * @return ConsentInterceptorTemplate.
     * @throws X Generic exception thrown. If any.
     */
    public ConsentInterceptorTemplate<T, X> executeWith(OperationDelegate<T> delegate) throws X {

        result = delegate.execute();
        return this;
    }

    /**
     * Intercepting PRE and POST interceptors.
     *
     * @param operation Operation name.
     * @param binder    PropertyBinder.
     * @return ConsentInterceptorTemplate.
     */
    public ConsentInterceptorTemplate<T, X> intercept(String operation, PropertyBinder binder) throws X {

        Map<String, Object> operationProperties = new HashMap<>();
        binder.bind(operationProperties);
        if (result != null) {
            operationProperties.put(RESULT, result);
        }
        consentMgtInterceptors.forEach(rethrowConsumer(interceptor -> interceptor.intercept(new ConsentMessage
                (operation, operationProperties, context))));
        return this;
    }

    /**
     * Result of the main operation.
     *
     * @return Result.
     */
    public T getResult() {

        return result;
    }
}
