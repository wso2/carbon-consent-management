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
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.dao.JdbcTemplate;
import org.wso2.carbon.consent.mgt.core.dao.ReceiptDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.exception.DataAccessException;
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.DB2;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.H2;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.INFORMIX;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.MY_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.POSTGRE_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.S_MICROSOFT;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_ACTIVE_RECEIPTS_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PII_CAT_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_CAT_SQL;
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
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_TENANT;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_TENANT_DB2;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_TENANT_INFORMIX;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_TENANT_MSSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SEARCH_RECEIPT_SQL_WITHOUT_TENANT_ORACLE;
import static org.wso2.carbon.consent.mgt.core.util.LambdaExceptionUtils.rethrowConsumer;

/**
 * Default implementation of {@link ReceiptDAO}. This handles {@link Receipt} related DB operations.
 */
public class ReceiptDAOImpl implements ReceiptDAO {

    private final JdbcTemplate jdbcTemplate;
    private static final String SQL_FILTER_STRING_ANY = "%";
    private static final String QUERY_FILTER_STRING_ANY = "*";
    private static final String QUERY_FILTER_STRING_ANY_ESCAPED = "\\*";
    private static final Log log = LogFactory.getLog(ReceiptDAOImpl.class);

    public ReceiptDAOImpl(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int getPriority() {

        return 1;
    }

    @Override
    public void addReceipt(ReceiptInput receiptInput) throws ConsentManagementException {

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

        addReceiptProperties(receiptInput.getConsentReceiptId(), receiptInput.getProperties());
    }

    private void revokeActiveReceipts(ReceiptInput receiptInput) {

        receiptInput.getServices().forEach(rethrowConsumer(receiptServiceInput -> {
            try {
                List<String> ids = jdbcTemplate.executeQuery(GET_ACTIVE_RECEIPTS_SQL, (resultSet, rowNumber) -> resultSet
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
            } catch (DataAccessException e) {
                throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages.ERROR_CODE_REVOKE_ACTIVE_RECEIPT,
                        receiptInput.getPiiPrincipalId(), e);
            }
        }));
    }

    @Override
    public Receipt getReceipt(String receiptId) throws ConsentManagementException {

        ReceiptContext receiptContext = new ReceiptContext();
        Receipt receipt;
        try {
            receipt = jdbcTemplate.fetchSingleRecord(GET_RECEIPT_SQL, (resultSet, rowNumber) -> {
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
                return receiptInfo;
            }, preparedStatement -> preparedStatement.setString(1, receiptId));

            if (receipt != null) {
                receipt.setServices(getServiceInfoOfReceipt(receiptId, receiptContext));
                setReceiptSensitivity(receiptContext, receipt);
            }
            return receipt;
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages.ERROR_CODE_RETRIEVE_RECEIPT_INFO,
                    String.valueOf(receiptId), e);
        }
    }

    @Override
    public void revokeReceipt(String receiptId) throws ConsentManagementException {

        try {
            jdbcTemplate.executeUpdate(REVOKE_RECEIPT_SQL, preparedStatement -> {
                preparedStatement.setString(1, ConsentConstants.REVOKE_STATE);
                preparedStatement.setString(2, receiptId);
            });
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages.ERROR_CODE_REVOKE_RECEIPT,
                    receiptId, e);
        }
    }

    @Override
    public List<ReceiptListResponse> searchReceipts(int limit, int offset, String piiPrincipalId, int
            spTenantId, String service, String state) throws ConsentManagementException {

        List<ReceiptListResponse> receiptListResponses;
        try {
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

            if (spTenantId != 0) { // Tenant domain is used for search results.

                if (isMysqlH2PostgresDB()) {
                    query = SEARCH_RECEIPT_SQL;
                } else if (isDatabaseDB2()) {
                    query = SEARCH_RECEIPT_SQL_DB2;
                    offset = offset + limit;
                } else if (isMssqlDB()) {
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
                            preparedStatement.setString(2, finalService);
                            preparedStatement.setInt(3, spTenantId);
                            preparedStatement.setString(4, finalState);
                            preparedStatement.setInt(5, finalLimit);
                            preparedStatement.setInt(6, finalOffset);
                        });
            } else {
                if (isMysqlH2PostgresDB()) {
                    query = SEARCH_RECEIPT_SQL_WITHOUT_TENANT;
                } else if (isDB2()) {
                    query = SEARCH_RECEIPT_SQL_WITHOUT_TENANT_DB2;
                    offset = offset + limit;
                } else if (isMssqlDB()) {
                    query = SEARCH_RECEIPT_SQL_WITHOUT_TENANT_MSSQL;
                } else if (isInformixDB()) {
                    query = SEARCH_RECEIPT_SQL_WITHOUT_TENANT_INFORMIX;
                } else {
                    //oracle
                    query = SEARCH_RECEIPT_SQL_WITHOUT_TENANT_ORACLE;
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
            }
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages.ERROR_CODE_SEARCH_RECEIPTS,
                    piiPrincipalId, e);
        }
        return receiptListResponses;
    }

    protected void setReceiptSensitivity(ReceiptContext receiptContext, Receipt receipt) {

        if (receiptContext.getSecretPIICategory().getSecretPIICategories().size() > 0) {
            receipt.setSensitive(true);
            receipt.setSpiCat(receiptContext.getSecretPIICategory().getSecretPIICategories());
        }
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
                preparedStatement.setInt(8, receiptInput.getTenantId());
                preparedStatement.setString(9, receiptInput.getPolicyUrl());
                preparedStatement.setString(10, ConsentConstants.ACTIVE_STATE);

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
                preparedStatement.setInt(3, receiptServiceInput.getTenantId());
                preparedStatement.setString(4, receiptServiceInput.getSpDisplayName());
                preparedStatement.setString(5, receiptServiceInput.getSpDescription());
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
                    String.valueOf(receiptPurposeInput.getPurposeName()), e);
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

    protected void addSpPurposeToPiiCategoryAssociation(int spToPurposeAssocId, int id, String validity) throws
            ConsentManagementServerException {

        try {
            jdbcTemplate.executeInsert(INSERT_SP_PURPOSE_TO_PII_CAT_ASSOC_SQL, (preparedStatement -> {
                preparedStatement.setInt(1, spToPurposeAssocId);
                preparedStatement.setInt(2, id);
                preparedStatement.setString(3, validity);
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

    protected List<ReceiptService> getServiceInfoOfReceipt(String consentReceiptId, ReceiptContext receiptContext) throws
            ConsentManagementServerException {

        List<ReceiptService> receiptServices;

        try {
            receiptServices = jdbcTemplate.executeQuery(GET_RECEIPT_SP_SQL, (resultSet, rowNumber) -> {
                ReceiptService receiptService = new ReceiptService();
                receiptService.setReceiptToServiceId(resultSet.getInt(1));
                receiptService.setService(resultSet.getString(2));
                receiptService.setTenantId(resultSet.getInt(3));
                receiptService.setSpDisplayName(resultSet.getString(4));
                receiptService.setSpDescription(resultSet.getString(5));
                return receiptService;
            }, preparedStatement -> preparedStatement.setString(1, consentReceiptId));

            if (receiptServices != null) {
                receiptServices.forEach(rethrowConsumer(receiptService -> receiptService.setPurposes
                        (getPurposeInfoOfService(receiptService.getReceiptToServiceId(), consentReceiptId, receiptContext))));
            }
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages.ERROR_CODE_RETRIEVE_RECEIPT_INFO,
                    consentReceiptId, e);
        }
        return receiptServices;
    }

    private List<ConsentPurpose> getPurposeInfoOfService(int receiptToServiceId, String consentReceiptId,
                                                         ReceiptContext receiptContext)
            throws ConsentManagementException {

        List<ConsentPurpose> consentPurposes;
        try {
            consentPurposes = jdbcTemplate.executeQuery(GET_SP_PURPOSE_SQL, (resultSet, rowNumber) -> {
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

            if (consentPurposes != null) {
                consentPurposes.forEach(rethrowConsumer(consentPurpose -> {
                    consentPurpose.setPiiCategory(getPIICategoryInfoOfPurpose(consentPurpose.getServiceToPurposeId(),
                            consentReceiptId, receiptContext));
                    consentPurpose.setPurposeCategory(getPurposeCategoryInfoOfPurpose(consentPurpose.getServiceToPurposeId(),
                            consentReceiptId));
                }));
            }
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages.ERROR_CODE_RETRIEVE_PURPOSE_INFO,
                    consentReceiptId, e);
        }
        return consentPurposes;
    }

    private List<PIICategoryValidity> getPIICategoryInfoOfPurpose(int serviceToPurposeId, String consentReceiptId,
                                                                  ReceiptContext receiptContext) throws
            ConsentManagementServerException {

        try {
            return jdbcTemplate.executeQuery(GET_PII_CAT_SQL, ((resultSet, rowNumber) -> {

                String name = resultSet.getString(1);
                boolean isSensitive = resultSet.getInt(2) == 1;
                String validity = resultSet.getString(3);
                int id = resultSet.getInt(4);
                if (isSensitive) {
                    receiptContext.getSecretPIICategory().addSecretCategory(name);
                }
                return new PIICategoryValidity(name, validity, id);
            }), preparedStatement -> preparedStatement.setInt(1, serviceToPurposeId));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages.ERROR_CODE_RETRIEVE_RECEIPT_INFO,
                    consentReceiptId, e);
        }
    }

    private List<String> getPurposeCategoryInfoOfPurpose(int serviceToPurposeId, String consentReceiptId) throws
            ConsentManagementServerException {

        try {
            return jdbcTemplate.executeQuery(GET_PURPOSE_CAT_SQL, (resultSet, rowNumber) -> resultSet
                    .getString(1), preparedStatement -> preparedStatement.setInt(1, serviceToPurposeId)
            );
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages.ERROR_CODE_RETRIEVE_RECEIPT_INFO,
                    consentReceiptId, e);
        }
    }

    private boolean isMysqlH2PostgresDB() throws DataAccessException {

        return jdbcTemplate.getDriverName().contains(MY_SQL) || jdbcTemplate.getDriverName().contains(H2) ||
                jdbcTemplate.getDriverName().contains(POSTGRE_SQL);
    }

    private boolean isDatabaseDB2() throws DataAccessException {

        return jdbcTemplate.getDriverName().contains(DB2);
    }

    private boolean isDB2() throws DataAccessException {

        return jdbcTemplate.getDriverName().contains(DB2);
    }

    private boolean isMssqlDB() throws DataAccessException {

        return jdbcTemplate.getDriverName().contains(ConsentConstants.MICROSOFT) || jdbcTemplate.getDriverName()
                .contains(S_MICROSOFT);
    }

    private boolean isInformixDB() throws DataAccessException {

        return jdbcTemplate.getDriverName().contains(INFORMIX);
    }
}
