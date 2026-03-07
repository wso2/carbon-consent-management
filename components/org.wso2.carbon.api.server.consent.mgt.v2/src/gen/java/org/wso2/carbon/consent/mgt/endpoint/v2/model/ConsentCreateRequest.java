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
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentPurposeBinding;
import org.wso2.carbon.consent.mgt.endpoint.v2.model.KeyValuePair;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class ConsentCreateRequest  {
  
    private String service;
    private String language;
    private List<ConsentPurposeBinding> purposes = new ArrayList<>();

    private List<KeyValuePair> metadata = null;


    /**
    * Service name requesting consent
    **/
    public ConsentCreateRequest service(String service) {

        this.service = service;
        return this;
    }
    
    @ApiModelProperty(example = "admin-dashboard", required = true, value = "Service name requesting consent")
    @JsonProperty("service")
    @Valid
    @NotNull(message = "Property service cannot be null.")
 @Size(min=1,max=255)
    public String getService() {
        return service;
    }
    public void setService(String service) {
        this.service = service;
    }

    /**
    * Language code for the consent
    **/
    public ConsentCreateRequest language(String language) {

        this.language = language;
        return this;
    }
    
    @ApiModelProperty(example = "en", value = "Language code for the consent")
    @JsonProperty("language")
    @Valid @Size(min=1,max=20)
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
    * List of purposes and associated elements
    **/
    public ConsentCreateRequest purposes(List<ConsentPurposeBinding> purposes) {

        this.purposes = purposes;
        return this;
    }
    
    @ApiModelProperty(required = true, value = "List of purposes and associated elements")
    @JsonProperty("purposes")
    @Valid
    @NotNull(message = "Property purposes cannot be null.")
 @Size(min=1)
    public List<ConsentPurposeBinding> getPurposes() {
        return purposes;
    }
    public void setPurposes(List<ConsentPurposeBinding> purposes) {
        this.purposes = purposes;
    }

    public ConsentCreateRequest addPurposesItem(ConsentPurposeBinding purposesItem) {
        this.purposes.add(purposesItem);
        return this;
    }

        /**
    * Optional metadata key-value pairs
    **/
    public ConsentCreateRequest metadata(List<KeyValuePair> metadata) {

        this.metadata = metadata;
        return this;
    }
    
    @ApiModelProperty(value = "Optional metadata key-value pairs")
    @JsonProperty("metadata")
    @Valid
    public List<KeyValuePair> getMetadata() {
        return metadata;
    }
    public void setMetadata(List<KeyValuePair> metadata) {
        this.metadata = metadata;
    }

    public ConsentCreateRequest addMetadataItem(KeyValuePair metadataItem) {
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
        ConsentCreateRequest consentCreateRequest = (ConsentCreateRequest) o;
        return Objects.equals(this.service, consentCreateRequest.service) &&
            Objects.equals(this.language, consentCreateRequest.language) &&
            Objects.equals(this.purposes, consentCreateRequest.purposes) &&
            Objects.equals(this.metadata, consentCreateRequest.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, language, purposes, metadata);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentCreateRequest {\n");
        
        sb.append("    service: ").append(toIndentedString(service)).append("\n");
        sb.append("    language: ").append(toIndentedString(language)).append("\n");
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

