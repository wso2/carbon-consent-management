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

package org.wso2.carbon.consent.mgt.core.dao.impl;

import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.dao.JdbcTemplate;
import org.wso2.carbon.consent.mgt.core.dao.ReceiptDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.exception.DataAccessException;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_RECEIPT_PROPERTIES_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_RECEIPT_SP_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_RECEIPT_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_SP_PURPOSE_TO_PII_CAT_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_SP_PURPOSE_TO_PURPOSE_CAT_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_SP_TO_PURPOSE_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.util.LambdaExceptionUtils.rethrowConsumer;

/**
 * Default implementation of {@link ReceiptDAO}. This handles {@link Receipt} related DB operations.
 */
public class ReceiptDAOImpl implements ReceiptDAO {
    private final JdbcTemplate jdbcTemplate;

    public ReceiptDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Receipt addReceipt(ReceiptInput receiptInput) throws ConsentManagementException {
        addReceiptInfo(receiptInput);

        receiptInput.getServices().forEach(rethrowConsumer(receiptServiceInput -> {
            int receiptToSPAssocId = addReceiptSPAssociation(receiptInput.getConsentReceiptId(), receiptServiceInput);
            receiptServiceInput.getPurposes().forEach(rethrowConsumer(receiptPurposeInput -> {
                int spToPurposeAssocId = addSpToPurposeAssociation(receiptToSPAssocId, receiptPurposeInput);

                receiptPurposeInput.getPurposeCategoryId().forEach(rethrowConsumer(id ->
                        addSpPurposeToPurposeCategoryAssociation(spToPurposeAssocId, id)));

                receiptPurposeInput.getPiiCategoryId().forEach(rethrowConsumer(id ->
                        addSpPurposeToPiiCategoryAssociation(spToPurposeAssocId, id)));
            }));
        }));

        addReceiptProperties(receiptInput.getConsentReceiptId(), receiptInput.getProperties());
        return new Receipt();
    }

    @Override
    public Receipt getReceipt(String receiptId) throws ConsentManagementException {
        return null;
    }


    protected void addReceiptInfo(ReceiptInput receiptInput) throws ConsentManagementServerException {
        try {
            jdbcTemplate.executeInsert(INSERT_RECEIPT_SQL, (preparedStatement -> {
                preparedStatement.setString(1, receiptInput.getConsentReceiptId());
                preparedStatement.setString(2, receiptInput.getVersion());
                preparedStatement.setString(3, receiptInput.getJurisdiction());
                preparedStatement.setTimestamp(4, new java.sql.Timestamp(new Date().getTime()),
                        Calendar.getInstance(TimeZone.getTimeZone(UTC)));
                preparedStatement.setString(5, receiptInput.getCollectionMethod());
                preparedStatement.setString(6, receiptInput.getLanguage());
                preparedStatement.setString(7, receiptInput.getPiiPrincipalId());
                preparedStatement.setString(8, receiptInput.getTenantDomain() != null ?
                        receiptInput.getTenantDomain() : MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                preparedStatement.setString(9, receiptInput.getPolicyUrl());

            }), receiptInput, false);
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages.ERROR_CODE_ADD_RECEIPT,
                    receiptInput.getPiiPrincipalId(), e);
        }
    }

    protected int addReceiptSPAssociation(String receiptId, ReceiptServiceInput receiptServiceInput) throws
            ConsentManagementServerException {
        try {
            return jdbcTemplate.executeInsert(INSERT_RECEIPT_SP_ASSOC_SQL, (preparedStatement -> {
                preparedStatement.setString(1, receiptId);
                preparedStatement.setString(2, receiptServiceInput.getService());
                preparedStatement.setString(3, receiptServiceInput.getTenantDomain());
            }), receiptServiceInput, true);
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages.ERROR_CODE_ADD_RECEIPT_SP_ASSOC,
                    receiptServiceInput.getService(), e);
        }
    }

    protected int addSpToPurposeAssociation(int receiptToSPAssocId, ReceiptPurposeInput receiptPurposeInput) throws
            ConsentManagementServerException {
        try {
            return jdbcTemplate.executeInsert(INSERT_SP_TO_PURPOSE_ASSOC_SQL, (preparedStatement -> {
                preparedStatement.setInt(1, receiptToSPAssocId);
                preparedStatement.setInt(2, receiptPurposeInput.getPurposeId());
                preparedStatement.setString(3, receiptPurposeInput.getConsentType());
                preparedStatement.setInt(4, receiptPurposeInput.isPrimaryPurpose() ? 1 : 0);
                preparedStatement.setString(5, receiptPurposeInput.getTermination());
                preparedStatement.setInt(6, receiptPurposeInput.isThirdPartyDisclosure() ? 1 : 0);
                preparedStatement.setString(7, receiptPurposeInput.getThirdPartyName());
            }), receiptPurposeInput, true);
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages.ERROR_CODE_ADD_SP_TO_PURPOSE_ASSOC,
                    String.valueOf(receiptPurposeInput.getPurposeId()), e);
            //TODO need to use purposeName here.IT should set to receiptPurposeInput when validation.
        }
    }


    protected void addSpPurposeToPurposeCategoryAssociation(int spToPurposeAssocId, int id) throws
            ConsentManagementServerException {
        try {
            jdbcTemplate.executeInsert(INSERT_SP_PURPOSE_TO_PURPOSE_CAT_ASSOC_SQL, (preparedStatement -> {
                preparedStatement.setInt(1, spToPurposeAssocId);
                preparedStatement.setInt(2, id);
            }), id, false);
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages
                    .ERROR_CODE_ADD_SP_PURPOSE_TO_PURPOSE_CAT_ASSOC, null, e);
        }
    }

    protected void addSpPurposeToPiiCategoryAssociation(int spToPurposeAssocId, Integer id) throws
            ConsentManagementServerException {
        try {
            jdbcTemplate.executeInsert(INSERT_SP_PURPOSE_TO_PII_CAT_ASSOC_SQL, (preparedStatement -> {
                preparedStatement.setInt(1, spToPurposeAssocId);
                preparedStatement.setInt(2, id);
            }), id, false);
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages
                    .ERROR_CODE_ADD_SP_PURPOSE_TO_PII_CAT_ASSOC, null, e);
        }
    }

    protected void addReceiptProperties(String consentReceiptId, Map<String, String> properties) throws
            ConsentManagementServerException {
        try {
            jdbcTemplate.executeBatchInsert(INSERT_RECEIPT_PROPERTIES_SQL, (preparedStatement -> {

                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    preparedStatement.setString(1, consentReceiptId);
                    preparedStatement.setString(2, entry.getKey());
                    preparedStatement.setString(3, entry.getValue());
                    preparedStatement.addBatch();
                }
            }), consentReceiptId);
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages
                    .ERROR_CODE_ADD_RECEIPT_PROPERTIES, null, e);
        }
    }
}
