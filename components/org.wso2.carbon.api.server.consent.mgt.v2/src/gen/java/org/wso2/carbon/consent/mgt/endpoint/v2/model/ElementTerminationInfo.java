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
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import java.util.Objects;
import javax.validation.Valid;

public class ElementTerminationInfo  {
  
    private Integer elementId;
    private Integer validityPeriod;

    /**
    * Unique identifier of the consent element
    **/
    public ElementTerminationInfo elementId(Integer elementId) {

        this.elementId = elementId;
        return this;
    }
    
    @ApiModelProperty(example = "2", required = true, value = "Unique identifier of the consent element")
    @JsonProperty("elementId")
    @Valid
    @NotNull(message = "Property elementId cannot be null.")

    public Integer getElementId() {
        return elementId;
    }
    public void setElementId(Integer elementId) {
        this.elementId = elementId;
    }

    /**
    * Validity period in days (-1 for indefinite)
    **/
    public ElementTerminationInfo validityPeriod(Integer validityPeriod) {

        this.validityPeriod = validityPeriod;
        return this;
    }
    
    @ApiModelProperty(example = "365", value = "Validity period in days (-1 for indefinite)")
    @JsonProperty("validityPeriod")
    @Valid
    public Integer getValidityPeriod() {
        return validityPeriod;
    }
    public void setValidityPeriod(Integer validityPeriod) {
        this.validityPeriod = validityPeriod;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ElementTerminationInfo elementTerminationInfo = (ElementTerminationInfo) o;
        return Objects.equals(this.elementId, elementTerminationInfo.elementId) &&
            Objects.equals(this.validityPeriod, elementTerminationInfo.validityPeriod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, validityPeriod);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ElementTerminationInfo {\n");
        
        sb.append("    elementId: ").append(toIndentedString(elementId)).append("\n");
        sb.append("    validityPeriod: ").append(toIndentedString(validityPeriod)).append("\n");
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

