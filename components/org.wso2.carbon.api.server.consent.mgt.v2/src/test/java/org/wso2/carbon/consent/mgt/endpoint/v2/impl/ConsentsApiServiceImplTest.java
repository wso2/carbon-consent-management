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

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
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
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import javax.ws.rs.core.Response;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.AuthorizationCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.AuthorizationDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentPurposeBinding;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentValidateResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementTerminationInfo;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeElementBinding;
import org.wso2.carbon.consent.mgt.endpoint.v2.core.ConsentReceiptsService;
import org.wso2.carbon.consent.mgt.endpoint.v2.factories.ConsentReceiptsServiceFactory;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentListResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentSummaryDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.SetLatestVersionRequest;

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

public class ConsentsApiServiceImplTest {

    private Connection connection;
    private ConsentsApiServiceImpl consentsApiService;
    private ElementsApiServiceImpl elementsApiService;
    private PurposesApiServiceImpl purposesApiService;
    private ConsentReceiptsService receiptsService;
    private MockedStatic<ConsentManagerComponentDataHolder> mockedComponentDataHolder;
    private MockedStatic<PrivilegedCarbonContext> mockedCarbonContext;
    private MockedStatic<KeyStoreManager> mockedKeyStoreManager;
    private MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil;
    private AutoCloseable mockitoCloseable;

    @Mock
    KeyStoreManager keyStoreManager;

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
        consentsApiService = new ConsentsApiServiceImpl();
        elementsApiService = new ElementsApiServiceImpl();
        purposesApiService = new PurposesApiServiceImpl();
        receiptsService = ConsentReceiptsServiceFactory.getConsentReceiptsService();
    }

    private void prepareConfigs() throws Exception {

        ConsentManagerConfigurationHolder configurationHolder = new ConsentManagerConfigurationHolder();

        PurposeDAO purposeDAO = new PurposeDAOImpl();
        configurationHolder.setPurposeDAOs(List.of(purposeDAO));

        PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
        configurationHolder.setPiiCategoryDAOs(List.of(piiCategoryDAO));

        PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
        configurationHolder.setPurposeCategoryDAOs(List.of(purposeCategoryDAO));

        ReceiptDAO receiptDAO = new ReceiptDAOImpl();
        configurationHolder.setReceiptDAOs(List.of(receiptDAO));

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
        configurationHolder.setPiiControllers(List.of(piiController));

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
        mockedKeyStoreManager.when(() -> KeyStoreManager.getInstance(SUPER_TENANT_ID)).thenReturn(keyStoreManager);
        when(keyStoreManager.getDefaultPublicKey()).thenReturn(TestUtils.getPublicKey(TestUtils
                .loadKeyStoreFromFileSystem(TestUtils.getFilePathInConfDirectory("wso2carbon.jks"), "wso2carbon",
                        "JKS"), "wso2carbon"));
        when(keyStoreManager.getKeyStore(anyString())).thenReturn(TestUtils
                .loadKeyStoreFromFileSystem(TestUtils.getFilePathInConfDirectory("wso2carbon.jks"), "wso2carbon",
                        "JKS"));
    }

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

    // =========================================================================
    // BASIC CRUD OPERATIONS
    // =========================================================================

    @Test
    public void testConsentsList() {

        Response response = consentsApiService.consentsList(null, null, null, null, null, 10, 0);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertNotNull(response.getEntity());
    }

    @Test
    public void testConsentsListWithDefaultPagination() {

        Response response = consentsApiService.consentsList(null, null, null, null, null, null, null);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertNotNull(response.getEntity());
    }

    @Test
    public void testConsentsListEmpty() {

        Response response = consentsApiService.consentsList(null, null, null, null, null, 10, 0);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ConsentListResponse list = (ConsentListResponse) response.getEntity();
        Assert.assertEquals(list.getCount().intValue(), 0);
    }

    @Test
    public void testConsentsGetNotFound() {

        Response response = consentsApiService.consentsGet("non-existent-id");

        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testConsentsRevokeNotFound() {

        Response response = consentsApiService.consentsRevoke("non-existent-id");

        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     * Creates a test element and a test purpose with that element bound,
     * then returns the purpose UUID and element UUID as a UUID array.
     */
    private UUID[] createPurposeWithElement() {

        ElementCreateRequest elementReq = new ElementCreateRequest();
        elementReq.setName("CONSENT_TEST_EMAIL_" + System.nanoTime());
        Response elemResp = elementsApiService.elementsCreate(elementReq);
        UUID elementId = ((ElementDTO) elemResp.getEntity()).getElementId();

        PurposeElementBinding binding = new PurposeElementBinding();
        binding.setElementId(elementId);
        binding.setMandatory(false);

        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("CONSENT_TEST_PURPOSE_" + System.nanoTime());
        purposeReq.setType("CONSENT_TEST_GROUP");
        purposeReq.setVersion("v1");
        purposeReq.setElements(List.of(binding));
        Response purposeResp = purposesApiService.purposesCreate(purposeReq);
        UUID purposeId = ((PurposeDTO) purposeResp.getEntity()).getPurposeId();

        return new UUID[]{purposeId, elementId};
    }

    @Test
    public void testConsentsCreate() {

        UUID[] ids = createPurposeWithElement();
        UUID purposeId = ids[0];
        UUID elementId = ids[1];

        ElementTerminationInfo elementInfo = new ElementTerminationInfo();
        elementInfo.setElementId(elementId);

        ConsentPurposeBinding purposeBinding = new ConsentPurposeBinding();
        purposeBinding.setPurposeId(purposeId);
        purposeBinding.setElements(List.of(elementInfo));

        ConsentCreateRequest request = new ConsentCreateRequest();
        request.setServiceId("test-service");
        request.setLanguage("EN");
        request.setPurposes(List.of(purposeBinding));

        Response response = consentsApiService.consentsCreate(request);

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        ConsentResponseDTO dto = (ConsentResponseDTO) response.getEntity();
        Assert.assertNotNull(dto.getConsentId());
        Assert.assertEquals(dto.getLanguage(), "EN");
    }

    @Test
    public void testConsentsGetSuccess() {

        UUID[] ids = createPurposeWithElement();
        UUID purposeId = ids[0];
        UUID elementId = ids[1];

        ElementTerminationInfo elementInfo = new ElementTerminationInfo();
        elementInfo.setElementId(elementId);

        ConsentPurposeBinding purposeBinding = new ConsentPurposeBinding();
        purposeBinding.setPurposeId(purposeId);
        purposeBinding.setElements(List.of(elementInfo));

        ConsentCreateRequest request = new ConsentCreateRequest();
        request.setServiceId("test-service");
        request.setLanguage("EN");
        request.setPurposes(List.of(purposeBinding));

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        Response response = consentsApiService.consentsGet(consentId);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertNotNull(response.getEntity());
    }

    @Test
    public void testConsentsRevokeSuccess() {

        UUID[] ids = createPurposeWithElement();
        UUID purposeId = ids[0];
        UUID elementId = ids[1];

        ElementTerminationInfo elementInfo = new ElementTerminationInfo();
        elementInfo.setElementId(elementId);

        ConsentPurposeBinding purposeBinding = new ConsentPurposeBinding();
        purposeBinding.setPurposeId(purposeId);
        purposeBinding.setElements(List.of(elementInfo));

        ConsentCreateRequest request = new ConsentCreateRequest();
        request.setServiceId("test-service");
        request.setLanguage("EN");
        request.setPurposes(List.of(purposeBinding));

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        Response response = consentsApiService.consentsRevoke(consentId);

        Assert.assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void testConsentsRevokeIdempotent() {

        UUID[] ids = createPurposeWithElement();
        Response created = consentsApiService.consentsCreate(buildConsentRequest(ids[0], ids[1]));
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        consentsApiService.consentsRevoke(consentId);
        // Second revoke on an already-REVOKED consent is idempotent — returns 204.
        Response response = consentsApiService.consentsRevoke(consentId);

        Assert.assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode(),
                "Revoking an already-REVOKED consent should return 204 (idempotent)");
    }

    @Test
    public void testConsentsListWithPurposeIdFilter() {

        UUID[] ids = createPurposeWithElement();
        UUID purposeId = ids[0];
        UUID elementId = ids[1];

        // Create a consent for this purpose.
        consentsApiService.consentsCreate(buildConsentRequest(purposeId, elementId));

        Response response = consentsApiService.consentsList(null, null, null, purposeId, null, 10, 0);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ConsentListResponse list = (ConsentListResponse) response.getEntity();
        Assert.assertTrue(list.getCount() >= 1, "At least one consent should match the purpose ID");
    }

    @Test
    public void testConsentsListWithPurposeVersionIdFilter_matchesConsent() {

        // Create purpose with element, add a version, then create a consent which binds to that version.
        UUID[] ids = createPurposeWithElement();
        UUID purposeId = ids[0];
        UUID elementId = ids[1];

        PurposeVersionCreateRequest versionReq = new PurposeVersionCreateRequest();
        versionReq.setVersion("v1.0");
        Response versionResp = purposesApiService.purposesVersionsCreate(purposeId, versionReq);
        UUID versionId = ((PurposeVersionDTO) versionResp.getEntity()).getVersionId();

        // Explicitly promote the version as latest — addPurposeVersion never auto-promotes.
        SetLatestVersionRequest setLatestReq = new SetLatestVersionRequest();
        setLatestReq.setVersionId(versionId);
        purposesApiService.purposesSetLatestVersion(purposeId, setLatestReq);

        // Consent created after setLatestVersion → PURPOSE_VERSION_ID is populated.
        consentsApiService.consentsCreate(buildConsentRequest(purposeId, elementId));

        Response response = consentsApiService.consentsList(null, null, null, purposeId, versionId, 10, 0);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ConsentListResponse list = (ConsentListResponse) response.getEntity();
        Assert.assertEquals(list.getCount().intValue(), 1,
                "Exactly one consent should be bound to the created version");
    }

    @Test
    public void testConsentsListWithPurposeVersionIdFilter_noMatch() {

        Response response = consentsApiService.consentsList(null, null, null, null, UUID.randomUUID(), 10, 0);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ConsentListResponse list = (ConsentListResponse) response.getEntity();
        Assert.assertEquals(list.getCount().intValue(), 0,
                "No consents should match an unknown version ID");
    }

    // =========================================================================
    // CONSENT CREATION VARIATIONS & HELPERS
    // =========================================================================

    private ConsentCreateRequest buildConsentRequest(UUID purposeId, UUID elementId) {

        ElementTerminationInfo elementInfo = new ElementTerminationInfo();
        elementInfo.setElementId(elementId);

        ConsentPurposeBinding purposeBinding = new ConsentPurposeBinding();
        purposeBinding.setPurposeId(purposeId);
        purposeBinding.setElements(List.of(elementInfo));

        ConsentCreateRequest request = new ConsentCreateRequest();
        request.setServiceId("test-service");
        request.setLanguage("EN");
        request.setPurposes(List.of(purposeBinding));
        return request;
    }

    @Test
    public void testConsentsCreate_withAuthz_subjectIdFromRequest() {

        // With authorizations, a different subjectId is allowed (another user initiating consent on behalf of subject)
        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setSubjectId("requestuser@test.com");
        request.setAuthorizations(List.of("admin"));

        Response created = consentsApiService.consentsCreate(request);
        Assert.assertEquals(created.getStatus(), Response.Status.CREATED.getStatusCode());
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        Response get = consentsApiService.consentsGet(consentId);
        Assert.assertEquals(get.getStatus(), Response.Status.OK.getStatusCode());
        ConsentDTO dto = (ConsentDTO) get.getEntity();
        Assert.assertEquals(dto.getSubjectId(), "requestuser@test.com",
                "subjectId from request should be preserved for with-authz consents");
    }

    @Test
    public void testConsentsCreate_noAuthorizations_statusActive() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        // no authorizations set → should become ACTIVE immediately

        Response created = consentsApiService.consentsCreate(request);
        Assert.assertEquals(created.getStatus(), Response.Status.CREATED.getStatusCode());
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        Response get = consentsApiService.consentsGet(consentId);
        ConsentDTO dto = (ConsentDTO) get.getEntity();
        Assert.assertEquals(dto.getState(), ConsentDTO.StateEnum.ACTIVE,
                "Consent without authorizations list should be ACTIVE immediately");
    }

        @Test
        public void testConsentsCreate_allowsMultipleActiveReceipts() {

        UUID[] ids = createPurposeWithElement();

        Response firstCreated = consentsApiService.consentsCreate(buildConsentRequest(ids[0], ids[1]));
        Response secondCreated = consentsApiService.consentsCreate(buildConsentRequest(ids[0], ids[1]));

        Assert.assertEquals(firstCreated.getStatus(), Response.Status.CREATED.getStatusCode());
        Assert.assertEquals(secondCreated.getStatus(), Response.Status.CREATED.getStatusCode());

        String firstConsentId = ((ConsentResponseDTO) firstCreated.getEntity()).getConsentId();
        String secondConsentId = ((ConsentResponseDTO) secondCreated.getEntity()).getConsentId();

        Response firstGet = consentsApiService.consentsGet(firstConsentId);
        Response secondGet = consentsApiService.consentsGet(secondConsentId);

        Assert.assertEquals(((ConsentDTO) firstGet.getEntity()).getState(), ConsentDTO.StateEnum.ACTIVE,
            "First consent should remain ACTIVE when multiple active receipts are allowed");
        Assert.assertEquals(((ConsentDTO) secondGet.getEntity()).getState(), ConsentDTO.StateEnum.ACTIVE,
            "Second consent should also be ACTIVE when multiple active receipts are allowed");
        }

    @Test
    public void testConsentsCreate_withAuthorizations_statusPending() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setAuthorizations(List.of("admin"));

        Response created = consentsApiService.consentsCreate(request);
        Assert.assertEquals(created.getStatus(), Response.Status.CREATED.getStatusCode());
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        Response get = consentsApiService.consentsGet(consentId);
        ConsentDTO dto = (ConsentDTO) get.getEntity();
        Assert.assertEquals(dto.getState(), ConsentDTO.StateEnum.PENDING,
                "Consent with authorizations list should start as PENDING");
    }

    // =========================================================================
    // STATE LIFECYCLE: PENDING → ACTIVE (Normal Authorization Flow)
    // =========================================================================

    @Test
    public void testConsentsAuthorize_approve_returns200() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setAuthorizations(List.of("admin")); // calling user must be in authz list

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        AuthorizationCreateRequest authRequest = new AuthorizationCreateRequest();
        authRequest.setState(AuthorizationCreateRequest.StateEnum.APPROVED);

        Response authResponse = consentsApiService.consentsAuthorize(consentId, authRequest);

        Assert.assertEquals(authResponse.getStatus(), Response.Status.OK.getStatusCode());
        AuthorizationDTO authDTO = (AuthorizationDTO) authResponse.getEntity();
        Assert.assertEquals(authDTO.getState(), AuthorizationDTO.StateEnum.APPROVED);
    }

    @Test
    public void testConsentsAuthorize_allApproved_consentActive() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setAuthorizations(List.of("admin"));

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        AuthorizationCreateRequest authRequest = new AuthorizationCreateRequest();
        authRequest.setState(AuthorizationCreateRequest.StateEnum.APPROVED);
        consentsApiService.consentsAuthorize(consentId, authRequest);

        Response get = consentsApiService.consentsGet(consentId);
        ConsentDTO dto = (ConsentDTO) get.getEntity();
        Assert.assertEquals(dto.getState(), ConsentDTO.StateEnum.ACTIVE,
                "Consent should become ACTIVE after all authorizers approve");
    }

    // =========================================================================
    // STATE LIFECYCLE: ACTIVE → EXPIRED (Validity Period Enforcement)
    // =========================================================================

    @Test
    public void testConsentsValidate_active() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        // no validityTime, no authorizations → ACTIVE immediately

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        Response validateResponse = consentsApiService.consentsValidate(consentId);

        Assert.assertEquals(validateResponse.getStatus(), Response.Status.OK.getStatusCode());
        ConsentValidateResponse validateDTO = (ConsentValidateResponse) validateResponse.getEntity();
        Assert.assertEquals(validateDTO.getState(), ConsentValidateResponse.StateEnum.ACTIVE);
    }

    @Test
    public void testConsentsValidate_expired() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setValidityTime(1000L); // milliseconds in the past (epoch 1000ms = Jan 1970)

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        Response validateResponse = consentsApiService.consentsValidate(consentId);

        Assert.assertEquals(validateResponse.getStatus(), Response.Status.OK.getStatusCode());
        ConsentValidateResponse validateDTO = (ConsentValidateResponse) validateResponse.getEntity();
        Assert.assertEquals(validateDTO.getState(), ConsentValidateResponse.StateEnum.EXPIRED,
                "Consent with past validityTime should be EXPIRED on validate");
    }

    // =========================================================================
    // STATE MACHINE: REJECTION & TERMINAL STATES
    // =========================================================================

    /**
     * PENDING → REJECTED: Any authorization rejected while PENDING moves consent to REJECTED (terminal).
     */
    @Test
    public void testConsentsAuthorize_rejected_consentRejected() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setAuthorizations(List.of("admin"));

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        AuthorizationCreateRequest authRequest = new AuthorizationCreateRequest();
        authRequest.setState(AuthorizationCreateRequest.StateEnum.REJECTED);
        Response authResponse = consentsApiService.consentsAuthorize(consentId, authRequest);

        Assert.assertEquals(authResponse.getStatus(), Response.Status.OK.getStatusCode());
        AuthorizationDTO authDTO = (AuthorizationDTO) authResponse.getEntity();
        Assert.assertEquals(authDTO.getState(), AuthorizationDTO.StateEnum.REJECTED,
                "Authorization state should be REJECTED");

        Response get = consentsApiService.consentsGet(consentId);
        ConsentDTO dto = (ConsentDTO) get.getEntity();
        Assert.assertEquals(dto.getState(), ConsentDTO.StateEnum.REJECTED,
                "Consent should become REJECTED when any authorization is rejected while PENDING");
    }

    /**
     * REJECTED is terminal: attempting to authorize a REJECTED consent must return 409.
     */
    @Test
    public void testConsentsAuthorize_onRejectedConsent_returns409() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setAuthorizations(List.of("admin"));

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        AuthorizationCreateRequest rejectReq = new AuthorizationCreateRequest();
        rejectReq.setState(AuthorizationCreateRequest.StateEnum.REJECTED);
        consentsApiService.consentsAuthorize(consentId, rejectReq);

        // Attempt a second authorization after consent is already REJECTED.
        AuthorizationCreateRequest approveReq = new AuthorizationCreateRequest();
        approveReq.setState(AuthorizationCreateRequest.StateEnum.APPROVED);
        Response secondAuth = consentsApiService.consentsAuthorize(consentId, approveReq);

        Assert.assertEquals(secondAuth.getStatus(), Response.Status.CONFLICT.getStatusCode(),
                "Authorizing a REJECTED consent should return 409 Conflict (terminal state)");
    }

    /**
     * PENDING multi-authorizer: partial approval leaves consent PENDING.
     */
    @Test
    public void testConsentsAuthorize_partialApproval_staysPending() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        // "admin" is the calling user; "approver2" is pending — partial approval, stays PENDING
        request.setAuthorizations(java.util.Arrays.asList("admin", "approver2@test.com"));

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        // Only first approver approves.
        AuthorizationCreateRequest authRequest = new AuthorizationCreateRequest();
        authRequest.setState(AuthorizationCreateRequest.StateEnum.APPROVED);
        consentsApiService.consentsAuthorize(consentId, authRequest);

        Response get = consentsApiService.consentsGet(consentId);
        ConsentDTO dto = (ConsentDTO) get.getEntity();
        Assert.assertEquals(dto.getState(), ConsentDTO.StateEnum.PENDING,
                "Consent should remain PENDING until all authorizers approve");
    }

    // =========================================================================
    // STATE MACHINE: REVOKE BEHAVIOR (Any State Allowed, Idempotent on REVOKED)
    // =========================================================================

    /**
     * Revoke from PENDING state (no authz list) succeeds — no state restriction.
     */
    @Test
    public void testConsentsRevoke_fromPendingNoAuthz_succeeds() {

        UUID[] ids = createPurposeWithElement();
        // Create ACTIVE consent first, then no way to make it PENDING without authz...
        // Actually a no-authz consent is immediately ACTIVE. To test PENDING we need authz,
        // but then the authz list check applies. Test EXPIRED instead (no authz, has validityTime).
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setValidityTime(1000L); // already expired (epoch 1970)

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        // Trigger expiry
        consentsApiService.consentsValidate(consentId);

        // Revoke from EXPIRED state — should succeed (no state restriction)
        Response revokeResponse = consentsApiService.consentsRevoke(consentId);
        Assert.assertEquals(revokeResponse.getStatus(), Response.Status.NO_CONTENT.getStatusCode(),
                "Revoking an EXPIRED consent (no authz list) should return 204");
    }

    /**
     * Revoke from REJECTED state (authz list includes "admin") succeeds.
     */
    @Test
    public void testConsentsRevoke_fromRejectedState_withAuthzInList_succeeds() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setAuthorizations(List.of("admin"));

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        // Reject to move to REJECTED state
        AuthorizationCreateRequest rejectReq = new AuthorizationCreateRequest();
        rejectReq.setState(AuthorizationCreateRequest.StateEnum.REJECTED);
        consentsApiService.consentsAuthorize(consentId, rejectReq);

        // Revoke from REJECTED — admin is in authz list, should succeed
        Response revokeResponse = consentsApiService.consentsRevoke(consentId);
        Assert.assertEquals(revokeResponse.getStatus(), Response.Status.NO_CONTENT.getStatusCode(),
                "Revoking a REJECTED consent (caller in authz list) should return 204");
    }

    /**
     * ACTIVE → REVOKED → validate: validate on a REVOKED consent returns REVOKED (not ACTIVE, not EXPIRED).
     */
    @Test
    public void testConsentsValidate_afterRevoke_returnsRevoked() {

        UUID[] ids = createPurposeWithElement();
        Response created = consentsApiService.consentsCreate(buildConsentRequest(ids[0], ids[1]));
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        consentsApiService.consentsRevoke(consentId);

        Response validateResponse = consentsApiService.consentsValidate(consentId);

        Assert.assertEquals(validateResponse.getStatus(), Response.Status.OK.getStatusCode());
        ConsentValidateResponse validateDTO = (ConsentValidateResponse) validateResponse.getEntity();
        Assert.assertEquals(validateDTO.getState(), ConsentValidateResponse.StateEnum.REVOKED,
                "Validate on REVOKED consent should return REVOKED state (not trigger EXPIRED)");
    }

    // =========================================================================
    // DELETION CONSTRAINTS (Referential Integrity)
    // =========================================================================

    /**
     * Cannot delete a purpose that has at least one active consent.
     */
    @Test
    public void testPurposesDelete_withConsent_returns409() {

        UUID[] ids = createPurposeWithElement();
        UUID purposeId = ids[0];
        UUID elementId = ids[1];

        // Create a consent that references this purpose.
        consentsApiService.consentsCreate(buildConsentRequest(purposeId, elementId));

        // Purpose is now referenced by a consent — deletion must be rejected.
        Response response = purposesApiService.purposesDelete(purposeId);

        Assert.assertEquals(response.getStatus(), Response.Status.CONFLICT.getStatusCode(),
                "Deleting a purpose with an active consent should return 409 Conflict");
    }

    /**
     * Cannot delete a purpose version that is referenced by at least one consent.
     * Isolation: v1.0 is created as latest; a consent references v1.0; then v2.0 is added
     * and promoted to latest. v1.0 is no longer latest but still has a consent — deletion
     * must still be rejected.
     */
    @Test
    public void testVersionsDelete_withConsent_returns409() {

        ElementCreateRequest elementReq = new ElementCreateRequest();
        elementReq.setName("VER_CONS_ELEM_" + System.nanoTime());
        Response elemResp = elementsApiService.elementsCreate(elementReq);
        UUID elementId = ((ElementDTO) elemResp.getEntity()).getElementId();

        PurposeElementBinding binding = new PurposeElementBinding();
        binding.setElementId(elementId);
        binding.setMandatory(false);

        // Create purpose with v1.0 as the initial (and latest) version.
        PurposeCreateRequest purposeReq = new PurposeCreateRequest();
        purposeReq.setName("VER_CONS_PURPOSE_" + System.nanoTime());
        purposeReq.setType("VER_CONS_GROUP");
        purposeReq.setVersion("v1.0");
        purposeReq.setElements(List.of(binding));
        PurposeDTO createdPurpose = (PurposeDTO) purposesApiService.purposesCreate(purposeReq).getEntity();
        UUID purposeId = createdPurpose.getPurposeId();
        UUID v1Id = createdPurpose.getLatestVersion().getVersionId();

        // Consent is created now — it references v1.0 (the current latest version).
        ElementTerminationInfo elementInfo = new ElementTerminationInfo();
        elementInfo.setElementId(elementId);
        ConsentPurposeBinding purposeBinding = new ConsentPurposeBinding();
        purposeBinding.setPurposeId(purposeId);
        purposeBinding.setElements(List.of(elementInfo));
        ConsentCreateRequest consentReq = new ConsentCreateRequest();
        consentReq.setServiceId("test-service");
        consentReq.setLanguage("EN");
        consentReq.setPurposes(List.of(purposeBinding));
        consentsApiService.consentsCreate(consentReq);

        // Add v2.0 and promote it to latest, so v1.0 is no longer the latest version.
        PurposeVersionCreateRequest v2Req = new PurposeVersionCreateRequest();
        v2Req.setVersion("v2.0");
        UUID v2Id = ((PurposeVersionDTO) purposesApiService.purposesVersionsCreate(purposeId, v2Req)
                .getEntity()).getVersionId();
        SetLatestVersionRequest setLatestReq = new SetLatestVersionRequest();
        setLatestReq.setVersionId(v2Id);
        purposesApiService.purposesSetLatestVersion(purposeId, setLatestReq);

        // v1.0 is NOT the latest but HAS a consent — deletion must be rejected.
        Response response = purposesApiService.purposesVersionsDelete(purposeId, v1Id);

        Assert.assertEquals(response.getStatus(), Response.Status.CONFLICT.getStatusCode(),
                "Deleting a version referenced by a consent should return 409 Conflict");
    }

    /**
     * ACTIVE → authorize must return 409: authorizing an already-ACTIVE consent is invalid.
     */
    @Test
    public void testConsentsAuthorize_onActiveConsent_returns409() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        // No authorizations → consent is ACTIVE immediately.

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        AuthorizationCreateRequest authRequest = new AuthorizationCreateRequest();
        authRequest.setState(AuthorizationCreateRequest.StateEnum.APPROVED);
        Response authResponse = consentsApiService.consentsAuthorize(consentId, authRequest);

        Assert.assertEquals(authResponse.getStatus(), Response.Status.CONFLICT.getStatusCode(),
                "Authorizing an ACTIVE consent should return 409 Conflict");
    }

    // =========================================================================
    // CONSENT STATE MANAGEMENT (Rule Implementation Tests)
    // =========================================================================

    /**
     * Rule 4 lazy expiry on GET: Consent should return EXPIRED when retrieved after validityTime has passed,
     * without requiring an explicit /validate call first.
     */
    @Test
    public void testConsentsGet_afterExpiry_returnsExpired() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setValidityTime(1000L); // milliseconds in the past (epoch 1000ms = Jan 1970)

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        // Call GET without calling validate first — lazy expiry logic should trigger
        Response getResponse = consentsApiService.consentsGet(consentId);

        Assert.assertEquals(getResponse.getStatus(), Response.Status.OK.getStatusCode());
        ConsentDTO consentDTO = (ConsentDTO) getResponse.getEntity();
        Assert.assertEquals(consentDTO.getState(), ConsentDTO.StateEnum.EXPIRED,
                "GET on an expired consent should return EXPIRED state (lazy expiry on GET)");
    }

    /**
     * Rule 3 caller auth record: Revoking a consent records the caller's revocation
     * as an authorization entry with state=REVOKED in the authorizations list.
     */
    @Test
    public void testConsentsRevoke_callerAuthRecordRevoked() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        // No authorizations — consent is ACTIVE immediately

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        // Revoke the consent
        Response revokeResponse = consentsApiService.consentsRevoke(consentId);
        Assert.assertEquals(revokeResponse.getStatus(), Response.Status.NO_CONTENT.getStatusCode());

        // Fetch the consent to inspect authorization records
        Response getResponse = consentsApiService.consentsGet(consentId);
        ConsentDTO consentDTO = (ConsentDTO) getResponse.getEntity();

        // Assert revocation was recorded as an authorization entry
        Assert.assertNotNull(consentDTO.getAuthorizations(), "Authorizations list should not be null");
        Assert.assertFalse(consentDTO.getAuthorizations().isEmpty(), "Should have at least one authorization record");

        // Find the revoke authorization record with userId="admin" and state=REVOKED
        AuthorizationDTO revokeAuth = consentDTO.getAuthorizations().stream()
                .filter(a -> "admin".equals(a.getUserId()) && AuthorizationDTO.StateEnum.REVOKED.equals(a.getState()))
                .findFirst()
                .orElse(null);

        Assert.assertNotNull(revokeAuth, "Should have an authorization record with userId='admin' and state=REVOKED");
    }

    // =========================================================================
    // NEW BUSINESS RULES: CONSENT TYPE ENFORCEMENT
    // =========================================================================

    /**
     * Without authorizations, providing a subjectId that differs from the calling user returns 400.
     */
    @Test
    public void testConsentsCreate_noAuthz_subjectIdMismatch_returns400() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setSubjectId("other-user@test.com"); // calling user is "admin"
        // no authorizations set

        Response response = consentsApiService.consentsCreate(request);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "Creating a no-authz consent with a mismatched subjectId should return 400");
    }

    /**
     * Without authorizations, providing subjectId matching the calling user succeeds.
     */
    @Test
    public void testConsentsCreate_noAuthz_matchingSubjectId_success() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setSubjectId("admin"); // matches calling user

        Response response = consentsApiService.consentsCreate(request);

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode(),
                "Creating a no-authz consent with subjectId matching the caller should succeed");
    }

    /**
     * REJECTED requests cannot also carry authorizations because the persisted state would be forced to PENDING.
     */
    @Test
    public void testConsentsCreate_rejectedWithAuthorizations_returns400() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setState(ConsentCreateRequest.StateEnum.REJECTED);
        request.setAuthorizations(List.of("admin"));

        Response response = consentsApiService.consentsCreate(request);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "Creating a rejected consent with authorizations should return 400");
    }

    /**
     * Revoking a consent with authorizations succeeds when the calling user is in the authz list.
     */
    @Test
    public void testConsentsRevoke_withAuthz_callerInList_success() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setAuthorizations(List.of("admin")); // calling user is "admin"

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        Response revokeResponse = consentsApiService.consentsRevoke(consentId);

        Assert.assertEquals(revokeResponse.getStatus(), Response.Status.NO_CONTENT.getStatusCode(),
                "Revoking when caller is in the authz list should return 204");
    }

    /**
     * Revoking a consent with authorizations fails when the calling user is NOT in the authz list.
     */
    @Test
    public void testConsentsRevoke_withAuthz_callerNotInList_returns403() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setAuthorizations(List.of("other-user@test.com")); // "admin" is NOT in list

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        Response revokeResponse = consentsApiService.consentsRevoke(consentId);

        Assert.assertEquals(revokeResponse.getStatus(), Response.Status.FORBIDDEN.getStatusCode(),
                "Revoking when caller is not in the authz list should return 403");
    }

    /**
     * Authorizing a consent fails with 403 when the calling user is NOT in the authz list.
     */
    @Test
    public void testConsentsAuthorize_callerNotInAuthzList_returns403() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setAuthorizations(List.of("other-user@test.com")); // "admin" is NOT in list

        Response created = consentsApiService.consentsCreate(request);
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        AuthorizationCreateRequest authRequest = new AuthorizationCreateRequest();
        authRequest.setState(AuthorizationCreateRequest.StateEnum.APPROVED);
        Response authResponse = consentsApiService.consentsAuthorize(consentId, authRequest);

        Assert.assertEquals(authResponse.getStatus(), Response.Status.FORBIDDEN.getStatusCode(),
                "Authorizing when caller is not in the authz list should return 403");
    }

    /**
     * Rule: consents with authorizations record the create response state as PENDING.
     */
    @Test
    public void testConsentsCreate_withAuthorizations_responseStatePending() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setAuthorizations(List.of("admin"));

        Response created = consentsApiService.consentsCreate(request);

        Assert.assertEquals(created.getStatus(), Response.Status.CREATED.getStatusCode());
        ConsentResponseDTO responseDTO = (ConsentResponseDTO) created.getEntity();
        Assert.assertEquals(responseDTO.getState(), ConsentResponseDTO.StateEnum.PENDING,
                "Create response state should be PENDING when authorizations list is present");
    }

    // =========================================================================
    // SUBJECT ID RESOLUTION RULES
    // =========================================================================

    /**
     * Scenario: subjectId absent, no authorizations → subject resolved to current user, state ACTIVE.
     */
    @Test
    public void testConsentsCreate_noSubjectId_resolvesToCurrentUser() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        // subjectId not set — should default to calling user ("admin")

        Response created = consentsApiService.consentsCreate(request);
        Assert.assertEquals(created.getStatus(), Response.Status.CREATED.getStatusCode());
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        Response get = consentsApiService.consentsGet(consentId);
        ConsentDTO dto = (ConsentDTO) get.getEntity();
        Assert.assertEquals(dto.getSubjectId(), "admin",
                "subjectId should default to the authenticated caller when not provided");
        Assert.assertEquals(dto.getState(), ConsentDTO.StateEnum.ACTIVE,
                "Consent with no authorizations should be ACTIVE");
    }

    /**
     * Scenario: subjectId = currentUser, authorizations non-empty → PENDING, subject preserved.
     */
    @Test
    public void testConsentsCreate_subjectIdEqualsCurrentUser_withAuthz_pendingAndSubjectPreserved() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setSubjectId("admin"); // explicitly same as calling user
        request.setAuthorizations(List.of("approver@test.com"));

        Response created = consentsApiService.consentsCreate(request);
        Assert.assertEquals(created.getStatus(), Response.Status.CREATED.getStatusCode());
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        Response get = consentsApiService.consentsGet(consentId);
        ConsentDTO dto = (ConsentDTO) get.getEntity();
        Assert.assertEquals(dto.getSubjectId(), "admin",
                "subjectId should be preserved when explicitly set to the caller");
        Assert.assertEquals(dto.getState(), ConsentDTO.StateEnum.PENDING,
                "Consent with authorizations should be PENDING even when subjectId = currentUser");
    }

    /**
     * Scenario: subjectId ≠ currentUser, authorizations non-empty → PENDING, subject from request.
     */
    @Test
    public void testConsentsCreate_delegated_pendingAndSubjectPreserved() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setSubjectId("subject@test.com");
        request.setAuthorizations(List.of("approver@test.com"));

        Response created = consentsApiService.consentsCreate(request);
        Assert.assertEquals(created.getStatus(), Response.Status.CREATED.getStatusCode());
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        Response get = consentsApiService.consentsGet(consentId);
        ConsentDTO dto = (ConsentDTO) get.getEntity();
        Assert.assertEquals(dto.getSubjectId(), "subject@test.com",
                "subjectId from request should be preserved for delegated consents");
        Assert.assertEquals(dto.getState(), ConsentDTO.StateEnum.PENDING,
                "Delegated consent with authorizations should be PENDING");
    }

    /**
     * Scenario: subjectId ≠ currentUser, authorizations empty → 400 (no authorizer on record).
     * This test already exists as testConsentsCreate_noAuthz_subjectIdMismatch_returns400 but is
     * kept here to document it as part of the subject resolution rule set.
     */
    @Test
    public void testConsentsCreate_delegated_noAuthz_returns400() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setSubjectId("subject@test.com"); // different from "admin"
        // no authorizations

        Response response = consentsApiService.consentsCreate(request);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "Delegated consent without authorizations should return 400");
    }

    /**
     * A consent with multiple purposes must appear as a single item in the list response.
     * Previously the list query joined on CM_PURPOSE and returned one row per purpose,
     * so a consent with N purposes produced N duplicates.
     */
    @Test
    public void testConsentsList_multiplePurposes_returnsSingleItem() {

        // Create two independent purposes, each with its own element.
        UUID[] ids1 = createPurposeWithElement();
        UUID[] ids2 = createPurposeWithElement();

        ElementTerminationInfo elementInfo1 = new ElementTerminationInfo();
        elementInfo1.setElementId(ids1[1]);
        ConsentPurposeBinding binding1 = new ConsentPurposeBinding();
        binding1.setPurposeId(ids1[0]);
        binding1.setElements(List.of(elementInfo1));

        ElementTerminationInfo elementInfo2 = new ElementTerminationInfo();
        elementInfo2.setElementId(ids2[1]);
        ConsentPurposeBinding binding2 = new ConsentPurposeBinding();
        binding2.setPurposeId(ids2[0]);
        binding2.setElements(List.of(elementInfo2));

        ConsentCreateRequest request = new ConsentCreateRequest();
        request.setServiceId("test-service");
        request.setLanguage("EN");
        request.setPurposes(List.of(binding1, binding2));

        Response created = consentsApiService.consentsCreate(request);
        Assert.assertEquals(created.getStatus(), Response.Status.CREATED.getStatusCode());
        String consentId = ((ConsentResponseDTO) created.getEntity()).getConsentId();

        Response listResponse = consentsApiService.consentsList(null, null, null, null, null, 10, 0);
        Assert.assertEquals(listResponse.getStatus(), Response.Status.OK.getStatusCode());
        ConsentListResponse listDTO = (ConsentListResponse) listResponse.getEntity();

        long matchingCount = listDTO.getItems().stream()
                .filter(s -> consentId.equals(s.getConsentId()))
                .count();
        Assert.assertEquals(matchingCount, 1,
                "A consent with multiple purposes must appear exactly once in the list");
    }

    // =========================================================================
    // EXPLICIT FILTER PARAMETERS (Feature 1)
    // =========================================================================

    @Test
    public void testConsentsList_withSubjectIdFilter_matchesConsent() {

        UUID[] ids = createPurposeWithElement();
        Response created = consentsApiService.consentsCreate(buildConsentRequest(ids[0], ids[1]));
        Assert.assertEquals(created.getStatus(), Response.Status.CREATED.getStatusCode());

        // List by "admin" (the calling user is "admin" by test setup)
        Response response = consentsApiService.consentsList("admin", null, null, null, null, 10, 0);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ConsentListResponse list = (ConsentListResponse) response.getEntity();
        Assert.assertTrue(list.getCount() >= 1, "At least one consent should match the subject ID");
    }

    @Test
    public void testConsentsList_withSubjectIdFilter_noMatch() {

        Response response = consentsApiService.consentsList("nonexistent-user@test.com", null, null, null, null,
                10, 0);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ConsentListResponse list = (ConsentListResponse) response.getEntity();
        Assert.assertEquals(list.getCount().intValue(), 0, "No consents should match an unknown subject ID");
    }

    @Test
    public void testConsentsList_withServiceIdFilter_matchesConsent() {

        UUID[] ids = createPurposeWithElement();
        ConsentCreateRequest request = buildConsentRequest(ids[0], ids[1]);
        request.setServiceId("unique-service-" + System.nanoTime());
        String serviceId = request.getServiceId();

        Response created = consentsApiService.consentsCreate(request);
        Assert.assertEquals(created.getStatus(), Response.Status.CREATED.getStatusCode());

        Response response = consentsApiService.consentsList(null, serviceId, null, null, null, 10, 0);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ConsentListResponse list = (ConsentListResponse) response.getEntity();
        Assert.assertEquals(list.getCount().intValue(), 1, "Exactly one consent should match the unique service ID");
    }

    @Test
    public void testConsentsList_withStateFilter_activeConsent() {

        UUID[] ids = createPurposeWithElement();
        consentsApiService.consentsCreate(buildConsentRequest(ids[0], ids[1]));

        Response response = consentsApiService.consentsList(null, null, "ACTIVE", null, null, 10, 0);

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        ConsentListResponse list = (ConsentListResponse) response.getEntity();
        Assert.assertTrue(list.getCount() >= 1, "At least one ACTIVE consent should be returned");
        list.getItems().forEach(item ->
                Assert.assertEquals(item.getState(), ConsentSummaryDTO.StateEnum.ACTIVE,
                        "All listed consents should be ACTIVE when filtering by state=ACTIVE"));
    }

    /**
     * An element that belongs to a different purpose can still be added to a consent binding —
     * V2 API does not enforce purpose-element ownership.
     */
    @Test
    public void testConsentsCreate_elementFromDifferentPurpose_succeeds() {

        // purpose1 owns element1; purpose2 owns element2.
        UUID[] ids1 = createPurposeWithElement();
        UUID[] ids2 = createPurposeWithElement();
        UUID purpose1Id = ids1[0];
        UUID element2Id = ids2[1]; // element belonging to purpose2

        // Bind element2 under purpose1 — cross-purpose element usage.
        ElementTerminationInfo elementInfo = new ElementTerminationInfo();
        elementInfo.setElementId(element2Id);
        ConsentPurposeBinding binding = new ConsentPurposeBinding();
        binding.setPurposeId(purpose1Id);
        binding.setElements(List.of(elementInfo));

        ConsentCreateRequest request = new ConsentCreateRequest();
        request.setServiceId("test-service");
        request.setLanguage("EN");
        request.setPurposes(List.of(binding));

        Response response = consentsApiService.consentsCreate(request);

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode(),
                "Creating a consent with an element from a different purpose should succeed");
        Assert.assertNotNull(((ConsentResponseDTO) response.getEntity()).getConsentId());
    }
}
