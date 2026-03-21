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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementListResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.util.FilterAttributeExtractor;
import org.wso2.carbon.identity.core.model.FilterTreeBuilder;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.base.IdentityException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_ELEMENT_UUID_NOT_FOUND;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_EXPRESSION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.FilterConstants;
import static org.wso2.carbon.consent.mgt.core.util.ConsentUtils.handleClientException;


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

        String displayName = StringUtils.isNotBlank(request.getDisplayName()) ? request.getDisplayName() : request.getName();
        PIICategory piiCategory = new PIICategory(request.getName(), request.getDescription(), false, displayName);
        piiCategory.setTenantId(ConsentUtils.getTenantIdFromCarbonContext());
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
    public Response getElement(UUID elementId) throws ConsentManagementException {

        PIICategory piiCategory = consentManager.getPIICategoryByUuid(elementId.toString());
        if (piiCategory == null) {
            throw handleClientException(ERROR_CODE_ELEMENT_UUID_NOT_FOUND, elementId.toString());
        }
        return Response.ok(toElementDTO(piiCategory)).build();
    }

    /**
     * Lists elements with optional filtering and pagination.
     *
     * @param filterExpression Filter expression string (null for no filtering).
     * @param limit            Maximum results.
     * @param offset           Pagination offset.
     * @return Response with ElementListResponse.
     * @throws ConsentManagementException if listing fails.
     */
    public Response listElements(String filterExpression, int limit, int offset) throws ConsentManagementException {

        Node filterTree = null;

        // Parse filter expression if provided
        if (StringUtils.isNotEmpty(filterExpression)) {
            try {
                FilterTreeBuilder filterTreeBuilder = new FilterTreeBuilder(filterExpression);
                filterTree = filterTreeBuilder.buildTree();

                // Validate filter attributes (only "name" is supported)
                Set<String> supportedAttrs = new HashSet<>(List.of(FilterConstants.FILTER_ATTR_NAME));
                FilterAttributeExtractor extractor = new FilterAttributeExtractor();
                extractor.validateFilterAttributes(filterTree, supportedAttrs);
            } catch (IdentityException | java.io.IOException e) {
                throw handleClientException(ERROR_CODE_INVALID_FILTER_EXPRESSION, e.getMessage());
            }
        }

        // Pass filterTree to manager
        List<PIICategory> categories = consentManager.listPIICategories(filterTree, limit, offset);
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
    public Response deleteElement(UUID elementId) throws ConsentManagementException {

        consentManager.deletePIICategory(elementId.toString());
        return Response.noContent().build();
    }

    private ElementDTO toElementDTO(PIICategory cat) {

        ElementDTO dto = new ElementDTO();
        if (StringUtils.isNotBlank(cat.getUuid())) {
            dto.setElementId(UUID.fromString(cat.getUuid()));
        }
        dto.setName(cat.getName());
        dto.setDisplayName(cat.getDisplayName());
        dto.setDescription(cat.getDescription());
        return dto;
    }
}
