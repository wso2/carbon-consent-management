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

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.internal.ConsentManagerComponentDataHolder;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;

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

@PrepareForTest(ConsentManagerComponentDataHolder.class)
public class PIICategoryDAOImplTest extends PowerMockTestCase {

    private static List<PIICategory> piiCategories = new ArrayList<>();

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();
        PIICategory piiCategory1 = new PIICategory("PII1", "D1", true, -1234);
        PIICategory piiCategory2 = new PIICategory("PII2", "D2", false, -1234);
        piiCategories.add(piiCategory1);
        piiCategories.add(piiCategory2);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        closeH2Base();
    }

    @DataProvider(name = "piiCategoryProvider")
    public Object[][] providePIICategoryData() throws Exception {

        return new Object[][]{
                {0},
                {1}
        };
    }

    @DataProvider(name = "piiCategoryListProvider")
    public Object[][] provideListData() throws Exception {

        return new Object[][]{
                // limit, offset, tenantId, resultSize
                {0, 0, -1234, 0},
                {1, 1, -1234, 1},
                {10, 0, -1234, 2}
        };
    }

    @Test
    public void testAddPIICategory() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockComponentDataHolder(dataSource);
        try (Connection connection = getConnection()) {

            when(dataSource.getConnection()).thenReturn(connection);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
            PIICategory piiCategory = piiCategoryDAO.addPIICategory(piiCategories.get(0));

            Assert.assertEquals(piiCategory.getName(), piiCategories.get(0).getName());
            Assert.assertEquals(piiCategory.getDescription(), piiCategories.get(0).getDescription());
            Assert.assertEquals(piiCategory.getSensitive(), piiCategories.get(0).getSensitive());
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testAddDuplicatePIICategory() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            when(dataSource.getConnection()).thenReturn(connection);
            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
            piiCategoryDAO.addPIICategory(piiCategories.get(1));
            piiCategoryDAO.addPIICategory(piiCategories.get(1));

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test(dataProvider = "piiCategoryProvider")
    public void testGetPIICategoryById(int piiCategoryIndexId) throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockComponentDataHolder(dataSource);
        PIICategory piiCategoryInput = piiCategories.get(piiCategoryIndexId);
        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
            PIICategory piiCategory = piiCategoryDAO.addPIICategory(piiCategoryInput);
            Assert.assertEquals(piiCategory.getName(), piiCategoryInput.getName());

            PIICategory piiCategoryById = piiCategoryDAO.getPIICategoryById(piiCategory.getId());

            Assert.assertEquals(piiCategoryById.getName(), piiCategory.getName());
        }
    }

    @Test
    public void testGetPIICategoryByInvalidId() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
            PIICategory piiCategoryById = piiCategoryDAO.getPIICategoryById(0);

            Assert.assertNull(piiCategoryById);
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testGetPIICategoryByIdWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
            piiCategoryDAO.getPIICategoryById(0);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test(dataProvider = "piiCategoryProvider")
    public void testGetPIICategoryByName(int piiCategoryIndexId) throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockComponentDataHolder(dataSource);
        PIICategory piiCategoryInput = piiCategories.get(piiCategoryIndexId);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
            PIICategory piiCategory = piiCategoryDAO.addPIICategory(piiCategoryInput);
            Assert.assertEquals(piiCategory.getName(), piiCategoryInput.getName());

            PIICategory piiCategoryById = piiCategoryDAO.getPIICategoryByName(piiCategory.getName(), piiCategory
                    .getTenantId());

            Assert.assertEquals(piiCategoryById.getId(), piiCategory.getId());
            Assert.assertEquals(piiCategoryById.getName(), piiCategory.getName());
            Assert.assertEquals(piiCategoryById.getDescription(), piiCategory.getDescription());
            Assert.assertEquals(piiCategoryById.getSensitive(), piiCategory.getSensitive());
            Assert.assertEquals(piiCategoryById.getTenantId(), piiCategory.getTenantId());
        }
    }

    @Test
    public void testGetPIICategoryByInvalidName() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
            PIICategory piiCategoryByName = piiCategoryDAO.getPIICategoryByName("InvalidName", -1);

            Assert.assertNull(piiCategoryByName);
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testGetPIICategoryByNameWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
            piiCategoryDAO.getPIICategoryByName("InvalidName", -1);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test(dataProvider = "piiCategoryListProvider")
    public void testListPIICategories(int limit, int offset, int tenantID, int resultSize) throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();

            PIICategory piiCategory1 = piiCategoryDAO.addPIICategory(piiCategories.get(0));
            Assert.assertEquals(piiCategory1.getName(), piiCategories.get(0).getName());

            PIICategory piiCategory2 = piiCategoryDAO.addPIICategory(piiCategories.get(1));
            Assert.assertEquals(piiCategory2.getName(), piiCategories.get(1).getName());

            List<PIICategory> piiCategoryList = piiCategoryDAO.listPIICategories(limit, offset, tenantID);

            Assert.assertEquals(piiCategoryList.size(), resultSize);

            if (resultSize == 1) {
                Assert.assertEquals(piiCategoryList.get(0).getName(), piiCategories.get(offset).getName());
                Assert.assertEquals(piiCategoryList.get(0).getTenantId(), piiCategories.get(offset).getTenantId());
            }
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testListPIICategoriesWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
            piiCategoryDAO.listPIICategories(0, 0, 0);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test
    public void testDeletePIICategory() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
            PIICategory piiCategory = piiCategoryDAO.addPIICategory(piiCategories.get(0));
            Assert.assertEquals(piiCategory.getName(), piiCategories.get(0).getName());

            int id = piiCategoryDAO.deletePIICategory(piiCategory.getId());

            Assert.assertEquals(new Integer(id), piiCategory.getId());
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testDeletePurposeWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
            piiCategoryDAO.deletePIICategory(0);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }
}
