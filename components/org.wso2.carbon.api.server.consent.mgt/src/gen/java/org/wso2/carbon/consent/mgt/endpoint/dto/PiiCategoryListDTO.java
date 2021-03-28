package org.wso2.carbon.consent.mgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class PiiCategoryListDTO  {
  
  
  
  private Integer piiCategoryId = null;
  
  
  private String validity = null;

  
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
  @JsonProperty("validity")
  public String getValidity() {
    return validity;
  }
  public void setValidity(String validity) {
    this.validity = validity;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PiiCategoryListDTO {\n");
    
    sb.append("  piiCategoryId: ").append(piiCategoryId).append("\n");
    sb.append("  validity: ").append(validity).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
