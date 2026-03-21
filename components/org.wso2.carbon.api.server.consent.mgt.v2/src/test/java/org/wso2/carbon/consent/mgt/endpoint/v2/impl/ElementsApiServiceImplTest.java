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

package org.wso2.carbon.consent.mgt.endpoint.v2.impl;

import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
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
import org.wso2.carbon.consent.mgt.core.model.ConsentManagerConfigurationHolder;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementListResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.util.TestUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Collections;
import java.util.UUID;
import javax.sql.DataSource;
import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.consent.mgt.endpoint.v2.util.TestUtils.closeH2Base;
import static org.wso2.carbon.consent.mgt.endpoint.v2.util.TestUtils.getConnection;
import static org.wso2.carbon.consent.mgt.endpoint.v2.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.consent.mgt.endpoint.v2.util.TestUtils.mockComponentDataHolder;
import static org.wso2.carbon.consent.mgt.endpoint.v2.util.TestUtils.spyConnection;

public class ElementsApiServiceImplTest {

    private Connection connection;
    private ElementsApiServiceImpl elementsApiService;
    private MockedStatic<ConsentManagerComponentDataHolder> mockedComponentDataHolder;
    private MockedStatic<PrivilegedCarbonContext> mockedCarbonContext;
    private MockedStatic<KeyStoreManager> mockedKeyStoreManager;
    private MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil;
    private AutoCloseable mockitoCloseable;

    @BeforeMethod
    public void setUp() throws Exception {

        mockitoCloseable = MockitoAnnotations.openMocks(this);
        initiateH2Base();
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        connection = getConnection();
        Connection spyConnection = spyConnection(connection);
        when(dataSource.getConnection()).thenReturn(spyConnection);

        prepareConfigs();
        elementsApiService = new ElementsApiServiceImpl();
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
        when(authorizationManager.isUserAuthorized(anyString(), anyString(), anyString())).thenReturn(true);

        configurationHolder.setRealmService(realmService);

        // Use ConsentConfigParser to create DefaultPIIController
        org.wso2.carbon.consent.mgt.core.util.ConsentConfigParser configParser = new org.wso2.carbon.consent.mgt.core.util.ConsentConfigParser();
        PIIController piiController = new DefaultPIIController(configParser);
        configurationHolder.setPiiControllers(Collections.singletonList(piiController));

        ConsentManager consentManager = new InterceptingConsentManager(configurationHolder, Collections.emptyList());

        mockedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext carbonContext = mock(PrivilegedCarbonContext.class);
        mockedCarbonContext.when(PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(carbonContext);
        when(carbonContext.getOSGiService(ConsentManager.class, null)).thenReturn(consentManager);
        when(carbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(carbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        when(carbonContext.getUsername()).thenReturn("admin");

        mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class);
        mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(SUPER_TENANT_ID))
                .thenReturn(SUPER_TENANT_DOMAIN_NAME);

        mockedKeyStoreManager = mockStatic(KeyStoreManager.class);
    }

    // =========================================================================
    // BASIC CRUD: ELEMENTS
    // =========================================================================

    @AfterMethod
    public void tearDown() throws Exception {

        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        closeH2Base();
        mockedComponentDataHolder.close();
        mockedCarbonContext.close();
        mockedKeyStoreManager.close();
        mockedIdentityTenantUtil.close();
        mockitoCloseable.close();
    }

    @Test
    public void testElementsCreate() {

        ElementCreateRequest request = new ElementCreateRequest();
        request.setName("EMAIL");
        request.setDescription("Email address");

        Response response = elementsApiService.elementsCreate(request);

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        Assert.assertNotNull(response.getEntity());
    }

    @Test
    public void testElementsCreateWithoutDescription() {

        ElementCreateRequest request = new ElementCreateRequest();
        request.setName("PHONE");

        Response response = elementsApiService.elementsCreate(request);

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        Assert.assertNotNull(response.getEntity());
    }

    @Test
    public void testElementsList() {

        ElementCreateRequest request1 = new ElementCreateRequest();
        request1.setName("EMAIL");
        request1.setDescription("Email address");
        elementsApiService.elementsCreate(request1);

        ElementCreateRequest request2 = new ElementCreateRequest();
        request2.setName("PHONE");
        request2.setDescription("Phone number");
        elementsApiService.elementsCreate(request2);

        Response response = elementsApiService.elementsList(null, 10, 0);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertNotNull(response.getEntity());
    }

    @Test
    public void testElementsListWithDefaultPagination() {

        ElementCreateRequest request = new ElementCreateRequest();
        request.setName("EMAIL");
        elementsApiService.elementsCreate(request);

        Response response = elementsApiService.elementsList(null, null, null);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertNotNull(response.getEntity());
    }

    @Test
    public void testElementsListWithCustomPagination() {

        ElementCreateRequest request = new ElementCreateRequest();
        request.setName("EMAIL");
        elementsApiService.elementsCreate(request);

        Response response = elementsApiService.elementsList(null, 5, 0);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    // =========================================================================
    // ERROR HANDLING & VALIDATION
    // =========================================================================

    @Test
    public void testElementsCreateInvalidRequest() {

        ElementCreateRequest request = new ElementCreateRequest();
        // Missing required name field

        Response response = elementsApiService.elementsCreate(request);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testElementsGet() {

        ElementCreateRequest request = new ElementCreateRequest();
        request.setName("BIRTHDAY");
        request.setDescription("Date of birth");
        Response created = elementsApiService.elementsCreate(request);
        UUID elementId = ((ElementDTO) created.getEntity()).getElementId();

        Response response = elementsApiService.elementsGet(elementId);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ElementDTO dto = (ElementDTO) response.getEntity();
        Assert.assertEquals(dto.getName(), "BIRTHDAY");
        Assert.assertEquals(dto.getElementId(), elementId);
    }

    @Test
    public void testElementsGetNotFound() {

        Response response = elementsApiService.elementsGet(UUID.randomUUID());

        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testElementsDelete() {

        ElementCreateRequest request = new ElementCreateRequest();
        request.setName("TO_DELETE_ELEMENT");
        Response created = elementsApiService.elementsCreate(request);
        UUID elementId = ((ElementDTO) created.getEntity()).getElementId();

        Response response = elementsApiService.elementsDelete(elementId);

        Assert.assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        // Verify it's gone.
        Response getResponse = elementsApiService.elementsGet(elementId);
        Assert.assertEquals(getResponse.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testElementsDeleteNotFound() {

        Response response = elementsApiService.elementsDelete(UUID.randomUUID());

        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testElementsCreateDuplicate() {

        ElementCreateRequest request = new ElementCreateRequest();
        request.setName("DUPLICATE_ELEMENT");
        elementsApiService.elementsCreate(request);

        ElementCreateRequest duplicate = new ElementCreateRequest();
        duplicate.setName("DUPLICATE_ELEMENT");
        Response response = elementsApiService.elementsCreate(duplicate);

        Assert.assertEquals(response.getStatus(), Response.Status.CONFLICT.getStatusCode());
    }

    // =========================================================================
    // FILTERING & SEARCH
    // =========================================================================

    @Test
    public void testElementsList_withNameFilter() {

        long suffix = System.nanoTime();
        ElementCreateRequest alpha = new ElementCreateRequest();
        alpha.setName("ALPHA_ELEM_" + suffix);
        elementsApiService.elementsCreate(alpha);

        ElementCreateRequest beta = new ElementCreateRequest();
        beta.setName("BETA_ELEM_" + suffix);
        elementsApiService.elementsCreate(beta);

        ElementCreateRequest gamma = new ElementCreateRequest();
        gamma.setName("GAMMA_ELEM_" + suffix);
        elementsApiService.elementsCreate(gamma);

        Response response = elementsApiService.elementsList("name co \"ALPHA\"", 10, 0);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ElementListResponse list = (ElementListResponse) response.getEntity();
        Assert.assertEquals((int) list.getCount(), 1, "Name filter 'ALPHA' should return exactly 1 element");
        Assert.assertTrue(list.getItems().get(0).getName().contains("ALPHA"));
    }

    @Test
    public void testElementsList_noFilter_returnsAll() {

        long suffix = System.nanoTime();
        ElementCreateRequest e1 = new ElementCreateRequest();
        e1.setName("NOFILT_A_" + suffix);
        elementsApiService.elementsCreate(e1);

        ElementCreateRequest e2 = new ElementCreateRequest();
        e2.setName("NOFILT_B_" + suffix);
        elementsApiService.elementsCreate(e2);

        Response response = elementsApiService.elementsList(null, 10, 0);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ElementListResponse list = (ElementListResponse) response.getEntity();
        Assert.assertTrue(list.getItems().size() >= 2, "No filter should return all created elements");
    }
}
