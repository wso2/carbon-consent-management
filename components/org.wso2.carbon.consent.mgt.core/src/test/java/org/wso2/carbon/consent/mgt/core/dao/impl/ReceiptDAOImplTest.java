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

package org.wso2.carbon.consent.mgt.core.dao.impl;

import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.dao.ReceiptDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.internal.ConsentManagerComponentDataHolder;
import org.wso2.carbon.consent.mgt.core.model.Address;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.PIICategoryValidity;
import org.wso2.carbon.consent.mgt.core.model.PiiController;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptUpdateInput;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import org.wso2.carbon.consent.mgt.core.model.ConsentAuthorization;
import org.wso2.carbon.consent.mgt.core.util.FilterQueriesUtil;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.closeH2Base;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.getConnection;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.mockComponentDataHolder;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.spyConnection;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.spyConnectionWithError;

public class ReceiptDAOImplTest {

    private static List<ReceiptInput> receiptInputs = new ArrayList<>();

    private MockedStatic<PrivilegedCarbonContext> mockedCarbonContext;
    private AutoCloseable mockitoCloseable;

    @BeforeMethod
    public void setUp() throws Exception {

        mockitoCloseable = MockitoAnnotations.openMocks(this);
        initiateH2Base();

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        DataSource dataSource = mock(DataSource.class);
        
        try (MockedStatic<ConsentManagerComponentDataHolder> mockedDataHolder = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            Purpose purpose1 = new Purpose("P1", "D1", "SIGNUP", "RESIDENT", -1234);
            Purpose purpose2 = new Purpose("P2", "D3", "JIT", "IDP", -1234);
            purposeDAO.addPurposeWithUuid(purpose1);
            purposeDAO.addPurposeWithUuid(purpose2);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
            PurposeCategory purposeCategory = new PurposeCategory("PC1", "D1", -1234);
            purposeCategoryDAO.addPurposeCategory(purposeCategory);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
            PIICategory piiCategory = new PIICategory("PII1", "D1", true, -1234);
            piiCategoryDAO.addPIICategory(piiCategory);
        }

        String collectionMethod = "Sign-up";
        String jurisdiction = "LK";
        String principleID1 = "subject1";
        String principleID2 = "subject2";
        String language = "EN";
        String policyUrl = "http://foo.com/policy";
        String service1 = "foo-company";
        String serviceDisplayName = "Foo Company";
        String serviceDescription = "foo company";
        String tenantDomain = "carbon.super";
        int tenantId = -1234;
        String consentType = "EXPLICIT";
        String termination = "1 year";
        String version = "KI-CR-v1.1.0";
        String piiControllerInput = "{\n" +
                "      \"piiController\": \"samplePiiController\",\n" +
                "      \"contact\": \"sample\",\n" +
                "      \"address\": {\n" +
                "        \"addressCountry\": \"country\",\n" +
                "        \"addressLocality\": \"locality\",\n" +
                "        \"addressRegion\": \"region\",\n" +
                "        \"postOfficeBoxNumber\": \"box\",\n" +
                "        \"postalCode\": \"code\",\n" +
                "        \"streetAddress\": \"address\"\n" +
                "      },\n" +
                "      \"email\": \"mail\",\n" +
                "      \"phone\": \"phone\",\n" +
                "      \"onBehalf\": true,\n" +
                "      \"piiControllerUrl\": \"sample.com\"\n" +
                "    }";

        List<ReceiptServiceInput> serviceInputs = new ArrayList<>();
        List<ReceiptPurposeInput> purposeInputs = new ArrayList<>();
        List<PiiController> piiControllers = new ArrayList<>();
        List<Integer> purposeCategoryIds = new ArrayList<>();
        List<PIICategoryValidity> piiCategoryIds = new ArrayList<>();
        Map<String, String> properties = new HashMap<>();

        purposeCategoryIds.add(1);
        piiCategoryIds.add(new PIICategoryValidity("http://wso2.org/claims/lastname", "45", 1, "Last Name", true));
        properties.put("K1", "V1");
        properties.put("K2", "V2");

        ReceiptPurposeInput purposeInput1 = new ReceiptPurposeInput();
        purposeInput1.setPrimaryPurpose(true);
        purposeInput1.setTermination(termination);
        purposeInput1.setConsentType(consentType);
        purposeInput1.setThirdPartyDisclosure(false);
        purposeInput1.setPurposeId(1);
        purposeInput1.setPurposeCategoryId(purposeCategoryIds);
        purposeInput1.setPiiCategory(piiCategoryIds);

        ReceiptPurposeInput purposeInput2 = new ReceiptPurposeInput();
        purposeInput2.setPrimaryPurpose(false);
        purposeInput2.setTermination(termination);
        purposeInput2.setConsentType(consentType);
        purposeInput2.setThirdPartyDisclosure(true);
        purposeInput2.setThirdPartyName("bar-company");
        purposeInput2.setPurposeId(2);
        purposeInput2.setPurposeCategoryId(purposeCategoryIds);
        purposeInput2.setPiiCategory(piiCategoryIds);

        purposeInputs.add(purposeInput1);
        purposeInputs.add(purposeInput2);

        ReceiptServiceInput serviceInput = new ReceiptServiceInput();
        serviceInput.setPurposes(purposeInputs);
        serviceInput.setTenantDomain(tenantDomain);
        serviceInput.setTenantId(tenantId);
        serviceInput.setService(service1);
        serviceInput.setSpDisplayName(serviceDisplayName);
        serviceInput.setSpDescription(serviceDescription);

        serviceInputs.add(serviceInput);

        Address address = new Address("LK", "EN", "South", "1435", "10443", "2nd Street, Colombo 03");
        PiiController piiController = new PiiController("ACME", false, "John Wick", "johnw@acme.com",
                "+17834563445", "http://acme.com", address);
        piiControllers.add(piiController);

        ReceiptInput receiptInput1 = new ReceiptInput();

        receiptInput1.setConsentReceiptId(UUID.randomUUID().toString());
        receiptInput1.setCollectionMethod(collectionMethod);
        receiptInput1.setJurisdiction(jurisdiction);
        receiptInput1.setPiiPrincipalId(principleID1);
        receiptInput1.setLanguage(language);
        receiptInput1.setPolicyUrl(policyUrl);
        receiptInput1.setServices(serviceInputs);
        receiptInput1.setTenantDomain(tenantDomain);
        receiptInput1.setTenantId(tenantId);
        receiptInput1.setState(ConsentConstants.ACTIVE_STATE);
        receiptInput1.setVersion(version);
        receiptInput1.setPiiControllerInfo(piiControllerInput);
        receiptInput1.setProperties(properties);

        ReceiptInput receiptInput2 = new ReceiptInput();

        receiptInput2.setConsentReceiptId(UUID.randomUUID().toString());
        receiptInput2.setCollectionMethod(collectionMethod);
        receiptInput2.setJurisdiction(jurisdiction);
        receiptInput2.setPiiPrincipalId(principleID2);
        receiptInput2.setLanguage(language);
        receiptInput2.setPolicyUrl(policyUrl);
        receiptInput2.setServices(serviceInputs);
        receiptInput2.setState(ConsentConstants.ACTIVE_STATE);
        receiptInput2.setVersion(version);
        receiptInput2.setPiiControllerInfo(piiControllerInput);
        receiptInput2.setProperties(properties);
        receiptInput2.setTenantDomain(tenantDomain);
        receiptInput2.setTenantId(tenantId);

        receiptInputs.add(receiptInput1);
        receiptInputs.add(receiptInput2);
        mockCarbonContext();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        closeH2Base();
        
        if (mockedCarbonContext != null) {
            mockedCarbonContext.close();
        }
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    @DataProvider(name = "exceptionLevelProvider")
    public Object[][] provideExceptionLevelData() throws Exception {

        return new Object[][]{
                // exception level
                {1},
                {2}
        };
    }

    @DataProvider(name = "receiptSearchProvider")
    public Object[][] provideListData() throws Exception {

        return new Object[][]{
                // limit, offset, principalId, tenantDomain, service, state, resultCount
                {10, 0, "subject1", -1234, "foo-company", "ACTIVE", 1},
                {10, 0, "subject1", -1234, "foo-company", null, 1},
                {10, 0, "subject1", -1234, null, null, 1},
                {10, 0, "subject1", 0, null, null, 1},
                {10, 0, null, 0, null, null, 2},
                {10, 1, null, 0, null, null, 1},
                {1, 1, null, 0, null, null, 1},
                {0, 0, null, 0, null, null, 0},
                {10, 0, "subject*", 0, null, null, 2},
                {10, 0, null, -1234, null, null, 2},
                {10, 0, null, 0, "foo*", null, 2}
        };
    }

    @Test
    public void testAddReceipt() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0));

            Receipt receipt = receiptDAO.getReceipt(receiptInputs.get(0).getConsentReceiptId());

            Assert.assertNotNull(receipt, "Receipt cannot be null for a valid receipt ID.");
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testAddDuplicateReceipt() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0));
            receiptDAO.addReceipt(receiptInputs.get(0));
        }
    }

    @Test(dataProvider = "exceptionLevelProvider", expectedExceptions = ConsentManagementServerException.class)
    public void testAddReceiptWithException(int exceptionLevel) throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);

            if (exceptionLevel == 1) {
                when(dataSource.getConnection()).thenThrow(new SQLException("Test exception"));
            } else if (exceptionLevel == 2) {
                when(dataSource.getConnection()).thenReturn(spy);
                when(spy.prepareStatement(anyString())).thenThrow(new SQLException("Test exception"));
            } else if (exceptionLevel == 3) {
                when(dataSource.getConnection()).thenReturn(spy).thenReturn(spy)
                        .thenThrow(new SQLException("Test exception"));
            }

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0));
        }
    }

    @Test(dataProvider = "receiptSearchProvider")
    public void testSearchReceipts(int limit, int offset, String principalId, int tenantId, String service,
                                   String state, int resultCount) throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0));
            receiptDAO.addReceipt(receiptInputs.get(1));

            List<ReceiptListResponse> receiptResponses = receiptDAO.searchReceipts(limit, offset, principalId,
                    tenantId, service, state, tenantId);
            Assert.assertNotNull(receiptResponses);
            Assert.assertEquals(receiptResponses.size(), resultCount);
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testSearchReceiptsWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {
            Connection spy = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.searchReceipts(1, 0, "subject1", -1234, "foo*", "ACTIVE", -1234);
        }
    }

    @Test
    public void testRevokeReceipt() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0));
            receiptDAO.revokeReceipt(receiptInputs.get(0).getConsentReceiptId());
            Receipt receipt = receiptDAO.getReceipt(receiptInputs.get(0).getConsentReceiptId());

            Assert.assertEquals(receipt.getState(), ConsentConstants.REVOKE_STATE);
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testRevokeReceiptWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {
            Connection spy = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.revokeReceipt(receiptInputs.get(0).getConsentReceiptId());
        }
    }

    @Test
    public void testGetReceipt() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(1));
            Receipt receipt = receiptDAO.getReceipt(receiptInputs.get(1).getConsentReceiptId());

            Assert.assertNotNull(receipt);
        }
    }

    @Test
    public void testGetInvalidReceipt() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(1));
            Receipt receipt = receiptDAO.getReceipt("invalid-receipt-id");

            Assert.assertNull(receipt);
        }
    }

    @Test(dataProvider = "exceptionLevelProvider", expectedExceptions = ConsentManagementServerException.class)
    public void testGetReceiptWithException(int exceptionLevel) throws Exception {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource)) {
            try (Connection connection = getConnection()) {
                Connection spy = spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spy);

                ReceiptDAO receiptDAO = new ReceiptDAOImpl();
                receiptDAO.addReceipt(receiptInputs.get(1));
            }

            try (Connection connection = getConnection()) {
                Connection spy = spyConnection(connection);

                if (exceptionLevel == 1) {
                    when(dataSource.getConnection()).thenThrow(new SQLException("Test exception"));
                } else if (exceptionLevel == 2) {
                    when(dataSource.getConnection()).thenReturn(spy);
                    when(spy.prepareStatement(anyString())).thenThrow(new SQLException("Test exception"));
                }

                ReceiptDAO receiptDAO = new ReceiptDAOImpl();
                receiptDAO.getReceipt(receiptInputs.get(1).getConsentReceiptId());
            }
        }
    }

    @DataProvider(name = "listReceiptsProvider")
    public Object[][] provideListReceiptsData() {

        return new Object[][]{
                // subjectId, serviceId, state, purposeId, purposeVersionId, limit, offset, tenantId, expectedCount
                {null,      null,         null,     null, null, 10, 0, SUPER_TENANT_ID, 2},
                {"subject1", null,        null,     null, null, 10, 0, SUPER_TENANT_ID, 1},
                {"subject2", null,        null,     null, null, 10, 0, SUPER_TENANT_ID, 1},
                {null,      "foo-company", null,    null, null, 10, 0, SUPER_TENANT_ID, 2},
                {null,      null,         "ACTIVE", null, null, 10, 0, SUPER_TENANT_ID, 2},
                {null,      null,         "REVOKED",null, null, 10, 0, SUPER_TENANT_ID, 0},
                {null,      null,         null,     null, null,  1, 0, SUPER_TENANT_ID, 1},
                {null,      null,         null,     null, null, 10, 1, SUPER_TENANT_ID, 1},
                {null,      null,         null,     null, null,  0, 0, SUPER_TENANT_ID, 0},
                {"nonexistent", null,     null,     null, null, 10, 0, SUPER_TENANT_ID, 0},
        };
    }

    @Test(dataProvider = "listReceiptsProvider")
    public void testListReceipts(String subjectId, String serviceId, String state, String purposeId,
            String purposeVersionId, int limit, int offset, int tenantId, int expectedCount)
            throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0));
            if (offset > 0) {
                Thread.sleep(10);
            }
            receiptDAO.addReceipt(receiptInputs.get(1));

            List<ExpressionNode> expressionNodes = Collections.emptyList();
            if (offset > 0) {
                List<Receipt> firstPage = receiptDAO.listReceipts(null, null, null, null, null,
                        1, tenantId, Collections.emptyList());
                String after = java.util.Base64.getEncoder().encodeToString(
                        firstPage.get(0).getCursor().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                expressionNodes = FilterQueriesUtil.getExpressionNodes(null, after, null);
            }

            List<Receipt> results = receiptDAO.listReceipts(subjectId, serviceId, state, purposeId,
                    purposeVersionId, limit, tenantId, expressionNodes);

            Assert.assertNotNull(results);
            Assert.assertEquals(results.size(), expectedCount,
                    String.format("Expected %d receipts for subjectId=%s, serviceId=%s, state=%s, limit=%d, offset=%d",
                            expectedCount, subjectId, serviceId, state, limit, offset));
        }
    }

    @Test
    public void testListReceipts_allFiltersNull_returnsAll() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0));
            receiptDAO.addReceipt(receiptInputs.get(1));

            List<Receipt> results = receiptDAO.listReceipts(null, null, null, null, null, 100,
                    SUPER_TENANT_ID, Collections.emptyList());

            Assert.assertNotNull(results);
            Assert.assertEquals(results.size(), 2, "All receipts should be returned when no filters applied");
        }
    }

    @Test
    public void testListReceipts_nonExistentPurposeVersionId_returnsEmpty() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0));

            // A random UUID that doesn't exist as a purposeVersionId.
            String unknownVersionId = UUID.randomUUID().toString();
            List<Receipt> results = receiptDAO.listReceipts(null, null, null, null, unknownVersionId, 10,
                    SUPER_TENANT_ID, Collections.emptyList());

            Assert.assertNotNull(results);
            Assert.assertEquals(results.size(), 0, "No receipts should match an unknown purposeVersionId");
        }
    }

    @Test
    public void testListReceipts_receiptsContainExpectedFields() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0));

            List<Receipt> results = receiptDAO.listReceipts("subject1", null, null, null, null, 10,
                    SUPER_TENANT_ID, Collections.emptyList());

            Assert.assertNotNull(results);
            Assert.assertEquals(results.size(), 1);

            Receipt receipt = results.get(0);
            Assert.assertNotNull(receipt.getConsentReceiptId(), "consentReceiptId must not be null");
            Assert.assertEquals(receipt.getPiiPrincipalId(), "subject1", "piiPrincipalId must match");
            Assert.assertEquals(receipt.getState(), ConsentConstants.ACTIVE_STATE, "state must be ACTIVE");
            Assert.assertNotNull(receipt.getServices(), "services must not be null");
        }
    }

    @Test
    public void testInsertConsentAuthorization_withType_typeStored() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> mockedHolder = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0));

            String receiptId = receiptInputs.get(0).getConsentReceiptId();
            ConsentAuthorization auth = new ConsentAuthorization(
                    receiptId, "alice@example.com",
                    ConsentAuthorization.AuthorizationStatus.PENDING,
                    System.currentTimeMillis(), "DELEGATE");
            receiptDAO.insertConsentAuthorization(auth);

            List<ConsentAuthorization> auths = receiptDAO.getConsentAuthorizations(receiptId);
            Assert.assertEquals(auths.size(), 1);
            Assert.assertEquals(auths.get(0).getUserId(), "alice@example.com");
            Assert.assertEquals(auths.get(0).getType(), "DELEGATE");
        }
    }

    @Test
    public void testInsertConsentAuthorization_withoutType_typeIsNull() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> mockedHolder = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0));

            String receiptId = receiptInputs.get(0).getConsentReceiptId();
            ConsentAuthorization auth = new ConsentAuthorization(
                    receiptId, "bob@example.com",
                    ConsentAuthorization.AuthorizationStatus.PENDING,
                    System.currentTimeMillis(), null);
            receiptDAO.insertConsentAuthorization(auth);

            ConsentAuthorization fetched = receiptDAO.getConsentAuthorizationByUser(receiptId, "bob@example.com");
            Assert.assertNotNull(fetched);
            Assert.assertEquals(fetched.getUserId(), "bob@example.com");
            Assert.assertNull(fetched.getType(), "type should be null when not set");
        }
    }

    @Test
    public void testGetConsentAuthorizations_multipleWithType_allTypesReturned() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> mockedHolder = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0));
            String receiptId = receiptInputs.get(0).getConsentReceiptId();

            receiptDAO.insertConsentAuthorization(new ConsentAuthorization(
                    receiptId, "user1", ConsentAuthorization.AuthorizationStatus.PENDING,
                    System.currentTimeMillis(), "GUARDIAN"));
            receiptDAO.insertConsentAuthorization(new ConsentAuthorization(
                    receiptId, "user2", ConsentAuthorization.AuthorizationStatus.PENDING,
                    System.currentTimeMillis(), "LEGAL_REP"));

            List<ConsentAuthorization> auths = receiptDAO.getConsentAuthorizations(receiptId);
            Assert.assertEquals(auths.size(), 2);

            Map<String, String> typeByUser = new HashMap<>();
            auths.forEach(a -> typeByUser.put(a.getUserId(), a.getType()));
            Assert.assertEquals(typeByUser.get("user1"), "GUARDIAN");
            Assert.assertEquals(typeByUser.get("user2"), "LEGAL_REP");
        }
    }

    @Test
    public void testListReceipts_propertyFilter_matchingKeyValue_returnsReceipt() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> mockedHolder = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0)); // has K1=V1, K2=V2

            // Filter: properties.K1 eq V1
            List<ExpressionNode> nodes = FilterQueriesUtil.getExpressionNodes("properties.K1 eq V1", null, null);
            List<Receipt> results = receiptDAO.listReceipts(null, null, null, null, null,
                    100, SUPER_TENANT_ID, nodes);

            Assert.assertEquals(results.size(), 1, "Matching property filter should return the receipt");
            Assert.assertEquals(results.get(0).getPiiPrincipalId(), "subject1");
        }
    }

    @Test
    public void testListReceipts_propertyFilter_nonMatchingValue_returnsEmpty() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> mockedHolder = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0)); // has K1=V1

            List<ExpressionNode> nodes = FilterQueriesUtil.getExpressionNodes("properties.K1 eq WRONG", null, null);
            List<Receipt> results = receiptDAO.listReceipts(null, null, null, null, null,
                    100, SUPER_TENANT_ID, nodes);

            Assert.assertEquals(results.size(), 0, "Non-matching property filter should return empty");
        }
    }

    @Test
    public void testListReceipts_propertyFilter_multipleProperties_allMustMatch() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> mockedHolder = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0)); // has K1=V1, K2=V2

            // Both match → should find the receipt
            List<ExpressionNode> bothMatch = FilterQueriesUtil.getExpressionNodes(
                    "properties.K1 eq V1 and properties.K2 eq V2", null, null);
            List<Receipt> matched = receiptDAO.listReceipts(null, null, null, null, null,
                    100, SUPER_TENANT_ID, bothMatch);
            Assert.assertEquals(matched.size(), 1, "Both properties matching should return the receipt");

            // One mismatch → should return empty
            List<ExpressionNode> oneMismatch = FilterQueriesUtil.getExpressionNodes(
                    "properties.K1 eq V1 and properties.K2 eq WRONG", null, null);
            List<Receipt> noMatch = receiptDAO.listReceipts(null, null, null, null, null,
                    100, SUPER_TENANT_ID, oneMismatch);
            Assert.assertEquals(noMatch.size(), 0, "One mismatching property should return empty");
        }
    }

    @Test
    public void testListReceipts_propertyFilter_onlyMatchesCorrectReceipt() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> mockedHolder = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            // receiptInput1: K1=V1, K2=V2; receiptInput2: K1=V1, K2=V2 as well (same setup)
            // Override receiptInput2 to have K1=UNIQUE
            ReceiptInput uniqueReceipt = cloneReceiptInput(receiptInputs.get(1));
            Map<String, String> uniqueProps = new HashMap<>();
            uniqueProps.put("K1", "UNIQUE");
            uniqueReceipt.setProperties(uniqueProps);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0)); // K1=V1
            receiptDAO.addReceipt(uniqueReceipt);         // K1=UNIQUE

            List<ExpressionNode> nodes = FilterQueriesUtil.getExpressionNodes("properties.K1 eq UNIQUE", null, null);
            List<Receipt> results = receiptDAO.listReceipts(null, null, null, null, null,
                    100, SUPER_TENANT_ID, nodes);

            Assert.assertEquals(results.size(), 1, "Filter should only return the receipt with the matching property");
            Assert.assertEquals(results.get(0).getPiiPrincipalId(), "subject2");
        }
    }

    @Test
    public void testListReceipts_propertyFilter_startsWith_returnsReceipt() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> mockedHolder = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0)); // has K1=V1

            // 'sw' V → matches V1; 'sw' X → no match.
            List<ExpressionNode> match = FilterQueriesUtil.getExpressionNodes("properties.K1 sw V", null, null);
            Assert.assertEquals(receiptDAO.listReceipts(null, null, null, null, null,
                    100, SUPER_TENANT_ID, match).size(), 1, "'sw V' should match value 'V1'");

            List<ExpressionNode> noMatch = FilterQueriesUtil.getExpressionNodes("properties.K1 sw X", null, null);
            Assert.assertEquals(receiptDAO.listReceipts(null, null, null, null, null,
                    100, SUPER_TENANT_ID, noMatch).size(), 0, "'sw X' should not match value 'V1'");
        }
    }

    @Test
    public void testListReceipts_propertyFilter_contains_returnsReceipt() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> mockedHolder = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0)); // has K1=V1

            // 'co' 1 → matches V1; 'co' Z → no match.
            List<ExpressionNode> match = FilterQueriesUtil.getExpressionNodes("properties.K1 co 1", null, null);
            Assert.assertEquals(receiptDAO.listReceipts(null, null, null, null, null,
                    100, SUPER_TENANT_ID, match).size(), 1, "'co 1' should match value 'V1'");

            List<ExpressionNode> noMatch = FilterQueriesUtil.getExpressionNodes("properties.K1 co Z", null, null);
            Assert.assertEquals(receiptDAO.listReceipts(null, null, null, null, null,
                    100, SUPER_TENANT_ID, noMatch).size(), 0, "'co Z' should not match value 'V1'");
        }
    }

    @Test
    public void testListReceipts_propertyFilter_endsWith_returnsReceipt() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> mockedHolder = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            receiptDAO.addReceipt(receiptInputs.get(0)); // has K1=V1

            // 'ew' 1 → matches V1; 'ew' V → no match (value ends with '1', not 'V').
            List<ExpressionNode> match = FilterQueriesUtil.getExpressionNodes("properties.K1 ew 1", null, null);
            Assert.assertEquals(receiptDAO.listReceipts(null, null, null, null, null,
                    100, SUPER_TENANT_ID, match).size(), 1, "'ew 1' should match value 'V1'");

            List<ExpressionNode> noMatch = FilterQueriesUtil.getExpressionNodes("properties.K1 ew V", null, null);
            Assert.assertEquals(receiptDAO.listReceipts(null, null, null, null, null,
                    100, SUPER_TENANT_ID, noMatch).size(), 0, "'ew V' should not match value 'V1'");
        }
    }

    @Test
    public void testAddReceipt_secondReceipt_revokesFirst() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();

            receiptDAO.addReceipt(receiptInputs.get(0));

            ReceiptInput second = cloneReceiptInput(receiptInputs.get(0));
            second.setConsentReceiptId(UUID.randomUUID().toString());
            receiptDAO.addReceipt(second);

            Receipt first = receiptDAO.getReceipt(receiptInputs.get(0).getConsentReceiptId());
            Assert.assertEquals(first.getState(), ConsentConstants.REVOKE_STATE,
                    "First receipt must be revoked when a second consent is added for the same user+service.");
        }
    }

    /**
     * Mirrors {@code ConsentManagerImpl.calculateConsentStatus} so the DAO test exercises the same
     * state-derivation rule the production code passes into {@code updateConsent}.
     */
    private static final Function<List<ConsentAuthorization>, String> STATUS_CALCULATOR = authorizations -> {
        if (authorizations == null || authorizations.isEmpty()) {
            return ConsentConstants.PENDING_STATE;
        }
        boolean anyRevoked = false;
        boolean anyRejected = false;
        boolean allApproved = true;
        for (ConsentAuthorization authorization : authorizations) {
            ConsentAuthorization.AuthorizationStatus status = authorization.getStatus();
            if (ConsentAuthorization.AuthorizationStatus.REVOKED.equals(status)) {
                anyRevoked = true;
            } else if (ConsentAuthorization.AuthorizationStatus.REJECTED.equals(status)) {
                anyRejected = true;
            }
            if (!ConsentAuthorization.AuthorizationStatus.APPROVED.equals(status)) {
                allApproved = false;
            }
        }
        if (anyRevoked) {
            return ConsentConstants.REVOKE_STATE;
        }
        if (allApproved) {
            return ConsentConstants.ACTIVE_STATE;
        }
        if (anyRejected) {
            return ConsentConstants.REJECTED_STATE;
        }
        return ConsentConstants.PENDING_STATE;
    };

    @Test
    public void testUpdateConsentRecomputesStateFromAuthorizations() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            ReceiptInput receipt = receiptInputs.get(0);
            receiptDAO.addReceipt(receipt);
            String consentId = receipt.getConsentReceiptId();

            ReceiptUpdateInput updateInput = new ReceiptUpdateInput();
            updateInput.setConsentReceiptId(consentId);
            updateInput.setAuthorizations(Arrays.asList(
                    authorization("user1", ConsentAuthorization.AuthorizationStatus.APPROVED),
                    authorization("user2", ConsentAuthorization.AuthorizationStatus.REVOKED)));

            receiptDAO.updateConsent(updateInput, STATUS_CALCULATOR);

            Assert.assertEquals(receiptDAO.getConsentAuthorizations(consentId).size(), 2,
                    "Both authorizations should be inserted within the update.");
            Assert.assertEquals(receiptDAO.getReceipt(consentId).getState(), ConsentConstants.REVOKE_STATE,
                    "State must be recomputed atomically from the post-update authorizations (any REVOKED wins).");
        }
    }

    @Test
    public void testUpdateConsentUpdatesExistingAuthorizationAndRecomputesState() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            ReceiptInput receipt = receiptInputs.get(0);
            receiptDAO.addReceipt(receipt);
            String consentId = receipt.getConsentReceiptId();

            ReceiptUpdateInput insert = new ReceiptUpdateInput();
            insert.setConsentReceiptId(consentId);
            insert.setAuthorizations(Collections.singletonList(
                    authorization("user1", ConsentAuthorization.AuthorizationStatus.APPROVED)));
            receiptDAO.updateConsent(insert, STATUS_CALCULATOR);
            Assert.assertEquals(receiptDAO.getReceipt(consentId).getState(), ConsentConstants.ACTIVE_STATE,
                    "All-approved authorizations should yield ACTIVE.");

            ReceiptUpdateInput revoke = new ReceiptUpdateInput();
            revoke.setConsentReceiptId(consentId);
            revoke.setAuthorizations(Collections.singletonList(
                    authorization("user1", ConsentAuthorization.AuthorizationStatus.REVOKED)));
            receiptDAO.updateConsent(revoke, STATUS_CALCULATOR);

            List<ConsentAuthorization> result = receiptDAO.getConsentAuthorizations(consentId);
            Assert.assertEquals(result.size(), 1, "Existing authorization must be updated, not duplicated.");
            Assert.assertEquals(result.get(0).getStatus(), ConsentAuthorization.AuthorizationStatus.REVOKED);
            Assert.assertEquals(receiptDAO.getReceipt(consentId).getState(), ConsentConstants.REVOKE_STATE,
                    "State must be recomputed to REVOKED after revoking the authorization.");
        }
    }

    @Test
    public void testUpdateConsentSkipsStateRecomputeWhenAuthorizationsNull() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            ReceiptInput receipt = receiptInputs.get(0);
            receiptDAO.addReceipt(receipt);
            String consentId = receipt.getConsentReceiptId();

            Timestamp expiry = new Timestamp(System.currentTimeMillis() + 86_400_000L);
            ReceiptUpdateInput updateInput = new ReceiptUpdateInput();
            updateInput.setConsentReceiptId(consentId);
            updateInput.setExpiryTime(expiry);

            // The calculator returns a sentinel that must never be applied because no authorizations are supplied.
            receiptDAO.updateConsent(updateInput, authorizations -> ConsentConstants.REVOKE_STATE);

            Assert.assertEquals(receiptDAO.getReceipt(consentId).getState(), ConsentConstants.ACTIVE_STATE,
                    "State must stay unchanged when the update carries no authorizations.");
            Assert.assertNotNull(receiptDAO.getReceiptExpiryTime(consentId),
                    "Expiry time should still be updated.");
        }
    }

    @Test
    public void testUpdateConsentClearsExpiry() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            ReceiptInput receipt = receiptInputs.get(0);
            receiptDAO.addReceipt(receipt);
            String consentId = receipt.getConsentReceiptId();

            // First set an expiry so there is something to clear.
            ReceiptUpdateInput setExpiry = new ReceiptUpdateInput();
            setExpiry.setConsentReceiptId(consentId);
            setExpiry.setExpiryTime(new Timestamp(System.currentTimeMillis() + 86_400_000L));
            receiptDAO.updateConsent(setExpiry, STATUS_CALCULATOR);
            Assert.assertNotNull(receiptDAO.getReceiptExpiryTime(consentId),
                    "Precondition: the expiry must have been set.");

            // Now clear it.
            ReceiptUpdateInput clear = new ReceiptUpdateInput();
            clear.setConsentReceiptId(consentId);
            clear.setClearExpiry(true);
            receiptDAO.updateConsent(clear, STATUS_CALCULATOR);

            Assert.assertNull(receiptDAO.getReceiptExpiryTime(consentId),
                    "clearExpiry must remove the expiry entirely.");
        }
    }

    @Test
    public void testUpdateConsentClearExpiryTakesPrecedenceOverExpiryTime() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            ReceiptInput receipt = receiptInputs.get(0);
            receiptDAO.addReceipt(receipt);
            String consentId = receipt.getConsentReceiptId();

            // Both an expiry timestamp and the clear flag are supplied — clear must win.
            ReceiptUpdateInput updateInput = new ReceiptUpdateInput();
            updateInput.setConsentReceiptId(consentId);
            updateInput.setExpiryTime(new Timestamp(System.currentTimeMillis() + 86_400_000L));
            updateInput.setClearExpiry(true);
            receiptDAO.updateConsent(updateInput, STATUS_CALCULATOR);

            Assert.assertNull(receiptDAO.getReceiptExpiryTime(consentId),
                    "clearExpiry must take precedence over a supplied expiryTime.");
        }
    }

    @Test
    public void testUpdateConsentLeavesExpiryUnchangedWhenNeitherSet() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (MockedStatic<ConsentManagerComponentDataHolder> dataHolderMockedStatic = mockComponentDataHolder(dataSource);
             Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl();
            ReceiptInput receipt = receiptInputs.get(0);
            receiptDAO.addReceipt(receipt);
            String consentId = receipt.getConsentReceiptId();

            Timestamp expiry = new Timestamp(System.currentTimeMillis() + 86_400_000L);
            ReceiptUpdateInput setExpiry = new ReceiptUpdateInput();
            setExpiry.setConsentReceiptId(consentId);
            setExpiry.setExpiryTime(expiry);
            receiptDAO.updateConsent(setExpiry, STATUS_CALCULATOR);

            // An update that touches neither expiryTime nor clearExpiry must leave the expiry intact.
            ReceiptUpdateInput propertiesOnly = new ReceiptUpdateInput();
            propertiesOnly.setConsentReceiptId(consentId);
            Map<String, String> properties = new HashMap<>();
            properties.put("region", "EU");
            propertiesOnly.setProperties(properties);
            receiptDAO.updateConsent(propertiesOnly, STATUS_CALCULATOR);

            Assert.assertEquals(receiptDAO.getReceiptExpiryTime(consentId), expiry,
                    "Expiry must be left unchanged when neither expiryTime nor clearExpiry is set.");
        }
    }

    private ConsentAuthorization authorization(String userId, ConsentAuthorization.AuthorizationStatus status) {

        ConsentAuthorization authorization = new ConsentAuthorization();
        authorization.setUserId(userId);
        authorization.setStatus(status);
        authorization.setType("EXPLICIT");
        return authorization;
    }

    private ReceiptInput cloneReceiptInput(ReceiptInput source) {

        ReceiptInput clone = new ReceiptInput();
        clone.setConsentReceiptId(source.getConsentReceiptId());
        clone.setCollectionMethod(source.getCollectionMethod());
        clone.setJurisdiction(source.getJurisdiction());
        clone.setPiiPrincipalId(source.getPiiPrincipalId());
        clone.setLanguage(source.getLanguage());
        clone.setPolicyUrl(source.getPolicyUrl());
        clone.setServices(source.getServices());
        clone.setTenantDomain(source.getTenantDomain());
        clone.setTenantId(source.getTenantId());
        clone.setState(source.getState());
        clone.setVersion(source.getVersion());
        clone.setPiiControllerInfo(source.getPiiControllerInfo());
        clone.setProperties(source.getProperties());
        return clone;
    }

    private void mockCarbonContext() {

        mockedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);

        mockedCarbonContext.when(PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(privilegedCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }
}
