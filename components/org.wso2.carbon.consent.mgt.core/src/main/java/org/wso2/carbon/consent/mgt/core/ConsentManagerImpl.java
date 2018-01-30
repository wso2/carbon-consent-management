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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.connector.PIIController;
import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.dao.ReceiptDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.model.AddReceiptResponse;
import org.wso2.carbon.consent.mgt.core.model.ConsentManagerConfigurationHolder;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.PiiController;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;
import org.wso2.carbon.consent.mgt.core.util.ConsentConfigParser;
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.API_VERSION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_AT_LEAST_ONE_CATEGORY_ID_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_AT_LEAST_ONE_PII_CATEGORY_ID_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_AT_LEAST_ONE_PURPOSE_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_AT_LEAST_ONE_SERVICE_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_CONSENT_TYPE_MANDATORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_GET_DAO;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_INVALID_ARGUMENTS_FOR_LIM_OFFSET;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_IS_PRIMARY_PURPOSE_IS_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_ALREADY_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_ID_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_NAME_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_COLLECTION_METHOD_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_PRINCIPAL_ID_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_ALREADY_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CATEGORY_ALREADY_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CATEGORY_ID_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CATEGORY_NAME_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_ID_MANDATORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_ID_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_NAME_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_SERVICE_NAME_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_TERMINATION_IS_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_THIRD_PARTY_DISCLOSURE_IS_REQUIRED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PURPOSE_SEARCH_LIMIT_PATH;
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

        Purpose purpose = getPurposeDAO(purposeDAOs).getPurposeById(purposeId);
        if (purpose == null) {
            throw ConsentUtils.handleClientException(ERROR_CODE_PURPOSE_ID_INVALID, String.valueOf(purposeId));
        }
        purpose.getPiiCategoryIds().forEach(rethrowConsumer(id -> purpose.getPiiCategories().add(getPIICategory(id))));
        return purpose;
    }

    /**
     * This API is used to get the purpose by purpose name.
     *
     * @param name Name of the purpose.
     * @return 200 Ok with purpose element.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public Purpose getPurposeByName(String name) throws ConsentManagementException {

        return getPurposeDAO(purposeDAOs).getPurposeByName(name, getTenantIdFromCarbonContext());
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
     * This api is used to delete existing purpose by purpose Id.
     *
     * @param purposeId ID of the purpose.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public void deletePurpose(int purposeId) throws ConsentManagementException {

        if (purposeId == 0 || purposeId < 0) {
            if (log.isDebugEnabled()) {
                log.debug("Purpose Id is not found in the request or invalid purpose Id");
            }
            throw handleClientException(ERROR_CODE_PURPOSE_ID_REQUIRED, null);
        }

        if (getPurpose(purposeId) == null) {
            throw handleClientException(ERROR_CODE_PURPOSE_ID_INVALID, String.valueOf(purposeId));
        }
        int id = getPurposeDAO(purposeDAOs).deletePurpose(purposeId);
        if (log.isDebugEnabled()) {
            log.debug("Purpose deleted successfully. ID: " + id);
        }
    }

    /**
     * This API is used to check whether a purpose exists with given name.
     *
     * @param name Name of the purpose.
     * @return true, if an element is found.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public boolean isPurposeExists(String name) throws ConsentManagementException {

        return getPurposeByName(name) != null;
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
        return getPurposeCategoryDAO(purposeCategoryDAOs).addPurposeCategory(purposeCategory);
    }

    /**
     * This API is used to get purpose category by ID.
     *
     * @param purposeCategoryId Purpose category ID.
     * @return 200 Ok with purpose category element.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public PurposeCategory getPurposeCategory(int purposeCategoryId) throws ConsentManagementException {

        PurposeCategory category = getPurposeCategoryDAO(purposeCategoryDAOs).getPurposeCategoryById(purposeCategoryId);
        if (category == null) {
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

        return getPurposeCategoryDAO(purposeCategoryDAOs).getPurposeCategoryByName(name, getTenantIdFromCarbonContext());
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

        if (purposeCategoryId == 0 || purposeCategoryId < 0) {
            if (log.isDebugEnabled()) {
                log.debug("Purpose Category Id is not found in the request or invalid Id");
            }
            throw handleClientException(ERROR_CODE_PURPOSE_CATEGORY_ID_REQUIRED, null);
        }

        if (getPurposeCategory(purposeCategoryId) == null) {
            throw handleClientException(ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID, String.valueOf(purposeCategoryId));
        }
        int id = getPurposeCategoryDAO(purposeCategoryDAOs).deletePurposeCategory(purposeCategoryId);
        if (log.isDebugEnabled()) {
            log.debug("Purpose category deleted successfully. ID: " + id);
        }
    }

    /**
     * This API is used to check whether a purpose category exists for a given name.
     *
     * @param name Name of the purpose.
     * @return true if a category found.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public boolean isPurposeCategoryExists(String name) throws ConsentManagementException {

        return getPurposeCategoryByName(name) != null;
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
        return getPiiCategoryDAO(piiCategoryDAOs).addPIICategory(piiCategory);
    }

    /**
     * This API is used ot get PII category by name.
     *
     * @param name Name of the PII category.
     * @return 200 OK. Returns PII category with ID.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public PIICategory getPIICategoryByName(String name) throws ConsentManagementException {

        return getPiiCategoryDAO(piiCategoryDAOs).getPIICategoryByName(name, getTenantIdFromCarbonContext());
    }

    /**
     * This API is sued to get PII category by ID.
     *
     * @param piiCategoryId ID of the PII category.
     * @return 200 OK. Returns PII category
     * @throws ConsentManagementException Consent Management Exception.
     */
    public PIICategory getPIICategory(int piiCategoryId) throws ConsentManagementException {

        PIICategory piiCategory = getPiiCategoryDAO(piiCategoryDAOs).getPIICategoryById(piiCategoryId);
        if (piiCategory == null) {
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

        if (piiCategoryId == 0 || piiCategoryId < 0) {
            if (log.isDebugEnabled()) {
                log.debug("PII Category Id is not found in the request or invalid PII category Id");
            }
            throw handleClientException(ERROR_CODE_PII_CATEGORY_ID_REQUIRED, null);
        }

        if (getPIICategory(piiCategoryId) == null) {
            throw handleClientException(ERROR_CODE_PII_CATEGORY_ID_INVALID, String.valueOf(piiCategoryId));
        }
        int id = getPiiCategoryDAO(piiCategoryDAOs).deletePIICategory(piiCategoryId);
        if (log.isDebugEnabled()) {
            log.debug("PII Category deleted successfully. ID: " + id);
        }
    }

    /**
     * This API is sued to check whether a PII category exists for a given name.
     *
     * @param name Name of the PII category.
     * @return true if a category exists.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public boolean isPIICategoryExists(String name) throws ConsentManagementException {

        return getPIICategoryByName(name) != null;
    }

    /**
     * This API is used to verify and store consent input.
     *
     * @param receiptInput consent input.
     * @throws ConsentManagementException Consent Management Exception.
     */
    public AddReceiptResponse addConsent(ReceiptInput receiptInput) throws ConsentManagementException {
        validateInputParameters(receiptInput);
        receiptInput.setConsentReceiptId(generateConsentReceiptId(receiptInput));
        setAPIVersion(receiptInput);
        getReceiptsDAO(receiptDAOs).addReceipt(receiptInput);
        return new AddReceiptResponse(receiptInput.getConsentReceiptId(), receiptInput.getCollectionMethod(),
                receiptInput.getLanguage(), receiptInput.getPiiPrincipalId(), receiptInput.getTenantDomain());
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
        populateTenantDomain(receipt);
        setPIIControllerInfo(receipt);
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

        int spTenantId = 0;
        if (StringUtils.isNotBlank(spTenantDomain)) {
            spTenantId = ConsentUtils.getTenantId(realmService, spTenantDomain);
        }

        validatePaginationParameters(limit, offset);
        if (limit == 0) {
            limit = getDefaultLimitFromConfig();
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defied the request, default to: " + limit);
            }
        }
        List<ReceiptListResponse> receiptListResponses = getReceiptsDAO(receiptDAOs).searchReceipts(limit, offset,
                piiPrincipalId, spTenantId, service, state);
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

        if (CollectionUtils.isNotEmpty(piiControllers)) {
            return piiControllers.get(piiControllers.size() - 1);
        } else {
            throw handleServerException(ERROR_CODE_GET_DAO, PII_CONTROLLER);
        }
    }

    /**
     * This API is used to select the ReceiptDAO from List of registered ReceiptDAO. By default set the highest
     * priority one.
     *
     * @param receiptDAOs list of ReceiptDAOs.
     * @return selected ReceiptDAO.
     */
    private ReceiptDAO getReceiptsDAO(List<ReceiptDAO> receiptDAOs) throws ConsentManagementServerException {

        if (CollectionUtils.isNotEmpty(receiptDAOs)) {
            return receiptDAOs.get(receiptDAOs.size() - 1);
        } else {
            throw handleServerException(ERROR_CODE_GET_DAO, RECEIPT_DAO);
        }
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

        if (CollectionUtils.isNotEmpty(piiCategoryDAOs)) {
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

        if (CollectionUtils.isNotEmpty(purposeCategoryDAOs)) {
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

        if (CollectionUtils.isNotEmpty(purposeDAOs)) {
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
     * @param receiptInput ReceiptInput
     * @return A unique ID.
     */
    protected String generateConsentReceiptId(ReceiptInput receiptInput) {

        return UUID.randomUUID().toString();
    }

    private void setPIIControllerInfo(Receipt receipt) throws ConsentManagementServerException {

        PiiController controllerInfo = getPIIController(piiControllers).getControllerInfo(receipt.getTenantDomain());
        List<PiiController> piiControllers = Arrays.asList(controllerInfo);
        receipt.setPiiControllers(piiControllers);
    }

    private void validateInputParameters(ReceiptInput receiptInput) throws ConsentManagementException {

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
            receiptServiceInput.getPurposes().forEach(rethrowConsumer(receiptPurposeInput -> {
                validateRequiredParametersInPurpose(receiptServiceInput, receiptPurposeInput);
            }));
        }));
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
            getPurpose(receiptPurposeInput.getPurposeId());
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
            receiptPurposeInput.getPiiCategory().forEach(rethrowConsumer(piiCategoryValidity -> {
                getPIICategory(piiCategoryValidity.getId());
            }));
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
    }

    private void validateInputParameters(Purpose purpose) throws ConsentManagementException {

        if (isBlank(purpose.getName())) {
            if (log.isDebugEnabled()) {
                log.debug("Purpose name cannot be empty");
            }
            throw handleClientException(ERROR_CODE_PURPOSE_NAME_REQUIRED, null);
        }

        if (isPurposeExists(purpose.getName())) {
            if (log.isDebugEnabled()) {
                log.debug("A purpose already exists with name: " + purpose.getName());
            }
            throw handleClientException(ERROR_CODE_PURPOSE_ALREADY_EXIST, purpose.getName());
        }

        // Set authenticated user's tenant id if it is not set.
        if (isBlank(purpose.getTenantDomain())) {
            purpose.setTenantId(getTenantIdFromCarbonContext());
            purpose.setTenantDomain(getTenantDomainFromCarbonContext());
        } else {
            purpose.setTenantId(getTenantId(realmService, purpose.getTenantDomain()));
        }

        if (CollectionUtils.isNotEmpty(purpose.getPiiCategoryIds())) {
            purpose.getPiiCategoryIds().forEach(rethrowConsumer(id -> {
                if (getPIICategory(id) == null) {
                    throw handleClientException(ERROR_CODE_PII_CATEGORY_ID_INVALID, String.valueOf(id));
                }
            }));
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
    }

    private void populateTenantDomain(Receipt receipt) throws ConsentManagementServerException {

        receipt.setTenantDomain(ConsentUtils.getTenantDomain(realmService, receipt.getTenantId()));
        receipt.getServices().forEach(rethrowConsumer(receiptService -> receiptService.setTenantDomain(ConsentUtils
                .getTenantDomain(realmService, receiptService.getTenantId()))));
    }

    private Purpose populatePiiCategories(Purpose purposeResponse) {

        List<PIICategory> piiCategories = new ArrayList<>();
        purposeResponse.getPiiCategoryIds().forEach(rethrowConsumer(id -> piiCategories.add(getPIICategory(id))));
        purposeResponse.setPiiCategories(piiCategories);
        return purposeResponse;
    }
}
