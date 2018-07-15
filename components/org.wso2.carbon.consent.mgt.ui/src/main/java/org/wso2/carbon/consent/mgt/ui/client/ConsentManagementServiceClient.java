/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.consent.mgt.ui.client;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.util.LambdaExceptionUtils;
import org.wso2.carbon.consent.mgt.ui.dto.PurposeRequestDTO;
import org.wso2.carbon.consent.mgt.ui.internal.ConsentManagementUIServiceDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.wso2.carbon.CarbonConstants.UI_PERMISSION_ACTION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_NO_AUTH_USER_FOUND;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_UNEXPECTED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_USER_NOT_AUTHORIZED;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PERMISSION_CONSENT_MGT_ADD;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PERMISSION_CONSENT_MGT_DELETE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PERMISSION_CONSENT_MGT_LIST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PERMISSION_CONSENT_MGT_VIEW;

/**
 * Consent API OSGI client.
 */
public class ConsentManagementServiceClient {

    private String loggedInUser;
    public ConsentManagementServiceClient(String loggedInUser) {

        this.loggedInUser = loggedInUser;
    }

    public Purpose[] listPurposes() throws ConsentManagementException {

        List<Purpose> purposes = getConsentManager().listPurposes(0, 0);
        return purposes.toArray(new Purpose[purposes.size()]);
    }


    public Purpose[] listPurposes(String purposeGroupName , String purposeGroupType) throws ConsentManagementException {

        List<Purpose> purposes = getConsentManager().listPurposes(purposeGroupName, purposeGroupType, 0, 0);
        return purposes.toArray(new Purpose[purposes.size()]);
    }
    public Purpose getPurpose(int purposeId) throws ConsentManagementException {

        return getConsentManager().getPurpose(purposeId);
    }

    public void deletePurpose(int purposeId) throws ConsentManagementException {

        handleLoggedInUserAuthorization(PERMISSION_CONSENT_MGT_DELETE);
        getConsentManager().deletePurpose(purposeId);
    }

    public void deletePurposeByName(String purposeName) throws ConsentManagementException {

        handleLoggedInUserAuthorization(PERMISSION_CONSENT_MGT_DELETE);
        Purpose purposeByName = getConsentManager().getPurposeByName(purposeName);
        deletePurpose(purposeByName.getId());
    }

    public void addPurpose(PurposeRequestDTO purposeRequestDTO) throws ConsentManagementException {

        handleLoggedInUserAuthorization(PERMISSION_CONSENT_MGT_ADD);
        Purpose purpose = new Purpose(purposeRequestDTO.getPurpose(), purposeRequestDTO.getDescription(),
                                      purposeRequestDTO.getGroup(), purposeRequestDTO.getGroupType(),
                                      purposeRequestDTO.isMandatory());
        List<PurposePIICategory> piiCategories = new ArrayList<>();
        purposeRequestDTO.getPiiCategories().forEach(LambdaExceptionUtils.rethrowConsumer(piiCategoryDTO -> {
            if (getConsentManager().isPIICategoryExists(piiCategoryDTO.getName())) {
                PIICategory piiCategoryId = getConsentManager().getPIICategoryByName(piiCategoryDTO.getName());

                boolean isPiiCategoryAdded = false;
                for (PurposePIICategory purposePIICategory : piiCategories) {
                    if (purposePIICategory.getId().equals(piiCategoryId.getId())) {
                        isPiiCategoryAdded = true;
                        break;
                    }
                }
                if (!isPiiCategoryAdded) {
                    piiCategories.add(new PurposePIICategory(piiCategoryId.getId(), piiCategoryDTO.isMandatory()));
                }
            } else {
                PIICategory piiCategory = new PIICategory(piiCategoryDTO.getName(), piiCategoryDTO.getDescription(),
                        true, piiCategoryDTO.getDisplayName());
                PIICategory piiCategoryResponse = getConsentManager().addPIICategory(piiCategory);
                boolean isPiiCategoryAdded = false;
                for (PurposePIICategory purposePIICategory : piiCategories) {
                    if (purposePIICategory.getId().equals(piiCategoryResponse.getId())) {
                        isPiiCategoryAdded = true;
                        break;
                    }
                }
                if (!isPiiCategoryAdded) {
                    piiCategories.add(new PurposePIICategory(piiCategoryResponse.getId(), piiCategoryDTO.isMandatory()));
                }
            }
        }));
        purpose.setPurposePIICategories(piiCategories);
        getConsentManager().addPurpose(purpose);
    }

    /**
     * This is used to handle the authorization. Authentication supports in rest API via a tomcat valve.
     *
     * @param permission permission string.
     * @throws ConsentManagementException Consent Management Exception.
     */
    private void handleLoggedInUserAuthorization(String permission) throws
            ConsentManagementException {

        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

            if (StringUtils.isBlank(loggedInUser)) {
                throw new ConsentManagementException(ERROR_CODE_NO_AUTH_USER_FOUND.getMessage(),
                        ERROR_CODE_NO_AUTH_USER_FOUND.getCode());
            }
            AuthorizationManager authorizationManager = ConsentManagementUIServiceDataHolder.getInstance().getRealmService()
                    .getTenantUserRealm(tenantId)
                    .getAuthorizationManager();
            if (!authorizationManager.isUserAuthorized(loggedInUser, permission, UI_PERMISSION_ACTION)) {
                throw new ConsentManagementException(ERROR_CODE_USER_NOT_AUTHORIZED.getMessage(),
                        ERROR_CODE_USER_NOT_AUTHORIZED.getCode());
            }
        } catch (UserStoreException e) {
            throw new ConsentManagementException(ERROR_CODE_UNEXPECTED.getMessage(),
                    ERROR_CODE_UNEXPECTED.getCode());
        }
    }

    private ConsentManager getConsentManager() {

        return ConsentManagementUIServiceDataHolder.getInstance().getConsentManager();
    }
}
