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

package org.wso2.carbon.consent.mgt.endpoint.v2.impl;

import org.wso2.carbon.consent.mgt.endpoint.v2.ElementsApiService;
import org.wso2.carbon.consent.mgt.endpoint.v2.core.ConsentElementsService;
import org.wso2.carbon.consent.mgt.endpoint.v2.factories.ConsentElementsServiceFactory;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementCreateRequest;

import javax.ws.rs.core.Response;

public class ElementsApiServiceImpl implements ElementsApiService {

    private final ConsentElementsService elementsService;

    public ElementsApiServiceImpl() {

        this.elementsService = ConsentElementsServiceFactory.getConsentElementsService();
    }

    @Override
    public Response elementsCreate(ElementCreateRequest elementCreateRequest) {

        return elementsService.createElement(elementCreateRequest);
    }

    @Override
    public Response elementsDelete(Integer elementId) {

        return elementsService.deleteElement(elementId);
    }

    @Override
    public Response elementsGet(Integer elementId) {

        return elementsService.getElement(elementId);
    }

    @Override
    public Response elementsList(Integer limit, Integer offset) {

        int resolvedLimit = (limit != null) ? limit : 50;
        int resolvedOffset = (offset != null) ? offset : 0;
        return elementsService.listElements(resolvedLimit, resolvedOffset);
    }
}
