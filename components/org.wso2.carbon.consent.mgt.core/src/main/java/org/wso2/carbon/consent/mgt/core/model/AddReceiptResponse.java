/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.consent.mgt.core.model;

/**
 * The model representing a Receipt Response.
 */
public class AddReceiptResponse {

    private String consentReceiptId;
    private String collectionMethod;
    private String language;
    private String piiPrincipalId;
    private String tenantDomain;

    public AddReceiptResponse(String consentReceiptId, String collectionMethod, String language,
                              String piiPrincipalId, String tenantDomain) {
        this.consentReceiptId = consentReceiptId;
        this.collectionMethod = collectionMethod;
        this.language = language;
        this.piiPrincipalId = piiPrincipalId;
        this.tenantDomain = tenantDomain;
    }

    public String getConsentReceiptId() {
        return consentReceiptId;
    }

    public void setConsentReceiptId(String consentReceiptId) {
        this.consentReceiptId = consentReceiptId;
    }

    public String getCollectionMethod() {
        return collectionMethod;
    }

    public void setCollectionMethod(String collectionMethod) {
        this.collectionMethod = collectionMethod;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPiiPrincipalId() {
        return piiPrincipalId;
    }

    public void setPiiPrincipalId(String piiPrincipalId) {
        this.piiPrincipalId = piiPrincipalId;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
}
