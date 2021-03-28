package org.wso2.carbon.consent.mgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class PIIcategoryRequestDTO  {
  
  
  @NotNull
  private String piiCategory = null;
  
  
  private String description = null;
  
  
  private String displayName = null;
  
  
  private Boolean sensitive = null;

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
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
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("sensitive")
  public Boolean getSensitive() {
    return sensitive;
  }
  public void setSensitive(Boolean sensitive) {
    this.sensitive = sensitive;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PIIcategoryRequestDTO {\n");
    
    sb.append("  piiCategory: ").append(piiCategory).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  displayName: ").append(displayName).append("\n");
    sb.append("  sensitive: ").append(sensitive).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
