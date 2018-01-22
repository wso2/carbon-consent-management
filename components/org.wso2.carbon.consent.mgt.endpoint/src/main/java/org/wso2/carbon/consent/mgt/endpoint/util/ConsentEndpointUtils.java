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
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;
import org.wso2.carbon.consent.mgt.endpoint.dto.ConsentRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ErrorDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PIIcategoryRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PiiCategoryListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeCategoryListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeCategoryRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.exception.BadRequestException;
import org.wso2.carbon.consent.mgt.endpoint.exception.ConflictRequestException;
import org.wso2.carbon.consent.mgt.endpoint.exception.InternalServerErrorException;
import org.wso2.carbon.consent.mgt.endpoint.exception.NotFoundException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                ConsentConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT, code);
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

        ErrorDTO errorDTO = getErrorDTO(ConsentConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, description, code);
        logDebug(log, e);
        return new BadRequestException(errorDTO);
    }

    /**
     * This method is used to create a ConflictRequestException with the known errorCode and message.
     *
     * @param description Error Message Description.
     * @param code        Error Code.
     * @return ConflictRequestException with the given errorCode and description.
     */
    public static ConflictRequestException buildConflictRequestException(String description, String code,
                                                                         Log log, Throwable e) {

        ErrorDTO errorDTO = getErrorDTO(ConsentConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, description, code);
        logDebug(log, e);
        return new ConflictRequestException(errorDTO);
    }

    /**
     * This method is used to create a NotFoundException with the known errorCode and message.
     *
     * @param description Error Message Description.
     * @param code        Error Code.
     * @return NotFoundException with the given errorCode and description.
     */
    public static NotFoundException buildNotFoundRequestException(String description, String code,
                                                                  Log log, Throwable e) {

        ErrorDTO errorDTO = getErrorDTO(ConsentConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, description, code);
        logDebug(log, e);
        return new NotFoundException(errorDTO);
    }

    public static Purpose getPurposeRequest(PurposeRequestDTO purposeRequestDTO) {

        return new Purpose(purposeRequestDTO.getPurpose(), purposeRequestDTO.getDescription());
    }

    public static PurposeCategory getPurposeCategoryRequest(PurposeCategoryRequestDTO purposeCategoryRequestDTO) {

        return new PurposeCategory(purposeCategoryRequestDTO.getPurposeCategory(),
                purposeCategoryRequestDTO.getDescription());
    }

    public static PIICategory getPIICategoryRequest(PIIcategoryRequestDTO piIcategoryRequestDTO) {

        return new PIICategory(piIcategoryRequestDTO.getPiiCategory(),
                piIcategoryRequestDTO.getDescription(), piIcategoryRequestDTO.getSensitive());
    }

    public static PurposeListResponseDTO getPurposeListResponse(Purpose purposeResponse) {

        PurposeListResponseDTO purposeListResponseDTO = new PurposeListResponseDTO();
        purposeListResponseDTO.setPurposeId(purposeResponse.getId());
        purposeListResponseDTO.setPurpose(purposeResponse.getName());
        purposeListResponseDTO.setDescription(purposeResponse.getDescription());
        return purposeListResponseDTO;
    }

    public static PurposeCategoryListResponseDTO getPurposeCategoryListResponse(PurposeCategory purposeCategory) {

        PurposeCategoryListResponseDTO purposeCategoryListResponseDTO = new PurposeCategoryListResponseDTO();
        purposeCategoryListResponseDTO.setPurposeCategoryId(purposeCategory.getId());
        purposeCategoryListResponseDTO.setPurposeCategory(purposeCategory.getName());
        purposeCategoryListResponseDTO.setDescription(purposeCategory.getDescription());
        return purposeCategoryListResponseDTO;
    }

    public static PiiCategoryListResponseDTO getPiiCategoryListResponse(PIICategory piiCategory) {

        PiiCategoryListResponseDTO piiCategoryListResponseDTO = new PiiCategoryListResponseDTO();
        piiCategoryListResponseDTO.setPiiCategoryId(piiCategory.getId());
        piiCategoryListResponseDTO.setPiiCategory(piiCategory.getName());
        piiCategoryListResponseDTO.setDescription(piiCategory.getDescription());
        piiCategoryListResponseDTO.setSensitive(piiCategory.getSensitive());
        return piiCategoryListResponseDTO;
    }

    private static void logError(Log log, Throwable throwable) {

        log.error(throwable.getMessage(), throwable);
    }

    private static void logDebug(Log log, Throwable throwable) {

        if (log.isDebugEnabled()) {
            log.debug(ConsentConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, throwable);
        }
    }

    private static ErrorDTO getErrorDTO(String message, String description, String code) {

        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(code);
        errorDTO.setMessage(message);
        errorDTO.setDescription(description);
        return errorDTO;
    }

    public static List<PurposeListResponseDTO> getPurposeResponseDTOList(List<Purpose> purposes) {

        return purposes.stream()
                .map(purpose -> {
                    PurposeListResponseDTO purposeListResponseDTO = new PurposeListResponseDTO();
                    purposeListResponseDTO.setPurpose(purpose.getName());
                    purposeListResponseDTO.setDescription(purpose.getDescription());
                    purposeListResponseDTO.setPurposeId(purpose.getId());
                    return purposeListResponseDTO;
                })
                .collect(Collectors.toList());
    }

    public static List<PurposeCategoryListResponseDTO> getPurposeCategoryResponseDTOList(List<PurposeCategory>
                                                                                                 purposeCategories) {

        return purposeCategories.stream()
                .map(purposeCategory -> {
                    PurposeCategoryListResponseDTO purposeCategoryListResponseDTO = new PurposeCategoryListResponseDTO();
                    purposeCategoryListResponseDTO.setPurposeCategory(purposeCategory.getName());
                    purposeCategoryListResponseDTO.setDescription(purposeCategory.getDescription());
                    purposeCategoryListResponseDTO.setPurposeCategoryId(purposeCategory.getId());
                    return purposeCategoryListResponseDTO;
                })
                .collect(Collectors.toList());
    }

    public static List<PiiCategoryListResponseDTO> getPiiCategoryResponseDTOList(List<PIICategory> piiCategories) {

        return piiCategories.stream()
                .map(piiCategory -> {
                    PiiCategoryListResponseDTO piiCategoryListResponseDTO = new PiiCategoryListResponseDTO();
                    piiCategoryListResponseDTO.setPiiCategory(piiCategory.getName());
                    piiCategoryListResponseDTO.setDescription(piiCategory.getDescription());
                    piiCategoryListResponseDTO.setPiiCategoryId(piiCategory.getId());
                    piiCategoryListResponseDTO.setSensitive(piiCategory.getSensitive());
                    return piiCategoryListResponseDTO;
                })
                .collect(Collectors.toList());
    }

    /**
     * This Util is used to Get ReceiptInput instance from ConsentRequestDTO instance.
     *
     * @param consent ConsentRequestDTO instance.
     * @return ReceiptInput instance.
     */
    public static ReceiptInput getReceiptInput(ConsentRequestDTO consent) {

        ReceiptInput receiptInput = new ReceiptInput();
        receiptInput.setCollectionMethod(consent.getCollectionMethod());
        receiptInput.setJurisdiction(consent.getJurisdiction());
        receiptInput.setPiiPrincipalId(consent.getPiiPrincipalId());
        receiptInput.setLanguage(consent.getLanguage());
        receiptInput.setPolicyUrl(consent.getPolicyURL());

        Map<String, String> properties = new HashMap<>();
        consent.getProperties().forEach(propertyDTO -> {
            properties.put(propertyDTO.getKey(), propertyDTO.getValue());
        });
        receiptInput.setProperties(properties);

        receiptInput.setServices(consent.getServices().stream().map(serviceDTO -> {
            ReceiptServiceInput receiptServiceInput = new ReceiptServiceInput();
            receiptServiceInput.setService(serviceDTO.getService());
            receiptServiceInput.setTenantDomain(serviceDTO.getTenantDomain());
            receiptServiceInput.setPurposes(serviceDTO.getPurposes().stream().map(purposeDTO -> {
                ReceiptPurposeInput receiptPurposeInput = new ReceiptPurposeInput();
                receiptPurposeInput.setConsentType(purposeDTO.getConsentType());
                receiptPurposeInput.setPrimaryPurpose(purposeDTO.getPrimaryPurpose());
                receiptPurposeInput.setPurposeId(purposeDTO.getPurposeId());
                receiptPurposeInput.setTermination(purposeDTO.getTermination());
                receiptPurposeInput.setThirdPartyDisclosure(purposeDTO.getThirdPartyDisclosure());
                receiptPurposeInput.setThirdPartyName(purposeDTO.getThirdPartyName());
                receiptPurposeInput.setPiiCategoryId(purposeDTO.getPiiCategoryId());
                receiptPurposeInput.setPurposeCategoryId(purposeDTO.getPurposeCategoryId());
                return receiptPurposeInput;
            }).collect(Collectors.toList()));
            return receiptServiceInput;
        }).collect(Collectors.toList()));
        return receiptInput;
    }
}
