/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.consent.mgt.core.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds a parameterized SQL filter fragment and its ordered parameter values.
 * Mirrors the pattern used in APIResourceManagementDAOImpl.
 */
public class FilterQueryBuilder {

    private final Map<Integer, String> stringParameters = new LinkedHashMap<>();
    private String filterQuery = "";

    public void setFilterAttributeValue(int count, String value) {

        stringParameters.put(count, value);
    }

    public Map<Integer, String> getFilterAttributeValue() {

        return stringParameters;
    }

    public String getFilterQuery() {

        return filterQuery;
    }

    public void setFilterQuery(String filter) {

        this.filterQuery = filter;
    }
}
