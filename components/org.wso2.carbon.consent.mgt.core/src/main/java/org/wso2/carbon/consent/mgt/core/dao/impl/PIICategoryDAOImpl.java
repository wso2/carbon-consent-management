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
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;
import org.wso2.carbon.consent.mgt.core.util.FilterQueryBuilder;
import org.wso2.carbon.consent.mgt.core.util.FilterQueriesUtil;
import org.wso2.carbon.consent.mgt.core.util.JdbcUtils;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.FilterConstants;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PII_CATEGORY_BY_TENANT_ID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PII_CATEGORY_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PII_CATEGORY_BY_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_COUNT_ASSOCIATED_WITH_PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_VERSION_COUNT_ASSOCIATED_WITH_PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_SP_PURPOSE_COUNT_ASSOCIATED_WITH_PII_CATEGORY;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_PII_CATEGORY_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_PII_CATEGORY_WITH_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_FILTERED_PII_CATEGORY_DB2;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_FILTERED_PII_CATEGORY_MSSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_FILTERED_PII_CATEGORY_MYSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_FILTERED_PII_CATEGORY_ORACLE;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_DB2;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_INFORMIX;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_MSSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_MYSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_ORACLE;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SELECT_PII_CATEGORY_BY_ID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SELECT_PII_CATEGORY_BY_ID_WITH_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SELECT_PII_CATEGORY_BY_NAME_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.UPDATE_PII_CATEGORY_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isDB2DB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isH2MySqlOrPostgresDB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isInformixDB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isMSSqlDB;

/**
 * Default implementation of {@link PIICategoryDAO}. This handles {@link PIICategory} related DB operations.
 */
public class PIICategoryDAOImpl implements PIICategoryDAO {

    private static final Map<String, String> ELEMENT_ATTR_COL_MAP;

    static {
        ELEMENT_ATTR_COL_MAP = new LinkedHashMap<>();
        ELEMENT_ATTR_COL_MAP.put(FilterConstants.FILTER_ATTR_NAME, FilterConstants.DB_COL_NAME);
        ELEMENT_ATTR_COL_MAP.put(FilterConstants.FILTER_ATTR_AFTER, FilterConstants.DB_COL_ID);
        ELEMENT_ATTR_COL_MAP.put(FilterConstants.FILTER_ATTR_BEFORE, FilterConstants.DB_COL_ID);
    }

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
    public PIICategory addPIICategoryWithUuid(PIICategory piiCategory) throws ConsentManagementException {

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
        return new PIICategory(insertedId, piiCategory.getName(), piiCategory.getDescription(),
                piiCategory.getSensitive(), piiCategory.getTenantId(), piiCategory.getDisplayName(), uuid);
    }

    @Override
    public void updatePIICategoryUuid(int id, String uuid) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(UPDATE_PII_CATEGORY_UUID_SQL, preparedStatement -> {
                preparedStatement.setString(1, uuid);
                preparedStatement.setInt(2, id);
            });
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_UPDATE_PII_CATEGORY,
                    String.valueOf(id), e);
        }
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
    public PIICategory getPIICategoryByIdWithUuid(int id) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        PIICategory piiCategory;

        try {
            piiCategory = jdbcTemplate.fetchSingleRecord(SELECT_PII_CATEGORY_BY_ID_WITH_UUID_SQL,
                    (resultSet, rowNumber) -> new PIICategory(resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getInt(4) == 1,
                            resultSet.getInt(5),
                            resultSet.getString(6),
                            resultSet.getString(7)),
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
    public List<PIICategory> listPIICategories(List<ExpressionNode> expressionNodes, int limit, int tenantId)
            throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        List<PIICategory> categories;
        try {
            FilterQueryBuilder filterQueryBuilder =
                    FilterQueriesUtil.buildFilterQueryBuilder(expressionNodes, ELEMENT_ATTR_COL_MAP);
            String filterQuery = filterQueryBuilder.getFilterQuery();
            Map<Integer, String> filterParams = filterQueryBuilder.getFilterAttributeValue();

            String query;
            if (isH2MySqlOrPostgresDB()) {
                query = String.format(LIST_FILTERED_PII_CATEGORY_MYSQL, filterQuery);
            } else if (isDB2DB()) {
                query = String.format(LIST_FILTERED_PII_CATEGORY_DB2, filterQuery);
            } else if (isMSSqlDB()) {
                query = String.format(LIST_FILTERED_PII_CATEGORY_MSSQL, filterQuery);
            } else if (isInformixDB()) {
                throw new ConsentManagementServerException("This method is not supported for Informix database.",
                        ErrorMessages.ERROR_CODE_DATABASE_QUERY_PERFORMING.getCode());
            } else {
                query = String.format(LIST_FILTERED_PII_CATEGORY_ORACLE, filterQuery);
            }

            categories = jdbcTemplate.executeQuery(query,
                    (resultSet, rowNumber) -> new PIICategory(resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getInt(4) == 1,
                            resultSet.getInt(5),
                            resultSet.getString(6),
                            resultSet.getString(7)),
                    preparedStatement -> {
                        int paramIndex = 1;
                        preparedStatement.setInt(paramIndex++, tenantId);
                        for (String paramValue : filterParams.values()) {
                            preparedStatement.setString(paramIndex++, paramValue);
                        }
                        preparedStatement.setInt(paramIndex, limit);
                    });
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException(
                    String.format("Error listing PII categories with cursor for tenant %d", tenantId),
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

    private boolean isPiiCategoryUsedInPurposeVersion(int id) {

        try {
            return isAssociationExists(id, GET_PURPOSE_VERSION_COUNT_ASSOCIATED_WITH_PII_CATEGORY);
        } catch (DataAccessException e) {
            // CM_PURPOSE_VERSION_PII_CAT_ASSOC table is only present in the extended schema.
            // If the table does not exist, the PII category cannot be used in any version.
            return false;
        }
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
