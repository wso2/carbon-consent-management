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

package org.wso2.carbon.consent.mgt.endpoint.v2.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.AddReceiptResponse;
import org.wso2.carbon.consent.mgt.core.model.ConsentPurpose;
import org.wso2.carbon.consent.mgt.core.model.PIICategoryValidity;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptService;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentListResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentPurposeBinding;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentReceiptDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentSummaryDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentedElementDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentedPurposeDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementTerminationInfo;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.KeyValuePair;
import org.wso2.carbon.consent.mgt.endpoint.v2.util.ConsentV2EndpointUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.API_VERSION;

/**
 * Service class for consent (receipt) operations in the V2 Consent Management API.
 */
public class ConsentReceiptsService {

    private static final Log LOG = LogFactory.getLog(ConsentReceiptsService.class);

    private static final String DEFAULT_COLLECTION_METHOD = "API_V2";
    private static final String DEFAULT_CONSENT_TYPE = "EXPLICIT";
    private static final String DEFAULT_PURPOSE_CATEGORY_NAME = "DEFAULT";
    private static final String DEFAULT_JURISDICTION = "";
    private static final String DEFAULT_POLICY_URL = "";
    private static final String TERMINATION_INDEFINITE = "INDEFINITE";
    private static final String TERMINATION_DAYS_PREFIX = "days:";

    private final ConsentManager consentManager;

    public ConsentReceiptsService(ConsentManager consentManager) {

        this.consentManager = consentManager;
    }

    /**
     * Creates a new consent receipt.
     *
     * @param request Consent create request.
     * @return Response with receiptId or error.
     */
    public Response createConsent(ConsentCreateRequest request) {

        try {
            int defaultPurposeCategoryId = getDefaultPurposeCategoryId();
            ReceiptInput receiptInput = buildReceiptInput(request, defaultPurposeCategoryId);
            AddReceiptResponse addReceiptResponse = consentManager.addConsent(receiptInput);
            URI location = URI.create("consents/" + addReceiptResponse.getConsentReceiptId());
            return Response.status(Response.Status.CREATED)
                    .header(javax.ws.rs.core.HttpHeaders.LOCATION, location)
                    .entity(addReceiptResponse).build();
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Throwable t) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(t, LOG);
        }
    }

    /**
     * Retrieves a consent receipt by ID.
     *
     * @param receiptId Receipt ID.
     * @return Response with ConsentReceiptDTO or error.
     */
    public Response getConsent(String receiptId) {

        try {
            Receipt receipt = consentManager.getReceipt(receiptId);
            // Return 404 if the consent is revoked
            if ("REVOKED".equals(receipt.getState())) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(toConsentReceiptDTO(receipt)).build();
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Throwable t) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(t, LOG);
        }
    }

    /**
     * Lists consent receipts with optional filtering and pagination.
     *
     * @param subjectUserId PII principal ID filter.
     * @param service          Service name filter.
     * @param state            State filter.
     * @param purposeId        Purpose ID filter (0 or negative means no filter).
     * @param limit            Maximum results.
     * @param offset           Pagination offset.
     * @return Response with list of ConsentSummaryDTOs or error.
     */
    public Response listConsents(String subjectUserId, String service, String state,
                                 int purposeId, int limit, int offset) {

        try {
            List<ReceiptListResponse> receipts;
            if (purposeId > 0) {
                receipts = consentManager.searchReceipts(limit, offset, subjectUserId, null, service, state,
                        purposeId);
            } else {
                receipts = consentManager.searchReceipts(limit, offset, subjectUserId, null, service, state);
            }

            List<ConsentSummaryDTO> summaries = new ArrayList<>();
            if (receipts != null) {
                for (ReceiptListResponse r : receipts) {
                    summaries.add(toConsentSummaryDTO(r));
                }
            }

            ConsentListResponse listResponse = new ConsentListResponse();
            listResponse.setStartIndex(offset);
            listResponse.setCount(summaries.size());
            listResponse.setItems(summaries);
            return Response.ok(listResponse).build();
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Throwable t) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(t, LOG);
        }
    }

    /**
     * Revokes a consent receipt.
     *
     * @param receiptId Receipt ID.
     * @return Response with no content or error.
     */
    public Response revokeConsent(String receiptId) {

        try {
            Receipt receipt = consentManager.getReceipt(receiptId);
            // If already revoked, treat as idempotent success.
            if ("REVOKED".equals(receipt.getState())) {
                return Response.noContent().build();
            }
            consentManager.revokeReceipt(receiptId);
            return Response.noContent().build();
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Throwable t) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(t, LOG);
        }
    }

    private int getDefaultPurposeCategoryId() throws ConsentManagementException {

        PurposeCategory defaultCategory = consentManager.getPurposeCategoryByName(DEFAULT_PURPOSE_CATEGORY_NAME);
        return defaultCategory.getId();
    }

    private ReceiptInput buildReceiptInput(ConsentCreateRequest request, int defaultPurposeCategoryId) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();

        ReceiptInput receiptInput = new ReceiptInput();
        receiptInput.setVersion(API_VERSION);
        receiptInput.setJurisdiction(DEFAULT_JURISDICTION);
        receiptInput.setPolicyUrl(DEFAULT_POLICY_URL);
        receiptInput.setCollectionMethod(DEFAULT_COLLECTION_METHOD);
        receiptInput.setLanguage(request.getLanguage());
        receiptInput.setPiiPrincipalId(carbonContext.getUsername());
        receiptInput.setTenantDomain(carbonContext.getTenantDomain());

        // Build metadata properties map.
        if (request.getMetadata() != null) {
            Map<String, String> properties = new HashMap<>();
            for (KeyValuePair kv : request.getMetadata()) {
                properties.put(kv.getKey(), kv.getValue());
            }
            receiptInput.setProperties(properties);
        }

        // Build single service input from the request service name.
        ReceiptServiceInput serviceInput = new ReceiptServiceInput();
        serviceInput.setService(request.getService());
        serviceInput.setSpDisplayName(request.getService());
        serviceInput.setTenantDomain(carbonContext.getTenantDomain());

        // Build purpose inputs.
        List<ReceiptPurposeInput> purposeInputs = new ArrayList<>();
        if (request.getPurposes() != null) {
            for (ConsentPurposeBinding purposeBinding : request.getPurposes()) {
                ReceiptPurposeInput purposeInput = new ReceiptPurposeInput();
                int purposeId = purposeBinding.getPurposeId();
                purposeInput.setPurposeId(purposeId);

                // Resolve and set the latest version ID for this purpose
                try {
                    Purpose purpose = consentManager.getPurpose(purposeId);
                    PurposeVersion latestVersion = purpose.getLatestVersion();
                    if (latestVersion != null) {
                        purposeInput.setPurposeVersionId(latestVersion.getId());
                    }
                } catch (ConsentManagementException e) {
                    // If we can't resolve the purpose, let the backend handle the error
                    LOG.debug("Unable to resolve latest version for purpose ID: " + purposeId, e);
                }

                purposeInput.setConsentType(DEFAULT_CONSENT_TYPE);
                purposeInput.setPrimaryPurpose(true);
                purposeInput.setThirdPartyDisclosure(false);
                purposeInput.setPurposeCategoryId(Collections.singletonList(defaultPurposeCategoryId));
                purposeInput.setTermination(encodePeriod(purposeBinding.getTerminationPeriod()));

                // Build element validities.
                List<PIICategoryValidity> piiCategoryValidities = new ArrayList<>();
                if (purposeBinding.getElements() != null) {
                    for (ElementTerminationInfo elementInfo : purposeBinding.getElements()) {
                        PIICategoryValidity validity = new PIICategoryValidity(
                                elementInfo.getElementId(), encodePeriod(elementInfo.getValidityPeriod()));
                        piiCategoryValidities.add(validity);
                    }
                }
                purposeInput.setPiiCategory(piiCategoryValidities);
                purposeInputs.add(purposeInput);
            }
        }
        serviceInput.setPurposes(purposeInputs);
        receiptInput.setServices(Collections.singletonList(serviceInput));
        return receiptInput;
    }

    private String encodePeriod(Integer period) {

        if (period == null || period == -1) {
            return TERMINATION_INDEFINITE;
        }
        return TERMINATION_DAYS_PREFIX + period;
    }

    private int decodePeriod(String period) {

        if (period == null || TERMINATION_INDEFINITE.equalsIgnoreCase(period)) {
            return -1;
        }
        if (period.startsWith(TERMINATION_DAYS_PREFIX)) {
            try {
                return Integer.parseInt(period.substring(TERMINATION_DAYS_PREFIX.length()));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private ConsentReceiptDTO toConsentReceiptDTO(Receipt receipt) {

        ConsentReceiptDTO dto = new ConsentReceiptDTO();
        dto.setReceiptId(receipt.getConsentReceiptId());
        dto.setTimestamp(receipt.getConsentTimestamp());
        dto.setLanguage(receipt.getLanguage());
        dto.setSubjectUserId(receipt.getPiiPrincipalId());
        dto.setState(ConsentReceiptDTO.StateEnum.fromValue(receipt.getState()));

        // Extract service name and purposes from the first service entry (V2 is single-service).
        List<ConsentedPurposeDTO> purposeDTOs = new ArrayList<>();
        List<ReceiptService> services = receipt.getServices();
        if (services != null && !services.isEmpty()) {
            ReceiptService receiptService = services.get(0);
            dto.setService(receiptService.getService());

            if (receiptService.getPurposes() != null) {
                for (ConsentPurpose consentPurpose : receiptService.getPurposes()) {
                    purposeDTOs.add(toConsentedPurposeDTO(consentPurpose));
                }
            }
        }
        dto.setPurposes(purposeDTOs);

        List<KeyValuePair> metadata = new ArrayList<>();
        if (receipt.getProperties() != null) {
            for (Map.Entry<String, String> e : receipt.getProperties().entrySet()) {
                metadata.add(new KeyValuePair().key(e.getKey()).value(e.getValue()));
            }
        }
        dto.setMetadata(metadata);
        return dto;
    }

    private ConsentedPurposeDTO toConsentedPurposeDTO(ConsentPurpose consentPurpose) {

        ConsentedPurposeDTO dto = new ConsentedPurposeDTO();
        dto.setName(consentPurpose.getPurpose());
        dto.setPurposeId(consentPurpose.getPurposeId());
        dto.setPurposeVersionId(consentPurpose.getPurposeVersionId());
        dto.setTerminationPeriod(decodePeriod(consentPurpose.getTermination()));

        List<ConsentedElementDTO> elementDTOs = new ArrayList<>();
        if (consentPurpose.getPiiCategory() != null) {
            for (PIICategoryValidity piiCategoryValidity : consentPurpose.getPiiCategory()) {
                ConsentedElementDTO elementDTO = new ConsentedElementDTO();
                elementDTO.setElementId(piiCategoryValidity.getId());
                elementDTO.setName(piiCategoryValidity.getName());
                elementDTO.setDisplayName(piiCategoryValidity.getDisplayName());
                elementDTO.setValidityPeriod(decodePeriod(piiCategoryValidity.getValidity()));
                elementDTOs.add(elementDTO);
            }
        }
        dto.setElements(elementDTOs);
        return dto;
    }

    private ConsentSummaryDTO toConsentSummaryDTO(ReceiptListResponse receipt) {

        ConsentSummaryDTO dto = new ConsentSummaryDTO();
        dto.setReceiptId(receipt.getConsentReceiptId());
        dto.setLanguage(receipt.getLanguage());
        dto.setSubjectUserId(receipt.getPiiPrincipalId());
        dto.setState(ConsentSummaryDTO.StateEnum.fromValue(receipt.getState()));
        dto.setService(receipt.getSpDisplayName());
        return dto;
    }
}
