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
public class ConsentConstants {

    public static final String CONSENT_MANAGEMENT_CONFIG_XML = "consent-mgt-config.xml";
    public static final String CONSENT_MANAGEMENT_DEFAULT_NAMESPACE = "http://wso2.org/carbon/consent/management";
    public static final String APPLICATION_JSON = "application/json";
    public static final String DEFAULT_RESPONSE_CONTENT_TYPE = APPLICATION_JSON;
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT = "Internal server error";
    public static final String STATUS_INTERNAL_SERVER_ERROR_DESCRIPTION_DEFAULT = "The server encountered "
            + "an internal error. Please contact administrator.";
    public static final String STATUS_BAD_REQUEST_MESSAGE_DEFAULT = "Bad Request";

    public enum ErrorMessages {
        ERROR_CODE_DATABASE_CONNECTION("55000", "Error when getting a database connection object from the Identity " +
                "data source."),
        ERROR_CODE_DATABASE_INITIALIZATION("55001", "Error while initializing the consent management data source."),
        ERROR_CODE_DATABASE_QUERY_PERFORMING("55002", "Error in performing Database query: '%s.'"),
        ERROR_CODE_MORE_RECORDS_IN_QUERY("55003", "There are more records than one found for query: '%s.'"),
        ERROR_CODE_AUTO_GENERATED_ID_FAILURE("55004", "Creating the record failed with Auto-Generated ID, no ID " +
                "obtained."),
        ERROR_CODE_BUILDING_CONFIG("55005", "Error occurred while building configuration from consent-mgt-config.xml."),
        ERROR_CODE_ADD_PURPOSE("55006", "Error occurred while adding the purpose: %s with the description: %s to DB."),
        ERROR_CODE_SELECT_PURPOSE_BY_ID("55007", "Error occurred while retrieving purpose from DB for the ID: %s."),
        ERROR_CODE_DELETE_PURPOSE("55008", "Error occurred while deleting purpose from DB for the ID: %s."),
        ERROR_CODE_LIST_PURPOSE("55009", "Error occurred while listing purpose from DB for limit: %s and offset: %s."),
        ERROR_CODE_ADD_PII_CATEGORY("55010", "Error occurred while adding the purpose: %s with the description: %s to" +
                                             " DB."),
        ERROR_CODE_SELECT_PII_CATEGORY_BY_ID("55011", "Error occurred while retrieving purpose from DB for the ID: %s" +
                                                      "."),
        ERROR_CODE_DELETE_PII_CATEGORY("55012", "Error occurred while deleting purpose from DB for the ID: %s."),
        ERROR_CODE_LIST_PII_CATEGORY("55013", "Error occurred while listing purpose from DB for limit: %s and offset:" +
                                              " %s."),

        ERROR_CODE_PURPOSE_NAME_REQUIRED("55018", "Purpose name is required.");

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

    public class PIIControllerElements{
        public static final String countryElement = "PIIController.Address.Country";
        public static final String localityElement = "PIIController.Address.Locality";
        public static final String regionElement = "PIIController.Address.Region";
        public static final String postOfficeBoxNumberElement = "PIIController.Address.PostOfficeBoxNumber";
        public static final String postCodeElement = "PIIController.Address.PostalCode";
        public static final String streetAddressElement = "PIIController.Address.StreetAddress";
        public static final String piiControllerNameElement = "PIIController.PiiController";
        public static final String piiControllerContactElement = "PIIController.Contact";
        public static final String piiControllerPhoneElement = "PIIController.Phone";
        public static final String piiControllerEmailElement = "PIIController.Email";
        public static final String piiControllerOnBehalfElement = "PIIController.OnBehalf";
        public static final String piiControllerUrlElement = "PIIController.PiiControllerUrl";
    }
}
