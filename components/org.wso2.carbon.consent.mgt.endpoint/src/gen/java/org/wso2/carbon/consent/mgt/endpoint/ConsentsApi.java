package org.wso2.carbon.consent.mgt.endpoint;

import org.wso2.carbon.consent.mgt.endpoint.dto.*;
import org.wso2.carbon.consent.mgt.endpoint.ConsentsApiService;
import org.wso2.carbon.consent.mgt.endpoint.factories.ConsentsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.consent.mgt.endpoint.dto.ErrorDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ConsentResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PiiCategoriesDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PiiCategoryListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PIIcategoryRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ConsentRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ConsentAddResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurpseCategoriesDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeCategoryListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeCategoryRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurpsesDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeGetResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ConsentReceiptDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/consents")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/consents", description = "the consents API")
public class ConsentsApi  {

   private final ConsentsApiService delegate = ConsentsApiServiceFactory.getConsentsApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "List Consents\n", notes = "This API is used to list consents elements based on the filetered attributes.\n", response = ConsentResponseDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsGet(@ApiParam(value = "Number of search results.") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Start index of the search.") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Subject identifier") @QueryParam("piiPrincipalId")  String piiPrincipalId,
    @ApiParam(value = "Service provider tenant domain") @QueryParam("spTenantDomain")  String spTenantDomain,
    @ApiParam(value = "Service name") @QueryParam("service")  String service,
    @ApiParam(value = "State Ex. ACTIVE/REVOKED") @QueryParam("state")  String state)
    {
    return delegate.consentsGet(limit,offset,piiPrincipalId,spTenantDomain,service,state);
    }
    @GET
    @Path("/pii-categories")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve all PII categories\n", notes = "This API is used to get  all PII categories.\n", response = PiiCategoriesDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsPiiCategoriesGet(@ApiParam(value = "Number of search results.") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Start index of the search.") @QueryParam("offset")  Integer offset)
    {
    return delegate.consentsPiiCategoriesGet(limit,offset);
    }
    @DELETE
    @Path("/pii-categories/{piiCategoryId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete PII category\n", notes = "This API is used to delete a PII  category.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsPiiCategoriesPiiCategoryIdDelete(@ApiParam(value = "Id of the pii category",required=true ) @PathParam("piiCategoryId")  String piiCategoryId)
    {
    return delegate.consentsPiiCategoriesPiiCategoryIdDelete(piiCategoryId);
    }
    @GET
    @Path("/pii-categories/{piiCategoryId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve a pii category\n", notes = "This API is used to get PII category from the pii category Id.\n", response = PiiCategoryListResponseDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsPiiCategoriesPiiCategoryIdGet(@ApiParam(value = "Id of the pii category",required=true ) @PathParam("piiCategoryId")  String piiCategoryId)
    {
    return delegate.consentsPiiCategoriesPiiCategoryIdGet(piiCategoryId);
    }
    @POST
    @Path("/pii-categories")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add PII category\n", notes = "This API is used to add new PII category for the consent management.\n", response = PiiCategoryListResponseDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successful response"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsPiiCategoriesPost(@ApiParam(value = "This represents the PII catogory element need to be stored" ,required=true ) PIIcategoryRequestDTO piiCategory)
    {
    return delegate.consentsPiiCategoriesPost(piiCategory);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add Consents\n", notes = "This API is used to storing consent information given by the users.\n", response = ConsentAddResponseDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successful response"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsPost(@ApiParam(value = "This represents the consent element need to be stored" ,required=true ) ConsentRequestDTO consent)
    {
    return delegate.consentsPost(consent);
    }
    @GET
    @Path("/purpose-categories")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve all purposes categories\n", notes = "This API is used to get  all purposes categories.\n", response = PurpseCategoriesDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsPurposeCategoriesGet(@ApiParam(value = "Number of search results.") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Start index of the search.") @QueryParam("offset")  Integer offset)
    {
    return delegate.consentsPurposeCategoriesGet(limit,offset);
    }
    @POST
    @Path("/purpose-categories")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add Purpose category\n", notes = "This API is used to add new purpose category for the consent management.\n", response = PurposeCategoryListResponseDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successful response"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsPurposeCategoriesPost(@ApiParam(value = "This represents the purpose catogory element need to be stored" ,required=true ) PurposeCategoryRequestDTO purposeCategory)
    {
    return delegate.consentsPurposeCategoriesPost(purposeCategory);
    }
    @DELETE
    @Path("/purpose-categories/{purposeCategoryId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete purpose category\n", notes = "This API is used to delete a purpose category.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsPurposeCategoriesPurposeCategoryIdDelete(@ApiParam(value = "Id of the purpose category",required=true ) @PathParam("purposeCategoryId")  String purposeCategoryId)
    {
    return delegate.consentsPurposeCategoriesPurposeCategoryIdDelete(purposeCategoryId);
    }
    @GET
    @Path("/purpose-categories/{purposeCategoryId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve a purpose category\n", notes = "This API is used to get Purpose category from the purpose category Id.\n", response = PurposeCategoryListResponseDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsPurposeCategoriesPurposeCategoryIdGet(@ApiParam(value = "Id of the purpose category",required=true ) @PathParam("purposeCategoryId")  String purposeCategoryId)
    {
    return delegate.consentsPurposeCategoriesPurposeCategoryIdGet(purposeCategoryId);
    }
    @GET
    @Path("/purposes")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve all purposes\n", notes = "This API is used to get  all Purposes.\n", response = PurpsesDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsPurposesGet(@ApiParam(value = "Number of search results.") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Start index of the search.") @QueryParam("offset")  Integer offset)
    {
    return delegate.consentsPurposesGet(limit,offset);
    }
    @POST
    @Path("/purposes")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add Purpose\n", notes = "This API is used to add new purposes for the consent management.\n", response = PurposeGetResponseDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successful response"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsPurposesPost(@ApiParam(value = "This represents the purpose element need to be stored" ,required=true ) PurposeRequestDTO purpose)
    {
    return delegate.consentsPurposesPost(purpose);
    }
    @DELETE
    @Path("/purposes/{purposeId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete Purpose\n", notes = "This API is used to delete a purpose.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsPurposesPurposeIdDelete(@ApiParam(value = "Id of the purpose",required=true ) @PathParam("purposeId")  String purposeId)
    {
    return delegate.consentsPurposesPurposeIdDelete(purposeId);
    }
    @GET
    @Path("/purposes/{purposeId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve a purpose\n", notes = "This API is used to get Purpose from the purpose Id.\n", response = PurposeGetResponseDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsPurposesPurposeIdGet(@ApiParam(value = "Id of the purpose",required=true ) @PathParam("purposeId")  String purposeId)
    {
    return delegate.consentsPurposesPurposeIdGet(purposeId);
    }
    @DELETE
    @Path("/receipts/{receiptId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Revoke Consents\n", notes = "This API is used to revoke consent.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsReceiptsReceiptIdDelete(@ApiParam(value = "This represents the Revoke Receipt Id.",required=true ) @PathParam("receiptId")  String receiptId)
    {
    return delegate.consentsReceiptsReceiptIdDelete(receiptId);
    }
    @GET
    @Path("/receipts/{receiptId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve a consent Receipt\n", notes = "This API is used to get consent from the conset receipt Id.\n", response = ConsentReceiptDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response consentsReceiptsReceiptIdGet(@ApiParam(value = "The unique identifier of an receipt.",required=true ) @PathParam("receiptId")  String receiptId)
    {
    return delegate.consentsReceiptsReceiptIdGet(receiptId);
    }
}

