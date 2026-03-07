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
 * Minimal purpose information for list responses
 **/

import java.util.Objects;
import javax.validation.Valid;

@ApiModel(description = "Minimal purpose information for list responses")
public class PurposeSummaryDTO  {
  
    private Integer id;
    private String name;
    private String description;
    private String group;
    private String groupType;
    private Integer version;

    /**
    **/
    public PurposeSummaryDTO id(Integer id) {

        this.id = id;
        return this;
    }
    
    @ApiModelProperty(example = "1", value = "")
    @JsonProperty("id")
    @Valid
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    /**
    **/
    public PurposeSummaryDTO name(String name) {

        this.name = name;
        return this;
    }

    @ApiModelProperty(example = "User Authentication", value = "")
    @JsonProperty("name")
    @Valid
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
    **/
    public PurposeSummaryDTO description(String description) {

        this.description = description;
        return this;
    }
    
    @ApiModelProperty(example = "To authenticate users and manage their identity in the system", value = "")
    @JsonProperty("description")
    @Valid
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    /**
    **/
    public PurposeSummaryDTO group(String group) {

        this.group = group;
        return this;
    }
    
    @ApiModelProperty(example = "Core Identity", value = "")
    @JsonProperty("group")
    @Valid
    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
    }

    /**
    **/
    public PurposeSummaryDTO groupType(String groupType) {

        this.groupType = groupType;
        return this;
    }
    
    @ApiModelProperty(example = "System", value = "")
    @JsonProperty("groupType")
    @Valid
    public String getGroupType() {
        return groupType;
    }
    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    /**
    **/
    public PurposeSummaryDTO version(Integer version) {

        this.version = version;
        return this;
    }
    
    @ApiModelProperty(example = "1", value = "")
    @JsonProperty("version")
    @Valid
    public Integer getVersion() {
        return version;
    }
    public void setVersion(Integer version) {
        this.version = version;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PurposeSummaryDTO purposeSummaryDTO = (PurposeSummaryDTO) o;
        return Objects.equals(this.id, purposeSummaryDTO.id) &&
            Objects.equals(this.name, purposeSummaryDTO.name) &&
            Objects.equals(this.description, purposeSummaryDTO.description) &&
            Objects.equals(this.group, purposeSummaryDTO.group) &&
            Objects.equals(this.groupType, purposeSummaryDTO.groupType) &&
            Objects.equals(this.version, purposeSummaryDTO.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, group, groupType, version);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class PurposeSummaryDTO {\n");
        
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    group: ").append(toIndentedString(group)).append("\n");
        sb.append("    groupType: ").append(toIndentedString(groupType)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
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

