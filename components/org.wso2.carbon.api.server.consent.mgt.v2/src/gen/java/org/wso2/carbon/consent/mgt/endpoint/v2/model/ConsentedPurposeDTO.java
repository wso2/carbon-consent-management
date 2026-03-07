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
import org.wso2.carbon.consent.mgt.endpoint.v2.model.ConsentedElementDTO;
import javax.validation.constraints.*;

/**
 * Purpose information within a consent receipt
 **/

import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;
@ApiModel(description = "Purpose information within a consent receipt")
public class ConsentedPurposeDTO  {
  
    private String name;
    private Integer purposeId;
    private Integer purposeVersionId;
    private Integer terminationPeriod;
    private List<ConsentedElementDTO> elements = null;


    /**
    **/
    public ConsentedPurposeDTO name(String name) {

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
    public ConsentedPurposeDTO purposeId(Integer purposeId) {

        this.purposeId = purposeId;
        return this;
    }
    
    @ApiModelProperty(example = "1", value = "")
    @JsonProperty("purposeId")
    @Valid
    public Integer getPurposeId() {
        return purposeId;
    }
    public void setPurposeId(Integer purposeId) {
        this.purposeId = purposeId;
    }

    /**
    * Version ID of the purpose that was consented
    **/
    public ConsentedPurposeDTO purposeVersionId(Integer purposeVersionId) {

        this.purposeVersionId = purposeVersionId;
        return this;
    }
    
    @ApiModelProperty(example = "3", value = "Version ID of the purpose that was consented")
    @JsonProperty("purposeVersionId")
    @Valid
    public Integer getPurposeVersionId() {
        return purposeVersionId;
    }
    public void setPurposeVersionId(Integer purposeVersionId) {
        this.purposeVersionId = purposeVersionId;
    }

    /**
    * Termination period in days (-1 for indefinite)
    **/
    public ConsentedPurposeDTO terminationPeriod(Integer terminationPeriod) {

        this.terminationPeriod = terminationPeriod;
        return this;
    }
    
    @ApiModelProperty(example = "365", value = "Termination period in days (-1 for indefinite)")
    @JsonProperty("terminationPeriod")
    @Valid
    public Integer getTerminationPeriod() {
        return terminationPeriod;
    }
    public void setTerminationPeriod(Integer terminationPeriod) {
        this.terminationPeriod = terminationPeriod;
    }

    /**
    * Consented elements for this purpose
    **/
    public ConsentedPurposeDTO elements(List<ConsentedElementDTO> elements) {

        this.elements = elements;
        return this;
    }
    
    @ApiModelProperty(value = "Consented elements for this purpose")
    @JsonProperty("elements")
    @Valid
    public List<ConsentedElementDTO> getElements() {
        return elements;
    }
    public void setElements(List<ConsentedElementDTO> elements) {
        this.elements = elements;
    }

    public ConsentedPurposeDTO addElementsItem(ConsentedElementDTO elementsItem) {
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
        ConsentedPurposeDTO consentedPurposeDTO = (ConsentedPurposeDTO) o;
        return Objects.equals(this.name, consentedPurposeDTO.name) &&
            Objects.equals(this.purposeId, consentedPurposeDTO.purposeId) &&
            Objects.equals(this.purposeVersionId, consentedPurposeDTO.purposeVersionId) &&
            Objects.equals(this.terminationPeriod, consentedPurposeDTO.terminationPeriod) &&
            Objects.equals(this.elements, consentedPurposeDTO.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, purposeId, purposeVersionId, terminationPeriod, elements);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentedPurposeDTO {\n");
        
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    purposeId: ").append(toIndentedString(purposeId)).append("\n");
        sb.append("    purposeVersionId: ").append(toIndentedString(purposeVersionId)).append("\n");
        sb.append("    terminationPeriod: ").append(toIndentedString(terminationPeriod)).append("\n");
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

