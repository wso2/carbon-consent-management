package org.wso2.carbon.consent.mgt.endpoint;

import org.wso2.carbon.consent.mgt.endpoint.*;
import org.wso2.carbon.consent.mgt.endpoint.dto.*;

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
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposeListResponseDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ConsentReceiptDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ConsentsApiService {
    public abstract Response consentsGet(Integer limit,Integer offset,String piiPrincipalId,String spTenantDomain,String service);
    public abstract Response consentsPiiCategoriesGet(Integer limit,Integer offset);
    public abstract Response consentsPiiCategoriesPiiCategoryIdDelete(String piiCategoryId);
    public abstract Response consentsPiiCategoriesPiiCategoryIdGet(String piiCategoryId);
    public abstract Response consentsPiiCategoriesPost(PIIcategoryRequestDTO piiCategory);
    public abstract Response consentsPost(ConsentRequestDTO consent);
    public abstract Response consentsPurposeCategoriesGet(Integer limit,Integer offset);
    public abstract Response consentsPurposeCategoriesPost(PurposeCategoryRequestDTO purposeCategory);
    public abstract Response consentsPurposeCategoriesPurposeCategoryIdDelete(String purposeCategoryId);
    public abstract Response consentsPurposeCategoriesPurposeCategoryIdGet(String purposeCategoryId);
    public abstract Response consentsPurposesGet(Integer limit,Integer offset);
    public abstract Response consentsPurposesPost(PurposeRequestDTO purpose);
    public abstract Response consentsPurposesPurposeIdDelete(String purposeId);
    public abstract Response consentsPurposesPurposeIdGet(String purposeId);
    public abstract Response consentsReceiptsReceiptIdDelete(String receiptId);
    public abstract Response consentsReceiptsReceiptIdGet(String receiptId);
}

