package org.wso2.carbon.consent.mgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class PiiCategoryNameListDTO  {
  
  
  
  private String piiCategoryName = null;
  
  
  private Integer piiCategoryId = null;
  
  
  private String validity = null;
  
  
  private String piiCategoryDisplayName = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("piiCategoryName")
  public String getPiiCategoryName() {
    return piiCategoryName;
  }
  public void setPiiCategoryName(String piiCategoryName) {
    this.piiCategoryName = piiCategoryName;
  }

  
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

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("piiCategoryDisplayName")
  public String getPiiCategoryDisplayName() {
    return piiCategoryDisplayName;
  }
  public void setPiiCategoryDisplayName(String piiCategoryDisplayName) {
    this.piiCategoryDisplayName = piiCategoryDisplayName;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PiiCategoryNameListDTO {\n");
    
    sb.append("  piiCategoryName: ").append(piiCategoryName).append("\n");
    sb.append("  piiCategoryId: ").append(piiCategoryId).append("\n");
    sb.append("  validity: ").append(validity).append("\n");
    sb.append("  piiCategoryDisplayName: ").append(piiCategoryDisplayName).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
