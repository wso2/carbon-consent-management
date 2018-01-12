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

package org.wso2.carbon.consent.mgt.core.constant;

/**
 * Constants related to consent management configurations.
 */
public class ConfigurationConstants {

    public static String CONSENT_MANAGEMENT_CONFIG_XML = "consent-mgt-config.xml";
    public static String DEFAULT_DATA_SOURCE_NAME = "jdbc/WSO2ConsentDB";
    public static String CONSENT_MANAGEMENT_DEFAULT_NAMESPACE = "http://wso2.org/carbon/consent/management";
    public static String DATA_SOURCE_NAME_ELEMENT = "Name";
    public static String DATA_SOURCE_ELEMENT = "DataSource";

    public enum ErrorMessages {
        ERROR_CODE_DATABASE_CONNECTION("55000", "Error when getting a database connection object from the Identity " +
                "data source."),
        ERROR_CODE_DATABASE_INITIALIZATION("55001", "Error while initializing the consent management data source."),
        ERROR_CODE_DATABASE_QUERY_PERFORMING("55002", "Error in performing Database query: '%s.'"),
        ERROR_CODE_MORE_RECORDS_IN_QUERY("55003", "There are more records than one found for query: '%s.'"),
        ERROR_CODE_AUTO_GENERATED_ID_FAILURE("55004", "Creating the record failed with Auto-Generated ID, no ID obtained.");

        private final String code;
        private final String message;

        ErrorMessages(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return code + " : " + message;
        }
    }
}
