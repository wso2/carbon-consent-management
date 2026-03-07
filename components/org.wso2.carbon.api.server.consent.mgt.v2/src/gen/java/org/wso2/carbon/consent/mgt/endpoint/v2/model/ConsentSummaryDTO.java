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
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Minimal consent information for list responses
 **/

import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;
@ApiModel(description = "Minimal consent information for list responses")
public class ConsentSummaryDTO  {
  
    private String receiptId;
    private String language;
    private String subjectUserId;

@XmlType(name="StateEnum")
@XmlEnum(String.class)
public enum StateEnum {

    @XmlEnumValue("ACTIVE") ACTIVE(String.valueOf("ACTIVE")), @XmlEnumValue("REVOKED") REVOKED(String.valueOf("REVOKED"));


    private String value;

    StateEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static StateEnum fromValue(String value) {
        for (StateEnum b : StateEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

    private StateEnum state;
    private String service;

    /**
    * Unique identifier for the consent receipt
    **/
    public ConsentSummaryDTO receiptId(String receiptId) {

        this.receiptId = receiptId;
        return this;
    }
    
    @ApiModelProperty(example = "f83aa1a3-5d4d-4c0e-84db-c3a4f1e6c8b2", value = "Unique identifier for the consent receipt")
    @JsonProperty("receiptId")
    @Valid
    public String getReceiptId() {
        return receiptId;
    }
    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    /**
    **/
    public ConsentSummaryDTO language(String language) {

        this.language = language;
        return this;
    }
    
    @ApiModelProperty(example = "en", value = "")
    @JsonProperty("language")
    @Valid
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
    **/
    public ConsentSummaryDTO subjectUserId(String subjectUserId) {

        this.subjectUserId = subjectUserId;
        return this;
    }
    
    @ApiModelProperty(example = "user@example.com", value = "")
    @JsonProperty("subjectUserId")
    @Valid
    public String getSubjectUserId() {
        return subjectUserId;
    }
    public void setSubjectUserId(String subjectUserId) {
        this.subjectUserId = subjectUserId;
    }

    /**
    **/
    public ConsentSummaryDTO state(StateEnum state) {

        this.state = state;
        return this;
    }
    
    @ApiModelProperty(example = "ACTIVE", value = "")
    @JsonProperty("state")
    @Valid
    public StateEnum getState() {
        return state;
    }
    public void setState(StateEnum state) {
        this.state = state;
    }

    /**
    **/
    public ConsentSummaryDTO service(String service) {

        this.service = service;
        return this;
    }
    
    @ApiModelProperty(example = "admin-dashboard", value = "")
    @JsonProperty("service")
    @Valid
    public String getService() {
        return service;
    }
    public void setService(String service) {
        this.service = service;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsentSummaryDTO consentSummaryDTO = (ConsentSummaryDTO) o;
        return Objects.equals(this.receiptId, consentSummaryDTO.receiptId) &&
            Objects.equals(this.language, consentSummaryDTO.language) &&
            Objects.equals(this.subjectUserId, consentSummaryDTO.subjectUserId) &&
            Objects.equals(this.state, consentSummaryDTO.state) &&
            Objects.equals(this.service, consentSummaryDTO.service);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiptId, language, subjectUserId, state, service);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentSummaryDTO {\n");
        
        sb.append("    receiptId: ").append(toIndentedString(receiptId)).append("\n");
        sb.append("    language: ").append(toIndentedString(language)).append("\n");
        sb.append("    subjectUserId: ").append(toIndentedString(subjectUserId)).append("\n");
        sb.append("    state: ").append(toIndentedString(state)).append("\n");
        sb.append("    service: ").append(toIndentedString(service)).append("\n");
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

