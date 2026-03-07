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

import org.wso2.carbon.consent.mgt.endpoint.v2.factories.ConsentsApiServiceFactory;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentCreateRequest;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentListResponse;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentReceiptDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ErrorDTO;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import io.swagger.annotations.*;

import javax.validation.constraints.*;

@Path("/consents")
@Api(description = "The consents API")

public class ConsentsApi  {

    private final ConsentsApiService delegate;

    public ConsentsApi() {

        this.delegate = ConsentsApiServiceFactory.getConsentsApi();
    }

    @Valid
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a consent record", notes = "Record user consent for specified purposes and elements. ", response = ConsentResponseDTO.class, authorizations = {
        @Authorization(value = "BasicAuth"),
        @Authorization(value = "OAuth2", scopes = {
            
        })
    }, tags={ "Consents", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Consent recorded successfully", response = ConsentResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorDTO.class)
    })
    public Response consentsCreate(@ApiParam(value = "" ,required=true) @Valid ConsentCreateRequest consentCreateRequest) {

        return delegate.consentsCreate(consentCreateRequest );
    }

    @Valid
    @GET
    @Path("/{receiptId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a consent record", notes = "Retrieve a specific consent record by receipt ID. ", response = ConsentReceiptDTO.class, authorizations = {
        @Authorization(value = "BasicAuth"),
        @Authorization(value = "OAuth2", scopes = {
            
        })
    }, tags={ "Consents", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Consent record details", response = ConsentReceiptDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorDTO.class)
    })
    public Response consentsGet( @Size(min=1,max=255)@ApiParam(value = "Unique identifier of the consent receipt (UUID).",required=true) @PathParam("receiptId") String receiptId) {

        return delegate.consentsGet(receiptId );
    }

    @Valid
    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "List consent records", notes = "Retrieve consent records with optional filtering by user, service, state, or purpose. ", response = ConsentListResponse.class, authorizations = {
        @Authorization(value = "BasicAuth"),
        @Authorization(value = "OAuth2", scopes = {
            
        })
    }, tags={ "Consents", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of consents", response = ConsentListResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorDTO.class)
    })
    public Response consentsList(    @Valid @Size(max=255)@ApiParam(value = "Filter by subject user ID.")  @QueryParam("subjectUserId") String subjectUserId,     @Valid @Size(max=255)@ApiParam(value = "Filter by service name.")  @QueryParam("service") String service,     @Valid@ApiParam(value = "Filter by consent state.", allowableValues="ACTIVE, REVOKED")  @QueryParam("state") String state,     @Valid@ApiParam(value = "Filter consents by purpose ID.")  @QueryParam("purposeId") Integer purposeId,     @Valid @Min(1)@ApiParam(value = "Maximum number of records to return.", defaultValue="10") @DefaultValue("10")  @QueryParam("limit") Integer limit,     @Valid @Min(0)@ApiParam(value = "Number of records to skip before collecting the response set.", defaultValue="0") @DefaultValue("0")  @QueryParam("offset") Integer offset) {

        return delegate.consentsList(subjectUserId,  service,  state,  purposeId,  limit,  offset );
    }

    @Valid
    @POST
    @Path("/{receiptId}/revoke")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Revoke a consent record", notes = "Revoke an active consent record. ", response = Void.class, authorizations = {
        @Authorization(value = "BasicAuth"),
        @Authorization(value = "OAuth2", scopes = {
            
        })
    }, tags={ "Consents" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Consent revoked successfully", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorDTO.class)
    })
    public Response consentsRevoke( @Size(min=1,max=255)@ApiParam(value = "Unique identifier of the consent receipt (UUID).",required=true) @PathParam("receiptId") String receiptId) {

        return delegate.consentsRevoke(receiptId );
    }

}
