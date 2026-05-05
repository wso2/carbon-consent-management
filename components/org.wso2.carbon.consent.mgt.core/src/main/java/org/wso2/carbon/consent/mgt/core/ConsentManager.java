/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.consent.mgt.core;

import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.AddReceiptResponse;
import org.wso2.carbon.consent.mgt.core.model.ConsentAuthorization;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;
import org.wso2.carbon.identity.core.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Consent manager service interface.
 *
 * @since 1.0.0
 */
public interface ConsentManager {

    /**
     * This API is used to add a new Purpose.
     *
     * @param purpose Purpose element with name and description.
     * @return 201 Created. Return purpose element with purpose Id.
     * @throws ConsentManagementException Consent Management Exception.
     */
    Purpose addPurpose(Purpose purpose) throws ConsentManagementException;

    /**
     * This API is used to get the purpose by purpose Id.
     *
     * @param purposeId ID of the purpose.
     * @return 200 OK with purpose element.
     * @throws ConsentManagementException Consent Management Exception.
     */
    Purpose getPurpose(int purposeId) throws ConsentManagementException;

    /**
     * This API is used to get the purpose by purpose name.
     *
     * @param name      Name of the purpose.
     * @param group     Name of the purpose group.
     * @param groupType Type of the purpose group.
     * @return Purpose matching the input criteria.
     * @throws ConsentManagementException Consent Management Exception.
     */
    Purpose getPurposeByName(String name, String group, String groupType) throws ConsentManagementException;

    /**
     * This API is used to get all or filtered existing purposes.
     * This method is deprecated. Use listPurposes(group, groupType, limit, offset) instead.
     *
     * @param limit  Number of search results.
     * @param offset Start index of the search.
     * @return 200 OK with Filtered list of Purpose elements
     * @throws ConsentManagementException Consent Management Exception.
     */
    @Deprecated
    List<Purpose> listPurposes(int limit, int offset) throws ConsentManagementException;

    /**
     * This API is used to get all or filtered existing purposes.
     *
     * @param group     Name of the purpose group.
     * @param groupType Type of the purpose group
     * @param limit     Number of search results.
     * @param offset    Start index of the search.
     * @return Filtered list of Purpose elements
     * @throws ConsentManagementException Consent Management Exception.
     */
    @Deprecated
    List<Purpose> listPurposes(String group, String groupType, int limit, int offset) throws ConsentManagementException;

    /**
     * This api is used to delete existing purpose by purpose Id.
     *
     * @param purposeId ID of the purpose.
     * @throws ConsentManagementException Consent Management Exception.
     */
    void deletePurpose(int purposeId) throws ConsentManagementException;

    /**
     * This API is used to delete existing purposes by tenant id.
     *
     * @param tenantId ID of the tenant.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default void deletePurposes(int tenantId) throws ConsentManagementException {

    };

    /**
     * This API is used to check whether a purpose exists with given name, group and groupType.
     *
     * @param name      Name of the purpose.
     * @param group     Purpose group.
     * @param groupType Purpose group type.
     * @return true, if an element is found.
     * @throws ConsentManagementException Consent Management Exception.
     */
    boolean isPurposeExists(String name, String group, String groupType) throws ConsentManagementException;

    /**
     * This API is used to add a new purpose category.
     *
     * @param purposeCategory purpose category element with name and description.
     * @return 201 created. Return PurposeCategory element with the category ID.
     * @throws ConsentManagementException Consent Management Exception.
     */
    PurposeCategory addPurposeCategory(PurposeCategory purposeCategory) throws ConsentManagementException;

    /**
     * This API is used to get purpose category by ID.
     *
     * @param purposeCategoryId Purpose category ID.
     * @return 200 Ok with purpose category element.
     * @throws ConsentManagementException Consent Management Exception.
     */
    PurposeCategory getPurposeCategory(int purposeCategoryId) throws ConsentManagementException;

    /**
     * This API is used to get purpose category by name.
     *
     * @param name Name of the purpose category.
     * @return 200 Ok with purpose category element.
     * @throws ConsentManagementException Consent Management Exception.
     */
    PurposeCategory getPurposeCategoryByName(String name) throws ConsentManagementException;

    /**
     * This API is used to list all or filtered list of purpose categories.
     *
     * @param limit  Number of search results.
     * @param offset Start index of the search.
     * @return Filtered list of purpose categories.
     * @throws ConsentManagementException Consent Management Exception.
     */
    List<PurposeCategory> listPurposeCategories(int limit, int offset) throws ConsentManagementException;

    /**
     * This API is used to delete purpose category by ID.
     *
     * @param purposeCategoryId ID of the purpose category to be deleted.
     * @throws ConsentManagementException Consent Management Exception.
     */
    void deletePurposeCategory(int purposeCategoryId) throws ConsentManagementException;


    /**
     * This API is used to delete existing purpose categories by tenant id.
     *
     * @param tenantId ID of the tenant.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default void deletePurposeCategories(int tenantId) throws ConsentManagementException {

    };

    /**
     * This API is used to check whether a purpose category exists for a given name.
     *
     * @param name Name of the purpose.
     * @return true if a category found.
     * @throws ConsentManagementException Consent Management Exception.
     */
    boolean isPurposeCategoryExists(String name) throws ConsentManagementException;

    /**
     * This API is used to add a new PII category.
     *
     * @param piiCategory PIICategory element with name and description.
     * @return 201 Created. Returns PII Category element with ID.
     * @throws ConsentManagementException Consent Management Exception.
     */
    PIICategory addPIICategory(PIICategory piiCategory) throws ConsentManagementException;

    /**
     * This API is used ot get PII category by name.
     *
     * @param name Name of the PII category.
     * @return 200 OK. Returns PII category with ID.
     * @throws ConsentManagementException Consent Management Exception.
     */
    PIICategory getPIICategoryByName(String name) throws ConsentManagementException;

    /**
     * This API is sued to get PII category by ID.
     *
     * @param piiCategoryId ID of the PII category.
     * @return 200 OK. Returns PII category
     * @throws ConsentManagementException Consent Management Exception.
     */
    PIICategory getPIICategory(int piiCategoryId) throws ConsentManagementException;

    /**
     * This API is used to list all or filtered set of PII categories.
     *
     * @param limit  Number of search results.
     * @param offset Start index of the search.
     * @return 200 Ok. Returns filtered list of PII category elements.
     * @throws ConsentManagementException Consent Management Exception.
     */
    @Deprecated
    List<PIICategory> listPIICategories(int limit, int offset) throws ConsentManagementException;

    /**
     * This API is used to delete PII category by ID.
     *
     * @param piiCategoryId ID of the PII category.
     * @throws ConsentManagementException Consent Management Exception.
     */
    void deletePIICategory(int piiCategoryId) throws ConsentManagementException;

    /**
     * This API is used to delete existing PII categories by tenant id.
     *
     * @param tenantId ID of the tenant.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default void deletePIICategories(int tenantId) throws ConsentManagementException {

    };

    /**
     * This API is sued to check whether a PII category exists for a given name.
     *
     * @param name Name of the PII category.
     * @return true if a category exists.
     * @throws ConsentManagementException Consent Management Exception.
     */
    boolean isPIICategoryExists(String name) throws ConsentManagementException;

    /**
     * This API is used to verify and store consent input.
     *
     * @param receiptInput consent input.
     * @throws ConsentManagementException Consent Management Exception.
     */
    AddReceiptResponse addConsent(ReceiptInput receiptInput) throws ConsentManagementException;

    /**
     * This API is used to retrieve the consent receipt.
     *
     * @param receiptId Receipt Id.
     * @return Consent Receipt.
     * @throws ConsentManagementException Consent Management Exception.
     */
    Receipt getReceipt(String receiptId) throws ConsentManagementException;

    /**
     * This API is used to search receipts.
     *
     * @param limit          No of search results.
     * @param offset         start index of the search.
     * @param piiPrincipalId PII principal Id.
     * @param spTenantDomain SP tenant domain.
     * @param service        Service name.
     * @return List of Receipts details.
     * @throws ConsentManagementException Consent Management Exception.
     */
    List<ReceiptListResponse> searchReceipts(int limit, int offset, String piiPrincipalId, String spTenantDomain,
                                             String service, String state) throws ConsentManagementException;

    /**
     * This API is used to search receipts.
     *
     * @param limit                 No of search results.
     * @param offset                start index of the search.
     * @param piiPrincipalId        PII principal Id.
     * @param spTenantDomain        SP tenant domain.
     * @param service               Service name.
     * @param principalTenantDomain Tenant domain of the principal.
     * @return List of Receipts details.
     * @throws ConsentManagementException Consent Management Exception.
     */
    List<ReceiptListResponse> searchReceipts(int limit, int offset, String piiPrincipalId, String spTenantDomain,
                                             String service, String state, String principalTenantDomain) throws
            ConsentManagementException;

    /**
     * This API is used to revoke a given receipt.
     *
     * @param receiptId Receipt Id.
     * @throws ConsentManagementException Consent Management Exception.
     */
    void revokeReceipt(String receiptId) throws ConsentManagementException;

    /**
     * This API is used to delete a given receipt.
     *
     * @param receiptId Receipt Id.
     * @throws ConsentManagementException Consent Management Exception.
     */
    void deleteReceipt(String receiptId) throws ConsentManagementException;

    /**
     * This API is used to delete existing receipts by tenant id.
     *
     * @param tenantId ID of the tenant.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default void deleteReceipts(int tenantId) throws ConsentManagementException {

    };

    /**
     * This API is used to check whether a receipt exists for the user identified by the tenantAwareUser name in the
     * provided tenant.
     *
     * @param receiptId           Consent Receipt ID
     * @param tenantAwareUsername Tenant aware username
     * @param tenantId            User tenant id
     * @return boolean true if receipt exists for match criteria
     */
    boolean isReceiptExist(String receiptId, String tenantAwareUsername, int tenantId) throws ConsentManagementException;

    /**
     * This API is used to retrieve all versions of a purpose.
     *
     * @param uuid UUID of the purpose.
     * @return List of {@link PurposeVersion} entries.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default List<PurposeVersion> listPurposeVersions(String uuid) throws ConsentManagementException {

        return new ArrayList<>();
    }

    /**
     * This API is used to retrieve a specific version of a purpose.
     *
     * @param purposeUuid UUID of the purpose.
     * @param versionUuid UUID of the version record.
     * @return {@link PurposeVersion} matching the given UUIDs.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default PurposeVersion getPurposeVersion(String purposeUuid, String versionUuid) throws ConsentManagementException {

        return null;
    }

    /**
     * This API is used to delete a specific version of a purpose.
     *
     * @param purposeUuid UUID of the purpose.
     * @param versionUuid UUID of the version record.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default void deletePurposeVersion(String purposeUuid, String versionUuid) throws ConsentManagementException {

    }

    /**
     * This API is used to delete an existing purpose by its UUID.
     *
     * @param uuid UUID of the purpose.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default void deletePurpose(String uuid) throws ConsentManagementException {

    }

    /**
     * This API is used to add a new version to an existing purpose identified by its UUID,
     * optionally designating it as the latest version.
     *
     * @param purposeUuid    UUID of the purpose.
     * @param purposeVersion {@link PurposeVersion} to add.
     * @param setAsLatest    Whether to set the new version as the latest version.
     * @return Created {@link PurposeVersion}.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default PurposeVersion addPurposeVersion(String purposeUuid, PurposeVersion purposeVersion, boolean setAsLatest)
            throws ConsentManagementException {

        return null;
    }

    /**
     * This API is used to get a purpose by its UUID.
     *
     * @param uuid UUID of the purpose.
     * @return Purpose matching the UUID.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default Purpose getPurposeByUuid(String uuid) throws ConsentManagementException {

        return null;
    }

    /**
     * This API is used to delete an existing PII category by its UUID.
     *
     * @param uuid UUID of the PII category.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default void deletePIICategory(String uuid) throws ConsentManagementException {

    }

    /**
     * This API is used to get a PII category by its UUID.
     *
     * @param uuid UUID of the PII category.
     * @return PIICategory matching the UUID.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default PIICategory getPIICategoryByUuid(String uuid) throws ConsentManagementException {

        return null;
    }

    /**
     * This API is used to get a specific version of a purpose by its version label string.
     *
     * @param purposeId    DB ID of the purpose.
     * @param versionLabel Version label string (e.g. "v1.0", "2024-Q1").
     * @return PurposeVersion matching the label.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default PurposeVersion getPurposeVersionByLabel(int purposeId, String versionLabel)
            throws ConsentManagementException {

        return null;
    }

    /**
     * This API is used to set the latest version of a purpose by version label.
     *
     * @param purposeId    DB ID of the purpose.
     * @param versionLabel Version label string to set as latest.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default void setLatestPurposeVersion(int purposeId, String versionLabel) throws ConsentManagementException {

    }

    /**
     * This API is used to create a purpose and its first version atomically.
     *
     * @param purpose      Purpose to create.
     * @param firstVersion First version to associate with the purpose.
     * @return Array where [0] is the created Purpose and [1] is the created PurposeVersion.
     * @throws ConsentManagementException Consent Management Exception.
     */
    default Object[] addPurpose(Purpose purpose, PurposeVersion firstVersion)
            throws ConsentManagementException {

        return null;
    }

    /**
     * Authorize or update a consent authorization record (V2 API).
     *
     * @param consentId  Consent receipt ID.
     * @param userId     User performing the authorization.
     * @param authStatus New status (APPROVED / REJECTED / REVOKED).
     * @throws ConsentManagementException if operation fails.
     */
    default void authorizeConsent(String consentId, String userId, String authStatus)
            throws ConsentManagementException {
    }

    /**
     * Get all authorization records for a consent (V2 API).
     */
    default List<ConsentAuthorization> getConsentAuthorizations(String consentId)
            throws ConsentManagementException {

        return Collections.emptyList();
    }

    /**
     * Validate consent status — lazily marks EXPIRED if validityTime has passed (V2 API).
     *
     * @param consentId Consent receipt ID.
     * @return Current effective status string.
     * @throws ConsentManagementException if operation fails.
     */
    default String validateConsentStatus(String consentId) throws ConsentManagementException {

        return "ACTIVE";
    }

    /**
     * Lists purposes with optional filter tree (V2 API).
     *
     * @param filterTree Filter tree from FilterTreeBuilder (null for no filtering)
     * @param limit      Maximum results
     * @param offset     Pagination offset
     * @return List of purposes matching filter
     * @throws ConsentManagementException if operation fails
     */
    default List<Purpose> listPurposes(Node filterTree, int limit, int offset)
            throws ConsentManagementException {

        return Collections.emptyList();
    }

    /**
     * Lists PII categories with optional filter tree (V2 API).
     *
     * @param filterTree Filter tree from FilterTreeBuilder (null for no filtering)
     * @param limit      Maximum results
     * @param offset     Pagination offset
     * @return List of PII categories matching filter
     * @throws ConsentManagementException if operation fails
     */
    default List<PIICategory> listPIICategories(Node filterTree, int limit, int offset)
            throws ConsentManagementException {

        return Collections.emptyList();
    }

    /**
     * Lists receipts/consents with explicit filter params (V2 API).
     *
     * @param subjectId        Filter by subject user ID (null for no filter)
     * @param serviceId        Filter by service ID (null for no filter)
     * @param state            Filter by consent state (null for no filter)
     * @param purposeId        Filter by purpose UUID string (null for no filter)
     * @param purposeVersionId Filter by purpose version UUID string (null for no filter)
     * @param limit            Maximum results
     * @param offset           Pagination offset
     * @return List of receipts matching filter
     * @throws ConsentManagementException if operation fails
     */
    default List<Receipt> listReceipts(String subjectId, String serviceId, String state,
                               String purposeId, String purposeVersionId, int limit, int offset)
            throws ConsentManagementException {

        return Collections.emptyList();
    }
}
