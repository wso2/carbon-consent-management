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

package org.wso2.carbon.consent.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.internal.ConsentManagerComponentDataHolder;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.services.IdentityEventService;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.AUTHZ_STATUS;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_EVENT_PUBLISHING;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_ADD_PURPOSE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_ADD_PURPOSE_VERSION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_ADD_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_AUTHORIZE_CONSENT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_DELETE_PURPOSE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_DELETE_PURPOSE_VERSION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_DELETE_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_REVOKE_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.POST_SET_LATEST_PURPOSE_VERSION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_ADD_PURPOSE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_ADD_PURPOSE_VERSION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_ADD_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_AUTHORIZE_CONSENT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_DELETE_PURPOSE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_DELETE_PURPOSE_VERSION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_DELETE_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_REVOKE_RECEIPT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.PRE_SET_LATEST_PURPOSE_VERSION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PURPOSE_ID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PURPOSE_NAME;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PURPOSE_UUID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PURPOSE_VERSION;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PURPOSE_VERSION_LABEL;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.RECEIPT_ID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.RECEIPT_INPUT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.SET_AS_LATEST;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.VERSION_ID;

/**
 * Publishes IdentityEventService events for purpose and consent management operations.
 */
public class ConsentEventPublisherProxy {

    private static final Log LOG = LogFactory.getLog(ConsentEventPublisherProxy.class);
    private static final ConsentEventPublisherProxy proxy = new ConsentEventPublisherProxy();

    private ConsentEventPublisherProxy() {
    }

    public static ConsentEventPublisherProxy getInstance() {

        return proxy;
    }

    public void publishPreAddPurposeWithException(String purposeUuid, String purposeName, String tenantDomain)
            throws ConsentManagementException {

        Map<String, Object> props = new HashMap<>();
        props.put(PURPOSE_UUID, purposeUuid);
        props.put(PURPOSE_NAME, purposeName);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEventWithException(new Event(PRE_ADD_PURPOSE, props));
    }

    public void publishPostAddPurpose(String purposeUuid, String purposeName, String tenantDomain) {

        Map<String, Object> props = new HashMap<>();
        props.put(PURPOSE_UUID, purposeUuid);
        props.put(PURPOSE_NAME, purposeName);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEvent(new Event(POST_ADD_PURPOSE, props));
    }

    public void publishPreDeletePurposeWithException(String purposeUuid, String tenantDomain)
            throws ConsentManagementException {

        Map<String, Object> props = new HashMap<>();
        props.put(PURPOSE_UUID, purposeUuid);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEventWithException(new Event(PRE_DELETE_PURPOSE, props));
    }

    public void publishPostDeletePurpose(String purposeUuid, String tenantDomain) {

        Map<String, Object> props = new HashMap<>();
        props.put(PURPOSE_UUID, purposeUuid);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEvent(new Event(POST_DELETE_PURPOSE, props));
    }

    public void publishPreAddPurposeVersionWithException(String purposeUuid, PurposeVersion purposeVersion,
                                                         boolean setAsLatest,
                                                         String tenantDomain) throws ConsentManagementException {

        Map<String, Object> props = new HashMap<>();
        props.put(PURPOSE_UUID, purposeUuid);
        props.put(PURPOSE_VERSION, purposeVersion);
        props.put(SET_AS_LATEST, setAsLatest);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEventWithException(new Event(PRE_ADD_PURPOSE_VERSION, props));
    }

    public void publishPostAddPurposeVersion(String purposeUuid, PurposeVersion purposeVersion,
                                             boolean setAsLatest, String tenantDomain) {

        Map<String, Object> props = new HashMap<>();
        props.put(PURPOSE_UUID, purposeUuid);
        props.put(PURPOSE_VERSION, purposeVersion);
        props.put(SET_AS_LATEST, setAsLatest);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEvent(new Event(POST_ADD_PURPOSE_VERSION, props));
    }

    public void publishPreDeletePurposeVersionWithException(String purposeUuid, String versionUuid,
                                                            String tenantDomain) throws ConsentManagementException {

        Map<String, Object> props = new HashMap<>();
        props.put(PURPOSE_UUID, purposeUuid);
        props.put(VERSION_ID, versionUuid);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEventWithException(new Event(PRE_DELETE_PURPOSE_VERSION, props));
    }

    public void publishPostDeletePurposeVersion(String purposeUuid, String versionUuid, String tenantDomain) {

        Map<String, Object> props = new HashMap<>();
        props.put(PURPOSE_UUID, purposeUuid);
        props.put(VERSION_ID, versionUuid);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEvent(new Event(POST_DELETE_PURPOSE_VERSION, props));
    }

    public void publishPreSetLatestPurposeVersionWithException(String purposeUUID, String versionLabel,
                                                               String tenantDomain)
            throws ConsentManagementException {

        Map<String, Object> props = new HashMap<>();
        props.put(PURPOSE_ID, purposeUUID);
        props.put(PURPOSE_VERSION_LABEL, versionLabel);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEventWithException(new Event(PRE_SET_LATEST_PURPOSE_VERSION, props));
    }

    public void publishPostSetLatestPurposeVersion(String purposeUUID, String versionLabel, String tenantDomain) {

        Map<String, Object> props = new HashMap<>();
        props.put(PURPOSE_ID, purposeUUID);
        props.put(PURPOSE_VERSION_LABEL, versionLabel);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEvent(new Event(POST_SET_LATEST_PURPOSE_VERSION, props));
    }

    public void publishPreAuthorizeConsentWithException(String consentId, String userId, String authStatus,
                                                        String tenantDomain)
            throws ConsentManagementException {

        Map<String, Object> props = new HashMap<>();
        props.put(RECEIPT_ID, consentId);
        props.put(IdentityEventConstants.EventProperty.USER_ID, userId);
        props.put(AUTHZ_STATUS, authStatus);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEventWithException(new Event(PRE_AUTHORIZE_CONSENT, props));
    }

    public void publishPostAuthorizeConsent(String consentId, String userId, String authStatus,
                                            String tenantDomain) {

        Map<String, Object> props = new HashMap<>();
        props.put(RECEIPT_ID, consentId);
        props.put(IdentityEventConstants.EventProperty.USER_ID, userId);
        props.put(AUTHZ_STATUS, authStatus);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEvent(new Event(POST_AUTHORIZE_CONSENT, props));
    }

    public void publishPreAddConsentWithException(ReceiptInput receiptInput, String tenantDomain)
            throws ConsentManagementException {

        Map<String, Object> props = new HashMap<>();
        props.put(RECEIPT_INPUT, receiptInput);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEventWithException(new Event(PRE_ADD_RECEIPT, props));
    }

    public void publishPostAddConsent(ReceiptInput receiptInput, String tenantDomain) {

        Map<String, Object> props = new HashMap<>();
        props.put(RECEIPT_INPUT, receiptInput);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEvent(new Event(POST_ADD_RECEIPT, props));
    }

    public void publishPreRevokeConsentWithException(String receiptId, String tenantDomain)
            throws ConsentManagementException {

        Map<String, Object> props = new HashMap<>();
        props.put(RECEIPT_ID, receiptId);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEventWithException(new Event(PRE_REVOKE_RECEIPT, props));
    }

    public void publishPostRevokeConsent(String receiptId, String tenantDomain) {

        Map<String, Object> props = new HashMap<>();
        props.put(RECEIPT_ID, receiptId);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEvent(new Event(POST_REVOKE_RECEIPT, props));
    }

    public void publishPreDeleteConsentWithException(String receiptId, String tenantDomain)
            throws ConsentManagementException {

        Map<String, Object> props = new HashMap<>();
        props.put(RECEIPT_ID, receiptId);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEventWithException(new Event(PRE_DELETE_RECEIPT, props));
    }

    public void publishPostDeleteConsent(String receiptId, String tenantDomain) {

        Map<String, Object> props = new HashMap<>();
        props.put(RECEIPT_ID, receiptId);
        props.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        doPublishEvent(new Event(POST_DELETE_RECEIPT, props));
    }

    private void doPublishEventWithException(Event event) throws ConsentManagementException {

        try {
            IdentityEventService eventService =
                    ConsentManagerComponentDataHolder.getInstance().getIdentityEventService();
            if (eventService != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Publishing event: " + event.getEventName());
                }
                eventService.handleEvent(event);
            }
        } catch (IdentityEventException e) {
            throw new ConsentManagementServerException(
                    String.format(ERROR_CODE_EVENT_PUBLISHING.getMessage(), event.getEventName()),
                    ERROR_CODE_EVENT_PUBLISHING.getCode(), e);
        }
    }

    private void doPublishEvent(Event event) {

        try {
            IdentityEventService eventService =
                    ConsentManagerComponentDataHolder.getInstance().getIdentityEventService();
            if (eventService != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Publishing event: " + event.getEventName());
                }
                eventService.handleEvent(event);
            }
        } catch (IdentityEventException e) {
            LOG.error("Error while publishing event: " + event.getEventName(), e);
        }
    }
}
