/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.consent.mgt.endpoint.v2.factories;

import org.wso2.carbon.consent.mgt.endpoint.v2.core.ConsentElementsService;
import org.wso2.carbon.consent.mgt.endpoint.v2.util.ConsentV2EndpointUtils;

/**
 * Factory class for creating {@link ConsentElementsService} instances.
 */
public class ConsentElementsServiceFactory {

    private ConsentElementsServiceFactory() {

    }

    /**
     * Returns a new ConsentElementsService backed by the OSGi ConsentManager.
     *
     * @return ConsentElementsService instance.
     */
    public static ConsentElementsService getConsentElementsService() {

        return new ConsentElementsService(ConsentV2EndpointUtils.getConsentManager());
    }
}
