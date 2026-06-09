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

package org.wso2.carbon.consent.mgt.core.listener;

import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptUpdateInput;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;

/**
 * Abstract base for ConsentManagementListener with no-op defaults and identity.xml-driven enable/disable.
 */
public abstract class AbstractConsentManagementListener implements ConsentManagementListener {

    @Override
    public boolean isEnable() {

        IdentityEventListenerConfig config = IdentityUtil.readEventListenerProperty(
                ConsentManagementListener.class.getName(), this.getClass().getName());
        if (config == null) {
            return true;
        }
        return Boolean.parseBoolean(config.getEnable());
    }

    @Override
    public void preAddPurpose(Purpose purpose, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void postAddPurpose(Purpose purpose, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void preDeletePurpose(String purposeUuid, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void postDeletePurpose(String purposeUuid, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void preAddPurposeVersion(String purposeUuid, PurposeVersion purposeVersion, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void postAddPurposeVersion(String purposeUuid, PurposeVersion purposeVersion, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void preDeletePurposeVersion(String purposeUuid, String versionUuid, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void postDeletePurposeVersion(String purposeUuid, String versionUuid, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void preAddConsent(ReceiptInput receiptInput, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void postAddConsent(ReceiptInput receiptInput, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void preRevokeConsent(String receiptId, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void postRevokeConsent(String receiptId, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void preDeleteConsent(String receiptId, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void postDeleteConsent(String receiptId, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void preSetLatestPurposeVersion(String purposeUUID, String versionLabel, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void postSetLatestPurposeVersion(String purposeUUID, String versionLabel, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void preAuthorizeConsent(String consentId, String userId, String authStatus, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void postAuthorizeConsent(String consentId, String userId, String authStatus, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void preUpdateConsent(ReceiptUpdateInput updateInput, String tenantDomain)
            throws ConsentManagementException {

    }

    @Override
    public void postUpdateConsent(ReceiptUpdateInput updateInput, String tenantDomain)
            throws ConsentManagementException {

    }
}
