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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link FilterQueriesUtil#toSqlOperator(String)} and
 * {@link FilterQueriesUtil#toSqlValue(String, String)} — the shared filter-operator translation
 * used by the purpose, element, and consent receipt property filters.
 */
public class FilterQueriesUtilTest {

    @DataProvider(name = "operatorProvider")
    public Object[][] operatorProvider() {

        return new Object[][]{
                // operator, expected SQL operator
                {"eq", "="},
                {"ne", "!="},
                {"sw", "LIKE"},
                {"co", "LIKE"},
                {"ew", "LIKE"},
                {"ge", ">="},
                {"le", "<="},
                {"gt", ">"},
                {"lt", "<"},
                // Case-insensitivity.
                {"EQ", "="},
                {"Sw", "LIKE"},
                {"CO", "LIKE"},
                // null and unknown operators fall back to equality (never raw user input → no injection).
                {null, "="},
                {"unknown", "="},
        };
    }

    @Test(dataProvider = "operatorProvider")
    public void testToSqlOperator(String op, String expected) {

        Assert.assertEquals(FilterQueriesUtil.toSqlOperator(op), expected,
                "Unexpected SQL operator for filter operation: " + op);
    }

    @DataProvider(name = "valueProvider")
    public Object[][] valueProvider() {

        return new Object[][]{
                // operator, raw value, expected bound value
                {"eq", "EU", "EU"},          // equality — value unchanged
                {"sw", "EU", "EU%"},         // starts-with — trailing wildcard
                {"co", "EU", "%EU%"},        // contains — surrounding wildcards
                {"ew", "EU", "%EU"},         // ends-with — leading wildcard
                {"SW", "EU", "EU%"},         // case-insensitive operator
                {"CO", "EU", "%EU%"},
                {"ne", "EU", "EU"},          // non-LIKE operators leave the value untouched
                {null, "EU", "EU"},          // null operator → unchanged
                {"co", null, null},          // null value → unchanged (no NPE)
        };
    }

    @Test(dataProvider = "valueProvider")
    public void testToSqlValue(String op, String value, String expected) {

        Assert.assertEquals(FilterQueriesUtil.toSqlValue(op, value), expected,
                "Unexpected SQL value for operation '" + op + "' and value '" + value + "'");
    }
}
