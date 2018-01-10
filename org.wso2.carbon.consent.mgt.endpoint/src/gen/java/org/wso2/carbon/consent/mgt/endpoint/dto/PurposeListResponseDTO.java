package org.wso2.carbon.consent.mgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class PurposeListResponseDTO  {
  
  
  
  private String purposeId = null;
  
  
  private String purpose = null;
  
  
  private String discripiton = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("purposeId")
  public String getPurposeId() {
    return purposeId;
  }
  public void setPurposeId(String purposeId) {
    this.purposeId = purposeId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("purpose")
  public String getPurpose() {
    return purpose;
  }
  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("discripiton")
  public String getDiscripiton() {
    return discripiton;
  }
  public void setDiscripiton(String discripiton) {
    this.discripiton = discripiton;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PurposeListResponseDTO {\n");
    
    sb.append("  purposeId: ").append(purposeId).append("\n");
    sb.append("  purpose: ").append(purpose).append("\n");
    sb.append("  discripiton: ").append(discripiton).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
