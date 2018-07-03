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

package org.wso2.carbon.consent.mgt.core;

import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.consent.mgt.core.connector.PIIController;
import org.wso2.carbon.consent.mgt.core.connector.impl.DefaultPIIController;
import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.dao.ReceiptDAO;
import org.wso2.carbon.consent.mgt.core.dao.impl.PIICategoryDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.PurposeCategoryDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.PurposeDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.ReceiptDAOImpl;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.internal.ConsentManagerComponentDataHolder;
import org.wso2.carbon.consent.mgt.core.model.AddReceiptResponse;
import org.wso2.carbon.consent.mgt.core.model.ConsentManagerConfigurationHolder;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.PIICategoryValidity;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;
import org.wso2.carbon.consent.mgt.core.util.ConsentConfigParser;
import org.wso2.carbon.consent.mgt.core.util.TestUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.wso2.carbon.CarbonConstants.UI_PERMISSION_ACTION;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PERMISSION_CONSENT_MGT_DELETE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PERMISSION_CONSENT_MGT_LIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PERMISSION_CONSENT_MGT_VIEW;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.REVOKE_STATE;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.closeH2Base;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.getConnection;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.mockComponentDataHolder;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.spyConnection;

@PrepareForTest({PrivilegedCarbonContext.class, ConsentManagerComponentDataHolder.class, KeyStoreManager.class})
public class InterceptingConsentManagerTest extends PowerMockTestCase {

    private Connection connection;
    private ConsentManager consentManager;

    @Mock
    KeyStoreManager keyStoreManager;

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        DataSource dataSource = mock(DataSource.class);
        mockComponentDataHolder(dataSource);

        connection = getConnection();
        Connection spyConnection = spyConnection(connection);
        when(dataSource.getConnection()).thenReturn(spyConnection);
        prepareConfigs();
    }

    private void prepareConfigs() throws Exception {

        ConsentManagerConfigurationHolder configurationHolder = new ConsentManagerConfigurationHolder();

        PurposeDAO purposeDAO = new PurposeDAOImpl();
        configurationHolder.setPurposeDAOs(Collections.singletonList(purposeDAO));

        PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
        configurationHolder.setPiiCategoryDAOs(Collections.singletonList(piiCategoryDAO));

        PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
        configurationHolder.setPurposeCategoryDAOs(Collections.singletonList(purposeCategoryDAO));

        ReceiptDAO receiptDAO = new ReceiptDAOImpl();
        configurationHolder.setReceiptDAOs(Collections.singletonList(receiptDAO));

        RealmService realmService = mock(RealmService.class);
        TenantManager tenantManager = mock(TenantManager.class);
        UserRealm userRealm = mock(UserRealm.class);
        AuthorizationManager authorizationManager = mock(AuthorizationManager.class);
        when(tenantManager.getTenantId(SUPER_TENANT_DOMAIN_NAME)).thenReturn(SUPER_TENANT_ID);
        when(tenantManager.getDomain(SUPER_TENANT_ID)).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        when(userRealm.getAuthorizationManager()).thenReturn(authorizationManager);
        when(authorizationManager.isUserAuthorized("admin", PERMISSION_CONSENT_MGT_VIEW, UI_PERMISSION_ACTION))
                .thenReturn(true);
        when(authorizationManager.isUserAuthorized("admin", PERMISSION_CONSENT_MGT_LIST, UI_PERMISSION_ACTION))
                .thenReturn(true);
        when(authorizationManager.isUserAuthorized("admin", PERMISSION_CONSENT_MGT_DELETE, UI_PERMISSION_ACTION))
                .thenReturn(true);

        configurationHolder.setRealmService(realmService);

        ConsentConfigParser configParser = new ConsentConfigParser();
        PIIController piiController = new DefaultPIIController(configParser);

        configurationHolder.setPiiControllers(Collections.singletonList(piiController));
        configurationHolder.setConfigParser(configParser);

        mockCarbonContext();
        mockKeyStoreManager();

        consentManager = new InterceptingConsentManager(configurationHolder, Collections.emptyList());
    }

    private void mockKeyStoreManager() throws Exception {

        mockStatic(KeyStoreManager.class);
        PowerMockito.when(KeyStoreManager.getInstance(SUPER_TENANT_ID)).thenReturn(keyStoreManager);

        PowerMockito.when(keyStoreManager.getDefaultPublicKey())
                .thenReturn(TestUtils.getPublicKey(TestUtils.loadKeyStoreFromFileSystem(TestUtils
                        .getFilePathInConfDirectory("wso2carbon.jks"), "wso2carbon", "JKS"), "wso2carbon"));
        PowerMockito.when(keyStoreManager.getKeyStore(anyString())).thenReturn(TestUtils.loadKeyStoreFromFileSystem
                (TestUtils.getFilePathInConfDirectory("wso2carbon.jks"), "wso2carbon", "JKS"));
    }

    private void mockCarbonContext() {

        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);

        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(privilegedCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    @AfterMethod
    public void tearDown() throws Exception {

        connection.close();
        closeH2Base();
    }

    @DataProvider(name = "listDataProvider")
    public static Object[][] listData() {
        return new Object[][]{
                // limit, offset, resultCount
                {0, 0, 2},
                {0, 1, 1},
                {0, 2, 0},
                {1, 0, 1}
        };
    }

    @DataProvider(name = "listWithDefaultDataProvider")
    public static Object[][] listWithDefaultData() {
        return new Object[][]{
                // limit, offset, resultCount
                {0, 0, 3},
                {0, 1, 2},
                {0, 2, 1},
                {1, 0, 1}
        };
    }

    @DataProvider(name = "deleteDataProvider")
    public static Object[][] deleteData() {
        return new Object[][]{
                // deleteId
                {-1},
                {100}
        };
    }

    @DataProvider(name = "receiptListDataProvider")
    public static Object[][] receiptListData() {
        return new Object[][]{
                // limit, offset, principalId, tenantDomain, service, state, resultCount
                {10, 0, "subject1", "carbon.super", "foo-company", "ACTIVE", 1},
                {10, 0, "subject1", "carbon.super", "foo-company", null, 1},
                {10, 0, "subject1", "carbon.super", null, null, 1},
                {10, 0, "subject1", null, null, null, 1},
                {10, 0, null, null, null, null, 2},
                {10, 1, null, null, null, null, 1},
                {1, 1, null, null, null, null, 1},
                {0, 0, null, null, null, null, 2},
                {0, 2, null, null, null, null, 0},
                {10, 0, "subject*", null, null, null, 2},
                {10, 0, null, "carbon.super", null, null, 2},
                {10, 0, null, null, "foo*", null, 2}
        };
    }

    @Test
    public void testAddPurposeCategory() throws Exception {

        PurposeCategory purposeCategory = new PurposeCategory("PC1", "D1");
        PurposeCategory result = consentManager.addPurposeCategory(purposeCategory);

        Assert.assertNotNull(result.getId(), "PurposeCategory cannot be null.");
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testAddDuplicatePurposeCategory() throws Exception {

        addPurposeCategory("PC1");
        addPurposeCategory("PC1");

        Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
    }

    @Test
    public void testGetPurposeCategory() throws Exception {

        PurposeCategory purposeCategory = new PurposeCategory("PC1", "D1");
        PurposeCategory addPurposeCategory = consentManager.addPurposeCategory(purposeCategory);
        Assert.assertNotNull(addPurposeCategory, "PurposeCategory cannot be null.");

        PurposeCategory getPurposeCategory = consentManager.getPurposeCategory(addPurposeCategory.getId());

        Assert.assertNotNull(getPurposeCategory, "PurposeCategory cannot be null.");
        Assert.assertNotNull(getPurposeCategory.getId(), "PurposeCategory ID cannot be null.");
        Assert.assertEquals(getPurposeCategory.getId(), addPurposeCategory.getId());
        Assert.assertEquals(getPurposeCategory.getDescription(), addPurposeCategory.getDescription());
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testGetPurposeCategoryByInvalidId() throws Exception {

        consentManager.getPurposeCategory(123);
        Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
    }

    @Test
    public void testGetPurposeCategoryByName() throws Exception {

        String name = "PC1";
        PurposeCategory purposeCategory = addPurposeCategory(name);
        PurposeCategory result = consentManager.getPurposeCategoryByName(name);

        Assert.assertNotNull(result, "PurposeCategory cannot be null.");
        Assert.assertEquals(result.getId(), purposeCategory.getId());
        Assert.assertEquals(result.getName(), name);
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testGetPurposeCategoryByInvalidName() throws Exception {

        consentManager.getPurposeCategoryByName("Invalid");
        Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
    }

    @Test(dataProvider = "listWithDefaultDataProvider")
    public void testListPurposeCategories(int limit, int offset, int resultSize) throws Exception {

        addPurposeCategory("PC1");
        addPurposeCategory("PC2");

        List<PurposeCategory> purposeCategories = consentManager.listPurposeCategories(limit, offset);
        Assert.assertEquals(purposeCategories.size(), resultSize);
    }

    @Test
    public void testDeletePurposeCategory() throws Exception {

        PurposeCategory purposeCategory = addPurposeCategory("PC1");
        consentManager.deletePurposeCategory(purposeCategory.getId());
    }

    @Test(expectedExceptions = ConsentManagementClientException.class, dataProvider = "deleteDataProvider")
    public void testDeleteInvalidPurposeCategory(int id) throws Exception {

        consentManager.deletePurposeCategory(id);
        Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
    }

    @Test
    public void testIsPurposeCategoryExists() throws Exception {

        PurposeCategory purposeCategory = addPurposeCategory("PC1");
        Assert.assertTrue(consentManager.isPurposeCategoryExists(purposeCategory.getName()), "PurposeCategory PC1 " +
                "should exist.");
    }

    @Test
    public void testIsInvalidPurposeCategoryExists() throws Exception {

        Assert.assertTrue(!consentManager.isPurposeCategoryExists("Invalid"), "PurposeCategory Invalid " +
                "should not exist.");
    }

    @Test
    public void testAddPIICategory() throws Exception {

        PIICategory piiCategory = new PIICategory("PII1", "D1", null, "PII-DISPLAY");
        PIICategory result = consentManager.addPIICategory(piiCategory);

        Assert.assertNotNull(result.getId());
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testAddDuplicatePIICategory() throws Exception {

        addPIICategory("PII");
        addPIICategory("PII");

        Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
    }

    @Test
    public void testGetPIICategory() throws Exception {

        PIICategory piiCategory = new PIICategory("PII1", "D1", true, "PII-DISPlAY");
        PIICategory addPIICategory = consentManager.addPIICategory(piiCategory);
        Assert.assertNotNull(addPIICategory, "PurposeCategory cannot be null.");

        PIICategory getPIICategory = consentManager.getPIICategory(addPIICategory.getId());

        Assert.assertNotNull(getPIICategory, "PIICategory cannot be null.");
        Assert.assertNotNull(getPIICategory.getId(), "PIICategory ID cannot be null.");
        Assert.assertEquals(getPIICategory.getId(), addPIICategory.getId());
        Assert.assertEquals(getPIICategory.getDescription(), addPIICategory.getDescription());
        Assert.assertEquals(getPIICategory.getSensitive(), addPIICategory.getSensitive());
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testGetInvalidPIICategory() throws Exception {

        consentManager.getPIICategory(1);
        Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
    }

    @Test
    public void testGetPIICategoryByName() throws Exception {

        String name = "PII1";
        PIICategory piiCategory = addPIICategory(name);
        PIICategory result = consentManager.getPIICategoryByName(name);

        Assert.assertNotNull(result, "PIICategory cannot be null.");
        Assert.assertEquals(result.getId(), piiCategory.getId());
        Assert.assertEquals(result.getName(), name);
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testGetPIICategoryByInvalidName() throws Exception {

        consentManager.getPIICategoryByName("Invalid");
        Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
    }

    @Test(dataProvider = "listDataProvider")
    public void testListPIICategories(int limit, int offset, int resultCount) throws Exception {

        addPIICategory("PII1");
        addPIICategory("PII2");

        List<PIICategory> piiCategories = consentManager.listPIICategories(limit, offset);
        Assert.assertEquals(piiCategories.size(), resultCount);
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testListInvalidPIICategories() throws Exception {

        consentManager.listPIICategories(-1, -1);
        Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
    }

    @Test
    public void testDeletePIICategory() throws Exception {

        PIICategory piiCategory = addPIICategory("PII1");
        consentManager.deletePIICategory(piiCategory.getId());
    }

    @Test(expectedExceptions = ConsentManagementClientException.class, dataProvider = "deleteDataProvider")
    public void testDeleteInvalidPIICategory(int id) throws Exception {

        consentManager.deletePIICategory(id);
        Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
    }

    @Test
    public void testIsPIICategoryExists() throws Exception {

        PIICategory piiCategory = addPIICategory("PII1");
        Assert.assertTrue(consentManager.isPIICategoryExists(piiCategory.getName()), "PII1 should exist.");
    }

    @Test
    public void testIsInvalidPIICategoryExists() throws Exception {

        Assert.assertTrue(!consentManager.isPIICategoryExists("Invalid"), "Invalid PII category shouldn't exist");
    }

    @Test
    public void testAddPurpose() throws Exception {

        Purpose purpose = new Purpose("P1", "D1");
        Purpose result = consentManager.addPurpose(purpose);

        Assert.assertNotNull(result, "Purpose cannot be null.");
        Assert.assertNotNull(result.getId(), "Purpose ID cannot be null.");
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testAddDuplicatePurpose() throws Exception {

        addPurpose("P1");
        addPurpose("P1");
        Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
    }

    @Test
    public void testGetPurpose() throws Exception {

        Purpose purpose = new Purpose("P1", "D1");
        Purpose addPurpose = consentManager.addPurpose(purpose);
        Assert.assertNotNull(addPurpose, "Purpose cannot be null.");

        Purpose getPurpose = consentManager.getPurpose(addPurpose.getId());

        Assert.assertNotNull(getPurpose, "Purpose cannot be null.");
        Assert.assertNotNull(getPurpose.getId(), "Purpose ID cannot be null.");
        Assert.assertEquals(getPurpose.getId(), addPurpose.getId());
        Assert.assertEquals(getPurpose.getDescription(), addPurpose.getDescription());
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testGetInvalidPurpose() throws Exception {

        consentManager.getPurpose(123);
        Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testGetPurposeByInvalidName() throws Exception {

        consentManager.getPurposeByName("Invalid");
        Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
    }

    @Test
    public void testGetPurposeByName() throws Exception {

        String name = "P1";
        Purpose purpose = addPurpose(name);
        Purpose purposeByName = consentManager.getPurposeByName(purpose.getName());

        Assert.assertNotNull(purposeByName, "Purpose cannot be null.");
        Assert.assertEquals(purposeByName.getId(), purpose.getId());
        Assert.assertEquals(purposeByName.getName(), name);
    }

    @Test(dataProvider = "listWithDefaultDataProvider")
    public void testListPurposes(int limit, int offset, int resultCount) throws Exception {

        addPurpose("P1");
        addPurpose("P2");

        List<Purpose> purposes = consentManager.listPurposes(limit, offset);
        Assert.assertEquals(purposes.size(), resultCount);
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testListInvalidPurposes() throws Exception {

        consentManager.listPurposes(-1, -1);
        Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
    }

    @Test
    public void testDeletePurpose() throws Exception {

        Purpose purpose = addPurpose("P1");
        consentManager.deletePurpose(purpose.getId());
    }

    @Test(expectedExceptions = ConsentManagementClientException.class, dataProvider = "deleteDataProvider")
    public void testDeleteInvalidPurpose(int id) throws Exception {

        consentManager.deletePurpose(id);
    }

    @Test
    public void testIsPurposeExists() throws Exception {

        Purpose purpose = addPurpose("P1");
        Assert.assertTrue(consentManager.isPurposeExists(purpose.getName()), "Purpose 'P1' should exist.");
    }

    @Test
    public void testIsInvalidPurposeExists() throws Exception {

        Assert.assertTrue(!consentManager.isPurposeExists("Invalid"), "Purpose should not exist.");
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testIsNullPurposeExists() throws Exception {

        consentManager.isPurposeExists(null);
    }

    private Purpose addPurpose(String name) throws ConsentManagementException {

        Purpose purpose = new Purpose(name, "D1");
        Purpose purposeResult = consentManager.addPurpose(purpose);
        Assert.assertNotNull(purposeResult, "Purpose cannot be null.");

        return purposeResult;
    }

    private Purpose addPurpose(String name, List<PurposePIICategory> purposePIICategory) throws
            ConsentManagementException {

        Purpose purpose = new Purpose(name, "D1", purposePIICategory);
        Purpose purposeResult = consentManager.addPurpose(purpose);
        Assert.assertNotNull(purposeResult, "Purpose cannot be null.");

        return purposeResult;
    }

    private PIICategory addPIICategory(String name) throws ConsentManagementException {

        PIICategory piiCategory = new PIICategory(name, "D1", true, "PII-DISPLAY");
        PIICategory piiCategoryResult = consentManager.addPIICategory(piiCategory);
        Assert.assertNotNull(piiCategoryResult, "PIICategory cannot be null.");

        return piiCategoryResult;
    }

    private PurposeCategory addPurposeCategory(String name) throws ConsentManagementException {

        PurposeCategory purposeCategory = new PurposeCategory(name, "D1");
        PurposeCategory purposeCategoryResult = consentManager.addPurposeCategory(purposeCategory);
        Assert.assertNotNull(purposeCategoryResult, "PurposeCategory cannot be null.");

        return purposeCategoryResult;
    }

    @Test
    public void testAddConsent() throws Exception {

        PIICategory piiCategory = addPIICategory("PII1");
        PurposeCategory purposeCategory = addPurposeCategory("PC1");
        PurposePIICategory purposePIICategory = new PurposePIICategory(piiCategory.getId(), true);

        Purpose purpose1 = addPurpose("P1", Collections.singletonList(purposePIICategory));
        Purpose purpose2 = addPurpose("P2", Collections.singletonList(purposePIICategory));

        String collectionMethod = "Sign-up";
        String jurisdiction = "LK";
        String principleID = "subject1";
        String language = "EN";
        String policyUrl = "http://foo.com/policy";
        String service1 = "foo-company";
        String tenantDomain = "carbon.super";
        int tenantId = -1234;
        String consentType = "EXPLICIT";
        String termination = "1 year";

        List<ReceiptServiceInput> serviceInputs = new ArrayList<>();
        List<ReceiptPurposeInput> purposeInputs = new ArrayList<>();
        List<Integer> purposeCategoryIds = new ArrayList<>();
        List<PIICategoryValidity> piiCategoryIds = new ArrayList<>();
        Map<String, String> properties = new HashMap<>();

        purposeCategoryIds.add(purposeCategory.getId());
        piiCategoryIds.add(new PIICategoryValidity(1, "45"));
        properties.put("K1", "V1");
        properties.put("K2", "V2");

        ReceiptPurposeInput purposeInput1 = new ReceiptPurposeInput();
        purposeInput1.setPrimaryPurpose(true);
        purposeInput1.setTermination(termination);
        purposeInput1.setConsentType(consentType);
        purposeInput1.setThirdPartyDisclosure(false);
        purposeInput1.setPurposeId(purpose1.getId());
        purposeInput1.setPurposeCategoryId(purposeCategoryIds);
        purposeInput1.setPiiCategory(piiCategoryIds);

        ReceiptPurposeInput purposeInput2 = new ReceiptPurposeInput();
        purposeInput2.setPrimaryPurpose(false);
        purposeInput2.setTermination(termination);
        purposeInput2.setConsentType(consentType);
        purposeInput2.setThirdPartyDisclosure(true);
        purposeInput2.setThirdPartyName("bar-company");
        purposeInput2.setPurposeId(purpose2.getId());
        purposeInput2.setPurposeCategoryId(purposeCategoryIds);
        purposeInput2.setPiiCategory(piiCategoryIds);

        purposeInputs.add(purposeInput1);
        purposeInputs.add(purposeInput2);

        ReceiptServiceInput serviceInput = new ReceiptServiceInput();
        serviceInput.setPurposes(purposeInputs);
        serviceInput.setTenantDomain(tenantDomain);
        serviceInput.setTenantId(tenantId);
        serviceInput.setService(service1);

        serviceInputs.add(serviceInput);

        ReceiptInput receiptInput1 = new ReceiptInput();

        //receiptInput1.setConsentReceiptId(UUID.randomUUID().toString());
        receiptInput1.setCollectionMethod(collectionMethod);
        receiptInput1.setJurisdiction(jurisdiction);
        receiptInput1.setPiiPrincipalId(principleID);
        receiptInput1.setLanguage(language);
        receiptInput1.setPolicyUrl(policyUrl);
        receiptInput1.setServices(serviceInputs);
        receiptInput1.setTenantDomain(tenantDomain);
        receiptInput1.setTenantId(tenantId);
        receiptInput1.setProperties(properties);

        AddReceiptResponse receiptResponse = consentManager.addConsent(receiptInput1);

        Assert.assertNotNull(receiptResponse, "AddReceiptResponse cannot be null.");
        Assert.assertNotNull(receiptResponse.getConsentReceiptId(), "ConsentReceiptId cannot be null.");
        Assert.assertEquals(receiptResponse.getTenantDomain(), tenantDomain);
        Assert.assertEquals(receiptResponse.getCollectionMethod(), collectionMethod);
        Assert.assertEquals(receiptResponse.getLanguage(), language);
        Assert.assertEquals(receiptResponse.getPiiPrincipalId(), principleID);
    }

    @Test
    public void testAddDuplicateConsent() throws Exception {

        List<AddReceiptResponse> receiptResponses = addReceipt("Subject1", "Subject1");

        Receipt receipt = consentManager.getReceipt(receiptResponses.get(0).getConsentReceiptId());
        Assert.assertNotNull(receipt, "Receipt should not be null.");
        Assert.assertEquals(receipt.getState(), REVOKE_STATE, "First receipt should be revoked for " +
                "duplicate receipts.");
    }

    @Test
    public void testGetReceipt() throws Exception {

        List<AddReceiptResponse> receiptResponses = addReceipt("Subject1");
        AddReceiptResponse receiptResponse = receiptResponses.get(0);
        Receipt receipt = consentManager.getReceipt(receiptResponse.getConsentReceiptId());

        Assert.assertNotNull(receipt, "Receipt should not be null.");
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testGetInvalidReceipt() throws Exception {

        consentManager.getReceipt("InvalidID");
        Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
    }

    @Test(dataProvider = "receiptListDataProvider")
    public void testSearchReceipts(int limit, int offset, String principalId, String tenantDomain, String service,
                                   String state, int resultCount) throws Exception {

        addReceipt("subject1", "subject2");
        List<ReceiptListResponse> receiptListResponses = consentManager.searchReceipts(limit, offset, principalId,
                tenantDomain, service, state);
        Assert.assertNotNull(receiptListResponses, "ReceiptListResponse list cannot be null");
        Assert.assertEquals(receiptListResponses.size(), resultCount);
    }

    @Test
    public void testRevokeReceipt() throws Exception {

        List<AddReceiptResponse> receiptResponses = addReceipt("Subject1");
        String consentReceiptId = receiptResponses.get(0).getConsentReceiptId();
        consentManager.revokeReceipt(consentReceiptId);

        Receipt receipt = consentManager.getReceipt(consentReceiptId);
        Assert.assertNotNull(receipt, "Receipt cannot be null.");
        Assert.assertEquals(receipt.getState(), REVOKE_STATE);
    }

    private List<AddReceiptResponse> addReceipt(String... principleIDs) throws ConsentManagementException {

        PIICategory piiCategory = addPIICategory("PII1");
        PurposeCategory purposeCategory = addPurposeCategory("PC1");
        PurposePIICategory purposePIICategory = new PurposePIICategory(piiCategory.getId(), true);

        Purpose purpose1 = addPurpose("P1", Collections.singletonList(purposePIICategory));
        Purpose purpose2 = addPurpose("P2", Collections.singletonList(purposePIICategory));

        String collectionMethod = "Sign-up";
        String jurisdiction = "LK";
        String language = "EN";
        String policyUrl = "http://foo.com/policy";
        String service1 = "foo-company";
        String tenantDomain = "carbon.super";
        int tenantId = -1234;
        String consentType = "EXPLICIT";
        String termination = "1 year";

        List<ReceiptServiceInput> serviceInputs = new ArrayList<>();
        List<ReceiptPurposeInput> purposeInputs = new ArrayList<>();
        List<Integer> purposeCategoryIds = new ArrayList<>();
        List<PIICategoryValidity> piiCategoryIds = new ArrayList<>();
        Map<String, String> properties = new HashMap<>();

        purposeCategoryIds.add(purposeCategory.getId());
        piiCategoryIds.add(new PIICategoryValidity(1, "45"));
        properties.put("K1", "V1");
        properties.put("K2", "V2");

        ReceiptPurposeInput purposeInput1 = new ReceiptPurposeInput();
        purposeInput1.setPrimaryPurpose(true);
        purposeInput1.setTermination(termination);
        purposeInput1.setConsentType(consentType);
        purposeInput1.setThirdPartyDisclosure(false);
        purposeInput1.setPurposeId(purpose1.getId());
        purposeInput1.setPurposeCategoryId(purposeCategoryIds);
        purposeInput1.setPiiCategory(piiCategoryIds);

        ReceiptPurposeInput purposeInput2 = new ReceiptPurposeInput();
        purposeInput2.setPrimaryPurpose(false);
        purposeInput2.setTermination(termination);
        purposeInput2.setConsentType(consentType);
        purposeInput2.setThirdPartyDisclosure(true);
        purposeInput2.setThirdPartyName("bar-company");
        purposeInput2.setPurposeId(purpose2.getId());
        purposeInput2.setPurposeCategoryId(purposeCategoryIds);
        purposeInput2.setPiiCategory(piiCategoryIds);

        purposeInputs.add(purposeInput1);
        purposeInputs.add(purposeInput2);

        ReceiptServiceInput serviceInput = new ReceiptServiceInput();
        serviceInput.setPurposes(purposeInputs);
        serviceInput.setTenantDomain(tenantDomain);
        serviceInput.setTenantId(tenantId);
        serviceInput.setService(service1);

        serviceInputs.add(serviceInput);

        ReceiptInput receiptInput1 = new ReceiptInput();

        //receiptInput1.setConsentReceiptId(UUID.randomUUID().toString());
        receiptInput1.setCollectionMethod(collectionMethod);
        receiptInput1.setJurisdiction(jurisdiction);
        receiptInput1.setLanguage(language);
        receiptInput1.setPolicyUrl(policyUrl);
        receiptInput1.setServices(serviceInputs);
        receiptInput1.setTenantDomain(tenantDomain);
        receiptInput1.setTenantId(tenantId);
        receiptInput1.setProperties(properties);

        List<AddReceiptResponse> receiptResponses = new ArrayList<>();

        for (String principleID : principleIDs) {
            receiptInput1.setPiiPrincipalId(principleID);
            AddReceiptResponse receiptResponse = consentManager.addConsent(receiptInput1);
            Assert.assertNotNull(receiptResponse, "AddReceiptResponse cannot be null.");
            receiptResponses.add(receiptResponse);
        }

        return receiptResponses;
    }

}
