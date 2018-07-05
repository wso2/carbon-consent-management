package org.wso2.carbon.consent.mgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class PurposePiiCategoryRequestDTO  {
  
  
  
  private Integer piiCategoryId = null;
  
  
  private Boolean mandatory = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("piiCategoryId")
  public Integer getPiiCategoryId() {
    return piiCategoryId;
  }
  public void setPiiCategoryId(Integer piiCategoryId) {
    this.piiCategoryId = piiCategoryId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("mandatory")
  public Boolean getMandatory() {
    return mandatory;
  }
  public void setMandatory(Boolean mandatory) {
    this.mandatory = mandatory;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PurposePiiCategoryRequestDTO {\n");
    
    sb.append("  piiCategoryId: ").append(piiCategoryId).append("\n");
    sb.append("  mandatory: ").append(mandatory).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
