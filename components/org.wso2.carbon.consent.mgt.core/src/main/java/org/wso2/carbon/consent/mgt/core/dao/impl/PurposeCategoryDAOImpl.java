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

import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;
import org.wso2.carbon.consent.mgt.core.util.JdbcUtils;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;

import java.util.List;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PURPOSE_CATEGORY_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_PURPOSE_CATEGORY_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_CATEGORY_DB2;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_CATEGORY_INFORMIX;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_CATEGORY_MSSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_CATEGORY_MYSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_CATEGORY_ORACLE;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SELECT_PURPOSE_CATEGORY_BY_ID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SELECT_PURPOSE_CATEGORY_BY_NAME_SQL;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isDB2DB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isH2MySqlOrPostgresDB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isInformixDB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isMSSqlDB;

/**
 * Default implementation of {@link PurposeCategoryDAO}. This handles {@link PurposeCategory} related DB operations.
 */
public class PurposeCategoryDAOImpl implements PurposeCategoryDAO {

    public PurposeCategoryDAOImpl() {

    }

    @Override
    public int getPriority() {

        return 1;
    }

    @Override
    public PurposeCategory addPurposeCategory(PurposeCategory purposeCategory) throws ConsentManagementException {

        PurposeCategory purposeCategoryResult;
        int insertedId;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            insertedId = jdbcTemplate.executeInsert(INSERT_PURPOSE_CATEGORY_SQL, (preparedStatement -> {
                preparedStatement.setString(1, purposeCategory.getName());
                preparedStatement.setString(2, purposeCategory.getDescription());
                preparedStatement.setInt(3, purposeCategory.getTenantId());
            }), purposeCategory, true);
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_PURPOSE_CATEGORY, purposeCategory
                    .getName(), e);
        }
        purposeCategoryResult = new PurposeCategory(insertedId, purposeCategory.getName(),
                purposeCategory.getDescription(), purposeCategory.getTenantId());
        return purposeCategoryResult;
    }

    @Override
    public PurposeCategory getPurposeCategoryById(int id) throws ConsentManagementException {

        PurposeCategory purposeCategory;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            purposeCategory = jdbcTemplate.fetchSingleRecord(SELECT_PURPOSE_CATEGORY_BY_ID_SQL, (resultSet,
                                                                                                 rowNumber) ->
                            new PurposeCategory(resultSet.getInt(1),
                                    resultSet.getString(2),
                                    resultSet.getString(3),
                                    resultSet.getInt(4)),
                    preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PURPOSE_CATEGORY_BY_ID, String
                    .valueOf(id), e);
        }
        return purposeCategory;
    }

    @Override
    public List<PurposeCategory> listPurposeCategories(int limit, int offset, int tenantId) throws
            ConsentManagementException {

        List<PurposeCategory> purposesCategories;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            String query;
            if (isH2MySqlOrPostgresDB()) {
                query = LIST_PAGINATED_PURPOSE_CATEGORY_MYSQL;
            } else if (isDB2DB()) {
                query = LIST_PAGINATED_PURPOSE_CATEGORY_DB2;
                int initialOffset = offset;
                offset = offset + limit;
                limit = initialOffset + 1;
            } else if (isMSSqlDB()) {
                query = LIST_PAGINATED_PURPOSE_CATEGORY_MSSQL;
                int initialOffset = offset;
                offset = limit + offset;
                limit = initialOffset + 1;
            } else if (isInformixDB()) {
                query = LIST_PAGINATED_PURPOSE_CATEGORY_INFORMIX;
            } else {
                //oracle
                query = LIST_PAGINATED_PURPOSE_CATEGORY_ORACLE;
                limit = offset + limit;
            }
            int finalLimit = limit;
            int finalOffset = offset;

            purposesCategories = jdbcTemplate.executeQuery(query,
                    (resultSet, rowNumber) -> new PurposeCategory(resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getInt(4)),
                    preparedStatement -> {
                        preparedStatement.setInt(1, tenantId);
                        preparedStatement.setInt(2, finalLimit);
                        preparedStatement.setInt(3, finalOffset);
                    });
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException(String.format(ErrorMessages.ERROR_CODE_LIST_PURPOSE_CATEGORY
                    .getMessage(), limit, offset),
                    ErrorMessages.ERROR_CODE_LIST_PURPOSE_CATEGORY.getCode(), e);
        }
        return purposesCategories;
    }

    @Override
    public int deletePurposeCategory(int id) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_PURPOSE_CATEGORY_SQL,
                    preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_PURPOSE_CATEGORY, String
                    .valueOf(id), e);
        }
        return id;
    }

    @Override
    public PurposeCategory getPurposeCategoryByName(String name, int tenantId) throws ConsentManagementException {

        PurposeCategory purposeCategory;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            purposeCategory = jdbcTemplate.fetchSingleRecord(SELECT_PURPOSE_CATEGORY_BY_NAME_SQL, (resultSet,
                                                                                                   rowNumber) ->
                            new PurposeCategory(resultSet.getInt(1),
                                    resultSet.getString(2),
                                    resultSet.getString(3),
                                    resultSet.getInt(4)),
                    preparedStatement -> {
                        preparedStatement.setString(1, name);
                        preparedStatement.setInt(2, tenantId);
                    });
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PURPOSE_CATEGORY_BY_NAME, name, e);
        }
        return purposeCategory;
    }
}
