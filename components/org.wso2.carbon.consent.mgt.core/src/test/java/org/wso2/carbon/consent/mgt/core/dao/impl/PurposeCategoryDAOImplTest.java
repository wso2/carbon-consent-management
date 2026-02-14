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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.internal.ConsentManagerComponentDataHolder;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.closeH2Base;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.getConnection;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.mockComponentDataHolder;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.spyConnection;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.spyConnectionWithError;

public class PurposeCategoryDAOImplTest {

    private static List<PurposeCategory> purposeCategories = new ArrayList<>();
    private MockedStatic<ConsentManagerComponentDataHolder> componentDataHolderMock;

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();
        PurposeCategory purposeCategory1 = new PurposeCategory("PC1", "D1", -1234);
        PurposeCategory purposeCategory2 = new PurposeCategory("PC2", "D2", -1234);
        purposeCategories.add(purposeCategory1);
        purposeCategories.add(purposeCategory2);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        if (componentDataHolderMock != null) {
            componentDataHolderMock.close();
            componentDataHolderMock = null;
        }
        closeH2Base();
    }

    @DataProvider(name = "purposeCategoryListProvider")
    public Object[][] provideListData() throws Exception {

        return new Object[][]{
                // limit, offset, tenantId, resultSize
                {0, 0, -1234, 0},
                {1, 1, -1234, 1},
                {10, 0, -1234, 3}
        };
    }

    @Test
    public void testAddPurposeCategory() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        componentDataHolderMock = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            when(dataSource.getConnection()).thenReturn(connection);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
            PurposeCategory purposeCategory = purposeCategoryDAO.addPurposeCategory(purposeCategories.get(0));

            Assert.assertEquals(purposeCategory.getName(), purposeCategories.get(0).getName());
            Assert.assertEquals(purposeCategory.getDescription(), purposeCategories.get(0).getDescription());
            Assert.assertEquals(purposeCategory.getTenantId(), purposeCategories.get(0).getTenantId());
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testAddDuplicatePurposeCategory() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        componentDataHolderMock = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            when(dataSource.getConnection()).thenReturn(connection);
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
            purposeCategoryDAO.addPurposeCategory(purposeCategories.get(1));
            purposeCategoryDAO.addPurposeCategory(purposeCategories.get(1));

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test
    public void testGetPurposeCategoryById() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        componentDataHolderMock = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
            PurposeCategory purposeCategory = purposeCategoryDAO.addPurposeCategory(purposeCategories.get(0));
            Assert.assertEquals(purposeCategory.getName(), purposeCategories.get(0).getName());

            PurposeCategory purposeCategoryById = purposeCategoryDAO.getPurposeCategoryById(purposeCategory.getId());

            Assert.assertEquals(purposeCategoryById.getName(), purposeCategory.getName());
        }
    }

    @Test
    public void testGetPurposeCategoryByInvalidId() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        componentDataHolderMock = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
            PurposeCategory purposeCategoryById = purposeCategoryDAO.getPurposeCategoryById(0);

            Assert.assertNull(purposeCategoryById);
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testGetPurposeCategoryByIdWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        componentDataHolderMock = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
            purposeCategoryDAO.getPurposeCategoryById(0);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test
    public void testGetPurposeCategoryByName() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        componentDataHolderMock = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
            PurposeCategory purposeCategory = purposeCategoryDAO.addPurposeCategory(purposeCategories.get(0));
            Assert.assertEquals(purposeCategory.getName(), purposeCategories.get(0).getName());

            PurposeCategory purposeCategoryById = purposeCategoryDAO.getPurposeCategoryByName(purposeCategory.getName
                    (), purposeCategory.getTenantId());

            Assert.assertEquals(purposeCategoryById.getId(), purposeCategory.getId());
            Assert.assertEquals(purposeCategoryById.getName(), purposeCategory.getName());
            Assert.assertEquals(purposeCategoryById.getDescription(), purposeCategory.getDescription());
        }
    }

    @Test
    public void testGetPurposeCategoryByInvalidName() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        componentDataHolderMock = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
            PurposeCategory purposeCategoryByName = purposeCategoryDAO.getPurposeCategoryByName("InvalidName", 0);

            Assert.assertNull(purposeCategoryByName);
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testGetPurposeCategoryByNameWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        componentDataHolderMock = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
            purposeCategoryDAO.getPurposeCategoryByName("InvalidName", 0);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test(dataProvider = "purposeCategoryListProvider")
    public void testListPurposeCategories(int limit, int offset, int tenantId, int resultSize) throws Exception {

        DataSource dataSource = mock(DataSource.class);
        componentDataHolderMock = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();

            PurposeCategory purposeCategory1 = purposeCategoryDAO.addPurposeCategory(purposeCategories.get(0));
            Assert.assertEquals(purposeCategory1.getName(), purposeCategories.get(0).getName());

            PurposeCategory purposeCategory2 = purposeCategoryDAO.addPurposeCategory(purposeCategories.get(1));
            Assert.assertEquals(purposeCategory2.getName(), purposeCategories.get(1).getName());

            List<PurposeCategory> purposeCategoryList = purposeCategoryDAO.listPurposeCategories(limit, offset,
                    tenantId);

            Assert.assertEquals(purposeCategoryList.size(), resultSize);

            if (resultSize == 1) {
                Assert.assertEquals(purposeCategoryList.get(0).getName(), purposeCategories.get(offset + 1).getName());
            }
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testListPurposeCategoriesWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        componentDataHolderMock = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
            purposeCategoryDAO.listPurposeCategories(0, 0, 0);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test
    public void testDeletePurposeCategory() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        componentDataHolderMock = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
            PurposeCategory purposeCategory = purposeCategoryDAO.addPurposeCategory(purposeCategories.get(0));
            Assert.assertEquals(purposeCategory.getName(), purposeCategories.get(0).getName());

            int id = purposeCategoryDAO.deletePurposeCategory(purposeCategory.getId());

            Assert.assertEquals(new Integer(id), purposeCategory.getId());
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testDeletePurposeWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        componentDataHolderMock = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
            purposeCategoryDAO.deletePurposeCategory(0);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test
    public void testDeletePurposeCategoriesByTenantId() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        componentDataHolderMock = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
            purposeCategoryDAO.addPurposeCategory(purposeCategories.get(0));
            purposeCategoryDAO.addPurposeCategory(purposeCategories.get(1));

            int tenantId = purposeCategories.get(0).getTenantId();
            purposeCategoryDAO.deletePurposeCategoriesByTenantId(tenantId);

            Assert.assertTrue(Boolean.TRUE);
        }
    }

}