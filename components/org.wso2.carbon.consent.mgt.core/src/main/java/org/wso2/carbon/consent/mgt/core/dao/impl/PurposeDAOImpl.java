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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.dao.JdbcTemplate;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.exception.DataAccessException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.DB2;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.H2;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.INFORMIX;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.MY_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.POSTGRE_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.S_MICROSOFT;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PURPOSE_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_BY_ID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_BY_NAME_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_PII_CAT_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_RECEIPT_COUNT_ASSOCIATED_WITH_PURPOSE;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_PURPOSE_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_RECEIPT_PURPOSE_PII_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_DB2;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_INFORMIX;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_MSSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_MYSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_ORACLE;
import static org.wso2.carbon.consent.mgt.core.util.LambdaExceptionUtils.rethrowConsumer;

/**
 * Default implementation of {@link PurposeDAO}. This handles {@link Purpose} related DB operations.
 */
public class PurposeDAOImpl implements PurposeDAO {

    private final JdbcTemplate jdbcTemplate;

    public PurposeDAOImpl(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int getPriority() {

        return 1;
    }

    @Override
    public Purpose addPurpose(Purpose purpose) throws ConsentManagementException {

        Purpose purposeResult;
        int insertedId;

        try {
            insertedId = jdbcTemplate.executeInsert(INSERT_PURPOSE_SQL, (preparedStatement -> {
                preparedStatement.setString(1, purpose.getName());
                preparedStatement.setString(2, purpose.getDescription());
                preparedStatement.setInt(3, purpose.getTenantId());
            }), purpose, true);
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_PURPOSE, purpose.getName(), e);
        }

        purpose.getPiiCategoryIds().forEach(rethrowConsumer(id -> {
            try {
                jdbcTemplate.executeInsert(INSERT_RECEIPT_PURPOSE_PII_ASSOC_SQL, (preparedStatement -> {
                    preparedStatement.setInt(1, insertedId);
                    preparedStatement.setInt(2, id);
                }), id, false);
            } catch (DataAccessException e) {
                throw ConsentUtils.handleServerException(ConsentConstants.ErrorMessages.ERROR_CODE_ADD_PURPOSE_PII_ASSOC,
                        String.valueOf(insertedId), e);
            }
        }));
        purposeResult = new Purpose(insertedId, purpose.getName(), purpose.getDescription(), purpose.getTenantId(),
                purpose.getPiiCategoryIds());
        return purposeResult;
    }

    @Override
    public Purpose getPurposeById(int id) throws ConsentManagementException {

        if (id == 0) {
            throw ConsentUtils.handleClientException(ErrorMessages.ERROR_CODE_PURPOSE_ID_REQUIRED, null);
        }

        Purpose purpose;

        try {
            purpose = jdbcTemplate.fetchSingleRecord(GET_PURPOSE_BY_ID_SQL, (resultSet, rowNumber) ->
                            new Purpose(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3),
                                    resultSet.getInt(4)),
                    preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PURPOSE_BY_ID, String.valueOf(id), e);
        }

        if (purpose != null) {
            try {
                List<Integer> piiCategories = new ArrayList<>();
                jdbcTemplate.executeQuery(GET_PURPOSE_PII_CAT_SQL, (resultSet, rowNumber) ->
                                piiCategories.add(resultSet.getInt(1)),
                        preparedStatement -> preparedStatement.setInt(1, purpose.getId()));
                purpose.setPiiCategoryIds(piiCategories);
            } catch (DataAccessException e) {
                throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PURPOSE_BY_ID, String.valueOf(id), e);
            }
        }
        return purpose;
    }

    @Override
    public Purpose getPurposeByName(String name, int tenantId) throws ConsentManagementException {

        if (StringUtils.isBlank(name)) {
            throw ConsentUtils.handleClientException(ErrorMessages.ERROR_CODE_PURPOSE_NAME_REQUIRED, null);
        }

        Purpose purpose;

        try {
            purpose = jdbcTemplate.fetchSingleRecord(GET_PURPOSE_BY_NAME_SQL, (resultSet, rowNumber) ->
                            new Purpose(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3)),
                    preparedStatement -> {
                        preparedStatement.setString(1, name);
                        preparedStatement.setInt(2, tenantId);
                    });
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PURPOSE_BY_NAME, name, e);
        }
        return purpose;
    }

    @Override
    public List<Purpose> listPurposes(int limit, int offset, int tenantId) throws ConsentManagementException {

        List<Purpose> purposes;
        try {
            String query;
            if (isMysqlH2OrPostgresDB()) {
                query = LIST_PAGINATED_PURPOSE_MYSQL;
            } else if (isDatabaseDB2()) {
                query = LIST_PAGINATED_PURPOSE_DB2;
                int initialOffset = offset;
                offset = offset + limit;
                limit = initialOffset + 1;
            } else if (isMsSqlDB()) {
                int initialOffset = offset;
                offset = limit + offset;
                limit = initialOffset + 1;
                query = LIST_PAGINATED_PURPOSE_MSSQL;
            } else if (isInformixDB()) {
                query = LIST_PAGINATED_PURPOSE_INFORMIX;
            } else {
                //oracle
                query = LIST_PAGINATED_PURPOSE_ORACLE;
                limit = offset + limit;
            }
            int finalLimit = limit;
            int finalOffset = offset;

            purposes = jdbcTemplate.executeQuery(query,
                    (resultSet, rowNumber) -> new Purpose(resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getString(3)),
                    preparedStatement -> {
                        preparedStatement.setInt(1, tenantId);
                        preparedStatement.setInt(2, finalLimit);
                        preparedStatement.setInt(3, finalOffset);
                    });
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException(String.format(ErrorMessages.ERROR_CODE_LIST_PURPOSE.getMessage(),
                    limit, offset), ErrorMessages.ERROR_CODE_LIST_PURPOSE.getCode(), e);
        }
        return purposes;
    }

    @Override
    public int deletePurpose(int id) throws ConsentManagementException {

        try {
            jdbcTemplate.executeUpdate(DELETE_PURPOSE_SQL, preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_PURPOSE, String.valueOf(id), e);
        }

        return id;
    }

    /**
     * Check whether the {@link Purpose} by ID is used in a receipt
     *
     * @param id D of the {@link Purpose} to be validated
     * @return
     */
    @Override
    public boolean isPurposeUsed(int id) throws ConsentManagementServerException {

        int count;
        try {
            count = jdbcTemplate.fetchSingleRecord(GET_RECEIPT_COUNT_ASSOCIATED_WITH_PURPOSE, (resultSet, rowNumber) ->
                            resultSet.getInt(1),
                    preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages
                    .ERROR_CODE_RETRIEVE_RECEIPTS_ASSOCIATED_WITH_PURPOSE, String.valueOf(id), e);
        }
        return (count > 0);
    }

    private boolean isMysqlH2OrPostgresDB() throws DataAccessException {

        return jdbcTemplate.getDriverName().contains(MY_SQL) || jdbcTemplate.getDriverName().contains(H2) ||
                jdbcTemplate.getDriverName().contains(POSTGRE_SQL);
    }

    private boolean isDatabaseDB2() throws DataAccessException {

        return jdbcTemplate.getDriverName().contains(DB2);
    }

    private boolean isMsSqlDB() throws DataAccessException {

        return jdbcTemplate.getDriverName().contains(ConsentConstants.MICROSOFT) || jdbcTemplate.getDriverName()
                .contains(S_MICROSOFT);
    }

    private boolean isInformixDB() throws DataAccessException {

        return jdbcTemplate.getDriverName().contains(INFORMIX);
    }
}
