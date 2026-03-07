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
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;

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
     * Delete {@link Purpose} for a given ID.
     *
     * @param id ID of the {@link Purpose} to be deleted.
     * @return ID of the deleted {@link Purpose} if successful.
     * @throws ConsentManagementException If error occurs while deleting the {@link Purpose}
     */
    int deletePurpose(int id) throws ConsentManagementException;

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
     * @param versionId ID of the {@link PurposeVersion} to be validated
     * @return true if the version is used, false otherwise
     */
    boolean isPurposeVersionUsed(int versionId) throws ConsentManagementServerException;

    /**
     * Add a new version for a {@link Purpose}.
     *
     * @param purposeVersion {@link PurposeVersion} to insert.
     * @return Inserted {@link PurposeVersion}.
     * @throws ConsentManagementException If error occurs while adding the {@link PurposeVersion}.
     */
    default PurposeVersion addPurposeVersion(PurposeVersion purposeVersion) throws ConsentManagementException {

        return null;
    }

    /**
     * List all versions for a given purpose ID.
     *
     * @param purposeId ID of the {@link Purpose}.
     * @return List of {@link PurposeVersion} entries.
     * @throws ConsentManagementException If error occurs while listing {@link PurposeVersion}.
     */
    default List<PurposeVersion> listPurposeVersions(int purposeId) throws ConsentManagementException {

        return new ArrayList<>();
    }

    /**
     * Returns the current maximum version number for a purpose, or 1 if none exist (version 1 is implicit).
     *
     * @param purposeId ID of the {@link Purpose}.
     * @return Maximum version number, or 1 if no versions have been explicitly created.
     * @throws ConsentManagementException If error occurs while retrieving the max version.
     */
    default int getMaxPurposeVersionNumber(int purposeId) throws ConsentManagementException {

        return 1;
    }

    /**
     * Retrieve a single {@link PurposeVersion} by its ID.
     *
     * @param versionId ID of the {@link PurposeVersion}.
     * @return PurposeVersion for the given ID, or {@code null} if not found.
     * @throws ConsentManagementException If error occurs while retrieving the {@link PurposeVersion}.
     */
    default PurposeVersion getPurposeVersionById(int versionId) throws ConsentManagementException {

        return null;
    }

    /**
     * Delete a {@link PurposeVersion} by its ID.
     *
     * @param versionId ID of the {@link PurposeVersion} to delete.
     * @throws ConsentManagementException If error occurs while deleting the {@link PurposeVersion}.
     */
    default void deletePurposeVersion(int versionId) throws ConsentManagementException {

    }

}
