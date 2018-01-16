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

import java.util.List;

/**
 * The model representing a a consent receipt.
 */
public class Receipt {

    private String consentReceiptId;
    private String version;
    private String jurisdiction;
    private String collectionMethod;
    private String publicKey;
    private String language;
    private String piiPrincipalId;
    private long consentTimestamp;
    private List<PiiController> piiControllers;

    public String getConsentReceiptId() {
        return consentReceiptId;
    }

    public void setConsentReceiptId(String consentReceiptId) {
        this.consentReceiptId = consentReceiptId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public String getCollectionMethod() {
        return collectionMethod;
    }

    public void setCollectionMethod(String collectionMethod) {
        this.collectionMethod = collectionMethod;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
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

    public long getConsentTimestamp() {
        return consentTimestamp;
    }

    public void setConsentTimestamp(long consentTimestamp) {
        this.consentTimestamp = consentTimestamp;
    }

    public List<PiiController> getPiiControllers() {
        return piiControllers;
    }

    public void setPiiControllers(List<PiiController> piiControllers) {
        this.piiControllers = piiControllers;
    }

    public List<ReceiptService> getServices() {
        return services;
    }

    public void setServices(List<ReceiptService> services) {
        this.services = services;
    }

    public String getPolicyUrl() {
        return policyUrl;
    }

    public void setPolicyUrl(String policyUrl) {
        this.policyUrl = policyUrl;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public List<String> getSpiCat() {
        return spiCat;
    }

    public void setSpiCat(List<String> spiCat) {
        this.spiCat = spiCat;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    private List<ReceiptService> services;
    private String policyUrl;
    private boolean sensitive;
    private List<String> spiCat;
    private String state;
}
