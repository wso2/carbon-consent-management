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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;
import org.wso2.carbon.consent.mgt.core.util.FilterSqlBuilder;
import org.wso2.carbon.consent.mgt.core.util.JdbcUtils;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.OperationNode;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.FilterConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PURPOSES_BY_TENANT_ID;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PURPOSE_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PURPOSE_VERSION_PROPERTIES;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PURPOSE_VERSION_PII_CAT_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PURPOSE_VERSION_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_BY_ID_WITH_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_BY_NAME_WITH_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_BY_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_PII_CAT_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_VERSION_BY_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_VERSION_BY_VERSION_LABEL_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_VERSION_PROPERTIES;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_VERSION_PII_CAT_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_RECEIPT_COUNT_ASSOCIATED_WITH_PURPOSE;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_RECEIPT_COUNT_ASSOCIATED_WITH_PURPOSE_VERSION;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_PII_CATEGORY_WITH_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_PURPOSE_WITH_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_PURPOSE_VERSION_PROPERTY;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_PURPOSE_VERSION_PII_CAT_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_PURPOSE_VERSION_WITH_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.INSERT_RECEIPT_PURPOSE_PII_ASSOC_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_DB2_WITH_UUID;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_INFORMIX_WITH_UUID;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_MSSQL_WITH_UUID;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_MYSQL_WITH_UUID;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PAGINATED_PURPOSE_ORACLE_WITH_UUID;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.LIST_PURPOSE_VERSIONS_WITH_UUID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PURPOSE_PII_CAT_ASSOC_BY_PURPOSE_ID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.UPDATE_PURPOSE_LATEST_VERSION_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PURPOSE_VERSION_PII_CAT_ASSOC_BY_PURPOSE_ID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.DELETE_PURPOSE_VERSIONS_BY_PURPOSE_ID_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.UPDATE_PURPOSE_DESCRIPTION_SQL;
import static org.wso2.carbon.consent.mgt.core.constant.SQLConstants.GET_PURPOSE_VERSION_DESCRIPTION_SQL;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isDB2DB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isH2MySqlOrPostgresDB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isInformixDB;
import static org.wso2.carbon.consent.mgt.core.util.JdbcUtils.isMSSqlDB;
import static org.wso2.carbon.consent.mgt.core.util.LambdaExceptionUtils.rethrowConsumer;

/**
 * Default implementation of {@link PurposeDAO}. This handles {@link Purpose} related DB operations.
 */
public class PurposeDAOImpl implements PurposeDAO {

    private static final String SQL_FILTER_STRING_ANY = "%";
    private static final String QUERY_FILTER_STRING_ANY = "*";
    private static final String QUERY_FILTER_STRING_ANY_ESCAPED = "\\*";

    public PurposeDAOImpl() {

    }

    @Override
    public int getPriority() {

        return 1;
    }

    @Override
    public Purpose addPurpose(Purpose purpose) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        final int[] insertedIdHolder = new int[1];
        String uuid = UUID.randomUUID().toString();

        try {
            jdbcTemplate.withTransaction(template -> {
                insertedIdHolder[0] = template.executeInsert(INSERT_PURPOSE_WITH_UUID_SQL, (preparedStatement -> {
                    preparedStatement.setString(1, purpose.getName());
                    preparedStatement.setString(2, purpose.getDescription());
                    preparedStatement.setString(3, purpose.getGroup());
                    preparedStatement.setString(4, purpose.getGroupType());
                    preparedStatement.setInt(5, purpose.getTenantId());
                    preparedStatement.setString(6, uuid);
                }), purpose, true);

                purpose.getPurposePIICategories().forEach(rethrowConsumer(piiCategory -> {
                    try {
                        template.executeInsert(INSERT_RECEIPT_PURPOSE_PII_ASSOC_SQL, (preparedStatement -> {
                            preparedStatement.setInt(1, insertedIdHolder[0]);
                            preparedStatement.setInt(2, piiCategory.getId());
                            preparedStatement.setInt(3, piiCategory.getMandatory() ? 1 : 0);
                        }), piiCategory, false);
                    } catch (DataAccessException e) {
                        throw ConsentUtils.handleServerException(ErrorMessages
                                .ERROR_CODE_ADD_PURPOSE_PII_ASSOC, String.valueOf(insertedIdHolder[0]), e);
                    }
                }));

                return null;
            });
        } catch (Exception e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_PURPOSE, purpose.getName(), e);
        }

        Purpose purposeResult = new Purpose(insertedIdHolder[0], purpose.getName(), purpose.getDescription(),
                purpose.getGroup(), purpose.getGroupType(), purpose.getTenantId(), purpose
                .getPurposePIICategories(), uuid);
        return purposeResult;
    }

    @Override
    public Purpose getPurposeById(int id) throws ConsentManagementException {

        if (id == 0) {
            throw ConsentUtils.handleClientException(ErrorMessages.ERROR_CODE_PURPOSE_ID_REQUIRED, null);
        }

        Purpose purpose;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            purpose = jdbcTemplate.fetchSingleRecord(GET_PURPOSE_BY_ID_WITH_UUID_SQL, (resultSet, rowNumber) -> {
                Purpose p = new Purpose(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3),
                        resultSet.getString(4), resultSet.getString(5), resultSet.getInt(6), resultSet.getString(7));
                p.setLatestVersionId(resultSet.getString(8));
                return p;
            }, preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PURPOSE_BY_ID, String.valueOf(id), e);
        }

        if (purpose != null) {
            try {
                List<PurposePIICategory> piiCategories = new ArrayList<>();
                jdbcTemplate.executeQuery(GET_PURPOSE_PII_CAT_SQL, (resultSet, rowNumber) ->
                                piiCategories.add(new PurposePIICategory(
                                        resultSet.getInt(1),
                                        resultSet.getInt(2) == 1)),
                        preparedStatement -> preparedStatement.setInt(1, purpose.getId()));
                purpose.setPurposePIICategories(piiCategories);
            } catch (DataAccessException e) {
                throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PURPOSE_BY_ID, String.valueOf(id), e);
            }
        }
        return purpose;
    }

    @Override
    public Purpose getPurposeByName(String name, String group, String groupType, int tenantId) throws
            ConsentManagementException {

        if (StringUtils.isBlank(name)) {
            throw ConsentUtils.handleClientException(ErrorMessages.ERROR_CODE_PURPOSE_NAME_REQUIRED, null);
        }

        if (StringUtils.isBlank(group)) {
            throw ConsentUtils.handleClientException(ErrorMessages.ERROR_CODE_PURPOSE_GROUP_REQUIRED, null);
        }

        if (StringUtils.isBlank(groupType)) {
            throw ConsentUtils.handleClientException(ErrorMessages.ERROR_CODE_PURPOSE_GROUP_TYPE_REQUIRED, null);
        }

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        Purpose purpose;

        try {
            purpose = jdbcTemplate.fetchSingleRecord(GET_PURPOSE_BY_NAME_WITH_UUID_SQL,
                    (resultSet, rowNumber) -> {
                        Purpose p = new Purpose(resultSet.getInt(1), resultSet.getString(2),
                                resultSet.getString(3), resultSet.getString(4),
                                resultSet.getString(5), resultSet.getInt(6), resultSet.getString(7));
                        p.setLatestVersionId(resultSet.getString(8));
                        return p;
                    },
                    preparedStatement -> {
                        preparedStatement.setString(1, name);
                        preparedStatement.setString(2, group);
                        preparedStatement.setString(3, groupType);
                        preparedStatement.setInt(4, tenantId);
                    });
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PURPOSE_BY_NAME, name, e);
        }
        return purpose;
    }

    @Override
    public List<Purpose> listPurposes(int limit, int offset, int tenantId) throws ConsentManagementException {

        return listPurposes(QUERY_FILTER_STRING_ANY, QUERY_FILTER_STRING_ANY, limit, offset, tenantId);
    }

    @Override
    public List<Purpose> listPurposes(String group, String groupType, int limit, int offset, int tenantId) throws
            ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        List<Purpose> purposes;
        try {

            if (StringUtils.isEmpty(group)) {
                group = SQL_FILTER_STRING_ANY;
            } else if (group.contains(QUERY_FILTER_STRING_ANY)) {
                group = group.replaceAll(QUERY_FILTER_STRING_ANY_ESCAPED, SQL_FILTER_STRING_ANY);
            }

            if (StringUtils.isEmpty(groupType)) {
                groupType = SQL_FILTER_STRING_ANY;
            } else if (groupType.contains(QUERY_FILTER_STRING_ANY)) {
                groupType = groupType.replaceAll(QUERY_FILTER_STRING_ANY_ESCAPED, SQL_FILTER_STRING_ANY);
            }

            String query;
            if (isH2MySqlOrPostgresDB()) {
                query = LIST_PAGINATED_PURPOSE_MYSQL_WITH_UUID;
            } else if (isDB2DB()) {
                query = LIST_PAGINATED_PURPOSE_DB2_WITH_UUID;
                int initialOffset = offset;
                offset = offset + limit;
                limit = initialOffset + 1;
            } else if (isMSSqlDB()) {
                int initialOffset = offset;
                offset = limit + offset;
                limit = initialOffset + 1;
                query = LIST_PAGINATED_PURPOSE_MSSQL_WITH_UUID;
            } else if (isInformixDB()) {
                query = LIST_PAGINATED_PURPOSE_INFORMIX_WITH_UUID;
            } else {
                //oracle
                query = LIST_PAGINATED_PURPOSE_ORACLE_WITH_UUID;
                limit = offset + limit;
            }
            int finalLimit = limit;
            int finalOffset = offset;
            String finalGroup = group;
            String finalGroupType = groupType;

            purposes = jdbcTemplate.executeQuery(query,
                    (resultSet, rowNumber) -> new Purpose(resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            resultSet.getString(5),
                            resultSet.getInt(6),
                            resultSet.getString(7)),
                    preparedStatement -> {
                        preparedStatement.setInt(1, tenantId);
                        preparedStatement.setString(2, finalGroup);
                        preparedStatement.setString(3, finalGroupType);
                        preparedStatement.setInt(4, finalLimit);
                        preparedStatement.setInt(5, finalOffset);
                    });
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException(String.format(ErrorMessages.ERROR_CODE_LIST_PURPOSE.getMessage(),
                    group, groupType, limit, offset), ErrorMessages.ERROR_CODE_LIST_PURPOSE.getCode(), e);
        }
        return purposes;
    }

    @Override
    public List<Purpose> listPurposes(Node filterTree, int limit, int offset, int tenantId)
            throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        List<Purpose> purposes;
        try {
            // Map filter attributes to database column names before building SQL
            if (filterTree != null) {
                filterTree = mapFilterAttributes(filterTree);
            }

            // Build filter WHERE clause if filterTree provided
            List<Object> filterParams = new ArrayList<>();
            String filterWhereClause = "";
            if (filterTree != null) {
                FilterSqlBuilder filterSqlBuilder = new FilterSqlBuilder();
                String builtClause = filterSqlBuilder.buildWhereClause(filterTree, filterParams);
                if (builtClause != null && !builtClause.isEmpty()) {
                    filterWhereClause = " AND " + builtClause;
                }
            }

            // Determine database type and select appropriate query
            String query;
            int finalLimit = limit;
            int finalOffset = offset;

            if (isH2MySqlOrPostgresDB()) {
                query = "SELECT ID, NAME, DESCRIPTION, PURPOSE_GROUP, GROUP_TYPE, TENANT_ID, UUID, " +
                        "LATEST_VERSION_ID FROM CM_PURPOSE WHERE TENANT_ID = ?" + filterWhereClause +
                        " ORDER BY ID ASC LIMIT ? OFFSET ?";
            } else if (isDB2DB()) {
                finalOffset = offset + limit;
                finalLimit = offset + 1;
                query = "SELECT ID, NAME, DESCRIPTION, PURPOSE_GROUP, GROUP_TYPE, TENANT_ID, UUID, " +
                        "LATEST_VERSION_ID FROM (SELECT ROW_NUMBER() OVER (ORDER BY ID) AS rn, p.* " +
                        "FROM CM_PURPOSE AS p) WHERE TENANT_ID = ?" + filterWhereClause +
                        " AND rn BETWEEN ? AND ?";
            } else if (isMSSqlDB()) {
                finalOffset = limit + offset;
                finalLimit = offset + 1;
                query = "SELECT ID, NAME, DESCRIPTION, PURPOSE_GROUP, GROUP_TYPE, TENANT_ID, UUID, " +
                        "LATEST_VERSION_ID FROM (SELECT ID, NAME, DESCRIPTION, PURPOSE_GROUP, " +
                        "GROUP_TYPE, TENANT_ID, UUID, LATEST_VERSION_ID, ROW_NUMBER() OVER (ORDER BY ID) " +
                        "AS RowNum FROM CM_PURPOSE) AS P WHERE P.TENANT_ID = ?" + filterWhereClause +
                        " AND P.RowNum BETWEEN ? AND ?";
            } else if (isInformixDB()) {
                // Informix
                throw new ConsentManagementServerException("This method is not supported for Informix database.",
                        ErrorMessages.ERROR_CODE_DATABASE_QUERY_PERFORMING.getCode());
            } else {
                // Oracle
                finalLimit = offset + limit;
                query = "SELECT ID, NAME, DESCRIPTION, PURPOSE_GROUP, GROUP_TYPE, TENANT_ID, UUID, " +
                        "LATEST_VERSION_ID FROM (SELECT ROW_NUMBER() OVER (ORDER BY ID) AS RN, ID, NAME, " +
                        "DESCRIPTION, PURPOSE_GROUP, GROUP_TYPE, TENANT_ID, UUID, LATEST_VERSION_ID " +
                        "FROM CM_PURPOSE WHERE TENANT_ID = ?" + filterWhereClause +
                        ") WHERE RN <= ? AND RN > ?";
            }

            final int paramLimit = finalLimit;
            final int paramOffset = finalOffset;
            final List<Object> finalFilterParams = filterParams;

            purposes = jdbcTemplate.executeQuery(query,
                    (resultSet, rowNumber) -> {
                        Purpose p = new Purpose(resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getString(4),
                                resultSet.getString(5),
                                resultSet.getInt(6),
                                resultSet.getString(7));
                        p.setLatestVersionId(resultSet.getString(8));
                        return p;
                    },
                    preparedStatement -> {
                        int paramIndex = 1;
                        preparedStatement.setInt(paramIndex++, tenantId);

                        // Set filter parameters
                        for (Object filterParam : finalFilterParams) {
                            preparedStatement.setObject(paramIndex++, filterParam);
                        }

                        preparedStatement.setInt(paramIndex++, paramLimit);
                        preparedStatement.setInt(paramIndex, paramOffset);
                    });
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException(
                    String.format("Error listing purposes with filter for tenant %d", tenantId),
                    ErrorMessages.ERROR_CODE_LIST_PURPOSE.getCode(), e);
        }
        return purposes;
    }

    @Override
    public int deletePurpose(int id) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(DELETE_PURPOSE_PII_CAT_ASSOC_BY_PURPOSE_ID_SQL,
                        preparedStatement -> preparedStatement.setInt(1, id));
                template.executeUpdate(DELETE_PURPOSE_SQL,
                        preparedStatement -> preparedStatement.setInt(1, id));
                return null;
            });
        } catch (Exception e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_PURPOSE, String.valueOf(id), e);
        }

        return id;
    }

    @Override
    public void deletePurposeWithVersions(int purposeId) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(UPDATE_PURPOSE_LATEST_VERSION_SQL, preparedStatement -> {
                    preparedStatement.setString(1, null);
                    preparedStatement.setInt(2, purposeId);
                    preparedStatement.setInt(3, ConsentUtils.getTenantIdFromCarbonContext());
                });

                template.executeUpdate(DELETE_PURPOSE_VERSION_PII_CAT_ASSOC_BY_PURPOSE_ID_SQL,
                        preparedStatement -> preparedStatement.setInt(1, purposeId));

                template.executeUpdate(DELETE_PURPOSE_VERSIONS_BY_PURPOSE_ID_SQL,
                        preparedStatement -> preparedStatement.setInt(1, purposeId));

                template.executeUpdate(DELETE_PURPOSE_PII_CAT_ASSOC_BY_PURPOSE_ID_SQL,
                        preparedStatement -> preparedStatement.setInt(1, purposeId));

                template.executeUpdate(DELETE_PURPOSE_SQL,
                        preparedStatement -> preparedStatement.setInt(1, purposeId));
                return null;
            });
        } catch (Exception e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_PURPOSE,
                    String.valueOf(purposeId), e);
        }
    }

    /**
     * Delete all {@link Purpose} of a given tenant id.
     *
     * @param tenantId Id of the tenant
     * @throws ConsentManagementException
     */
    @Override
    public void deletePurposesByTenantId(int tenantId) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_PURPOSES_BY_TENANT_ID,
                    preparedStatement -> preparedStatement.setInt(1, tenantId));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_PURPOSES_BY_TENANT_ID,
                    String.valueOf(tenantId), e);
        }
    }

    /**
     * Add a new version for a {@link Purpose}.
     *
     * @param purposeVersion {@link PurposeVersion} to insert.
     * @return Inserted {@link PurposeVersion}.
     * @throws ConsentManagementException If error occurs while adding the {@link PurposeVersion}.
     */
    @Override
    public PurposeVersion addPurposeVersion(PurposeVersion purposeVersion, boolean setAsLatest)
            throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        String versionUuid = UUID.randomUUID().toString();

        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeInsert(INSERT_PURPOSE_VERSION_WITH_UUID_SQL, (preparedStatement -> {
                    preparedStatement.setInt(1, purposeVersion.getPurposeId());
                    preparedStatement.setString(2, purposeVersion.getVersion());
                    preparedStatement.setString(3, purposeVersion.getDescription());
                    preparedStatement.setInt(4, purposeVersion.getTenantId());
                    preparedStatement.setString(5, versionUuid);
                }), purposeVersion, false);

                purposeVersion.getPurposePIICategories().forEach(rethrowConsumer(piiCategory -> {
                    try {
                        template.executeInsert(INSERT_PURPOSE_VERSION_PII_CAT_ASSOC_SQL, (preparedStatement -> {
                            preparedStatement.setString(1, versionUuid);
                            preparedStatement.setInt(2, piiCategory.getId());
                            preparedStatement.setInt(3, piiCategory.getMandatory() ? 1 : 0);
                        }), piiCategory, false);
                    } catch (DataAccessException e) {
                        throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_PURPOSE_VERSION,
                                versionUuid, e);
                    }
                }));

                Map<String, String> properties = purposeVersion.getProperties();
                if (properties != null && !properties.isEmpty()) {
                    properties.entrySet().forEach(rethrowConsumer(entry -> {
                        try {
                            template.executeInsert(INSERT_PURPOSE_VERSION_PROPERTY, (preparedStatement -> {
                                preparedStatement.setString(1, versionUuid);
                                preparedStatement.setString(2, entry.getKey());
                                preparedStatement.setString(3, entry.getValue());
                                preparedStatement.setInt(4, purposeVersion.getTenantId());
                            }), null, false);
                        } catch (DataAccessException e) {
                            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_PURPOSE_VERSION,
                                    versionUuid, e);
                        }
                    }));
                }

                if (setAsLatest) {
                    try {
                        // Sync purpose PII categories to match this version.
                        template.executeUpdate(DELETE_PURPOSE_PII_CAT_ASSOC_BY_PURPOSE_ID_SQL,
                                preparedStatement -> preparedStatement.setInt(1, purposeVersion.getPurposeId()));
                        for (PurposePIICategory cat : purposeVersion.getPurposePIICategories()) {
                            template.executeInsert(INSERT_RECEIPT_PURPOSE_PII_ASSOC_SQL, preparedStatement -> {
                                preparedStatement.setInt(1, purposeVersion.getPurposeId());
                                preparedStatement.setInt(2, cat.getId());
                                preparedStatement.setInt(3, cat.getMandatory() ? 1 : 0);
                            }, cat, false);
                        }
                        // Sync purpose description to match this version.
                        template.executeUpdate(UPDATE_PURPOSE_DESCRIPTION_SQL, preparedStatement -> {
                            preparedStatement.setString(1, purposeVersion.getDescription());
                            preparedStatement.setInt(2, purposeVersion.getPurposeId());
                            preparedStatement.setInt(3, purposeVersion.getTenantId());
                        });
                        // Update latest version pointer.
                        template.executeUpdate(UPDATE_PURPOSE_LATEST_VERSION_SQL, preparedStatement -> {
                            preparedStatement.setString(1, versionUuid);
                            preparedStatement.setInt(2, purposeVersion.getPurposeId());
                            preparedStatement.setInt(3, purposeVersion.getTenantId());
                        });
                    } catch (DataAccessException e) {
                        throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_PURPOSE_VERSION,
                                versionUuid, e);
                    }
                }

                return null;
            });
        } catch (Exception e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_ADD_PURPOSE_VERSION,
                    String.valueOf(purposeVersion.getPurposeId()), e);
        }

        PurposeVersion result = new PurposeVersion(purposeVersion.getPurposeId(),
                purposeVersion.getVersion(), purposeVersion.getDescription(), purposeVersion.getTenantId(), versionUuid);
        result.setPurposePIICategories(purposeVersion.getPurposePIICategories());
        result.setProperties(purposeVersion.getProperties());
        return result;
    }

    /**
     * List all versions for a given purpose UUID.
     *
     * @param uuid UUID of the {@link Purpose}.
     * @return List of {@link PurposeVersion} entries.
     * @throws ConsentManagementException If error occurs while listing {@link PurposeVersion}.
     */
    @Override
    public List<PurposeVersion> listPurposeVersions(String uuid) throws ConsentManagementException {

        List<PurposeVersion> versions;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            versions = jdbcTemplate.executeQuery(LIST_PURPOSE_VERSIONS_WITH_UUID_SQL,
                    (resultSet, rowNumber) -> {
                        PurposeVersion pv = new PurposeVersion(
                                resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getInt(4),
                                resultSet.getString(5));
                        return pv;
                    },
                    preparedStatement -> preparedStatement.setString(1, uuid));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_GET_PURPOSE_VERSION_LIST, uuid, e);
        }

        for (PurposeVersion version : versions) {
            try {
                List<PurposePIICategory> piiCategories = new ArrayList<>();
                jdbcTemplate.executeQuery(GET_PURPOSE_VERSION_PII_CAT_SQL, (resultSet, rowNumber) -> {
                    PIICategory piiCategory = new PIICategory(
                            resultSet.getInt(1),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            resultSet.getInt(5) == 1,
                            resultSet.getInt(6),
                            resultSet.getString(7),
                            resultSet.getString(8));
                    piiCategories.add(new PurposePIICategory(piiCategory, resultSet.getInt(2) == 1));
                    return null;
                }, preparedStatement -> preparedStatement.setString(1, version.getUuid()));
                version.setPurposePIICategories(piiCategories);
            } catch (DataAccessException e) {
                throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_GET_PURPOSE_VERSION_LIST, uuid, e);
            }
        }
        return versions;
    }

    /**
     * Delete a {@link PurposeVersion} by its UUID.
     *
     * @param versionUuid UUID of the {@link PurposeVersion} to delete.
     * @throws ConsentManagementException If error occurs while deleting the {@link PurposeVersion}.
     */
    @Override
    public void deletePurposeVersion(String versionUuid) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
            template.executeUpdate(DELETE_PURPOSE_VERSION_PROPERTIES,
                preparedStatement -> preparedStatement.setString(1, versionUuid));
                template.executeUpdate(DELETE_PURPOSE_VERSION_PII_CAT_ASSOC_SQL,
                        preparedStatement -> preparedStatement.setString(1, versionUuid));
                template.executeUpdate(DELETE_PURPOSE_VERSION_SQL,
                        preparedStatement -> preparedStatement.setString(1, versionUuid));
                return null;
            });
        } catch (Exception e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_DELETE_PURPOSE_VERSION,
                    versionUuid, e);
        }
    }

    /**
     * Check whether the {@link Purpose} by ID is used in a receipt
     *
     * @param uuid ID of the {@link Purpose} to be validated
     * @return true if purpose is used, false otherwise.
     */
    @Override
    public Purpose getPurposeByUuid(String uuid, int tenantId) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        Purpose purpose;
        try {
            purpose = jdbcTemplate.fetchSingleRecord(GET_PURPOSE_BY_UUID_SQL, (resultSet, rowNumber) -> {
                Purpose p = new Purpose(resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getString(5),
                        resultSet.getInt(6),
                        resultSet.getString(7));
                p.setLatestVersionId(resultSet.getString(8));
                return p;
            }, preparedStatement -> {
                preparedStatement.setString(1, uuid);
                preparedStatement.setInt(2, tenantId);
            });
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PURPOSE_BY_ID, uuid, e);
        }

        if (purpose != null) {
            try {
                List<PurposePIICategory> piiCategories = new ArrayList<>();
                jdbcTemplate.executeQuery(GET_PURPOSE_PII_CAT_SQL, (resultSet, rowNumber) ->
                                piiCategories.add(new PurposePIICategory(
                                        resultSet.getInt(1),
                                        resultSet.getInt(2) == 1)),
                        preparedStatement -> preparedStatement.setInt(1, purpose.getId()));
                purpose.setPurposePIICategories(piiCategories);
            } catch (DataAccessException e) {
                throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_SELECT_PURPOSE_BY_ID, uuid, e);
            }
        }
        return purpose;
    }

    @Override
    public PurposeVersion getPurposeVersionByUuid(String uuid) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        PurposeVersion version;
        try {
            version = jdbcTemplate.fetchSingleRecord(GET_PURPOSE_VERSION_BY_UUID_SQL, (resultSet, rowNumber) -> {
                PurposeVersion pv = new PurposeVersion(resultSet.getInt(1),
                        resultSet.getString(2), resultSet.getString(3), resultSet.getInt(4), resultSet.getString(5));
                return pv;
            }, preparedStatement -> preparedStatement.setString(1, uuid));
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_GET_PURPOSE_VERSION, uuid, e);
        }

        if (version != null) {
            try {
                List<PurposePIICategory> piiCategories = new ArrayList<>();
                jdbcTemplate.executeQuery(GET_PURPOSE_VERSION_PII_CAT_SQL, (resultSet, rowNumber) -> {
                    PIICategory piiCategory = new PIICategory(
                            resultSet.getInt(1),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            resultSet.getInt(5) == 1,
                            resultSet.getInt(6),
                            resultSet.getString(7),
                            resultSet.getString(8));
                    piiCategories.add(new PurposePIICategory(piiCategory, resultSet.getInt(2) == 1));
                    return null;
                }, preparedStatement -> preparedStatement.setString(1, version.getUuid()));
                version.setPurposePIICategories(piiCategories);
            } catch (DataAccessException e) {
                throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_GET_PURPOSE_VERSION, uuid, e);
            }

            try {
                Map<String, String> properties = new HashMap<>();
                jdbcTemplate.executeQuery(GET_PURPOSE_VERSION_PROPERTIES, (resultSet, rowNumber) -> {
                    properties.put(resultSet.getString(1), resultSet.getString(2));
                    return null;
                }, preparedStatement -> preparedStatement.setString(1, version.getUuid()));
                version.setProperties(properties);
            } catch (DataAccessException e) {
                throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_GET_PURPOSE_VERSION, uuid, e);
            }
        }
        return version;
    }

    @Override
    public PurposeVersion getPurposeVersionByLabel(int purposeId, String version, int tenantId)
            throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        PurposeVersion purposeVersion;
        try {
            purposeVersion = jdbcTemplate.fetchSingleRecord(GET_PURPOSE_VERSION_BY_VERSION_LABEL_SQL,
                    (resultSet, rowNumber) -> {
                        PurposeVersion pv = new PurposeVersion(resultSet.getInt(1),
                                resultSet.getString(2), resultSet.getString(3), resultSet.getInt(4),
                                resultSet.getString(5));
                        return pv;
                    }, preparedStatement -> {
                        preparedStatement.setInt(1, purposeId);
                        preparedStatement.setString(2, version);
                        preparedStatement.setInt(3, tenantId);
                    });
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_GET_PURPOSE_VERSION_LIST,
                    String.valueOf(purposeId), e);
        }
        return purposeVersion;
    }

    @Override
    public void updateLatestVersionId(int purposeId, String versionUuid, int tenantId)
            throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                // Fetch version's PII categories and description inside the transaction.
                List<PurposePIICategory> piiCategories = new ArrayList<>();
                template.executeQuery(GET_PURPOSE_VERSION_PII_CAT_SQL, (resultSet, rowNumber) -> {
                    piiCategories.add(new PurposePIICategory(resultSet.getInt(1), resultSet.getInt(2) == 1));
                    return null;
                }, preparedStatement -> preparedStatement.setString(1, versionUuid));

                String[] descriptionHolder = {null};
                template.executeQuery(GET_PURPOSE_VERSION_DESCRIPTION_SQL, (resultSet, rowNumber) -> {
                    descriptionHolder[0] = resultSet.getString(1);
                    return null;
                }, preparedStatement -> preparedStatement.setString(1, versionUuid));

                // Replace purpose's PII category associations with the version's.
                template.executeUpdate(DELETE_PURPOSE_PII_CAT_ASSOC_BY_PURPOSE_ID_SQL,
                        preparedStatement -> preparedStatement.setInt(1, purposeId));

                for (PurposePIICategory cat : piiCategories) {
                    try {
                        template.executeInsert(INSERT_RECEIPT_PURPOSE_PII_ASSOC_SQL, preparedStatement -> {
                            preparedStatement.setInt(1, purposeId);
                            preparedStatement.setInt(2, cat.getId());
                            preparedStatement.setInt(3, cat.getMandatory() ? 1 : 0);
                        }, cat, false);
                    } catch (DataAccessException e) {
                        throw ConsentUtils.handleServerException(
                                ErrorMessages.ERROR_CODE_ADD_PURPOSE_PII_ASSOC, String.valueOf(purposeId), e);
                    }
                }

                // Sync purpose description to match the promoted version.
                template.executeUpdate(UPDATE_PURPOSE_DESCRIPTION_SQL, preparedStatement -> {
                    preparedStatement.setString(1, descriptionHolder[0]);
                    preparedStatement.setInt(2, purposeId);
                    preparedStatement.setInt(3, tenantId);
                });

                // Update the latest version pointer.
                template.executeUpdate(UPDATE_PURPOSE_LATEST_VERSION_SQL, preparedStatement -> {
                    preparedStatement.setString(1, versionUuid);
                    preparedStatement.setInt(2, purposeId);
                    preparedStatement.setInt(3, tenantId);
                });
                return null;
            });
        } catch (Exception e) {
            throw ConsentUtils.handleServerException(ErrorMessages.ERROR_CODE_GET_PURPOSE_VERSION_LIST,
                    String.valueOf(purposeId), e);
        }
    }

    @Override
    public boolean isPurposeUsed(int id) throws ConsentManagementServerException {

        Integer count;
        try {
            JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
            count = jdbcTemplate.fetchSingleRecord(GET_RECEIPT_COUNT_ASSOCIATED_WITH_PURPOSE, (resultSet, rowNumber) ->
                            resultSet.getInt(1),
                    preparedStatement -> preparedStatement.setInt(1, id));
            if (count == null) {
                return false;
            }
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages
                    .ERROR_CODE_RETRIEVE_RECEIPTS_ASSOCIATED_WITH_PURPOSE, String.valueOf(id), e);
        }
        return (count > 0);
    }

    @Override
    public boolean isPurposeVersionUsed(String versionUuid) throws ConsentManagementServerException {

        Integer count;
        try {
            JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
            count = jdbcTemplate.fetchSingleRecord(GET_RECEIPT_COUNT_ASSOCIATED_WITH_PURPOSE_VERSION, (resultSet, rowNumber) ->
                            resultSet.getInt(1),
                    preparedStatement -> preparedStatement.setString(1, versionUuid));
            if (count == null) {
                return false;
            }
        } catch (DataAccessException e) {
            throw ConsentUtils.handleServerException(ErrorMessages
                    .ERROR_CODE_RETRIEVE_RECEIPTS_ASSOCIATED_WITH_PURPOSE, versionUuid, e);
        }
        return (count > 0);
    }

    /**
     * Maps filter attribute names to database column names.
     * Recursively traverses the filter tree and replaces known attribute name mappings.
     * This is necessary because the API filter attributes (e.g., "type") may differ
     * from the actual database column names (e.g., "GROUP_TYPE").
     *
     * @param node The filter tree node (can be ExpressionNode or OperationNode)
     * @return The same node with attribute names mapped to database column names
     */
    private Node mapFilterAttributes(Node node) {

        if (node == null) {
            return null;
        }

        if (node instanceof ExpressionNode) {
            ExpressionNode exprNode = (ExpressionNode) node;
            String attribute = exprNode.getAttributeValue();

            // Map filter attribute names to database column names
            if (FilterConstants.FILTER_ATTR_NAME.equalsIgnoreCase(attribute)) {
                exprNode.setAttributeValue(FilterConstants.DB_COL_NAME);
            } else if (FilterConstants.FILTER_ATTR_TYPE.equalsIgnoreCase(attribute)) {
                exprNode.setAttributeValue(FilterConstants.DB_COL_GROUP_TYPE);
            }
            return exprNode;
        } else if (node instanceof OperationNode) {
            OperationNode opNode = (OperationNode) node;
            mapFilterAttributes(opNode.getLeftNode());
            mapFilterAttributes(opNode.getRightNode());
            return opNode;
        }
        return node;
    }
}
