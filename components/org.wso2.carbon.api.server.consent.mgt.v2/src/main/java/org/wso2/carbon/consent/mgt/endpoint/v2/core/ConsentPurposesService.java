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
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeElementBinding;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeElementDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeListResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeSummaryDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionListResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.util.ConsentV2EndpointUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;

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
     * @return Response with created PurposeDTO or error.
     */
    public Response createPurpose(PurposeCreateRequest request) {

        try {
            List<PurposePIICategory> purposePIICategories = buildPurposePIICategories(request.getElements());
            Purpose purpose = new Purpose(request.getName(), request.getDescription(),
                    request.getGroup(), request.getGroupType(), purposePIICategories);
            Purpose created = consentManager.addPurpose(purpose);
            return Response.status(Response.Status.CREATED).entity(toPurposeDTO(created)).build();
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Throwable t) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(t, LOG);
        }
    }

    /**
     * Retrieves a purpose by ID.
     *
     * @param purposeId Purpose ID.
     * @return Response with PurposeDTO or error.
     */
    public Response getPurpose(int purposeId) {

        try {
            Purpose purpose = consentManager.getPurpose(purposeId);
            return Response.ok(toPurposeDTO(purpose)).build();
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Throwable t) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(t, LOG);
        }
    }

    /**
     * Lists purposes with optional filtering and pagination.
     *
     * @param group     Group filter (may be null).
     * @param groupType Group type filter (may be null).
     * @param limit     Maximum results.
     * @param offset    Pagination offset.
     * @return Response with list of PurposeDTOs or error.
     */
    public Response listPurposes(String group, String groupType, int limit, int offset) {

        try {
            List<Purpose> purposes = consentManager.listPurposes(group, groupType, limit, offset);
            List<PurposeSummaryDTO> items = new ArrayList<>();
            if (purposes != null) {
                for (Purpose p : purposes) {
                    items.add(toPurposeSummaryDTO(p));
                }
            }

            PurposeListResponse listResponse = new PurposeListResponse();
            listResponse.setStartIndex(offset);
            listResponse.setCount(items.size());
            listResponse.setItems(items);
            return Response.ok(listResponse).build();
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Throwable t) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(t, LOG);
        }
    }

    /**
     * Deletes a purpose by ID.
     *
     * @param purposeId Purpose ID.
     * @return Response with no content or error.
     */
    public Response deletePurpose(int purposeId) {

        try {
            consentManager.deletePurpose(purposeId);
            return Response.noContent().build();
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Throwable t) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(t, LOG);
        }
    }

    /**
     * Creates a new version for a purpose.
     *
     * @param purposeId Purpose ID.
     * @param request   Version create request.
     * @return Response with created PurposeVersionDTO or error.
     */
    public Response createPurposeVersion(int purposeId, PurposeVersionCreateRequest request) {

        try {
            PurposeVersion version = new PurposeVersion();
            version.setPurposeId(purposeId);
            version.setDescription(request.getDescription());
            version.setPurposePIICategories(buildPurposePIICategories(request.getElements()));

            PurposeVersion created = consentManager.addPurposeVersion(purposeId, version);
            return Response.status(Response.Status.CREATED).entity(toPurposeVersionDTO(created)).build();
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Throwable t) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(t, LOG);
        }
    }

    /**
     * Retrieves a specific version of a purpose.
     *
     * @param purposeId Purpose ID.
     * @param versionId Version ID.
     * @return Response with PurposeVersionDTO or error.
     */
    public Response getPurposeVersion(int purposeId, int versionId) {

        try {
            PurposeVersion version = consentManager.getPurposeVersion(purposeId, versionId);
            return Response.ok(toPurposeVersionDTO(version)).build();
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Throwable t) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(t, LOG);
        }
    }

    /**
     * Lists versions of a purpose with pagination applied in memory.
     *
     * @param purposeId Purpose ID.
     * @param limit     Maximum results.
     * @param offset    Pagination offset.
     * @return Response with list of PurposeVersionDTOs or error.
     */
    public Response listPurposeVersions(int purposeId, int limit, int offset) {

        try {
            List<PurposeVersion> versions = consentManager.listPurposeVersions(purposeId);
            if (versions == null) {
                versions = Collections.emptyList();
            }
            int total = versions.size();
            int fromIndex = Math.min(offset, total);
            int toIndex = Math.min(offset + limit, total);
            List<PurposeVersion> page = versions.subList(fromIndex, toIndex);

            List<PurposeVersionDTO> dtos = new ArrayList<>();
            for (PurposeVersion v : page) {
                dtos.add(toPurposeVersionDTO(v));
            }

            PurposeVersionListResponse listResponse = new PurposeVersionListResponse();
            listResponse.setStartIndex(offset);
            listResponse.setCount(dtos.size());
            listResponse.setItems(dtos);
            return Response.ok(listResponse).build();
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Throwable t) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(t, LOG);
        }
    }

    /**
     * Deletes a specific version of a purpose.
     *
     * @param purposeId Purpose ID.
     * @param versionId Version ID.
     * @return Response with no content or error.
     */
    public Response deletePurposeVersion(int purposeId, int versionId) {

        try {
            consentManager.deletePurposeVersion(purposeId, versionId);
            return Response.noContent().build();
        } catch (ConsentManagementClientException e) {
            return ConsentV2EndpointUtils.handleBadRequestResponse(e, LOG);
        } catch (ConsentManagementException e) {
            return ConsentV2EndpointUtils.handleServerErrorResponse(e, LOG);
        } catch (Throwable t) {
            return ConsentV2EndpointUtils.handleUnexpectedServerError(t, LOG);
        }
    }

    private List<PurposePIICategory> buildPurposePIICategories(List<PurposeElementBinding> elements) {

        List<PurposePIICategory> result = new ArrayList<>();
        if (elements == null) {
            return result;
        }
        for (PurposeElementBinding binding : elements) {
            Boolean mandatory = Boolean.TRUE.equals(binding.getMandatory());
            result.add(new PurposePIICategory(binding.getElementId(), mandatory));
        }
        return result;
    }

    private PurposeSummaryDTO toPurposeSummaryDTO(Purpose purpose) {

        PurposeSummaryDTO dto = new PurposeSummaryDTO();
        dto.setId(purpose.getId());
        dto.setName(purpose.getName());
        dto.setDescription(purpose.getDescription());
        dto.setGroup(purpose.getGroup());
        dto.setGroupType(purpose.getGroupType());
        PurposeVersion latestVersion = purpose.getLatestVersion();
        dto.setVersion(latestVersion != null ? latestVersion.getVersion() : 1);
        return dto;
    }

    private PurposeDTO toPurposeDTO(Purpose purpose) {

        PurposeDTO dto = new PurposeDTO();
        dto.setId(purpose.getId());
        dto.setName(purpose.getName());
        dto.setDescription(purpose.getDescription());
        dto.setGroup(purpose.getGroup());
        dto.setGroupType(purpose.getGroupType());

        PurposeVersion latestVersion = purpose.getLatestVersion();
        dto.setVersion(latestVersion != null ? latestVersion.getVersion() : 1);

        List<PurposeElementDTO> elementDTOs = new ArrayList<>();
        List<PurposePIICategory> categories = purpose.getPurposePIICategories();
        if (categories != null) {
            for (PurposePIICategory cat : categories) {
                elementDTOs.add(toPurposeElementDTO(cat));
            }
        }
        dto.setElements(elementDTOs);
        return dto;
    }

    private PurposeVersionDTO toPurposeVersionDTO(PurposeVersion version) {

        PurposeVersionDTO dto = new PurposeVersionDTO();
        dto.setId(version.getId());
        dto.setVersion(version.getVersion());
        dto.setDescription(version.getDescription());

        List<PurposeElementDTO> elementDTOs = new ArrayList<>();
        if (version.getPurposePIICategories() != null) {
            for (PurposePIICategory cat : version.getPurposePIICategories()) {
                elementDTOs.add(toPurposeElementDTO(cat));
            }
        }
        dto.setElements(elementDTOs);
        return dto;
    }

    private PurposeElementDTO toPurposeElementDTO(PurposePIICategory cat) {

        PurposeElementDTO dto = new PurposeElementDTO();
        dto.setElementId(cat.getId());
        dto.setName(cat.getName());
        dto.setDisplayName(cat.getDisplayName());
        dto.setDescription(cat.getDescription());
        dto.setMandatory(cat.getMandatory());
        return dto;
    }

}

