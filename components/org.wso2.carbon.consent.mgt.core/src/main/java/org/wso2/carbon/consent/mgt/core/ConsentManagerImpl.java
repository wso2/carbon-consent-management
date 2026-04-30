/*
 *
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.wso2.carbon.consent.mgt.core;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.consent.mgt.core.connector.PIIController;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.dao.ReceiptDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.model.AddReceiptResponse;
import org.wso2.carbon.consent.mgt.core.model.Address;
import org.wso2.carbon.consent.mgt.core.model.ConsentAuthorization;
import org.wso2.carbon.consent.mgt.core.model.ConsentManagerConfigurationHolder;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.PIICategoryValidity;
import org.wso2.carbon.consent.mgt.core.model.PiiController;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;
import org.wso2.carbon.consent.mgt.core.util.ConsentConfigParser;
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.*;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_AT_LEAST_ONE_CATEGORY_ID_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_AT_LEAST_ONE_PII_CATEGORY_ID_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_AT_LEAST_ONE_PURPOSE_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_AT_LEAST_ONE_SERVICE_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_CONSENT_TYPE_MANDATORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_GETTING_PUBLIC_CERT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_GETTING_TENANT_ID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_GETTING_USER_STORE_MANAGER;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_GET_DAO;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_INVALID_ARGUMENTS_FOR_LIM_OFFSET;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_IS_PRIMARY_PURPOSE_IS_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_ALREADY_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_ID_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_IS_ASSOCIATED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_NAME_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CAT_NAME_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_COLLECTION_METHOD_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_PRINCIPAL_ID_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_ALREADY_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CATEGORY_ALREADY_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CATEGORY_ID_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CATEGORY_NAME_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CAT_NAME_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_GROUP_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_GROUP_TYPE_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_ID_MANDATORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_ID_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_IS_ASSOCIATED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_ELEMENT_UUID_NOT_FOUND;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_UUID_NOT_FOUND;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_VERSION_ALREADY_EXISTS;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_VERSION_LABEL_ALREADY_EXISTS;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_VERSION_LABEL_NOT_FOUND;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_VERSION_MISMATCH;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_VERSION_NOT_FOUND;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_VERSION_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_NAME_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_NAME_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_HAS_VERSIONS_WITH_CONSENTS;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_PII_CONSTRAINT_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_RECEIPT_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_SERVICE_NAME_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_TENANT_ID_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_TERMINATION_IS_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_THIRD_PARTY_DISCLOSURE_IS_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_CANNOT_DELETE_LATEST_PURPOSE_VERSION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_CANNOT_DELETE_DEFAULT_PURPOSE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.ADDRESS;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.ADDRESS_COUNTRY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.ADDRESS_LOCALITY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.ADDRESS_REGION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.CONTACT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.EMAIL;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.ON_BEHALF;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.PHONE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.PII_CONTROLLER_NAME;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.PII_CONTROLLER_URL;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.POSTAL_CODE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.POST_OFFICE_BOX_NUMBER;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.STREET_ADDRESS;
import static org.wso2.carbon.consent.mgt.core.util.ConsentUtils.getTenantDomainFromCarbonContext;
import static org.wso2.carbon.consent.mgt.core.util.ConsentUtils.getTenantId;
import static org.wso2.carbon.consent.mgt.core.util.ConsentUtils.getTenantIdFromCarbonContext;
import static org.wso2.carbon.consent.mgt.core.util.ConsentUtils.handleClientException;
import static org.wso2.carbon.consent.mgt.core.util.ConsentUtils.handleServerException;
import static org.wso2.carbon.consent.mgt.core.util.LambdaExceptionUtils.rethrowConsumer;

/**
 * Consent manager service implementation.
 */
public class ConsentManagerImpl implements ConsentManager {

    private static final Log log = LogFactory.getLog(ConsentManagerImpl.class);
    private static final int DEFAULT_SEARCH_LIMIT = 100;
    private static final String PII_CONTROLLER = "piiControllers";
    private static final String RECEIPT_DAO = "receiptDAOs";
    private static final String PII_CATEGORY_DAO = "piiCategoryDAOs";
    private static final String PURPOSE_CATEGORY_DAO = "purposedCategoryDAOs";
    private static final String PURPOSE_DAO = "purposedDAOs";
    private static final String USE_CASE_SENSITIVE_USERNAME_FOR_CACHE_KEYS = "UseCaseSensitiveUsernameForCacheKeys";
    private Boolean isCaseSensitiveUserName;
    private List<PurposeDAO> purposeDAOs;
    private List<PurposeCategoryDAO> purposeCategoryDAOs;
    private List<PIICategoryDAO> piiCategoryDAOs;
    private List<ReceiptDAO> receiptDAOs;
    private ConsentConfigParser configParser;
    private List<PIIController> piiControllers;
    private RealmService realmService;

    public ConsentManagerImpl(ConsentManagerConfigurationHolder configHolder) {

        purposeDAOs = configHolder.getPurposeDAOs();
        purposeCategoryDAOs = configHolder.getPurposeCategoryDAOs();
        piiCategoryDAOs = configHolder.getPiiCategoryDAOs();
        receiptDAOs = configHolder.getReceiptDAOs();
        piiControllers = configHolder.getPiiControllers();
        configParser = configHolder.getConfigParser();
        realmService = configHolder.getRealmService();
    }

    /**
     * This API is used to add a new Purpose.
     *
     * @param purpose Purpose element with name and description.
     * @return 201 Created. Return purpose element with purpose Id.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public Purpose addPurpose(Purpose purpose) throws ConsentManagementException {

        validateInputParameters(purpose);
        Purpose purposeResponse = getPurposeDAO(purposeDAOs).addPurpose(purpose);
        return populatePiiCategories(purposeResponse);
    }

    /**
     * This API is used to get the purpose by purpose Id.
     *
     * @param purposeId ID of the purpose.
     * @return 200 OK with purpose element.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public Purpose getPurpose(int purposeId) throws ConsentManagementException {

        Purpose purpose = getPurposeById(purposeId);
        if (purpose == null) {
            if (log.isDebugEnabled()) {
                log.debug("No purpose found for the Id: " + purposeId);
            }
            throw ConsentUtils.handleClientException(ERROR_CODE_PURPOSE_ID_INVALID, String.valueOf(purposeId));
        }
        List<PurposePIICategory> purposePIICategories = new ArrayList<>();
        purpose.getPurposePIICategories().forEach(rethrowConsumer(piiCategory -> purposePIICategories.add
                (getPurposePIICategory(piiCategory))));
        purpose.setPurposePIICategories(purposePIICategories);

        if (purpose.getLatestVersionId() != null) {
            PurposeVersion latestVersion =
                    getPurposeDAO(purposeDAOs).getPurposeVersionByUuid(purpose.getLatestVersionId());
            purpose.setLatestVersion(latestVersion);
        }
        return purpose;
    }

    /**
     * This API is used to get the purpose by purpose name.
     *
     * @param name      Name of the purpose.
     * @param group     Name of the purpose group.
     * @param groupType Type of the purpose group.
     * @return Purpose matching the input criteria.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public Purpose getPurposeByName(String name, String group, String groupType) throws ConsentManagementException {

        Purpose purposeByName = getPurposeFromName(name, group, groupType);
        if (purposeByName == null) {
            if (log.isDebugEnabled()) {
                log.debug("No purpose found as the name: " + name + " in tenant domain: " +
                        getTenantDomainFromCarbonContext());
            }
            throw ConsentUtils.handleClientException(ERROR_CODE_PURPOSE_NAME_INVALID, name);
        }
        return getPurposeById(purposeByName.getId());
    }

    /**
     * This API is used to get all or filtered existing purposes.
     *
     * @param limit  Number of search results.
     * @param offset Start index of the search.
     * @return 200 OK with Filtered list of Purpose elements
     * @throws ConsentManagementException Consent Management Exception.
     */
    public List<Purpose> listPurposes(int limit, int offset) throws ConsentManagementException {

        validatePaginationParameters(limit, offset);

        if (limit == 0) {
            limit = getDefaultLimitFromConfig();
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defied the request, default to: " + limit);
            }
        }
        return getPurposeDAO(purposeDAOs).listPurposes(limit, offset, getTenantIdFromCarbonContext());
    }

    /**
     * This API is used to get all or filtered existing purposes.
     *
     * @param group     Name of the purpose group.
     * @param groupType Type of the purpose group.
     * @param limit     Number of search results.
     * @param offset    Start index of the search.
     * @return 200 OK with Filtered list of Purpose elements
     * @throws ConsentManagementException Consent Management Exception.
     */
    public List<Purpose> listPurposes(String group, String groupType, int limit, int offset) throws
            ConsentManagementException {

        validatePaginationParameters(limit, offset);

        if (limit == 0) {
            limit = getDefaultLimitFromConfig();
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defied the request, default to: " + limit);
            }
        }
        return getPurposeDAO(purposeDAOs).listPurposes(group, groupType, limit, offset, getTenantIdFromCarbonContext());
    }

    /**
     * This api is used to delete existing purpose by purpose Id.
     *
     * @param purposeId ID of the purpose.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public void deletePurpose(int purposeId) throws ConsentManagementException {

        if (purposeId <= 0) {
            if (log.isDebugEnabled()) {
                log.debug("Purpose Id is not found in the request or invalid purpose Id: " + purposeId);
            }
            throw handleClientException(ERROR_CODE_PURPOSE_ID_REQUIRED, null);
        }

        if (getPurposeById(purposeId) == null) {
            throw handleClientException(ERROR_CODE_PURPOSE_ID_INVALID, String.valueOf(purposeId));
        }

        if (getPurposeDAO(purposeDAOs).isPurposeUsed(purposeId)) {
            throw handleClientException(ERROR_CODE_PURPOSE_IS_ASSOCIATED, String.valueOf(purposeId));
        }
        int id = getPurposeDAO(purposeDAOs).deletePurpose(purposeId);
        if (log.isDebugEnabled()) {
            log.debug("Purpose deleted successfully. ID: " + id);
        }
    }

    @Override
    public void deletePurpose(String uuid) throws ConsentManagementException {

        Purpose purpose = getPurposeByUuid(uuid);
        if (DEFAULT_PURPOSE_GROUP.equals(purpose.getName())) {
            throw handleClientException(ERROR_CODE_CANNOT_DELETE_DEFAULT_PURPOSE, purpose.getName());
        }

        List<PurposeVersion> versions = listPurposeVersions(uuid);
        if (isEmpty(versions)) {
            deletePurpose(purpose.getId());
            return;
        }

        for (PurposeVersion version : versions) {
            if (getPurposeDAO(purposeDAOs).isPurposeVersionUsed(version.getUuid())) {
                throw handleClientException(ERROR_CODE_PURPOSE_HAS_VERSIONS_WITH_CONSENTS, purpose.getName());
            }
        }

        getPurposeDAO(purposeDAOs).deletePurposeWithVersions(purpose.getId());
        if (log.isDebugEnabled()) {
            log.debug("Purpose and versions deleted successfully. UUID: " + uuid);
        }
    }

    /**
     * This API is used to delete existing purposes by tenant id.
     *
     * @param tenantId Id of the tenant.
     * @throws ConsentManagementException Consent Management Exception.
     */
    @Override
    public void deletePurposes(int tenantId) throws ConsentManagementException {

        if (tenantId <= 0) {
            if (log.isDebugEnabled()) {
                log.debug("Tenant id is not found in the request or invalid tenant id: " + tenantId);
            }
            throw handleClientException(ERROR_CODE_TENANT_ID_REQUIRED, null);
        }

        getPurposeDAO(purposeDAOs).deletePurposesByTenantId(tenantId);
        if (log.isDebugEnabled()) {
            log.debug("All purposes deleted successfully for tenant: " + tenantId);
        }
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
    public boolean isPurposeExists(String name, String group, String groupType) throws ConsentManagementException {

        return getPurposeFromName(name, group, groupType) != null;
    }

    /**
     * This API is used to add a new purpose category.
     *
     * @param purposeCategory purpose category element with name and description.
     * @return 201 created. Return PurposeCategory element with the category ID.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public PurposeCategory addPurposeCategory(PurposeCategory purposeCategory) throws ConsentManagementException {

        validateInputParameters(purposeCategory);
        PurposeCategory category = getPurposeCategoryDAO(purposeCategoryDAOs).addPurposeCategory(purposeCategory);
        if (log.isDebugEnabled()) {
            log.debug("Purpose category created successfully with the name: " + category.getName());
        }
        return category;
    }

    /**
     * This API is used to get purpose category by ID.
     *
     * @param purposeCategoryId Purpose category ID.
     * @return 200 Ok with purpose category element.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public PurposeCategory getPurposeCategory(int purposeCategoryId) throws ConsentManagementException {

        PurposeCategory category = getPurposeCategoryById(purposeCategoryId);
        if (category == null) {
            if (log.isDebugEnabled()) {
                log.debug("No purpose category found for the Id: " + purposeCategoryId);
            }
            throw ConsentUtils.handleClientException(ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID,
                    String.valueOf(purposeCategoryId));
        }
        return category;
    }

    /**
     * This API is used to get purpose category by name.
     *
     * @param name Name of the purpose category.
     * @return 200 Ok with purpose category element.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public PurposeCategory getPurposeCategoryByName(String name) throws ConsentManagementException {

        PurposeCategory purposeCategoryByName = getPurposeCategoryFromName(name);
        if (purposeCategoryByName == null) {
            if (log.isDebugEnabled()) {
                log.debug("No purpose category found for the name: " + name);
            }
            throw ConsentUtils.handleClientException(ERROR_CODE_PURPOSE_CAT_NAME_INVALID, name);
        }
        return purposeCategoryByName;
    }

    /**
     * This API is used to list all or filtered list of purpose categories.
     *
     * @param limit  Number of search results.
     * @param offset Start index of the search.
     * @return Filtered list of purpose categories.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public List<PurposeCategory> listPurposeCategories(int limit, int offset) throws ConsentManagementException {

        validatePaginationParameters(limit, offset);

        if (limit == 0) {
            limit = getDefaultLimitFromConfig();
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defied the request, default to: " + limit);
            }
        }
        return getPurposeCategoryDAO(purposeCategoryDAOs).listPurposeCategories(limit, offset,
                getTenantIdFromCarbonContext());
    }

    /**
     * This API is used to delete purpose category by ID.
     *
     * @param purposeCategoryId ID of the purpose category to be deleted.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public void deletePurposeCategory(int purposeCategoryId) throws ConsentManagementException {

        if (purposeCategoryId <= 0) {
            if (log.isDebugEnabled()) {
                log.debug("Purpose Category Id is not found in the request or invalid Id: " + purposeCategoryId);
            }
            throw handleClientException(ERROR_CODE_PURPOSE_CATEGORY_ID_REQUIRED, null);
        }

        if (getPurposeCategoryById(purposeCategoryId) == null) {
            throw handleClientException(ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID, String.valueOf(purposeCategoryId));
        }
        int id = getPurposeCategoryDAO(purposeCategoryDAOs).deletePurposeCategory(purposeCategoryId);
        if (log.isDebugEnabled()) {
            log.debug("Purpose category deleted successfully. ID: " + id);
        }
    }

    /**
     * This API is used to delete existing purpose categories by tenant id.
     *
     * @param tenantId Id of the tenant.
     * @throws ConsentManagementException Consent Management Exception.
     */
    @Override
    public void deletePurposeCategories(int tenantId) throws ConsentManagementException {

        if (tenantId <= 0) {
            if (log.isDebugEnabled()) {
                log.debug("Tenant id is not found in the request or invalid tenant id: " + tenantId);
            }
            throw handleClientException(ERROR_CODE_TENANT_ID_REQUIRED, null);
        }

        getPurposeCategoryDAO(purposeCategoryDAOs).deletePurposeCategoriesByTenantId(tenantId);
        if (log.isDebugEnabled()) {
            log.debug("All purpose categories deleted successfully for tenant: " + tenantId);
        }
    };

    /**
     * This API is used to check whether a purpose category exists for a given name.
     *
     * @param name Name of the purpose.
     * @return true if a category found.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public boolean isPurposeCategoryExists(String name) throws ConsentManagementException {

        return getPurposeCategoryFromName(name) != null;
    }

    /**
     * This API is used to add a new PII category.
     *
     * @param piiCategory PIICategory element with name and description.
     * @return 201 Created. Returns PII Category element with ID.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public PIICategory addPIICategory(PIICategory piiCategory) throws ConsentManagementException {

        validateInputParameters(piiCategory);
        PIICategory category = getPiiCategoryDAO(piiCategoryDAOs).addPIICategory(piiCategory);
        if (log.isDebugEnabled()) {
            log.debug("PII category added successfully with the name: " + category.getName());
        }
        return category;
    }

    /**
     * This API is used ot get PII category by name.
     *
     * @param name Name of the PII category.
     * @return 200 OK. Returns PII category with ID.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public PIICategory getPIICategoryByName(String name) throws ConsentManagementException {

        PIICategory piiCategoryByName = getPiiCategoryFromName(name);
        if (piiCategoryByName == null) {
            if (log.isDebugEnabled()) {
                log.debug("No PII category found with the name: " + name);
            }
            throw ConsentUtils.handleClientException(ERROR_CODE_PII_CAT_NAME_INVALID, name);
        }
        return piiCategoryByName;
    }

    /**
     * This API is sued to get PII category by ID.
     *
     * @param piiCategoryId ID of the PII category.
     * @return 200 OK. Returns PII category
     * @throws ConsentManagementException Consent Management Exception.
     */
    public PIICategory getPIICategory(int piiCategoryId) throws ConsentManagementException {

        PIICategory piiCategory = getPiiCategoryById(piiCategoryId);
        if (piiCategory == null) {
            if (log.isDebugEnabled()) {
                log.debug("No PII category found with the Id: " + piiCategoryId);
            }
            throw ConsentUtils.handleClientException(ERROR_CODE_PII_CATEGORY_ID_INVALID, String.valueOf(piiCategoryId));
        }
        return piiCategory;
    }

    /**
     * This API is used to list all or filtered set of PII categories.
     *
     * @param limit  Number of search results.
     * @param offset Start index of the search.
     * @return 200 Ok. Returns filtered list of PII category elements.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public List<PIICategory> listPIICategories(int limit, int offset) throws ConsentManagementException {

        validatePaginationParameters(limit, offset);

        if (limit == 0) {
            limit = getDefaultLimitFromConfig();
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defied the request, default to: " + limit);
            }
        }
        return getPiiCategoryDAO(piiCategoryDAOs).listPIICategories(limit, offset, getTenantIdFromCarbonContext());
    }

    /**
     * This API is used to delete PII category by ID.
     *
     * @param piiCategoryId ID of the PII category.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public void deletePIICategory(int piiCategoryId) throws ConsentManagementException {

        if (piiCategoryId <= 0) {
            if (log.isDebugEnabled()) {
                log.debug("PII Category Id is not found in the request or invalid PII category Id: " + piiCategoryId);
            }
            throw handleClientException(ERROR_CODE_PII_CATEGORY_ID_REQUIRED, null);
        }

        if (getPiiCategoryById(piiCategoryId) == null) {
            throw handleClientException(ERROR_CODE_PII_CATEGORY_ID_INVALID, String.valueOf(piiCategoryId));
        }
        if (getPiiCategoryDAO(piiCategoryDAOs).isPIICategoryUsed(piiCategoryId)) {
            throw handleClientException(ERROR_CODE_PII_CATEGORY_IS_ASSOCIATED, String.valueOf(piiCategoryId));
        }
        int id = getPiiCategoryDAO(piiCategoryDAOs).deletePIICategory(piiCategoryId);
        if (log.isDebugEnabled()) {
            log.debug("PII Category deleted successfully. ID: " + id);
        }
    }

    @Override
    public void deletePIICategory(String uuid) throws ConsentManagementException {

        PIICategory category = getPIICategoryByUuid(uuid);
        deletePIICategory(category.getId());
    }

    /**
     * This API is used to delete existing PII categories by tenant id.
     *
     * @param tenantId Id of the tenant.
     * @throws ConsentManagementException Consent Management Exception.
     */
    @Override
    public void deletePIICategories(int tenantId) throws ConsentManagementException {

        if (tenantId <= 0) {
            if (log.isDebugEnabled()) {
                log.debug("Tenant id is not found in the request or invalid tenant id: " + tenantId);
            }
            throw handleClientException(ERROR_CODE_TENANT_ID_REQUIRED, null);
        }

        getPiiCategoryDAO(piiCategoryDAOs).deletePIICategoriesByTenantId(tenantId);
        if (log.isDebugEnabled()) {
            log.debug("All PII categories deleted successfully for tenant: " + tenantId);
        }
    };

    /**
     * This API is sued to check whether a PII category exists for a given name.
     *
     * @param name Name of the PII category.
     * @return true if a category exists.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public boolean isPIICategoryExists(String name) throws ConsentManagementException {

        return getPiiCategoryFromName(name) != null;
    }

    /**
     * This API is used to verify and store consent input.
     *
     * @param receiptInput consent input.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public AddReceiptResponse addConsent(ReceiptInput receiptInput) throws ConsentManagementException {

        validateInputParameters(receiptInput);
        receiptInput.setConsentReceiptId(generateConsentReceiptId());
        setAPIVersion(receiptInput);
        setPIIControllerInfo(receiptInput);
        if (!isUserNameCaseSensitive(receiptInput.getPiiPrincipalId())) {
            receiptInput.setPiiPrincipalId(getLowerCaseUserName(receiptInput.getPiiPrincipalId()));
        }

        java.util.List<String> authorizations = receiptInput.getAuthorizations();
        List<ConsentAuthorization> authorizationsList = new ArrayList<>();
        if (authorizations != null && !authorizations.isEmpty()) {
            receiptInput.setState(PENDING_STATE);
            long now = System.currentTimeMillis();
            for (String userId : authorizations) {
                authorizationsList.add(new ConsentAuthorization(receiptInput.getConsentReceiptId(), userId, PENDING_STATE, now));
            }
        }

        getReceiptsDAO(receiptDAOs).addReceiptWithAuthorizations(receiptInput, authorizationsList);

        if (log.isDebugEnabled()) {
            log.debug("Consent stored successfully with the Id: " + receiptInput.getConsentReceiptId());
        }
        return new AddReceiptResponse(receiptInput.getConsentReceiptId(), receiptInput.getCollectionMethod(),
                receiptInput.getLanguage(), receiptInput.getPiiPrincipalId(), receiptInput.getTenantDomain());
    }

    @Override
    public AddReceiptResponse addConsent(String userId, String applicationId, String tenantDomain,
                                         String consentType, Map<String, List<Integer>> purposeToElementIds,
                                         String collectionMethod, String state)
            throws ConsentManagementException {

        PurposeCategory defaultCategory = getPurposeCategoryByName(DEFAULT_PURPOSE_GROUP);
        int defaultPurposeCategoryId = defaultCategory.getId();

        List<ReceiptPurposeInput> purposeInputs = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : purposeToElementIds.entrySet()) {
            Purpose purpose = getPurposeByUuid(entry.getKey());

            ReceiptPurposeInput purposeInput = new ReceiptPurposeInput();
            purposeInput.setPurposeId(purpose.getId());
            PurposeVersion latestVersion = purpose.getLatestVersion();
            if (latestVersion != null) {
                purposeInput.setPurposeVersionId(latestVersion.getUuid());
            }
            purposeInput.setConsentType(consentType);
            purposeInput.setPrimaryPurpose(true);
            purposeInput.setThirdPartyDisclosure(false);
            purposeInput.setPurposeCategoryId(Arrays.asList(defaultPurposeCategoryId));
            purposeInput.setTermination(TERMINATION_INDEFINITE);

            List<PIICategoryValidity> piiValidities = new ArrayList<>();
            if (entry.getValue() != null) {
                for (Integer elementId : entry.getValue()) {
                    PIICategory element = getPIICategory(elementId);
                    piiValidities.add(new PIICategoryValidity(element.getId(), TERMINATION_INDEFINITE));
                }
            }
            purposeInput.setPiiCategory(piiValidities);
            purposeInputs.add(purposeInput);
        }

        ReceiptServiceInput serviceInput = new ReceiptServiceInput();
        serviceInput.setService(applicationId);
        serviceInput.setSpDisplayName(applicationId);
        serviceInput.setTenantDomain(tenantDomain);
        serviceInput.setPurposes(purposeInputs);

        ReceiptInput receiptInput = new ReceiptInput();
        receiptInput.setVersion(API_VERSION);
        receiptInput.setCollectionMethod(collectionMethod);
        receiptInput.setJurisdiction("");
        receiptInput.setPolicyUrl("");
        receiptInput.setLanguage("");
        receiptInput.setPiiPrincipalId(userId);
        receiptInput.setTenantDomain(tenantDomain);
        receiptInput.setServices(Arrays.asList(serviceInput));
        receiptInput.setAllowMultipleActiveReceipts(true);
        receiptInput.setState(state);

        return addConsent(receiptInput);
    }

    /**
     * This API is used to retrieve the consent receipt.
     *
     * @param receiptId Receipt Id.
     * @return Consent Receipt.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public Receipt getReceipt(String receiptId) throws ConsentManagementException {

        Receipt receipt = getReceiptsDAO(receiptDAOs).getReceipt(receiptId);

        if (receipt == null || receipt.getConsentReceiptId() == null) {
            if (log.isDebugEnabled()) {
                log.debug("No receipt found with the Id: " + receiptId);
            }
            String message = String.format(ERROR_CODE_RECEIPT_ID_INVALID.getMessage(), receiptId) + " in tenant: " +
                    ConsentUtils.getTenantDomainFromCarbonContext();
            throw new ConsentManagementClientException(message, ERROR_CODE_RECEIPT_ID_INVALID.getCode());
        }
        populateTenantDomain(receipt);
        setPIIControllerInfo(receipt);
        setPublicKey(receipt);
        return receipt;
    }

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
    public List<ReceiptListResponse> searchReceipts(int limit, int offset, String piiPrincipalId, String spTenantDomain,
                                                    String service, String state) throws ConsentManagementException {

        validatePaginationParameters(limit, offset);
        if (limit == 0) {
            limit = getDefaultLimitFromConfig();
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defied the request, default to: " + limit);
            }
        }
        String piiPrincipalTenantId = ConsentUtils.getTenantDomainFromCarbonContext();
        List<ReceiptListResponse> receiptListResponses = searchReceipts(limit, offset, piiPrincipalId, spTenantDomain,
                service, state, piiPrincipalTenantId);
        receiptListResponses.forEach(rethrowConsumer(receiptListResponse -> receiptListResponse.setTenantDomain
                (ConsentUtils.getTenantDomain(realmService, receiptListResponse.getTenantId()))));

        return receiptListResponses;
    }

    public List<ReceiptListResponse> searchReceipts(int limit, int offset, String piiPrincipalId, String spTenantDomain,
                                                    String service, String state, String principleTenantDomain) throws
            ConsentManagementException {

        int spTenantId = 0;
        int principalTenantId = 0;
        if (StringUtils.isNotBlank(spTenantDomain)) {
            spTenantId = ConsentUtils.getTenantId(realmService, spTenantDomain);
        }
        if (StringUtils.isNotBlank(principleTenantDomain)) {
            principalTenantId = ConsentUtils.getTenantId(realmService, principleTenantDomain);
        }
        validatePaginationParameters(limit, offset);
        if (limit == 0) {
            limit = getDefaultLimitFromConfig();
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defied the request, default to: " + limit);
            }
        }
        if (StringUtils.isNotBlank(piiPrincipalId) && !isUserNameCaseSensitive(piiPrincipalId)) {
            piiPrincipalId = getLowerCaseUserName(piiPrincipalId);
        }
        List<ReceiptListResponse> receiptListResponses = getReceiptsDAO(receiptDAOs).searchReceipts(limit, offset,
                piiPrincipalId, spTenantId, service, state, principalTenantId);
        receiptListResponses.forEach(rethrowConsumer(receiptListResponse -> receiptListResponse.setTenantDomain
                (ConsentUtils.getTenantDomain(realmService, receiptListResponse
                        .getTenantId()))));

        return receiptListResponses;
    }

    /**
     * This API is used to revoke a given receipt.
     *
     * @param receiptId Receipt Id.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public void revokeReceipt(String receiptId) throws ConsentManagementException {

        getReceiptsDAO(receiptDAOs).revokeReceipt(receiptId);
        if (log.isDebugEnabled()) {
            log.debug("Receipt revoked successfully with the Id: " + receiptId);
        }
    }

    /**
     * This API is used to delete a given receipt.
     *
     * @param receiptId Receipt Id.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public void deleteReceipt(String receiptId) throws ConsentManagementException {

        getReceiptsDAO(receiptDAOs).deleteReceipt(receiptId);
        if (log.isDebugEnabled()) {
            log.debug("Receipt deleted successfully with the Id: " + receiptId);
        }
    }

    /**
     * This API is used to delete existing receipts by tenant id.
     *
     * @param tenantId Id of the tenant.
     * @throws ConsentManagementException Consent Management Exception.
     */
    @Override
    public void deleteReceipts(int tenantId) throws ConsentManagementException {

        if (tenantId <= 0) {
            if (log.isDebugEnabled()) {
                log.debug("Tenant id is not found in the request or invalid tenant id: " + tenantId);
            }
            throw handleClientException(ERROR_CODE_TENANT_ID_REQUIRED, null);
        }

        getReceiptsDAO(receiptDAOs).deleteReceiptsByTenantId(tenantId);

        if (log.isDebugEnabled()) {
            log.debug("All receipts deleted successfully for tenant: " + tenantId);
        }
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
    @Override
    public boolean isReceiptExist(String receiptId, String tenantAwareUsername, int tenantId) throws
            ConsentManagementException {

        if (!isUserNameCaseSensitive(tenantAwareUsername)) {
            tenantAwareUsername = getLowerCaseUserName(tenantAwareUsername);
        }
        return getReceiptsDAO(receiptDAOs).isReceiptExist(receiptId, tenantAwareUsername, tenantId);
    }

    /**
     * This API is used to add a new version to an existing purpose.
     *
     * @param purposeUuid    UUID of the purpose.
     * @param purposeVersion {@link PurposeVersion} to add.
     * @return Created {@link PurposeVersion}.
     * @throws ConsentManagementException Consent Management Exception.
     */
    @Override
    public PurposeVersion addPurposeVersion(String purposeUuid, PurposeVersion purposeVersion, boolean setAsLatest)
            throws ConsentManagementException {

        // Validate version label is provided.
        String versionLabel = purposeVersion.getVersion();
        if (isBlank(versionLabel)) {
            throw handleClientException(ERROR_CODE_PURPOSE_VERSION_REQUIRED, null);
        }

        // Confirm the purpose exists.
        Purpose purpose = getPurposeByUuid(purposeUuid);

        // Check for duplicate version label.
        // Note: purposes created via the V1 API may have no versions. Adding a version to such a purpose works,
        // but there is no auto-snapshot of the pre-versioning state. The V2 API enforces version at creation
        // time to prevent this for new purposes, but existing V1 purposes are unaffected.
        List<PurposeVersion> existing = getPurposeDAO(purposeDAOs).listPurposeVersions(purpose.getUuid());
        if (existing.stream().anyMatch(v -> versionLabel.equals(v.getVersion()))) {
            throw handleClientException(ERROR_CODE_PURPOSE_VERSION_LABEL_ALREADY_EXISTS, versionLabel);
        }

        if (isNotEmpty(purposeVersion.getPurposePIICategories())) {
            purposeVersion.getPurposePIICategories().forEach(rethrowConsumer(purposePIICategory -> {
                int id = purposePIICategory.getId();
                if (getPiiCategoryById(id) == null) {
                    throw handleClientException(ERROR_CODE_PII_CATEGORY_ID_INVALID, String.valueOf(id));
                }
                if (purposePIICategory.getMandatory() == null) {
                    throw handleClientException(ERROR_CODE_PURPOSE_PII_CONSTRAINT_REQUIRED, String.valueOf(id));
                }
            }));
        }

        purposeVersion.setPurposeId(purpose.getId());
        purposeVersion.setTenantId(getTenantIdFromCarbonContext());

        return populatePiiCategories(getPurposeDAO(purposeDAOs).addPurposeVersion(purposeVersion, setAsLatest));
    }

    /**
     * This API is used to retrieve all versions of a purpose.
     *
     * @param uuid UUID of the purpose.
     * @return List of {@link PurposeVersion} entries.
     * @throws ConsentManagementException Consent Management Exception.
     */
    @Override
    public List<PurposeVersion> listPurposeVersions(String uuid) throws ConsentManagementException {

        if (isBlank(uuid)) {
            throw handleClientException(ERROR_CODE_PURPOSE_ID_REQUIRED, null);
        }

        // Confirm the purpose exists.
        Purpose purpose = getPurposeByUuid(uuid);
        if (purpose == null) {
            throw handleClientException(ERROR_CODE_PURPOSE_ID_INVALID, uuid);
        }

        return getPurposeDAO(purposeDAOs).listPurposeVersions(uuid);
    }

    /**
     * This API is used to retrieve a specific version of a purpose.
     *
     * @param purposeUuid UUID of the purpose.
     * @param versionUuid UUID of the version record.
     * @return {@link PurposeVersion} matching the given UUIDs.
     * @throws ConsentManagementException Consent Management Exception.
     */
    @Override
    public PurposeVersion getPurposeVersion(String purposeUuid, String versionUuid) throws ConsentManagementException {

        if (isBlank(purposeUuid)) {
            throw handleClientException(ERROR_CODE_PURPOSE_ID_REQUIRED, null);
        }

        Purpose purpose = getPurposeByUuid(purposeUuid);
        if (purpose == null) {
            throw handleClientException(ERROR_CODE_PURPOSE_ID_INVALID, purposeUuid);
        }

        PurposeVersion version = getPurposeDAO(purposeDAOs).getPurposeVersionByUuid(versionUuid);
        if (version == null) {
            throw handleClientException(ERROR_CODE_PURPOSE_VERSION_NOT_FOUND, versionUuid);
        }
        if (version.getPurposeId() != purpose.getId()) {
            throw new ConsentManagementClientException(
                    String.format(ERROR_CODE_PURPOSE_VERSION_MISMATCH.getMessage(),
                            versionUuid, purposeUuid),
                    ERROR_CODE_PURPOSE_VERSION_MISMATCH.getCode());
        }
        return version;
    }

    /**
     * This API is used to delete a specific version of a purpose.
     *
     * @param purposeUuid UUID of the purpose.
     * @param versionUuid UUID of the version record.
     * @throws ConsentManagementException Consent Management Exception.
     */
    @Override
    public void deletePurposeVersion(String purposeUuid, String versionUuid) throws ConsentManagementException {

        // Validates: purpose exists, version exists, version belongs to purpose.
        getPurposeVersion(purposeUuid, versionUuid);

        // Prevent deletion of the version currently marked as latest (would violate FK constraint).
        Purpose purpose = getPurposeByUuid(purposeUuid);
        if (versionUuid.equals(purpose.getLatestVersionId())) {
            throw handleClientException(ERROR_CODE_CANNOT_DELETE_LATEST_PURPOSE_VERSION, versionUuid);
        }

        if (getPurposeDAO(purposeDAOs).isPurposeVersionUsed(versionUuid)) {
            throw handleClientException(ERROR_CODE_PURPOSE_IS_ASSOCIATED, versionUuid);
        }

        getPurposeDAO(purposeDAOs).deletePurposeVersion(versionUuid);
        if (log.isDebugEnabled()) {
            log.debug("Purpose version deleted successfully. versionUuid: " + versionUuid);
        }
    }

    @Override
    public Purpose getPurposeByUuid(String uuid) throws ConsentManagementException {

        Purpose purpose = getPurposeDAO(purposeDAOs).getPurposeByUuid(uuid, getTenantIdFromCarbonContext());
        if (purpose == null) {
            throw handleClientException(ERROR_CODE_PURPOSE_UUID_NOT_FOUND, uuid);
        }
        List<PurposePIICategory> purposePIICategories = new ArrayList<>();
        purpose.getPurposePIICategories().forEach(rethrowConsumer(
                piiCategory -> purposePIICategories.add(getPurposePIICategory(piiCategory))));
        purpose.setPurposePIICategories(purposePIICategories);
        if (purpose.getLatestVersionId() != null) {
            PurposeVersion latestVersion =
                    getPurposeDAO(purposeDAOs).getPurposeVersionByUuid(purpose.getLatestVersionId());
            purpose.setLatestVersion(latestVersion);
        }
        return purpose;
    }

    @Override
    public PIICategory getPIICategoryByUuid(String uuid) throws ConsentManagementException {

        PIICategory category = getPiiCategoryDAO(piiCategoryDAOs).getPIICategoryByUuid(uuid,
                getTenantIdFromCarbonContext());
        if (category == null) {
            throw handleClientException(ERROR_CODE_ELEMENT_UUID_NOT_FOUND, uuid);
        }
        return category;
    }

    @Override
    public PurposeVersion getPurposeVersionByLabel(int purposeId, String versionLabel)
            throws ConsentManagementException {

        PurposeVersion version = getPurposeDAO(purposeDAOs).getPurposeVersionByLabel(purposeId, versionLabel,
                getTenantIdFromCarbonContext());
        if (version == null) {
            throw handleClientException(ERROR_CODE_PURPOSE_VERSION_LABEL_NOT_FOUND, versionLabel);
        }
        return version;
    }

    @Override
    public void setLatestPurposeVersion(int purposeId, String versionLabel) throws ConsentManagementException {

        if (purposeId <= 0) {
            throw handleClientException(ERROR_CODE_PURPOSE_ID_REQUIRED, null);
        }
        int tenantId = getTenantIdFromCarbonContext();
        PurposeVersion version = getPurposeDAO(purposeDAOs).getPurposeVersionByLabel(purposeId, versionLabel,
                tenantId);
        if (version == null) {
            throw handleClientException(ERROR_CODE_PURPOSE_VERSION_LABEL_NOT_FOUND, versionLabel);
        }
        getPurposeDAO(purposeDAOs).updateLatestVersionId(purposeId, version.getUuid(), tenantId);
    }

    @Override
    public Object[] addPurpose(Purpose purpose, PurposeVersion firstVersion)
            throws ConsentManagementException {

        Purpose createdPurpose = addPurpose(purpose);

        PurposeVersion createdVersion = addPurposeVersion(createdPurpose.getUuid(), firstVersion, true);
        createdPurpose.setLatestVersion(createdVersion);
        return new Object[]{createdPurpose, createdVersion};
    }

    /**
     * Lists purposes with tree-based filtering and pagination.
     *
     * @param filterTree Node tree for attribute-based filtering (null for no filtering).
     * @param limit      Maximum number of results to return.
     * @param offset     Pagination offset.
     * @return List of purposes matching the filter.
     * @throws ConsentManagementException if retrieval fails.
     */
    @Override
    public List<Purpose> listPurposes(Node filterTree, int limit, int offset)
            throws ConsentManagementException {

        if (limit == 0) {
            limit = getDefaultLimitFromConfig();
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defied the request, default to: " + limit);
            }
        }
        validatePaginationParameters(limit, offset);

        List<Purpose> purposes = getPurposeDAO(purposeDAOs).listPurposes(filterTree, limit, offset,
                getTenantIdFromCarbonContext());

        // Fetch latest version for each purpose if it exists
        if (purposes != null) {
            for (Purpose purpose : purposes) {
                if (purpose.getLatestVersionId() != null) {
                    PurposeVersion latestVersion = getPurposeDAO(purposeDAOs)
                            .getPurposeVersionByUuid(purpose.getLatestVersionId());
                    purpose.setLatestVersion(latestVersion);
                }
            }
        }
        return purposes;
    }

    /**
     * Lists PII categories with tree-based filtering and pagination.
     *
     * @param filterTree Node tree for attribute-based filtering (null for no filtering).
     * @param limit      Maximum number of results to return.
     * @param offset     Pagination offset.
     * @return List of PII categories matching the filter.
     * @throws ConsentManagementException if retrieval fails.
     */
    @Override
    public List<PIICategory> listPIICategories(Node filterTree, int limit, int offset)
            throws ConsentManagementException {

        if (limit == 0) {
            limit = getDefaultLimitFromConfig();
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defied the request, default to: " + limit);
            }
        }
        validatePaginationParameters(limit, offset);
        return getPiiCategoryDAO(piiCategoryDAOs).listPIICategories(filterTree, limit, offset,
                getTenantIdFromCarbonContext());
    }

    /**
     * Lists receipts (consents) with tree-based filtering and pagination.
     *
     * @param limit      Maximum number of results to return.
     * @param offset     Pagination offset.
     * @return List of receipts matching the filter.
     * @throws ConsentManagementException if retrieval fails.
     */
    @Override
    public List<Receipt> listReceipts(String subjectId, String serviceId, String state,
                                      String purposeId, String purposeVersionId, int limit, int offset)
            throws ConsentManagementException {

        if (limit == 0) {
            limit = getDefaultLimitFromConfig();
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defied the request, default to: " + limit);
            }
        }
        validatePaginationParameters(limit, offset);
        return getReceiptsDAO(receiptDAOs).listReceipts(subjectId, serviceId, state, purposeId,
                purposeVersionId, limit, offset, getTenantIdFromCarbonContext());
    }

    /**
     * Updates authorization status for a user on a consent receipt.
     *
     * @param consentId  Consent receipt ID.
     * @param userId     User ID for whom to update authorization.
     * @param authStatus Authorization status (APPROVED, REJECTED, or REVOKED).
     * @throws ConsentManagementException if update fails.
     */
    @Override
    public void authorizeConsent(String consentId, String userId, String authStatus)
            throws ConsentManagementException {

        ReceiptDAO receiptDAO = getReceiptsDAO(receiptDAOs);
        long now = System.currentTimeMillis();

        ConsentAuthorization existing = receiptDAO.getConsentAuthorizationByUser(consentId, userId);
        if (existing != null) {
            receiptDAO.updateConsentAuthorization(consentId, userId, authStatus, now);
        } else {
            receiptDAO.insertConsentAuthorization(new ConsentAuthorization(consentId, userId, authStatus, now));
        }

        String currentState = receiptDAO.getReceiptState(consentId);
        if (PENDING_STATE.equals(currentState)) {
            if (REJECTED_STATE.equals(authStatus)) {
                receiptDAO.updateReceiptState(consentId, REJECTED_STATE);
            } else if (APPROVED_STATE.equals(authStatus)) {
                List<ConsentAuthorization> all = receiptDAO.getConsentAuthorizations(consentId);
                boolean allApproved = all.stream().allMatch(a -> APPROVED_STATE.equals(a.getStatus()));
                if (allApproved) {
                    receiptDAO.updateReceiptState(consentId, ACTIVE_STATE);
                }
            }
        } else if (!REVOKE_STATE.equals(currentState) && REVOKE_STATE.equals(authStatus)) {
            receiptDAO.updateReceiptState(consentId, REVOKE_STATE);
        }
    }

    /**
     * Retrieves all authorization records for a consent receipt.
     *
     * @param consentId Consent receipt ID.
     * @return List of authorization records for the consent.
     * @throws ConsentManagementException if retrieval fails.
     */
    @Override
    public List<ConsentAuthorization> getConsentAuthorizations(String consentId)
            throws ConsentManagementException {

        return getReceiptsDAO(receiptDAOs).getConsentAuthorizations(consentId);
    }

    /**
     * Validates and updates consent status, checking for expiration.
     * If consent is ACTIVE and validityTime has passed, updates status to EXPIRED and returns it.
     *
     * @param consentId Consent receipt ID.
     * @return Current status of the consent (PENDING, ACTIVE, REJECTED, REVOKED, or EXPIRED).
     * @throws ConsentManagementException if validation fails.
     */
    @Override
    public String validateConsentStatus(String consentId) throws ConsentManagementException {

        ReceiptDAO vReceiptDAO = getReceiptsDAO(receiptDAOs);
        String state = vReceiptDAO.getReceiptState(consentId);

        if (state == null) {
            if (log.isDebugEnabled()) {
                log.debug("No receipt found with the Id: " + consentId);
            }
            String message = String.format(ERROR_CODE_RECEIPT_ID_INVALID.getMessage(), consentId) + " in tenant: " +
                    ConsentUtils.getTenantDomainFromCarbonContext();
            throw new ConsentManagementClientException(message, ERROR_CODE_RECEIPT_ID_INVALID.getCode());
        }

        if (ACTIVE_STATE.equals(state)) {
            Long validityTime = vReceiptDAO.getReceiptValidityTime(consentId);
            if (validityTime != null && validityTime < System.currentTimeMillis()) {
                vReceiptDAO.updateReceiptState(consentId, EXPIRED_STATE);
                return EXPIRED_STATE;
            }
        }
        return state;
    }

    private Purpose getPurposeFromName(String name, String group, String groupType) throws ConsentManagementException {

        return getPurposeDAO(purposeDAOs).getPurposeByName(name, group, groupType, getTenantIdFromCarbonContext());
    }

    /**
     * This API is used to select the PIIController from List of registered PIIController. By default set the highest
     * priority one.
     *
     * @param piiControllers list of PIIControllers.
     * @return selected PIIController.
     */
    private PIIController getPIIController(List<PIIController> piiControllers)
            throws ConsentManagementServerException {

        if (isNotEmpty(piiControllers)) {
            return piiControllers.get(piiControllers.size() - 1);
        } else {
            throw handleServerException(ERROR_CODE_GET_DAO, PII_CONTROLLER);
        }
    }

    private void setPublicKey(Receipt receipt) throws ConsentManagementException {

        String publicKey = getPublicKey(receipt.getTenantDomain());
        receipt.setPublicKey(publicKey);
    }

    private PIICategory getPiiCategoryById(int piiCategoryId) throws ConsentManagementException {

        return getPiiCategoryDAO(piiCategoryDAOs).getPIICategoryById(piiCategoryId);
    }

    private PurposeCategory getPurposeCategoryFromName(String name) throws ConsentManagementException {

        return getPurposeCategoryDAO(purposeCategoryDAOs).getPurposeCategoryByName
                (name, getTenantIdFromCarbonContext());
    }

    private PurposeCategory getPurposeCategoryById(int purposeCategoryId) throws ConsentManagementException {

        return getPurposeCategoryDAO(purposeCategoryDAOs).getPurposeCategoryById(purposeCategoryId);
    }

    private PIICategory getPiiCategoryFromName(String name) throws ConsentManagementException {

        return getPiiCategoryDAO(piiCategoryDAOs).getPIICategoryByName(name,
                getTenantIdFromCarbonContext());
    }

    /**
     * This API is used to select the ReceiptDAO from List of registered ReceiptDAO. By default set the highest
     * priority one.
     *
     * @param receiptDAOs list of ReceiptDAOs.
     * @return selected ReceiptDAO.
     */
    private ReceiptDAO getReceiptsDAO(List<ReceiptDAO> receiptDAOs) throws ConsentManagementServerException {

        if (isNotEmpty(receiptDAOs)) {
            return receiptDAOs.get(receiptDAOs.size() - 1);
        } else {
            throw handleServerException(ERROR_CODE_GET_DAO, RECEIPT_DAO);
        }
    }

    private Purpose getPurposeById(int purposeId) throws ConsentManagementException {

        return getPurposeDAO(purposeDAOs).getPurposeById(purposeId);
    }

    /**
     * This API is used to select the PIICategoryDAO from List of registered PIICategoryDAOs. By default set the highest
     * priority one.
     *
     * @param piiCategoryDAOs list of PIICategoryDAOs.
     * @return selected PIICategoryDAO.
     */
    private PIICategoryDAO getPiiCategoryDAO(List<PIICategoryDAO> piiCategoryDAOs)
            throws ConsentManagementServerException {

        if (isNotEmpty(piiCategoryDAOs)) {
            return piiCategoryDAOs.get(piiCategoryDAOs.size() - 1);
        } else {
            throw handleServerException(ERROR_CODE_GET_DAO, PII_CATEGORY_DAO);
        }
    }

    /**
     * This API is used to select the PurposeCategoryDAO from List of registered PurposeCategoryDAOs. By default set the
     * highest priority one.
     *
     * @param purposeCategoryDAOs list of PurposeCategoryDAOs.
     * @return selected PurposeCategoryDAO.
     */
    private PurposeCategoryDAO getPurposeCategoryDAO(List<PurposeCategoryDAO> purposeCategoryDAOs)
            throws ConsentManagementServerException {

        if (isNotEmpty(purposeCategoryDAOs)) {
            return purposeCategoryDAOs.get(purposeCategoryDAOs.size() - 1);
        } else {
            throw handleServerException(ERROR_CODE_GET_DAO, PURPOSE_CATEGORY_DAO);
        }
    }

    /**
     * This API is used to select the PurposeDAO from List of registered PurposeDAOs. By default set the
     * highest priority one.
     *
     * @param purposeDAOs list of PurposeDAOs.
     * @return selected PurposeDAO.
     */
    private PurposeDAO getPurposeDAO(List<PurposeDAO> purposeDAOs) throws ConsentManagementServerException {

        if (isNotEmpty(purposeDAOs)) {
            return purposeDAOs.get(purposeDAOs.size() - 1);
        } else {
            throw handleServerException(ERROR_CODE_GET_DAO, PURPOSE_DAO);
        }
    }

    /**
     * This API is used to set the API version is being used.
     *
     * @param receiptInput ReceiptInput.
     */
    protected void setAPIVersion(ReceiptInput receiptInput) {

        receiptInput.setVersion(API_VERSION);
    }

    /**
     * This API is used to generate a unique consent receipt Id.
     *
     * @return A unique ID.
     */
    protected String generateConsentReceiptId() {

        String consentId = UUID.randomUUID().toString();
        if (log.isDebugEnabled()) {
            log.debug("Consent receipt Id generated: " + consentId);
        }
        return consentId;
    }

    private void setPIIControllerInfo(Receipt receipt) {

        JSONObject controller = new JSONObject(receipt.getPiiController());
        Address piiAddress = getAddress(controller);
        PiiController piiController = getPiiController(controller, piiAddress);
        List<PiiController> piiControllers = Arrays.asList(piiController);
        receipt.setPiiControllers(piiControllers);
    }

    private PiiController getPiiController(JSONObject controller, Address piiAddress) {

        String piiControllerName = controller.optString(PII_CONTROLLER_NAME);
        boolean piiControllerOnBehalf = controller.getBoolean(ON_BEHALF);
        String piiControllerContact = controller.optString(CONTACT);
        String piiControllerEmail = controller.optString(EMAIL);
        String piiControllerPhone = controller.optString(PHONE);
        String piiControllerURL = controller.optString(PII_CONTROLLER_URL);
        return new PiiController(piiControllerName, piiControllerOnBehalf,
                piiControllerContact, piiControllerEmail, piiControllerPhone, piiControllerURL, piiAddress);
    }

    private Address getAddress(JSONObject controller) {

        JSONObject address = controller.optJSONObject(ADDRESS);
        String addressCountry = address.optString(ADDRESS_COUNTRY);
        String addressLocality = address.optString(ADDRESS_LOCALITY);
        String addressRegion = address.optString(ADDRESS_REGION);
        String addressPostOfficeBoxNumber = address.optString(POST_OFFICE_BOX_NUMBER);
        String addressPostCode = address.optString(POSTAL_CODE);
        String addressStreetAddress = address.optString(STREET_ADDRESS);

        return new Address(addressCountry, addressLocality, addressRegion, addressPostOfficeBoxNumber,
                addressPostCode, addressStreetAddress);
    }

    private void setPIIControllerInfo(ReceiptInput receipt) throws ConsentManagementException {

        PiiController controllerInfo = getPIIController(piiControllers).getControllerInfo(receipt.getTenantDomain());
        JSONObject controller = new JSONObject();

        controller.put(PII_CONTROLLER_NAME, controllerInfo.getPiiController());
        controller.put(ON_BEHALF, controllerInfo.isOnBehalf());
        controller.put(CONTACT, controllerInfo.getContact());
        controller.put(EMAIL, controllerInfo.getEmail());
        controller.put(PHONE, controllerInfo.getPhone());
        controller.put(PII_CONTROLLER_URL, controllerInfo.getPiiControllerUrl());

        Address piiAddress = controllerInfo.getAddress();
        if (piiAddress != null) {
            JSONObject address = new JSONObject();
            address.put(ADDRESS_COUNTRY, piiAddress.getAddressCountry());
            address.put(ADDRESS_LOCALITY, piiAddress.getAddressLocality());
            address.put(ADDRESS_REGION, piiAddress.getAddressRegion());
            address.put(POST_OFFICE_BOX_NUMBER, piiAddress.getPostOfficeBoxNumber());
            address.put(POSTAL_CODE, piiAddress.getPostalCode());
            address.put(STREET_ADDRESS, piiAddress.getStreetAddress());
            controller.put(ADDRESS, address);
        }
        receipt.setPiiControllerInfo(controller.toString());
    }

    private String getPublicKey(String tenantDomain) throws ConsentManagementException {

        PublicKey publicKey;
        int tenantId = ConsentUtils.getTenantId(realmService, tenantDomain);
        try {
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
            if (isNotSuperTenant(tenantDomain)) {
                String fileName = getKeystoreName(tenantDomain);
                publicKey = getPublicKey(tenantDomain, keyStoreManager, fileName);
            } else {
                publicKey = keyStoreManager.getDefaultPublicKey();
            }

            byte[] data = publicKey.getEncoded();
            return Base64.encode(data);
        } catch (Exception e) {
            throw handleServerException(ERROR_CODE_GETTING_PUBLIC_CERT, tenantDomain);
        }
    }

    private PublicKey getPublicKey(String tenantDomain, KeyStoreManager keyStoreManager, String keystoreName) throws
            Exception {

        return keyStoreManager.getKeyStore(keystoreName).getCertificate(tenantDomain).getPublicKey();
    }

    private String getKeystoreName(String tenantDomain) {

        return KeystoreUtils.getKeyStoreFileLocation(tenantDomain);
    }

    private boolean isNotSuperTenant(String tenantDomain) {

        return !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain);
    }

    private void validateInputParameters(ReceiptInput receiptInput) throws ConsentManagementException {

        //Set authenticated user.
        if (isBlank(receiptInput.getPiiPrincipalId())) {
            receiptInput.setPiiPrincipalId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername());
        }
        // Set authenticated user's tenant id if it is not set.
        if (isBlank(receiptInput.getTenantDomain())) {
            receiptInput.setTenantId(getTenantIdFromCarbonContext());
            receiptInput.setTenantDomain(getTenantDomainFromCarbonContext());
        } else {
            receiptInput.setTenantId(getTenantId(realmService, receiptInput.getTenantDomain()));
        }
        validateRequiredParametersInConsent(receiptInput);
        receiptInput.getServices().forEach(rethrowConsumer(receiptServiceInput -> {
            validateRequiredParametersInService(receiptServiceInput);
            receiptServiceInput.getPurposes().forEach(rethrowConsumer(receiptPurposeInput ->
                    validateRequiredParametersInPurpose(receiptServiceInput, receiptPurposeInput)));
        }));

        if (log.isDebugEnabled()) {
            log.debug("Consent adding request validation success");
        }
    }

    private void validateRequiredParametersInConsent(ReceiptInput receiptInput) throws ConsentManagementClientException {

        if (isBlank(receiptInput.getPiiPrincipalId())) {
            throw handleClientException(ERROR_CODE_PII_PRINCIPAL_ID_REQUIRED, null);
        }

        if (isBlank(receiptInput.getCollectionMethod())) {
            throw handleClientException(ERROR_CODE_PII_COLLECTION_METHOD_REQUIRED, null);
        }

        if (isEmpty(receiptInput.getServices())) {
            throw handleClientException(ERROR_CODE_AT_LEAST_ONE_SERVICE_REQUIRED, null);
        }
    }

    private void validateRequiredParametersInService(ReceiptServiceInput receiptServiceInput)
            throws ConsentManagementException {

        if (isBlank(receiptServiceInput.getService())) {
            throw handleClientException(ERROR_CODE_SERVICE_NAME_REQUIRED, null);
        }

        if (isEmpty(receiptServiceInput.getPurposes())) {
            throw handleClientException(ERROR_CODE_AT_LEAST_ONE_PURPOSE_REQUIRED, null);
        }

        // Set authenticated user's tenant id if it is not set.
        if (isBlank(receiptServiceInput.getTenantDomain())) {
            receiptServiceInput.setTenantId(getTenantIdFromCarbonContext());
            receiptServiceInput.setTenantDomain(getTenantDomainFromCarbonContext());
        } else {
            receiptServiceInput.setTenantId(getTenantId(realmService, receiptServiceInput.getTenantDomain()));
        }
    }

    private void validateRequiredParametersInPurpose(ReceiptServiceInput receiptServiceInput,
                                                     ReceiptPurposeInput receiptPurposeInput)
            throws ConsentManagementException {

        String serviceName = receiptServiceInput.getService();
        if (receiptPurposeInput.getPurposeId() == null) {
            throw handleClientException(ERROR_CODE_PURPOSE_ID_MANDATORY, serviceName);
        } else {
            // To verify whether the purpose exist in the system. This method will throw an exception if not exist.
            Purpose purpose = getPurpose(receiptPurposeInput.getPurposeId());
            receiptPurposeInput.setPurposeName(purpose.getName());
        }

        if (isBlank(receiptPurposeInput.getConsentType())) {
            throw handleClientException(ERROR_CODE_CONSENT_TYPE_MANDATORY, serviceName);
        }

        if (isEmpty(receiptPurposeInput.getPurposeCategoryId())) {
            throw handleClientException(ERROR_CODE_AT_LEAST_ONE_CATEGORY_ID_REQUIRED, serviceName);
        } else {
            // To verify whether the purposeCategory exist in the system.
            // This method will throw an exception if not exist.
            receiptPurposeInput.getPurposeCategoryId().forEach(rethrowConsumer(this::getPurposeCategory));
        }

        if (isEmpty(receiptPurposeInput.getPiiCategory())) {
            throw handleClientException(ERROR_CODE_AT_LEAST_ONE_PII_CATEGORY_ID_REQUIRED, serviceName);
        } else {
            // To verify whether the piiCategory exist in the system.
            // This method will throw an exception if not exist.
            receiptPurposeInput.getPiiCategory().forEach(rethrowConsumer(piiCategoryValidity -> getPIICategory
                    (piiCategoryValidity.getId())));
        }

        if (receiptPurposeInput.isPrimaryPurpose() == null) {
            throw handleClientException(ERROR_CODE_IS_PRIMARY_PURPOSE_IS_REQUIRED, serviceName);
        }

        if (isBlank(receiptPurposeInput.getTermination())) {
            throw handleClientException(ERROR_CODE_TERMINATION_IS_REQUIRED, serviceName);
        }

        if (receiptPurposeInput.isThirdPartyDisclosure() == null) {
            throw handleClientException(ERROR_CODE_THIRD_PARTY_DISCLOSURE_IS_REQUIRED, serviceName);
        }
    }

    private int getDefaultLimitFromConfig() {

        int limit = DEFAULT_SEARCH_LIMIT;

        if (configParser.getConfiguration().get(PURPOSE_SEARCH_LIMIT_PATH) != null) {
            limit = Integer.parseInt(configParser.getConfiguration()
                    .get(PURPOSE_SEARCH_LIMIT_PATH).toString());
        }
        return limit;
    }

    private void validatePaginationParameters(int limit, int offset) throws ConsentManagementClientException {

        if (limit < 0 || offset < 0) {
            throw handleClientException(ERROR_CODE_INVALID_ARGUMENTS_FOR_LIM_OFFSET, null);
        }
    }

    private void validateInputParameters(PurposeCategory purposeCategory) throws ConsentManagementException {

        if (isBlank(purposeCategory.getName())) {
            if (log.isDebugEnabled()) {
                log.debug("Purpose Category name cannot be empty");
            }
            throw handleClientException(ERROR_CODE_PURPOSE_CATEGORY_NAME_REQUIRED, null);
        }

        if (isPurposeCategoryExists(purposeCategory.getName())) {
            if (log.isDebugEnabled()) {
                log.debug("A purpose category already exists with name: " + purposeCategory.getName());
            }
            throw handleClientException(ERROR_CODE_PURPOSE_CATEGORY_ALREADY_EXIST, purposeCategory.getName());
        }

        // Set authenticated user's tenant id if it is not set.
        if (isBlank(purposeCategory.getTenantDomain())) {
            purposeCategory.setTenantId(getTenantIdFromCarbonContext());
            purposeCategory.setTenantDomain(getTenantDomainFromCarbonContext());
        } else {
            purposeCategory.setTenantId(getTenantId(realmService, purposeCategory.getTenantDomain()));
        }

        if (log.isDebugEnabled()) {
            log.debug("PurposeCategory request validation success: " + purposeCategory.getName());
        }
    }

    private void validateInputParameters(Purpose purpose) throws ConsentManagementException {

        if (isBlank(purpose.getName())) {
            if (log.isDebugEnabled()) {
                log.debug("Purpose name cannot be empty");
            }
            throw handleClientException(ERROR_CODE_PURPOSE_NAME_REQUIRED, null);
        }

        if (isPurposeExists(purpose.getName(), purpose.getGroup(), purpose.getGroupType())) {
            if (log.isDebugEnabled()) {
                log.debug("A purpose already exists with name: " + purpose.getName());
            }
            throw handleClientException(ERROR_CODE_PURPOSE_ALREADY_EXIST, purpose.getName());
        }

        if (isBlank(purpose.getGroup())) {
            if (log.isDebugEnabled()) {
                log.debug("Purpose group is empty for: " + purpose.getName());
            }
            throw handleClientException(ERROR_CODE_PURPOSE_GROUP_REQUIRED, null);
        }

        if (isBlank(purpose.getGroupType())) {
            if (log.isDebugEnabled()) {
                log.debug("Purpose group type is empty for: " + purpose.getName());
            }
            throw handleClientException(ERROR_CODE_PURPOSE_GROUP_TYPE_REQUIRED, null);
        }

        // Set authenticated user's tenant id if it is not set.
        if (isBlank(purpose.getTenantDomain())) {
            purpose.setTenantId(getTenantIdFromCarbonContext());
            purpose.setTenantDomain(getTenantDomainFromCarbonContext());
        } else {
            purpose.setTenantId(getTenantId(realmService, purpose.getTenantDomain()));
        }

        if (isNotEmpty(purpose.getPurposePIICategories())) {
            purpose.getPurposePIICategories().forEach(rethrowConsumer(purposePIICategory -> {
                int id = purposePIICategory.getId();
                if (getPiiCategoryById(id) == null) {
                    throw handleClientException(ERROR_CODE_PII_CATEGORY_ID_INVALID, String.valueOf(id));
                }
                if (purposePIICategory.getMandatory() == null) {
                    throw handleClientException(ERROR_CODE_PURPOSE_PII_CONSTRAINT_REQUIRED, String.valueOf(id));
                }
            }));
        }

        if (log.isDebugEnabled()) {
            log.debug("Purpose request validation success: " + purpose.getName());
        }
    }

    private void validateInputParameters(PIICategory piiCategory) throws ConsentManagementException {

        if (isBlank(piiCategory.getName())) {
            if (log.isDebugEnabled()) {
                log.debug("PII Category name cannot be empty");
            }
            throw handleClientException(ERROR_CODE_PII_CATEGORY_NAME_REQUIRED, null);
        }

        if (isPIICategoryExists(piiCategory.getName())) {
            if (log.isDebugEnabled()) {
                log.debug("A PII Category already exists with name: " + piiCategory.getName());
            }
            throw handleClientException(ERROR_CODE_PII_CATEGORY_ALREADY_EXIST, piiCategory.getName());
        }

        if (piiCategory.getSensitive() == null) {
            piiCategory.setSensitive(false);
        }

        // Set authenticated user's tenant id if it is not set.
        if (isBlank(piiCategory.getTenantDomain())) {
            piiCategory.setTenantId(getTenantIdFromCarbonContext());
            piiCategory.setTenantDomain(getTenantDomainFromCarbonContext());
        } else {
            piiCategory.setTenantId(getTenantId(realmService, piiCategory.getTenantDomain()));
        }

        if (log.isDebugEnabled()) {
            log.debug("PII category request validation success: " + piiCategory.getName());
        }
    }

    private void populateTenantDomain(Receipt receipt) throws ConsentManagementServerException {

        receipt.setTenantDomain(ConsentUtils.getTenantDomain(realmService, receipt.getTenantId()));
        receipt.getServices().forEach(rethrowConsumer(receiptService -> receiptService.setTenantDomain(ConsentUtils
                .getTenantDomain(realmService, receiptService.getTenantId()))));
    }

    private Purpose populatePiiCategories(Purpose purposeResponse) {

        List<PurposePIICategory> purposePIICategories = new ArrayList<>();
        purposeResponse.getPurposePIICategories().forEach(rethrowConsumer(
                piiCategory -> purposePIICategories.add(getPurposePIICategory(piiCategory))));
        purposeResponse.setPurposePIICategories(purposePIICategories);

        if (log.isDebugEnabled()) {
            log.debug("Purpose created successfully with the name: " + purposeResponse.getName());
        }

        return purposeResponse;
    }

    private PurposeVersion populatePiiCategories(PurposeVersion purposeVersionResponse) {

        List<PurposePIICategory> purposePIICategories = new ArrayList<>();
        purposeVersionResponse.getPurposePIICategories().forEach(rethrowConsumer(
                piiCategory -> purposePIICategories.add(getPurposePIICategory(piiCategory))));
        purposeVersionResponse.setPurposePIICategories(purposePIICategories);
        return purposeVersionResponse;
    }

    private PurposePIICategory getPurposePIICategory(PurposePIICategory purposePIICategory) throws ConsentManagementException {

        PIICategory piiCategory = getPiiCategoryById(purposePIICategory.getId());

        PurposePIICategory purposePIICategoryResult = new PurposePIICategory(piiCategory,
                purposePIICategory.getMandatory());
        return purposePIICategoryResult;
    }

    private boolean isUserNameCaseSensitive(String username) throws ConsentManagementException {

        try {
            UserStoreManager userStoreManager = null;
            UserRealm userRealm = getUserRealm();
            if (realmService != null && userRealm != null) {
                AbstractUserStoreManager abstractUserStoreManager = (AbstractUserStoreManager) userRealm
                        .getUserStoreManager();
                if (abstractUserStoreManager != null) {
                    userStoreManager = abstractUserStoreManager
                            .getSecondaryUserStoreManager(UserCoreUtil.extractDomainFromName(username));
                }
                if (userStoreManager != null && userStoreManager.getRealmConfiguration() != null) {
                    String useCaseSensitiveUsernameForCacheKeysProperty = userStoreManager.getRealmConfiguration()
                            .getUserStoreProperty(USE_CASE_SENSITIVE_USERNAME_FOR_CACHE_KEYS);
                    if (useCaseSensitiveUsernameForCacheKeysProperty != null) {
                        isCaseSensitiveUserName = Boolean.parseBoolean(useCaseSensitiveUsernameForCacheKeysProperty);
                        return isCaseSensitiveUserName;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("User store property: " + USE_CASE_SENSITIVE_USERNAME_FOR_CACHE_KEYS
                                    + " not configured. Considering username as case sensitive.");
                        }
                        return false;
                    }
                }
            }
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while reading user store property: " + USE_CASE_SENSITIVE_USERNAME_FOR_CACHE_KEYS
                        + ". Considering username as case sensitive.");
            }
            throw handleServerException(ERROR_CODE_GETTING_USER_STORE_MANAGER, username, e);
        }
        return false;
    }

    private String getLowerCaseUserName(String userName) {

        String domainFreeName;
        String domainName;
        if (userName != null) {
            domainFreeName = UserCoreUtil.removeDomainFromName(userName);
            domainName = UserCoreUtil.extractDomainFromName(userName);
            return UserCoreUtil.addDomainToName(domainFreeName.toLowerCase(), domainName);
        } else {
            return null;
        }
    }

    /**
     * Get the user realm of the logged in user.
     *
     * @return the userRealm for given username
     * @throws ConsentManagementException
     */
    private UserRealm getUserRealm() throws ConsentManagementException {

        UserRealm userRealm;
        String tenantDomain = getTenantDomainFromCarbonContext();
        int tenantId;
        try {
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            userRealm = realmService.getTenantUserRealm(tenantId);
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_GETTING_TENANT_ID, tenantDomain, e);
        }
        return userRealm;
    }
}
