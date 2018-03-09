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
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;

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
     * @param name Name of the purpose.
     * @return 200 Ok with purpose element.
     * @throws ConsentManagementException Consent Management Exception.
     */
    Purpose getPurposeByName(String name) throws ConsentManagementException;

    /**
     * This API is used to get all or filtered existing purposes.
     *
     * @param limit  Number of search results.
     * @param offset Start index of the search.
     * @return 200 OK with Filtered list of Purpose elements
     * @throws ConsentManagementException Consent Management Exception.
     */
    List<Purpose> listPurposes(int limit, int offset) throws ConsentManagementException;

    /**
     * This api is used to delete existing purpose by purpose Id.
     *
     * @param purposeId ID of the purpose.
     * @throws ConsentManagementException Consent Management Exception.
     */
    void deletePurpose(int purposeId) throws ConsentManagementException;

    /**
     * This API is used to check whether a purpose exists with given name.
     *
     * @param name Name of the purpose.
     * @return true, if an element is found.
     * @throws ConsentManagementException Consent Management Exception.
     */
    boolean isPurposeExists(String name) throws ConsentManagementException;

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
    List<PIICategory> listPIICategories(int limit, int offset) throws ConsentManagementException;

    /**
     * This API is used to delete PII category by ID.
     *
     * @param piiCategoryId ID of the PII category.
     * @throws ConsentManagementException Consent Management Exception.
     */
    void deletePIICategory(int piiCategoryId) throws ConsentManagementException;

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
     * @param limit          No of search results.
     * @param offset         start index of the search.
     * @param piiPrincipalId PII principal Id.
     * @param spTenantDomain SP tenant domain.
     * @param service        Service name.
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
     * This API is used to check whether a receipt exists for the user identified by the tenantAwareUser name in the
     * provided tenant.
     * @param receiptId Consent Receipt ID
     * @param tenantAwareUsername Tenant aware username
     * @param tenantId User tenant id
     * @return boolean true if receipt exists for match criteria
     */
    boolean isReceiptExist(String receiptId, String tenantAwareUsername, int tenantId) throws ConsentManagementException;

}
