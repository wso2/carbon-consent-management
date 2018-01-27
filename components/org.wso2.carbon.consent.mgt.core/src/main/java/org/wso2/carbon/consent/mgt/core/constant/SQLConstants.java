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
 * Constant related to SQL operations.
 */
public class SQLConstants {

    public static final String INSERT_PURPOSE_SQL = "INSERT INTO CM_PURPOSE (NAME, DESCRIPTION, TENANT_ID) values (?," +
                                                    " ?, ?)";
    public static final String GET_PURPOSE_BY_ID_SQL = "SELECT ID, NAME, DESCRIPTION, TENANT_ID FROM CM_PURPOSE WHERE" +
                                                       " ID = ?";
    public static final String GET_PURPOSE_BY_NAME_SQL = "SELECT ID, NAME, DESCRIPTION, TENANT_ID FROM CM_PURPOSE " +
                                                         "WHERE NAME = ? AND TENANT_ID = ?";
    public static final String LIST_PAGINATED_PURPOSE_MYSQL = "SELECT ID, NAME, DESCRIPTION, TENANT_ID FROM " +
                                                              "CM_PURPOSE WHERE TENANT_ID = ? ORDER BY ID ASC LIMIT ?" +
                                                              " OFFSET ?";
    public static final String DELETE_PURPOSE_SQL = "DELETE FROM CM_PURPOSE WHERE ID = ?";
    public static final String INSERT_PII_CATEGORY_SQL = "INSERT INTO CM_PII_CATEGORY (NAME, DESCRIPTION," +
                                                         "IS_SENSITIVE, TENANT_ID) VALUES (?,?,?,?)";
    public static final String SELECT_PII_CATEGORY_BY_ID_SQL = "SELECT ID, NAME, DESCRIPTION, IS_SENSITIVE, TENANT_ID" +
                                                               " FROM CM_PII_CATEGORY WHERE ID = ?";
    public static final String LIST_PAGINATED_PII_CATEGORY_MYSQL = "SELECT ID, NAME, DESCRIPTION, IS_SENSITIVE, " +
                                                                   "TENANT_ID FROM CM_PII_CATEGORY WHERE TENANT_ID = " +
                                                                   "? ORDER BY ID ASC LIMIT ? OFFSET ?";
    public static final String DELETE_PII_CATEGORY_SQL = "DELETE FROM CM_PII_CATEGORY WHERE ID = ?";
    public static final String SELECT_PII_CATEGORY_BY_NAME_SQL = "SELECT ID, NAME, DESCRIPTION, IS_SENSITIVE, " +
                                                                 "TENANT_ID FROM CM_PII_CATEGORY WHERE NAME = ? AND " +
                                                                 "TENANT_ID = ? ";
    public static final String INSERT_PURPOSE_CATEGORY_SQL = "INSERT INTO CM_PURPOSE_CATEGORY (NAME, DESCRIPTION) " +
            "VALUES (?,?)";
    public static final String SELECT_PURPOSE_CATEGORY_BY_ID_SQL = "SELECT ID, NAME, DESCRIPTION FROM " +
            "CM_PURPOSE_CATEGORY  WHERE ID = ?";
    public static final String LIST_PAGINATED_PURPOSE_CATEGORY_MYSQL = "SELECT ID, NAME, DESCRIPTION FROM " +
            "CM_PURPOSE_CATEGORY ORDER BY ID  ASC LIMIT ? OFFSET ?";
    public static final String DELETE_PURPOSE_CATEGORY_SQL = "DELETE FROM CM_PURPOSE_CATEGORY WHERE ID = ?";
    public static final String SELECT_PURPOSE_CATEGORY_BY_NAME_SQL = "SELECT ID, NAME, DESCRIPTION FROM " +
            "CM_PURPOSE_CATEGORY WHERE NAME= ?";

    public static final String INSERT_RECEIPT_SQL = "INSERT INTO CM_RECEIPT (CONSENT_RECEIPT_ID,VERSION, " +
            "JURISDICTION,CONSENT_TIMESTAMP,COLLECTION_METHOD,LANGUAGE,PII_PRINCIPAL_ID,PRINCIPAL_TENANT_DOMAIN, " +
            "POLICY_URL,STATE) values (?,?,?,?,?,?,?,?,?,?)";
    public static final String INSERT_RECEIPT_SP_ASSOC_SQL = "INSERT INTO CM_RECEIPT_SP_ASSOC (CONSENT_RECEIPT_ID, SP_NAME," +
            "SP_TENANT_DOMAIN) VALUES (?,?,?)";
    public static final String INSERT_SP_TO_PURPOSE_ASSOC_SQL = "INSERT INTO CM_SP_PURPOSE_ASSOC (RECEIPT_SP_ASSOC," +
            "PURPOSE_ID,CONSENT_TYPE,IS_PRIMARY_PURPOSE,TERMINATION,THIRD_PARTY_DISCLOSURE,THIRD_PARTY_NAME) VALUES " +
            "(?,?,?,?,?,?,?)";

    public static final String INSERT_SP_PURPOSE_TO_PURPOSE_CAT_ASSOC_SQL = "INSERT INTO CM_SP_PURPOSE_PURPOSE_CAT_ASSOC " +
            "(SP_PURPOSE_ASSOC_ID, PURPOSE_CATEGORY_ID) VALUES (?,?)";

    public static final String INSERT_SP_PURPOSE_TO_PII_CAT_ASSOC_SQL = "INSERT INTO CM_SP_PURPOSE_PII_CATEGORY_ASSOC " +
            "(SP_PURPOSE_ASSOC_ID, PII_CATEGORY_ID) VALUES (?,?)";

    public static final String INSERT_RECEIPT_PROPERTIES_SQL = "INSERT INTO CM_CONSENT_RECEIPT_PROPERTY " +
            "(CONSENT_RECEIPT_ID,NAME,VALUE) VALUES (?,?,?)";

    public static final String GET_RECEIPT_SQL = "SELECT version,jurisdiction,consent_timestamp,collection_method," +
            "language,pii_principal_id,principal_tenant_domain,policy_url,state FROM cm_receipt WHERE " +
            "consent_receipt_id =?";

    public static final String GET_RECEIPT_SP_SQL = "SELECT ID,SP_NAME,SP_TENANT_DOMAIN  FROM CM_RECEIPT_SP_ASSOC " +
            "WHERE CONSENT_RECEIPT_ID =?";

    public static final String GET_SP_PURPOSE_SQL = "SELECT SP.ID,SP.CONSENT_TYPE,SP.IS_PRIMARY_PURPOSE," +
            "SP.TERMINATION,SP.THIRD_PARTY_DISCLOSURE,SP.THIRD_PARTY_NAME,P.NAME,P.DESCRIPTION FROM CM_SP_PURPOSE_ASSOC SP " +
            "INNER JOIN  CM_PURPOSE P ON SP.PURPOSE_ID = P.ID WHERE RECEIPT_SP_ASSOC =?";

    public static final String GET_PURPOSE_CAT_SQL = "SELECT NAME FROM CM_SP_PURPOSE_PURPOSE_CAT_ASSOC SPC " +
            "INNER JOIN  CM_PURPOSE_CATEGORY PC ON SPC.PURPOSE_CATEGORY_ID = PC.ID WHERE SPC.SP_PURPOSE_ASSOC_ID =?";

    public static final String GET_PII_CAT_SQL = "SELECT NAME,IS_SENSITIVE FROM CM_SP_PURPOSE_PII_CATEGORY_ASSOC SPC " +
            "INNER JOIN  CM_PII_CATEGORY PC ON SPC.PII_CATEGORY_ID = PC.ID WHERE SPC.SP_PURPOSE_ASSOC_ID =?";

    public static final String SEARCH_RECEIPT_SQL = "SELECT R.CONSENT_RECEIPT_ID, R.LANGUAGE, R.PII_PRINCIPAL_ID, R" +
            ".PRINCIPAL_TENANT_DOMAIN, R.STATE FROM CM_RECEIPT R INNER JOIN CM_RECEIPT_SP_ASSOC RS ON R" +
            ".CONSENT_RECEIPT_ID=RS.CONSENT_RECEIPT_ID WHERE PII_PRINCIPAL_ID LIKE ? AND SP_NAME LIKE" +
            " ? AND SP_TENANT_DOMAIN LIKE ? AND STATE LIKE ? ORDER BY ID  ASC LIMIT ? OFFSET ?";

    public static final String REVOKE_RECEIPT_SQL = "UPDATE CM_RECEIPT SET STATE = ? WHERE CONSENT_RECEIPT_ID = ?";

}
