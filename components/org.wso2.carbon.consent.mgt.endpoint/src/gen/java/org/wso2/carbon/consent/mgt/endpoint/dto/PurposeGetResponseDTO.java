package org.wso2.carbon.consent.mgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.consent.mgt.endpoint.dto.PurposePiiCategoryListResponseDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class PurposeGetResponseDTO  {
  
  
  
  private Integer purposeId = null;
  
  
  private String purpose = null;
  
  
  private String description = null;
  
  
  private List<PurposePiiCategoryListResponseDTO> piiCategories = new ArrayList<PurposePiiCategoryListResponseDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("purposeId")
  public Integer getPurposeId() {
    return purposeId;
  }
  public void setPurposeId(Integer purposeId) {
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
  @JsonProperty("piiCategories")
  public List<PurposePiiCategoryListResponseDTO> getPiiCategories() {
    return piiCategories;
  }
  public void setPiiCategories(List<PurposePiiCategoryListResponseDTO> piiCategories) {
    this.piiCategories = piiCategories;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PurposeGetResponseDTO {\n");
    
    sb.append("  purposeId: ").append(purposeId).append("\n");
    sb.append("  purpose: ").append(purpose).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  piiCategories: ").append(piiCategories).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
