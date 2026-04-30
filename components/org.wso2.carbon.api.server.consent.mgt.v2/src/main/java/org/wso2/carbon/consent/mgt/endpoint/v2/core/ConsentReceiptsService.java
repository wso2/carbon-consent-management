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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;

import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.model.AddReceiptResponse;
import org.wso2.carbon.consent.mgt.core.model.ConsentAuthorization;
import org.wso2.carbon.consent.mgt.core.model.ConsentPurpose;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentResponseDTO;
import org.wso2.carbon.consent.mgt.core.model.PIICategoryValidity;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptService;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.AuthorizationCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.AuthorizationDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentListResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentPurposeBinding;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentSummaryDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentedPurposeDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentValidateResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentedElementDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementTerminationInfo;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ACTIVE_STATE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.REJECTED_STATE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.REVOKE_STATE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PENDING_STATE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.API_VERSION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.DEFAULT_PURPOSE_GROUP;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.DEFAULT_COLLECTION_METHOD;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.TERMINATION_INDEFINITE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_ELEMENT_UUID_NOT_FOUND;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_CONSENT_INVALID_STATE_FOR_AUTHORIZE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_CONSENT_USER_NOT_IN_AUTHORIZATION_LIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_CONSENT_SUBJECT_MISMATCH;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CATEGORY_NOT_FOUND;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_UUID_NOT_FOUND;
import static org.wso2.carbon.consent.mgt.core.util.ConsentUtils.handleClientException;

/**
 * Service class for consent (receipt) operations in the V2 Consent Management API.
 */
public class ConsentReceiptsService {

    private static final Log LOG = LogFactory.getLog(ConsentReceiptsService.class);

    private static final String DEFAULT_CONSENT_TYPE = "EXPLICIT";
    private static final String DEFAULT_JURISDICTION = "";
    private static final String DEFAULT_POLICY_URL = "";

    private final ConsentManager consentManager;

    public ConsentReceiptsService(ConsentManager consentManager) {

        this.consentManager = consentManager;
    }

    /**
     * Creates a new consent receipt.
     *
     * @param request Consent create request.
     * @return ConsentResponseDTO with receipt details.
     * @throws ConsentManagementException if creation fails.
     */
    public ConsentResponseDTO createConsent(ConsentCreateRequest request) throws ConsentManagementException {

        int defaultPurposeCategoryId = getDefaultPurposeCategoryId();
        ReceiptInput receiptInput = buildReceiptInput(request, defaultPurposeCategoryId);
        AddReceiptResponse addReceiptResponse = consentManager.addConsent(receiptInput);

        ConsentResponseDTO responseDTO = new ConsentResponseDTO();
        responseDTO.setConsentId(addReceiptResponse.getConsentReceiptId());
        responseDTO.setLanguage(addReceiptResponse.getLanguage());
        responseDTO.setSubjectId(addReceiptResponse.getPiiPrincipalId());
        responseDTO.setTenantDomain(addReceiptResponse.getTenantDomain());
        boolean hasPendingAuth = request.getAuthorizations() != null && !request.getAuthorizations().isEmpty();
        if (hasPendingAuth) {
            responseDTO.setState(ConsentResponseDTO.StateEnum.PENDING);
        } else if (ConsentCreateRequest.StateEnum.REJECTED.equals(request.getState())) {
            responseDTO.setState(ConsentResponseDTO.StateEnum.REJECTED);
        } else {
            responseDTO.setState(ConsentResponseDTO.StateEnum.ACTIVE);
        }
        return responseDTO;
    }

    /**
     * Retrieves a consent receipt by ID.
     *
     * @param receiptId Receipt ID.
     * @return ConsentDTO with receipt details.
     * @throws ConsentManagementException if retrieval fails.
     */
    public Response getConsent(String receiptId) throws ConsentManagementException {

        Receipt receipt = consentManager.getReceipt(receiptId);
        // Lazy expiry on GET — validate status and re-fetch if state changed
        String latestState = consentManager.validateConsentStatus(receiptId);
        if (!latestState.equals(receipt.getState())) {
            receipt = consentManager.getReceipt(receiptId);  // re-fetch only if state changed
        }
        return Response.ok(toConsentDTO(receipt)).build();
    }

    /**
     * Lists consent receipts with explicit filter params and pagination.
     *
     * @param subjectId        Filter by subject user ID.
     * @param serviceId        Filter by service ID.
     * @param state            Filter by consent state.
     * @param purposeId        Filter by purpose UUID.
     * @param purposeVersionId Filter by specific purpose version UUID.
     * @param limit            Maximum results.
     * @param offset           Pagination offset.
     * @return Response with list of ConsentSummaryDTOs.
     * @throws ConsentManagementException if listing fails.
     */
    public Response listConsents(String subjectId, String serviceId, String state, UUID purposeId,
                                 UUID purposeVersionId, int limit, int offset)
            throws ConsentManagementException {

        List<Receipt> receipts = consentManager.listReceipts(
                subjectId,
                serviceId,
                state,
                purposeId != null ? purposeId.toString() : null,
                purposeVersionId != null ? purposeVersionId.toString() : null,
                limit, offset);

        if (receipts == null) {
            receipts = Collections.emptyList();
        }

        List<ConsentSummaryDTO> summaries = new ArrayList<>();
        for (Receipt receipt : receipts) {
            summaries.add(toConsentSummaryDTO(receipt));
        }

        ConsentListResponse listResponse = new ConsentListResponse();
        listResponse.setStartIndex(offset);
        listResponse.setCount(summaries.size());
        listResponse.setItems(summaries);
        return Response.ok(listResponse).build();
    }

    /**
     * Revokes a consent receipt.
     *
     * @param receiptId Receipt ID.
     * @return Response with no content.
     * @throws ConsentManagementException if revocation fails.
     */
    public Response revokeConsent(String receiptId) throws ConsentManagementException {

        Receipt receipt = consentManager.getReceipt(receiptId);
        String currentState = StringUtils.isNotBlank(receipt.getState()) ? receipt.getState() : ACTIVE_STATE;
        // Idempotent: already revoked is treated as success
        if (REVOKE_STATE.equals(currentState)) {
            return Response.noContent().build();
        }
        String callingUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        // For consents with authorizations, only users in the list may revoke
        List<ConsentAuthorization> auths = consentManager.getConsentAuthorizations(receiptId);
        if (auths != null && !auths.isEmpty()) {
            boolean callingUserInList = auths.stream().anyMatch(a -> callingUser.equals(a.getUserId()));
            if (!callingUserInList) {
                throw handleClientException(ERROR_CODE_CONSENT_USER_NOT_IN_AUTHORIZATION_LIST, callingUser);
            }
        }
        consentManager.authorizeConsent(receiptId, callingUser, REVOKE_STATE);
        return Response.noContent().build();
    }

    private int getDefaultPurposeCategoryId() throws ConsentManagementException {

        PurposeCategory defaultCategory = consentManager.getPurposeCategoryByName(DEFAULT_PURPOSE_GROUP);
        if (defaultCategory == null) {
            throw new ConsentManagementServerException(
                    String.format(ERROR_CODE_PURPOSE_CATEGORY_NOT_FOUND.getMessage(), DEFAULT_PURPOSE_GROUP),
                    ERROR_CODE_PURPOSE_CATEGORY_NOT_FOUND.getCode());
        }
        return defaultCategory.getId();
    }

    private ReceiptInput buildReceiptInput(ConsentCreateRequest request, int defaultPurposeCategoryId)
            throws ConsentManagementException {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();

        ReceiptInput receiptInput = new ReceiptInput();
        receiptInput.setVersion(API_VERSION);
        receiptInput.setJurisdiction(DEFAULT_JURISDICTION);
        receiptInput.setPolicyUrl(DEFAULT_POLICY_URL);
        receiptInput.setCollectionMethod(DEFAULT_COLLECTION_METHOD);
        receiptInput.setAllowMultipleActiveReceipts(true);
        receiptInput.setLanguage(request.getLanguage());
        boolean hasAuthorizations = request.getAuthorizations() != null && !request.getAuthorizations().isEmpty();
        String currentUser = carbonContext.getUsername();
        String subjectId = StringUtils.isNotBlank(request.getSubjectId())
                ? request.getSubjectId() : currentUser;
        // Delegated consent (subject ≠ caller) requires at least one authorizer.
        if (!subjectId.equals(currentUser) && !hasAuthorizations) {
            throw handleClientException(ERROR_CODE_CONSENT_SUBJECT_MISMATCH, subjectId);
        }
        receiptInput.setPiiPrincipalId(subjectId);
        receiptInput.setTenantDomain(carbonContext.getTenantDomain());
        if (request.getValidityTime() != null) {
            receiptInput.setValidityTime(request.getValidityTime());
        }
        if (hasAuthorizations) {
            receiptInput.setAuthorizations(request.getAuthorizations());
        }
        if (ConsentCreateRequest.StateEnum.REJECTED.equals(request.getState())) {
            receiptInput.setState(REJECTED_STATE);
        }

        // Set metadata properties.
        if (request.getProperties() != null) {
            receiptInput.setProperties(request.getProperties());
        }

        // Build single service input from the request service name.
        ReceiptServiceInput serviceInput = new ReceiptServiceInput();
        serviceInput.setService(request.getServiceId());
        serviceInput.setSpDisplayName(request.getServiceId());
        serviceInput.setTenantDomain(carbonContext.getTenantDomain());

        // Build purpose inputs.
        List<ReceiptPurposeInput> purposeInputs = new ArrayList<>();
        if (request.getPurposes() != null) {
            for (ConsentPurposeBinding purposeBinding : request.getPurposes()) {
                ReceiptPurposeInput purposeInput = new ReceiptPurposeInput();

                // Resolve purpose by UUID to get the internal DB ID.
                Purpose purpose = consentManager.getPurposeByUuid(purposeBinding.getPurposeId().toString());
                if (purpose == null) {
                    throw handleClientException(ERROR_CODE_PURPOSE_UUID_NOT_FOUND,
                            purposeBinding.getPurposeId().toString());
                }
                purposeInput.setPurposeId(purpose.getId());

                // Resolve and set the latest version ID for this purpose.
                PurposeVersion latestVersion = purpose.getLatestVersion();
                if (latestVersion != null) {
                    purposeInput.setPurposeVersionId(latestVersion.getUuid());
                }

                purposeInput.setConsentType(DEFAULT_CONSENT_TYPE);
                purposeInput.setPrimaryPurpose(true);
                purposeInput.setThirdPartyDisclosure(false);
                purposeInput.setPurposeCategoryId(List.of(defaultPurposeCategoryId));
                purposeInput.setTermination(TERMINATION_INDEFINITE);

                // Build element validities, resolving each element UUID to its internal DB ID.
                List<PIICategoryValidity> piiCategoryValidities = new ArrayList<>();
                if (purposeBinding.getElements() != null) {
                    for (ElementTerminationInfo elementInfo : purposeBinding.getElements()) {
                        PIICategory piiCategory = consentManager.getPIICategoryByUuid(
                                elementInfo.getElementId().toString());
                        if (piiCategory == null) {
                            throw handleClientException(ERROR_CODE_ELEMENT_UUID_NOT_FOUND,
                                    elementInfo.getElementId().toString());
                        }
                        PIICategoryValidity validity = new PIICategoryValidity(
                                piiCategory.getId(), TERMINATION_INDEFINITE);
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

    private ConsentDTO toConsentDTO(Receipt receipt) throws ConsentManagementException {

        ConsentDTO dto = new ConsentDTO();
        dto.setConsentId(receipt.getConsentReceiptId());
        dto.setTimestamp(receipt.getConsentTimestamp());
        dto.setLanguage(receipt.getLanguage());
        dto.setSubjectId(receipt.getPiiPrincipalId());
        String state = StringUtils.isNotBlank(receipt.getState()) ? receipt.getState() : ACTIVE_STATE;
        dto.setState(ConsentDTO.StateEnum.fromValue(state));
        dto.setValidityTime(receipt.getValidityTime());

        // Extract service name and purposes from the first service entry (V2 is single-service).
        List<ConsentedPurposeDTO> purposeDTOs = new ArrayList<>();
        List<ReceiptService> services = receipt.getServices();
        if (services != null && !services.isEmpty()) {
            ReceiptService receiptService = services.get(0);
            dto.setServiceId(receiptService.getService());

            if (receiptService.getPurposes() != null) {
                for (ConsentPurpose consentPurpose : receiptService.getPurposes()) {
                    purposeDTOs.add(toConsentedPurposeDTO(consentPurpose));
                }
            }
        }
        dto.setPurposes(purposeDTOs);

        dto.setProperties(receipt.getProperties());

        // Populate authorizations from backend
        List<ConsentAuthorization> auths = consentManager.getConsentAuthorizations(receipt.getConsentReceiptId());
        List<AuthorizationDTO> authDTOs = new ArrayList<>();
        if (auths != null) {
            for (ConsentAuthorization auth : auths) {
                // Skip PENDING authorizations — they are not exposed in the API response DTO
                if ("PENDING".equals(auth.getStatus())) {
                    continue;
                }
                try {
                    AuthorizationDTO.StateEnum stateEnum = AuthorizationDTO.StateEnum.fromValue(auth.getStatus());
                    AuthorizationDTO authDTO = new AuthorizationDTO();
                    authDTO.setUserId(auth.getUserId());
                    authDTO.setState(stateEnum);
                    authDTO.setUpdatedTime(auth.getUpdatedTime());
                    authDTOs.add(authDTO);
                } catch (IllegalArgumentException e) {
                    // Skip unrecognized authorization states
                    LOG.warn("Skipping unrecognized authorization state: " + auth.getStatus(), e);
                }
            }
        }
        dto.setAuthorizations(authDTOs);
        return dto;
    }

    private ConsentedPurposeDTO toConsentedPurposeDTO(ConsentPurpose consentPurpose)
            throws ConsentManagementException {

        ConsentedPurposeDTO dto = new ConsentedPurposeDTO();
        dto.setName(consentPurpose.getPurpose());

        // Resolve purpose int ID to UUID, and populate version label using the fetched purpose.
        String versionUuid = consentPurpose.getPurposeVersionId();
        try {
            Purpose purpose = consentManager.getPurpose(consentPurpose.getPurposeId());
            if (purpose == null) {
                LOG.warn("Could not resolve purpose UUID for purposeId: " + consentPurpose.getPurposeId());
            } else {
                if (StringUtils.isNotBlank(purpose.getUuid())) {
                    dto.setPurposeId(UUID.fromString(purpose.getUuid()));
                }
                if (StringUtils.isNotBlank(versionUuid)) {
                    dto.setPurposeVersionId(UUID.fromString(versionUuid));
                    PurposeVersion latestVersion = purpose.getLatestVersion();
                    if (latestVersion != null && versionUuid.equals(latestVersion.getUuid())) {
                        dto.setVersion(latestVersion.getVersion());
                    } else if (dto.getPurposeId() != null) {
                        PurposeVersion pv = consentManager.getPurposeVersion(dto.getPurposeId().toString(), versionUuid);
                        if (pv != null) {
                            dto.setVersion(pv.getVersion());
                        }
                    }
                }
            }
        } catch (ConsentManagementException e) {
            LOG.warn("Could not resolve purpose UUID for purposeId: " + consentPurpose.getPurposeId(), e);
        }

        List<ConsentedElementDTO> elementDTOs = new ArrayList<>();
        if (consentPurpose.getPiiCategory() != null) {
            for (PIICategoryValidity piiCategoryValidity : consentPurpose.getPiiCategory()) {
                ConsentedElementDTO elementDTO = new ConsentedElementDTO();
                elementDTO.setName(piiCategoryValidity.getName());
                elementDTO.setDisplayName(piiCategoryValidity.getDisplayName());
                // Resolve element int ID to UUID.
                try {
                    PIICategory element = consentManager.getPIICategory(piiCategoryValidity.getId());
                    if (element == null) {
                        LOG.warn("Could not resolve element UUID for elementId: " + piiCategoryValidity.getId());
                    } else {
                        if (StringUtils.isNotBlank(element.getUuid())) {
                            elementDTO.setElementId(UUID.fromString(element.getUuid()));
                        }
                    }
                } catch (ConsentManagementException e) {
                    LOG.warn("Could not resolve element UUID for elementId: " + piiCategoryValidity.getId(), e);
                }
                elementDTOs.add(elementDTO);
            }
        }
        dto.setElements(elementDTOs);
        return dto;
    }

    /**
     * Authorizes (approves/rejects) a consent receipt.
     *
     * @param consentId Consent receipt ID.
     * @param request   Authorization create request.
     * @return Response with AuthorizationDTO.
     * @throws ConsentManagementException if authorization fails.
     */
    public Response authorizeConsent(String consentId, AuthorizationCreateRequest request)
            throws ConsentManagementException {

        Receipt receipt = consentManager.getReceipt(consentId);
        String currentState = StringUtils.isNotBlank(receipt.getState()) ? receipt.getState() : ACTIVE_STATE;
        if (!PENDING_STATE.equals(currentState)) {
            throw handleClientException(ERROR_CODE_CONSENT_INVALID_STATE_FOR_AUTHORIZE, consentId);
        }

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String callingUser = carbonContext.getUsername();
        String authStatus = request.getState() != null ? request.getState().toString() : "APPROVED";

        // Only users in the authorization list may authorize
        List<ConsentAuthorization> existing = consentManager.getConsentAuthorizations(consentId);
        boolean callingUserInList = existing.stream().anyMatch(a -> callingUser.equals(a.getUserId()));
        if (!callingUserInList) {
            throw handleClientException(ERROR_CODE_CONSENT_USER_NOT_IN_AUTHORIZATION_LIST, callingUser);
        }

        consentManager.authorizeConsent(consentId, callingUser, authStatus);

        List<ConsentAuthorization> all = consentManager.getConsentAuthorizations(consentId);
        ConsentAuthorization updated = all.stream()
                .filter(a -> callingUser.equals(a.getUserId()))
                .findFirst()
                .orElse(null);

        AuthorizationDTO dto = new AuthorizationDTO();
        dto.setUserId(callingUser);
        if (updated != null) {
            dto.setState(AuthorizationDTO.StateEnum.fromValue(updated.getStatus()));
            dto.setUpdatedTime(updated.getUpdatedTime());
        }

        return Response.status(Response.Status.OK).entity(dto).build();
    }

    /**
     * Validates the current status of a consent receipt.
     *
     * @param consentId Consent receipt ID.
     * @return Response with ConsentValidateResponse.
     * @throws ConsentManagementException if validation fails.
     */
    public Response validateConsent(String consentId) throws ConsentManagementException {

        String status = consentManager.validateConsentStatus(consentId);

        ConsentValidateResponse validateResponse = new ConsentValidateResponse();
        validateResponse.setState(ConsentValidateResponse.StateEnum.fromValue(status));

        Receipt receipt = consentManager.getReceipt(consentId);
        if (receipt != null) {
            validateResponse.setValidityTime(receipt.getValidityTime());
        }
        return Response.ok(validateResponse).build();
    }

    private ConsentSummaryDTO toConsentSummaryDTO(Receipt receipt) {

        ConsentSummaryDTO dto = new ConsentSummaryDTO();
        dto.setConsentId(receipt.getConsentReceiptId());
        dto.setSubjectId(receipt.getPiiPrincipalId());
        String state = StringUtils.isNotBlank(receipt.getState()) ? receipt.getState() : ACTIVE_STATE;
        dto.setState(ConsentSummaryDTO.StateEnum.fromValue(state));
        dto.setTimestamp(receipt.getConsentTimestamp());
        dto.setValidityTime(receipt.getValidityTime());
        if (receipt.getServices() != null && !receipt.getServices().isEmpty()) {
            dto.setServiceId(receipt.getServices().get(0).getService());
        }
        return dto;
    }
}
