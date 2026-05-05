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

package org.wso2.carbon.consent.mgt.core.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.PIICategoryValidity;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategoryBinding;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.API_VERSION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_CONSENT_SUBJECT_MISMATCH;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CAT_NAME_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CAT_NAME_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_UUID_NOT_FOUND;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.REJECTED_STATE;
import static org.wso2.carbon.consent.mgt.core.util.ConsentUtils.handleClientException;

/**
 * Utility methods for building consent receipt input objects and related operations. This class provides helper 
 * functions to construct {@link ReceiptInput} instances from high-level parameters, including resolving purpose
 * and PII category UUIDs to their internal database IDs via the consent manager. It also includes methods to 
 * retrieve or create default PII categories and purpose categories required for receipt construction.
 */
public class ConsentReceiptUtils {

    private static final String DEFAULT_PURPOSE_CATEGORY = "DEFAULT";
    private static final String DEFAULT_COLLECTION_METHOD = "V2";
    private static final String DEFAULT_CONSENT_TYPE = "EXPLICIT";
    private static final String TERMINATION_INDEFINITE = "VALID_UNTIL:INDEFINITE";

    private ConsentReceiptUtils() {

    }

    /**
     * Builds a {@link ReceiptInput} from the provided parameters by resolving purpose and element UUIDs
     * to their internal database IDs via the consent manager.
     *
     * @param language             Language code for the consent receipt.
     * @param subjectId            PII principal (data subject) user ID.
     * @param currentUser          Logged-in user making the request.
     * @param tenantDomain         Tenant domain of the request.
     * @param validityTime         Optional validity period in seconds; {@code null} means no expiry.
     * @param rejected             {@code true} to create the consent in REJECTED state.
     * @param authorizationUserIds List of user IDs required to authorize the consent (delegated consents).
     * @param properties           Optional key-value metadata to attach to the receipt.
     * @param serviceId            Identifier of the service to which consent is being given.
     * @param purposeBindings      Purpose-element bindings with UUID strings for resolution.
     * @param consentManager       Consent manager used to resolve UUIDs to internal IDs.
     * @return Fully populated {@link ReceiptInput} ready to pass to the consent management service.
     * @throws ConsentManagementException if any UUID cannot be resolved or a subject mismatch is detected.
     */
    public static ReceiptInput buildReceiptInput(String language, String subjectId, String currentUser,
                                                 String tenantDomain, Long validityTime, boolean rejected,
                                                 List<String> authorizationUserIds, Map<String, String> properties,
                                                 String serviceId, List<PurposePIICategoryBinding> purposeBindings,
                                                 ConsentManager consentManager)
            throws ConsentManagementException {

        ReceiptInput receiptInput = new ReceiptInput();
        receiptInput.setVersion(API_VERSION);
        receiptInput.setJurisdiction(StringUtils.EMPTY);
        receiptInput.setPolicyUrl(StringUtils.EMPTY);
        receiptInput.setCollectionMethod(DEFAULT_COLLECTION_METHOD);
        receiptInput.setAllowMultipleActiveReceipts(true);
        receiptInput.setLanguage(language);

        boolean hasAuthorizations = authorizationUserIds != null && !authorizationUserIds.isEmpty();
        if (!subjectId.equals(currentUser) && !hasAuthorizations) {
            throw handleClientException(ERROR_CODE_CONSENT_SUBJECT_MISMATCH, subjectId);
        }
        receiptInput.setPiiPrincipalId(subjectId);
        receiptInput.setTenantDomain(tenantDomain);

        if (validityTime != null) {
            receiptInput.setValidityTime(validityTime);
        }
        if (hasAuthorizations) {
            receiptInput.setAuthorizations(authorizationUserIds);
        }
        if (rejected) {
            receiptInput.setState(REJECTED_STATE);
        }
        if (properties != null) {
            receiptInput.setProperties(properties);
        }

        ReceiptServiceInput serviceInput = new ReceiptServiceInput();
        serviceInput.setService(serviceId);
        serviceInput.setSpDisplayName(serviceId);
        serviceInput.setTenantDomain(tenantDomain);

        List<ReceiptPurposeInput> purposeInputs = new ArrayList<>();
        if (purposeBindings != null) {
            for (PurposePIICategoryBinding binding : purposeBindings) {
                ReceiptPurposeInput purposeInput = new ReceiptPurposeInput();

                Purpose purpose = consentManager.getPurposeByUuid(binding.getPurposeId());
                if (purpose == null) {
                    throw handleClientException(ERROR_CODE_PURPOSE_UUID_NOT_FOUND, binding.getPurposeId());
                }
                purposeInput.setPurposeId(purpose.getId());

                PurposeVersion latestVersion = purpose.getLatestVersion();
                if (latestVersion != null) {
                    purposeInput.setPurposeVersionId(latestVersion.getUuid());
                }

                PurposeCategory purposeCategory = getDefaultPurposeCategory(consentManager);

                purposeInput.setConsentType(DEFAULT_CONSENT_TYPE);
                purposeInput.setPrimaryPurpose(true);
                purposeInput.setThirdPartyDisclosure(false);
                purposeInput.setPurposeCategoryId(List.of(purposeCategory.getId()));
                purposeInput.setTermination(TERMINATION_INDEFINITE);

                List<PIICategoryValidity> piiCategoryValidities = new ArrayList<>();
                if (binding.getPiiCategories() != null) {
                    for (PIICategory piiCategory : binding.getPiiCategories()) {
                        piiCategoryValidities.add(new PIICategoryValidity(
                                piiCategory.getId(), TERMINATION_INDEFINITE, true));
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

    /**
     * Retrieves the PII category used only for mandatory properties in the receipt input. If a PII category with 
     * the given name does not exist, it is created on the fly.
     * 
     * @param consentType      The name of the PII category to retrieve or create.
     * @param consentManager   The consent manager used to perform retrieval and creation operations.
     * @return The existing or newly created PII category.
     * @throws ConsentManagementException If an error occurs during retrieval or creation of the PII category.
     */
    public static PIICategory getDefaultPiiCategory(String consentType, ConsentManager consentManager)
            throws ConsentManagementException {

        PIICategory piiCategory;
        try {
            piiCategory = consentManager.getPIICategoryByName(consentType);
        } catch (ConsentManagementClientException e) {
            if (isInvalidPIICategoryError(e)) {
                PIICategory piiCategoryInput = new PIICategory(consentType, null, false, consentType);
                piiCategory = consentManager.addPIICategory(piiCategoryInput);
            } else {
                throw e;
            }
        }
        return piiCategory;
    }

    private static boolean isInvalidPIICategoryError(ConsentManagementClientException e) {

        return ERROR_CODE_PII_CAT_NAME_INVALID.getCode().equals(e.getErrorCode());
    }

    private static PurposeCategory getDefaultPurposeCategory(ConsentManager consentManager)
            throws ConsentManagementException {

        PurposeCategory purposeCategory;
        try {
            purposeCategory = consentManager.getPurposeCategoryByName(DEFAULT_PURPOSE_CATEGORY);
        } catch (ConsentManagementClientException e) {
            if (isInvalidPurposeCategoryError(e)) {
                PurposeCategory defaultPurposeCategory = new PurposeCategory(DEFAULT_PURPOSE_CATEGORY,
                        "For core functionalities of the product");
                purposeCategory = consentManager.addPurposeCategory(defaultPurposeCategory);
            } else {
                throw e;
            }
        }
        return purposeCategory;
    }

    private static boolean isInvalidPurposeCategoryError(ConsentManagementClientException e) {

        return ERROR_CODE_PURPOSE_CAT_NAME_INVALID.getCode().equals(e.getErrorCode());
    }
}
