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

package org.wso2.carbon.consent.mgt.endpoint.v2.util;

import org.apache.commons.logging.Log;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ErrorDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_NO_USER_FOUND;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_IS_ASSOCIATED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_ALREADY_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_IS_ASSOCIATED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_VERSION_ALREADY_EXISTS;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_VERSION_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_VERSION_NOT_FOUND;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_RECEIPT_ID_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_USER_NOT_AUTHORIZED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CATEGORY_ALREADY_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CATEGORY_ALREADY_EXIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.STATUS_INTERNAL_SERVER_ERROR_DESCRIPTION_DEFAULT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT;

/**
 * Utility class for the V2 Consent Management REST API.
 */
public class ConsentV2EndpointUtils {

    private static final Set<String> NOT_FOUND_CODES = new HashSet<>(Arrays.asList(
            ERROR_CODE_PURPOSE_ID_INVALID.getCode(),
            ERROR_CODE_PURPOSE_CATEGORY_ID_INVALID.getCode(),
            ERROR_CODE_PII_CATEGORY_ID_INVALID.getCode(),
            ERROR_CODE_RECEIPT_ID_INVALID.getCode(),
            ERROR_CODE_PURPOSE_VERSION_ID_INVALID.getCode(),
            ERROR_CODE_PURPOSE_VERSION_NOT_FOUND.getCode()
    ));

    private static final Set<String> CONFLICT_CODES = new HashSet<>(Arrays.asList(
            ERROR_CODE_PURPOSE_ALREADY_EXIST.getCode(),
            ERROR_CODE_PII_CATEGORY_ALREADY_EXIST.getCode(),
            ERROR_CODE_PURPOSE_CATEGORY_ALREADY_EXIST.getCode(),
            ERROR_CODE_PURPOSE_VERSION_ALREADY_EXISTS.getCode(),
            ERROR_CODE_PURPOSE_IS_ASSOCIATED.getCode(),
            ERROR_CODE_PII_CATEGORY_IS_ASSOCIATED.getCode()
    ));

    private static final Set<String> FORBIDDEN_CODES = new HashSet<>(Arrays.asList(
            ERROR_CODE_NO_USER_FOUND.getCode(),
            ERROR_CODE_USER_NOT_AUTHORIZED.getCode()
    ));

    private ConsentV2EndpointUtils() {
    }

    /**
     * Retrieves the ConsentManager OSGi service from the Carbon context.
     *
     * @return ConsentManager instance.
     */
    public static ConsentManager getConsentManager() {

        return (ConsentManager) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(ConsentManager.class, null);
    }

    /**
     * Handles a ConsentManagementClientException and returns an appropriate JAX-RS Response.
     *
     * @param e   Client exception.
     * @param log Logger.
     * @return HTTP Response with error body.
     */
    public static Response handleBadRequestResponse(ConsentManagementClientException e, Log log) {

        String code = e.getErrorCode();
        ErrorDTO errorDTO = buildErrorDTO(code, STATUS_BAD_REQUEST_MESSAGE_DEFAULT, e.getMessage());

        if (NOT_FOUND_CODES.contains(code)) {
            return Response.status(Response.Status.NOT_FOUND).entity(errorDTO).build();
        } else if (CONFLICT_CODES.contains(code)) {
            return Response.status(Response.Status.CONFLICT).entity(errorDTO).build();
        } else if (FORBIDDEN_CODES.contains(code)) {
            return Response.status(Response.Status.FORBIDDEN).entity(errorDTO).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
    }

    /**
     * Handles a ConsentManagementException (server error) and returns a 500 response.
     *
     * @param e   Server exception.
     * @param log Logger.
     * @return HTTP 500 Response.
     */
    public static Response handleServerErrorResponse(ConsentManagementException e, Log log) {

        log.error("Server error: ", e);
        ErrorDTO errorDTO = buildErrorDTO(e.getErrorCode(), STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT,
                STATUS_INTERNAL_SERVER_ERROR_DESCRIPTION_DEFAULT);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
    }

    /**
     * Handles an unexpected throwable and returns a 500 response.
     *
     * @param t   Throwable.
     * @param log Logger.
     * @return HTTP 500 Response.
     */
    public static Response handleUnexpectedServerError(Throwable t, Log log) {

        log.error("Unexpected error: " + t.getMessage(), t);
        ErrorDTO errorDTO = buildErrorDTO("CM_00000", STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT,
                STATUS_INTERNAL_SERVER_ERROR_DESCRIPTION_DEFAULT);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
    }

    private static ErrorDTO buildErrorDTO(String code, String message, String description) {

        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(code);
        errorDTO.setMessage(message);
        errorDTO.setDescription(description);
        errorDTO.setTraceId(UUID.randomUUID().toString()); // TODO: need a proper traceId
        return errorDTO;
    }
}
