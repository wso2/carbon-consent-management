/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.consent.mgt.endpoint.v2.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementListResponse;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

/**
 * Service class for element (PIICategory) operations in the V2 Consent Management API.
 */
public class ConsentElementsService {

    private static final Log LOG = LogFactory.getLog(ConsentElementsService.class);

    private final ConsentManager consentManager;

    public ConsentElementsService(ConsentManager consentManager) {

        this.consentManager = consentManager;
    }

    /**
     * Creates a new element (PIICategory).
     *
     * @param request Element create request.
     * @return ElementDTO with created element details.
     * @throws ConsentManagementException if creation fails.
     */
    public ElementDTO createElement(ElementCreateRequest request) throws ConsentManagementException {

        String displayName = (request.getDisplayName() != null) ? request.getDisplayName() : request.getName();
        PIICategory piiCategory = new PIICategory(request.getName(), request.getDescription(), false, displayName);
        PIICategory created = consentManager.addPIICategory(piiCategory);
        return toElementDTO(created);
    }

    /**
     * Retrieves a single element by ID.
     *
     * @param elementId Element ID.
     * @return Response with ElementDTO.
     * @throws ConsentManagementException if retrieval fails.
     */
    public Response getElement(int elementId) throws ConsentManagementException {

        PIICategory piiCategory = consentManager.getPIICategory(elementId);
        return Response.ok(toElementDTO(piiCategory)).build();
    }

    /**
     * Lists elements with pagination.
     *
     * @param limit  Maximum results.
     * @param offset Pagination offset.
     * @return Response with ElementListResponse.
     * @throws ConsentManagementException if listing fails.
     */
    public Response listElements(int limit, int offset) throws ConsentManagementException {

        List<PIICategory> categories = consentManager.listPIICategories(limit, offset);
        ElementListResponse listResponse = new ElementListResponse();
        List<ElementDTO> items = new ArrayList<>();
        if (categories != null) {
            for (PIICategory cat : categories) {
                items.add(toElementDTO(cat));
            }
        }
        listResponse.setStartIndex(offset);
        listResponse.setCount(items.size());
        listResponse.setItems(items);

        return Response.ok(listResponse).build();
    }

    /**
     * Deletes an element by ID.
     *
     * @param elementId Element ID.
     * @return Response with no content.
     * @throws ConsentManagementException if deletion fails.
     */
    public Response deleteElement(int elementId) throws ConsentManagementException {

        consentManager.deletePIICategory(elementId);
        return Response.noContent().build();
    }

    private ElementDTO toElementDTO(PIICategory cat) {

        ElementDTO dto = new ElementDTO();
        dto.setId(cat.getId());
        dto.setName(cat.getName());
        dto.setDisplayName(cat.getDisplayName());
        dto.setDescription(cat.getDescription());
        return dto;
    }
}
