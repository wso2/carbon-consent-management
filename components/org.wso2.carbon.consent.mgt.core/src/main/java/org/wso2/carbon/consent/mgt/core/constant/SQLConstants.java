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

    public static final String INSERT_PURPOSE_SQL = "INSERT INTO CM_PURPOSE (NAME, DESCRIPTION) values (?, ?)";
    public static final String GET_PURPOSE_BY_ID_SQL = "SELECT ID, NAME, DESCRIPTION FROM CM_PURPOSE WHERE ID = ?";
    public static final String GET_PURPOSE_BY_NAME_SQL = "SELECT ID, NAME, DESCRIPTION FROM CM_PURPOSE WHERE NAME = ?";
    public static final String LIST_PAGINATED_PURPOSE_MYSQL = "SELECT ID, NAME, DESCRIPTION FROM CM_PURPOSE ORDER BY " +
            "ID  ASC LIMIT ? OFFSET ?";
    public static final String DELETE_PURPOSE_SQL = "DELETE FROM CM_PURPOSE WHERE ID = ?";

}
