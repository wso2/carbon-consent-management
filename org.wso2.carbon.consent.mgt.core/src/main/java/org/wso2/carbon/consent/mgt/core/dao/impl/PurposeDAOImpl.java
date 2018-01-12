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
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.exception.DataAccessException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.persistence.JDBCPersistenceManager;

/**
 * Default implementation of {@link PurposeDAO}. This handle {@link Purpose} related DB operations.
 */
public class PurposeDAOImpl implements PurposeDAO {

    @Override
    public Purpose addPurpose(Purpose purpose) throws ConsentManagementServerException {
        JdbcTemplate jdbcTemplate = JDBCPersistenceManager.getInstance().getJDBCTemplate();
        final String INSERT_PURPOSE_SQL = "INSERT INTO PURPOSE(NAME,DESCRIPTION) VALUES(?,?)";
        Purpose purpose2;
        int insertedId;
        try {
            insertedId = jdbcTemplate.executeInsert(INSERT_PURPOSE_SQL, (preparedStatement -> {
                preparedStatement.setString(1, purpose.getName());
                preparedStatement.setString(2, purpose.getDescription());
            }), purpose, true);
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException();
        }
        purpose2 = new Purpose(insertedId, null, null);
        return purpose2;
    }

    @Override
    public Purpose getPurpose() {

        return null;
    }
}
