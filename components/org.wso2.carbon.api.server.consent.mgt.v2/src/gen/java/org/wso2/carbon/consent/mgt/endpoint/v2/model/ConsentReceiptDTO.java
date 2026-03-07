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
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentedPurposeDTO;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.KeyValuePair;
import javax.validation.constraints.*;

/**
 * Complete consent receipt details
 **/

import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;
@ApiModel(description = "Complete consent receipt details")
public class ConsentReceiptDTO  {
  
    private Long timestamp;
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
    private List<ConsentedPurposeDTO> purposes = null;

    private List<KeyValuePair> metadata = null;


    /**
    * Timestamp of consent (milliseconds since epoch)
    **/
    public ConsentReceiptDTO timestamp(Long timestamp) {

        this.timestamp = timestamp;
        return this;
    }
    
    @ApiModelProperty(example = "1677686400000", value = "Timestamp of consent (milliseconds since epoch)")
    @JsonProperty("timestamp")
    @Valid
    public Long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
    * Unique identifier of the consent
    **/
    public ConsentReceiptDTO receiptId(String receiptId) {

        this.receiptId = receiptId;
        return this;
    }
    
    @ApiModelProperty(example = "f83aa1a3-5d4d-4c0e-84db-c3a4f1e6c8b2", value = "Unique identifier of the consent")
    @JsonProperty("receiptId")
    @Valid
    public String getReceiptId() {
        return receiptId;
    }
    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    /**
    * Language code
    **/
    public ConsentReceiptDTO language(String language) {

        this.language = language;
        return this;
    }
    
    @ApiModelProperty(example = "en", value = "Language code")
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
    public ConsentReceiptDTO subjectUserId(String subjectUserId) {

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
    * Current state of the consent
    **/
    public ConsentReceiptDTO state(StateEnum state) {

        this.state = state;
        return this;
    }
    
    @ApiModelProperty(example = "ACTIVE", value = "Current state of the consent")
    @JsonProperty("state")
    @Valid
    public StateEnum getState() {
        return state;
    }
    public void setState(StateEnum state) {
        this.state = state;
    }

    /**
    * Service name
    **/
    public ConsentReceiptDTO service(String service) {

        this.service = service;
        return this;
    }
    
    @ApiModelProperty(example = "admin-dashboard", value = "Service name")
    @JsonProperty("service")
    @Valid
    public String getService() {
        return service;
    }
    public void setService(String service) {
        this.service = service;
    }

    /**
    * Purposes and elements in this consent
    **/
    public ConsentReceiptDTO purposes(List<ConsentedPurposeDTO> purposes) {

        this.purposes = purposes;
        return this;
    }
    
    @ApiModelProperty(value = "Purposes and elements in this consent")
    @JsonProperty("purposes")
    @Valid
    public List<ConsentedPurposeDTO> getPurposes() {
        return purposes;
    }
    public void setPurposes(List<ConsentedPurposeDTO> purposes) {
        this.purposes = purposes;
    }

    public ConsentReceiptDTO addPurposesItem(ConsentedPurposeDTO purposesItem) {
        if (this.purposes == null) {
            this.purposes = new ArrayList<>();
        }
        this.purposes.add(purposesItem);
        return this;
    }

        /**
    * Metadata associated with this consent
    **/
    public ConsentReceiptDTO metadata(List<KeyValuePair> metadata) {

        this.metadata = metadata;
        return this;
    }
    
    @ApiModelProperty(value = "Metadata associated with this consent")
    @JsonProperty("metadata")
    @Valid
    public List<KeyValuePair> getMetadata() {
        return metadata;
    }
    public void setMetadata(List<KeyValuePair> metadata) {
        this.metadata = metadata;
    }

    public ConsentReceiptDTO addMetadataItem(KeyValuePair metadataItem) {
        if (this.metadata == null) {
            this.metadata = new ArrayList<>();
        }
        this.metadata.add(metadataItem);
        return this;
    }

    

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsentReceiptDTO consentReceiptDTO = (ConsentReceiptDTO) o;
        return Objects.equals(this.timestamp, consentReceiptDTO.timestamp) &&
            Objects.equals(this.receiptId, consentReceiptDTO.receiptId) &&
            Objects.equals(this.language, consentReceiptDTO.language) &&
            Objects.equals(this.subjectUserId, consentReceiptDTO.subjectUserId) &&
            Objects.equals(this.state, consentReceiptDTO.state) &&
            Objects.equals(this.service, consentReceiptDTO.service) &&
            Objects.equals(this.purposes, consentReceiptDTO.purposes) &&
            Objects.equals(this.metadata, consentReceiptDTO.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, receiptId, language, subjectUserId, state, service, purposes, metadata);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentReceiptDTO {\n");
        
        sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
        sb.append("    receiptId: ").append(toIndentedString(receiptId)).append("\n");
        sb.append("    language: ").append(toIndentedString(language)).append("\n");
        sb.append("    subjectUserId: ").append(toIndentedString(subjectUserId)).append("\n");
        sb.append("    state: ").append(toIndentedString(state)).append("\n");
        sb.append("    service: ").append(toIndentedString(service)).append("\n");
        sb.append("    purposes: ").append(toIndentedString(purposes)).append("\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
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

