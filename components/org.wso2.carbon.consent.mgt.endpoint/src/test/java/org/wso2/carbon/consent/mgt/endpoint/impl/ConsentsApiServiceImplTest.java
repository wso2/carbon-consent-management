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

package org.wso2.carbon.consent.mgt.endpoint.impl;

import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.InterceptingConsentManager;
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
import org.wso2.carbon.consent.mgt.core.internal.ConsentManagerComponentDataHolder;
import org.wso2.carbon.consent.mgt.core.model.AddReceiptResponse;
import org.wso2.carbon.consent.mgt.core.model.ConsentManagerConfigurationHolder;
import org.wso2.carbon.consent.mgt.core.util.ConsentConfigParser;
import org.wso2.carbon.consent.mgt.endpoint.dto.ConsentReceiptDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ConsentRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ConsentResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PIIcategoryRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PiiCategoryListDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PiiCategoryListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeCategoryListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeCategoryRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeGetResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ServiceDTO;
import org.wso2.carbon.consent.mgt.endpoint.exception.ConflictRequestException;
import org.wso2.carbon.consent.mgt.endpoint.exception.NotFoundException;
import org.wso2.carbon.consent.mgt.endpoint.impl.util.TestUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import javax.ws.rs.core.Response;

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
import static org.wso2.carbon.consent.mgt.endpoint.impl.util.TestUtils.closeH2Base;
import static org.wso2.carbon.consent.mgt.endpoint.impl.util.TestUtils.getConnection;
import static org.wso2.carbon.consent.mgt.endpoint.impl.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.consent.mgt.endpoint.impl.util.TestUtils.mockComponentDataHolder;
import static org.wso2.carbon.consent.mgt.endpoint.impl.util.TestUtils.spyConnection;

@PrepareForTest({PrivilegedCarbonContext.class, ConsentManagerComponentDataHolder.class, KeyStoreManager.class})
public class ConsentsApiServiceImplTest extends PowerMockTestCase {

    private Connection connection;

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

        ConsentManager consentManager = new InterceptingConsentManager(configurationHolder, Collections.emptyList());
        mockCarbonContext(consentManager);
        mockKeyStoreManager();
    }

    private void mockCarbonContext(ConsentManager consentManager) {

        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);

        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getOSGiService(ConsentManager.class, null)).thenReturn
                (consentManager);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(privilegedCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
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

    @AfterMethod
    public void tearDown() throws Exception {

        connection.close();
        closeH2Base();
    }

    @Test
    public void testConsentsPurposesPost() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PurposeRequestDTO purposeRequestDTO = new PurposeRequestDTO();
        purposeRequestDTO.setPurpose("P1");
        purposeRequestDTO.setDescription("D1");
        purposeRequestDTO.setGroup("SIGNUP");
        purposeRequestDTO.setGroupType("SYSTEM");
        purposeRequestDTO.setMandatory(true);
        Response response = service.consentsPurposesPost(purposeRequestDTO);

        PurposeGetResponseDTO responseDTO = (PurposeGetResponseDTO) response.getEntity();

        Assert.assertNotNull(responseDTO);
        Assert.assertNotNull(responseDTO.getPurposeId());
    }

    @Test
    public void testConsentsPurposesPostDuplicate() throws Exception {

        try {
            ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

            PurposeRequestDTO purposeRequestDTO = new PurposeRequestDTO();
            purposeRequestDTO.setPurpose("P1");
            purposeRequestDTO.setDescription("D1");
            purposeRequestDTO.setGroup("SIGNUP");
            purposeRequestDTO.setGroupType("SYSTEM");
            purposeRequestDTO.setMandatory(true);

            service.consentsPurposesPost(purposeRequestDTO);
            service.consentsPurposesPost(purposeRequestDTO);
            Assert.fail("Expected " + ConflictRequestException.class.getName());
        } catch (Throwable e) {
            Assert.assertTrue(e instanceof ConflictRequestException);
        }
    }

    @Test
    public void testConsentsPurposesGet() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PurposeRequestDTO purposeRequestDTO = new PurposeRequestDTO();
        purposeRequestDTO.setPurpose("P1");
        purposeRequestDTO.setDescription("D1");
        purposeRequestDTO.setGroup("SIGNUP");
        purposeRequestDTO.setGroupType("SYSTEM");
        purposeRequestDTO.setMandatory(true);
        service.consentsPurposesPost(purposeRequestDTO);

        purposeRequestDTO.setPurpose("P2");
        purposeRequestDTO.setDescription("D2");
        purposeRequestDTO.setGroup("SIGNUP");
        purposeRequestDTO.setGroupType("SYSTEM");
        purposeRequestDTO.setMandatory(false);
        service.consentsPurposesPost(purposeRequestDTO);

        Response getResponse = service.consentsPurposesGet("*", "*", 2, 0);
        Assert.assertNotNull(getResponse);
    }

    @Test
    public void testConsentsPurposesPurposeIdDelete() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PurposeRequestDTO purposeRequestDTO = new PurposeRequestDTO();
        purposeRequestDTO.setPurpose("P1");
        purposeRequestDTO.setDescription("D1");
        purposeRequestDTO.setGroup("SIGNUP");
        purposeRequestDTO.setGroupType("SYSTEM");
        purposeRequestDTO.setMandatory(true);
        Response response = service.consentsPurposesPost(purposeRequestDTO);
        PurposeGetResponseDTO responseDTO = (PurposeGetResponseDTO) response.getEntity();
        Response response1 = service.consentsPurposesPurposeIdDelete(Integer.toString(responseDTO.getPurposeId()));

        Assert.assertNotNull(response1);
        Assert.assertEquals(response1.getStatus(), 200);
    }

    @Test
    public void testConsentsPurposesInvalidPurposeIdDelete() throws Exception {

        try {
            ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();
            service.consentsPurposesPurposeIdDelete("101");
            Assert.fail("Expected " + NotFoundException.class.getName());
        } catch (Throwable e) {
            Assert.assertTrue(e instanceof NotFoundException);
        }
    }

    @Test
    public void testConsentsPurposesPurposeIdGet() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PurposeRequestDTO purposeRequestDTO = new PurposeRequestDTO();
        purposeRequestDTO.setPurpose("P1");
        purposeRequestDTO.setDescription("D1");
        purposeRequestDTO.setGroup("SIGNUP");
        purposeRequestDTO.setGroupType("SYSTEM");
        purposeRequestDTO.setMandatory(true);
        Response response = service.consentsPurposesPost(purposeRequestDTO);
        PurposeGetResponseDTO responseDTO = (PurposeGetResponseDTO) response.getEntity();

        Response purposeIdGet = service.consentsPurposesPurposeIdGet(Integer.toString(responseDTO.getPurposeId()));
        PurposeGetResponseDTO responseDTO1 = (PurposeGetResponseDTO) purposeIdGet.getEntity();

        Assert.assertEquals(responseDTO1.getPurposeId(), responseDTO.getPurposeId());
    }

    @Test
    public void testConsentsPurposesInvalidPurposeIdGet() throws Exception {

        try {
            ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();
            service.consentsPurposesPurposeIdGet("101");
            Assert.fail("Expected " + NotFoundException.class.getName());
        } catch (Throwable e) {
            Assert.assertTrue(e instanceof NotFoundException);
        }
    }

    @Test
    public void testConsentsPiiCategoriesGet() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PIIcategoryRequestDTO requestDTO = new PIIcategoryRequestDTO();
        requestDTO.setPiiCategory("PII1");
        requestDTO.setDescription("D1");
        service.consentsPiiCategoriesPost(requestDTO);
        requestDTO.setPiiCategory("PII2");
        requestDTO.setDescription("D2");
        service.consentsPiiCategoriesPost(requestDTO);

        Response getResponse = service.consentsPiiCategoriesGet(2, 0);
        List<PiiCategoryListResponseDTO> responseDTOList = (List<PiiCategoryListResponseDTO>) getResponse.getEntity();

        Assert.assertNotNull(getResponse);
        Assert.assertEquals(responseDTOList.size(), 2);
    }

    @Test
    public void testConsentsPiiCategoriesPiiCategoryIdDelete() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PIIcategoryRequestDTO requestDTO = new PIIcategoryRequestDTO();
        requestDTO.setPiiCategory("PII1");
        requestDTO.setDescription("D1");
        Response response = service.consentsPiiCategoriesPost(requestDTO);
        PiiCategoryListResponseDTO responseDTO = (PiiCategoryListResponseDTO) response.getEntity();

        Response categoryIdDelete = service.consentsPiiCategoriesPiiCategoryIdDelete(
                Integer.toString(responseDTO.getPiiCategoryId()));

        Assert.assertNotNull(categoryIdDelete);
        Assert.assertEquals(categoryIdDelete.getStatus(), 200);
    }

    @Test
    public void testConsentsPiiCategoriesInvalidPiiCategoryIdDelete() throws Exception {

        try {
            ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

            service.consentsPiiCategoriesPiiCategoryIdDelete("101");
            Assert.fail("Expected " + NotFoundException.class.getName());
        } catch (Throwable e) {
            Assert.assertTrue(e instanceof NotFoundException);
        }
    }

    @Test
    public void testConsentsPiiCategoriesPiiCategoryIdGet() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PIIcategoryRequestDTO requestDTO = new PIIcategoryRequestDTO();
        requestDTO.setPiiCategory("PII1");
        requestDTO.setDescription("D1");
        Response response = service.consentsPiiCategoriesPost(requestDTO);
        PiiCategoryListResponseDTO responseDTO = (PiiCategoryListResponseDTO) response.getEntity();

        Response categoryIdGet = service.consentsPiiCategoriesPiiCategoryIdGet(
                Integer.toString(responseDTO.getPiiCategoryId()));
        PiiCategoryListResponseDTO listResponseDTO = (PiiCategoryListResponseDTO) categoryIdGet.getEntity();

        Assert.assertEquals(listResponseDTO.getPiiCategoryId(), responseDTO.getPiiCategoryId());
    }

    @Test
    public void testConsentsPiiCategoriesInvalidPiiCategoryIdGet() throws Exception {

        try {
            ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

            service.consentsPiiCategoriesPiiCategoryIdGet("101");
            Assert.fail("Expected " + NotFoundException.class.getName());
        } catch (Throwable e) {
            Assert.assertTrue(e instanceof NotFoundException);
        }
    }

    @Test
    public void testConsentsPiiCategoriesPost() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PIIcategoryRequestDTO requestDTO = new PIIcategoryRequestDTO();
        requestDTO.setPiiCategory("PII1");
        requestDTO.setDescription("D1");
        Response response = service.consentsPiiCategoriesPost(requestDTO);

        PiiCategoryListResponseDTO responseDTO = (PiiCategoryListResponseDTO) response.getEntity();

        Assert.assertNotNull(responseDTO);
        Assert.assertNotNull(responseDTO.getPiiCategoryId());
    }

    @Test
    public void testConsentsDuplicatePiiCategoriesPost() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        try {

            PIIcategoryRequestDTO requestDTO = new PIIcategoryRequestDTO();
            requestDTO.setPiiCategory("PII1");
            requestDTO.setDescription("D1");
            service.consentsPiiCategoriesPost(requestDTO);
            service.consentsPiiCategoriesPost(requestDTO);
            Assert.fail("Expected " + ConflictRequestException.class.getName());
        } catch (Throwable e) {
            Assert.assertTrue(e instanceof ConflictRequestException);
        }
    }

    @Test
    public void testConsentsPurposeCategoriesGet() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PurposeCategoryRequestDTO requestDTO = new PurposeCategoryRequestDTO();
        requestDTO.setPurposeCategory("PC1");
        requestDTO.setDescription("D1");
        service.consentsPurposeCategoriesPost(requestDTO);
        requestDTO.setPurposeCategory("PC2");
        requestDTO.setDescription("D2");
        service.consentsPurposeCategoriesPost(requestDTO);

        Response getResponse = service.consentsPurposeCategoriesGet(2, 0);
        List<PurposeCategoryListResponseDTO> responseDTOList = (List<PurposeCategoryListResponseDTO>) getResponse
                .getEntity();

        Assert.assertNotNull(getResponse);
        Assert.assertEquals(responseDTOList.size(), 2);
    }

    @Test
    public void testConsentsPurposeCategoriesPost() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PurposeCategoryRequestDTO requestDTO = new PurposeCategoryRequestDTO();
        requestDTO.setPurposeCategory("PC1");
        requestDTO.setDescription("D1");
        Response response = service.consentsPurposeCategoriesPost(requestDTO);
        PurposeCategoryListResponseDTO responseDTO = (PurposeCategoryListResponseDTO) response.getEntity();

        Assert.assertNotNull(responseDTO);
        Assert.assertNotNull(responseDTO.getPurposeCategoryId());
    }

    @Test
    public void testConsentsPurposeCategoriesPurposeCategoryIdDelete() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PurposeCategoryRequestDTO requestDTO = new PurposeCategoryRequestDTO();
        requestDTO.setPurposeCategory("PC1");
        requestDTO.setDescription("D1");
        Response response = service.consentsPurposeCategoriesPost(requestDTO);
        PurposeCategoryListResponseDTO responseDTO = (PurposeCategoryListResponseDTO) response.getEntity();

        Response categoryIdDelete = service.consentsPurposeCategoriesPurposeCategoryIdDelete(
                Integer.toString(responseDTO.getPurposeCategoryId()));

        Assert.assertNotNull(categoryIdDelete);
        Assert.assertEquals(categoryIdDelete.getStatus(), 200);
    }

    @Test
    public void testConsentsPurposeCategoriesPurposeCategoryIdGet() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PurposeCategoryRequestDTO requestDTO = new PurposeCategoryRequestDTO();
        requestDTO.setPurposeCategory("PC1");
        requestDTO.setDescription("D1");
        Response response = service.consentsPurposeCategoriesPost(requestDTO);
        PurposeCategoryListResponseDTO responseDTO = (PurposeCategoryListResponseDTO) response.getEntity();

        Response categoryIdGet = service.consentsPurposeCategoriesPurposeCategoryIdGet(
                Integer.toString(responseDTO.getPurposeCategoryId()));
        PurposeCategoryListResponseDTO listResponseDTO = (PurposeCategoryListResponseDTO) categoryIdGet.getEntity();

        Assert.assertEquals(listResponseDTO.getPurposeCategoryId(), responseDTO.getPurposeCategoryId());
    }

    @Test
    public void testConsentsPost() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        Integer purposeId = addPurpose("P1");
        Integer purposeCategoryId = addPurposeCategory("PC1");
        Integer piiCategoryId1 = addPiiCategory("PII1");
        Integer piiCategoryId2 = addPiiCategory("PII2");

        ConsentRequestDTO consentRequestDTO = new ConsentRequestDTO();
        consentRequestDTO.setCollectionMethod("SIGN-UP");
        consentRequestDTO.setJurisdiction("CA");
        consentRequestDTO.setLanguage("EN");
        consentRequestDTO.setPolicyURL("http://foo-service.com/privacy");

        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setService("foo-service");
        serviceDTO.setTenantDomain("carbon.super");

        PurposeDTO purposeDTO = new PurposeDTO();
        purposeDTO.setPrimaryPurpose(true);
        purposeDTO.setPurposeCategoryId(Collections.singletonList(purposeCategoryId));
        purposeDTO.setConsentType("EXPLICIT");

        PiiCategoryListDTO piiCategoryListDTO = new PiiCategoryListDTO();
        piiCategoryListDTO.setPiiCategoryId(piiCategoryId1);
        piiCategoryListDTO.setValidity("days:30");

        PiiCategoryListDTO piiCategoryListDTO1 = new PiiCategoryListDTO();
        piiCategoryListDTO1.setPiiCategoryId(piiCategoryId2);
        piiCategoryListDTO1.setValidity("days:30");

        purposeDTO.setPiiCategory(Arrays.asList(piiCategoryListDTO, piiCategoryListDTO1));
        purposeDTO.setPurposeId(purposeId);
        purposeDTO.setTermination("days:30");
        purposeDTO.setThirdPartyDisclosure(false);

        serviceDTO.setPurposes(Collections.singletonList(purposeDTO));

        consentRequestDTO.setServices(Collections.singletonList(serviceDTO));

        Response consentsPost = service.consentsPost(consentRequestDTO);
        Assert.assertNotNull(consentsPost);

        AddReceiptResponse receiptResponse = (AddReceiptResponse) consentsPost.getEntity();

        Assert.assertNotNull(receiptResponse, "AddReceiptResponse cannot be null.");
        Assert.assertNotNull(receiptResponse.getConsentReceiptId(), "Receipt ID cannot be null in a receipt.");

        Response receiptIdGet = service.consentsReceiptsReceiptIdGet(receiptResponse.getConsentReceiptId());
        ConsentReceiptDTO receipt = (ConsentReceiptDTO) receiptIdGet.getEntity();

        Assert.assertNotNull(receipt, "ConsentReceiptDTO cannot be null.");
        Assert.assertEquals(receipt.getConsentReceiptID(), receiptResponse.getConsentReceiptId(), "ReceiptId " +
                "mismatch.");
        Assert.assertNotNull(receipt.getPiiPrincipalId(), "PiiPrincipalId cannot be null in a receipt.");
        Assert.assertNotNull(receipt.getPiiControllers(), "PiiControllers cannot be null in a receipt.");
    }

    @Test
    public void testConsentsReceiptsReceiptIdDelete() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();
        String receiptId = addReceipt();
        Response response = service.consentsReceiptsReceiptIdDelete(receiptId);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        Response receiptIdGet = service.consentsReceiptsReceiptIdGet(receiptId);
        ConsentReceiptDTO receipt = (ConsentReceiptDTO) receiptIdGet.getEntity();

        Assert.assertEquals(receipt.getState(), REVOKE_STATE, "Receipt state should be: " + REVOKE_STATE);
    }

    @Test
    public void testConsentsGet() throws Exception {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();
        String receiptId = addReceipt();
        Response response = service.consentsGet(10, 0, null, "carbon.super", null, null);
        Assert.assertNotNull(response, "ConsentsGet response cannot be null.");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        List<ConsentResponseDTO> responseDTOS = (List<ConsentResponseDTO>) response.getEntity();
        Assert.assertNotNull(responseDTOS, "ConsentResponseDTO list cannot be null.");
        Assert.assertEquals(responseDTOS.size(), 1, "ConsentResponseDTO list should have 1 entry.");
        Assert.assertEquals(responseDTOS.get(0).getConsentReceiptID(), receiptId, "Receipt IDs should match.");
    }

    private Integer addPurpose(String purpose) {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PurposeRequestDTO purposeRequestDTO = new PurposeRequestDTO();
        purposeRequestDTO.setPurpose(purpose);
        purposeRequestDTO.setDescription("D1");
        purposeRequestDTO.setGroup("SIGNUP");
        purposeRequestDTO.setGroupType("SYSTEM");
        purposeRequestDTO.setMandatory(true);
        Response response = service.consentsPurposesPost(purposeRequestDTO);
        PurposeGetResponseDTO responseDTO = (PurposeGetResponseDTO) response.getEntity();

        Assert.assertNotNull(responseDTO, "PurposeListResponse cannot be null.");
        Assert.assertNotNull(responseDTO.getPurposeId(), "PurposeId cannot be null.");

        return responseDTO.getPurposeId();
    }

    private Integer addPurposeCategory(String name) {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PurposeCategoryRequestDTO requestDTO = new PurposeCategoryRequestDTO();
        requestDTO.setPurposeCategory(name);
        requestDTO.setDescription("D1");
        Response response = service.consentsPurposeCategoriesPost(requestDTO);
        PurposeCategoryListResponseDTO responseDTO = (PurposeCategoryListResponseDTO) response.getEntity();

        Assert.assertNotNull(responseDTO, "PurposeCategoryListResponse cannot be null.");
        Assert.assertNotNull(responseDTO.getPurposeCategoryId(), "PurposeCategoryId cannot be null.");

        return responseDTO.getPurposeCategoryId();
    }

    private Integer addPiiCategory(String name) {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PIIcategoryRequestDTO requestDTO = new PIIcategoryRequestDTO();
        requestDTO.setPiiCategory(name);
        requestDTO.setDescription("D1");
        Response response = service.consentsPiiCategoriesPost(requestDTO);

        PiiCategoryListResponseDTO responseDTO = (PiiCategoryListResponseDTO) response.getEntity();

        Assert.assertNotNull(responseDTO, "PiiCategoryListResponse cannot be null.");
        Assert.assertNotNull(responseDTO.getPiiCategoryId(), "PiiCategoryId cannot be null.");

        return responseDTO.getPiiCategoryId();
    }

    private String addReceipt() {

        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        Integer purposeId = addPurpose("P1");
        Integer purposeCategoryId = addPurposeCategory("PC1");
        Integer piiCategoryId1 = addPiiCategory("PII1");
        Integer piiCategoryId2 = addPiiCategory("PII2");

        ConsentRequestDTO consentRequestDTO = new ConsentRequestDTO();
        consentRequestDTO.setCollectionMethod("SIGN-UP");
        consentRequestDTO.setJurisdiction("CA");
        consentRequestDTO.setLanguage("EN");
        consentRequestDTO.setPolicyURL("http://foo-service.com/privacy");

        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setService("foo-service");
        serviceDTO.setTenantDomain("carbon.super");

        PurposeDTO purposeDTO = new PurposeDTO();
        purposeDTO.setPrimaryPurpose(true);
        purposeDTO.setPurposeCategoryId(Collections.singletonList(purposeCategoryId));
        purposeDTO.setConsentType("EXPLICIT");

        PiiCategoryListDTO piiCategoryListDTO = new PiiCategoryListDTO();
        piiCategoryListDTO.setPiiCategoryId(piiCategoryId1);
        piiCategoryListDTO.setValidity("days:30");

        PiiCategoryListDTO piiCategoryListDTO1 = new PiiCategoryListDTO();
        piiCategoryListDTO1.setPiiCategoryId(piiCategoryId2);
        piiCategoryListDTO1.setValidity("days:30");

        purposeDTO.setPiiCategory(Arrays.asList(piiCategoryListDTO, piiCategoryListDTO1));
        purposeDTO.setPurposeId(purposeId);
        purposeDTO.setTermination("days:30");
        purposeDTO.setThirdPartyDisclosure(false);

        serviceDTO.setPurposes(Collections.singletonList(purposeDTO));

        consentRequestDTO.setServices(Collections.singletonList(serviceDTO));

        Response consentsPost = service.consentsPost(consentRequestDTO);
        Assert.assertNotNull(consentsPost);

        AddReceiptResponse receiptResponse = (AddReceiptResponse) consentsPost.getEntity();

        Assert.assertNotNull(receiptResponse, "AddReceiptResponse cannot be null.");
        Assert.assertNotNull(receiptResponse.getConsentReceiptId(), "Receipt ID cannot be null in a receipt.");

        return receiptResponse.getConsentReceiptId();
    }
}
