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

import org.wso2.carbon.consent.mgt.core.dao.JdbcTemplate;
import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.exception.DataAccessException;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;

import java.util.List;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PURPOSE_CATEGORY_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_PURPOSE_CATEGORY_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_CATEGORY_MYSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SELECT_PURPOSE_CATEGORY_BY_ID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SELECT_PURPOSE_CATEGORY_BY_NAME_SQL;

/**
 * Default implementation of {@link PurposeCategoryDAO}. This handles {@link PurposeCategory} related DB operations.
 */
public class PurposeCategoryDAOImpl implements PurposeCategoryDAO {

    @Override
    public PurposeCategory addPurposeCategory(PurposeCategory purposeCategory) throws ConsentManagementException {
        JdbcTemplate jdbcTemplate = JDBCPersistenceManager.getInstance().getJDBCTemplate();
        PurposeCategory purposeCategoryResult;
        int insertedId;
        try {
            insertedId = jdbcTemplate.executeInsert(INSERT_PURPOSE_CATEGORY_SQL, (preparedStatement -> {
                preparedStatement.setString(1, purposeCategory.getName());
                preparedStatement.setString(2, purposeCategory.getDescription());
            }), purposeCategory, true);
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_PURPOSE_CATEGORY, purposeCategory.getName(), e);
        }
        purposeCategoryResult = new PurposeCategory(insertedId, purposeCategory.getName(),
                purposeCategory.getDescription());
        return purposeCategoryResult;
    }

    @Override
    public PurposeCategory getPurposeCategoryById(int id) throws ConsentManagementException {
        JdbcTemplate jdbcTemplate = JDBCPersistenceManager.getInstance().getJDBCTemplate();

        PurposeCategory purposeCategory;

        try {
            purposeCategory = jdbcTemplate.fetchSingleRecord(SELECT_PURPOSE_CATEGORY_BY_ID_SQL, (resultSet, rowNumber) ->
                            new PurposeCategory(resultSet.getInt(1),
                                    resultSet.getString(2),
                                    resultSet.getString(3)),
                    preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PURPOSE_CATEGORY_BY_ID, String
                    .valueOf(id), e);
        }

        if (purposeCategory == null) {
            throw ConsentUtils.handleClientException(ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID, String.valueOf(id));
        }
        return purposeCategory;
    }

    @Override
    public List<PurposeCategory> listPurposeCategories(int limit, int offset) throws ConsentManagementException {
        JdbcTemplate jdbcTemplate = JDBCPersistenceManager.getInstance().getJDBCTemplate();

        List<PurposeCategory> purposesCategories;
        try {
            purposesCategories = jdbcTemplate.executeQuery(LIST_PAGINATED_PURPOSE_CATEGORY_MYSQL,
                    (resultSet, rowNumber) -> new PurposeCategory(resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getString(3)),
                    preparedStatement -> {
                        preparedStatement.setInt(1, limit);
                        preparedStatement.setInt(2, offset);
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

        JdbcTemplate jdbcTemplate = JDBCPersistenceManager.getInstance().getJDBCTemplate();

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
    public PurposeCategory getPurposeCategoryByName(String name) throws ConsentManagementException {
        JdbcTemplate jdbcTemplate = JDBCPersistenceManager.getInstance().getJDBCTemplate();
        PurposeCategory purposeCategory;

        try {
            purposeCategory = jdbcTemplate.fetchSingleRecord(SELECT_PURPOSE_CATEGORY_BY_NAME_SQL, (resultSet, rowNumber) ->
                            new PurposeCategory(resultSet.getInt(1),
                                    resultSet.getString(2),
                                    resultSet.getString(3)),
                    preparedStatement -> preparedStatement.setString(1, name));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PURPOSE_CATEGORY_BY_NAME, name, e);
        }
        return purposeCategory;
    }
}
