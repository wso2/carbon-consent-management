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

public class ConsentPurpose {

    private String purpose;
    private List<String> purposeCategory;
    private String consentType;
    private List<String> piiCategory;
    private boolean primaryPurpose;
    private String termination;
    private boolean thirdPartyDisclosure;
    private String thirdPartyName;
    private int serviceToPurposeId;

    public String getPurpose() {

        return purpose;
    }

    public void setPurpose(String purpose) {

        this.purpose = purpose;
    }

    public List<String> getPurposeCategory() {

        return purposeCategory;
    }

    public void setPurposeCategory(List<String> purposeCategory) {

        this.purposeCategory = purposeCategory;
    }

    public String getConsentType() {

        return consentType;
    }

    public void setConsentType(String consentType) {

        this.consentType = consentType;
    }

    public List<String> getPiiCategory() {

        return piiCategory;
    }

    public void setPiiCategory(List<String> piiCategory) {

        this.piiCategory = piiCategory;
    }

    public boolean isPrimaryPurpose() {

        return primaryPurpose;
    }

    public void setPrimaryPurpose(boolean primaryPurpose) {

        this.primaryPurpose = primaryPurpose;
    }

    public String getTermination() {

        return termination;
    }

    public void setTermination(String termination) {

        this.termination = termination;
    }

    public boolean isThirdPartyDisclosure() {

        return thirdPartyDisclosure;
    }

    public void setThirdPartyDisclosure(boolean thirdPartyDisclosure) {

        this.thirdPartyDisclosure = thirdPartyDisclosure;
    }

    public String getThirdPartyName() {

        return thirdPartyName;
    }

    public void setThirdPartyName(String thirdPartyName) {

        this.thirdPartyName = thirdPartyName;
    }

    public int getServiceToPurposeId() {

        return serviceToPurposeId;
    }

    public void setServiceToPurposeId(int serviceToPurposeId) {

        this.serviceToPurposeId = serviceToPurposeId;
    }
}
