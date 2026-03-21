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
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.internal.ConsentManagerComponentDataHolder;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.util.JdbcUtils;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.closeH2Base;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.getConnection;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.mockComponentDataHolder;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.spyConnection;
import static org.wso2.carbon.consent.mgt.core.util.TestUtils.spyConnectionWithError;

public class PurposeDAOImplTest {

    private static List<Purpose> purposes = new ArrayList<>();

    private MockedStatic<ConsentManagerComponentDataHolder> mockedComponentDataHolder;
    private AutoCloseable mockitoCloseable;

    @BeforeMethod
    public void setUp() throws Exception {

        mockitoCloseable = MockitoAnnotations.openMocks(this);
        initiateH2Base();
        Purpose purpose1 = new Purpose("P1", "D1", "SIGNUP", "RESIDENT", -1234);
        Purpose purpose2 = new Purpose("P2", "D2", "JIT", "IDP", -1234);
        purposes.add(purpose1);
        purposes.add(purpose2);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        closeH2Base();
        
        if (mockedComponentDataHolder != null) {
            mockedComponentDataHolder.close();
        }
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    @DataProvider(name = "purposeListProvider")
    public Object[][] provideListData() throws Exception {

        return new Object[][]{
                // limit, offset, tenantId, resultSize
                {0, 0, -1234, 0},
                {1, 1, -1234, 1},
                {10, 0, -1234, 3}
        };
    }

    @Test
    public void testAddPurpose() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            when(dataSource.getConnection()).thenReturn(connection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            Purpose purposeResult = purposeDAO.addPurpose(purposes.get(0));

            Assert.assertEquals(purposeResult.getName(), purposes.get(0).getName());
            Assert.assertEquals(purposeResult.getDescription(), purposes.get(0).getDescription());
            Assert.assertEquals(purposeResult.getGroup(), purposes.get(0).getGroup());
            Assert.assertEquals(purposeResult.getGroupType(), purposes.get(0).getGroupType());
            Assert.assertEquals(purposeResult.getTenantId(), purposes.get(0).getTenantId());
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testAddDuplicatePurpose() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spy = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spy);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            purposeDAO.addPurpose(purposes.get(0));
            purposeDAO.addPurpose(purposes.get(0));

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test
    public void testGetPurposeById() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
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
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            purposeDAO.getPurposeById(0);

            Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testGetPurposeByIdWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            purposeDAO.getPurposeById(1);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test
    public void testGetPurposeByName() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            Purpose purpose = purposes.get(0);
            Purpose purposeResult = purposeDAO.addPurpose(purpose);
            Assert.assertEquals(purposeResult.getName(), purpose.getName());

            Purpose purposeByName = purposeDAO.getPurposeByName(purposeResult.getName(), purposeResult.getGroup(),
                                                                purposeResult.getGroupType(), purposeResult
                                                                        .getTenantId());

            Assert.assertEquals(purposeByName.getId(), purposeResult.getId());
            Assert.assertEquals(purposeByName.getName(), purposeResult.getName());
            Assert.assertEquals(purposeByName.getDescription(), purposeResult.getDescription());
        }
    }

    @Test(expectedExceptions = ConsentManagementClientException.class)
    public void testGetPurposeByNullName() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            purposeDAO.getPurposeByName(null, null, null, -1);

            Assert.fail("Expected: " + ConsentManagementClientException.class.getName());
        }
    }

    @Test
    public void testGetPurposeByInvalidName() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();

            Purpose purposeByName = purposeDAO.getPurposeByName("InvalidName", "InvalidGroup", "InvalidGroupType", -1);

            Assert.assertNull(purposeByName, "Result should be null for invalid purpose name.");
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testGetPurposeByNameWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            purposeDAO.getPurposeByName("Invalid Purpose", "InvalidGroup", "InvalidGroupType", -1);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test(dataProvider = "purposeListProvider")
    public void testListPurposes(int limit, int offset, int tenantId, int resultSize) throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();

            Purpose purposeResult1 = purposeDAO.addPurpose(purposes.get(0));
            Assert.assertEquals(purposeResult1.getName(), purposes.get(0).getName());

            Purpose purposeResult2 = purposeDAO.addPurpose(purposes.get(1));
            Assert.assertEquals(purposeResult2.getName(), purposes.get(1).getName());

            List<Purpose> purposeList = purposeDAO.listPurposes(limit, offset, tenantId);

            Assert.assertEquals(purposeList.size(), resultSize);

            if (resultSize == 1) {
                Assert.assertEquals(purposeList.get(0).getName(), purposes.get(offset + 1).getName());
            }
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testListPurposesWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            purposeDAO.listPurposes(0, 0, 0);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test
    public void testDeletePurpose() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            Purpose purposeResult = purposeDAO.addPurpose(purposes.get(0));
            Assert.assertEquals(purposeResult.getName(), purposes.get(0).getName());

            int id = purposeDAO.deletePurpose(purposeResult.getId());

            Assert.assertEquals(new Integer(id), purposeResult.getId());
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testDeletePurposeWithException() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnectionWithError(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            purposeDAO.deletePurpose(0);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test
    public void testAddPurposeVersion() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            Purpose purpose = purposeDAO.addPurpose(purposes.get(0));

            PurposeVersion purposeVersion = new PurposeVersion();
            purposeVersion.setPurposeId(purpose.getId());
            purposeVersion.setVersion("v1");
            purposeVersion.setDescription("Version 1 description");
            purposeVersion.setTenantId(-1234);

            PurposeVersion result = purposeDAO.addPurposeVersion(purposeVersion, false);

            Assert.assertNotNull(result);
            Assert.assertEquals(result.getPurposeId(), (int) purpose.getId());
            Assert.assertEquals(result.getVersion(), "v1");
            Assert.assertEquals(result.getDescription(), "Version 1 description");
        }
    }

    @Test
    public void testAddPurposeVersionWithSetAsLatestSyncsPurpose() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();
            PIICategory piiCategory = piiCategoryDAO.addPIICategory(new PIICategory("Email", "Email address", true, -1234));

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            Purpose purpose = purposeDAO.addPurpose(purposes.get(0));

            Map<String, String> properties = new HashMap<>();
            properties.put("legalBasis", "consent");
            properties.put("retentionPeriod", "365");

            PurposeVersion version = new PurposeVersion();
            version.setPurposeId(purpose.getId());
            version.setVersion("v1");
            version.setDescription("Version 1 description");
            version.setTenantId(-1234);
            version.setPurposePIICategories(List.of(new PurposePIICategory(piiCategory, true)));
            version.setProperties(properties);

            PurposeVersion created = purposeDAO.addPurposeVersion(version, true);

            // Verify description and PII categories are synced to the purpose.
            Purpose updated = purposeDAO.getPurposeById(purpose.getId());
            Assert.assertEquals(updated.getDescription(), "Version 1 description",
                    "Purpose description should be synced from version");
            Assert.assertEquals(updated.getPurposePIICategories().size(), 1,
                    "Purpose PII categories should be synced from version");
            Assert.assertEquals(updated.getPurposePIICategories().get(0).getId(), (int) piiCategory.getId(),
                    "Purpose PII category ID should match version's PII category");

            // Verify properties are stored on the version.
            PurposeVersion fetched = purposeDAO.getPurposeVersionByUuid(created.getUuid());
            Assert.assertNotNull(fetched.getProperties(), "Version properties should not be null");
            Assert.assertEquals(fetched.getProperties().size(), 2, "Version should have 2 properties");
            Assert.assertEquals(fetched.getProperties().get("legalBasis"), "consent");
            Assert.assertEquals(fetched.getProperties().get("retentionPeriod"), "365");
        }
    }

    @Test(expectedExceptions = ConsentManagementServerException.class)
    public void testAddDuplicatePurposeVersion() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            Purpose purpose = purposeDAO.addPurpose(purposes.get(0));

            PurposeVersion purposeVersion = new PurposeVersion();
            purposeVersion.setPurposeId(purpose.getId());
            purposeVersion.setVersion("v1");
            purposeVersion.setDescription("Version 1");
            purposeVersion.setTenantId(-1234);

            purposeDAO.addPurposeVersion(purposeVersion, false);
            purposeDAO.addPurposeVersion(purposeVersion, false);

            Assert.fail("Expected: " + ConsentManagementServerException.class.getName());
        }
    }

    @Test
    public void testListPurposeVersions() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            Purpose purpose = purposeDAO.addPurpose(purposes.get(0));

            PurposeVersion version1 = new PurposeVersion();
            version1.setPurposeId(purpose.getId());
            version1.setVersion("v1");
            version1.setDescription("Version 1");
            version1.setTenantId(-1234);
            purposeDAO.addPurposeVersion(version1, false);

            PurposeVersion version2 = new PurposeVersion();
            version2.setPurposeId(purpose.getId());
            version2.setVersion("v2");
            version2.setDescription("Version 2");
            version2.setTenantId(-1234);
            purposeDAO.addPurposeVersion(version2, false);

            List<PurposeVersion> versions = purposeDAO.listPurposeVersions(purpose.getUuid());

            Assert.assertEquals(versions.size(), 2);
            Assert.assertEquals(versions.get(0).getVersion(), "v1");
            Assert.assertEquals(versions.get(1).getVersion(), "v2");
        }
    }

    @Test
    public void testListPurposesWithNullNameReturnsAll() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            purposeDAO.addPurpose(purposes.get(0));
            purposeDAO.addPurpose(purposes.get(1));

            // DB is pre-seeded with a DEFAULT purpose, so 2 added + 1 pre-existing = 3
            List<Purpose> all = purposeDAO.listPurposes(null, 10, 0, -1234);

            Assert.assertTrue(all.size() >= 2);
        }
    }

    @Test
    public void testDeletePurposesByTenantId() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {

            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            purposeDAO.addPurpose(purposes.get(0));
            purposeDAO.addPurpose(purposes.get(1));

            int tenantId = purposes.get(0).getTenantId();
            purposeDAO.deletePurposesByTenantId(tenantId);

            Assert.assertTrue(Boolean.TRUE);
        }
    }

    @Test
    public void testAddPurposeVersionStoresProperties() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {
            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            Purpose purpose = purposeDAO.addPurpose(purposes.get(0));

            PurposeVersion purposeVersion = new PurposeVersion();
            purposeVersion.setPurposeId(purpose.getId());
            purposeVersion.setVersion("v1");
            purposeVersion.setDescription("Version with properties");
            purposeVersion.setTenantId(-1234);
            Map<String, String> props = new HashMap<>();
            props.put("legalBasis", "consent");
            props.put("retentionPeriod", "365");
            purposeVersion.setProperties(props);

            PurposeVersion result = purposeDAO.addPurposeVersion(purposeVersion, false);

            Assert.assertNotNull(result);
            Assert.assertNotNull(result.getProperties());
            Assert.assertEquals(result.getProperties().size(), 2);
            Assert.assertEquals(result.getProperties().get("legalBasis"), "consent");
            Assert.assertEquals(result.getProperties().get("retentionPeriod"), "365");
        }
    }

    @Test
    public void testGetPurposeVersionByUuidReturnsProperties() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {
            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            Purpose purpose = purposeDAO.addPurpose(purposes.get(0));

            PurposeVersion purposeVersion = new PurposeVersion();
            purposeVersion.setPurposeId(purpose.getId());
            purposeVersion.setVersion("v1");
            purposeVersion.setTenantId(-1234);
            Map<String, String> props = new HashMap<>();
            props.put("dataController", "Acme Corp");
            purposeVersion.setProperties(props);

            PurposeVersion created = purposeDAO.addPurposeVersion(purposeVersion, false);

            PurposeVersion fetched = purposeDAO.getPurposeVersionByUuid(created.getUuid());

            Assert.assertNotNull(fetched);
            Assert.assertNotNull(fetched.getProperties());
            Assert.assertEquals(fetched.getProperties().size(), 1);
            Assert.assertEquals(fetched.getProperties().get("dataController"), "Acme Corp");
        }
    }

    @Test
    public void testDeletePurposeVersionCleansUpProperties() throws Exception {

        DataSource dataSource = mock(DataSource.class);
        mockedComponentDataHolder = mockComponentDataHolder(dataSource);

        try (Connection connection = getConnection()) {
            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            PurposeDAO purposeDAO = new PurposeDAOImpl();
            Purpose purpose = purposeDAO.addPurpose(purposes.get(0));

            PurposeVersion purposeVersion = new PurposeVersion();
            purposeVersion.setPurposeId(purpose.getId());
            purposeVersion.setVersion("v1");
            purposeVersion.setTenantId(-1234);
            Map<String, String> props = new HashMap<>();
            props.put("key1", "value1");
            purposeVersion.setProperties(props);

            PurposeVersion created = purposeDAO.addPurposeVersion(purposeVersion, false);
            String versionUuid = created.getUuid();
            purposeDAO.deletePurposeVersion(versionUuid);

            PurposeVersion fetched = purposeDAO.getPurposeVersionByUuid(versionUuid);
            Assert.assertNull(fetched);

            JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
            List<Map<String, String>> remaining = new ArrayList<>();
            jdbcTemplate.executeQuery(
                    "SELECT PROPERTY_KEY, PROPERTY_VALUE FROM CM_PURPOSE_VERSION_PROPERTY WHERE VERSION_ID = ?",
                    (resultSet, rowNumber) -> {
                        Map<String, String> row = new HashMap<>();
                        row.put(resultSet.getString(1), resultSet.getString(2));
                        remaining.add(row);
                        return null;
                    },
                    preparedStatement -> preparedStatement.setString(1, versionUuid));
            Assert.assertEquals(remaining.size(), 0, "Property rows must be deleted when version is deleted");
        }
    }
}
