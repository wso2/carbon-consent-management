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

/**
 * Represents an authorization record for a consent receipt.
 */
public class ConsentAuthorization {

    private String consentReceiptId;
    private String userId;
    private String status;
    private long updatedTime;

    public ConsentAuthorization() {
    }

    public ConsentAuthorization(String consentReceiptId, String userId, String status, long updatedTime) {

        this.consentReceiptId = consentReceiptId;
        this.userId = userId;
        this.status = status;
        this.updatedTime = updatedTime;
    }

    public String getConsentReceiptId() {

        return consentReceiptId;
    }

    public void setConsentReceiptId(String consentReceiptId) {

        this.consentReceiptId = consentReceiptId;
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public long getUpdatedTime() {

        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {

        this.updatedTime = updatedTime;
    }
}
