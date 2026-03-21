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
import org.wso2.carbon.consent.mgt.endpoint.v2.model.PurposeVersionSummaryDTO;
import javax.validation.constraints.*;

/**
 * Paginated list of purpose versions
 **/

import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;
@ApiModel(description = "Paginated list of purpose versions")
public class PurposeVersionListResponse  {
  
    private Integer startIndex;
    private Integer count;
    private List<PurposeVersionSummaryDTO> items = null;


    /**
    * Starting index of the returned records
    **/
    public PurposeVersionListResponse startIndex(Integer startIndex) {

        this.startIndex = startIndex;
        return this;
    }
    
    @ApiModelProperty(example = "0", value = "Starting index of the returned records")
    @JsonProperty("startIndex")
    @Valid
    public Integer getStartIndex() {
        return startIndex;
    }
    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    /**
    * Number of records returned
    **/
    public PurposeVersionListResponse count(Integer count) {

        this.count = count;
        return this;
    }
    
    @ApiModelProperty(example = "2", value = "Number of records returned")
    @JsonProperty("count")
    @Valid
    public Integer getCount() {
        return count;
    }
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
    * List of purpose versions
    **/
    public PurposeVersionListResponse items(List<PurposeVersionSummaryDTO> items) {

        this.items = items;
        return this;
    }
    
    @ApiModelProperty(example = "[{\"versionId\":\"a1b2c3d4-1234-5678-abcd-ef1234567890\",\"version\":\"v1.0\",\"description\":\"Initial version\"},{\"versionId\":\"b2c3d4e5-2345-6789-bcde-f01234567892\",\"version\":\"v2.0\",\"description\":\"Added new consent elements for enhanced user authentication\"}]", value = "List of purpose versions")
    @JsonProperty("items")
    @Valid
    public List<PurposeVersionSummaryDTO> getItems() {
        return items;
    }
    public void setItems(List<PurposeVersionSummaryDTO> items) {
        this.items = items;
    }

    public PurposeVersionListResponse addItemsItem(PurposeVersionSummaryDTO itemsItem) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(itemsItem);
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
        PurposeVersionListResponse purposeVersionListResponse = (PurposeVersionListResponse) o;
        return Objects.equals(this.startIndex, purposeVersionListResponse.startIndex) &&
            Objects.equals(this.count, purposeVersionListResponse.count) &&
            Objects.equals(this.items, purposeVersionListResponse.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startIndex, count, items);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class PurposeVersionListResponse {\n");
        
        sb.append("    startIndex: ").append(toIndentedString(startIndex)).append("\n");
        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    items: ").append(toIndentedString(items)).append("\n");
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

