/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.consent.mgt.core;

import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.consent.mgt.core.connector.ConsentMgtInterceptor;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.AddReceiptResponse;
import org.wso2.carbon.consent.mgt.core.model.ConsentInterceptorTemplate;
import org.wso2.carbon.consent.mgt.core.model.ConsentManagerConfigurationHolder;
import org.wso2.carbon.consent.mgt.core.model.ConsentMessageContext;
import org.wso2.carbon.consent.mgt.core.model.OperationDelegate;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;

import java.util.List;
import java.util.Map;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.GROUP;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.GROUP_TYPE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_ADD_PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_ADD_PURPOSE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_ADD_PURPOSE_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_ADD_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_DELETE_PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_DELETE_PURPOSE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_DELETE_PURPOSE_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_DELETE_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_GET_PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_GET_PII_CATEGORY_BY_NAME;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_GET_PII_CATEGORY_LIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_GET_PURPOSE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_GET_PURPOSE_BY_NAME;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_GET_PURPOSE_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_GET_PURPOSE_CATEGORY_BY_NAME;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_GET_PURPOSE_CATEGORY_LIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_GET_PURPOSE_LIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_GET_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_IS_PII_CATEGORY_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_IS_PURPOSE_CATEGORY_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_IS_PURPOSE_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_LIST_RECEIPTS;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_REVOKE_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_ADD_PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_ADD_PURPOSE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_ADD_PURPOSE_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_ADD_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_DELETE_PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_DELETE_PURPOSE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_DELETE_PURPOSE_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_DELETE_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_GET_PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_GET_PII_CATEGORY_BY_NAME;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_GET_PII_CATEGORY_LIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_GET_PURPOSE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_GET_PURPOSE_BY_NAME;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_GET_PURPOSE_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_GET_PURPOSE_CATEGORY_BY_NAME;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_GET_PURPOSE_CATEGORY_LIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_GET_PURPOSE_LIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_GET_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_IS_PII_CATEGORY_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_IS_PURPOSE_CATEGORY_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_IS_PURPOSE_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_LIST_RECEIPTS;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_REVOKE_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.LIMIT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.OFFSET;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PII_CATEGORY_ID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PII_CATEGORY_NAME;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PII_PRINCIPAL_ID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PRINCIPAL_TENANT_DOMAIN;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PURPOSE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PURPOSE_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PURPOSE_CATEGORY_ID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PURPOSE_CATEGORY_NAME;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PURPOSE_ID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PURPOSE_NAME;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.RECEIPT_ID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.RECEIPT_INPUT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.SERVICE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.SP_TENANT_DOMAIN;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.STATE;

/**
 * Consent Manager intercepting layer responsible for triggering listeners and calling the actual
 * {@link ConsentManagerImpl} to perform consent management functionality.
 * <p>
 * This intercepting layer is free from any authorization logic, therefore any component consuming the
 * {@link ConsentManager} as an OSGi service need to use this implementation instead of
 * {@link InterceptingConsentManager} implementation. This can be done by listening to implementations registered for
 * {@link PrivilegedConsentManager} interface.
 */
public class PrivilegedConsentManagerImpl implements PrivilegedConsentManager {

    protected ConsentManager consentManager;
    protected List<ConsentMgtInterceptor> consentMgtInterceptors;

    public PrivilegedConsentManagerImpl(ConsentManagerConfigurationHolder configHolder,
                                        List<ConsentMgtInterceptor> consentMgtInterceptors) {

        consentManager = new ConsentManagerImpl(configHolder);
        this.consentMgtInterceptors = consentMgtInterceptors;

    }

    public Purpose addPurpose(Purpose purpose) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<Purpose, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_ADD_PURPOSE, properties -> properties.put(PURPOSE, purpose))
                .executeWith(new OperationDelegate<Purpose>() {
                    @Override
                    public Purpose execute() throws ConsentManagementException {

                        return consentManager.addPurpose(purpose);
                    }
                })
                .intercept(POST_ADD_PURPOSE, properties -> properties.put(PURPOSE, purpose))
                .getResult();
    }

    public Purpose getPurpose(int purposeId) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<Purpose, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_GET_PURPOSE, properties -> properties.put(PURPOSE_ID, purposeId))
                .executeWith(new OperationDelegate<Purpose>() {
                    @Override
                    public Purpose execute() throws ConsentManagementException {

                        return consentManager.getPurpose(purposeId);
                    }
                })
                .intercept(POST_GET_PURPOSE, properties -> properties.put(PURPOSE_ID, purposeId))
                .getResult();
    }

    public Purpose getPurposeByName(String name, String group, String groupType) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<Purpose, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_GET_PURPOSE_BY_NAME, properties -> {
            properties.put(PURPOSE_NAME, name);
            properties.put(GROUP, group);
            properties.put(GROUP_TYPE, groupType);
        })
                .executeWith(new OperationDelegate<Purpose>() {
                    @Override
                    public Purpose execute() throws ConsentManagementException {

                        return consentManager.getPurposeByName(name, group, groupType);
                    }
                })
                .intercept(POST_GET_PURPOSE_BY_NAME, properties -> properties.put(PURPOSE_NAME, name))
                .getResult();
    }

    @Deactivate
    public List<Purpose> listPurposes(int limit, int offset) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<List<Purpose>, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_GET_PURPOSE_LIST, properties -> {
            properties.put(LIMIT, limit);
            properties.put(OFFSET, offset);
        })
                .executeWith(new OperationDelegate<List<Purpose>>() {
                    @Override
                    public List<Purpose> execute() throws ConsentManagementException {

                        return consentManager.listPurposes(limit, offset);
                    }
                })
                .intercept(POST_GET_PURPOSE_LIST, properties -> {
                    properties.put(LIMIT, limit);
                    properties.put(OFFSET, offset);
                })
                .getResult();
    }

    public List<Purpose> listPurposes(String group, String groupType, int limit, int offset) throws
            ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<List<Purpose>, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_GET_PURPOSE_LIST, properties -> {
            properties.put(GROUP, group);
            properties.put(GROUP_TYPE, groupType);
            properties.put(LIMIT, limit);
            properties.put(OFFSET, offset);
        })
                .executeWith(new OperationDelegate<List<Purpose>>() {
                    @Override
                    public List<Purpose> execute() throws ConsentManagementException {

                        return consentManager.listPurposes(group, groupType, limit, offset);
                    }
                })
                .intercept(POST_GET_PURPOSE_LIST, properties -> {
                    properties.put(LIMIT, limit);
                    properties.put(OFFSET, offset);
                })
                .getResult();
    }

    public void deletePurpose(int purposeId) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<Void, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        template.intercept(PRE_DELETE_PURPOSE, properties -> properties.put(PURPOSE_ID, purposeId))
                .executeWith(new OperationDelegate<Void>() {
                    @Override
                    public Void execute() throws ConsentManagementException {

                        consentManager.deletePurpose(purposeId);
                        return null;
                    }
                })
                .intercept(POST_DELETE_PURPOSE, properties -> properties.put(PURPOSE_ID, purposeId));
    }

    public boolean isPurposeExists(String name, String group, String groupType) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<Boolean, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_IS_PURPOSE_EXIST, properties -> {
            properties.put(PURPOSE_NAME, name);
            properties.put(GROUP, group);
            properties.put(GROUP_TYPE, groupType);
        })
                .executeWith(new OperationDelegate<Boolean>() {
                    @Override
                    public Boolean execute() throws ConsentManagementException {

                        return consentManager.isPurposeExists(name, group, groupType);
                    }
                })
                .intercept(POST_IS_PURPOSE_EXIST, properties -> properties.put(PURPOSE_NAME, name))
                .getResult();
    }

    public PurposeCategory addPurposeCategory(PurposeCategory purposeCategory) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<PurposeCategory, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_ADD_PURPOSE_CATEGORY,
                properties -> properties.put(PURPOSE_CATEGORY, purposeCategory))
                .executeWith(new OperationDelegate<PurposeCategory>() {
                    @Override
                    public PurposeCategory execute() throws ConsentManagementException {

                        return consentManager.addPurposeCategory(purposeCategory);
                    }
                })
                .intercept(POST_ADD_PURPOSE_CATEGORY, properties -> properties.put(PURPOSE_CATEGORY, purposeCategory))
                .getResult();
    }

    public PurposeCategory getPurposeCategory(int purposeCategoryId) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<PurposeCategory, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_GET_PURPOSE_CATEGORY, properties -> properties.put(PURPOSE_CATEGORY_ID,
                purposeCategoryId))
                .executeWith(new OperationDelegate<PurposeCategory>() {
                    @Override
                    public PurposeCategory execute() throws ConsentManagementException {

                        return consentManager.getPurposeCategory(purposeCategoryId);
                    }
                })
                .intercept(POST_GET_PURPOSE_CATEGORY,
                        properties -> properties.put(PURPOSE_CATEGORY_ID, purposeCategoryId))
                .getResult();
    }

    public PurposeCategory getPurposeCategoryByName(String name) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<PurposeCategory, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_GET_PURPOSE_CATEGORY_BY_NAME, properties -> properties.put(PURPOSE_CATEGORY_NAME,
                name))
                .executeWith(new OperationDelegate<PurposeCategory>() {
                    @Override
                    public PurposeCategory execute() throws ConsentManagementException {

                        return consentManager.getPurposeCategoryByName(name);
                    }
                })
                .intercept(POST_GET_PURPOSE_CATEGORY_BY_NAME, properties -> properties.put(PURPOSE_CATEGORY_NAME, name))
                .getResult();
    }

    public List<PurposeCategory> listPurposeCategories(int limit, int offset) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<List<PurposeCategory>, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_GET_PURPOSE_CATEGORY_LIST, properties -> {
            properties.put(LIMIT, limit);
            properties.put(OFFSET, offset);
        })
                .executeWith(new OperationDelegate<List<PurposeCategory>>() {
                    @Override
                    public List<PurposeCategory> execute() throws ConsentManagementException {

                        return consentManager.listPurposeCategories(limit, offset);
                    }
                })
                .intercept(POST_GET_PURPOSE_CATEGORY_LIST, properties -> {
                    properties.put(LIMIT, limit);
                    properties.put(OFFSET, offset);
                })
                .getResult();
    }

    public void deletePurposeCategory(int purposeCategoryId) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<Void, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        template.intercept(PRE_DELETE_PURPOSE_CATEGORY,
                properties -> properties.put(PURPOSE_CATEGORY_ID, purposeCategoryId))
                .executeWith(new OperationDelegate<Void>() {
                    @Override
                    public Void execute() throws ConsentManagementException {

                        consentManager.deletePurposeCategory(purposeCategoryId);
                        return null;
                    }
                })
                .intercept(POST_DELETE_PURPOSE_CATEGORY, properties -> properties.put(PURPOSE_CATEGORY_ID,
                        purposeCategoryId));
    }

    public boolean isPurposeCategoryExists(String name) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<Boolean, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_IS_PURPOSE_CATEGORY_EXIST, properties -> properties.put(PURPOSE_CATEGORY_NAME,
                name))
                .executeWith(new OperationDelegate<Boolean>() {
                    @Override
                    public Boolean execute() throws ConsentManagementException {

                        return consentManager.isPurposeCategoryExists(name);
                    }
                })
                .intercept(POST_IS_PURPOSE_CATEGORY_EXIST, properties -> properties.put(PURPOSE_CATEGORY_NAME, name))
                .getResult();
    }

    public PIICategory addPIICategory(PIICategory piiCategory) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<PIICategory, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_ADD_PII_CATEGORY, properties -> properties.put(PII_CATEGORY, piiCategory))
                .executeWith(new OperationDelegate<PIICategory>() {
                    @Override
                    public PIICategory execute() throws ConsentManagementException {

                        return consentManager.addPIICategory(piiCategory);
                    }
                })
                .intercept(POST_ADD_PII_CATEGORY, properties -> properties.put(PII_CATEGORY, piiCategory))
                .getResult();
    }

    public PIICategory getPIICategoryByName(String name) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<PIICategory, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_GET_PII_CATEGORY_BY_NAME, properties -> properties.put(PII_CATEGORY_NAME, name))
                .executeWith(new OperationDelegate<PIICategory>() {
                    @Override
                    public PIICategory execute() throws ConsentManagementException {

                        return consentManager.getPIICategoryByName(name);
                    }
                })
                .intercept(POST_GET_PII_CATEGORY_BY_NAME, properties -> properties.put(PII_CATEGORY_NAME, name))
                .getResult();
    }

    public PIICategory getPIICategory(int piiCategoryId) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<PIICategory, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_GET_PII_CATEGORY, properties -> properties.put(PII_CATEGORY_ID, piiCategoryId))
                .executeWith(new OperationDelegate<PIICategory>() {
                    @Override
                    public PIICategory execute() throws ConsentManagementException {

                        return consentManager.getPIICategory(piiCategoryId);
                    }
                })
                .intercept(POST_GET_PII_CATEGORY, properties -> properties.put(PII_CATEGORY_ID, piiCategoryId))
                .getResult();
    }

    public List<PIICategory> listPIICategories(int limit, int offset) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<List<PIICategory>, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_GET_PII_CATEGORY_LIST, properties -> {
            properties.put(LIMIT, limit);
            properties.put(OFFSET, offset);
        })
                .executeWith(new OperationDelegate<List<PIICategory>>() {
                    @Override
                    public List<PIICategory> execute() throws ConsentManagementException {

                        return consentManager.listPIICategories(limit, offset);
                    }
                })
                .intercept(POST_GET_PII_CATEGORY_LIST, properties -> {
                    properties.put(LIMIT, limit);
                    properties.put(OFFSET, offset);
                })
                .getResult();
    }

    public void deletePIICategory(int piiCategoryId) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<Void, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        template.intercept(PRE_DELETE_PII_CATEGORY, properties -> properties.put(PII_CATEGORY_ID, piiCategoryId))
                .executeWith(new OperationDelegate<Void>() {
                    @Override
                    public Void execute() throws ConsentManagementException {

                        consentManager.deletePIICategory(piiCategoryId);
                        return null;
                    }
                })
                .intercept(POST_DELETE_PII_CATEGORY, properties -> properties.put(PII_CATEGORY_ID, piiCategoryId));
    }

    public boolean isPIICategoryExists(String name) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<Boolean, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_IS_PII_CATEGORY_EXIST, properties -> properties.put(PII_CATEGORY_NAME,
                name))
                .executeWith(new OperationDelegate<Boolean>() {
                    @Override
                    public Boolean execute() throws ConsentManagementException {

                        return consentManager.isPIICategoryExists(name);
                    }
                })
                .intercept(POST_IS_PII_CATEGORY_EXIST, properties -> properties.put(PII_CATEGORY_NAME, name))
                .getResult();
    }

    public AddReceiptResponse addConsent(ReceiptInput receiptInput) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<AddReceiptResponse, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_ADD_RECEIPT, properties -> properties.put(RECEIPT_INPUT, receiptInput))
                .executeWith(new OperationDelegate<AddReceiptResponse>() {
                    @Override
                    public AddReceiptResponse execute() throws ConsentManagementException {

                        return consentManager.addConsent(receiptInput);
                    }
                })
                .intercept(POST_ADD_RECEIPT, properties -> properties.put(RECEIPT_INPUT, receiptInput))
                .getResult();
    }

    public Receipt getReceipt(String receiptId) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<Receipt, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_GET_RECEIPT, properties -> properties.put(RECEIPT_ID, receiptId))
                .executeWith(new OperationDelegate<Receipt>() {
                    @Override
                    public Receipt execute() throws ConsentManagementException {

                        return consentManager.getReceipt(receiptId);
                    }
                })
                .intercept(POST_GET_RECEIPT, properties -> properties.put(RECEIPT_ID, receiptId))
                .getResult();
    }

    public List<ReceiptListResponse> searchReceipts(int limit, int offset, String piiPrincipalId, String spTenantDomain,
                                                    String service, String state) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<List<ReceiptListResponse>, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_LIST_RECEIPTS, properties -> {
            populateProperties(limit, offset, piiPrincipalId, spTenantDomain, service, state, properties);
        })
                .executeWith(new OperationDelegate<List<ReceiptListResponse>>() {
                    @Override
                    public List<ReceiptListResponse> execute() throws ConsentManagementException {

                        return consentManager.searchReceipts(limit, offset, piiPrincipalId, spTenantDomain, service,
                                state);
                    }
                })
                .intercept(POST_LIST_RECEIPTS, properties -> {
                    populateProperties(limit, offset, piiPrincipalId, spTenantDomain, service, state, properties);
                })
                .getResult();
    }

    @Override
    public List<ReceiptListResponse> searchReceipts(int limit, int offset, String piiPrincipalId, String spTenantDomain,
                                                    String service, String state, String principalTenantDomain)
            throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<List<ReceiptListResponse>, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        return template.intercept(PRE_LIST_RECEIPTS, properties -> {
            populateProperties(limit, offset, piiPrincipalId, spTenantDomain, service, state, principalTenantDomain,
                    properties);
        })
                .executeWith(new OperationDelegate<List<ReceiptListResponse>>() {
                    @Override
                    public List<ReceiptListResponse> execute() throws ConsentManagementException {

                        return consentManager.searchReceipts(limit, offset, piiPrincipalId, spTenantDomain, service,
                                state, principalTenantDomain);
                    }
                })
                .intercept(POST_LIST_RECEIPTS, properties -> {
                    populateProperties(limit, offset, piiPrincipalId, spTenantDomain, service, state,
                            principalTenantDomain, properties);
                })
                .getResult();
    }

    public void revokeReceipt(String receiptId) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<Void, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        template.intercept(PRE_REVOKE_RECEIPT, properties -> properties.put(RECEIPT_ID, receiptId))
                .executeWith(new OperationDelegate<Void>() {
                    @Override
                    public Void execute() throws ConsentManagementException {

                        consentManager.revokeReceipt(receiptId);
                        return null;
                    }
                })
                .intercept(POST_REVOKE_RECEIPT, properties -> properties.put(RECEIPT_ID, receiptId));
    }

    public void deleteReceipt(String receiptId) throws ConsentManagementException {

        ConsentMessageContext context = new ConsentMessageContext();
        ConsentInterceptorTemplate<Void, ConsentManagementException>
                template = new ConsentInterceptorTemplate<>(consentMgtInterceptors, context);

        template.intercept(PRE_DELETE_RECEIPT, properties -> properties.put(RECEIPT_ID, receiptId))
                .executeWith(new OperationDelegate<Void>() {
                    @Override
                    public Void execute() throws ConsentManagementException {

                        consentManager.deleteReceipt(receiptId);
                        return null;
                    }
                })
                .intercept(POST_DELETE_RECEIPT, properties -> properties.put(RECEIPT_ID, receiptId));
    }

    /**
     * This API is used to check whether a receipt exists for the user identified by the tenantAwareUser name in the
     * provided tenant
     * This is not supposed to invoke any interceptor, since this is added to
     *
     * @param receiptId           Consent Receipt ID
     * @param tenantAwareUsername Tenant aware username
     * @param tenantId            User tenant id
     * @return boolean true if receipt exists for match criteria
     */
    @Override
    public boolean isReceiptExist(String receiptId,
                                  String tenantAwareUsername,
                                  int tenantId) throws ConsentManagementException {

        return consentManager.isReceiptExist(receiptId, tenantAwareUsername, tenantId);
    }

    private void populateProperties(int limit, int offset, String piiPrincipalId, String spTenantDomain, String
            service, String state, Map<String, Object> properties) {

        properties.put(LIMIT, limit);
        properties.put(OFFSET, offset);
        properties.put(PII_PRINCIPAL_ID, piiPrincipalId);
        properties.put(SP_TENANT_DOMAIN, spTenantDomain);
        properties.put(SERVICE, service);
        properties.put(STATE, state);
    }

    private void populateProperties(int limit, int offset, String piiPrincipalId, String spTenantDomain, String
            service, String state, String principalTenantDomain, Map<String, Object> properties) {

        properties.put(LIMIT, limit);
        properties.put(OFFSET, offset);
        properties.put(PII_PRINCIPAL_ID, piiPrincipalId);
        properties.put(SP_TENANT_DOMAIN, spTenantDomain);
        properties.put(SERVICE, service);
        properties.put(STATE, state);
        properties.put(PRINCIPAL_TENANT_DOMAIN, principalTenantDomain);
    }
}
