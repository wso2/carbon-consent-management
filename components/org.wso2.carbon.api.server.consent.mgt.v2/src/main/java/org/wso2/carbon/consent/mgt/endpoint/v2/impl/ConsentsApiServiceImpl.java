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
import org.wso2.carbon.consent.mgt.endpoint.v2.ConsentsApiService;
import org.wso2.carbon.consent.mgt.endpoint.v2.core.ConsentReceiptsService;
import org.wso2.carbon.consent.mgt.endpoint.v2.factories.ConsentReceiptsServiceFactory;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.util.ConsentV2EndpointUtils;

import java.net.URI;
import javax.ws.rs.core.Response;

public class ConsentsApiServiceImpl implements ConsentsApiService {

    private static final Log LOG = LogFactory.getLog(ConsentsApiServiceImpl.class);
    private final ConsentReceiptsService receiptsService;

    public ConsentsApiServiceImpl() {

        this.receiptsService = ConsentReceiptsServiceFactory.getConsentReceiptsService();
    }

    @Override
    public Response consentsCreate(ConsentCreateRequest consentCreateRequest) {

        try {
            ConsentResponseDTO responseDTO = receiptsService.createConsent(consentCreateRequest);
            URI location = URI.create("consents/" + responseDTO.getReceiptId());
            return Response.created(location).entity(responseDTO).build();
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Exception e) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(e, LOG);
        }
    }

    @Override
    public Response consentsGet(String receiptId) {

        try {
            return receiptsService.getConsent(receiptId);
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Exception e) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(e, LOG);
        }
    }

    @Override
    public Response consentsList(String subjectUserId, String service, String state, Integer purposeId,
                                 Integer limit, Integer offset) {

        try {
            int resolvedPurposeId = (purposeId != null) ? purposeId : 0;
            int resolvedLimit = (limit != null) ? limit : ConsentConstants.DEFAULT_LIMIT;
            int resolvedOffset = (offset != null) ? offset : ConsentConstants.DEFAULT_OFFSET;
            return receiptsService.listConsents(subjectUserId, service, state, resolvedPurposeId, resolvedLimit,
                    resolvedOffset);
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Exception e) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(e, LOG);
        }
    }

    @Override
    public Response consentsRevoke(String receiptId) {

        try {
            return receiptsService.revokeConsent(receiptId);
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Exception e) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(e, LOG);
        }
    }
}
