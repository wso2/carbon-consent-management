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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.consent.mgt.core.dao.JdbcTemplate;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.closeH2Base;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.getConnection;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.spyConnection;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.spyConnectionWithError;

public class PurposeDAOImplTest {

    private static List<Purpose> purposes = new ArrayList<>();

    @BeforeMethod
    public void setUp() throws Exception {
        initiateH2Base();
        Purpose purpose1 = new Purpose("P1", "D1", -1234);
        Purpose purpose2 = new Purpose("P2", "D2", -1234);
        purposes.add(purpose1);
        purposes.add(purpose2);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        closeH2Base();
    }

    @DataProvider(name = "purposeListProvider")
    public Object[][] provideListData() throws Exception {
        return new Object[][]{
                // limit, offset, tenantId, resultSize
                {0, 0, -1234, 0},
                {1, 1, -1234, 1},
                {10, 0, -1234, 2}
        };
    }

    @Test
    public void testAddPurpose() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            when(dataSource.getConnection()).thenReturn(connection);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);
            Purpose purposeResult = purposeDAO.addPurpose(purposes.get(0));

            Assert.assertEquals(purposeResult.getName(), purposes.get(0).getName());
            Assert.assertEquals(purposeResult.getDescription(), purposes.get(0).getDescription());
            Assert.assertEquals(purposeResult.getTenantId(), purposes.get(0).getTenantId());
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testAddDuplicatePurpose() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);
            purposeDAO.addPurpose(purposes.get(0));
            purposeDAO.addPurpose(purposes.get(0));

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test
    public void testGetPurposeById() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);
            Purpose purposeResult = purposeDAO.addPurpose(purposes.get(0));
            Assert.assertEquals(purposeResult.getName(), purposes.get(0).getName());

            Purpose purposeById = purposeDAO.getPurposeById(purposeResult.getId());

            Assert.assertEquals(purposeById.getId(), purposeResult.getId());
            Assert.assertEquals(purposeById.getName(), purposeResult.getName());
            Assert.assertEquals(purposeById.getDescription(), purposeResult.getDescription());
        }
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testGetPurposeByInvalidId() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);
            purposeDAO.getPurposeById(0);

            Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testGetPurposeByIdWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);
            purposeDAO.getPurposeById(1);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test
    public void testGetPurposeByName() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);
            Purpose purposeResult = purposeDAO.addPurpose(purposes.get(0));
            Assert.assertEquals(purposeResult.getName(), purposes.get(0).getName());

            Purpose purposeByName = purposeDAO.getPurposeByName(purposeResult.getName(), purposeResult.getTenantId());

            Assert.assertEquals(purposeByName.getId(), purposeResult.getId());
            Assert.assertEquals(purposeByName.getName(), purposeResult.getName());
            Assert.assertEquals(purposeByName.getDescription(), purposeResult.getDescription());
        }
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testGetPurposeByNullName() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);
            purposeDAO.getPurposeByName(null, -1);

            Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
        }
    }

    @Test
    public void testGetPurposeByInvalidName() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);

            Purpose purposeByName = purposeDAO.getPurposeByName("InvalidName", -1);

            Assert.assertNull(purposeByName, "Result should be null for invalid purpose name.");
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testGetPurposeByNameWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);
            purposeDAO.getPurposeByName("Invalid Purpose", -1);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test(dataProvider = "purposeListProvider")
    public void testListPurposes(int limit, int offset, int teanantId, int resultSize) throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);

            Purpose purposeResult1 = purposeDAO.addPurpose(purposes.get(0));
            Assert.assertEquals(purposeResult1.getName(), purposes.get(0).getName());

            Purpose purposeResult2 = purposeDAO.addPurpose(purposes.get(1));
            Assert.assertEquals(purposeResult2.getName(), purposes.get(1).getName());

            List<Purpose> purposeList = purposeDAO.listPurposes(limit, offset, teanantId);

            Assert.assertEquals(purposeList.size(), resultSize);

            if (resultSize == 1) {
                Assert.assertEquals(purposeList.get(0).getName(), purposes.get(offset).getName());
            }
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testListPurposesWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);
            purposeDAO.listPurposes(0, 0, 0);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test
    public void testDeletePurpose() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);
            Purpose purposeResult = purposeDAO.addPurpose(purposes.get(0));
            Assert.assertEquals(purposeResult.getName(), purposes.get(0).getName());

            int id = purposeDAO.deletePurpose(purposeResult.getId());

            Assert.assertEquals(new Integer(id), purposeResult.getId());
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testDeletePurposeWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            PurposeDAO purposeDAO = new PurposeDAOImpl(jdbcTemplate);
            purposeDAO.deletePurpose(0);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }
}
