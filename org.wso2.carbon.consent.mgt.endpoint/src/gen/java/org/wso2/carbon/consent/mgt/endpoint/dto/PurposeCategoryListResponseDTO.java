package org.wso2.carbon.consent.mgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class PurposeCategoryListResponseDTO  {
  
  
  
  private String purposeCategoryId = null;
  
  
  private String purposeCategory = null;
  
  
  private String description = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("purposeCategoryId")
  public String getPurposeCategoryId() {
    return purposeCategoryId;
  }
  public void setPurposeCategoryId(String purposeCategoryId) {
    this.purposeCategoryId = purposeCategoryId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("purposeCategory")
  public String getPurposeCategory() {
    return purposeCategory;
  }
  public void setPurposeCategory(String purposeCategory) {
    this.purposeCategory = purposeCategory;
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
    sb.append("class PurposeCategoryListResponseDTO {\n");
    
    sb.append("  purposeCategoryId: ").append(purposeCategoryId).append("\n");
    sb.append("  purposeCategory: ").append(purposeCategory).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
