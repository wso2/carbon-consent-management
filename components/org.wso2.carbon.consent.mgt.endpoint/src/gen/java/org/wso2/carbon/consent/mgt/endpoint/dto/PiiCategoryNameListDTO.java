package org.wso2.carbon.consent.mgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class PiiCategoryNameListDTO  {
  
  
  
  private String piiCategory = null;
  
  
  private String validity = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("piiCategory")
  public String getPiiCategory() {
    return piiCategory;
  }
  public void setPiiCategory(String piiCategory) {
    this.piiCategory = piiCategory;
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
    sb.append("class PiiCategoryNameListDTO {\n");
    
    sb.append("  piiCategory: ").append(piiCategory).append("\n");
    sb.append("  validity: ").append(validity).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
