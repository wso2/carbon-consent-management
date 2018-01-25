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

import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.internal.ConsentManagerConfiguration;
import org.wso2.carbon.consent.mgt.core.model.AddReceiptResponse;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;

import java.util.List;

/**
 * Consent manager intercepting layer.
 */
public class InterceptingConsentManager implements ConsentManager {

    private ConsentManager consentManager;

    public InterceptingConsentManager(ConsentManagerConfiguration configuration) {

        consentManager = new ConsentManagerImpl(configuration);
    }

    public Purpose addPurpose(Purpose purpose) throws ConsentManagementException {

        return consentManager.addPurpose(purpose);
    }

    public Purpose getPurpose(int purposeId) throws ConsentManagementException {

        return consentManager.getPurpose(purposeId);
    }

    public Purpose getPurposeByName(String name) throws ConsentManagementException {

        return consentManager.getPurposeByName(name);
    }

    public List<Purpose> listPurposes(int limit, int offset) throws ConsentManagementException {

        return consentManager.listPurposes(limit, offset);
    }

    public void deletePurpose(int purposeId) throws ConsentManagementException {

        consentManager.deletePurpose(purposeId);
    }

    public boolean isPurposeExists(String name) throws ConsentManagementException {

        return consentManager.isPurposeExists(name);
    }

    public PurposeCategory addPurposeCategory(PurposeCategory purposeCategory) throws ConsentManagementException {

        return consentManager.addPurposeCategory(purposeCategory);
    }

    public PurposeCategory getPurposeCategory(int purposeCategoryId) throws ConsentManagementException {

        return consentManager.getPurposeCategory(purposeCategoryId);
    }

    public PurposeCategory getPurposeCategoryByName(String name) throws ConsentManagementException {

        return consentManager.getPurposeCategoryByName(name);
    }

    public List<PurposeCategory> listPurposeCategories(int limit, int offset) throws ConsentManagementException {

        return consentManager.listPurposeCategories(limit, offset);
    }

    public void deletePurposeCategory(int purposeCategoryId) throws ConsentManagementException {

        consentManager.deletePurposeCategory(purposeCategoryId);
    }

    public boolean isPurposeCategoryExists(String name) throws ConsentManagementException {

        return getPurposeCategoryByName(name) != null;
    }

    public PIICategory addPIICategory(PIICategory piiCategory) throws ConsentManagementException {

        return consentManager.addPIICategory(piiCategory);
    }

    public PIICategory getPIICategoryByName(String name) throws ConsentManagementException {

        return consentManager.getPIICategoryByName(name);
    }

    public PIICategory getPIICategory(int piiCategoryId) throws ConsentManagementException {

        return consentManager.getPIICategory(piiCategoryId);
    }

    public List<PIICategory> listPIICategories(int limit, int offset) throws ConsentManagementException {

        return consentManager.listPIICategories(limit, offset);
    }

    public void deletePIICategory(int piiCategoryId) throws ConsentManagementException {

        consentManager.deletePIICategory(piiCategoryId);
    }

    public boolean isPIICategoryExists(String name) throws ConsentManagementException {

        return consentManager.isPIICategoryExists(name);
    }

    public AddReceiptResponse addConsent(ReceiptInput receiptInput) throws ConsentManagementException {

        return consentManager.addConsent(receiptInput);
    }

    public Receipt getReceipt(String receiptId) throws ConsentManagementException {

        return consentManager.getReceipt(receiptId);
    }

    public List<ReceiptListResponse> searchReceipts(int limit, int offset, String piiPrincipalId, String spTenantDomain,
                                                    String service, String state) throws ConsentManagementException {

        return consentManager.searchReceipts(limit, offset, piiPrincipalId, spTenantDomain, service, state);
    }

    public void revokeReceipt(String receiptId) throws ConsentManagementException {

        consentManager.revokeReceipt(receiptId);
    }

}
