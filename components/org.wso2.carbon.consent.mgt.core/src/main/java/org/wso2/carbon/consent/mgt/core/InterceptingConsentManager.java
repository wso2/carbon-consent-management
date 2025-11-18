/*
 *
 *   Copyright (c) 2018-2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.connector.ConsentMgtInterceptor;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.ConsentManagerConfigurationHolder;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.wso2.carbon.CarbonConstants.UI_PERMISSION_ACTION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_NO_USER_FOUND;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_RECEIPT_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_UNEXPECTED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_USER_NOT_AUTHORIZED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.GET_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.LIST_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PERMISSION_CONSENT_MGT_DELETE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PERMISSION_CONSENT_MGT_LIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PERMISSION_CONSENT_MGT_VIEW;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.REVOKE_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.util.ConsentUtils.handleClientException;
import static org.wso2.carbon.consent.mgt.core.util.ConsentUtils.handleServerException;

/**
 * Consent Manager intercepting layer enforcing authorization.
 */
public class InterceptingConsentManager extends PrivilegedConsentManagerImpl {

    private static final Log log = LogFactory.getLog(InterceptingConsentManager.class);
    private RealmService realmService;

    public InterceptingConsentManager(ConsentManagerConfigurationHolder configHolder,
                                      List<ConsentMgtInterceptor> consentMgtInterceptors) {

        super(configHolder, consentMgtInterceptors);
        this.realmService = configHolder.getRealmService();
    }

    public Receipt getReceipt(String receiptId) throws ConsentManagementException {

        validateAuthorizationForGetOrRevokeReceipts(receiptId, GET_RECEIPT);
        return super.getReceipt(receiptId);
    }

    public List<ReceiptListResponse> searchReceipts(int limit, int offset, String piiPrincipalId, String spTenantDomain,
                                                    String service, String state) throws ConsentManagementException {

        validateAuthorizationForListReceipts(piiPrincipalId);
        return super.searchReceipts(limit, offset, piiPrincipalId, spTenantDomain, service, state);
    }

    public void revokeReceipt(String receiptId) throws ConsentManagementException {

        validateAuthorizationForGetOrRevokeReceipts(receiptId, REVOKE_RECEIPT);
        super.revokeReceipt(receiptId);
    }

    /**
     * Delete PII Category after validating the tenant domain of the PII Category.
     *
     * @param piiCategoryId PII Category ID.
     * @throws ConsentManagementException Consent Management Exception if the tenant domain of the PII Category is
     * different from the accessing tenant domain.
     */
    @Override
    public void deletePIICategory(int piiCategoryId) throws ConsentManagementException {

        PIICategory piiCategory = super.getPIICategory(piiCategoryId);

        if (isCrossTenantOperation(ConsentUtils.getTenantDomainFromCarbonContext(),
                IdentityTenantUtil.getTenantDomain(piiCategory.getTenantId()))) {
            String message = String.format(ERROR_CODE_PII_CATEGORY_ID_INVALID.getMessage(), piiCategoryId) +
                    " in tenant: " + ConsentUtils.getTenantDomainFromCarbonContext();
            throw new ConsentManagementClientException(message, ERROR_CODE_PII_CATEGORY_ID_INVALID.getCode());
        }

        super.deletePIICategory(piiCategoryId);
    }

    /**
     * Get PII Category after validating the tenant domain of the PII Category.
     *
     * @param piiCategoryId PII Category ID.
     * @return PIICategory PII Category.
     * @throws ConsentManagementException Consent Management Exception if the tenant domain of the PII Category is
     * different from the accessing tenant domain.
     */
    @Override
    public PIICategory getPIICategory(int piiCategoryId) throws ConsentManagementException {

        PIICategory piiCategory = super.getPIICategory(piiCategoryId);

        if (isCrossTenantOperation(ConsentUtils.getTenantDomainFromCarbonContext(),
                IdentityTenantUtil.getTenantDomain(piiCategory.getTenantId()))) {
            String message = String.format(ERROR_CODE_PII_CATEGORY_ID_INVALID.getMessage(), piiCategoryId) +
                    " in tenant: " + ConsentUtils.getTenantDomainFromCarbonContext();
            throw new ConsentManagementClientException(message, ERROR_CODE_PII_CATEGORY_ID_INVALID.getCode());
        }

        return piiCategory;
    }

    /**
     * Delete Purpose Category after validating the tenant domain of the Purpose Category.
     *
     * @param purposeCategoryId Purpose Category ID.
     * @throws ConsentManagementException Consent Management Exception if the tenant domain of the Purpose Category
     * is different from the accessing tenant domain.
     */
    @Override
    public void deletePurposeCategory(int purposeCategoryId) throws ConsentManagementException {

        PurposeCategory purposeCategory = super.getPurposeCategory(purposeCategoryId);

        if (isCrossTenantOperation(ConsentUtils.getTenantDomainFromCarbonContext(),
                IdentityTenantUtil.getTenantDomain(purposeCategory.getTenantId()))) {
            String message = String.format(ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID.getMessage(), purposeCategoryId) +
                    " in tenant: " + ConsentUtils.getTenantDomainFromCarbonContext();
            throw new ConsentManagementClientException(message, ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID.getCode());
        }

        super.deletePurposeCategory(purposeCategoryId);
    }

    /**
     * Get Purpose Category after validating the tenant domain of the Purpose Category.
     *
     * @param purposeCategoryId Purpose Category ID.
     * @return PurposeCategory Purpose Category.
     * @throws ConsentManagementException Consent Management Exception if the tenant domain of the Purpose Category
     * is different from the accessing tenant domain.
     */
    @Override
    public PurposeCategory getPurposeCategory(int purposeCategoryId) throws ConsentManagementException {

        PurposeCategory purposeCategory = super.getPurposeCategory(purposeCategoryId);

        if (isCrossTenantOperation(ConsentUtils.getTenantDomainFromCarbonContext(),
                IdentityTenantUtil.getTenantDomain(purposeCategory.getTenantId()))) {
            String message = String.format(ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID.getMessage(), purposeCategoryId) +
                    " in tenant: " + ConsentUtils.getTenantDomainFromCarbonContext();
            throw new ConsentManagementClientException(message, ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID.getCode());
        }

        return purposeCategory;
    }

    /**
     * Delete Purpose after validating the tenant domain of the Purpose.
     *
     * @param purposeId Purpose ID.
     * @throws ConsentManagementException Consent Management Exception if the tenant domain of the Purpose is
     * different from the accessing tenant domain.
     */
    @Override
    public void deletePurpose(int purposeId) throws ConsentManagementException {

        Purpose purpose = super.getPurpose(purposeId);

        if (isCrossTenantOperation(ConsentUtils.getTenantDomainFromCarbonContext(),
                IdentityTenantUtil.getTenantDomain(purpose.getTenantId()))) {
            String message = String.format(ERROR_CODE_PURPOSE_ID_INVALID.getMessage(), purposeId) + " in tenant: " +
                    ConsentUtils.getTenantDomainFromCarbonContext();
            throw new ConsentManagementClientException(message, ERROR_CODE_PURPOSE_ID_INVALID.getCode());
        }

        super.deletePurpose(purposeId);
    }

    /**
     * Get Purpose after validating the tenant domain of the Purpose.
     *
     * @param purposeId Purpose ID.
     * @return Purpose Purpose.
     * @throws ConsentManagementException Consent Management Exception if the tenant domain of the Purpose is
     * different from the accessing tenant domain.
     */
    @Override
    public Purpose getPurpose(int purposeId) throws ConsentManagementException {

        Purpose purpose = super.getPurpose(purposeId);

        if (isCrossTenantOperation(ConsentUtils.getTenantDomainFromCarbonContext(),
                IdentityTenantUtil.getTenantDomain(purpose.getTenantId()))) {
            String message = String.format(ERROR_CODE_PURPOSE_ID_INVALID.getMessage(), purposeId) + " in tenant: " +
                    ConsentUtils.getTenantDomainFromCarbonContext();
            throw new ConsentManagementClientException(message, ERROR_CODE_PURPOSE_ID_INVALID.getCode());
        }

        return purpose;
    }

    /**
     * Validate whether the accessing tenant domain is same as the resource tenant domain.
     *
     * @param accessingTenantDomain - Accessing tenant domain.
     * @param resourceTenantDomain  - Resource tenant domain.
     * @return - true if both tenant domains are same, else false.
     */
    private boolean isCrossTenantOperation(String accessingTenantDomain, String resourceTenantDomain) {

        return !StringUtils.equals(accessingTenantDomain, resourceTenantDomain);
    }

    private void validateAuthorizationForListReceipts(String piiPrincipalId) throws ConsentManagementException {

        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (isBlank(loggedInUser)) {
            throw handleClientException(ERROR_CODE_NO_USER_FOUND, LIST_RECEIPT);
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (isNotBlank(piiPrincipalId) && piiPrincipalId.equalsIgnoreCase(loggedInUser)) {
            if (log.isDebugEnabled()) {
                log.debug("User: " + piiPrincipalId + " is authorized to perform a search on own consent receipts.");
            }
            //Returns here since same user is trying to search own receipts.
            return;
        }

        handleAuthorization(LIST_RECEIPT, loggedInUser, tenantId);
    }

    private void validateAuthorizationForGetOrRevokeReceipts(String receiptId, String operation) throws
            ConsentManagementException {

        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (isBlank(loggedInUser)) {
            throw handleClientException(ERROR_CODE_NO_USER_FOUND, operation);
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (super.isReceiptExist(receiptId, loggedInUser, tenantId)) {
            if (log.isDebugEnabled()) {
                log.debug("User: " + loggedInUser + " is authorized to perform a " + operation + " on own " +
                        "consent receipt.");
            }
            //Returns here since same user is trying to get/revoke own receipts.
            return;
        }

        handleAuthorization(operation, loggedInUser, tenantId);
        // If the receipt owners tenant domain is different from the authenticated users tenant domain, it should
        // fail irrespective of permission.
        handleCrossDomainPermission(receiptId);
    }

    private void handleCrossDomainPermission(String receiptId) throws ConsentManagementException {

        String tenantDomain = ConsentUtils.getTenantDomainFromCarbonContext();
        Receipt receipt = super.getReceipt(receiptId);
        if (receipt != null) {
            if (StringUtils.equals(receipt.getTenantDomain(), tenantDomain)) {
                return;
            } else if (receipt.getServices().stream().anyMatch(service -> StringUtils.equals(service.getTenantDomain(),
                    tenantDomain))) {
                return;
            }
        }
        String message = String.format(ERROR_CODE_RECEIPT_ID_INVALID.getMessage(), receiptId) + " in tenant: " +
                tenantDomain;
        throw new ConsentManagementClientException(message, ERROR_CODE_RECEIPT_ID_INVALID.getCode());
    }

    private void handleAuthorization(String operation, String tenantAwareUsername, int tenantId) throws
            ConsentManagementException {

        try {
            boolean authorized = false;
            AuthorizationManager authorizationManager = realmService.getTenantUserRealm(tenantId)
                    .getAuthorizationManager();
            if (GET_RECEIPT.equals(operation)) {
                authorized = authorizationManager.isUserAuthorized(tenantAwareUsername,
                        PERMISSION_CONSENT_MGT_VIEW, UI_PERMISSION_ACTION);
            } else if (LIST_RECEIPT.equals(operation)) {
                authorized = authorizationManager.isUserAuthorized(tenantAwareUsername,
                        PERMISSION_CONSENT_MGT_LIST, UI_PERMISSION_ACTION);
            } else if (REVOKE_RECEIPT.equals(operation)) {
                authorized = authorizationManager.isUserAuthorized(tenantAwareUsername,
                        PERMISSION_CONSENT_MGT_DELETE, UI_PERMISSION_ACTION);
            }

            if (authorized) {
                if (log.isDebugEnabled()) {
                    log.debug("User: " + tenantAwareUsername + " is successfully authorized to perform the " +
                            "operation: " + operation);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("LoggedIn user: " + tenantAwareUsername + " is not authorized to perform operation :" +
                            operation + " of another users");
                }
                throw handleClientException(ERROR_CODE_USER_NOT_AUTHORIZED, tenantAwareUsername);
            }
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_UNEXPECTED, null, e);
        }
    }
}
