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

package org.wso2.carbon.consent.mgt.endpoint.v2;

import org.wso2.carbon.consent.mgt.endpoint.v2.factories.PurposesApiServiceFactory;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ErrorDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeListResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionListResponse;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import io.swagger.annotations.*;

import javax.validation.constraints.*;

@Path("/purposes")
@Api(description = "The purposes API")

public class PurposesApi  {

    private PurposesApiService delegate;

    public PurposesApi() {

        this.delegate = PurposesApiServiceFactory.getPurposesApi();
    }

    @Valid
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a new purpose", notes = "Create a new consent purpose (e.g., \"User Authentication\", \"Marketing\"). ", response = PurposeDTO.class, tags={ "Purposes", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Purpose created successfully", response = PurposeDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorDTO.class)
    })
    public Response purposesCreate(@ApiParam(value = "" ,required=true) @Valid PurposeCreateRequest purposeCreateRequest) {

        return delegate.purposesCreate(purposeCreateRequest );
    }

    @Valid
    @DELETE
    @Path("/{purposeId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a purpose", notes = "Delete a purpose by ID. ", response = Void.class, tags={ "Purposes", })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Purpose deleted successfully", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorDTO.class)
    })
    public Response purposesDelete(@ApiParam(value = "Unique identifier of the purpose.",required=true) @PathParam("purposeId") Integer purposeId) {

        return delegate.purposesDelete(purposeId );
    }

    @Valid
    @GET
    @Path("/{purposeId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a purpose", notes = "Retrieve a specific purpose by ID. ", response = PurposeDTO.class, tags={ "Purposes", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Purpose details", response = PurposeDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorDTO.class)
    })
    public Response purposesGet(@ApiParam(value = "Unique identifier of the purpose.",required=true) @PathParam("purposeId") Integer purposeId) {

        return delegate.purposesGet(purposeId );
    }

    @Valid
    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "List all purposes", notes = "Retrieve all purposes with optional filtering by group or groupType. ", response = PurposeListResponse.class, tags={ "Purposes", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of purposes", response = PurposeListResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorDTO.class)
    })
    public Response purposesList(    @Valid @Size(max=255)@ApiParam(value = "Filter by purpose group.")  @QueryParam("group") String group,     @Valid @Size(max=255)@ApiParam(value = "Filter by purpose group type.")  @QueryParam("groupType") String groupType,     @Valid @Min(1) @Max(200)@ApiParam(value = "Maximum number of records to return.", defaultValue="50") @DefaultValue("50")  @QueryParam("limit") Integer limit,     @Valid @Min(0)@ApiParam(value = "Number of records to skip before collecting the response set.", defaultValue="0") @DefaultValue("0")  @QueryParam("offset") Integer offset) {

        return delegate.purposesList(group,  groupType,  limit,  offset );
    }

    @Valid
    @POST
    @Path("/{purposeId}/versions")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a new purpose version", notes = "Add a new version to an existing purpose. ", response = PurposeVersionDTO.class, tags={ "Purposes", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Purpose version created successfully", response = PurposeVersionDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorDTO.class)
    })
    public Response purposesVersionsCreate(@ApiParam(value = "Unique identifier of the purpose.",required=true) @PathParam("purposeId") Integer purposeId, @ApiParam(value = "" ,required=true) @Valid PurposeVersionCreateRequest purposeVersionCreateRequest) {

        return delegate.purposesVersionsCreate(purposeId,  purposeVersionCreateRequest );
    }

    @Valid
    @DELETE
    @Path("/{purposeId}/versions/{versionId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a purpose version", notes = "Delete a specific version of a purpose. ", response = Void.class, tags={ "Purposes", })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Purpose version deleted successfully", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorDTO.class)
    })
    public Response purposesVersionsDelete(@ApiParam(value = "Unique identifier of the purpose.",required=true) @PathParam("purposeId") Integer purposeId, @ApiParam(value = "Unique identifier of the purpose version.",required=true) @PathParam("versionId") Integer versionId) {

        return delegate.purposesVersionsDelete(purposeId,  versionId );
    }

    @Valid
    @GET
    @Path("/{purposeId}/versions/{versionId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a purpose version", notes = "Retrieve a specific version of a purpose. ", response = PurposeVersionDTO.class, tags={ "Purposes", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Purpose version details", response = PurposeVersionDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorDTO.class)
    })
    public Response purposesVersionsGet(@ApiParam(value = "Unique identifier of the purpose.",required=true) @PathParam("purposeId") Integer purposeId, @ApiParam(value = "Unique identifier of the purpose version.",required=true) @PathParam("versionId") Integer versionId) {

        return delegate.purposesVersionsGet(purposeId,  versionId );
    }

    @Valid
    @GET
    @Path("/{purposeId}/versions")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "List purpose versions", notes = "Retrieve all versions of a specific purpose with pagination support. ", response = PurposeVersionListResponse.class, tags={ "Purposes" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of purpose versions", response = PurposeVersionListResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorDTO.class)
    })
    public Response purposesVersionsList(@ApiParam(value = "Unique identifier of the purpose.",required=true) @PathParam("purposeId") Integer purposeId,     @Valid @Min(1) @Max(200)@ApiParam(value = "Maximum number of records to return.", defaultValue="50") @DefaultValue("50")  @QueryParam("limit") Integer limit,     @Valid @Min(0)@ApiParam(value = "Number of records to skip before collecting the response set.", defaultValue="0") @DefaultValue("0")  @QueryParam("offset") Integer offset) {

        return delegate.purposesVersionsList(purposeId,  limit,  offset );
    }

}
