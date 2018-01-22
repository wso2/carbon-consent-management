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
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;

import java.util.List;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PII_CATEGORY_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_PII_CATEGORY_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PII_CATEGORY_MYSQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SELECT_PII_CATEGORY_BY_ID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.SELECT_PII_CATEGORY_BY_NAME_SQL;

/**
 * Default implementation of {@link PIICategoryDAO}. This handles {@link PIICategory} related DB operations.
 */
public class PIICategoryDAOImpl implements PIICategoryDAO {

    private final JdbcTemplate jdbcTemplate;

    public PIICategoryDAOImpl(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int getPriority() {

        return 1;
    }

    @Override
    public PIICategory addPIICategory(PIICategory piiCategory) throws ConsentManagementException {

        PIICategory purposeResult;
        int insertedId;

        try {
            insertedId = jdbcTemplate.executeInsert(INSERT_PII_CATEGORY_SQL, (preparedStatement -> {
                preparedStatement.setString(1, piiCategory.getName());
                preparedStatement.setString(2, piiCategory.getDescription());
                preparedStatement.setInt(3, piiCategory.getSensitive() ? 1 : 0);
            }), piiCategory, true);
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_PII_CATEGORY, piiCategory.getName(), e);
        }
        purposeResult = new PIICategory(insertedId, piiCategory.getName(), piiCategory.getDescription(),
                piiCategory.getSensitive());
        return purposeResult;
    }

    @Override
    public PIICategory getPIICategoryById(int id) throws ConsentManagementException {

        PIICategory piiCategory;

        try {
            piiCategory = jdbcTemplate.fetchSingleRecord(SELECT_PII_CATEGORY_BY_ID_SQL, (resultSet, rowNumber) ->
                            new PIICategory(resultSet.getInt(1),
                                    resultSet.getString(2),
                                    resultSet.getString(3),
                                    resultSet.getInt(4) == 1),
                    preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PII_CATEGORY_BY_ID, String
                    .valueOf(id), e);
        }

        if (piiCategory == null) {
            throw ConsentUtils.handleClientException(ERROR_CODE_PII_CATEGORY_ID_INVALID, String.valueOf(id));
        }
        return piiCategory;
    }

    @Override
    public List<PIICategory> listPIICategories(int limit, int offset) throws ConsentManagementException {

        List<PIICategory> piiCategories;

        try {
            piiCategories = jdbcTemplate.executeQuery(LIST_PAGINATED_PII_CATEGORY_MYSQL,
                    (resultSet, rowNumber) -> new PIICategory(resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getInt(4) == 1),
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

        try {
            jdbcTemplate.executeUpdate(DELETE_PII_CATEGORY_SQL, preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_PII_CATEGORY, String
                    .valueOf(id), e);
        }
        return id;
    }

    @Override
    public PIICategory getPIICategoryByName(String name) throws ConsentManagementServerException {

        PIICategory piiCategory;

        try {
            piiCategory = jdbcTemplate.fetchSingleRecord(SELECT_PII_CATEGORY_BY_NAME_SQL, (resultSet, rowNumber) ->
                            new PIICategory(resultSet.getInt(1),
                                    resultSet.getString(2),
                                    resultSet.getString(3),
                                    resultSet.getInt(4) == 1),
                    preparedStatement -> preparedStatement.setString(1, name));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PII_CATEGORY_BY_NAME, name, e);
        }
        return piiCategory;

    }
}
