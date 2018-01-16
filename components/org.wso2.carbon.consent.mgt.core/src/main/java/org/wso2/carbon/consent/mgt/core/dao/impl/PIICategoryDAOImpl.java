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
import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.exception.DataAccessException;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.persistence.JDBCPersistenceManager;

import java.util.List;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages;

/**
 * Default implementation of {@link PIICategoryDAO}. This handles {@link PIICategory} related DB operations.
 */
public class PIICategoryDAOImpl implements PIICategoryDAO {

    @Override
    public PIICategory addPIICategory(PIICategory piiCategory) throws ConsentManagementException {
        JdbcTemplate jdbcTemplate = JDBCPersistenceManager.getInstance().getJDBCTemplate();
        final String INSERT_PII_CATEGORY_SQL = "INSERT INTO CM_PII_CATEGORY (NAME, DESCRIPTION) VALUES (?,?)";
        PIICategory purposeResult;
        int insertedId;
        try {
            insertedId = jdbcTemplate.executeInsert(INSERT_PII_CATEGORY_SQL, (preparedStatement -> {
                preparedStatement.setString(1, piiCategory.getName());
                preparedStatement.setString(2, piiCategory.getDescription());
            }), piiCategory, true);
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException(String.format(ErrorMessages.ERROR_CODE_ADD_PII_CATEGORY
                                                                             .getMessage(),
                                                                     piiCategory.getName(),
                                                                     piiCategory.getDescription()),
                                                       ErrorMessages.ERROR_CODE_ADD_PII_CATEGORY.getCode(), e);
        }
        purposeResult = new PIICategory(insertedId, piiCategory.getName(), piiCategory.getDescription());
        return purposeResult;
    }

    @Override
    public PIICategory getPIICategoryById(int id) throws ConsentManagementException {
        JdbcTemplate jdbcTemplate = JDBCPersistenceManager.getInstance().getJDBCTemplate();
        final String SELECT_PII_CATEGORY_BY_ID_SQL = "SELECT ID, NAME, DESCRIPTION FROM CM_PII_CATEGORY WHERE ID = ?";
        PIICategory piiCategory;

        try {
            piiCategory = jdbcTemplate.fetchSingleRecord(SELECT_PII_CATEGORY_BY_ID_SQL, (resultSet, rowNumber) ->
                                                             new PIICategory(resultSet.getInt(1),
                                                                             resultSet.getString(2),
                                                                             resultSet.getString(3)),
                                                         preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException(String.format(ErrorMessages.ERROR_CODE_SELECT_PII_CATEGORY_BY_ID
                                                                             .getMessage(), id),
                                                       ErrorMessages.ERROR_CODE_SELECT_PII_CATEGORY_BY_ID.getCode(), e);
        }
        return piiCategory;
    }

    @Override
    public List<PIICategory> listPIICategories(int limit, int offset) throws ConsentManagementException {
        JdbcTemplate jdbcTemplate = JDBCPersistenceManager.getInstance().getJDBCTemplate();
        final String LIST_PAGINATED_PII_CATEGORY_MYSQL = "SELECT ID, NAME, DESCRIPTION FROM CM_PII_CATEGORY ORDER BY " +
                "ID ASC LIMIT ? OFFSET ?";

        List<PIICategory> piiCategories;
        try {
            piiCategories = jdbcTemplate.executeQuery(LIST_PAGINATED_PII_CATEGORY_MYSQL,
                                                 (resultSet, rowNumber) -> new PIICategory(resultSet.getInt(1),
                                                                                       resultSet.getString(2),
                                                                                       resultSet.getString(3)),
                                                 preparedStatement -> {
                                                     preparedStatement.setInt(1, limit);
                                                     preparedStatement.setInt(2, offset);
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
        JdbcTemplate jdbcTemplate = JDBCPersistenceManager.getInstance().getJDBCTemplate();
        final String DELETE_PII_CATEGORY_SQL = "DELETE FROM CM_PII_CATEGORY WHERE ID = ?";

        try {
            jdbcTemplate.executeUpdate(DELETE_PII_CATEGORY_SQL, preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException(String.format(ErrorMessages.ERROR_CODE_DELETE_PII_CATEGORY
                                                                             .getMessage(), id),
                                                       ErrorMessages.ERROR_CODE_DELETE_PII_CATEGORY.getCode(), e);
        }

        return id;
    }
}
