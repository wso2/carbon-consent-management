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
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ElementDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class ElementListResponse  {
  
    private Integer startIndex;
    private Integer count;
    private List<ElementDTO> items = null;


    /**
    * Starting index of the returned records
    **/
    public ElementListResponse startIndex(Integer startIndex) {

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
    public ElementListResponse count(Integer count) {

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
    * List of consent elements
    **/
    public ElementListResponse items(List<ElementDTO> items) {

        this.items = items;
        return this;
    }
    
    @ApiModelProperty(example = "[{\"elementId\":\"f83aa1a3-5d4d-4c0e-84db-c3a4f1e6c8b2\",\"name\":\"email_address\",\"displayName\":\"Email Address\",\"description\":\"User email address used for account notifications and communications\"},{\"elementId\":\"c2d3e4f5-2345-6789-bcde-f01234567891\",\"name\":\"phone_number\",\"displayName\":\"Phone Number\",\"description\":\"User phone number used for SMS notifications and two-factor authentication\"}]", value = "List of consent elements")
    @JsonProperty("items")
    @Valid
    public List<ElementDTO> getItems() {
        return items;
    }
    public void setItems(List<ElementDTO> items) {
        this.items = items;
    }

    public ElementListResponse addItemsItem(ElementDTO itemsItem) {
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
        ElementListResponse elementListResponse = (ElementListResponse) o;
        return Objects.equals(this.startIndex, elementListResponse.startIndex) &&
            Objects.equals(this.count, elementListResponse.count) &&
            Objects.equals(this.items, elementListResponse.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startIndex, count, items);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ElementListResponse {\n");
        
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

