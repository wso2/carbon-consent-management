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

package org.wso2.carbon.consent.mgt.core.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Input model for updating an existing consent receipt.
 * Each field is optional — null means "leave unchanged".
 * To remove an existing expiry, set {@code clearExpiry} to {@code true} (leaving
 * {@code expiryTime} null); when {@code clearExpiry} is true it takes precedence
 * over {@code expiryTime}.
 */
public class ReceiptUpdateInput {

    private String consentReceiptId;
    private Timestamp expiryTime;
    private boolean clearExpiry;
    private Map<String, String> properties;
    private List<ConsentAuthorization> authorizations;

    public String getConsentReceiptId() {

        return consentReceiptId;
    }

    public void setConsentReceiptId(String consentReceiptId) {

        this.consentReceiptId = consentReceiptId;
    }

    public Timestamp getExpiryTime() {

        return expiryTime;
    }

    public void setExpiryTime(Timestamp expiryTime) {

        this.expiryTime = expiryTime;
    }

    public boolean isClearExpiry() {

        return clearExpiry;
    }

    public void setClearExpiry(boolean clearExpiry) {

        this.clearExpiry = clearExpiry;
    }

    public Map<String, String> getProperties() {

        return properties;
    }

    public void setProperties(Map<String, String> properties) {

        this.properties = properties;
    }

    public List<ConsentAuthorization> getAuthorizations() {

        return authorizations;
    }

    public void setAuthorizations(List<ConsentAuthorization> authorizations) {

        this.authorizations = authorizations;
    }
}
