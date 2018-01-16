/*
 *
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.consent.mgt.endpoint.util;

import org.apache.commons.logging.Log;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.endpoint.dto.ErrorDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.exception.BadRequestException;
import org.wso2.carbon.consent.mgt.endpoint.exception.ConflictRequestException;
import org.wso2.carbon.consent.mgt.endpoint.exception.InternalServerErrorException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * This class is used to define the utilities require for Consent Management Web App.
 */
public class ConsentEndpointUtils {

    public static ConsentManager getConsentManager() {
        return (ConsentManager) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(ConsentManager.class, null);
    }

    /**
     * This method is used to create an InternalServerErrorException with the known errorCode.
     *
     * @param code Error Code.
     * @return a new InternalServerErrorException with default details.
     */
    public static InternalServerErrorException buildInternalServerErrorException(String code,
                                                                                 Log log, Throwable e) {
        ErrorDTO errorDTO = getErrorDTO(ConsentConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT,
                ConsentConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT, code, log);
        logError(log, e);
        return new InternalServerErrorException(errorDTO);
    }

    /**
     * This method is used to create a BadRequestException with the known errorCode and message.
     *
     * @param description Error Message Desription.
     * @param code        Error Code.
     * @return BadRequestException with the given errorCode and description.
     */
    public static BadRequestException buildBadRequestException(String description, String code,
                                                               Log log, Throwable e) {
        ErrorDTO errorDTO = getErrorDTO(ConsentConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, description, code, log);
        logDebug(log, e);
        return new BadRequestException(errorDTO);
    }

    /**
     * This method is used to create a ConflictRequestException with the known errorCode and message.
     *
     * @param description Error Message Desription.
     * @param code        Error Code.
     * @return ConflictRequestException with the given errorCode and description.
     */
    public static ConflictRequestException buildConflictRequestException(String description, String code,
                                                                         Log log, Throwable e) {
        ErrorDTO errorDTO = getErrorDTO(ConsentConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, description, code, log);
        logDebug(log, e);
        return new ConflictRequestException(errorDTO);
    }

    public static Purpose getPurposeRequest(PurposeRequestDTO purposeRequestDTO) {
        return new Purpose(purposeRequestDTO.getPurpose(), purposeRequestDTO.getDescription());
    }

    public static PurposeListResponseDTO getPurposeListResponse(Purpose purposeResponse) {
        PurposeListResponseDTO purposeListResponseDTO = new PurposeListResponseDTO();
        purposeListResponseDTO.setPurposeId(String.valueOf(purposeResponse.getId()));
        purposeListResponseDTO.setPurpose(purposeResponse.getName());
        purposeListResponseDTO.setDiscripiton(purposeResponse.getDescription());
        return purposeListResponseDTO;
    }

    private static void logError(Log log, Throwable throwable) {
        log.error(throwable.getMessage(), throwable);
    }

    private static void logDebug(Log log, Throwable throwable) {
        if (log.isDebugEnabled()) {
            log.debug(ConsentConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, throwable);
        }
    }

    private static ErrorDTO getErrorDTO(String message, String description, String code, Log log) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(code);
        errorDTO.setMessage(message);
        errorDTO.setDescription(description);
        return errorDTO;
    }

}
