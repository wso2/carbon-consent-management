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
import org.wso2.carbon.consent.mgt.endpoint.v2.PurposesApiService;
import org.wso2.carbon.consent.mgt.endpoint.v2.core.ConsentPurposesService;
import org.wso2.carbon.consent.mgt.endpoint.v2.factories.ConsentPurposesServiceFactory;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.util.ConsentV2EndpointUtils;

import java.net.URI;
import javax.ws.rs.core.Response;

public class PurposesApiServiceImpl implements PurposesApiService {

    private static final Log LOG = LogFactory.getLog(PurposesApiServiceImpl.class);
    private final ConsentPurposesService purposesService;

    public PurposesApiServiceImpl() {

        this.purposesService = ConsentPurposesServiceFactory.getConsentPurposesService();
    }

    @Override
    public Response purposesCreate(PurposeCreateRequest purposeCreateRequest) {

        try {
            PurposeDTO dto = purposesService.createPurpose(purposeCreateRequest);
            URI location = URI.create("purposes/" + dto.getId());
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
    public Response purposesDelete(Integer purposeId) {

        try {
            return purposesService.deletePurpose(purposeId);
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Exception e) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(e, LOG);
        }
    }

    @Override
    public Response purposesGet(Integer purposeId) {

        try {
            return purposesService.getPurpose(purposeId);
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Exception e) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(e, LOG);
        }
    }

    @Override
    public Response purposesList(String group, String groupType, Integer limit, Integer offset) {

        try {
            int resolvedLimit = (limit != null) ? limit : ConsentConstants.DEFAULT_LIMIT;
            int resolvedOffset = (offset != null) ? offset : ConsentConstants.DEFAULT_OFFSET;
            return purposesService.listPurposes(group, groupType, resolvedLimit, resolvedOffset);
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Exception e) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(e, LOG);
        }
    }

    @Override
    public Response purposesVersionsCreate(Integer purposeId, PurposeVersionCreateRequest purposeVersionCreateRequest) {

        try {
            PurposeVersionDTO dto = purposesService.createPurposeVersion(purposeId, purposeVersionCreateRequest);
            URI location = URI.create("purposes/" + purposeId + "/versions/" + dto.getId());
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
    public Response purposesVersionsDelete(Integer purposeId, Integer versionId) {

        try {
            return purposesService.deletePurposeVersion(purposeId, versionId);
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Exception e) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(e, LOG);
        }
    }

    @Override
    public Response purposesVersionsGet(Integer purposeId, Integer versionId) {

        try {
            return purposesService.getPurposeVersion(purposeId, versionId);
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Exception e) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(e, LOG);
        }
    }

    @Override
    public Response purposesVersionsList(Integer purposeId, Integer limit, Integer offset) {

        try {
            int resolvedLimit = (limit != null) ? limit : ConsentConstants.DEFAULT_LIMIT;
            int resolvedOffset = (offset != null) ? offset : ConsentConstants.DEFAULT_OFFSET;
            return purposesService.listPurposeVersions(purposeId, resolvedLimit, resolvedOffset);
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Exception e) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(e, LOG);
        }
    }
}
