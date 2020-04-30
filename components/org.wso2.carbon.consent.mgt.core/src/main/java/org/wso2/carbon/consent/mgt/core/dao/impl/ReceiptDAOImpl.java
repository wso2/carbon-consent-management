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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.dao.ReceiptDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.model.ConsentPurpose;
import org.wso2.carbon.consent.mgt.core.model.PIICategoryValidity;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptContext;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptService;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;
import org.wso2.carbon.consent.mgt.core.util.JdbcUtils;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ACTIVE_STATE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.REVOKE_STATE;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_RECEIPTS_BY_PRINCIPAL_TENANT_ID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_RECEIPT_PROPERTIES_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_RECEIPT_SP_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_RECEIPT_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_SP_PURPOSE_TO_PII_CAT_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_SP_PURPOSE_TO_PURPOSE_CAT_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_SP_TO_PURPOSE_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_ACTIVE_RECEIPTS_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PII_CAT_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_CAT_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_RECEIPT_BASIC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_RECEIPT_SP_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_RECEIPT_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_SP_PURPOSE_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_RECEIPT_PROPERTIES_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_RECEIPT_SP_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_RECEIPT_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_SP_PURPOSE_TO_PII_CAT_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_SP_PURPOSE_TO_PURPOSE_CAT_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_SP_TO_PURPOSE_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.REVOKE_RECEIPT_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_DB2;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_INFORMIX;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_MSSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_ORACLE;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_PRINCIPLE_TENANT;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_PRINCIPLE_TENANT_DB2;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_PRINCIPLE_TENANT_INFORMIX;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_PRINCIPLE_TENANT_MSSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_PRINCIPLE_TENANT_ORACLE;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_AND_PRINCIPLE_TENANT;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_AND_PRINCIPLE_TENANT_DB2;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_AND_PRINCIPLE_TENANT_INFORMIX;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_AND_PRINCIPLE_TENANT_MSSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_AND_PRINCIPLE_TENANT_ORACLE;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_DB2;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_INFORMIX;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_MSSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_ORACLE;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isDB2DB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isH2MySqlOrPostgresDB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isInformixDB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isMSSqlDB;
import static org.wso2.carbon.consent.mgt.core.util.LambdaExceptionUtils.rethrowConsumer;

/**
 * Default implementation of {@link ReceiptDAO}. This handles {@link Receipt} related DB operations.
 */
public class ReceiptDAOImpl implements ReceiptDAO {

    private static final String SQL_FILTER_STRING_ANY = "%";
    private static final String QUERY_FILTER_STRING_ANY = "*";
    private static final String QUERY_FILTER_STRING_ANY_ESCAPED = "\\*";
    private static final Log log = LogFactory.getLog(ReceiptDAOImpl.class);

    public ReceiptDAOImpl() {

    }

    @Override
    public int getPriority() {

        return 1;
    }

    @Override
    public void addReceipt(ReceiptInput receiptInput) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                revokeActiveReceipts(receiptInput);
                addReceiptInfo(receiptInput);

                receiptInput.getServices().forEach(rethrowConsumer(receiptServiceInput -> {
                    int receiptToSPAssocId = addReceiptSPAssociation(receiptInput.getConsentReceiptId(), receiptServiceInput);
                    receiptServiceInput.getPurposes().forEach(rethrowConsumer(receiptPurposeInput -> {
                        int spToPurposeAssocId = addSpToPurposeAssociation(receiptToSPAssocId, receiptPurposeInput);

                        receiptPurposeInput.getPurposeCategoryId().forEach(rethrowConsumer(id ->
                                addSpPurposeToPurposeCategoryAssociation(spToPurposeAssocId, id)));

                        receiptPurposeInput.getPiiCategory().forEach(rethrowConsumer(piiCategoryValidity ->
                                addSpPurposeToPiiCategoryAssociation(spToPurposeAssocId, piiCategoryValidity.getId(),
                                        piiCategoryValidity.getValidity())));
                    }));
                }));

                if (receiptInput.getProperties() != null) {
                    addReceiptProperties(receiptInput.getConsentReceiptId(), receiptInput.getProperties());
                }
                return null;
            });
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_CONSENT_RECEIPT,
                                                     receiptInput.getPiiPrincipalId(), e);
        }
    }

    private void revokeActiveReceipts(ReceiptInput receiptInput) throws ConsentManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                receiptInput.getServices().forEach(rethrowConsumer(receiptServiceInput -> {
                    List<String> ids = template.executeQuery(GET_ACTIVE_RECEIPTS_SQL, (resultSet, rowNumber) -> resultSet
                            .getString(1), preparedStatement -> {
                        preparedStatement.setString(1, receiptInput.getPiiPrincipalId());
                        preparedStatement.setString(2, receiptServiceInput.getService());
                        preparedStatement.setInt(3, receiptInput.getTenantId());
                        preparedStatement.setInt(4, receiptServiceInput.getTenantId());
                    });

                    if (isNotEmpty(ids)) {
                        ids.forEach(rethrowConsumer(id -> {
                            revokeReceipt(id);
                            if (log.isDebugEnabled()) {
                                log.debug("Revoked active receipt: " + id + " of the user: " + receiptInput
                                        .getPiiPrincipalId());
                            }
                        }));
                    }
                }));
                return null;
            });
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_REVOKE_ACTIVE_RECEIPT,
                                                     receiptInput.getPiiPrincipalId(), e);
        }
    }

    @Override
    public Receipt getReceipt(String receiptId) throws ConsentManagementException {

        ReceiptContext receiptContext = new ReceiptContext();
        Receipt receipt;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            receipt = jdbcTemplate.withTransaction(template -> {
                Receipt internalReceipt = template.fetchSingleRecord(GET_RECEIPT_SQL, (resultSet, rowNumber) -> {
                    Receipt receiptInfo = new Receipt();
                    receiptInfo.setConsentReceiptId(receiptId);
                    receiptInfo.setVersion(resultSet.getString(1));
                    receiptInfo.setJurisdiction(resultSet.getString(2));
                    receiptInfo.setConsentTimestamp(resultSet.getTimestamp(3).getTime());
                    receiptInfo.setCollectionMethod(resultSet.getString(4));
                    receiptInfo.setLanguage(resultSet.getString(5));
                    receiptInfo.setPiiPrincipalId(resultSet.getString(6));
                    receiptInfo.setTenantId(resultSet.getInt(7));
                    receiptInfo.setPolicyUrl(resultSet.getString(8));
                    receiptInfo.setState(resultSet.getString(9));
                    receiptInfo.setPiiController(resultSet.getString(10));
                    return receiptInfo;
                }, preparedStatement -> {
                    preparedStatement.setString(1, receiptId);
                });

                if (internalReceipt != null) {
                    internalReceipt.setServices(getServiceInfoOfReceipt(receiptId, receiptContext));
                    setReceiptSensitivity(receiptContext, internalReceipt);
                }
                return internalReceipt;
            });
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_RETRIEVE_RECEIPT_INFO,
                                                     String.valueOf(receiptId), e);
        }
        return receipt;
    }

    @Override
    public boolean isReceiptExist(String receiptId, String piiPrincipalId, int tenantId) throws
            ConsentManagementException {

        try {
            JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
            String receipt = jdbcTemplate.withTransaction(template -> template.fetchSingleRecord
                    (GET_RECEIPT_BASIC_SQL, (resultSet, rowNumber) ->
                    resultSet.getString(1), preparedStatement -> {
                        preparedStatement.setString(1, receiptId);
                        preparedStatement.setString(2, piiPrincipalId);
                        preparedStatement.setInt(3, tenantId);
                    }));
            return receipt != null;

        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_RETRIEVE_RECEIPT_EXISTENCE,
                    "Receipt Id: " + receiptId + ", PII Principal Id: " + piiPrincipalId + "and Tenant Id: "
                            + tenantId, e);
        }
    }

    @Override
    public void revokeReceipt(String receiptId) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(REVOKE_RECEIPT_SQL, preparedStatement -> {
                    preparedStatement.setString(1, REVOKE_STATE);
                    preparedStatement.setString(2, receiptId);
                });
                return null;
            });
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_REVOKE_RECEIPT,
                                                     receiptId, e);
        }
    }

    @Override
    public List<ReceiptListResponse> searchReceipts(int limit, int offset, String piiPrincipalId, int
            spTenantId, String service, String state, int principalTenantId) throws ConsentManagementException {

        List<ReceiptListResponse> receiptListResponses;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            int piiPrincipalTenantId = ConsentUtils.getTenantIdFromCarbonContext();
            if (piiPrincipalId == null) {
                piiPrincipalId = SQL_FILTER_STRING_ANY;
            } else if (piiPrincipalId.contains(QUERY_FILTER_STRING_ANY)) {
                piiPrincipalId = piiPrincipalId.replaceAll(QUERY_FILTER_STRING_ANY_ESCAPED, SQL_FILTER_STRING_ANY);
            }

            if (service == null) {
                service = SQL_FILTER_STRING_ANY;
            } else if (service.contains(QUERY_FILTER_STRING_ANY)) {
                service = service.replaceAll(QUERY_FILTER_STRING_ANY_ESCAPED, SQL_FILTER_STRING_ANY);
            }

            if (state == null) {
                state = SQL_FILTER_STRING_ANY;
            }

            String finalPiiPrincipalId = piiPrincipalId;
            String finalService = service;
            String finalState = state;
            String query;

            if (spTenantId != 0 && principalTenantId != 0) { // Tenant domain is used for search results.
                receiptListResponses = searchReceipt(limit, offset, spTenantId, jdbcTemplate, piiPrincipalTenantId,
                        finalPiiPrincipalId, finalService, finalState);
            } else if (spTenantId == 0 && principalTenantId != 0) {
                receiptListResponses = searchWithoutSpTenant(limit, offset, jdbcTemplate, piiPrincipalTenantId,
                        finalPiiPrincipalId, finalService, finalState);
            } else if (spTenantId != 0 && principalTenantId == 0) {
                receiptListResponses = searchReceiptWithoutPrincipleTenant(limit, offset, spTenantId, jdbcTemplate,
                        finalPiiPrincipalId, finalService, finalState);
            } else {
                receiptListResponses = searchWithoutPrincipleAndSPTenantDomain(limit, offset, jdbcTemplate,
                        finalPiiPrincipalId, finalService, finalState);
            }
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SEARCH_RECEIPTS,
                                                     piiPrincipalId, e);
        }
        return receiptListResponses;
    }

    protected List<ReceiptListResponse> searchReceipt(int limit, int offset, int spTenantId,
                                                    JdbcTemplate jdbcTemplate, int piiPrincipalTenantId,
                                                    String finalPiiPrincipalId, String finalService,
                                                    String finalState) throws DataAccessException {

        String query;
        List<ReceiptListResponse> receiptListResponses;
        if (isH2MySqlOrPostgresDB()) {
            query = SEARCH_RECEIPT_SQL;
        } else if (isDB2DB()) {
            query = SEARCH_RECEIPT_SQL_DB2;
            int initialOffset = offset;
            offset = offset + limit;
            limit = initialOffset + 1;
        } else if (isMSSqlDB()) {
            int initialOffset = offset;
            offset = limit + offset;
            limit = initialOffset + 1;
            query = SEARCH_RECEIPT_SQL_MSSQL;
        } else if (isInformixDB()) {
            query = SEARCH_RECEIPT_SQL_INFORMIX;
        } else {
            //oracle
            query = SEARCH_RECEIPT_SQL_ORACLE;
            limit = offset + limit;
        }

        int finalLimit = limit;
        int finalOffset = offset;
        receiptListResponses = jdbcTemplate.executeQuery(query, (resultSet, rowNumber) ->
                        new ReceiptListResponse(resultSet.getString(1), resultSet
                                .getString(2), resultSet.getString(3), resultSet.getInt(4), resultSet
                                .getString(5), resultSet.getString(6), resultSet.getString(7)),
                preparedStatement -> {
                    preparedStatement.setString(1, finalPiiPrincipalId);
                    preparedStatement.setInt(2, piiPrincipalTenantId);
                    preparedStatement.setString(3, finalService);
                    preparedStatement.setInt(4, spTenantId);
                    preparedStatement.setString(5, finalState);
                    preparedStatement.setInt(6, finalLimit);
                    preparedStatement.setInt(7, finalOffset);
                });
        return receiptListResponses;
    }

    protected List<ReceiptListResponse> searchWithoutSpTenant(int limit, int offset, JdbcTemplate jdbcTemplate,
                                                              int piiPrincipalTenantId, String finalPiiPrincipalId,
                                                              String finalService, String finalState)
            throws DataAccessException {

        String query;
        List<ReceiptListResponse> receiptListResponses;
        if (isH2MySqlOrPostgresDB()) {
            query = SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT;
        } else if (isDB2DB()) {
            query = SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_DB2;
            int initialOffset = offset;
            offset = offset + limit;
            limit = initialOffset + 1;
        } else if (isMSSqlDB()) {
            int initialOffset = offset;
            query = SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_MSSQL;
            offset = limit + offset;
            limit = initialOffset + 1;
        } else if (isInformixDB()) {
            query = SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_INFORMIX;
        } else {
            //oracle
            query = SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_ORACLE;
            limit = offset + limit;
        }
        int finalLimit = limit;
        int finalOffset = offset;
        receiptListResponses = jdbcTemplate.executeQuery(query, (resultSet, rowNumber) ->
                        new ReceiptListResponse(resultSet.getString(1), resultSet
                                .getString(2), resultSet.getString(3), resultSet.getInt(4), resultSet
                                .getString(5), resultSet.getString(6), resultSet.getString(7)),
                preparedStatement -> {

                    preparedStatement.setString(1, finalPiiPrincipalId);
                    preparedStatement.setInt(2, piiPrincipalTenantId);
                    preparedStatement.setString(3, finalService);
                    preparedStatement.setString(4, finalState);
                    preparedStatement.setInt(5, finalLimit);
                    preparedStatement.setInt(6, finalOffset);
                });
        return receiptListResponses;
    }

    protected List<ReceiptListResponse> searchReceiptWithoutPrincipleTenant(int limit, int offset, int spTenantId,
                                                    JdbcTemplate jdbcTemplate, String finalPiiPrincipalId,
                                                                          String finalService, String finalState)
            throws DataAccessException {

        String query;
        List<ReceiptListResponse> receiptListResponses;
        if (isH2MySqlOrPostgresDB()) {
            query = SEARCH_RECEIPT_SQL_WITHOUT_PRINCIPLE_TENANT;
        } else if (isDB2DB()) {
            query = SEARCH_RECEIPT_SQL_WITHOUT_PRINCIPLE_TENANT_DB2;
            int initialOffset = offset;
            offset = offset + limit;
            limit = initialOffset + 1;
        } else if (isMSSqlDB()) {
            int initialOffset = offset;
            offset = limit + offset;
            limit = initialOffset + 1;
            query = SEARCH_RECEIPT_SQL_WITHOUT_PRINCIPLE_TENANT_MSSQL;
        } else if (isInformixDB()) {
            query = SEARCH_RECEIPT_SQL_WITHOUT_PRINCIPLE_TENANT_INFORMIX;
        } else {
            //oracle
            query = SEARCH_RECEIPT_SQL_WITHOUT_PRINCIPLE_TENANT_ORACLE;
            limit = offset + limit;
        }

        int finalLimit = limit;
        int finalOffset = offset;
        receiptListResponses = jdbcTemplate.executeQuery(query, (resultSet, rowNumber) ->
                        new ReceiptListResponse(resultSet.getString(1), resultSet
                                .getString(2), resultSet.getString(3), resultSet.getInt(4), resultSet
                                .getString(5), resultSet.getString(6), resultSet.getString(7)),
                preparedStatement -> {
                    preparedStatement.setString(1, finalPiiPrincipalId);
                    preparedStatement.setString(2, finalService);
                    preparedStatement.setInt(3, spTenantId);
                    preparedStatement.setString(4, finalState);
                    preparedStatement.setInt(5, finalLimit);
                    preparedStatement.setInt(6, finalOffset);
                });
        return receiptListResponses;
    }

    protected List<ReceiptListResponse> searchWithoutPrincipleAndSPTenantDomain(int limit, int offset,
                                                         JdbcTemplate jdbcTemplate,
                                                         String finalPiiPrincipalId, String finalService,
                                                         String finalState) throws DataAccessException {

        String query;
        List<ReceiptListResponse> receiptListResponses;
        if (isH2MySqlOrPostgresDB()) {
            query = SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_AND_PRINCIPLE_TENANT;
        } else if (isDB2DB()) {
            query = SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_AND_PRINCIPLE_TENANT_DB2;
            int initialOffset = offset;
            offset = offset + limit;
            limit = initialOffset + 1;
        } else if (isMSSqlDB()) {
            int initialOffset = offset;
            offset = limit + offset;
            limit = initialOffset + 1;
            query = SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_AND_PRINCIPLE_TENANT_MSSQL;
        } else if (isInformixDB()) {
            query = SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_AND_PRINCIPLE_TENANT_INFORMIX;
        } else {
            //oracle
            query = SEARCH_RECEIPT_SQL_WITHOUT_SP_TENANT_AND_PRINCIPLE_TENANT_ORACLE;
            limit = offset + limit;
        }

        int finalLimit = limit;
        int finalOffset = offset;
        receiptListResponses = jdbcTemplate.executeQuery(query, (resultSet, rowNumber) ->
                        new ReceiptListResponse(resultSet.getString(1), resultSet
                                .getString(2), resultSet.getString(3), resultSet.getInt(4), resultSet
                                .getString(5), resultSet.getString(6), resultSet.getString(7)),
                preparedStatement -> {
                    preparedStatement.setString(1, finalPiiPrincipalId);
                    preparedStatement.setString(2, finalService);
                    preparedStatement.setString(3, finalState);
                    preparedStatement.setInt(4, finalLimit);
                    preparedStatement.setInt(5, finalOffset);
                });
        return receiptListResponses;
    }

    protected void setReceiptSensitivity(ReceiptContext receiptContext, Receipt receipt) {

        if (receiptContext.getSecretPIICategory().getSecretPIICategories().size() > 0) {
            receipt.setSensitive(true);
            receipt.setSpiCat(receiptContext.getSecretPIICategory().getSecretPIICategories());
        }
    }

    protected void addReceiptInfo(ReceiptInput receiptInput) throws ConsentManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeInsert(INSERT_RECEIPT_SQL, (preparedStatement -> {
                    preparedStatement.setString(1, receiptInput.getConsentReceiptId());
                    preparedStatement.setString(2, receiptInput.getVersion());
                    preparedStatement.setString(3, receiptInput.getJurisdiction());
                    preparedStatement.setTimestamp(4, new java.sql.Timestamp(new Date().getTime()),
                            Calendar.getInstance(TimeZone.getTimeZone(UTC)));
                    preparedStatement.setString(5, receiptInput.getCollectionMethod());
                    preparedStatement.setString(6, receiptInput.getLanguage());
                    preparedStatement.setString(7, receiptInput.getPiiPrincipalId());
                    preparedStatement.setInt(8, receiptInput.getTenantId());
                    preparedStatement.setString(9, receiptInput.getPolicyUrl());
                    preparedStatement.setString(10, ACTIVE_STATE);
                    preparedStatement.setString(11, receiptInput.getPiiControllerInfo());

                }), receiptInput, false);
                return null;
            });
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_RECEIPT,
                                                     receiptInput.getPiiPrincipalId(), e);
        }
    }

    protected int addReceiptSPAssociation(String receiptId, ReceiptServiceInput receiptServiceInput) throws
            ConsentManagementServerException {

        int receiptToSPAssocId;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            receiptToSPAssocId = jdbcTemplate.withTransaction(template -> template.executeInsert(INSERT_RECEIPT_SP_ASSOC_SQL, (preparedStatement -> {
                preparedStatement.setString(1, receiptId);
                preparedStatement.setString(2, receiptServiceInput.getService());
                preparedStatement.setInt(3, receiptServiceInput.getTenantId());
                preparedStatement.setString(4, receiptServiceInput.getSpDisplayName());
                preparedStatement.setString(5, receiptServiceInput.getSpDescription());
            }), receiptServiceInput, true));
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_RECEIPT_SP_ASSOC,
                                                     receiptServiceInput.getService(), e);
        }
        return receiptToSPAssocId;
    }

    protected int addSpToPurposeAssociation(int receiptToSPAssocId, ReceiptPurposeInput receiptPurposeInput) throws
            ConsentManagementServerException {

        int spToPurposeAssocId;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            spToPurposeAssocId = jdbcTemplate.withTransaction(template -> template.executeInsert(INSERT_SP_TO_PURPOSE_ASSOC_SQL, (preparedStatement -> {
                preparedStatement.setInt(1, receiptToSPAssocId);
                preparedStatement.setInt(2, receiptPurposeInput.getPurposeId());
                preparedStatement.setString(3, receiptPurposeInput.getConsentType());
                preparedStatement.setInt(4, receiptPurposeInput.isPrimaryPurpose() ? 1 : 0);
                preparedStatement.setString(5, receiptPurposeInput.getTermination());
                preparedStatement.setInt(6, receiptPurposeInput.isThirdPartyDisclosure() ? 1 : 0);
                preparedStatement.setString(7, receiptPurposeInput.getThirdPartyName());
            }), receiptPurposeInput, true));
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_SP_TO_PURPOSE_ASSOC,
                                                     String.valueOf(receiptPurposeInput.getPurposeName()), e);
        }
        return spToPurposeAssocId;
    }

    protected void addSpPurposeToPurposeCategoryAssociation(int spToPurposeAssocId, int id) throws
            ConsentManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeInsert(INSERT_SP_PURPOSE_TO_PURPOSE_CAT_ASSOC_SQL, (preparedStatement -> {
                    preparedStatement.setInt(1, spToPurposeAssocId);
                    preparedStatement.setInt(2, id);
                }), id, false);
                return null;
            });
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages
                    .ERROR_CODE_ADD_SP_PURPOSE_TO_PURPOSE_CAT_ASSOC, null, e);
        }
    }

    protected void addSpPurposeToPiiCategoryAssociation(int spToPurposeAssocId, int id, String validity) throws
            ConsentManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeInsert(INSERT_SP_PURPOSE_TO_PII_CAT_ASSOC_SQL, (preparedStatement -> {
                    preparedStatement.setInt(1, spToPurposeAssocId);
                    preparedStatement.setInt(2, id);
                    preparedStatement.setString(3, validity);
                }), id, false);
                return null;
            });
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages
                    .ERROR_CODE_ADD_SP_PURPOSE_TO_PII_CAT_ASSOC, null, e);
        }
    }

    protected void addReceiptProperties(String consentReceiptId, Map<String, String> properties) throws
            ConsentManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeBatchInsert(INSERT_RECEIPT_PROPERTIES_SQL, (preparedStatement -> {

                    for (Map.Entry<String, String> entry : properties.entrySet()) {
                        preparedStatement.setString(1, consentReceiptId);
                        preparedStatement.setString(2, entry.getKey());
                        preparedStatement.setString(3, entry.getValue());
                        preparedStatement.addBatch();
                    }
                }), consentReceiptId);
                return null;
            });
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages
                    .ERROR_CODE_ADD_RECEIPT_PROPERTIES, null, e);
        }
    }

    protected List<ReceiptService> getServiceInfoOfReceipt(String consentReceiptId, ReceiptContext receiptContext) throws
            ConsentManagementServerException {

        List<ReceiptService> receiptServices;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        try {
            receiptServices = jdbcTemplate.withTransaction(template -> {
                List<ReceiptService> internalReceiptServices = template.executeQuery(GET_RECEIPT_SP_SQL, (resultSet, rowNumber) -> {
                    ReceiptService receiptService = new ReceiptService();
                    receiptService.setReceiptToServiceId(resultSet.getInt(1));
                    receiptService.setService(resultSet.getString(2));
                    receiptService.setTenantId(resultSet.getInt(3));
                    receiptService.setSpDisplayName(resultSet.getString(4));
                    receiptService.setSpDescription(resultSet.getString(5));
                    return receiptService;
                }, preparedStatement -> preparedStatement.setString(1, consentReceiptId));

                if (internalReceiptServices != null) {
                    internalReceiptServices.forEach(rethrowConsumer(receiptService -> receiptService.setPurposes
                            (getPurposeInfoOfService(receiptService.getReceiptToServiceId(), consentReceiptId, receiptContext))));
                }
                return internalReceiptServices;
            });
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_RETRIEVE_RECEIPT_INFO,
                                                     consentReceiptId, e);
        }
        return receiptServices;
    }

    private List<ConsentPurpose> getPurposeInfoOfService(int receiptToServiceId, String consentReceiptId,
                                                         ReceiptContext receiptContext)
            throws ConsentManagementException {

        List<ConsentPurpose> consentPurposes;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            consentPurposes = jdbcTemplate.withTransaction(template -> {
                List<ConsentPurpose> internalConsentPurposes = jdbcTemplate.executeQuery(GET_SP_PURPOSE_SQL,
                                                                                         (resultSet, rowNumber) -> {
                    ConsentPurpose consentPurpose = new ConsentPurpose();
                    consentPurpose.setServiceToPurposeId(resultSet.getInt(1));
                    consentPurpose.setConsentType(resultSet.getString(2));
                    consentPurpose.setPrimaryPurpose(resultSet.getInt(3) == 1);
                    consentPurpose.setTermination(resultSet.getString(4));
                    consentPurpose.setThirdPartyDisclosure(resultSet.getInt(5) == 1);
                    consentPurpose.setThirdPartyName(resultSet.getString(6));
                    consentPurpose.setPurpose(resultSet.getString(7));
                    consentPurpose.setPurposeDescription(resultSet.getString(8));
                    consentPurpose.setPurposeId(resultSet.getInt(9));
                    return consentPurpose;
                }, preparedStatement -> preparedStatement.setInt(1, receiptToServiceId));

                if (internalConsentPurposes != null) {
                    internalConsentPurposes.forEach(rethrowConsumer(consentPurpose -> {
                        consentPurpose.setPiiCategory(getPIICategoryInfoOfPurpose(
                                consentPurpose.getServiceToPurposeId(), consentReceiptId, receiptContext));
                        consentPurpose.setPurposeCategory(getPurposeCategoryInfoOfPurpose(
                                consentPurpose.getServiceToPurposeId(), consentReceiptId));
                    }));
                }
                return internalConsentPurposes;
            });
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_RETRIEVE_PURPOSE_INFO,
                                                     consentReceiptId, e);
        }
        return consentPurposes;
    }

    private List<PIICategoryValidity> getPIICategoryInfoOfPurpose(int serviceToPurposeId, String consentReceiptId,
                                                                  ReceiptContext receiptContext) throws
            ConsentManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.withTransaction(template -> template.executeQuery(GET_PII_CAT_SQL,
                    ((resultSet, rowNumber) -> {
                        String name = resultSet.getString(1);
                        boolean isSensitive = resultSet.getInt(2) == 1;
                        String validity = resultSet.getString(3);
                        int id = resultSet.getInt(4);
                        String displayName = resultSet.getString(5);
                        if (isSensitive) {
                            receiptContext.getSecretPIICategory().addSecretCategory(name);
                        }
                        return new PIICategoryValidity(name, validity, id, displayName);
                    }), preparedStatement -> preparedStatement.setInt(1, serviceToPurposeId)));
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_RETRIEVE_RECEIPT_INFO,
                                                     consentReceiptId, e);
        }
    }

    private List<String> getPurposeCategoryInfoOfPurpose(int serviceToPurposeId, String consentReceiptId) throws
            ConsentManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.withTransaction(template -> template.executeQuery(GET_PURPOSE_CAT_SQL,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    preparedStatement -> preparedStatement.setInt(1, serviceToPurposeId)
            ));
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_RETRIEVE_RECEIPT_INFO,
                                                     consentReceiptId, e);
        }
    }

    public void deleteReceipt(String receiptID) throws ConsentManagementException {

        Receipt receipt = getReceipt(receiptID);
        if (receipt == null) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_RECEIPT, String
                    .valueOf(receiptID));
        }
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {

                receipt.getServices().forEach(rethrowConsumer(receiptService -> {
                    int receiptToServiceId = receiptService.getReceiptToServiceId();
                    receiptService.getPurposes().forEach(rethrowConsumer(consentPurpose -> {
                        int serviceToPurposeId = consentPurpose.getServiceToPurposeId();
                        deleteSpPurposeToPiiCategoryAssociation(serviceToPurposeId);
                        deleteSpPurposeToPurposeCategoryAssociation(serviceToPurposeId);
                    }));
                    deleteSpToPurposeAssociation(receiptToServiceId);
                }));

                deleteReceiptSPAssociation(receiptID);
                deleteReceiptProperties(receiptID);
                deleteReceiptOnly(receiptID);

                return null;
            });
        } catch (TransactionException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_RECEIPT,
                    receiptID, e);
        }
    }

    /**
     * Delete all {@link Receipt} of a given principal tenant id.
     *
     * @param principalTenantId Id of the tenant
     * @throws ConsentManagementException
     */
    @Override
    public void deleteReceiptsByPrincipalTenantId(int principalTenantId) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_RECEIPTS_BY_PRINCIPAL_TENANT_ID_SQL,
                    preparedStatement -> preparedStatement.setInt(1, principalTenantId));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_RECEIPTS_BY_PRINCIPAL_TENANT_ID,
                    String.valueOf(principalTenantId), e);
        }
    }

    protected void deleteReceiptOnly(String receiptID) throws ConsentManagementServerException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Deleting receipt with ID: %s", receiptID));
        }
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_RECEIPT_SQL,
                    preparedStatement -> preparedStatement.setString(1, receiptID));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_RECEIPT, String
                    .valueOf(receiptID), e);
        }
    }

    protected void deleteReceiptProperties(String consentReceiptId) throws
            ConsentManagementServerException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Deleting receipt properties for receipt ID : %s", consentReceiptId));
        }
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_RECEIPT_PROPERTIES_SQL,
                    preparedStatement -> preparedStatement.setString(1, consentReceiptId));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_RECEIPT, String
                    .valueOf(consentReceiptId), e);
        }
    }

    protected void deleteSpPurposeToPiiCategoryAssociation(int spToPurposeAssocId) throws
            ConsentManagementServerException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Deleting SP, Purpose and Pii Category association with ID: %d",
                    spToPurposeAssocId));
        }
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_SP_PURPOSE_TO_PII_CAT_ASSOC_SQL,
                    preparedStatement -> preparedStatement.setInt(1, spToPurposeAssocId));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_RECEIPT, String
                    .valueOf(spToPurposeAssocId), e);
        }
    }

    protected void deleteSpPurposeToPurposeCategoryAssociation(int spToPurposeAssocId) throws
            ConsentManagementServerException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Deleting SP, Purpose and Purpose Category association with ID: %d",
                    spToPurposeAssocId));
        }
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_SP_PURPOSE_TO_PURPOSE_CAT_ASSOC_SQL,
                    preparedStatement -> preparedStatement.setInt(1, spToPurposeAssocId));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_RECEIPT, String
                    .valueOf(spToPurposeAssocId), e);
        }
    }

    protected void deleteReceiptSPAssociation(String receiptID) throws
            ConsentManagementServerException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Deleting Receipt and SP association with ID: %s", receiptID));
        }
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_RECEIPT_SP_ASSOC_SQL,
                    preparedStatement -> preparedStatement.setString(1, receiptID));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_RECEIPT, String
                    .valueOf(receiptID), e);
        }
    }

    protected void deleteSpToPurposeAssociation(int receiptToSPAssocId) throws
            ConsentManagementServerException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Deleting SP to Purpose Association with id %d", receiptToSPAssocId));
        }
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_SP_TO_PURPOSE_ASSOC_SQL,
                    preparedStatement -> preparedStatement.setInt(1, receiptToSPAssocId));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_RECEIPT, String
                    .valueOf(receiptToSPAssocId), e);
        }
    }
}
