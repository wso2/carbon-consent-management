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

package org.wso2.carbon.consent.mgt.endpoint.v2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Response returned when consent is recorded
 **/

import java.util.Objects;
import javax.validation.Valid;

@ApiModel(description = "Response returned when consent is recorded")
public class ConsentResponseDTO  {
  
    private String receiptId;
    private String language;
    private String subjectUserId;
    private String tenantDomain;

    /**
    * Unique identifier for this consent receipt
    **/
    public ConsentResponseDTO receiptId(String receiptId) {

        this.receiptId = receiptId;
        return this;
    }
    
    @ApiModelProperty(example = "f83aa1a3-5d4d-4c0e-84db-c3a4f1e6c8b2", value = "Unique identifier for this consent receipt")
    @JsonProperty("receiptId")
    @Valid
    public String getReceiptId() {
        return receiptId;
    }
    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    /**
    * Language code of the consent
    **/
    public ConsentResponseDTO language(String language) {

        this.language = language;
        return this;
    }
    
    @ApiModelProperty(example = "en", value = "Language code of the consent")
    @JsonProperty("language")
    @Valid
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
    * ID of the user giving consent
    **/
    public ConsentResponseDTO subjectUserId(String subjectUserId) {

        this.subjectUserId = subjectUserId;
        return this;
    }

    @ApiModelProperty(example = "user@example.com", value = "ID of the user giving consent")
    @JsonProperty("subjectUserId")
    @Valid
    public String getSubjectUserId() {
        return subjectUserId;
    }
    public void setSubjectUserId(String subjectUserId) {
        this.subjectUserId = subjectUserId;
    }

    /**
    * Tenant domain
    **/
    public ConsentResponseDTO tenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
        return this;
    }
    
    @ApiModelProperty(example = "carbon.super", value = "Tenant domain")
    @JsonProperty("tenantDomain")
    @Valid
    public String getTenantDomain() {
        return tenantDomain;
    }
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsentResponseDTO consentResponseDTO = (ConsentResponseDTO) o;
        return Objects.equals(this.receiptId, consentResponseDTO.receiptId) &&
            Objects.equals(this.language, consentResponseDTO.language) &&
            Objects.equals(this.subjectUserId, consentResponseDTO.subjectUserId) &&
            Objects.equals(this.tenantDomain, consentResponseDTO.tenantDomain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiptId, language, subjectUserId, tenantDomain);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentResponseDTO {\n");
        
        sb.append("    receiptId: ").append(toIndentedString(receiptId)).append("\n");
        sb.append("    language: ").append(toIndentedString(language)).append("\n");
        sb.append("    subjectUserId: ").append(toIndentedString(subjectUserId)).append("\n");
        sb.append("    tenantDomain: ").append(toIndentedString(tenantDomain)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
    * Convert the given object to string with each line indented by 4 spaces
    * (except the first line).
    */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

