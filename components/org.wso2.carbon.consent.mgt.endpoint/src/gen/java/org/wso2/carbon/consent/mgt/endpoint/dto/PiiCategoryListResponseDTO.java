package org.wso2.carbon.consent.mgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class PiiCategoryListResponseDTO  {
  
  
  
  private String piiCategoryId = null;
  
  
  private String piiCategory = null;
  
  
  private String description = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("piiCategoryId")
  public String getPiiCategoryId() {
    return piiCategoryId;
  }
  public void setPiiCategoryId(String piiCategoryId) {
    this.piiCategoryId = piiCategoryId;
  }

  
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
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PiiCategoryListResponseDTO {\n");
    
    sb.append("  piiCategoryId: ").append(piiCategoryId).append("\n");
    sb.append("  piiCategory: ").append(piiCategory).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
