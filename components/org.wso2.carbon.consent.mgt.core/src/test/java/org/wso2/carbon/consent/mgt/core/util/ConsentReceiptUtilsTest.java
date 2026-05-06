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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.PIICategoryValidity;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategoryBinding;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConsentReceiptUtilsTest {

    private static final String SUBJECT_ID = "user@wso2.com";
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String SERVICE_ID = "TestService";
    private static final String PURPOSE_UUID = "purpose-uuid-001";
    private static final String VERSION_UUID = "version-uuid-001";
    private static final String LANG = "EN";

    @Mock
    private ConsentManager consentManager;

    private AutoCloseable mockCloseable;

    @BeforeMethod
    public void setUp() {

        mockCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        mockCloseable.close();
    }

    // --- buildReceiptInput ---

    @Test
    public void testBuildReceiptInput_sameUserNoAuthorizations() throws ConsentManagementException {

        PurposeCategory category = new PurposeCategory(1, "DEFAULT", "desc", -1234);
        when(consentManager.getPurposeCategoryByName("DEFAULT")).thenReturn(category);

        Purpose purpose = new Purpose(10, "TestPurpose", "desc", "grp", "grpType", -1234);
        when(consentManager.getPurposeByUuid(PURPOSE_UUID)).thenReturn(purpose);

        PurposePIICategoryBinding binding = new PurposePIICategoryBinding(PURPOSE_UUID, Collections.emptyList());
        ReceiptInput result = ConsentReceiptUtils.buildReceiptInput(
                LANG, SUBJECT_ID, TENANT_DOMAIN, null, false,
                null, null, SERVICE_ID, List.of(binding), consentManager);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getPiiPrincipalId(), SUBJECT_ID);
        Assert.assertEquals(result.getTenantDomain(), TENANT_DOMAIN);
        Assert.assertEquals(result.getVersion(), ConsentConstants.API_VERSION);
        Assert.assertEquals(result.getCollectionMethod(), "V2");
        Assert.assertTrue(result.isAllowMultipleActiveReceipts());
        Assert.assertEquals(result.getLanguage(), LANG);

        Assert.assertEquals(result.getServices().size(), 1);
        ReceiptServiceInput service = result.getServices().get(0);
        Assert.assertEquals(service.getService(), SERVICE_ID);
        Assert.assertEquals(service.getTenantDomain(), TENANT_DOMAIN);

        Assert.assertEquals(service.getPurposes().size(), 1);
        ReceiptPurposeInput purposeInput = service.getPurposes().get(0);
        Assert.assertEquals(purposeInput.getPurposeId(), (Integer) 10);
        Assert.assertEquals(purposeInput.getConsentType(), "EXPLICIT");
        Assert.assertTrue(purposeInput.isPrimaryPurpose());
        Assert.assertFalse(purposeInput.isThirdPartyDisclosure());
        Assert.assertEquals(purposeInput.getTermination(), "VALID_UNTIL:INDEFINITE");
        Assert.assertEquals(purposeInput.getPurposeCategoryId(), List.of(1));
    }

    @Test
    public void testBuildReceiptInput_differentUserWithAuthorizations() throws ConsentManagementException {

        PurposeCategory category = new PurposeCategory(1, "DEFAULT", "desc", -1234);
        when(consentManager.getPurposeCategoryByName("DEFAULT")).thenReturn(category);

        Purpose purpose = new Purpose(10, "TestPurpose", "desc", "grp", "grpType", -1234);
        when(consentManager.getPurposeByUuid(PURPOSE_UUID)).thenReturn(purpose);

        PurposePIICategoryBinding binding = new PurposePIICategoryBinding(PURPOSE_UUID, Collections.emptyList());
        ReceiptInput result = ConsentReceiptUtils.buildReceiptInput(
                LANG, SUBJECT_ID, TENANT_DOMAIN, null, false,
                List.of("admin@wso2.com"), null, SERVICE_ID, List.of(binding), consentManager);

        Assert.assertEquals(result.getPiiPrincipalId(), SUBJECT_ID);
        Assert.assertEquals(result.getAuthorizations(), List.of("admin@wso2.com"));
    }

    @Test
    public void testBuildReceiptInput_rejectedState() throws ConsentManagementException {

        when(consentManager.getPurposeCategoryByName("DEFAULT"))
                .thenReturn(new PurposeCategory(1, "DEFAULT", "desc", -1234));

        ReceiptInput result = ConsentReceiptUtils.buildReceiptInput(
                LANG, SUBJECT_ID, TENANT_DOMAIN, null, true,
                null, null, SERVICE_ID, Collections.emptyList(), consentManager);

        Assert.assertEquals(result.getState(), ConsentConstants.REJECTED_STATE);
    }

    @Test
    public void testBuildReceiptInput_notRejectedState_stateIsNull() throws ConsentManagementException {

        when(consentManager.getPurposeCategoryByName("DEFAULT"))
                .thenReturn(new PurposeCategory(1, "DEFAULT", "desc", -1234));

        ReceiptInput result = ConsentReceiptUtils.buildReceiptInput(
                LANG, SUBJECT_ID, TENANT_DOMAIN, null, false,
                null, null, SERVICE_ID, Collections.emptyList(), consentManager);

        Assert.assertNull(result.getState());
    }

    @Test
    public void testBuildReceiptInput_withValidityTime() throws ConsentManagementException {

        when(consentManager.getPurposeCategoryByName("DEFAULT"))
                .thenReturn(new PurposeCategory(1, "DEFAULT", "desc", -1234));

        ReceiptInput result = ConsentReceiptUtils.buildReceiptInput(
                LANG, SUBJECT_ID, TENANT_DOMAIN, 3600L, false,
                null, null, SERVICE_ID, Collections.emptyList(), consentManager);

        Assert.assertEquals(result.getValidityTime(), 3600L);
    }

    @Test
    public void testBuildReceiptInput_withProperties() throws ConsentManagementException {

        when(consentManager.getPurposeCategoryByName("DEFAULT"))
                .thenReturn(new PurposeCategory(1, "DEFAULT", "desc", -1234));

        Map<String, String> props = new HashMap<>();
        props.put("key1", "val1");

        ReceiptInput result = ConsentReceiptUtils.buildReceiptInput(
                LANG, SUBJECT_ID, TENANT_DOMAIN, null, false,
                null, props, SERVICE_ID, Collections.emptyList(), consentManager);

        Assert.assertEquals(result.getProperties(), props);
    }

    @Test
    public void testBuildReceiptInput_purposeWithLatestVersion() throws ConsentManagementException {

        PurposeCategory category = new PurposeCategory(1, "DEFAULT", "desc", -1234);
        when(consentManager.getPurposeCategoryByName("DEFAULT")).thenReturn(category);

        Purpose purpose = new Purpose(10, "TestPurpose", "desc", "grp", "grpType", -1234);
        PurposeVersion version = new PurposeVersion();
        version.setUuid(VERSION_UUID);
        purpose.setLatestVersion(version);
        when(consentManager.getPurposeByUuid(PURPOSE_UUID)).thenReturn(purpose);

        PIICategory piiCategory = new PIICategory(5, "email", "Email", false, -1234, "email");
        PurposePIICategoryBinding binding = new PurposePIICategoryBinding(PURPOSE_UUID, List.of(piiCategory));

        ReceiptInput result = ConsentReceiptUtils.buildReceiptInput(
                LANG, SUBJECT_ID, TENANT_DOMAIN, null, false,
                null, null, SERVICE_ID, List.of(binding), consentManager);

        ReceiptPurposeInput purposeInput = result.getServices().get(0).getPurposes().get(0);
        Assert.assertEquals(purposeInput.getPurposeVersionId(), VERSION_UUID);

        List<PIICategoryValidity> piiValidities = purposeInput.getPiiCategory();
        Assert.assertEquals(piiValidities.size(), 1);
        Assert.assertEquals(piiValidities.get(0).getId(), (Integer) 5);
        Assert.assertEquals(piiValidities.get(0).getValidity(), "VALID_UNTIL:INDEFINITE");
        Assert.assertTrue(piiValidities.get(0).isConsented());
    }

    @Test
    public void testBuildReceiptInput_purposeWithNoLatestVersion_versionIdIsNull() throws ConsentManagementException {

        PurposeCategory category = new PurposeCategory(1, "DEFAULT", "desc", -1234);
        when(consentManager.getPurposeCategoryByName("DEFAULT")).thenReturn(category);

        Purpose purpose = new Purpose(10, "TestPurpose", "desc", "grp", "grpType", -1234);
        when(consentManager.getPurposeByUuid(PURPOSE_UUID)).thenReturn(purpose);

        PurposePIICategoryBinding binding = new PurposePIICategoryBinding(PURPOSE_UUID, null);

        ReceiptInput result = ConsentReceiptUtils.buildReceiptInput(
                LANG, SUBJECT_ID, TENANT_DOMAIN, null, false,
                null, null, SERVICE_ID, List.of(binding), consentManager);

        ReceiptPurposeInput purposeInput = result.getServices().get(0).getPurposes().get(0);
        Assert.assertNull(purposeInput.getPurposeVersionId());
        Assert.assertTrue(purposeInput.getPiiCategory().isEmpty());
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testBuildReceiptInput_purposeNotFound_throwsException() throws ConsentManagementException {

        when(consentManager.getPurposeCategoryByName("DEFAULT"))
                .thenReturn(new PurposeCategory(1, "DEFAULT", "desc", -1234));
        when(consentManager.getPurposeByUuid(PURPOSE_UUID)).thenReturn(null);

        PurposePIICategoryBinding binding = new PurposePIICategoryBinding(PURPOSE_UUID, Collections.emptyList());

        ConsentReceiptUtils.buildReceiptInput(
                LANG, SUBJECT_ID, TENANT_DOMAIN, null, false,
                null, null, SERVICE_ID, List.of(binding), consentManager);
    }

    @Test
    public void testBuildReceiptInput_nullPurposeBindings_emptyPurposeList() throws ConsentManagementException {

        ReceiptInput result = ConsentReceiptUtils.buildReceiptInput(
                LANG, SUBJECT_ID, TENANT_DOMAIN, null, false,
                null, null, SERVICE_ID, null, consentManager);

        Assert.assertTrue(result.getServices().get(0).getPurposes().isEmpty());
    }

    @Test
    public void testBuildReceiptInput_defaultPurposeCategoryNotFound_createsIt() throws ConsentManagementException {

        ConsentManagementClientException notFound = new ConsentManagementClientException(
                "Invalid Purpose Category name: DEFAULT",
                ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CAT_NAME_INVALID.getCode());
        when(consentManager.getPurposeCategoryByName("DEFAULT")).thenThrow(notFound);

        PurposeCategory created = new PurposeCategory(2, "DEFAULT", "For core functionalities of the product", -1234);
        when(consentManager.addPurposeCategory(any(PurposeCategory.class))).thenReturn(created);

        Purpose purpose = new Purpose(10, "TestPurpose", "desc", "grp", "grpType", -1234);
        when(consentManager.getPurposeByUuid(PURPOSE_UUID)).thenReturn(purpose);

        PurposePIICategoryBinding binding = new PurposePIICategoryBinding(PURPOSE_UUID, Collections.emptyList());

        ReceiptInput result = ConsentReceiptUtils.buildReceiptInput(
                LANG, SUBJECT_ID, TENANT_DOMAIN, null, false,
                null, null, SERVICE_ID, List.of(binding), consentManager);

        verify(consentManager).addPurposeCategory(any(PurposeCategory.class));
        Assert.assertEquals(result.getServices().get(0).getPurposes().get(0).getPurposeCategoryId(), List.of(2));
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testBuildReceiptInput_defaultPurposeCategoryOtherError_rethrows() throws ConsentManagementException {

        ConsentManagementClientException otherError = new ConsentManagementClientException(
                "Some other error", "CM_99999");
        when(consentManager.getPurposeCategoryByName("DEFAULT")).thenThrow(otherError);

        when(consentManager.getPurposeByUuid(PURPOSE_UUID))
                .thenReturn(new Purpose(10, "p", "d", "g", "gt", -1234));
        PurposePIICategoryBinding binding = new PurposePIICategoryBinding(PURPOSE_UUID, Collections.emptyList());

        ConsentReceiptUtils.buildReceiptInput(
                LANG, SUBJECT_ID, TENANT_DOMAIN, null, false,
                null, null, SERVICE_ID, List.of(binding), consentManager);
    }

    // --- getDefaultPiiCategory ---

    @Test
    public void testGetDefaultPiiCategory_existingCategory_returnsIt() throws ConsentManagementException {

        PIICategory existing = new PIICategory(3, "CONSENT_TYPE", "desc", false, -1234, "CONSENT_TYPE");
        when(consentManager.getPIICategoryByName("CONSENT_TYPE")).thenReturn(existing);

        PIICategory result = ConsentReceiptUtils.getDefaultPiiCategory("CONSENT_TYPE", consentManager);

        Assert.assertEquals(result.getId(), (Integer) 3);
        verify(consentManager, never()).addPIICategory(any());
    }

    @Test
    public void testGetDefaultPiiCategory_categoryNotFound_createsNew() throws ConsentManagementException {

        ConsentManagementClientException notFound = new ConsentManagementClientException(
                "Invalid PII Category name: CONSENT_TYPE",
                ConsentConstants.ErrorMessages.ERROR_CODE_PII_CAT_NAME_INVALID.getCode());
        when(consentManager.getPIICategoryByName("CONSENT_TYPE")).thenThrow(notFound);

        PIICategory created = new PIICategory(7, "CONSENT_TYPE", null, false, -1234, "CONSENT_TYPE");
        when(consentManager.addPIICategory(any(PIICategory.class))).thenReturn(created);

        PIICategory result = ConsentReceiptUtils.getDefaultPiiCategory("CONSENT_TYPE", consentManager);

        Assert.assertEquals(result.getId(), (Integer) 7);
        verify(consentManager).addPIICategory(any(PIICategory.class));
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testGetDefaultPiiCategory_otherClientException_rethrows() throws ConsentManagementException {

        ConsentManagementClientException otherError = new ConsentManagementClientException(
                "Unexpected error", "CM_99998");
        when(consentManager.getPIICategoryByName(anyString())).thenThrow(otherError);

        ConsentReceiptUtils.getDefaultPiiCategory("CONSENT_TYPE", consentManager);
    }
}
