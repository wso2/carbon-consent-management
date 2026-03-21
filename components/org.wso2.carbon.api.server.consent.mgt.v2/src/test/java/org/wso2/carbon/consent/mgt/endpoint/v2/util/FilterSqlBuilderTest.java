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

package org.wso2.carbon.consent.mgt.endpoint.v2.util;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.wso2.carbon.consent.mgt.core.util.FilterSqlBuilder;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Tests for FilterSqlBuilder - SQL WHERE clause generation from filter trees.
 */
public class FilterSqlBuilderTest {

    private FilterSqlBuilder filterSqlBuilder = new FilterSqlBuilder();

    /**
     * Test 1: Simple equality filter
     * Input: "name eq Marketing"
     * Expected WHERE clause: "NAME = ?"
     * Expected paramValues: ["Marketing"]
     */
    @Test
    public void testBuildWhereClauseWithSimpleEquality() {

        ExpressionNode filterTree = new ExpressionNode();
        filterTree.setAttributeValue("name");
        filterTree.setOperation("eq");
        filterTree.setValue("Marketing");

        List<Object> paramValues = new ArrayList<>();
        String whereClause = filterSqlBuilder.buildWhereClause(filterTree, paramValues);

        assertEquals(whereClause, "NAME = ?", "WHERE clause should be 'NAME = ?'");
        assertEquals(paramValues.size(), 1, "Should have one parameter value");
        assertEquals(paramValues.get(0), "Marketing", "Parameter value should be 'Marketing'");
    }

    /**
     * Test 2: Starts with (LIKE) filter
     * Input: "name sw Data"
     * Expected WHERE clause: "NAME LIKE ?"
     * Expected paramValues: ["Data%"]
     */
    @Test
    public void testBuildWhereClauseWithStartsWith() {

        ExpressionNode filterTree = new ExpressionNode();
        filterTree.setAttributeValue("name");
        filterTree.setOperation("sw");
        filterTree.setValue("Data");

        List<Object> paramValues = new ArrayList<>();
        String whereClause = filterSqlBuilder.buildWhereClause(filterTree, paramValues);

        assertEquals(whereClause, "NAME LIKE ?", "WHERE clause should be 'NAME LIKE ?'");
        assertEquals(paramValues.size(), 1, "Should have one parameter value");
        assertEquals(paramValues.get(0), "Data%", "Parameter value should be 'Data%' with start wildcard");
    }

    /**
     * Test 3: AND combination of two expressions
     * Input: "name eq Marketing and type eq Policy"
     * Expected WHERE clause: Contains both "NAME = ?" and "TYPE = ?" with AND
     * Expected paramValues: ["Marketing", "Policy"]
     */
    @Test
    public void testBuildWhereClauseWithAndOperation() {

        // Left node: name eq Marketing
        ExpressionNode leftExpr = new ExpressionNode();
        leftExpr.setAttributeValue("name");
        leftExpr.setOperation("eq");
        leftExpr.setValue("Marketing");

        // Right node: type eq Policy
        ExpressionNode rightExpr = new ExpressionNode();
        rightExpr.setAttributeValue("type");
        rightExpr.setOperation("eq");
        rightExpr.setValue("Policy");

        // Create operation node
        OperationNode filterTree = new OperationNode("and");
        filterTree.setLeftNode(leftExpr);
        filterTree.setRightNode(rightExpr);

        List<Object> paramValues = new ArrayList<>();
        String whereClause = filterSqlBuilder.buildWhereClause(filterTree, paramValues);

        // Verify WHERE clause structure
        assertEquals(whereClause.contains("NAME = ?"), true, "WHERE clause should contain 'NAME = ?'");
        assertEquals(whereClause.contains("TYPE = ?"), true, "WHERE clause should contain 'TYPE = ?'");
        assertEquals(whereClause.contains(" AND "), true, "WHERE clause should contain ' AND '");

        // Verify parameter values
        assertEquals(paramValues.size(), 2, "Should have two parameter values");
        assertEquals(paramValues.get(0), "Marketing", "First parameter should be 'Marketing'");
        assertEquals(paramValues.get(1), "Policy", "Second parameter should be 'Policy'");
    }

    /**
     * Test 4: Contains (LIKE with both wildcards)
     * Input: "name co search"
     * Expected WHERE clause: "NAME LIKE ?"
     * Expected paramValues: ["%search%"]
     */
    @Test
    public void testBuildWhereClauseWithContains() {

        ExpressionNode filterTree = new ExpressionNode();
        filterTree.setAttributeValue("name");
        filterTree.setOperation("co");
        filterTree.setValue("search");

        List<Object> paramValues = new ArrayList<>();
        String whereClause = filterSqlBuilder.buildWhereClause(filterTree, paramValues);

        assertEquals(whereClause, "NAME LIKE ?", "WHERE clause should be 'NAME LIKE ?'");
        assertEquals(paramValues.size(), 1, "Should have one parameter value");
        assertEquals(paramValues.get(0), "%search%", "Parameter value should be '%search%'");
    }

    /**
     * Test 5: Ends with (LIKE with trailing wildcard)
     * Input: "name ew Policy"
     * Expected WHERE clause: "NAME LIKE ?"
     * Expected paramValues: ["%Policy"]
     */
    @Test
    public void testBuildWhereClauseWithEndsWith() {

        ExpressionNode filterTree = new ExpressionNode();
        filterTree.setAttributeValue("name");
        filterTree.setOperation("ew");
        filterTree.setValue("Policy");

        List<Object> paramValues = new ArrayList<>();
        String whereClause = filterSqlBuilder.buildWhereClause(filterTree, paramValues);

        assertEquals(whereClause, "NAME LIKE ?", "WHERE clause should be 'NAME LIKE ?'");
        assertEquals(paramValues.size(), 1, "Should have one parameter value");
        assertEquals(paramValues.get(0), "%Policy", "Parameter value should be '%Policy'");
    }

    /**
     * Test 6: OR combination
     * Input: "name eq Marketing or type eq Policy"
     * Expected WHERE clause: Contains both conditions with OR
     * Expected paramValues: ["Marketing", "Policy"]
     */
    @Test
    public void testBuildWhereClauseWithOrOperation() {

        // Left node: name eq Marketing
        ExpressionNode leftExpr = new ExpressionNode();
        leftExpr.setAttributeValue("name");
        leftExpr.setOperation("eq");
        leftExpr.setValue("Marketing");

        // Right node: type eq Policy
        ExpressionNode rightExpr = new ExpressionNode();
        rightExpr.setAttributeValue("type");
        rightExpr.setOperation("eq");
        rightExpr.setValue("Policy");

        // Create operation node
        OperationNode filterTree = new OperationNode("or");
        filterTree.setLeftNode(leftExpr);
        filterTree.setRightNode(rightExpr);

        List<Object> paramValues = new ArrayList<>();
        String whereClause = filterSqlBuilder.buildWhereClause(filterTree, paramValues);

        // Verify WHERE clause structure
        assertEquals(whereClause.contains("NAME = ?"), true, "WHERE clause should contain 'NAME = ?'");
        assertEquals(whereClause.contains("TYPE = ?"), true, "WHERE clause should contain 'TYPE = ?'");
        assertEquals(whereClause.contains(" OR "), true, "WHERE clause should contain ' OR '");

        // Verify parameter values
        assertEquals(paramValues.size(), 2, "Should have two parameter values");
        assertEquals(paramValues.get(0), "Marketing", "First parameter should be 'Marketing'");
        assertEquals(paramValues.get(1), "Policy", "Second parameter should be 'Policy'");
    }

    /**
     * Test 7: Null filter tree
     * Input: null
     * Expected: null returned
     */
    @Test
    public void testBuildWhereClauseWithNullFilterTree() {

        List<Object> paramValues = new ArrayList<>();
        String whereClause = filterSqlBuilder.buildWhereClause(null, paramValues);

        assertNull(whereClause, "WHERE clause should be null for null filter tree");
        assertEquals(paramValues.size(), 0, "Should have no parameter values");
    }

    /**
     * Test 8: Missing attribute value
     * Input: ExpressionNode with null attribute
     * Expected: null returned
     */
    @Test
    public void testBuildWhereClauseWithMissingAttribute() {

        ExpressionNode filterTree = new ExpressionNode();
        filterTree.setAttributeValue(null);
        filterTree.setOperation("eq");
        filterTree.setValue("Marketing");

        List<Object> paramValues = new ArrayList<>();
        String whereClause = filterSqlBuilder.buildWhereClause(filterTree, paramValues);

        assertNull(whereClause, "WHERE clause should be null for missing attribute");
        assertEquals(paramValues.size(), 0, "Should have no parameter values");
    }

    /**
     * Test 9: Missing value
     * Input: ExpressionNode with null value
     * Expected: null returned
     */
    @Test
    public void testBuildWhereClauseWithMissingValue() {

        ExpressionNode filterTree = new ExpressionNode();
        filterTree.setAttributeValue("name");
        filterTree.setOperation("eq");
        filterTree.setValue(null);

        List<Object> paramValues = new ArrayList<>();
        String whereClause = filterSqlBuilder.buildWhereClause(filterTree, paramValues);

        assertNull(whereClause, "WHERE clause should be null for missing value");
        assertEquals(paramValues.size(), 0, "Should have no parameter values");
    }

    /**
     * Test 10: Operator case insensitivity
     * Input: "name EQ Marketing" (uppercase operator)
     * Expected: Should still work, translating to "="
     */
    @Test
    public void testOperatorCaseInsensitivity() {

        ExpressionNode filterTree = new ExpressionNode();
        filterTree.setAttributeValue("name");
        filterTree.setOperation("EQ");
        filterTree.setValue("Marketing");

        List<Object> paramValues = new ArrayList<>();
        String whereClause = filterSqlBuilder.buildWhereClause(filterTree, paramValues);

        assertEquals(whereClause, "NAME = ?", "WHERE clause should handle uppercase operators");
        assertEquals(paramValues.get(0), "Marketing", "Parameter value should be 'Marketing'");
    }

    /**
     * Test 11: Attribute name case normalization
     * Input: "NaMe eq Marketing" (mixed case attribute)
     * Expected: Should normalize to uppercase "NAME"
     */
    @Test
    public void testAttributeNameNormalization() {

        ExpressionNode filterTree = new ExpressionNode();
        filterTree.setAttributeValue("NaMe");
        filterTree.setOperation("eq");
        filterTree.setValue("Marketing");

        List<Object> paramValues = new ArrayList<>();
        String whereClause = filterSqlBuilder.buildWhereClause(filterTree, paramValues);

        assertEquals(whereClause, "NAME = ?", "WHERE clause should normalize attribute to uppercase");
    }

    /**
     * Test 12: Complex nested operations (AND within parentheses)
     * Input: "(name eq Marketing and type eq Policy) and state eq active"
     * Expected: Multiple nested expressions properly parenthesized
     */
    @Test
    public void testComplexNestedOperations() {

        // Inner AND: name eq Marketing and type eq Policy
        ExpressionNode leftExpr = new ExpressionNode();
        leftExpr.setAttributeValue("name");
        leftExpr.setOperation("eq");
        leftExpr.setValue("Marketing");

        ExpressionNode rightExpr = new ExpressionNode();
        rightExpr.setAttributeValue("type");
        rightExpr.setOperation("eq");
        rightExpr.setValue("Policy");

        OperationNode innerAnd = new OperationNode("and");
        innerAnd.setLeftNode(leftExpr);
        innerAnd.setRightNode(rightExpr);

        // Outer AND: (above) and state eq active
        ExpressionNode stateExpr = new ExpressionNode();
        stateExpr.setAttributeValue("state");
        stateExpr.setOperation("eq");
        stateExpr.setValue("active");

        OperationNode outerAnd = new OperationNode("and");
        outerAnd.setLeftNode(innerAnd);
        outerAnd.setRightNode(stateExpr);

        List<Object> paramValues = new ArrayList<>();
        String whereClause = filterSqlBuilder.buildWhereClause(outerAnd, paramValues);

        // Verify all conditions are present
        assertEquals(whereClause.contains("NAME = ?"), true, "Should contain NAME condition");
        assertEquals(whereClause.contains("TYPE = ?"), true, "Should contain TYPE condition");
        assertEquals(whereClause.contains("STATE = ?"), true, "Should contain STATE condition");

        // Verify three parameter values
        assertEquals(paramValues.size(), 3, "Should have three parameter values");
        assertEquals(paramValues.get(0), "Marketing", "First parameter should be 'Marketing'");
        assertEquals(paramValues.get(1), "Policy", "Second parameter should be 'Policy'");
        assertEquals(paramValues.get(2), "active", "Third parameter should be 'active'");
    }

    /**
     * Test 13: Null paramValues list throws exception
     * Input: null paramValues argument
     * Expected: IllegalArgumentException thrown
     */
    @Test
    public void testBuildWhereClauseWithNullParamValues() {

        ExpressionNode filterTree = new ExpressionNode();
        filterTree.setAttributeValue("name");
        filterTree.setOperation("eq");
        filterTree.setValue("Marketing");

        Assert.assertThrows(IllegalArgumentException.class, (Assert.ThrowingRunnable) () -> {
            filterSqlBuilder.buildWhereClause(filterTree, null);
        });
    }

    /**
     * Test 14: Null operator in expression node
     * Input: ExpressionNode with null operator (should fall back to "=" operator)
     * Expected: WHERE clause built with default "=" operator
     */
    @Test
    public void testBuildWhereClauseWithNullOperator() {

        ExpressionNode filterTree = new ExpressionNode();
        filterTree.setAttributeValue("name");
        filterTree.setOperation(null);
        filterTree.setValue("Marketing");

        List<Object> paramValues = new ArrayList<>();
        String whereClause = filterSqlBuilder.buildWhereClause(filterTree, paramValues);

        assertEquals(whereClause, "NAME = ?", "Null operator should default to '='");
        assertEquals(paramValues.size(), 1, "Should have one parameter value");
        assertEquals(paramValues.get(0), "Marketing", "Parameter value should be 'Marketing'");
    }

    /**
     * Test 15: OperationNode with null right child
     * Input: OperationNode with valid left node but null right node
     * Expected: null returned (incomplete operation)
     */
    @Test
    public void testBuildWhereClauseWithOperationNodeNullChild() {

        ExpressionNode leftExpr = new ExpressionNode();
        leftExpr.setAttributeValue("name");
        leftExpr.setOperation("eq");
        leftExpr.setValue("Marketing");

        OperationNode opNode = new OperationNode("and");
        opNode.setLeftNode(leftExpr);
        opNode.setRightNode(null);

        List<Object> paramValues = new ArrayList<>();
        String whereClause = filterSqlBuilder.buildWhereClause(opNode, paramValues);

        assertNull(whereClause, "OperationNode with null right child should return null");
        assertEquals(paramValues.size(), 0, "Should have no parameter values");
    }
}
