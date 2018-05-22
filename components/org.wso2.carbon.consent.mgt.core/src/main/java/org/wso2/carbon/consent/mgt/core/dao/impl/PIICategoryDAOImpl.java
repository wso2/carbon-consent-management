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

import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;
import org.wso2.carbon.consent.mgt.core.util.JdbcUtils;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;

import java.util.List;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PII_CATEGORY_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_COUNT_ASSOCIATED_WITH_PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_SP_PURPOSE_COUNT_ASSOCIATED_WITH_PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_PII_CATEGORY_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_DB2;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_INFORMIX;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_MSSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_MYSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_ORACLE;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SELECT_PII_CATEGORY_BY_ID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SELECT_PII_CATEGORY_BY_NAME_SQL;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isDB2DB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isH2MySqlOrPostgresDB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isInformixDB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isMSSqlDB;

/**
 * Default implementation of {@link PIICategoryDAO}. This handles {@link PIICategory} related DB operations.
 */
public class PIICategoryDAOImpl implements PIICategoryDAO {

    public PIICategoryDAOImpl() {

    }

    @Override
    public int getPriority() {

        return 1;
    }

    @Override
    public PIICategory addPIICategory(PIICategory piiCategory) throws ConsentManagementException {

        PIICategory purposeResult;
        int insertedId;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            insertedId = jdbcTemplate.executeInsert(INSERT_PII_CATEGORY_SQL, (preparedStatement -> {
                preparedStatement.setString(1, piiCategory.getName());
                preparedStatement.setString(2, piiCategory.getDescription());
                preparedStatement.setInt(3, piiCategory.getSensitive() ? 1 : 0);
                preparedStatement.setInt(4, piiCategory.getTenantId());
                preparedStatement.setString(5, piiCategory.getDisplayName());
            }), piiCategory, true);
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_PII_CATEGORY,
                                                     piiCategory.getName(), e);
        }
        purposeResult = new PIICategory(insertedId, piiCategory.getName(), piiCategory.getDescription(),
                piiCategory.getSensitive(), piiCategory.getTenantId(), piiCategory.getDisplayName());
        return purposeResult;
    }

    @Override
    public PIICategory getPIICategoryById(int id) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        PIICategory piiCategory;

        try {
            piiCategory = jdbcTemplate.fetchSingleRecord(SELECT_PII_CATEGORY_BY_ID_SQL, (resultSet, rowNumber) ->
                            new PIICategory(resultSet.getInt(1),
                                    resultSet.getString(2),
                                    resultSet.getString(3),
                                    resultSet.getInt(4) == 1,
                                    resultSet.getInt(5),
                                    resultSet.getString(6)),
                    preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PII_CATEGORY_BY_ID, String
                    .valueOf(id), e);
        }

        return piiCategory;
    }

    @Override
    public List<PIICategory> listPIICategories(int limit, int offset, int tenantId) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        List<PIICategory> piiCategories;

        try {
            String query;
            if (isH2MySqlOrPostgresDB()) {
                query = LIST_PAGINATED_PII_CATEGORY_MYSQL;
            } else if (isDB2DB()) {
                query = LIST_PAGINATED_PII_CATEGORY_DB2;
                int initialOffset = offset;
                offset = offset + limit;
                limit = initialOffset + 1;
            } else if (isMSSqlDB()) {
                query = LIST_PAGINATED_PII_CATEGORY_MSSQL;
                int initialOffset = offset;
                offset = limit + offset;
                limit = initialOffset + 1;
            } else if (isInformixDB()) {
                query = LIST_PAGINATED_PII_CATEGORY_INFORMIX;
            } else {
                //oracle
                query = LIST_PAGINATED_PII_CATEGORY_ORACLE;
                limit = offset + limit;
            }
            int finalLimit = limit;
            int finalOffset = offset;

            piiCategories = jdbcTemplate.executeQuery(query,
                    (resultSet, rowNumber) -> new PIICategory(resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getInt(4) == 1,
                            resultSet.getInt(5),
                            resultSet.getString(6)),
                    preparedStatement -> {
                        preparedStatement.setInt(1, tenantId);
                        preparedStatement.setInt(2, finalLimit);
                        preparedStatement.setInt(3, finalOffset);
                    });
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException(String.format(ErrorMessages.ERROR_CODE_LIST_PII_CATEGORY
                    .getMessage(), limit, offset),
                    ErrorMessages.ERROR_CODE_LIST_PII_CATEGORY.getCode(), e);
        }
        return piiCategories;
    }

    @Override
    public int deletePIICategory(int id) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_PII_CATEGORY_SQL, preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_PII_CATEGORY, String
                    .valueOf(id), e);
        }
        return id;
    }

    @Override
    public PIICategory getPIICategoryByName(String name, int tenantId) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        PIICategory piiCategory;

        try {
            piiCategory = jdbcTemplate.fetchSingleRecord(SELECT_PII_CATEGORY_BY_NAME_SQL, (resultSet, rowNumber) ->
                            new PIICategory(resultSet.getInt(1),
                                    resultSet.getString(2),
                                    resultSet.getString(3),
                                    resultSet.getInt(4) == 1,
                                    resultSet.getInt(5),
                                    resultSet.getString(6)),
                    preparedStatement -> {
                        preparedStatement.setString(1, name);
                        preparedStatement.setInt(2, tenantId);
                    });
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PII_CATEGORY_BY_NAME, name, e);
        }
        return piiCategory;
    }

    /**
     * Check whether the {@link PIICategory} by ID is used in a purpose or service
     *
     * @param id ID of the {@link PIICategory} to be validated
     * @return true if PII category is used, false otherwise.
     */
    @Override
    public boolean isPIICategoryUsed(int id) throws ConsentManagementException {
        try {
            return isPiiCategoryUsedInPurpose(id) || isPiiCategoryUsedInSP(id);
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages
                    .ERROR_CODE_RETRIEVE_SP_PURPOSE_ASSOCIATED_WITH_PIICATERY, String.valueOf(id), e);
        }
    }

    private boolean isPiiCategoryUsedInPurpose(int id) throws DataAccessException {
        return isAssociationExists(id, GET_PURPOSE_COUNT_ASSOCIATED_WITH_PII_CATEGORY);
    }

    private boolean isPiiCategoryUsedInSP(int id) throws DataAccessException {
        return isAssociationExists(id, GET_SP_PURPOSE_COUNT_ASSOCIATED_WITH_PII_CATEGORY);
    }

    private boolean isAssociationExists(int id, String query) throws DataAccessException {
        Integer count;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        count = jdbcTemplate.fetchSingleRecord(query, (resultSet, rowNumber) ->
                        resultSet.getInt(1),
                preparedStatement -> preparedStatement.setInt(1, id));
        if (count == null) {
            return false;
        }
        return (count > 0);
    }
}
