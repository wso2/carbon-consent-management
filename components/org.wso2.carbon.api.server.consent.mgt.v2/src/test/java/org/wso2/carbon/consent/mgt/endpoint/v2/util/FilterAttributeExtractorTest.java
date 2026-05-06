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
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;

import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for FilterAttributeExtractor - attribute extraction and validation.
 */
public class FilterAttributeExtractorTest {

    private FilterAttributeExtractor extractor = new FilterAttributeExtractor();

    /**
     * Test 1: Validation succeeds for supported attributes
     * Input: Filter tree for "name eq Marketing and type eq Policy"
     * Supported attributes: {name, type}
     * Expected: No exception thrown
     */
    @Test
    public void testValidateFilterAttributesSucceedsForSupportedAttributes() throws ConsentManagementException {

        // Create filter tree: "name eq Marketing and type eq Policy"
        ExpressionNode nameExpr = new ExpressionNode();
        nameExpr.setAttributeValue("name");
        nameExpr.setValue("Marketing");

        ExpressionNode typeExpr = new ExpressionNode();
        typeExpr.setAttributeValue("type");
        typeExpr.setValue("Policy");

        OperationNode rootNode = new OperationNode("and");
        rootNode.setLeftNode(nameExpr);
        rootNode.setRightNode(typeExpr);

        Set<String> supportedAttributes = new HashSet<>();
        supportedAttributes.add("name");
        supportedAttributes.add("type");

        // Should not throw exception
        extractor.validateFilterAttributes(rootNode, supportedAttributes);
    }

    /**
     * Test 2: Validation fails for unsupported attribute
     * Input: Filter tree for "invalidAttr eq value"
     * Supported attributes: {name, type}
     * Expected: ConsentManagementException with ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE
     */
    @Test(expectedExceptions = ConsentManagementException.class)
    public void testValidateFilterAttributesFailsForUnsupportedAttribute() throws ConsentManagementException {

        // Create filter tree with unsupported attribute
        ExpressionNode invalidExpr = new ExpressionNode();
        invalidExpr.setAttributeValue("invalidAttr");
        invalidExpr.setValue("value");

        Set<String> supportedAttributes = new HashSet<>();
        supportedAttributes.add("name");
        supportedAttributes.add("type");

        // Should throw exception
        extractor.validateFilterAttributes(invalidExpr, supportedAttributes);
    }

    /**
     * Test 3: Validation handles case-insensitive attribute names
     * Input: Filter tree with "NAME" (uppercase)
     * Supported attributes: {name} (lowercase)
     * Expected: No exception thrown
     */
    @Test
    public void testValidateFilterAttributesCaseInsensitive() throws ConsentManagementException {

        // Create filter tree with uppercase attribute
        ExpressionNode nameExpr = new ExpressionNode();
        nameExpr.setAttributeValue("NAME");
        nameExpr.setValue("Marketing");

        Set<String> supportedAttributes = new HashSet<>();
        supportedAttributes.add("name");

        // Should not throw exception (case-insensitive match)
        extractor.validateFilterAttributes(nameExpr, supportedAttributes);
    }

    /**
     * Test 4: Validation handles null nodes gracefully
     * Input: null root node
     * Expected: No exception thrown
     */
    @Test
    public void testValidateFilterAttributesWithNullNode() throws ConsentManagementException {

        Set<String> supportedAttributes = new HashSet<>();
        supportedAttributes.add("name");

        // Should not throw exception for null
        extractor.validateFilterAttributes(null, supportedAttributes);
    }

    /**
     * Test 5: Extract attribute names from simple expression
     * Input: Filter tree for "name eq Marketing"
     * Expected: Set containing {"name"}
     */
    @Test
    public void testExtractAttributeNamesFromSimpleExpression() {

        ExpressionNode nameExpr = new ExpressionNode();
        nameExpr.setAttributeValue("name");
        nameExpr.setValue("Marketing");

        Set<String> attributeNames = extractor.extractAttributeNames(nameExpr);

        assertEquals(attributeNames.size(), 1);
        assertTrue(attributeNames.contains("name"));
    }

    /**
     * Test 6: Extract attribute names from complex tree
     * Input: Filter tree for "name eq Marketing and type eq Policy"
     * Expected: Set containing {"name", "type"}
     */
    @Test
    public void testExtractAttributeNamesFromComplexTree() {

        ExpressionNode nameExpr = new ExpressionNode();
        nameExpr.setAttributeValue("name");
        nameExpr.setValue("Marketing");

        ExpressionNode typeExpr = new ExpressionNode();
        typeExpr.setAttributeValue("type");
        typeExpr.setValue("Policy");

        OperationNode rootNode = new OperationNode("and");
        rootNode.setLeftNode(nameExpr);
        rootNode.setRightNode(typeExpr);

        Set<String> attributeNames = extractor.extractAttributeNames(rootNode);

        assertEquals(attributeNames.size(), 2);
        assertTrue(attributeNames.contains("name"));
        assertTrue(attributeNames.contains("type"));
    }

    /**
     * Test 7: Extract attribute names with null attributes
     * Input: Filter tree with null attribute values
     * Expected: Empty set
     */
    @Test
    public void testExtractAttributeNamesWithNullAttributes() {

        ExpressionNode exprNode = new ExpressionNode();
        exprNode.setAttributeValue(null);
        exprNode.setValue("value");

        Set<String> attributeNames = extractor.extractAttributeNames(exprNode);

        assertEquals(attributeNames.size(), 0);
    }

    /**
     * Test 8: Extract attribute names from null node
     * Input: null root node
     * Expected: Empty set
     */
    @Test
    public void testExtractAttributeNamesFromNullNode() {

        Set<String> attributeNames = extractor.extractAttributeNames(null);

        assertEquals(attributeNames.size(), 0);
    }

    /**
     * Test 9: Validation with multiple unsupported attributes
     * Input: Filter tree for "invalidAttr eq value and otherInvalid eq test"
     * Supported attributes: {name, type}
     * Expected: ConsentManagementException
     */
    @Test(expectedExceptions = ConsentManagementException.class)
    public void testValidateFilterAttributesMultipleUnsupported() throws ConsentManagementException {

        ExpressionNode invalidExpr1 = new ExpressionNode();
        invalidExpr1.setAttributeValue("invalidAttr");
        invalidExpr1.setValue("value");

        ExpressionNode invalidExpr2 = new ExpressionNode();
        invalidExpr2.setAttributeValue("otherInvalid");
        invalidExpr2.setValue("test");

        OperationNode rootNode = new OperationNode("and");
        rootNode.setLeftNode(invalidExpr1);
        rootNode.setRightNode(invalidExpr2);

        Set<String> supportedAttributes = new HashSet<>();
        supportedAttributes.add("name");
        supportedAttributes.add("type");

        // Should throw exception for first unsupported attribute
        extractor.validateFilterAttributes(rootNode, supportedAttributes);
    }

    /**
     * Test 10: Extract attribute names normalizes to lowercase
     * Input: Filter tree with "NAME" (uppercase)
     * Expected: Set containing {"name"} (lowercase)
     */
    @Test
    public void testExtractAttributeNamesNormalizesToLowercase() {

        ExpressionNode nameExpr = new ExpressionNode();
        nameExpr.setAttributeValue("NAME");
        nameExpr.setValue("Marketing");

        Set<String> attributeNames = extractor.extractAttributeNames(nameExpr);

        assertEquals(attributeNames.size(), 1);
        assertTrue(attributeNames.contains("name"));
    }
}
