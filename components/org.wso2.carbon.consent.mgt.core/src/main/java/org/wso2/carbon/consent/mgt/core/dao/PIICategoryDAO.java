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
import org.wso2.carbon.consent.mgt.core.model.PIICategory;

import java.util.List;

/**
 * Perform CRUD operations for {@link PIICategory}.
 *
 * @since 1.0.0
 */
public interface PIICategoryDAO {

    /**
     * Returns the priority of the DAO.
     *
     * @return priority of the DAO.
     */
    int getPriority();

    /**
     * Add a {@link PIICategory}.
     *
     * @param piiCategory {@link PIICategory} to insert.
     * @return Inserted {@link PIICategory}.
     * @throws ConsentManagementException If error occurs while adding the {@link PIICategory}.
     */
    PIICategory addPIICategory(PIICategory piiCategory) throws ConsentManagementException;

    /**
     * Retrieve {@link PIICategory} by ID.
     *
     * @param id ID of the {@link PIICategory} to retrieve.
     * @return PIICategory for the given ID.
     * @throws ConsentManagementException If error occurs while retrieving {@link PIICategory}.
     */
    PIICategory getPIICategoryById(int id) throws ConsentManagementException;

    /**
     * List {@link PIICategory} items for a given search criteria.
     *
     * @param limit Maximum number of results expected.
     * @param offset Result offset.
     * @param tenantId Tenant domain to be searched.
     * @return List of {@link PIICategory} entries.
     * @throws ConsentManagementException If error occurs while searching the {@link PIICategory}.
     */
    List<PIICategory> listPIICategories(int limit, int offset, int tenantId) throws ConsentManagementException;
    /**
     * Lists PII categories with optional filter tree.
     *
     * @param filterTree Filter tree from FilterTreeBuilder (null for no filtering)
     * @param limit      Maximum results
     * @param offset     Pagination offset
     * @param tenantId   Tenant ID
     * @return List of PII categories matching filter
     * @throws ConsentManagementException if operation fails
     */
    default List<PIICategory> listPIICategories(org.wso2.carbon.identity.core.model.Node filterTree, int limit,
                                                int offset, int tenantId) throws ConsentManagementException {

        return java.util.Collections.emptyList();
    }
    /**
     * Delete {@link PIICategory} for a given ID.
     *
     * @param id ID of the {@link PIICategory} to be deleted.
     * @return ID of the deleted {@link PIICategory} if successful.
     * @throws ConsentManagementException If error occurs while deleting the {@link PIICategory}
     */
    int deletePIICategory(int id) throws ConsentManagementException;

    /**
     * Delete all {@link PIICategory} of a given tenant id.
     *
     * @param tenantId Id of the tenant
     * @throws ConsentManagementException
     */
    default void deletePIICategoriesByTenantId(int tenantId) throws ConsentManagementException {

    }

    /**
     * Get the {@link PIICategory} corresponding to the input name.
     *
     * @param name Name of the {@link PIICategory}.
     * @param tenantId Tenant domain of the {@link PIICategory}.
     * @return PIICategory for the input criteria.
     * @throws ConsentManagementException If error occurs while retrieving the {@link PIICategory}.
     */
    PIICategory getPIICategoryByName(String name, int tenantId) throws ConsentManagementException;


    /**
     * Check whether the {@link PIICategory} by ID is used in a purpose or service.
     *
     * @param id D of the {@link PIICategory} to be validated
     * @return
     */
    boolean isPIICategoryUsed(int id) throws ConsentManagementException;

    /**
     * Retrieve a {@link PIICategory} by its UUID.
     *
     * @param uuid     UUID of the {@link PIICategory}.
     * @param tenantId Tenant ID.
     * @return PIICategory for the given UUID, or {@code null} if not found.
     * @throws ConsentManagementException If error occurs while retrieving the {@link PIICategory}.
     */
    default PIICategory getPIICategoryByUuid(String uuid, int tenantId) throws ConsentManagementException {

        return null;
    }
}
