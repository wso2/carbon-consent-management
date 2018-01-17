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
    public static final String PURPOSE_SEARCH_LIMIT_PATH = "SearchLimits.Purpose";
    public static final String CONSENT_RESOURCE_PATH = "consents";
    public static final String PURPOSE_RESOURCE_PATH = CONSENT_RESOURCE_PATH + "/" + "purposes";
    public static final String PURPOSE_CATEGORY_RESOURCE_PATH = CONSENT_RESOURCE_PATH + "/" + "purpose-categories";
    public static final String PII_CATEGORY_RESOURCE_PATH = CONSENT_RESOURCE_PATH + "/" + "pii-categories";

    public enum ErrorMessages {
        ERROR_CODE_DATABASE_CONNECTION("CM_00001", "Error when getting a database connection object from the Identity" +
                " data source."),
        ERROR_CODE_DATABASE_INITIALIZATION("CM_00002", "Error while initializing the consent management data source."),
        ERROR_CODE_DATABASE_QUERY_PERFORMING("CM_00003", "Error in performing Database query: '%s.'"),
        ERROR_CODE_MORE_RECORDS_IN_QUERY("CM_00004", "There are more records than one found for query: '%s.'"),
        ERROR_CODE_AUTO_GENERATED_ID_FAILURE("CM_00005", "Creating the record failed with Auto-Generated ID, no ID " +
                "obtained."),
        ERROR_CODE_BUILDING_CONFIG("CM_00006", "Error occurred while building configuration from consent-mgt-config" +
                ".xml."),
        ERROR_CODE_ADD_PURPOSE("CM_00007", "Error occurred while adding the purpose: %s"),
        ERROR_CODE_SELECT_PURPOSE_BY_ID("CM_00008", "Error occurred while retrieving purpose from DB for the ID: %s."),
        ERROR_CODE_DELETE_PURPOSE("CM_00009", "Error occurred while deleting purpose from DB for the ID: %s."),
        ERROR_CODE_LIST_PURPOSE("CM_00010", "Error occurred while listing purpose from DB for limit: %s and offset: " +
                "%s."),
        ERROR_CODE_ADD_PII_CATEGORY("CM_00011", "Error occurred while adding the PII category: %s to DB."),
        ERROR_CODE_SELECT_PII_CATEGORY_BY_ID("CM_00012", "Error occurred while retrieving PII category from DB for " +
                "the ID: %s."),
        ERROR_CODE_DELETE_PII_CATEGORY("CM_00013", "Error occurred while deleting PII category from DB for the ID: %s."),
        ERROR_CODE_LIST_PII_CATEGORY("CM_00014", "Error occurred while listing PII category from DB for limit: %s and" +
                " offset: %s."),
        ERROR_CODE_ADD_PURPOSE_CATEGORY("CM_00015", "Error occurred while adding the purpose category: %s to DB."),
        ERROR_CODE_SELECT_PURPOSE_CATEGORY_BY_ID("CM_00016", "Error occurred while retrieving purpose category from " +
                "DB for the ID: %s."),
        ERROR_CODE_DELETE_PURPOSE_CATEGORY("CM_00017", "Error occurred while deleting purpose category from DB for " +
                "the ID: %s."),
        ERROR_CODE_LIST_PURPOSE_CATEGORY("CM_00018", "Error occurred while listing purpose category from DB for " +
                "limit: %s and offset: %s."),
        ERROR_CODE_PURPOSE_NAME_REQUIRED("CM_00019", "Purpose name is required."),
        ERROR_CODE_PURPOSE_ID_REQUIRED("CM_00020", "Purpose ID is required."),
        ERROR_CODE_SELECT_PURPOSE_BY_NAME("CM_00021", "Error occurred while retrieving purpose from DB for the Name:%s."),
        ERROR_CODE_PURPOSE_ALREADY_EXIST("CM_00022", "Purpose with the name: %s already exists."),
        ERROR_CODE_INVALID_ARGUMENTS_FOR_LIM_OFFSET("CM_00023", "Limit or offset values cannot be negative."),
        ERROR_CODE_PURPOSE_ID_INVALID("CM_00024", "Invalid purpose Id: %s"),
        ERROR_CODE_SELECT_PURPOSE_CATEGORY_BY_NAME("CM_00025", "Error occurred while retrieving purpose category from" +
                "DB for the Name: %s."),
        ERROR_CODE_PURPOSE_CATEGORY_ID_REQUIRED("CM_00026", "Purpose category Id is required."),
        ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID("CM_00027", "Invalid purpose category Id: %s"),
        ERROR_CODE_PURPOSE_CATEGORY_NAME_REQUIRED("CM_00028", "Purpose category name is required."),
        ERROR_CODE_PURPOSE_CATEGORY_ALREADY_EXIST("CM_00029", "Purpose category with the name: %s already exists."),
        ERROR_CODE_UNEXPECTED("CM_00030", "Unexpected Error"),
        ERROR_CODE_PII_CATEGORY_NAME_REQUIRED("CM_00031", "PII Category name is required."),
        ERROR_CODE_PII_CATEGORY_ALREADY_EXIST("CM_00032", "PII Category already exists with the name: %s."),
        ERROR_CODE_SELECT_PII_CATEGORY_BY_NAME("CM_00033", "Error occurred while retrieving PII category from DB for " +
                "the Name: %s."),
        ERROR_CODE_PII_CATEGORY_ID_REQUIRED("CM_00034", "PII Category ID is required."),
        ERROR_CODE_PII_CATEGORY_ID_INVALID("CM_00035", "Invalid PII category Id: %s");
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

    public class PIIControllerElements {
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
