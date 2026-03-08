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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.endpoint.v2.ElementsApiService;
import org.wso2.carbon.consent.mgt.endpoint.v2.core.ConsentElementsService;
import org.wso2.carbon.consent.mgt.endpoint.v2.factories.ConsentElementsServiceFactory;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.util.ConsentV2EndpointUtils;

import java.net.URI;
import javax.ws.rs.core.Response;

public class ElementsApiServiceImpl implements ElementsApiService {

    private static final Log LOG = LogFactory.getLog(ElementsApiServiceImpl.class);
    private final ConsentElementsService elementsService;

    public ElementsApiServiceImpl() {

        this.elementsService = ConsentElementsServiceFactory.getConsentElementsService();
    }

    @Override
    public Response elementsCreate(ElementCreateRequest elementCreateRequest) {

        try {
            ElementDTO dto = elementsService.createElement(elementCreateRequest);
            URI location = URI.create("elements/" + dto.getId());
            return Response.created(location).entity(dto).build();
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Exception e) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(e, LOG);
        }
    }

    @Override
    public Response elementsDelete(Integer elementId) {

        try {
            return elementsService.deleteElement(elementId);
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Exception e) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(e, LOG);
        }
    }

    @Override
    public Response elementsGet(Integer elementId) {

        try {
            return elementsService.getElement(elementId);
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Exception e) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(e, LOG);
        }
    }

    @Override
    public Response elementsList(Integer limit, Integer offset) {

        try {
            int resolvedLimit = (limit != null) ? limit : ConsentConstants.DEFAULT_LIMIT;
            int resolvedOffset = (offset != null) ? offset : ConsentConstants.DEFAULT_OFFSET;
            return elementsService.listElements(resolvedLimit, resolvedOffset);
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Exception e) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(e, LOG);
        }
    }
}
