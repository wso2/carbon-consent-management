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
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PaginationLink;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeElementBinding;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeElementDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeListResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeSummaryDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionListResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.SetLatestVersionRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.util.TestUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class PurposesApiServiceImplTest {

    private Connection connection;
    private PurposesApiServiceImpl purposesApiService;
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
        purposesApiService = new PurposesApiServiceImpl();
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

        org.wso2.carbon.consent.mgt.core.util.ConsentConfigParser configParser =
                new org.wso2.carbon.consent.mgt.core.util.ConsentConfigParser();
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
    // BASIC CRUD: PURPOSES
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
    public void testPurposesCreate() {

        PurposeCreateRequest request = new PurposeCreateRequest();
        request.setName("Marketing");
        request.setDescription("Send marketing emails");
        request.setType("MARKETING");
        request.setVersion("v1");

        Response response = purposesApiService.purposesCreate(request);

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        Assert.assertNotNull(response.getEntity());
    }

    @Test
    public void testPurposesCreateWithoutDescription() {

        PurposeCreateRequest request = new PurposeCreateRequest();
        request.setName("Analytics");
        request.setType("ANALYTICS");
        request.setVersion("v1");

        Response response = purposesApiService.purposesCreate(request);

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    // =========================================================================
    // ERROR HANDLING & VALIDATION
    // =========================================================================

    @Test
    public void testPurposesCreateMissingGroup() {

        PurposeCreateRequest request = new PurposeCreateRequest();
        request.setName("Marketing");
        request.setDescription("Send marketing emails");
        // Missing group

        Response response = purposesApiService.purposesCreate(request);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testPurposesGet() {

        PurposeCreateRequest request = new PurposeCreateRequest();
        request.setName("Analytics");
        request.setType("ANALYTICS");
        request.setVersion("v1");
        Response created = purposesApiService.purposesCreate(request);
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        Response response = purposesApiService.purposesGet(purposeId);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        PurposeDTO dto = (PurposeDTO) response.getEntity();
        Assert.assertEquals(dto.getName(), "Analytics");
        Assert.assertEquals(dto.getId(), purposeId);
    }

    @Test
    public void testPurposesGetNotFound() {

        Response response = purposesApiService.purposesGet(UUID.randomUUID());

        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testPurposesGetReturnsElements() {

        ElementCreateRequest elementReq = new ElementCreateRequest();
        elementReq.setName("GET_ELEM_" + System.nanoTime());
        UUID elementId = ((ElementDTO) elementsApiService.elementsCreate(elementReq).getEntity()).getId();

        PurposeElementBinding binding = new PurposeElementBinding();
        binding.setId(elementId);
        binding.setMandatory(true);

        PurposeCreateRequest request = new PurposeCreateRequest();
        request.setName("Elements-Purpose");
        request.setType("ELEM_GROUP");
        request.setVersion("v1");
        request.setElements(Collections.singletonList(binding));
        UUID purposeId = ((PurposeDTO) purposesApiService.purposesCreate(request).getEntity()).getId();

        Response response = purposesApiService.purposesGet(purposeId);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        PurposeDTO dto = (PurposeDTO) response.getEntity();
        Assert.assertNotNull(dto.getElements());
        Assert.assertEquals(dto.getElements().size(), 1);
        PurposeElementDTO elem = dto.getElements().get(0);
        Assert.assertEquals(elem.getId(), elementId);
        Assert.assertTrue(Boolean.TRUE.equals(elem.getMandatory()));
    }

    @Test
    public void testPurposesGetWithVersionReturnsLatestVersionObject() {

        ElementCreateRequest elementReq = new ElementCreateRequest();
        elementReq.setName("VER_ELEM_" + System.nanoTime());
        UUID elementId = ((ElementDTO) elementsApiService.elementsCreate(elementReq).getEntity()).getId();

        PurposeElementBinding binding = new PurposeElementBinding();
        binding.setId(elementId);
        binding.setMandatory(true);

        PurposeCreateRequest request = new PurposeCreateRequest();
        request.setName("Versioned-Elements-Purpose");
        request.setType("VER_ELEM_GROUP");
        request.setVersion("v1.0");
        request.setElements(Collections.singletonList(binding));
        UUID purposeId = ((PurposeDTO) purposesApiService.purposesCreate(request).getEntity()).getId();

        Response response = purposesApiService.purposesGet(purposeId);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        PurposeDTO dto = (PurposeDTO) response.getEntity();
        Assert.assertNotNull(dto.getLatestVersion(), "latestVersion must not be null when version was set at create");
        Assert.assertEquals(dto.getLatestVersion().getVersion(), "v1.0");
        Assert.assertNotNull(dto.getElements());
        Assert.assertEquals(dto.getElements().size(), 1);
        Assert.assertEquals(dto.getElements().get(0).getId(), elementId);
        Assert.assertTrue(Boolean.TRUE.equals(dto.getElements().get(0).getMandatory()));
    }

    @Test
    public void testPurposesList() {

        PurposeCreateRequest req1 = new PurposeCreateRequest();
        req1.setName("Marketing-List");
        req1.setType("MKT");
        req1.setVersion("v1");
        purposesApiService.purposesCreate(req1);

        PurposeCreateRequest req2 = new PurposeCreateRequest();
        req2.setName("Analytics-List");
        req2.setType("ANLYT");
        req2.setVersion("v1");
        purposesApiService.purposesCreate(req2);

        Response response = purposesApiService.purposesList(null, 10, null, null);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        PurposeListResponse list = (PurposeListResponse) response.getEntity();
        Assert.assertTrue(list.getTotalResults() >= 2);
    }

    @Test
    public void testPurposesListWithDefaultPagination() {

        Response response = purposesApiService.purposesList(null, 10, null, null);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertNotNull(response.getEntity());
    }

    @Test
    public void testPurposesListWithGroupFilter() {

        PurposeCreateRequest req = new PurposeCreateRequest();
        req.setName("Group-Filter-Purpose");
        req.setType("UNIQUE_GROUP");
        req.setVersion("v1");
        purposesApiService.purposesCreate(req);

        Response response = purposesApiService.purposesList("type eq \"UNIQUE_GROUP\"", 10, null, null);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        PurposeListResponse list = (PurposeListResponse) response.getEntity();
        Assert.assertEquals(list.getTotalResults(), 1);
    }

    /**
     * TDD (Stage 3): name filter must return only purposes whose name contains the filter string.
     * Fails until Stage 4/5 implements LIKE-based name filtering in the DAO and service layers.
     */
    @Test
    public void testPurposesList_withNameFilter() {

        long suffix = System.nanoTime();
        PurposeCreateRequest alpha = new PurposeCreateRequest();
        alpha.setName("ALPHA_PURPOSE_" + suffix);
        alpha.setType("NF_GROUP_A_" + suffix);
        alpha.setVersion("v1");
        purposesApiService.purposesCreate(alpha);

        PurposeCreateRequest beta = new PurposeCreateRequest();
        beta.setName("BETA_PURPOSE_" + suffix);
        beta.setType("NF_GROUP_B_" + suffix);
        beta.setVersion("v1");
        purposesApiService.purposesCreate(beta);

        PurposeCreateRequest gamma = new PurposeCreateRequest();
        gamma.setName("GAMMA_PURPOSE_" + suffix);
        gamma.setType("NF_GROUP_C_" + suffix);
        gamma.setVersion("v1");
        purposesApiService.purposesCreate(gamma);

        Response response = purposesApiService.purposesList("name co \"ALPHA\"", 10, null, null);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        PurposeListResponse list = (PurposeListResponse) response.getEntity();
        Assert.assertEquals(list.getTotalResults(), 1,
                "Name filter 'ALPHA' should return exactly 1 purpose");
        Assert.assertTrue(((PurposeSummaryDTO) list.getPurposes().get(0)).getName().contains("ALPHA"),
                "Returned purpose name must contain 'ALPHA'");
    }

    /**
     * TDD (Stage 3): list purposes includes latestVersion nested object when a version exists.
     * Fails until Stage 5 updates toPurposeSummaryDTO to populate latestVersion.
     */
    @Test
    public void testPurposesList_latestVersionIsNestedObject() {

        PurposeCreateRequest req = new PurposeCreateRequest();
        req.setName("Versioned-List-Purpose_" + System.nanoTime());
        req.setType("VLP_GROUP");
        req.setVersion("v1.0");
        purposesApiService.purposesCreate(req);

        Response response = purposesApiService.purposesList(null, 10, null, null);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        PurposeListResponse list = (PurposeListResponse) response.getEntity();
        PurposeSummaryDTO versioned = list.getPurposes().stream()
                .filter(p -> p.getName().startsWith("Versioned-List-Purpose"))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(versioned, "Versioned purpose must appear in list");
        Assert.assertNotNull(versioned.getLatestVersion(),
                "latestVersion must not be null for a purpose with a version");
        Assert.assertEquals(versioned.getLatestVersion().getVersion(), "v1.0");
        Assert.assertNotNull(versioned.getLatestVersion().getId());
    }

    @Test
    public void testPurposesDelete() {

        PurposeCreateRequest request = new PurposeCreateRequest();
        request.setName("ToDelete");
        request.setType("DEL_GROUP");
        request.setVersion("v1");
        Response created = purposesApiService.purposesCreate(request);
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        Response response = purposesApiService.purposesDelete(purposeId);

        Assert.assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        Response getResponse = purposesApiService.purposesGet(purposeId);
        Assert.assertEquals(getResponse.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testPurposesDeleteNotFound() {

        Response response = purposesApiService.purposesDelete(UUID.randomUUID());

        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    // =========================================================================
    // VERSIONS: CRUD OPERATIONS
    // =========================================================================

    @Test
    public void testPurposesVersionsCreate() {

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("Versioned-Purpose");
        purposeReq.setType("VER_GROUP");
        purposeReq.setVersion("v1");
        Response created = purposesApiService.purposesCreate(purposeReq);
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        PurposeVersionCreateRequest versionReq = new PurposeVersionCreateRequest();
        versionReq.setVersion("v1.0");
        versionReq.setDescription("Initial version");

        Response response = purposesApiService.purposesVersionsCreate(purposeId, versionReq);

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        PurposeVersionDTO dto = (PurposeVersionDTO) response.getEntity();
        Assert.assertEquals(dto.getVersion(), "v1.0");
        Assert.assertNotNull(dto.getId());
    }

    @Test
    public void testPurposesVersionsCreatePurposeNotFound() {

        PurposeVersionCreateRequest versionReq = new PurposeVersionCreateRequest();
        versionReq.setVersion("v1.0");

        Response response = purposesApiService.purposesVersionsCreate(UUID.randomUUID(), versionReq);

        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testPurposesVersionsList() {

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("Multi-Version-Purpose");
        purposeReq.setType("MULTI_VER");
        purposeReq.setVersion("v1");
        Response created = purposesApiService.purposesCreate(purposeReq);
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        PurposeVersionCreateRequest v1 = new PurposeVersionCreateRequest();
        v1.setVersion("v1.0");
        purposesApiService.purposesVersionsCreate(purposeId, v1);

        PurposeVersionCreateRequest v2 = new PurposeVersionCreateRequest();
        v2.setVersion("v2.0");
        purposesApiService.purposesVersionsCreate(purposeId, v2);

        Response response = purposesApiService.purposesVersionsList(purposeId, 10, null, null);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        PurposeVersionListResponse list = (PurposeVersionListResponse) response.getEntity();
        Assert.assertEquals(list.getTotalResults(), 3);
    }

    @Test
    public void testPurposesVersionsListDefaultPagination() {

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("Paginated-Version-Purpose");
        purposeReq.setType("PAGE_VER");
        purposeReq.setVersion("v1");
        Response created = purposesApiService.purposesCreate(purposeReq);
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        Response response = purposesApiService.purposesVersionsList(purposeId, 10, null, null);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertNotNull(response.getEntity());
    }

    @Test
    public void testPurposesVersionsListPurposeNotFound() {

        Response response = purposesApiService.purposesVersionsList(UUID.randomUUID(), 10, null, null);

        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testPurposesVersionsGet() {

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("Get-Version-Purpose");
        purposeReq.setType("GET_VER");
        purposeReq.setVersion("v1");
        Response created = purposesApiService.purposesCreate(purposeReq);
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        PurposeVersionCreateRequest versionReq = new PurposeVersionCreateRequest();
        versionReq.setVersion("v1.0");
        Response versionCreated = purposesApiService.purposesVersionsCreate(purposeId, versionReq);
        UUID versionId = ((PurposeVersionDTO) versionCreated.getEntity()).getId();

        Response response = purposesApiService.purposesVersionsGet(purposeId, versionId);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        PurposeVersionDTO dto = (PurposeVersionDTO) response.getEntity();
        Assert.assertEquals(dto.getVersion(), "v1.0");
    }

    @Test
    public void testPurposesVersionsGetNotFound() {

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("Get-Version-Not-Found-Purpose");
        purposeReq.setType("GVN_GROUP");
        purposeReq.setVersion("v1");
        Response created = purposesApiService.purposesCreate(purposeReq);
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        Response response = purposesApiService.purposesVersionsGet(purposeId, UUID.randomUUID());

        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testPurposesVersionsDelete() {

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("Delete-Version-Purpose");
        purposeReq.setType("DEL_VER");
        purposeReq.setVersion("v1");
        Response created = purposesApiService.purposesCreate(purposeReq);
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        PurposeVersionCreateRequest versionReq = new PurposeVersionCreateRequest();
        versionReq.setVersion("v1.0");
        Response versionCreated = purposesApiService.purposesVersionsCreate(purposeId, versionReq);
        UUID versionId = ((PurposeVersionDTO) versionCreated.getEntity()).getId();

        Response response = purposesApiService.purposesVersionsDelete(purposeId, versionId);

        Assert.assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        Response getResponse = purposesApiService.purposesVersionsGet(purposeId, versionId);
        Assert.assertEquals(getResponse.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testPurposesVersionsDeleteNotFound() {

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("Del-Version-NF-Purpose");
        purposeReq.setType("DVNF_GROUP");
        purposeReq.setVersion("v1");
        Response created = purposesApiService.purposesCreate(purposeReq);
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        Response response = purposesApiService.purposesVersionsDelete(purposeId, UUID.randomUUID());

        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testPurposesCreateDuplicate() {

        PurposeCreateRequest request = new PurposeCreateRequest();
        request.setName("DuplicatePurpose");
        request.setType("DUP_GROUP");
        request.setVersion("v1");
        purposesApiService.purposesCreate(request);

        Response response = purposesApiService.purposesCreate(request);

        Assert.assertEquals(response.getStatus(), Response.Status.CONFLICT.getStatusCode());
    }

    // =========================================================================
    // VERSIONS: LATEST VERSION MANAGEMENT
    // =========================================================================

    @Test
    public void testPurposesSetLatestVersion() {

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("Set-Latest-Purpose");
        purposeReq.setType("SETLATEST_GROUP");
        purposeReq.setVersion("v1");
        Response created = purposesApiService.purposesCreate(purposeReq);
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        PurposeVersionCreateRequest v1 = new PurposeVersionCreateRequest();
        v1.setVersion("v1.0");
        purposesApiService.purposesVersionsCreate(purposeId, v1);

        PurposeVersionCreateRequest v2 = new PurposeVersionCreateRequest();
        v2.setVersion("v2.0");
        Response v2Created = purposesApiService.purposesVersionsCreate(purposeId, v2);
        UUID v2Id = ((PurposeVersionDTO) v2Created.getEntity()).getId();

        SetLatestVersionRequest setLatestReq = new SetLatestVersionRequest();
        setLatestReq.setId(v2Id);

        Response response = purposesApiService.purposesSetLatestVersion(purposeId, setLatestReq);

        Assert.assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void testPurposesSetLatestVersionPurposeNotFound() {

        SetLatestVersionRequest req = new SetLatestVersionRequest();
        req.setId(UUID.randomUUID());

        Response response = purposesApiService.purposesSetLatestVersion(UUID.randomUUID(), req);

        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testPurposesSetLatestVersionVersionNotFound() {

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("SetLatest-VersionNotFound-Purpose");
        purposeReq.setType("SLVNF_GROUP");
        purposeReq.setVersion("v1");
        Response created = purposesApiService.purposesCreate(purposeReq);
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        SetLatestVersionRequest req = new SetLatestVersionRequest();
        req.setId(UUID.randomUUID());

        Response response = purposesApiService.purposesSetLatestVersion(purposeId, req);

        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    // =========================================================================
    // DELETION CONSTRAINTS (Referential Integrity)
    // =========================================================================

    /**
     * Cannot delete an element that is bound to at least one purpose.
     */
    @Test
    public void testElementsDelete_withPurposeAttached_returns409() {

        ElementCreateRequest elementReq = new ElementCreateRequest();
        elementReq.setName("LOCKED_ELEM_" + System.nanoTime());
        UUID elementId = ((ElementDTO) elementsApiService.elementsCreate(elementReq).getEntity()).getId();

        PurposeElementBinding binding = new PurposeElementBinding();
        binding.setId(elementId);
        binding.setMandatory(false);

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("ELEM_LOCK_PURPOSE_" + System.nanoTime());
        purposeReq.setType("ELEM_LOCK_GROUP");
        purposeReq.setVersion("v1");
        purposeReq.setElements(Collections.singletonList(binding));
        purposesApiService.purposesCreate(purposeReq);

        // Element is now bound to a purpose — deletion must be rejected.
        Response response = elementsApiService.elementsDelete(elementId);

        Assert.assertEquals(response.getStatus(), Response.Status.CONFLICT.getStatusCode(),
                "Deleting an element attached to a purpose should return 409 Conflict");
    }

    /**
     * Cannot delete the version that is currently set as latest for its purpose.
     */
    @Test
    public void testVersionsDelete_latestVersion_returns409() {

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("LATEST_VER_PURPOSE_" + System.nanoTime());
        purposeReq.setType("LATEST_VER_GROUP");
        purposeReq.setVersion("v1.0");
        PurposeDTO createdPurpose = (PurposeDTO) purposesApiService.purposesCreate(purposeReq).getEntity();

        UUID purposeId = createdPurpose.getId();
        UUID latestVersionId = createdPurpose.getLatestVersion().getId();

        // v1.0 is the latest version — deletion must be rejected.
        Response response = purposesApiService.purposesVersionsDelete(purposeId, latestVersionId);

        Assert.assertEquals(response.getStatus(), Response.Status.CONFLICT.getStatusCode(),
                "Deleting the latest version of a purpose should return 409 Conflict");
    }

    @Test
    public void testPurposesVersionsGetReturnsElements() {

        ElementCreateRequest elementReq = new ElementCreateRequest();
        elementReq.setName("VER_GET_ELEM_" + System.nanoTime());
        UUID elementId = ((ElementDTO) elementsApiService.elementsCreate(elementReq).getEntity()).getId();

        PurposeElementBinding binding = new PurposeElementBinding();
        binding.setId(elementId);
        binding.setMandatory(true);

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("Version-Elements-Get-Purpose_" + System.nanoTime());
        purposeReq.setType("VGE_GROUP");
        purposeReq.setVersion("v1.0");
        purposeReq.setElements(Collections.singletonList(binding));
        PurposeDTO createdPurpose = (PurposeDTO) purposesApiService.purposesCreate(purposeReq).getEntity();
        UUID purposeId = createdPurpose.getId();
        UUID versionId = createdPurpose.getLatestVersion().getId();

        Response response = purposesApiService.purposesVersionsGet(purposeId, versionId);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        PurposeVersionDTO dto = (PurposeVersionDTO) response.getEntity();
        Assert.assertNotNull(dto.getElements(), "Version elements must not be null");
        Assert.assertFalse(dto.getElements().isEmpty(), "Version elements must not be empty");
        PurposeElementDTO elem = dto.getElements().get(0);
        Assert.assertNotNull(elem.getId(), "elementId in version response must not be null");
        Assert.assertEquals(elem.getId(), elementId);
        Assert.assertTrue(Boolean.TRUE.equals(elem.getMandatory()));
    }

    @Test
    public void testPurposesVersionsCreateWithProperties() {

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("Props-Version-Purpose");
        purposeReq.setType("PROPS_GROUP");
        purposeReq.setVersion("v1");
        Response created = purposesApiService.purposesCreate(purposeReq);
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        Map<String, String> props = new HashMap<>();
        props.put("legalBasis", "consent");
        props.put("retentionPeriod", "365");

        PurposeVersionCreateRequest versionReq = new PurposeVersionCreateRequest();
        versionReq.setVersion("v1.0");
        versionReq.setProperties(props);

        Response response = purposesApiService.purposesVersionsCreate(purposeId, versionReq);

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        PurposeVersionDTO dto = (PurposeVersionDTO) response.getEntity();
        Assert.assertNotNull(dto.getProperties());
        Assert.assertEquals(dto.getProperties().get("legalBasis"), "consent");
        Assert.assertEquals(dto.getProperties().get("retentionPeriod"), "365");
    }

    @Test
    public void testPurposesVersionsGetReturnsProperties() {

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("Get-Props-Purpose");
        purposeReq.setType("GET_PROPS");
        purposeReq.setVersion("v1");
        Response created = purposesApiService.purposesCreate(purposeReq);
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        Map<String, String> props = new HashMap<>();
        props.put("dataController", "Acme Corp");

        PurposeVersionCreateRequest versionReq = new PurposeVersionCreateRequest();
        versionReq.setVersion("v1.0");
        versionReq.setProperties(props);
        Response versionCreated = purposesApiService.purposesVersionsCreate(purposeId, versionReq);
        UUID versionId = ((PurposeVersionDTO) versionCreated.getEntity()).getId();

        Response response = purposesApiService.purposesVersionsGet(purposeId, versionId);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        PurposeVersionDTO dto = (PurposeVersionDTO) response.getEntity();
        Assert.assertNotNull(dto.getProperties());
        Assert.assertEquals(dto.getProperties().get("dataController"), "Acme Corp");
    }

    @Test
    public void testPurposesVersionsListDoesNotIncludeProperties() {

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("List-Props-Purpose");
        purposeReq.setType("LIST_PROPS");
        purposeReq.setVersion("v1");
        Response created = purposesApiService.purposesCreate(purposeReq);
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        Map<String, String> props = new HashMap<>();
        props.put("key", "value");

        PurposeVersionCreateRequest versionReq = new PurposeVersionCreateRequest();
        versionReq.setVersion("v1.0");
        versionReq.setProperties(props);
        purposesApiService.purposesVersionsCreate(purposeId, versionReq);

        Response listResponse = purposesApiService.purposesVersionsList(purposeId, 10, null, null);

        Assert.assertEquals(listResponse.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertNotNull(listResponse.getEntity());
    }

    @Test
    public void testCreatePurposeWithVersionAndPropertiesPassedThrough() {

        Map<String, String> props = new HashMap<>();
        props.put("legalBasis", "legitimate_interest");

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("Purpose-With-Version-Props");
        purposeReq.setType("VER_PROPS_GROUP");
        purposeReq.setVersion("v1.0");
        purposeReq.setProperties(props);

        Response created = purposesApiService.purposesCreate(purposeReq);
        Assert.assertEquals(created.getStatus(), Response.Status.CREATED.getStatusCode());
        UUID purposeId = ((PurposeDTO) created.getEntity()).getId();

        Response listResponse = purposesApiService.purposesVersionsList(purposeId, 10, null, null);
        Assert.assertEquals(listResponse.getStatus(), Response.Status.OK.getStatusCode());
        PurposeVersionListResponse listBody = (PurposeVersionListResponse) listResponse.getEntity();
        Assert.assertFalse(listBody.getVersions().isEmpty());
        UUID versionId = listBody.getVersions().get(0).getId();

        Response getResponse = purposesApiService.purposesVersionsGet(purposeId, versionId);
        Assert.assertEquals(getResponse.getStatus(), Response.Status.OK.getStatusCode());
        PurposeVersionDTO dto = (PurposeVersionDTO) getResponse.getEntity();
        Assert.assertNotNull(dto.getProperties());
        Assert.assertEquals(dto.getProperties().get("legalBasis"), "legitimate_interest");
    }

    // =========================================================================
    // PAGINATION: PURPOSES LIST
    // =========================================================================

    /**
     * With fewer results than the limit, no "next" link is produced.
     */
    @Test
    public void testPurposesList_fewerThanLimit_noNextLink() {

        long suffix = System.nanoTime();
        for (int i = 0; i < 3; i++) {
            PurposeCreateRequest req = new PurposeCreateRequest();
            req.setName("Pag-Few-" + i + "-" + suffix);
            req.setType("PAG_FEW_" + suffix);
            req.setVersion("v1");
            purposesApiService.purposesCreate(req);
        }

        Response response = purposesApiService.purposesList("type eq \"PAG_FEW_" + suffix + "\"", 5, null, null);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        PurposeListResponse list = (PurposeListResponse) response.getEntity();
        Assert.assertEquals(list.getTotalResults().intValue(), 3);
        Assert.assertTrue(list.getLinks() == null || list.getLinks().isEmpty(),
                "No 'next' link expected when results fit within the limit");
    }

    /**
     * With more results than the limit, a "next" link is returned and the item count is capped at limit.
     */
    @Test
    public void testPurposesList_moreThanLimit_hasNextLinkAndCappedCount() {

        long suffix = System.nanoTime();
        for (int i = 0; i < 4; i++) {
            PurposeCreateRequest req = new PurposeCreateRequest();
            req.setName("Pag-More-" + i + "-" + suffix);
            req.setType("PAG_MORE_" + suffix);
            req.setVersion("v1");
            purposesApiService.purposesCreate(req);
        }

        Response response = purposesApiService.purposesList("type eq \"PAG_MORE_" + suffix + "\"", 2, null, null);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        PurposeListResponse list = (PurposeListResponse) response.getEntity();
        Assert.assertEquals(list.getTotalResults().intValue(), 2,
                "totalResults should equal the limit when there are more results");
        Assert.assertNotNull(list.getLinks());
        Assert.assertFalse(list.getLinks().isEmpty(), "A 'next' link must be present when results exceed the limit");
        PaginationLink nextLink = list.getLinks().stream()
                .filter(l -> "next".equals(l.getRel())).findFirst().orElse(null);
        Assert.assertNotNull(nextLink, "A link with rel='next' must be present");
    }

    /**
     * The "next" link href contains the correct query parameters: after cursor and limit.
     */
    @Test
    public void testPurposesList_nextLinkHref_containsAfterCursorAndLimit() {

        long suffix = System.nanoTime();
        for (int i = 0; i < 3; i++) {
            PurposeCreateRequest req = new PurposeCreateRequest();
            req.setName("Pag-Href-" + i + "-" + suffix);
            req.setType("PAG_HREF_" + suffix);
            req.setVersion("v1");
            purposesApiService.purposesCreate(req);
        }

        int limit = 2;
        Response response = purposesApiService.purposesList("type eq \"PAG_HREF_" + suffix + "\"", limit, null, null);

        PurposeListResponse list = (PurposeListResponse) response.getEntity();
        PaginationLink nextLink = list.getLinks().stream()
                .filter(l -> "next".equals(l.getRel())).findFirst().orElseThrow();

        Assert.assertTrue(nextLink.getHref().contains("after="), "Next link href must contain 'after=' cursor param");
        Assert.assertTrue(nextLink.getHref().contains("limit=" + limit), "Next link href must contain 'limit=' param");

        // The cursor must decode to the UUID of the last item on the current page.
        String afterParam = nextLink.getHref().replaceAll(".*after=([^&]+).*", "$1");
        String decodedCursor = new String(Base64.getDecoder().decode(afterParam), StandardCharsets.UTF_8);
        String lastItemId = list.getPurposes().get(list.getPurposes().size() - 1).getId().toString();
        Assert.assertEquals(decodedCursor, lastItemId, "Cursor must be the UUID of the last item on the current page");
    }

    /**
     * Requesting the next page via the "after" cursor returns the remaining items
     * and produces a "previous" link.
     */
    @Test
    public void testPurposesList_withAfterCursor_returnRemainingItemsAndPreviousLink() {

        long suffix = System.nanoTime();
        for (int i = 0; i < 3; i++) {
            PurposeCreateRequest req = new PurposeCreateRequest();
            req.setName("Pag-After-" + i + "-" + suffix);
            req.setType("PAG_AFTER_" + suffix);
            req.setVersion("v1");
            purposesApiService.purposesCreate(req);
        }

        // First page: limit=2 → 2 items + next link.
        Response firstPage = purposesApiService.purposesList("type eq \"PAG_AFTER_" + suffix + "\"", 2, null, null);
        PurposeListResponse firstList = (PurposeListResponse) firstPage.getEntity();
        PaginationLink nextLink = firstList.getLinks().stream()
                .filter(l -> "next".equals(l.getRel())).findFirst().orElseThrow();
        String afterCursor = nextLink.getHref().replaceAll(".*after=([^&]+).*", "$1");

        // Second page using the cursor.
        Response secondPage = purposesApiService.purposesList("type eq \"PAG_AFTER_" + suffix + "\"", 2, afterCursor, null);

        Assert.assertEquals(secondPage.getStatus(), Response.Status.OK.getStatusCode());
        PurposeListResponse secondList = (PurposeListResponse) secondPage.getEntity();
        Assert.assertEquals(secondList.getTotalResults().intValue(), 1,
                "Second page should contain the remaining 1 item");

        // Items on page 2 must not overlap with items on page 1.
        List<UUID> firstIds = firstList.getPurposes().stream().map(PurposeSummaryDTO::getId).toList();
        secondList.getPurposes().forEach(item ->
                Assert.assertFalse(firstIds.contains(item.getId()),
                        "Second page must not contain items from the first page"));

        // A "previous" link must be present on the second page.
        Assert.assertNotNull(secondList.getLinks());
        PaginationLink prevLink = secondList.getLinks().stream()
                .filter(l -> "previous".equals(l.getRel())).findFirst().orElse(null);
        Assert.assertNotNull(prevLink, "A 'previous' link must be present when paginating forward");
        Assert.assertTrue(prevLink.getHref().contains("before="), "Previous link href must contain 'before=' param");
    }

    /**
     * Exactly limit results → no "next" link (boundary condition).
     */
    @Test
    public void testPurposesList_exactlyLimitResults_noNextLink() {

        long suffix = System.nanoTime();
        for (int i = 0; i < 3; i++) {
            PurposeCreateRequest req = new PurposeCreateRequest();
            req.setName("Pag-Exact-" + i + "-" + suffix);
            req.setType("PAG_EXACT_" + suffix);
            req.setVersion("v1");
            purposesApiService.purposesCreate(req);
        }

        Response response = purposesApiService.purposesList("type eq \"PAG_EXACT_" + suffix + "\"", 3, null, null);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        PurposeListResponse list = (PurposeListResponse) response.getEntity();
        Assert.assertEquals(list.getTotalResults().intValue(), 3);
        boolean hasNext = list.getLinks() != null &&
                list.getLinks().stream().anyMatch(l -> "next".equals(l.getRel()));
        Assert.assertFalse(hasNext, "No 'next' link expected when result count equals the limit exactly");
    }
}
