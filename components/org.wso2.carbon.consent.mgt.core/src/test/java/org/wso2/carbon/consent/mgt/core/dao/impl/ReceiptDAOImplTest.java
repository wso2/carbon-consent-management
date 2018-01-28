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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.dao.JdbcTemplate;
import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.dao.ReceiptDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.model.Address;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.PiiController;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.closeH2Base;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.getConnection;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.spyConnection;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.spyConnectionWithError;

public class ReceiptDAOImplTest {

    private static List<ReceiptInput> receiptInputs = new ArrayList<>();

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();

        DataSource dataSource = mock(DataSource.class);
        try (Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);
            Purpose purpose1 = new Purpose("P1", "D1", -1234);
            Purpose purpose2 = new Purpose("P2", "D3", -1234);
            purposeDAO.addPurpose(purpose1);
            purposeDAO.addPurpose(purpose2);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl(jdbcTemplate);
            PurposeCategory purposeCategory = new PurposeCategory("PC1", "D1", -1234);
            purposeCategoryDAO.addPurposeCategory(purposeCategory);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl(jdbcTemplate);
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
        String tenantDomain = "carbon.super";
        String consentType = "EXPLICIT";
        String termination = "1 year";
        String version = "KI-CR-v1.1.0";

        List<ReceiptServiceInput> serviceInputs = new ArrayList<>();
        List<ReceiptPurposeInput> purposeInputs = new ArrayList<>();
        List<PiiController> piiControllers = new ArrayList<>();
        List<Integer> purposeCategoryIds = new ArrayList<>();
        List<Integer> piiCategoryIds = new ArrayList<>();
        Map<String, String> properties = new HashMap<>();

        purposeCategoryIds.add(1);
        piiCategoryIds.add(1);
        properties.put("K1", "V1");
        properties.put("K2", "V2");

        ReceiptPurposeInput purposeInput1 = new ReceiptPurposeInput();
        purposeInput1.setPrimaryPurpose(true);
        purposeInput1.setTermination(termination);
        purposeInput1.setConsentType(consentType);
        purposeInput1.setThirdPartyDisclosure(false);
        purposeInput1.setPurposeId(1);
        purposeInput1.setPurposeCategoryId(purposeCategoryIds);
        purposeInput1.setPiiCategoryId(piiCategoryIds);

        ReceiptPurposeInput purposeInput2 = new ReceiptPurposeInput();
        purposeInput2.setPrimaryPurpose(false);
        purposeInput2.setTermination(termination);
        purposeInput2.setConsentType(consentType);
        purposeInput2.setThirdPartyDisclosure(true);
        purposeInput2.setThirdPartyName("bar-company");
        purposeInput2.setPurposeId(2);
        purposeInput2.setPurposeCategoryId(purposeCategoryIds);
        purposeInput2.setPiiCategoryId(piiCategoryIds);

        purposeInputs.add(purposeInput1);
        purposeInputs.add(purposeInput2);

        ReceiptServiceInput serviceInput = new ReceiptServiceInput();
        serviceInput.setPurposes(purposeInputs);
        serviceInput.setTenantDomain(tenantDomain);
        serviceInput.setService(service1);

        serviceInputs.add(serviceInput);

        Address address = new Address("LK", "EN", "South", "1435", "10443", "2nd Street, Colombo 03");
        PiiController piiController = new PiiController("ACME", false, "John Wick", "johnw@acme.com",
                                                        "+17834563445", "http://acme.com", address, null);
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
        receiptInput1.setPiiControllers(piiControllers);
        receiptInput1.setConsentTimestamp(new Date());
        receiptInput1.setSensitive(false);
        receiptInput1.setState(ConsentConstants.ACTIVE_STATE);
        receiptInput1.setVersion(version);
        receiptInput1.setProperties(properties);

        ReceiptInput receiptInput2 = new ReceiptInput();

        receiptInput2.setConsentReceiptId(UUID.randomUUID().toString());
        receiptInput2.setCollectionMethod(collectionMethod);
        receiptInput2.setJurisdiction(jurisdiction);
        receiptInput2.setPiiPrincipalId(principleID2);
        receiptInput2.setLanguage(language);
        receiptInput2.setPolicyUrl(policyUrl);
        receiptInput2.setServices(serviceInputs);
        receiptInput2.setPiiControllers(piiControllers);
        receiptInput2.setConsentTimestamp(new Date());
        receiptInput2.setSensitive(true);
        receiptInput2.setState(ConsentConstants.ACTIVE_STATE);
        receiptInput2.setVersion(version);
        receiptInput2.setProperties(properties);

        receiptInputs.add(receiptInput1);
        receiptInputs.add(receiptInput2);

    }

    @AfterMethod
    public void tearDown() throws Exception {
        closeH2Base();
    }

    @DataProvider(name = "exceptionLevelProvider")
    public Object[][] provideExceptionLevelData() throws Exception {

        return new Object[][]{
                // exception level
                {1},
                {2},
                {3},
                {4},
                {5},
                {6}
        };
    }

    @DataProvider(name = "receiptSearchProvider")
    public Object[][] provideListData() throws Exception {

        return new Object[][]{
                // limit, offset, principalId, tenantDomain, service, state, resultCount
                {10, 0, "subject1", "carbon.super", "foo-company", "ACTIVE", 1},
                {10, 0, "subject1", "carbon.super", "foo-company", null, 1},
                {10, 0, "subject1", "carbon.super", null, null, 1},
                {10, 0, "subject1", null, null, null, 1},
                {10, 0, null, null, null, null, 2},
                {10, 1, null, null, null, null, 1},
                {1, 1, null, null, null, null, 1},
                {0, 0, null, null, null, null, 0},
                {10, 0, "subject*", null, null, null, 2},
                {10, 0, null, "carbon.super", null, null, 2},
                {10, 0, null, null, "foo*", null, 2}
        };
    }

    @Test
    public void testAddReceipt() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl(jdbcTemplate);
            receiptDAO.addReceipt(receiptInputs.get(0));

            Receipt receipt = receiptDAO.getReceipt(receiptInputs.get(0).getConsentReceiptId());

            Assert.assertNotNull(receipt, "Receipt cannot be null for a valid receipt ID.");
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testAddDuplicateReceipt() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl(jdbcTemplate);
            receiptDAO.addReceipt(receiptInputs.get(0));
            receiptDAO.addReceipt(receiptInputs.get(0));
        }
    }

    @Test(dataProvider = "exceptionLevelProvider", expectedExceptions = ConsentManagementServerException.class)
    public void testAddReceiptWithException(int exceptionLevel) throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);

            if (exceptionLevel == 1) {
                when(dataSource.getConnection()).thenThrow(new SQLException("Test exception"));
            } else if (exceptionLevel == 2) {
                when(dataSource.getConnection()).thenReturn(spy).thenThrow(new SQLException("Test exception"));
            } else if (exceptionLevel == 3) {
                when(dataSource.getConnection()).thenReturn(spy).thenReturn(spy)
                                                .thenThrow(new SQLException("Test exception"));
            } else if (exceptionLevel == 4) {
                when(dataSource.getConnection()).thenReturn(spy).thenReturn(spy).thenReturn(spy)
                                                .thenThrow(new SQLException("Test exception"));
            } else if (exceptionLevel == 5) {
                when(dataSource.getConnection()).thenReturn(spy).thenReturn(spy).thenReturn(spy).thenReturn(spy)
                                                .thenThrow(new SQLException("Test exception"));
            } else if (exceptionLevel == 6) {
                when(dataSource.getConnection()).thenReturn(spy).thenReturn(spy).thenReturn(spy).thenReturn(spy)
                                                .thenReturn(spy).thenReturn(spy).thenReturn(spy).thenReturn(spy)
                                                .thenThrow(new SQLException("Test exception"));
            }

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl(jdbcTemplate);
            receiptDAO.addReceipt(receiptInputs.get(0));
        }
    }

    @Test(dataProvider = "receiptSearchProvider")
    public void testSearchReceipts(int limit, int offset, String principalId, String tenantDomain, String service,
                                   String state, int resultCount) throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl(jdbcTemplate);
            receiptDAO.addReceipt(receiptInputs.get(0));
            receiptDAO.addReceipt(receiptInputs.get(1));

            List<ReceiptListResponse> receiptResponses = receiptDAO.searchReceipts(limit, offset, principalId,
                                                                                   tenantDomain, service, state);
            Assert.assertNotNull(receiptResponses);
            Assert.assertEquals(receiptResponses.size(), resultCount);
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testSearchReceiptsWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {
            Connection spy = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spy);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl(jdbcTemplate);
            receiptDAO.searchReceipts(1, 0, "subject1", "carbon.super", "foo*", "ACTIVE");
        }
    }

    @Test
    public void testRevokeReceipt() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl(jdbcTemplate);
            receiptDAO.addReceipt(receiptInputs.get(0));
            receiptDAO.revokeReceipt(receiptInputs.get(0).getConsentReceiptId());
            Receipt receipt = receiptDAO.getReceipt(receiptInputs.get(0).getConsentReceiptId());

            Assert.assertEquals(receipt.getState(), ConsentConstants.REVOKE_STATE);
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testRevokeReceiptWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {
            Connection spy = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spy);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl(jdbcTemplate);
            receiptDAO.revokeReceipt(receiptInputs.get(0).getConsentReceiptId());
        }
    }

    @Test
    public void testGetReceipt() throws Exception {
        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl(jdbcTemplate);
            receiptDAO.addReceipt(receiptInputs.get(1));
            Receipt receipt = receiptDAO.getReceipt(receiptInputs.get(1).getConsentReceiptId());

            Assert.assertNotNull(receipt);
        }
    }

    @Test
    public void testGetInvalidReceipt() throws Exception {
        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl(jdbcTemplate);
            receiptDAO.addReceipt(receiptInputs.get(1));
            Receipt receipt = receiptDAO.getReceipt("invalid-receipt-id");

            Assert.assertNull(receipt);
        }
    }

    @Test(dataProvider = "exceptionLevelProvider", expectedExceptions = ConsentManagementServerException.class)
    public void testGetReceiptWithException(int exceptionLevel) throws Exception {
        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl(jdbcTemplate);
            receiptDAO.addReceipt(receiptInputs.get(1));
        }

        try (Connection connection = getConnection()) {
            Connection spy = spyConnection(connection);

            if (exceptionLevel == 1) {
                when(dataSource.getConnection()).thenThrow(new SQLException("Test exception"));
            } else if (exceptionLevel == 2) {
                when(dataSource.getConnection()).thenReturn(spy).thenThrow(new SQLException("Test exception"));
            } else if (exceptionLevel == 3) {
                when(dataSource.getConnection()).thenReturn(spy).thenReturn(spy)
                                                .thenThrow(new SQLException("Test exception"));
            } else if (exceptionLevel == 4) {
                when(dataSource.getConnection()).thenReturn(spy).thenReturn(spy).thenReturn(spy)
                                                .thenThrow(new SQLException("Test exception"));
            } else {
                when(dataSource.getConnection()).thenReturn(spy).thenReturn(spy).thenReturn(spy).thenReturn(spy)
                                                .thenThrow(new SQLException("Test exception"));
            }

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            ReceiptDAO receiptDAO = new ReceiptDAOImpl(jdbcTemplate);
            receiptDAO.getReceipt(receiptInputs.get(1).getConsentReceiptId());
        }
    }
}
