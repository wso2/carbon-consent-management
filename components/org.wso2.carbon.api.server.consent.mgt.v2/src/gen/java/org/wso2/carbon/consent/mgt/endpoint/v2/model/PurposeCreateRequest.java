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
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeElementBinding;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class PurposeCreateRequest  {
  
    private String name;
    private String description;
    private String group;
    private String groupType;
    private List<PurposeElementBinding> elements = null;


    /**
    * Name of the purpose
    **/
    public PurposeCreateRequest name(String name) {

        this.name = name;
        return this;
    }
    
    @ApiModelProperty(example = "User Authentication", required = true, value = "Name of the purpose")
    @JsonProperty("name")
    @Valid
    @NotNull(message = "Property name cannot be null.")
 @Size(min=1,max=255)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
    * Human-readable description of the purpose
    **/
    public PurposeCreateRequest description(String description) {

        this.description = description;
        return this;
    }
    
    @ApiModelProperty(example = "To authenticate users and manage their identity in the system", value = "Human-readable description of the purpose")
    @JsonProperty("description")
    @Valid @Size(max=1024)
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    /**
    * Purpose group classification
    **/
    public PurposeCreateRequest group(String group) {

        this.group = group;
        return this;
    }
    
    @ApiModelProperty(example = "Core Identity", required = true, value = "Purpose group classification")
    @JsonProperty("group")
    @Valid
    @NotNull(message = "Property group cannot be null.")
 @Size(max=255)
    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
    }

    /**
    * Type of the purpose group
    **/
    public PurposeCreateRequest groupType(String groupType) {

        this.groupType = groupType;
        return this;
    }
    
    @ApiModelProperty(example = "System", required = true, value = "Type of the purpose group")
    @JsonProperty("groupType")
    @Valid
    @NotNull(message = "Property groupType cannot be null.")
 @Size(max=255)
    public String getGroupType() {
        return groupType;
    }
    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    /**
    * Consent elements associated with this purpose
    **/
    public PurposeCreateRequest elements(List<PurposeElementBinding> elements) {

        this.elements = elements;
        return this;
    }
    
    @ApiModelProperty(value = "Consent elements associated with this purpose")
    @JsonProperty("elements")
    @Valid @Size(min=0)
    public List<PurposeElementBinding> getElements() {
        return elements;
    }
    public void setElements(List<PurposeElementBinding> elements) {
        this.elements = elements;
    }

    public PurposeCreateRequest addElementsItem(PurposeElementBinding elementsItem) {
        if (this.elements == null) {
            this.elements = new ArrayList<>();
        }
        this.elements.add(elementsItem);
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
        PurposeCreateRequest purposeCreateRequest = (PurposeCreateRequest) o;
        return Objects.equals(this.name, purposeCreateRequest.name) &&
            Objects.equals(this.description, purposeCreateRequest.description) &&
            Objects.equals(this.group, purposeCreateRequest.group) &&
            Objects.equals(this.groupType, purposeCreateRequest.groupType) &&
            Objects.equals(this.elements, purposeCreateRequest.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, group, groupType, elements);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class PurposeCreateRequest {\n");
        
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    group: ").append(toIndentedString(group)).append("\n");
        sb.append("    groupType: ").append(toIndentedString(groupType)).append("\n");
        sb.append("    elements: ").append(toIndentedString(elements)).append("\n");
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

