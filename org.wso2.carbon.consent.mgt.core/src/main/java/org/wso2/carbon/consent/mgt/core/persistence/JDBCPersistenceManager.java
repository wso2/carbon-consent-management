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

package org.wso2.carbon.consent.mgt.core.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.consent.mgt.core.dao.JdbcTemplate;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementRuntimeException;
import org.wso2.carbon.consent.mgt.core.util.ConsentConfigParser;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 */
public class JDBCPersistenceManager {

    private static final Logger log = LoggerFactory.getLogger(JDBCPersistenceManager.class);
    private static DataSource dataSource;
    private static JDBCPersistenceManager instance = new JDBCPersistenceManager();

    private JDBCPersistenceManager() {
        iniDataSource();
    }

    public static JDBCPersistenceManager getInstance() {
        return instance;
    }

    private static void iniDataSource() {
        String dataSourceName = ConsentConfigParser.getInstance().getConsentDataSource();
        Context ctx;
        try {
            ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(dataSourceName);
        } catch (NamingException e) {
            throw new ConsentManagementRuntimeException(ConfigurationConstants.ErrorMessages
                    .ERROR_CODE_DATABASE_INITIALIZATION.getMessage()
                    , ConfigurationConstants.ErrorMessages.ERROR_CODE_DATABASE_INITIALIZATION.getCode(), e);
        }
    }

    /**
     * Returns an database connection for Identity data source.
     *
     * @return Database connection
     * @throws ConsentManagementRuntimeException Exception occurred when getting the data source.
     */
    public Connection getDBConnection() throws ConsentManagementRuntimeException {
        try {
            Connection dbConnection = dataSource.getConnection();
            //TODO: Read from data source.
            dbConnection.setAutoCommit(false);
            dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return dbConnection;
        } catch (SQLException e) {
            throw new ConsentManagementRuntimeException(ConfigurationConstants.ErrorMessages
                    .ERROR_CODE_DATABASE_CONNECTION.getMessage()
                    , ConfigurationConstants.ErrorMessages.ERROR_CODE_DATABASE_CONNECTION.getCode(), e);
        }
    }

    public JdbcTemplate getJDBCTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
    }
}
