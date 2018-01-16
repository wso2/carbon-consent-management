/*
 *
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
 */

package org.wso2.carbon.consent.mgt.endpoint.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.endpoint.ApiResponseMessage;
import org.wso2.carbon.consent.mgt.endpoint.ConsentsApiService;
import org.wso2.carbon.consent.mgt.endpoint.dto.ConsentRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PIIcategoryRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeCategoryRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.util.ConsentEndpointUtils;

import java.util.List;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_ALREADY_EXIST;
import static org.wso2.carbon.consent.mgt.endpoint.util.ConsentEndpointUtils.getConsentManager;
import static org.wso2.carbon.consent.mgt.endpoint.util.ConsentEndpointUtils.getPurposeListResponse;
import static org.wso2.carbon.consent.mgt.endpoint.util.ConsentEndpointUtils.getPurposeRequest;
import static org.wso2.carbon.consent.mgt.endpoint.util.ConsentEndpointUtils.getPurposeResponseDTOList;

public class ConsentsApiServiceImpl extends ConsentsApiService {

    private static final Log LOG = LogFactory.getLog(ConsentsApiServiceImpl.class);

    @Override
    public Response consentsGet(Integer limit, Integer offset, String piiPrincipalId, String spTenantDomain, String service, String state, String collectionMethod, String piiCategoryId) {
        return null;
    }

    @Override
    public Response consentsPiiCategoriesGet(Integer limit, Integer offset) {
        return null;
    }

    @Override
    public Response consentsPiiCategoriesPost(PIIcategoryRequestDTO piiCategory) {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response consentsPiiCategoryPiiCategoryIdDelete(String piiCategoryId) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response consentsPiiCategoryPiiCategoryIdGet(String piiCategoryId) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response consentsPost(ConsentRequestDTO consent) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response consentsPurposeCategoriesGet(Integer limit, Integer offset) {
        return null;
    }

    @Override
    public Response consentsPurposeCategoriesPost(PurposeCategoryRequestDTO purposeCategory) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response consentsPurposeCategoriesPurposeCategoryIdDelete(String purposeCategoryId) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response consentsPurposeCategoriesPurposeCategoryIdGet(String purposeCategoryId) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response consentsPurposesGet(Integer limit, Integer offset) {
        try {
            List<PurposeListResponseDTO> purposeListResponseDTOS = getPurposeListResponseDTO(limit, offset);
            return Response.ok().entity(purposeListResponseDTOS).build();
        } catch (ConsentManagementClientException e) {
            return handleBadRequestResponse(e);
        } catch (ConsentManagementException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response consentsPurposesPost(PurposeRequestDTO purpose) {
        try {
            PurposeListResponseDTO response = addPurpose(purpose);
            return Response.ok().entity(response).build();
        } catch (ConsentManagementClientException e) {
            return handleBadRequestResponse(e);
        } catch (ConsentManagementException e) {
            return handleServerErrorResponse(e);
        }
        //TODO need to set the location header
    }

    @Override
    public Response consentsPurposesPurposeIdDelete(String purposeId) {
        try {
            getConsentManager().deletePurpose(Integer.parseInt(purposeId));
            return Response.ok().build();
        } catch (ConsentManagementClientException e) {
            return handleBadRequestResponse(e);
        } catch (ConsentManagementException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response consentsPurposesPurposeIdGet(String purposeId) {
        try {
            Purpose purpose = getConsentManager().getPurpose(Integer.parseInt(purposeId));
            PurposeListResponseDTO purposeListResponse = getPurposeListResponse(purpose);
            return Response.ok().entity(purposeListResponse).build();
        }
        catch (ConsentManagementClientException e) {
            return handleBadRequestResponse(e);
        }
        catch (ConsentManagementException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response consentsReceiptsReceiptIdDelete(String receiptId) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response consentsReceiptsReceiptIdGet(String receiptId) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    private Response handleBadRequestResponse(ConsentManagementClientException e) {

        if (ERROR_CODE_PURPOSE_ALREADY_EXIST.getCode().equals(e.getErrorCode())) {
            throw ConsentEndpointUtils.buildConflictRequestException(e.getMessage(), e.getErrorCode(), LOG, e);
        }
        throw ConsentEndpointUtils.buildBadRequestException(e.getMessage(), e.getErrorCode(), LOG, e);
    }

    private List<PurposeListResponseDTO> getPurposeListResponseDTO(Integer limit, Integer offset)
            throws ConsentManagementException {
        List<Purpose> purposes = getConsentManager().listPurposes(limit, offset);
        return getPurposeResponseDTOList(purposes);
    }

    private Response handleServerErrorResponse(ConsentManagementException e) {
        throw ConsentEndpointUtils.buildInternalServerErrorException(e.getErrorCode(), LOG, e);
    }

    private PurposeListResponseDTO addPurpose(PurposeRequestDTO purpose) throws ConsentManagementException {
        Purpose purposeRequest = getPurposeRequest(purpose);
        Purpose purposeResponse = getConsentManager().addPurpose(purposeRequest);
        return getPurposeListResponse(purposeResponse);
    }
}
