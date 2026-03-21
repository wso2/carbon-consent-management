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
import java.util.UUID;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class PurposeElementBinding  {
  
    private UUID elementId;
    private Boolean mandatory;

    /**
    * ID of the consent element
    **/
    public PurposeElementBinding elementId(UUID elementId) {

        this.elementId = elementId;
        return this;
    }
    
    @ApiModelProperty(example = "f83aa1a3-5d4d-4c0e-84db-c3a4f1e6c8b2", required = true, value = "ID of the consent element")
    @JsonProperty("elementId")
    @Valid
    @NotNull(message = "Property elementId cannot be null.")

    public UUID getElementId() {
        return elementId;
    }
    public void setElementId(UUID elementId) {
        this.elementId = elementId;
    }

    /**
    * Whether this element is mandatory for the purpose
    **/
    public PurposeElementBinding mandatory(Boolean mandatory) {

        this.mandatory = mandatory;
        return this;
    }
    
    @ApiModelProperty(example = "true", value = "Whether this element is mandatory for the purpose")
    @JsonProperty("mandatory")
    @Valid
    public Boolean getMandatory() {
        return mandatory;
    }
    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PurposeElementBinding purposeElementBinding = (PurposeElementBinding) o;
        return Objects.equals(this.elementId, purposeElementBinding.elementId) &&
            Objects.equals(this.mandatory, purposeElementBinding.mandatory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, mandatory);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class PurposeElementBinding {\n");
        
        sb.append("    elementId: ").append(toIndentedString(elementId)).append("\n");
        sb.append("    mandatory: ").append(toIndentedString(mandatory)).append("\n");
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

