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
import org.wso2.carbon.consent.mgt.core.model.Purpose;

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
     * @param tenantId Tenant domain of the {@link Purpose}.
     * @return Purpose for the input criteria.
     * @throws ConsentManagementException If error occurs while retrieving the {@link Purpose}.
     */
    Purpose getPurposeByName(String name, int tenantId) throws ConsentManagementException;

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
     * Delete {@link Purpose} for a given ID.
     *
     * @param id ID of the {@link Purpose} to be deleted.
     * @return ID of the deleted {@link Purpose} if successful.
     * @throws ConsentManagementException If error occurs while deleting the {@link Purpose}
     */
    int deletePurpose(int id) throws ConsentManagementException;
}
