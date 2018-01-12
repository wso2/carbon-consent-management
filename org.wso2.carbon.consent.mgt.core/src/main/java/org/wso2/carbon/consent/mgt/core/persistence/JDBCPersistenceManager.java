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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementRuntimeException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 */
public class JDBCPersistenceManager {

    private static final Log log = LogFactory.getLog(JDBCPersistenceManager.class);
    private static DataSource dataSource;
    private static JDBCPersistenceManager instance = new JDBCPersistenceManager();

    private JDBCPersistenceManager() {
        iniDataSource();
    }

    public static JDBCPersistenceManager getInstance() {
        return instance;
    }

    private static void iniDataSource() {
        String dataSourceName = readConfiguration();
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

    private static String readConfiguration() {

        String dataSourceName = ConfigurationConstants.DEFAULT_DATA_SOURCE_NAME;
        String configurationFilePath = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                ConfigurationConstants.CONSENT_MANAGEMENT_CONFIG_XML;

        try (FileInputStream fileInputStream = new FileInputStream(new File(configurationFilePath))) {

            OMElement documentElement = new StAXOMBuilder(fileInputStream).getDocumentElement();

            OMElement dataSourceElem = documentElement.getFirstChildWithName(
                    new QName(ConfigurationConstants.CONSENT_MANAGEMENT_DEFAULT_NAMESPACE, "DataSource"));

            if (dataSourceElem == null) {
                log.info("DataSource Element is not available in " + ConfigurationConstants
                        .CONSENT_MANAGEMENT_CONFIG_XML + ". Using default value: " + ConfigurationConstants
                        .DEFAULT_DATA_SOURCE_NAME + " as data source name.");

                return dataSourceName;
            }

            OMElement dataSourceNameElem = dataSourceElem.getFirstChildWithName(
                    new QName(ConfigurationConstants.CONSENT_MANAGEMENT_DEFAULT_NAMESPACE, ConfigurationConstants
                            .DATA_SOURCE_ELEMENT));

            if (dataSourceNameElem != null) {
                dataSourceName = dataSourceNameElem.getText();
            }

        } catch (FileNotFoundException e) {
            log.error("Cannot find the config file. Default values will be assumed.", e);
        } catch (IOException e) {
            log.error("Error reading the config file. Default values will be assumed.", e);
        } catch (XMLStreamException e) {
            log.error("Error parsing the config file. Default values will be assumed.", e);
        }

        return dataSourceName;
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
}
