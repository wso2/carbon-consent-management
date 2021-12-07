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
import org.apache.log4j.MDC;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.PIICategoryValidity;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;
import org.wso2.carbon.consent.mgt.endpoint.dto.AddressDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ConsentReceiptDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ConsentRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ErrorDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PIIcategoryRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PiiCategoryListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PiiCategoryNameListDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PiiControllerDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PropertyDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeCategoryListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeCategoryRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeGetResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposePiiCategoryListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeRequestDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ServiceResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.exception.BadRequestException;
import org.wso2.carbon.consent.mgt.endpoint.exception.ConflictRequestException;
import org.wso2.carbon.consent.mgt.endpoint.exception.ForbiddenException;
import org.wso2.carbon.consent.mgt.endpoint.exception.InternalServerErrorException;
import org.wso2.carbon.consent.mgt.endpoint.exception.NotFoundException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.List;
import java.util.UUID;
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
     * Check whether correlation id present in the log MDC
     *
     * @return whether the correlation id is present
     */
    public static boolean isCorrelationIDPresent() {
        return MDC.get(ConsentConstants.CORRELATION_ID_MDC) != null;
    }

    /**
     * Get correlation id of current thread
     *
     * @return correlation-id
     */
    public static String getCorrelation() {
        String ref = null;
        if (isCorrelationIDPresent()) {
            ref = MDC.get(ConsentConstants.CORRELATION_ID_MDC).toString();
        }
        return ref;
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

    /**
     * This method is used to create a Forbidden Exception with the known errorCode and message.
     *
     * @param description Error Message Description.
     * @param code        Error Code.
     * @return ForbiddenException with the given errorCode and description.
     */
    public static ForbiddenException buildForbiddenException(String description, String code,
                                                                   Log log, Throwable e) {

        ErrorDTO errorDTO = getErrorDTO(ConsentConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, description, code);
        logDebug(log, e);
        return new ForbiddenException(errorDTO);
    }

    public static Purpose getPurposeRequest(PurposeRequestDTO purposeRequestDTO) {

        return new Purpose(purposeRequestDTO.getPurpose(), purposeRequestDTO.getDescription(),
                           purposeRequestDTO.getGroup(), purposeRequestDTO.getGroupType(),
                           purposeRequestDTO.getPiiCategories().stream().map(
                                   purposePiiCategoryRequestDTO -> new PurposePIICategory(
                                           purposePiiCategoryRequestDTO.getPiiCategoryId(),
                                           purposePiiCategoryRequestDTO.getMandatory())).collect(Collectors.toList()));
    }

    public static PurposeCategory getPurposeCategoryRequest(PurposeCategoryRequestDTO purposeCategoryRequestDTO) {

        return new PurposeCategory(purposeCategoryRequestDTO.getPurposeCategory(),
                purposeCategoryRequestDTO.getDescription());
    }

    public static PIICategory getPIICategoryRequest(PIIcategoryRequestDTO piIcategoryRequestDTO) {

        return new PIICategory(piIcategoryRequestDTO.getPiiCategory(), piIcategoryRequestDTO.getDescription(),
                piIcategoryRequestDTO.getSensitive(), piIcategoryRequestDTO.getDisplayName());
    }

    public static PurposeGetResponseDTO getPurposeListResponse(Purpose purposeResponse) {

        PurposeGetResponseDTO purposeListResponseDTO = new PurposeGetResponseDTO();
        purposeListResponseDTO.setPurposeId(purposeResponse.getId());
        purposeListResponseDTO.setPurpose(purposeResponse.getName());
        purposeListResponseDTO.setDescription(purposeResponse.getDescription());
        purposeListResponseDTO.setGroup(purposeResponse.getGroup());
        purposeListResponseDTO.setGroupType(purposeResponse.getGroupType());
        purposeListResponseDTO.setPiiCategories(purposeResponse.getPurposePIICategories().stream().map(piiCategory -> {

            PurposePiiCategoryListResponseDTO purposePiiCategoryListResponseDTO = new
                    PurposePiiCategoryListResponseDTO();
            purposePiiCategoryListResponseDTO.setSensitive(piiCategory.getSensitive());
            purposePiiCategoryListResponseDTO.setMandatory(piiCategory.getMandatory());
            purposePiiCategoryListResponseDTO.setPiiCategory(piiCategory.getName());
            purposePiiCategoryListResponseDTO.setDescription(piiCategory.getDescription());
            purposePiiCategoryListResponseDTO.setPiiCategoryId(piiCategory.getId());
            purposePiiCategoryListResponseDTO.setDisplayName(piiCategory.getDisplayName());
            return purposePiiCategoryListResponseDTO;
        }).collect(Collectors.toList()));
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
        piiCategoryListResponseDTO.setDisplayName(piiCategory.getDisplayName());
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
        errorDTO.setRef(getCorrelation());
        return errorDTO;
    }

    public static List<PurposeListResponseDTO> getPurposeResponseDTOList(List<Purpose> purposes) {

        return purposes.stream()
                .map(purpose -> {
                    PurposeListResponseDTO purposeListResponseDTO = new PurposeListResponseDTO();
                    purposeListResponseDTO.setPurpose(purpose.getName());
                    purposeListResponseDTO.setDescription(purpose.getDescription());
                    purposeListResponseDTO.setPurposeId(purpose.getId());
                    purposeListResponseDTO.setGroup(purpose.getGroup());
                    purposeListResponseDTO.setGroupType(purpose.getGroupType());
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
                    piiCategoryListResponseDTO.setDisplayName(piiCategory.getDisplayName());
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
        receiptInput.setLanguage(consent.getLanguage());
        receiptInput.setPolicyUrl(consent.getPolicyURL());

        receiptInput.setProperties(consent.getProperties().stream().collect(Collectors.toMap(PropertyDTO::getKey,
                PropertyDTO::getValue)));

        receiptInput.setServices(consent.getServices().stream().map(serviceDTO -> {
            ReceiptServiceInput receiptServiceInput = new ReceiptServiceInput();
            receiptServiceInput.setService(serviceDTO.getService());
            receiptServiceInput.setSpDisplayName(serviceDTO.getServiceDisplayName());
            receiptServiceInput.setSpDescription(serviceDTO.getServiceDescription());
            receiptServiceInput.setTenantDomain(serviceDTO.getTenantDomain());
            receiptServiceInput.setPurposes(serviceDTO.getPurposes().stream().map(purposeDTO -> {
                ReceiptPurposeInput receiptPurposeInput = new ReceiptPurposeInput();
                receiptPurposeInput.setConsentType(purposeDTO.getConsentType());
                receiptPurposeInput.setPrimaryPurpose(purposeDTO.getPrimaryPurpose());
                receiptPurposeInput.setPurposeId(purposeDTO.getPurposeId());
                receiptPurposeInput.setTermination(purposeDTO.getTermination());
                receiptPurposeInput.setThirdPartyDisclosure(purposeDTO.getThirdPartyDisclosure());
                receiptPurposeInput.setThirdPartyName(purposeDTO.getThirdPartyName());
                receiptPurposeInput.setPiiCategory(purposeDTO.getPiiCategory().stream().map(piiCategoryListDTO ->
                        new PIICategoryValidity(piiCategoryListDTO.getPiiCategoryId(), piiCategoryListDTO
                                .getValidity(), true)).collect(Collectors.toList()));
                receiptPurposeInput.setPurposeCategoryId(purposeDTO.getPurposeCategoryId());
                return receiptPurposeInput;
            }).collect(Collectors.toList()));
            return receiptServiceInput;
        }).collect(Collectors.toList()));
        return receiptInput;
    }

    /**
     * This API is used to get ConsentReceiptDTO response.
     *
     * @param receipt Receipt instance.
     * @return ConsentReceiptDTO.
     */
    public static ConsentReceiptDTO getConsentReceiptDTO(Receipt receipt) {

        ConsentReceiptDTO consentReceiptDTO = new ConsentReceiptDTO();
        consentReceiptDTO.setCollectionMethod(receipt.getCollectionMethod());
        consentReceiptDTO.setConsentReceiptID(receipt.getConsentReceiptId());
        consentReceiptDTO.setJurisdiction(receipt.getJurisdiction());
        consentReceiptDTO.setConsentTimestamp(receipt.getConsentTimestamp());
        consentReceiptDTO.setLanguage(receipt.getLanguage());
        consentReceiptDTO.setPiiPrincipalId(receipt.getPiiPrincipalId());
        consentReceiptDTO.setPolicyUrl(receipt.getPolicyUrl());
        consentReceiptDTO.setSensitive(receipt.isSensitive());
        consentReceiptDTO.setTenantDomain(receipt.getTenantDomain());
        consentReceiptDTO.setVersion(receipt.getVersion());
        consentReceiptDTO.setState(receipt.getState());
        consentReceiptDTO.setPublicKey(receipt.getPublicKey());
        consentReceiptDTO.setServices(receipt.getServices().stream().map(receiptService -> {
            ServiceResponseDTO serviceResponseDTO = new ServiceResponseDTO();
            serviceResponseDTO.setService(receiptService.getService());
            serviceResponseDTO.setServiceDisplayName(receiptService.getSpDisplayName());
            serviceResponseDTO.setServiceDescription(receiptService.getSpDescription());
            serviceResponseDTO.setTenantDomain(receiptService.getTenantDomain());
            serviceResponseDTO.setPurposes(receiptService.getPurposes().stream().map(consentPurpose -> {
                PurposeResponseDTO purposeResponseDTO = new PurposeResponseDTO();
                purposeResponseDTO.setConsentType(consentPurpose.getConsentType());
                purposeResponseDTO.setPiiCategory(consentPurpose.getPiiCategory().stream().map(piiCategoryValidity -> {
                    PiiCategoryNameListDTO piiCategoryNameListDTO = new PiiCategoryNameListDTO();
                    piiCategoryNameListDTO.setPiiCategoryName(piiCategoryValidity.getName());
                    piiCategoryNameListDTO.setPiiCategoryId(piiCategoryValidity.getId());
                    piiCategoryNameListDTO.setValidity(piiCategoryValidity.getValidity());
                    piiCategoryNameListDTO.setPiiCategoryDisplayName(piiCategoryValidity.getDisplayName());
                    return piiCategoryNameListDTO;
                }).collect(Collectors.toList()));
                purposeResponseDTO.setPrimaryPurpose(consentPurpose.isPrimaryPurpose());
                purposeResponseDTO.setPurpose(consentPurpose.getPurpose());
                purposeResponseDTO.setPurposeId(consentPurpose.getPurposeId());
                purposeResponseDTO.setPurposeCategory(consentPurpose.getPurposeCategory());
                purposeResponseDTO.setTermination(consentPurpose.getTermination());
                purposeResponseDTO.setThirdPartyDisclosure(consentPurpose.isThirdPartyDisclosure());
                purposeResponseDTO.setThirdPartyName(consentPurpose.getThirdPartyName());
                return purposeResponseDTO;
            }).collect(Collectors.toList()));
            return serviceResponseDTO;
        }).collect(Collectors.toList()));
        consentReceiptDTO.setSpiCat(receipt.getSpiCat());
        consentReceiptDTO.setPiiControllers(receipt.getPiiControllers().stream().map(piiController -> {
            PiiControllerDTO piiControllerDTO = new PiiControllerDTO();
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setAddressCountry(piiController.getAddress().getAddressCountry());
            addressDTO.setAddressLocality(piiController.getAddress().getAddressLocality());
            addressDTO.setAddressRegion(piiController.getAddress().getAddressRegion());
            addressDTO.setPostalCode(piiController.getAddress().getPostalCode());
            addressDTO.setPostOfficeBoxNumber(piiController.getAddress().getPostOfficeBoxNumber());
            addressDTO.setStreetAddress(piiController.getAddress().getStreetAddress());
            piiControllerDTO.setAddress(addressDTO);
            piiControllerDTO.setContact(piiController.getContact());
            piiControllerDTO.setEmail(piiController.getEmail());
            piiControllerDTO.setPhone(piiController.getPhone());
            piiControllerDTO.setPiiController(piiController.getPiiController());
            piiControllerDTO.setPiiControllerUrl(piiController.getPiiControllerUrl());
            piiControllerDTO.setOnBehalf(piiController.isOnBehalf());
            return piiControllerDTO;
        }).collect(Collectors.toList()));
        return consentReceiptDTO;
    }
}
