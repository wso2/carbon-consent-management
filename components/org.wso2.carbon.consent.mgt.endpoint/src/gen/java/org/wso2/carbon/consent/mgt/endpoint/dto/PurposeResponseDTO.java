package org.wso2.carbon.consent.mgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.consent.mgt.endpoint.dto.PiiCategoryNameListDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class PurposeResponseDTO  {
  
  
  
  private String purpose = null;
  
  
  private Integer purposeId = null;
  
  @NotNull
  private List<String> purposeCategory = new ArrayList<String>();
  
  
  private String consentType = null;
  
  @NotNull
  private List<PiiCategoryNameListDTO> piiCategory = new ArrayList<PiiCategoryNameListDTO>();
  
  
  private Boolean primaryPurpose = null;
  
  @NotNull
  private String termination = null;
  
  @NotNull
  private Boolean thirdPartyDisclosure = null;
  
  @NotNull
  private String thirdPartyName = null;

  
  /**
   * A short, clear explanation of why the PII item is required.
   **/
  @ApiModelProperty(value = "A short, clear explanation of why the PII item is required.")
  @JsonProperty("purpose")
  public String getPurpose() {
    return purpose;
  }
  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  
  /**
   * Id of the purpose.
   **/
  @ApiModelProperty(value = "Id of the purpose.")
  @JsonProperty("purposeId")
  public Integer getPurposeId() {
    return purposeId;
  }
  public void setPurposeId(Integer purposeId) {
    this.purposeId = purposeId;
  }

  
  /**
   * The reason the PII Controller is collecting the PII.
   **/
  @ApiModelProperty(required = true, value = "The reason the PII Controller is collecting the PII.")
  @JsonProperty("purposeCategory")
  public List<String> getPurposeCategory() {
    return purposeCategory;
  }
  public void setPurposeCategory(List<String> purposeCategory) {
    this.purposeCategory = purposeCategory;
  }

  
  /**
   * The type of the consent used by the PII Controller as their authority to collect, use or disclose PII.
   **/
  @ApiModelProperty(value = "The type of the consent used by the PII Controller as their authority to collect, use or disclose PII.")
  @JsonProperty("consentType")
  public String getConsentType() {
    return consentType;
  }
  public void setConsentType(String consentType) {
    this.consentType = consentType;
  }

  
  /**
   * A list of defined PII categories Ids.  PII Category should reflect the category that will be shared as understood by the PII Principal.
   **/
  @ApiModelProperty(required = true, value = "A list of defined PII categories Ids.  PII Category should reflect the category that will be shared as understood by the PII Principal.")
  @JsonProperty("piiCategory")
  public List<PiiCategoryNameListDTO> getPiiCategory() {
    return piiCategory;
  }
  public void setPiiCategory(List<PiiCategoryNameListDTO> piiCategory) {
    this.piiCategory = piiCategory;
  }

  
  /**
   * Indicates if a purpose is part of the core service of the PII Controller. Possible values are TRUE or FALSE.
   **/
  @ApiModelProperty(value = "Indicates if a purpose is part of the core service of the PII Controller. Possible values are TRUE or FALSE.")
  @JsonProperty("primaryPurpose")
  public Boolean getPrimaryPurpose() {
    return primaryPurpose;
  }
  public void setPrimaryPurpose(Boolean primaryPurpose) {
    this.primaryPurpose = primaryPurpose;
  }

  
  /**
   * Conditions for the termination of consent. Link to policy defining how consent or purpose is terminated.
   **/
  @ApiModelProperty(required = true, value = "Conditions for the termination of consent. Link to policy defining how consent or purpose is terminated.")
  @JsonProperty("termination")
  public String getTermination() {
    return termination;
  }
  public void setTermination(String termination) {
    this.termination = termination;
  }

  
  /**
   * Indicates if the PII Controller is disclosing PII to a third party.  Possible values are TRUE or FALSE.
   **/
  @ApiModelProperty(required = true, value = "Indicates if the PII Controller is disclosing PII to a third party.  Possible values are TRUE or FALSE.")
  @JsonProperty("thirdPartyDisclosure")
  public Boolean getThirdPartyDisclosure() {
    return thirdPartyDisclosure;
  }
  public void setThirdPartyDisclosure(Boolean thirdPartyDisclosure) {
    this.thirdPartyDisclosure = thirdPartyDisclosure;
  }

  
  /**
   * The name or names of the third party to which the PII Processor may disclose the PII.
   **/
  @ApiModelProperty(required = true, value = "The name or names of the third party to which the PII Processor may disclose the PII.")
  @JsonProperty("thirdPartyName")
  public String getThirdPartyName() {
    return thirdPartyName;
  }
  public void setThirdPartyName(String thirdPartyName) {
    this.thirdPartyName = thirdPartyName;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PurposeResponseDTO {\n");
    
    sb.append("  purpose: ").append(purpose).append("\n");
    sb.append("  purposeId: ").append(purposeId).append("\n");
    sb.append("  purposeCategory: ").append(purposeCategory).append("\n");
    sb.append("  consentType: ").append(consentType).append("\n");
    sb.append("  piiCategory: ").append(piiCategory).append("\n");
    sb.append("  primaryPurpose: ").append(primaryPurpose).append("\n");
    sb.append("  termination: ").append(termination).append("\n");
    sb.append("  thirdPartyDisclosure: ").append(thirdPartyDisclosure).append("\n");
    sb.append("  thirdPartyName: ").append(thirdPartyName).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
