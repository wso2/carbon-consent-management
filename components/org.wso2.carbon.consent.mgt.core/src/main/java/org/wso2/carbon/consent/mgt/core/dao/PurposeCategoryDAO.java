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
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;

import java.util.List;

/**
 * Perform CRUD operations for {@link PurposeCategory}.
 *
 * @since 1.0.0
 */
public interface PurposeCategoryDAO {

    /**
     * Returns the priority of the DAO.
     *
     * @return priority of the DAO.
     */
    int getPriority();

    /**
     * Add a {@link PurposeCategory}.
     *
     * @param purposeCategory {@link PurposeCategory} to insert.
     * @return Inserted {@link PurposeCategory}.
     * @throws ConsentManagementException If error occurs while adding the {@link PurposeCategory}.
     */
    PurposeCategory addPurposeCategory(PurposeCategory purposeCategory) throws ConsentManagementException;

    /**
     * Retrieve {@link PurposeCategory} by ID.
     *
     * @param id ID of the {@link PurposeCategory} to retrieve.
     * @return PurposeCategory for the given ID.
     * @throws ConsentManagementException If error occurs while retrieving {@link PurposeCategory}.
     */
    PurposeCategory getPurposeCategoryById(int id) throws ConsentManagementException;

    /**
     * List {@link PurposeCategory} items for a given search criteria.
     *
     * @param limit Maximum number of results expected.
     * @param offset Result offset.
     * @param tenantId Tenant domain to be searched.
     * @return List of {@link PurposeCategory} entries.
     * @throws ConsentManagementException If error occurs while searching the {@link PurposeCategory}.
     */
    List<PurposeCategory> listPurposeCategories(int limit, int offset, int tenantId) throws ConsentManagementException;

    /**
     * Delete {@link PurposeCategory} for a given ID.
     *
     * @param id ID of the {@link PurposeCategory} to be deleted.
     * @return ID of the deleted {@link PurposeCategory} if successful.
     * @throws ConsentManagementException If error occurs while deleting the {@link PurposeCategory}
     */
    int deletePurposeCategory(int id) throws ConsentManagementException;

    /**
     * Delete all {@link PurposeCategory} of a given tenant id.
     *
     * @param tenantId Id of the tenant
     * @throws ConsentManagementException
     */
    default void deletePurposeCategoriesByTenantId(int tenantId) throws ConsentManagementException {

    }

    /**
     * Get the {@link PurposeCategory} corresponding to the input name.
     *
     * @param name Name of the {@link PurposeCategory}.
     * @param tenantId Tenant domain of the {@link PurposeCategory}.
     * @return PurposeCategory for the input criteria.
     * @throws ConsentManagementException If error occurs while retrieving the {@link PurposeCategory}.
     */
    PurposeCategory getPurposeCategoryByName(String name, int tenantId) throws ConsentManagementException;
}
