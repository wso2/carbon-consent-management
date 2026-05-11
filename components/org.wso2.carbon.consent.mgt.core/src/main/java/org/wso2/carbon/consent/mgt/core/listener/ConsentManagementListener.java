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

/**
 * Listener interface for consent management operations.
 * Implementations are registered as OSGi services and invoked directly by ConsentEventPublisherProxy.
 */
public interface ConsentManagementListener {

    int getDefaultOrderId();

    boolean isEnable();

    void preAddPurpose(Purpose purpose, String tenantDomain)
            throws ConsentManagementException;

    void postAddPurpose(Purpose purpose, String tenantDomain)
            throws ConsentManagementException;

    void preDeletePurpose(String purposeUuid, String tenantDomain)
            throws ConsentManagementException;

    void postDeletePurpose(String purposeUuid, String tenantDomain)
            throws ConsentManagementException;

    void preAddPurposeVersion(String purposeUuid, PurposeVersion purposeVersion, String tenantDomain)
            throws ConsentManagementException;

    void postAddPurposeVersion(String purposeUuid, PurposeVersion purposeVersion, String tenantDomain)
            throws ConsentManagementException;

    void preDeletePurposeVersion(String purposeUuid, String versionUuid, String tenantDomain)
            throws ConsentManagementException;

    void postDeletePurposeVersion(String purposeUuid, String versionUuid, String tenantDomain)
            throws ConsentManagementException;

    void preAddConsent(ReceiptInput receiptInput, String tenantDomain)
            throws ConsentManagementException;

    void postAddConsent(ReceiptInput receiptInput, String tenantDomain)
            throws ConsentManagementException;

    void preRevokeConsent(String receiptId, String tenantDomain)
            throws ConsentManagementException;

    void postRevokeConsent(String receiptId, String tenantDomain)
            throws ConsentManagementException;

    void preDeleteConsent(String receiptId, String tenantDomain)
            throws ConsentManagementException;

    void postDeleteConsent(String receiptId, String tenantDomain)
            throws ConsentManagementException;

    void preSetLatestPurposeVersion(String purposeUUID, String versionLabel, String tenantDomain)
            throws ConsentManagementException;

    void postSetLatestPurposeVersion(String purposeUUID, String versionLabel, String tenantDomain)
            throws ConsentManagementException;

    void preAuthorizeConsent(String consentId, String userId, String authStatus, String tenantDomain)
            throws ConsentManagementException;

    void postAuthorizeConsent(String consentId, String userId, String authStatus, String tenantDomain)
            throws ConsentManagementException;
}
