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

package org.wso2.carbon.consent.mgt.core.dao;

import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.identity.core.model.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Perform CRUD operations for {@link Purpose}.
 *
 * @since 1.0.0
 */
public interface PurposeDAO {

    /**
     * Returns the priority of the DAO.
     *
     * @return priority of the DAO.
     */
    int getPriority();

    /**
     * Add a {@link Purpose}.
     *
     * @param purpose {@link Purpose} to insert.
     * @return Inserted {@link Purpose}.
     * @throws ConsentManagementException If error occurs while adding the {@link Purpose}.
     */
    Purpose addPurpose(Purpose purpose) throws ConsentManagementException;

    /**
     * Retrieve {@link Purpose} by ID.
     *
     * @param id ID of the {@link Purpose} to retrieve.
     * @return Purpose for the given ID.
     * @throws ConsentManagementException If error occurs while retrieving {@link Purpose}.
     */
    Purpose getPurposeById(int id) throws ConsentManagementException;

    /**
     * Get the {@link Purpose} corresponding to the input name.
     *
     * @param name Name of the {@link Purpose}.
     * @param group Purpose group
     * @param groupType Purpose group type
     * @param tenantId Tenant domain of the {@link Purpose}.
     * @return Purpose for the input criteria.
     * @throws ConsentManagementException If error occurs while retrieving the {@link Purpose}.
     */
    Purpose getPurposeByName(String name, String group, String groupType, int tenantId) throws
            ConsentManagementException;

    /**
     * List {@link Purpose} items for a given search criteria.
     *
     * @param limit Maximum number of results expected.
     * @param offset Result offset.
     * @param tenantId Tenant domain to be searched.
     * @return List of {@link Purpose} entries.
     * @throws ConsentManagementException If error occurs while searching the {@link Purpose}.
     */
    List<Purpose> listPurposes(int limit, int offset, int tenantId) throws ConsentManagementException;

    /**
     * List {@link Purpose} items for a given search criteria.
     *
     * @param group Purpose group
     * @param groupType Purpose group type
     * @param limit Maximum number of results expected.
     * @param offset Result offset.
     * @param tenantId Tenant domain to be searched.
     * @return List of {@link Purpose} entries.
     * @throws ConsentManagementException If error occurs while searching the {@link Purpose}.
     */
    List<Purpose> listPurposes(String group, String groupType, int limit, int offset, int tenantId) throws
            ConsentManagementException;

    /**
     * Lists purposes with optional filter tree (V2 API).
     *
     * @param filterTree Filter tree from FilterTreeBuilder (null for no filtering)
     * @param limit      Maximum results
     * @param offset     Pagination offset
     * @param tenantId   Tenant ID
     * @return List of purposes matching filter
     * @throws ConsentManagementException if operation fails
     */
    default List<Purpose> listPurposes(Node filterTree, int limit, int offset, int tenantId)
            throws ConsentManagementException {

        return java.util.Collections.emptyList();
    }

    /**
     * Delete {@link Purpose} for a given ID.
     *
     * @param id ID of the {@link Purpose} to be deleted.
     * @return ID of the deleted {@link Purpose} if successful.
     * @throws ConsentManagementException If error occurs while deleting the {@link Purpose}
     */
    int deletePurpose(int id) throws ConsentManagementException;

    /**
     * Delete a {@link Purpose} and all of its versions in a single transaction.
     *
     * @param purposeId DB ID of the {@link Purpose} to be deleted.
     * @throws ConsentManagementException If error occurs while deleting.
     */
    default void deletePurposeWithVersions(int purposeId) throws ConsentManagementException {

    }

    /**
     * Delete all {@link Purpose} of a given tenant id.
     *
     * @param tenantId Id of the tenant
     * @throws ConsentManagementException
     */
    default void deletePurposesByTenantId(int tenantId) throws ConsentManagementException {

    }

    /**
     * Check whether the {@link Purpose} by ID is used in a receipt.
     *
     * @param id D of the {@link Purpose} to be validated
     * @return
     */
    boolean isPurposeUsed(int id) throws ConsentManagementServerException;

    /**
     * Check whether a {@link PurposeVersion} is used in any receipt.
     *
     * @param versionUuid UUID of the {@link PurposeVersion} to be validated
     * @return true if the version is used, false otherwise
     */
    default boolean isPurposeVersionUsed(String versionUuid) throws ConsentManagementServerException {

        return false;
    }

    /**
     * Add a new version for a {@link Purpose}.
     *
     * @param purposeVersion {@link PurposeVersion} to insert.
     * @param setAsLatest    Whether to set the new version as the latest version.
     * @return Inserted {@link PurposeVersion}.
     * @throws ConsentManagementException If error occurs while adding the {@link PurposeVersion}.
     */
    default PurposeVersion addPurposeVersion(PurposeVersion purposeVersion, boolean setAsLatest)
            throws ConsentManagementException {

        return null;
    }

    /**
     * List all versions for a given purpose UUID.
     *
     * @param uuid UUID of the {@link Purpose}.
     * @return List of {@link PurposeVersion} entries.
     * @throws ConsentManagementException If error occurs while listing {@link PurposeVersion}.
     */
    default List<PurposeVersion> listPurposeVersions(String uuid) throws ConsentManagementException {

        return new ArrayList<>();
    }

    /**
     * Delete a {@link PurposeVersion} by its UUID.
     *
     * @param versionUuid UUID of the {@link PurposeVersion} to delete.
     * @throws ConsentManagementException If error occurs while deleting the {@link PurposeVersion}.
     */
    default void deletePurposeVersion(String versionUuid) throws ConsentManagementException {

    }

    /**
     * Retrieve a {@link Purpose} by its UUID.
     *
     * @param uuid     UUID of the {@link Purpose}.
     * @param tenantId Tenant ID.
     * @return Purpose for the given UUID, or {@code null} if not found.
     * @throws ConsentManagementException If error occurs while retrieving the {@link Purpose}.
     */
    default Purpose getPurposeByUuid(String uuid, int tenantId) throws ConsentManagementException {

        return null;
    }

    /**
     * Retrieve a {@link PurposeVersion} by its UUID.
     *
     * @param uuid UUID of the {@link PurposeVersion}.
     * @return PurposeVersion for the given UUID, or {@code null} if not found.
     * @throws ConsentManagementException If error occurs.
     */
    default PurposeVersion getPurposeVersionByUuid(String uuid) throws ConsentManagementException {

        return null;
    }

    /**
     * Retrieve a {@link PurposeVersion} by purpose ID and version label string.
     *
     * @param purposeId Purpose DB ID.
     * @param version   Version label string.
     * @param tenantId  Tenant ID.
     * @return PurposeVersion, or {@code null} if not found.
     * @throws ConsentManagementException If error occurs.
     */
    default PurposeVersion getPurposeVersionByLabel(int purposeId, String version, int tenantId)
            throws ConsentManagementException {

        return null;
    }

    /**
     * Atomically syncs a purpose's PII category associations to match those of the given version,
     * and updates the purpose's LATEST_VERSION_ID.
     *
     * @param purposeId   DB ID of the purpose.
     * @param versionUuid UUID of the version to set as latest.
     * @param tenantId    Tenant ID.
     * @throws ConsentManagementException If error occurs.
     */
    default void updateLatestVersionId(int purposeId, String versionUuid, int tenantId)
            throws ConsentManagementException {

    }

}
