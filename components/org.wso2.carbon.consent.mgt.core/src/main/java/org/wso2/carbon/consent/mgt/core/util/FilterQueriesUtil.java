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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.FilterTreeBuilder;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_EXPRESSION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE;

/**
 * Utility for building FilterQueryBuilder instances from ExpressionNode lists.
 * Mirrors the pattern used in carbon-identity-framework APIResourceManagementDAOImpl.
 */
public class FilterQueriesUtil {

    public static final String AFTER = "after";
    public static final String BEFORE = "before";

    private FilterQueriesUtil() {}

    /**
     * Converts a filter string and optional cursor params into a flat ExpressionNode list.
     * Cursor nodes use operation "gt" (after) or "lt" (before); their values are
     * base64-decoded from the provided cursor strings.
     */
    public static List<ExpressionNode> getExpressionNodes(String filter, String after, String before)
            throws ConsentManagementClientException {

        List<ExpressionNode> nodes = new ArrayList<>();

        if (StringUtils.isNotBlank(filter)) {
            try {
                FilterTreeBuilder builder = new FilterTreeBuilder(filter);
                Node filterTree = builder.buildTree();
                collectExpressionNodes(filterTree, nodes);
            } catch (Exception e) {
                throw new ConsentManagementClientException(
                        String.format(ERROR_CODE_INVALID_FILTER_EXPRESSION.getMessage(), e.getMessage()),
                        ERROR_CODE_INVALID_FILTER_EXPRESSION.getCode());
            }
        }

        if (StringUtils.isNotBlank(after)) {
            ExpressionNode node = new ExpressionNode();
            node.setAttributeValue(AFTER);
            node.setOperation("gt");
            node.setValue(new String(Base64.getDecoder().decode(after), StandardCharsets.UTF_8));
            nodes.add(node);
        }

        if (StringUtils.isNotBlank(before)) {
            ExpressionNode node = new ExpressionNode();
            node.setAttributeValue(BEFORE);
            node.setOperation("lt");
            node.setValue(new String(Base64.getDecoder().decode(before), StandardCharsets.UTF_8));
            nodes.add(node);
        }

        return nodes;
    }

    /**
     * Builds a FilterQueryBuilder from an ExpressionNode list using the given attribute→column map.
     * Each node generates an {@code AND column op ?} fragment; parameter values are stored
     * in order inside the returned FilterQueryBuilder.
     */
    public static FilterQueryBuilder buildFilterQueryBuilder(List<ExpressionNode> nodes,
                                                             Map<String, String> attrColMap)
            throws ConsentManagementClientException {

        FilterQueryBuilder queryBuilder = new FilterQueryBuilder();
        if (nodes == null || nodes.isEmpty()) {
            return queryBuilder;
        }

        StringBuilder filterQuery = new StringBuilder();
        int paramCount = 0;

        for (ExpressionNode node : nodes) {
            String attr = node.getAttributeValue();
            String op = node.getOperation();
            String value = node.getValue();

            String column = attrColMap.get(attr);
            if (column == null) {
                throw new ConsentManagementClientException(
                        String.format(ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE.getMessage(), attr),
                        ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE.getCode());
            }

            filterQuery.append(" AND ").append(column).append(" ").append(toSqlOperator(op)).append(" ?");
            queryBuilder.setFilterAttributeValue(++paramCount, toSqlValue(op, value));
        }

        queryBuilder.setFilterQuery(filterQuery.toString());
        return queryBuilder;
    }

    /**
     * Base64-encodes a string value to use as an opaque cursor.
     */
    public static String encodeCursor(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes a base64 cursor to its original string value.
     */
    public static String decodeCursor(String cursor) {
        return new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
    }

    private static void collectExpressionNodes(Node node, List<ExpressionNode> expressions) {
        if (node instanceof ExpressionNode) {
            expressions.add((ExpressionNode) node);
        } else if (node instanceof OperationNode) {
            OperationNode opNode = (OperationNode) node;
            collectExpressionNodes(opNode.getLeftNode(), expressions);
            collectExpressionNodes(opNode.getRightNode(), expressions);
        }
    }

    private static String toSqlOperator(String op) {
        if (op == null) {
            return "=";
        }
        switch (op.toLowerCase()) {
            case "eq": return "=";
            case "ne": return "!=";
            case "sw": case "co": case "ew": return "LIKE";
            case "ge": return ">=";
            case "le": return "<=";
            case "gt": return ">";
            case "lt": return "<";
            default: return "=";
        }
    }

    private static String toSqlValue(String op, String value) {
        if (op == null || value == null) {
            return value;
        }
        switch (op.toLowerCase()) {
            case "sw": return value + "%";
            case "co": return "%" + value + "%";
            case "ew": return "%" + value;
            default: return value;
        }
    }
}
