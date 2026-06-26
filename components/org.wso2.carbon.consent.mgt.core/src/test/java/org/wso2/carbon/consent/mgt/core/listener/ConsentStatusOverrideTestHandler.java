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

import org.wso2.carbon.consent.mgt.core.model.ConsentAuthorization;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;

import java.util.List;
import java.util.Map;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ACTIVE_STATE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.CONSENT_AUTHORIZATIONS;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.CONSENT_STATUS;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PENDING_STATE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.InterceptorConstants.CALCULATE_CONSENT_STATUS;

/**
 * Test event handler that overrides the computed consent status. It demonstrates the
 * {@code CALCULATE_CONSENT_STATUS} extension point: a "first approval wins" policy that promotes a consent the
 * default rule would leave PENDING to ACTIVE as soon as any party has approved.
 * <p>
 * The override is communicated by replacing the {@code CONSENT_STATUS} property on the event; the publisher reads
 * that value back after dispatch.
 */
public class ConsentStatusOverrideTestHandler extends AbstractEventHandler {

    @Override
    public String getName() {

        return "consentStatusOverrideTestHandler";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleEvent(Event event) throws IdentityEventException {

        if (!CALCULATE_CONSENT_STATUS.equals(event.getEventName())) {
            return;
        }
        Map<String, Object> properties = event.getEventProperties();
        String computedStatus = (String) properties.get(CONSENT_STATUS);
        List<ConsentAuthorization> authorizations =
                (List<ConsentAuthorization>) properties.get(CONSENT_AUTHORIZATIONS);

        if (PENDING_STATE.equals(computedStatus) && hasAnyApproval(authorizations)) {
            properties.put(CONSENT_STATUS, ACTIVE_STATE);
        }
    }

    private boolean hasAnyApproval(List<ConsentAuthorization> authorizations) {

        if (authorizations == null) {
            return false;
        }
        return authorizations.stream().anyMatch(
                a -> ConsentAuthorization.AuthorizationStatus.APPROVED.equals(a.getStatus()));
    }
}
