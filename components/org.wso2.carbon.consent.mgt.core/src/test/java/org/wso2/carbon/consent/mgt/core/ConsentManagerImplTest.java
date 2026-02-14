/*
 *
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.wso2.carbon.consent.mgt.core;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.consent.mgt.core.connector.PIIController;
import org.wso2.carbon.consent.mgt.core.connector.impl.DefaultPIIController;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.dao.ReceiptDAO;
import org.wso2.carbon.consent.mgt.core.dao.impl.PIICategoryDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.PurposeCategoryDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.PurposeDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.ReceiptDAOImpl;
import org.wso2.carbon.consent.mgt.core.internal.ConsentManagerComponentDataHolder;
import org.wso2.carbon.consent.mgt.core.model.Address;
import org.wso2.carbon.consent.mgt.core.model.ConsentManagerConfigurationHolder;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.PIICategoryValidity;
import org.wso2.carbon.consent.mgt.core.model.PiiController;
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
import java.util.UUID;
import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.CarbonConstants.UI_PERMISSION_ACTION;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PERMISSION_CONSENT_MGT_DELETE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PERMISSION_CONSENT_MGT_LIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PERMISSION_CONSENT_MGT_VIEW;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.closeH2Base;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.getConnection;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.mockComponentDataHolder;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.spyConnection;

public class ConsentManagerImplTest {

    private ConsentManager consentManager;
    ConsentManagerConfigurationHolder configurationHolder;
    private Connection connection;
    private MockedStatic<PrivilegedCarbonContext> privilegedCarbonContextMock;
    private MockedStatic<KeyStoreManager> keyStoreManagerMock;
    private MockedStatic<ConsentManagerComponentDataHolder> componentDataHolderMock;

    @Mock
    KeyStoreManager keyStoreManager;

    private static List<PIICategory> piiCategories = new ArrayList<>();

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);
        initiateH2Base();
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        DataSource dataSource = mock(DataSource.class);
        componentDataHolderMock = mockComponentDataHolder(dataSource);

        connection = getConnection();
        Connection spyConnection = spyConnection(connection);
        when(dataSource.getConnection()).thenReturn(spyConnection);
        prepareConfigs();

        PIICategory piiCategory1 = new PIICategory("PII6", "D6", true, -1234);
        piiCategories.add(piiCategory1);

        PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
        piiCategoryDAO.addPIICategory(piiCategories.get(0));
    }

    private void prepareConfigs() throws Exception {

        configurationHolder = new ConsentManagerConfigurationHolder();

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

    }

    @Test
    public void testConsentManagementWithCaseSensitiveUserName() throws Exception {

        String collectionMethod = "Sign-up";
        String jurisdiction = "LK";
        String principleID1 = "subject1";
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
        String piiControllerInput =
                "{\n" + "      \"piiController\": \"samplePiiController\",\n" + "      \"contact\": \"sample\",\n"
                        + "      \"address\": {\n" + "        \"addressCountry\": \"country\",\n"
                        + "        \"addressLocality\": \"locality\",\n" + "        \"addressRegion\": \"region\",\n"
                        + "        \"postOfficeBoxNumber\": \"box\",\n" + "        \"postalCode\": \"code\",\n"
                        + "        \"streetAddress\": \"address\"\n" + "      },\n" + "      \"email\": \"mail\",\n"
                        + "      \"phone\": \"phone\",\n" + "      \"onBehalf\": true,\n"
                        + "      \"piiControllerUrl\": \"sample.com\"\n" + "    }";

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

        purposeInputs.add(purposeInput1);

        ReceiptServiceInput serviceInput = new ReceiptServiceInput();
        serviceInput.setPurposes(purposeInputs);
        serviceInput.setTenantDomain(tenantDomain);
        serviceInput.setTenantId(tenantId);
        serviceInput.setService(service1);
        serviceInput.setSpDisplayName(serviceDisplayName);
        serviceInput.setSpDescription(serviceDescription);

        serviceInputs.add(serviceInput);

        Address address = new Address("LK", "EN", "South", "1435", "10443", "2nd Street, Colombo 03");
        PiiController piiController = new PiiController("ACME", false, "John Wick", "johnw@acme.com", "+17834563445",
                "http://acme.com", address);
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

        consentManager = new ConsentManagerImpl(configurationHolder);
        // Test the default case-insensitive behavior
        // Adding receipts for "subject1" and "SUBJECT1" should be treated as the same user
        consentManager.addConsent(receiptInput1);
        String receiptID = receiptInput1.getConsentReceiptId();
        receiptInput1.setPiiPrincipalId("SUBJECT1");
        consentManager.addConsent(receiptInput1);
        
        // With case-insensitive behavior (default), adding "SUBJECT1" after "subject1" revokes the first receipt
        String firstReceiptState = consentManager.getReceipt(receiptID).getState();
        String secondReceiptState = consentManager.getReceipt(receiptInput1.getConsentReceiptId()).getState();
        
        Assert.assertEquals(firstReceiptState, "REVOKED", "First receipt should be REVOKED when case-insensitive");
        Assert.assertEquals(secondReceiptState, "ACTIVE", "Second receipt should be ACTIVE");
        
        // Search results should also treat "subject1" and "SUBJECT1" as the same
        List<ReceiptListResponse> receiptListResponses = consentManager
                .searchReceipts(10, 0, "subject1", "carbon" + ".super", receiptInput1.getServices().get(0).getService(),
                        receiptInput1.getState());
        List<ReceiptListResponse> receiptListResponsesCaps = consentManager
                .searchReceipts(10, 0, "SUBJECT1", "carbon" + ".super", receiptInput1.getServices().get(0).getService(),
                        receiptInput1.getState());
        
        // Both searches should return the same receipt
        Assert.assertEquals(receiptListResponses.get(0).getConsentReceiptId(),
                receiptListResponsesCaps.get(0).getConsentReceiptId(),
                "Search results should be the same for case-insensitive matching");
    }

    private void mockCarbonContext() {

        privilegedCarbonContextMock = mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);

        privilegedCarbonContextMock.when(PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(privilegedCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    private void mockKeyStoreManager() throws Exception {

        keyStoreManagerMock = mockStatic(KeyStoreManager.class);
        keyStoreManagerMock.when(() -> KeyStoreManager.getInstance(SUPER_TENANT_ID)).thenReturn(keyStoreManager);

        when(keyStoreManager.getDefaultPublicKey()).thenReturn(TestUtils.getPublicKey(TestUtils
                .loadKeyStoreFromFileSystem(TestUtils.getFilePathInConfDirectory("wso2carbon.jks"), "wso2carbon",
                        "JKS"), "wso2carbon"));
        when(keyStoreManager.getKeyStore(anyString())).thenReturn(TestUtils
                .loadKeyStoreFromFileSystem(TestUtils.getFilePathInConfDirectory("wso2carbon.jks"), "wso2carbon",
                        "JKS"));
    }

    @AfterMethod
    public void tearDown() throws Exception {

        connection.close();
        closeH2Base();
        if (componentDataHolderMock != null) {
            componentDataHolderMock.close();
        }
        if (privilegedCarbonContextMock != null) {
            privilegedCarbonContextMock.close();
        }
        if (keyStoreManagerMock != null) {
            keyStoreManagerMock.close();
        }
    }
}
