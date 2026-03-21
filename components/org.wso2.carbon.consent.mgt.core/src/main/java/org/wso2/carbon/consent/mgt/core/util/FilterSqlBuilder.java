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

import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;

import java.util.List;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.FilterConstants;

/**
 * Builds parameterized SQL WHERE clauses from filter expression trees.
 * Traverses ExpressionNode and OperationNode structures to generate SQL conditions.
 */
public class FilterSqlBuilder {

    /**
     * Builds a WHERE clause from a filter tree node.
     *
     * @param filterTree   Root node of the filter tree (null = no filtering)
     * @param paramValues  List to collect SQL parameter values. Cannot be null.
     * @return WHERE clause string WITHOUT the "WHERE" keyword, or null if filterTree is null/invalid.
     *         Returns null if attribute or value is null OR EMPTY.
     * @throws IllegalArgumentException if paramValues is null
     */
    public String buildWhereClause(Node filterTree, List<Object> paramValues) {

        if (paramValues == null) {
            throw new IllegalArgumentException("paramValues list cannot be null");
        }

        if (filterTree == null) {
            return null;
        }

        if (filterTree instanceof ExpressionNode) {
            return buildExpressionClause((ExpressionNode) filterTree, paramValues);
        } else if (filterTree instanceof OperationNode) {
            return buildOperationClause((OperationNode) filterTree, paramValues);
        }

        return null;
    }

    /**
     * Builds a WHERE clause from a single expression node.
     * Handles: "attribute operator value" format
     *
     * @param node         Expression node containing attribute, operator, and value
     * @param paramValues  List to populate with parameter value
     * @return SQL clause like "ATTRIBUTE OPERATOR ?" or null if attribute/operator/value is null or empty
     */
    private String buildExpressionClause(ExpressionNode node, List<Object> paramValues) {

        String attribute = node.getAttributeValue();
        String operator = node.getOperation();
        String value = node.getValue();

        // Validate required fields
        if (attribute == null || attribute.isEmpty() || value == null || value.isEmpty()) {
            return null;
        }

        // Normalize attribute name to uppercase
        String sqlAttribute = attribute.toUpperCase();

        // Translate operator to SQL format
        String sqlOperator = translateOperator(operator);

        // Escape value for LIKE operators
        String escapedValue = escapeValue(operator, value);

        // Add parameter value to list
        paramValues.add(escapedValue);

        // Return parameterized SQL clause
        return sqlAttribute + " " + sqlOperator + " ?";
    }

    /**
     * Builds a WHERE clause from an operation node.
     * Handles: "left AND right" or "left OR right" logical operations
     *
     * @param node         Operation node containing left, right nodes and operation
     * @param paramValues  List to populate with parameter values
     * @return SQL clause like "(left_clause AND right_clause)", or null if either side is null
     */
    private String buildOperationClause(OperationNode node, List<Object> paramValues) {

        Node leftNode = node.getLeftNode();
        Node rightNode = node.getRightNode();
        String operation = node.getOperation();

        if (leftNode == null || rightNode == null) {
            return null;
        }

        // Recursively build both sides
        String leftClause = buildWhereClause(leftNode, paramValues);
        String rightClause = buildWhereClause(rightNode, paramValues);

        // If either side is null, return null
        if (leftClause == null || rightClause == null) {
            return null;
        }

        // Determine the SQL operation (AND or OR)
        String sqlOperation = translateLogicalOperator(operation);

        // Return combined clause with parentheses
        return "(" + leftClause + " " + sqlOperation + " " + rightClause + ")";
    }

    /**
     * Translates filter operator to SQL operator.
     *
     * @param operator Filter operator (e.g., "eq", "sw", "co", "ew")
     * @return SQL operator (e.g., "=", "LIKE")
     */
    private String translateOperator(String operator) {

        if (operator == null) {
            return FilterConstants.SQL_OP_EQUALS;
        }

        switch (operator.toLowerCase()) {
            case FilterConstants.OP_EQ:
                return FilterConstants.SQL_OP_EQUALS;
            case FilterConstants.OP_SW:
                return FilterConstants.SQL_OP_LIKE;
            case FilterConstants.OP_CO:
                return FilterConstants.SQL_OP_LIKE;
            case FilterConstants.OP_EW:
                return FilterConstants.SQL_OP_LIKE;
            default:
                return FilterConstants.SQL_OP_EQUALS;
        }
    }

    /**
     * Translates logical operator to SQL logical operator.
     *
     * @param operation Logical operation (e.g., "and", "or")
     * @return SQL logical operator ("AND" or "OR")
     */
    private String translateLogicalOperator(String operation) {

        if (operation == null) {
            return "AND";
        }

        if (operation.equalsIgnoreCase("or")) {
            return "OR";
        }

        return "AND";
    }

    /**
     * Escapes value for LIKE operators by adding wildcards.
     *
     * @param operator Filter operator (used to determine wildcard placement)
     * @param value    Original value to escape
     * @return Escaped value with wildcards for LIKE, or original value for equals
     */
    private String escapeValue(String operator, String value) {

        if (value == null) {
            return value;
        }

        if (operator == null) {
            return value;
        }

        switch (operator.toLowerCase()) {
            case FilterConstants.OP_SW:
                // Starts with: "value%"
                return value + FilterConstants.LIKE_WILDCARD_END;
            case FilterConstants.OP_CO:
                // Contains: "%value%"
                return FilterConstants.LIKE_WILDCARD_START + value + FilterConstants.LIKE_WILDCARD_END;
            case FilterConstants.OP_EW:
                // Ends with: "%value"
                return FilterConstants.LIKE_WILDCARD_START + value;
            default:
                // For "eq" and others, return unchanged
                return value;
        }
    }
}
