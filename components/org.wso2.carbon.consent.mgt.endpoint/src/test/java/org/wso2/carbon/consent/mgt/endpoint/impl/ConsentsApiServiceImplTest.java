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

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.consent.mgt.core.InterceptingConsentManager;
import org.wso2.carbon.consent.mgt.core.dao.JdbcTemplate;
import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.dao.impl.PIICategoryDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.PurposeCategoryDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.PurposeDAOImpl;
import org.wso2.carbon.consent.mgt.core.model.ConsentManagerConfigurationHolder;
import org.wso2.carbon.consent.mgt.endpoint.dto.PIIcategoryRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PiiCategoryListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeCategoryListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeCategoryRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeRequestDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.wso2.carbon.consent.mgt.endpoint.impl.util.TestUtils.closeH2Base;
import static org.wso2.carbon.consent.mgt.endpoint.impl.util.TestUtils.getConnection;
import static org.wso2.carbon.consent.mgt.endpoint.impl.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.consent.mgt.endpoint.impl.util.TestUtils.spyConnection;

@PrepareForTest(PrivilegedCarbonContext.class)
public class ConsentsApiServiceImplTest extends PowerMockTestCase {

    private Connection connection;

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);

        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);

        DataSource dataSource = mock(DataSource.class);

        connection = getConnection();
        Connection spyConnection = spyConnection(connection);
        when(dataSource.getConnection()).thenReturn(spyConnection);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        ConsentManagerConfigurationHolder configurationHolder = new ConsentManagerConfigurationHolder();

        PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);
        configurationHolder.setPurposeDAOs(Collections.singletonList(purposeDAO));

        PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl(jdbcTemplate);
        configurationHolder.setPiiCategoryDAOs(Collections.singletonList(piiCategoryDAO));

        PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl(jdbcTemplate);
        configurationHolder.setPurposeCategoryDAOs(Collections.singletonList(purposeCategoryDAO));

        InterceptingConsentManager consentManager = new InterceptingConsentManager(configurationHolder, Collections
                .emptyList());

        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getOSGiService(InterceptingConsentManager.class, null)).thenReturn
                (consentManager);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn("carbon.super");
        when(privilegedCarbonContext.getTenantId()).thenReturn(-1234);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
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
        Response response = service.consentsPurposesPost(purposeRequestDTO);

        PurposeListResponseDTO responseDTO = (PurposeListResponseDTO) response.getEntity();

        Assert.assertNotNull(responseDTO);
        Assert.assertNotNull(responseDTO.getPurposeId());
    }

    @Test
    public void testConsentsPurposesGet() throws Exception {
        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PurposeRequestDTO purposeRequestDTO = new PurposeRequestDTO();
        purposeRequestDTO.setPurpose("P1");
        purposeRequestDTO.setDescription("D1");
        service.consentsPurposesPost(purposeRequestDTO);

        purposeRequestDTO.setPurpose("P2");
        purposeRequestDTO.setDescription("D2");
        service.consentsPurposesPost(purposeRequestDTO);

        Response getResponse = service.consentsPurposesGet(2, 0);
        Assert.assertNotNull(getResponse);
    }

    @Test
    public void testConsentsPurposesPurposeIdDelete() throws Exception {
        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PurposeRequestDTO purposeRequestDTO = new PurposeRequestDTO();
        purposeRequestDTO.setPurpose("P1");
        purposeRequestDTO.setDescription("D1");
        Response response = service.consentsPurposesPost(purposeRequestDTO);
        PurposeListResponseDTO responseDTO = (PurposeListResponseDTO) response.getEntity();
        Response response1 = service.consentsPurposesPurposeIdDelete(Integer.toString(responseDTO.getPurposeId()));

        Assert.assertNotNull(response1);
        Assert.assertEquals(response1.getStatus(), 200);
    }

    @Test
    public void testConsentsPurposesPurposeIdGet() throws Exception {
        ConsentsApiServiceImpl service = new ConsentsApiServiceImpl();

        PurposeRequestDTO purposeRequestDTO = new PurposeRequestDTO();
        purposeRequestDTO.setPurpose("P1");
        purposeRequestDTO.setDescription("D1");
        Response response = service.consentsPurposesPost(purposeRequestDTO);
        PurposeListResponseDTO responseDTO = (PurposeListResponseDTO) response.getEntity();

        Response purposeIdGet = service.consentsPurposesPurposeIdGet(Integer.toString(responseDTO.getPurposeId()));
        PurposeListResponseDTO responseDTO1 = (PurposeListResponseDTO) purposeIdGet.getEntity();

        Assert.assertEquals(responseDTO1.getPurposeId(), responseDTO.getPurposeId());
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
}