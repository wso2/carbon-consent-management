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
import org.wso2.carbon.consent.mgt.core.util.ConsentUtils;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeDTOLatestVersion;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeElementBinding;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeElementDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeListResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeSummaryDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionListResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionSummaryDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.SetLatestVersionRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.util.FilterAttributeExtractor;
import org.wso2.carbon.identity.core.model.FilterTreeBuilder;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.base.IdentityException;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.FilterConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.DEFAULT_PURPOSE_GROUP;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_INVALID_FILTER_EXPRESSION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_UUID_NOT_FOUND;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_VERSION_NOT_FOUND;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_ELEMENT_UUID_NOT_FOUND;
import static org.wso2.carbon.consent.mgt.core.util.ConsentUtils.handleClientException;

/**
 * Service class for purpose operations in the V2 Consent Management API.
 */
public class ConsentPurposesService {

    private static final Log LOG = LogFactory.getLog(ConsentPurposesService.class);

    private final ConsentManager consentManager;

    public ConsentPurposesService(ConsentManager consentManager) {

        this.consentManager = consentManager;
    }

    /**
     * Creates a new purpose.
     *
     * @param request Purpose create request.
     * @return PurposeDTO with created purpose details.
     * @throws ConsentManagementException if creation fails.
     */
    public PurposeDTO createPurpose(PurposeCreateRequest request) throws ConsentManagementException {

        List<PurposePIICategory> purposePIICategories = buildPurposePIICategories(request.getElements());
        Purpose purpose = new Purpose(request.getName(), request.getDescription(),
                DEFAULT_PURPOSE_GROUP, request.getType(), purposePIICategories);
        purpose.setTenantId(ConsentUtils.getTenantIdFromCarbonContext());

        if (request.getVersion() != null) {
            PurposeVersion firstVersion = new PurposeVersion();
            firstVersion.setVersion(request.getVersion());
            firstVersion.setDescription(request.getDescription());
            firstVersion.setTenantId(ConsentUtils.getTenantIdFromCarbonContext());
            firstVersion.setPurposePIICategories(purposePIICategories);
            firstVersion.setProperties(request.getProperties());
            Object[] result = consentManager.addPurpose(purpose, firstVersion);
            Purpose created = (Purpose) result[0];
            return toPurposeDTO(created);
        }

        Purpose created = consentManager.addPurpose(purpose);
        return toPurposeDTO(created);
    }

    /**
     * Retrieves a purpose by ID.
     *
     * @param purposeId Purpose ID.
     * @return Response with PurposeDTO.
     * @throws ConsentManagementException if retrieval fails.
     */
    public Response getPurpose(UUID purposeId) throws ConsentManagementException {

        Purpose purpose = consentManager.getPurposeByUuid(purposeId.toString());
        if (purpose == null) {
            throw handleClientException(ERROR_CODE_PURPOSE_UUID_NOT_FOUND, purposeId.toString());
        }
        return Response.ok(toPurposeDTO(purpose)).build();
    }

    /**
     * Lists purposes with optional filtering and pagination.
     *
     * @param filterExpression Filter expression string (null for no filtering).
     * @param limit            Maximum results.
     * @param offset           Pagination offset.
     * @return Response with list of PurposeSummaryDTOs.
     * @throws ConsentManagementException if listing fails.
     */
    public Response listPurposes(String filterExpression, int limit, int offset) throws ConsentManagementException {

        Node filterTree = null;

        // Parse filter expression if provided
        if (StringUtils.isNotEmpty(filterExpression)) {
            try {
                FilterTreeBuilder filterTreeBuilder = new FilterTreeBuilder(filterExpression);
                filterTree = filterTreeBuilder.buildTree();

                // Validate filter attributes
                Set<String> supportedAttrs = new HashSet<>(Arrays.asList(
                        FilterConstants.FILTER_ATTR_NAME,
                        FilterConstants.FILTER_ATTR_TYPE));
                FilterAttributeExtractor extractor = new FilterAttributeExtractor();
                extractor.validateFilterAttributes(filterTree, supportedAttrs);

            } catch (IdentityException | java.io.IOException e) {
                throw handleClientException(ERROR_CODE_INVALID_FILTER_EXPRESSION, e.getMessage());
            }
        }

        // Pass filterTree to manager (V2 method without group/groupType)
        List<Purpose> purposes = consentManager.listPurposes(filterTree, limit, offset);

        List<PurposeSummaryDTO> items = new ArrayList<>();
        if (purposes != null) {
            for (Purpose p : purposes) {
                if (StringUtils.isBlank(p.getUuid())) {
                    continue;
                }
                items.add(toPurposeSummaryDTO(p));
            }
        }

        PurposeListResponse listResponse = new PurposeListResponse();
        listResponse.setStartIndex(offset);
        listResponse.setCount(items.size());
        listResponse.setItems(items);
        return Response.ok(listResponse).build();
    }

    /**
     * Deletes a purpose by ID.
     *
     * @param purposeId Purpose ID.
     * @return Response with no content.
     * @throws ConsentManagementException if deletion fails.
     */
    public Response deletePurpose(UUID purposeId) throws ConsentManagementException {

        consentManager.deletePurpose(purposeId.toString());
        return Response.noContent().build();
    }

    /**
     * Creates a new version for a purpose.
     *
     * @param purposeId Purpose ID.
     * @param request   Version create request.
     * @return PurposeVersionDTO with created version details.
     * @throws ConsentManagementException if creation fails.
     */
    public PurposeVersionDTO createPurposeVersion(UUID purposeId, PurposeVersionCreateRequest request) throws ConsentManagementException {

        PurposeVersion version = new PurposeVersion();
        version.setVersion(request.getVersion());
        version.setDescription(request.getDescription());
        version.setPurposePIICategories(buildPurposePIICategories(request.getElements()));
        version.setProperties(request.getProperties());

        PurposeVersion created = consentManager.addPurposeVersion(purposeId.toString(), version, Boolean.TRUE.equals(request.getSetAsLatest()));
        return toPurposeVersionDTO(created);
    }

    /**
     * Retrieves a specific version of a purpose.
     *
     * @param purposeId Purpose UUID.
     * @param versionId Version UUID.
     * @return Response with PurposeVersionDTO.
     * @throws ConsentManagementException if retrieval fails.
     */
    public Response getPurposeVersion(UUID purposeId, UUID versionId) throws ConsentManagementException {

        PurposeVersion version = consentManager.getPurposeVersion(purposeId.toString(), versionId.toString());
        if (version == null) {
            throw handleClientException(ERROR_CODE_PURPOSE_VERSION_NOT_FOUND, versionId.toString());
        }
        return Response.ok(toPurposeVersionDTO(version)).build();
    }

    /**
     * Lists versions of a purpose with pagination applied in memory.
     *
     * @param purposeId Purpose UUID.
     * @param limit     Maximum results.
     * @param offset    Pagination offset.
     * @return Response with list of PurposeVersionDTOs.
     * @throws ConsentManagementException if listing fails.
     */
    public Response listPurposeVersions(UUID purposeId, int limit, int offset) throws ConsentManagementException {

        List<PurposeVersion> versions = consentManager.listPurposeVersions(purposeId.toString());
        if (versions == null) {
            versions = Collections.emptyList();
        }
        int total = versions.size();
        int fromIndex = Math.min(offset, total);
        int toIndex = Math.min(offset + limit, total);
        List<PurposeVersion> page = versions.subList(fromIndex, toIndex);

        List<PurposeVersionSummaryDTO> dtos = new ArrayList<>();
        for (PurposeVersion v : page) {
            dtos.add(toPurposeVersionSummaryDTO(v));
        }

        PurposeVersionListResponse listResponse = new PurposeVersionListResponse();
        listResponse.setStartIndex(offset);
        listResponse.setCount(dtos.size());
        listResponse.setItems(dtos);
        return Response.ok(listResponse).build();
    }

    /**
     * Deletes a specific version of a purpose.
     *
     * @param purposeId Purpose UUID.
     * @param versionId Version UUID.
     * @return Response with no content.
     * @throws ConsentManagementException if deletion fails.
     */
    public Response deletePurposeVersion(UUID purposeId, UUID versionId) throws ConsentManagementException {

        consentManager.deletePurposeVersion(purposeId.toString(), versionId.toString());
        return Response.noContent().build();
    }

    /**
     * Sets the latest version of a purpose using the version UUID from the request body.
     *
     * @param purposeId UUID of the purpose.
     * @param request   SetLatestVersionRequest containing the target versionId UUID.
     * @return Response with no content.
     * @throws ConsentManagementException if operation fails.
     */
    public Response setLatestVersion(UUID purposeId, SetLatestVersionRequest request) throws ConsentManagementException {

        PurposeVersion version = consentManager.getPurposeVersion(purposeId.toString(), request.getVersionId().toString());
        if (version == null) {
            throw handleClientException(ERROR_CODE_PURPOSE_VERSION_NOT_FOUND, request.getVersionId().toString());
        }
        consentManager.setLatestPurposeVersion(version.getPurposeId(), version.getVersion());
        return Response.noContent().build();
    }

    private List<PurposePIICategory> buildPurposePIICategories(List<PurposeElementBinding> elements)
            throws ConsentManagementException {

        List<PurposePIICategory> result = new ArrayList<>();
        if (elements == null) {
            return result;
        }
        for (PurposeElementBinding binding : elements) {
            PIICategory element = consentManager.getPIICategoryByUuid(binding.getElementId().toString());
            if (element == null) {
                throw handleClientException(ERROR_CODE_ELEMENT_UUID_NOT_FOUND, binding.getElementId().toString());
            }
            Boolean mandatory = Boolean.TRUE.equals(binding.getMandatory());
            result.add(new PurposePIICategory(element, mandatory));
        }
        return result;
    }

    private PurposeSummaryDTO toPurposeSummaryDTO(Purpose purpose) {

        PurposeSummaryDTO dto = new PurposeSummaryDTO();
        dto.setPurposeId(UUID.fromString(purpose.getUuid()));
        dto.setName(purpose.getName());
        dto.setDescription(purpose.getDescription());
        dto.setType(purpose.getGroupType());
        PurposeVersion latestVersion = purpose.getLatestVersion();
        if (latestVersion != null) {
            PurposeDTOLatestVersion lv = new PurposeDTOLatestVersion();
            if (StringUtils.isNotBlank(latestVersion.getUuid())) {
                lv.setVersionId(UUID.fromString(latestVersion.getUuid()));
            }
            lv.setVersion(latestVersion.getVersion());
            dto.setLatestVersion(lv);
        }
        return dto;
    }

    private PurposeDTO toPurposeDTO(Purpose purpose) {

        PurposeDTO dto = new PurposeDTO();
        dto.setPurposeId(UUID.fromString(purpose.getUuid()));
        dto.setName(purpose.getName());
        dto.setDescription(purpose.getDescription());
        dto.setType(purpose.getGroupType());

        PurposeVersion latestVersion = purpose.getLatestVersion();
        if (latestVersion != null) {
            PurposeDTOLatestVersion lv = new PurposeDTOLatestVersion();
            if (StringUtils.isNotBlank(latestVersion.getUuid())) {
                lv.setVersionId(UUID.fromString(latestVersion.getUuid()));
            }
            lv.setVersion(latestVersion.getVersion());
            dto.setLatestVersion(lv);
        }

        List<PurposeElementDTO> elementDTOs = new ArrayList<>();
        List<PurposePIICategory> categories = purpose.getPurposePIICategories();
        if (categories != null) {
            for (PurposePIICategory cat : categories) {
                elementDTOs.add(toPurposeElementDTO(cat));
            }
        }
        dto.setElements(elementDTOs);

        if (latestVersion != null && latestVersion.getProperties() != null) {
            dto.setProperties(latestVersion.getProperties());
        }
        return dto;
    }

    private PurposeVersionDTO toPurposeVersionDTO(PurposeVersion version) {

        PurposeVersionDTO dto = new PurposeVersionDTO();
        if (StringUtils.isNotBlank(version.getUuid())) {
            dto.setVersionId(UUID.fromString(version.getUuid()));
        }
        dto.setVersion(version.getVersion());
        dto.setDescription(version.getDescription());
        dto.setProperties(version.getProperties());

        List<PurposeElementDTO> elementDTOs = new ArrayList<>();
        if (version.getPurposePIICategories() != null) {
            for (PurposePIICategory cat : version.getPurposePIICategories()) {
                elementDTOs.add(toPurposeElementDTO(cat));
            }
        }
        dto.setElements(elementDTOs);
        return dto;
    }

    private PurposeVersionSummaryDTO toPurposeVersionSummaryDTO(PurposeVersion version) {

        PurposeVersionSummaryDTO dto = new PurposeVersionSummaryDTO();
        if (version.getUuid() != null) {
            dto.setVersionId(UUID.fromString(version.getUuid()));
        }
        dto.setVersion(version.getVersion());
        dto.setDescription(version.getDescription());
        return dto;
    }

    private PurposeElementDTO toPurposeElementDTO(PurposePIICategory cat) {

        PurposeElementDTO dto = new PurposeElementDTO();
        if (cat.getUuid() != null) {
            dto.setElementId(UUID.fromString(cat.getUuid()));
        }
        dto.setName(cat.getName());
        dto.setDisplayName(cat.getDisplayName());
        dto.setDescription(cat.getDescription());
        dto.setMandatory(cat.getMandatory());
        return dto;
    }

}

