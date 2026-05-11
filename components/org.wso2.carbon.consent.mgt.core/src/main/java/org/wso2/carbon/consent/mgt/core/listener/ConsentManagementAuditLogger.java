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

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.utils.AuditLog;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.jsonObjectToMap;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.triggerAuditLogEvent;

/**
 * Audit logger for consent management operations. Registered as a ConsentManagementListener OSGi service
 * and invoked directly by PrivilegedConsentManagerImpl — no dependency on IdentityEventService.
 */
public class ConsentManagementAuditLogger extends AbstractConsentManagementListener {

    private static final String TARGET_CONSENT = "Consent";
    private static final String TARGET_PURPOSE = "Purpose";
    private static final String TARGET_PURPOSE_VERSION = "PurposeVersion";

    private static final String ACTION_ADD_CONSENT = "add-consent";
    private static final String ACTION_REVOKE_CONSENT = "revoke-consent";
    private static final String ACTION_DELETE_CONSENT = "delete-consent";
    private static final String ACTION_AUTHORIZE_CONSENT = "authorize-consent";
    private static final String ACTION_ADD_PURPOSE = "add-purpose";
    private static final String ACTION_DELETE_PURPOSE = "delete-purpose";
    private static final String ACTION_ADD_PURPOSE_VERSION = "add-purpose-version";
    private static final String ACTION_DELETE_PURPOSE_VERSION = "delete-purpose-version";
    private static final String ACTION_SET_LATEST_PURPOSE_VERSION = "set-latest-purpose-version";

    private static final String DATA_NAME = "name";
    private static final String DATA_PURPOSE_ID = "purposeId";
    private static final String DATA_VERSION_LABEL = "versionLabel";
    private static final String DATA_PII_PRINCIPAL_ID = "piiPrincipalId";
    private static final String DATA_STATE = "state";
    private static final String DATA_SERVICE_NAMES = "serviceNames";
    private static final String DATA_PURPOSE_NAMES = "purposeNames";
    private static final String DATA_USER_ID = "userId";
    private static final String DATA_AUTH_STATUS = "authStatus";

    @Override
    public int getDefaultOrderId() {

        return 3;
    }

    @Override
    public boolean isEnable() {

        return true;
    }

    @Override
    public void postAddPurpose(String purposeUuid, String purposeName, String tenantDomain) {

        JSONObject data = new JSONObject();
        if (StringUtils.isNotBlank(purposeName)) {
            data.put(DATA_NAME, purposeName);
        }
        buildAuditLog(purposeUuid, TARGET_PURPOSE, ACTION_ADD_PURPOSE, data);
    }

    @Override
    public void postDeletePurpose(String purposeUuid, String tenantDomain) {

        buildAuditLog(purposeUuid, TARGET_PURPOSE, ACTION_DELETE_PURPOSE, null);
    }

    @Override
    public void postAddPurposeVersion(String purposeUuid, PurposeVersion purposeVersion, String tenantDomain) {

        JSONObject data = new JSONObject();
        data.put(DATA_PURPOSE_ID, purposeUuid);
        if (purposeVersion != null) {
            data.put(DATA_VERSION_LABEL, purposeVersion.getVersion());
        }
        String versionUuid = purposeVersion != null ? purposeVersion.getUuid() : purposeUuid;
        buildAuditLog(versionUuid, TARGET_PURPOSE_VERSION, ACTION_ADD_PURPOSE_VERSION, data);
    }

    @Override
    public void postDeletePurposeVersion(String purposeUuid, String versionUuid, String tenantDomain) {

        JSONObject data = new JSONObject();
        data.put(DATA_PURPOSE_ID, purposeUuid);
        buildAuditLog(versionUuid, TARGET_PURPOSE_VERSION, ACTION_DELETE_PURPOSE_VERSION, data);
    }

    @Override
    public void postAddConsent(ReceiptInput receiptInput, String tenantDomain) {

        if (receiptInput == null) {
            return;
        }
        JSONObject data = new JSONObject();
        data.put(DATA_PII_PRINCIPAL_ID, LoggerUtils.getMaskedContent(receiptInput.getPiiPrincipalId()));
        data.put(DATA_STATE, receiptInput.getState());
        if (receiptInput.getServices() != null) {
            JSONArray serviceNames = extractServiceNames(receiptInput);
            if (serviceNames.length() > 0) {
                data.put(DATA_SERVICE_NAMES, serviceNames);
            }
            JSONArray purposeNames = extractPurposeNames(receiptInput);
            if (purposeNames.length() > 0) {
                data.put(DATA_PURPOSE_NAMES, purposeNames);
            }
        }
        buildAuditLog(receiptInput.getConsentReceiptId(), TARGET_CONSENT, ACTION_ADD_CONSENT, data);
    }

    @Override
    public void postRevokeConsent(String receiptId, String tenantDomain) {

        buildAuditLog(receiptId, TARGET_CONSENT, ACTION_REVOKE_CONSENT, null);
    }

    @Override
    public void postDeleteConsent(String receiptId, String tenantDomain) {

        buildAuditLog(receiptId, TARGET_CONSENT, ACTION_DELETE_CONSENT, null);
    }

    @Override
    public void postSetLatestPurposeVersion(String purposeUUID, String versionLabel, String tenantDomain) {

        JSONObject data = new JSONObject();
        data.put(DATA_VERSION_LABEL, versionLabel);
        buildAuditLog(purposeUUID, TARGET_PURPOSE, ACTION_SET_LATEST_PURPOSE_VERSION, data);
    }

    @Override
    public void postAuthorizeConsent(String consentId, String userId, String authStatus, String tenantDomain) {

        JSONObject data = new JSONObject();
        if (StringUtils.isNotBlank(userId)) {
            data.put(DATA_USER_ID, LoggerUtils.getMaskedContent(userId));
        }
        if (StringUtils.isNotBlank(authStatus)) {
            data.put(DATA_AUTH_STATUS, authStatus);
        }
        buildAuditLog(consentId, TARGET_CONSENT, ACTION_AUTHORIZE_CONSENT, data);
    }

    private void buildAuditLog(String targetId, String targetType, String action,
                                                    JSONObject data) {

        String initiatorId = getInitiatorId();
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                initiatorId,
                LoggerUtils.getInitiatorType(initiatorId),
                targetId,
                targetType,
                action);
        if (data != null && data.length() > 0) {
            auditLogBuilder.data(jsonObjectToMap(data));
        }
        triggerAuditLogEvent(auditLogBuilder);
    }

    private static String getInitiatorId() {

        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (StringUtils.isBlank(username)) {
            return LoggerUtils.Initiator.System.name();
        }
        return LoggerUtils.getMaskedContent(username + "@" + tenantDomain);
    }

    private JSONArray extractServiceNames(ReceiptInput receiptInput) {

        Set<String> serviceNames = new LinkedHashSet<>();
        for (ReceiptServiceInput serviceInput : receiptInput.getServices()) {
            if (serviceInput != null && StringUtils.isNotBlank(serviceInput.getService())) {
                serviceNames.add(serviceInput.getService());
            }
        }
        return new JSONArray(serviceNames);
    }

    private JSONArray extractPurposeNames(ReceiptInput receiptInput) {

        Set<String> purposeNames = new LinkedHashSet<>();
        for (ReceiptServiceInput serviceInput : receiptInput.getServices()) {
            if (serviceInput == null || serviceInput.getPurposes() == null) {
                continue;
            }
            for (ReceiptPurposeInput purposeInput : serviceInput.getPurposes()) {
                if (purposeInput != null && StringUtils.isNotBlank(purposeInput.getPurposeName())) {
                    purposeNames.add(purposeInput.getPurposeName());
                }
            }
        }
        return new JSONArray(purposeNames);
    }
}
