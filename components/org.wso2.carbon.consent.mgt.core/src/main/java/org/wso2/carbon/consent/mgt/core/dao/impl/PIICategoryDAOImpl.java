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
import org.wso2.carbon.consent.mgt.core.util.FilterSqlBuilder;
import org.wso2.carbon.consent.mgt.core.util.JdbcUtils;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.model.Node;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages;
import java.util.UUID;

import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PII_CATEGORY_BY_TENANT_ID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PII_CATEGORY_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PII_CATEGORY_BY_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_COUNT_ASSOCIATED_WITH_PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_VERSION_COUNT_ASSOCIATED_WITH_PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_SP_PURPOSE_COUNT_ASSOCIATED_WITH_PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_PII_CATEGORY_WITH_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_DB2_WITH_UUID;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_INFORMIX_WITH_UUID;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_MSSQL_WITH_UUID;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_MYSQL_WITH_UUID;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_ORACLE_WITH_UUID;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SELECT_PII_CATEGORY_BY_ID_WITH_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SELECT_PII_CATEGORY_BY_NAME_WITH_UUID_SQL;
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
        String uuid = UUID.randomUUID().toString();
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            insertedId = jdbcTemplate.executeInsert(INSERT_PII_CATEGORY_WITH_UUID_SQL, (preparedStatement -> {
                preparedStatement.setString(1, piiCategory.getName());
                preparedStatement.setString(2, piiCategory.getDescription());
                preparedStatement.setInt(3, piiCategory.getSensitive() ? 1 : 0);
                preparedStatement.setInt(4, piiCategory.getTenantId());
                preparedStatement.setString(5, piiCategory.getDisplayName());
                preparedStatement.setString(6, uuid);
            }), piiCategory, true);
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_PII_CATEGORY,
                                                     piiCategory.getName(), e);
        }
        purposeResult = new PIICategory(insertedId, piiCategory.getName(), piiCategory.getDescription(),
                piiCategory.getSensitive(), piiCategory.getTenantId(), piiCategory.getDisplayName(), uuid);
        return purposeResult;
    }

    @Override
    public PIICategory getPIICategoryById(int id) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        PIICategory piiCategory;

        try {
            piiCategory = jdbcTemplate.fetchSingleRecord(SELECT_PII_CATEGORY_BY_ID_WITH_UUID_SQL,
                    (resultSet, rowNumber) -> {
                        PIICategory cat = new PIICategory(resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getInt(4) == 1,
                                resultSet.getInt(5),
                                resultSet.getString(6),
                                resultSet.getString(7));
                        return cat;
                    },
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
                query = LIST_PAGINATED_PII_CATEGORY_MYSQL_WITH_UUID;
            } else if (isDB2DB()) {
                query = LIST_PAGINATED_PII_CATEGORY_DB2_WITH_UUID;
                int initialOffset = offset;
                offset = offset + limit;
                limit = initialOffset + 1;
            } else if (isMSSqlDB()) {
                query = LIST_PAGINATED_PII_CATEGORY_MSSQL_WITH_UUID;
                int initialOffset = offset;
                offset = limit + offset;
                limit = initialOffset + 1;
            } else if (isInformixDB()) {
                query = LIST_PAGINATED_PII_CATEGORY_INFORMIX_WITH_UUID;
            } else {
                //oracle
                query = LIST_PAGINATED_PII_CATEGORY_ORACLE_WITH_UUID;
                limit = offset + limit;
            }
            int finalLimit = limit;
            int finalOffset = offset;

            piiCategories = jdbcTemplate.executeQuery(query,
                    (resultSet, rowNumber) -> {
                        PIICategory cat = new PIICategory(resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getInt(4) == 1,
                                resultSet.getInt(5),
                                resultSet.getString(6),
                                resultSet.getString(7));
                        return cat;
                    },
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
    public List<PIICategory> listPIICategories(Node filterTree, int limit, int offset, int tenantId)
            throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        List<PIICategory> categories;
        try {
            // Build filter WHERE clause if filterTree provided
            List<Object> filterParams = new ArrayList<>();
            String filterWhereClause = "";
            if (filterTree != null) {
                FilterSqlBuilder filterSqlBuilder = new FilterSqlBuilder();
                String builtClause = filterSqlBuilder.buildWhereClause(filterTree, filterParams);
                if (builtClause != null && !builtClause.isEmpty()) {
                    filterWhereClause = " AND " + builtClause;
                }
            }

            // Determine database type and select appropriate query
            String query;
            int finalLimit = limit;
            int finalOffset = offset;

            if (isH2MySqlOrPostgresDB()) {
                query = "SELECT ID, NAME, DESCRIPTION, IS_SENSITIVE, TENANT_ID, DISPLAY_NAME, UUID " +
                        "FROM CM_PII_CATEGORY WHERE TENANT_ID = ?" + filterWhereClause +
                        " ORDER BY ID ASC LIMIT ? OFFSET ?";
            } else if (isDB2DB()) {
                finalOffset = offset + limit;
                finalLimit = offset + 1;
                query = "SELECT ID, NAME, DESCRIPTION, IS_SENSITIVE, TENANT_ID, DISPLAY_NAME, UUID " +
                        "FROM (SELECT ROW_NUMBER() OVER (ORDER BY ID) AS rn, p.* " +
                        "FROM CM_PII_CATEGORY AS p WHERE p.TENANT_ID = ?" + filterWhereClause + ") " +
                        "WHERE rn BETWEEN ? AND ?";
            } else if (isMSSqlDB()) {
                finalOffset = limit + offset;
                finalLimit = offset + 1;
                query = "SELECT ID, NAME, DESCRIPTION, IS_SENSITIVE, TENANT_ID, DISPLAY_NAME, UUID " +
                        "FROM (SELECT ID, NAME, DESCRIPTION, IS_SENSITIVE, TENANT_ID, DISPLAY_NAME, UUID, " +
                        "ROW_NUMBER() OVER (ORDER BY ID) AS RowNum FROM CM_PII_CATEGORY " +
                        "WHERE TENANT_ID = ?" + filterWhereClause + ") AS P " +
                        "WHERE P.RowNum BETWEEN ? AND ?";
            } else if (isInformixDB()) {
                // Informix
                throw new ConsentManagementServerException("This method is not supported for Informix database.",
                        ErrorMessages.ERROR_CODE_DATABASE_QUERY_PERFORMING.getCode());
            } else {
                // Oracle
                finalLimit = offset + limit;
                query = "SELECT ID, NAME, DESCRIPTION, IS_SENSITIVE, TENANT_ID, DISPLAY_NAME, UUID " +
                        "FROM (SELECT ROW_NUMBER() OVER (ORDER BY ID) AS RN, ID, NAME, " +
                        "DESCRIPTION, IS_SENSITIVE, TENANT_ID, DISPLAY_NAME, UUID " +
                        "FROM CM_PII_CATEGORY WHERE TENANT_ID = ?" + filterWhereClause +
                        ") WHERE RN <= ? AND RN > ?";
            }

            final int paramLimit = finalLimit;
            final int paramOffset = finalOffset;
            final List<Object> finalFilterParams = filterParams;

            categories = jdbcTemplate.executeQuery(query,
                    (resultSet, rowNumber) -> {
                        PIICategory cat = new PIICategory(resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getInt(4) == 1,
                                resultSet.getInt(5),
                                resultSet.getString(6),
                                resultSet.getString(7));
                        return cat;
                    },
                    preparedStatement -> {
                        int paramIndex = 1;
                        preparedStatement.setInt(paramIndex++, tenantId);

                        // Set filter parameters
                        for (Object filterParam : finalFilterParams) {
                            preparedStatement.setObject(paramIndex++, filterParam);
                        }

                        preparedStatement.setInt(paramIndex++, paramLimit);
                        preparedStatement.setInt(paramIndex, paramOffset);
                    });
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException(
                    String.format("Error listing PII categories with filter for tenant %d", tenantId),
                    ErrorMessages.ERROR_CODE_LIST_PII_CATEGORY.getCode(), e);
        }
        return categories;
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

    /**
     * Delete all {@link PIICategory} of a given tenant id.
     *
     * @param tenantId Id of the tenant
     * @throws ConsentManagementException
     */
    @Override
    public void deletePIICategoriesByTenantId(int tenantId) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_PII_CATEGORY_BY_TENANT_ID_SQL,
                    preparedStatement -> preparedStatement.setInt(1, tenantId));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_PII_CATEGORIES_BY_TENANT_ID,
                    String.valueOf(tenantId), e);
        }
    }

    @Override
    public PIICategory getPIICategoryByName(String name, int tenantId) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        PIICategory piiCategory;

        try {
            piiCategory = jdbcTemplate.fetchSingleRecord(SELECT_PII_CATEGORY_BY_NAME_WITH_UUID_SQL,
                    (resultSet, rowNumber) -> {
                        PIICategory cat = new PIICategory(resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getInt(4) == 1,
                                resultSet.getInt(5),
                                resultSet.getString(6),
                                resultSet.getString(7));
                        return cat;
                    },
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
     * Check whether the {@link PIICategory} by ID is used in a purpose or service.
     *
     * @param id ID of the {@link PIICategory} to be validated
     * @return true if PII category is used, false otherwise.
     */
    @Override
    public boolean isPIICategoryUsed(int id) throws ConsentManagementException {
        try {
            return isPiiCategoryUsedInPurpose(id) || isPiiCategoryUsedInSP(id)
                    || isPiiCategoryUsedInPurposeVersion(id);
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

    private boolean isPiiCategoryUsedInPurposeVersion(int id) throws DataAccessException {
        return isAssociationExists(id, GET_PURPOSE_VERSION_COUNT_ASSOCIATED_WITH_PII_CATEGORY);
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

    @Override
    public PIICategory getPIICategoryByUuid(String uuid, int tenantId) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.fetchSingleRecord(GET_PII_CATEGORY_BY_UUID_SQL,
                    (resultSet, rowNumber) -> {
                        PIICategory cat = new PIICategory(resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getInt(4) == 1,
                                resultSet.getInt(5),
                                resultSet.getString(6),
                                resultSet.getString(7));
                        return cat;
                    },
                    preparedStatement -> {
                        preparedStatement.setString(1, uuid);
                        preparedStatement.setInt(2, tenantId);
                    });
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PII_CATEGORY_BY_ID, uuid, e);
        }
    }
}
