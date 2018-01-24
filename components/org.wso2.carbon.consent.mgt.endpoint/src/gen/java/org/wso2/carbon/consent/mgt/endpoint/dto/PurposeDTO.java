package org.wso2.carbon.consent.mgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class PurposeDTO  {
  
  
  
  private Integer purposeId = null;
  
  @NotNull
  private List<Integer> purposeCategoryId = new ArrayList<Integer>();
  
  
  private String consentType = null;
  
  @NotNull
  private List<Integer> piiCategoryId = new ArrayList<Integer>();
  
  
  private Boolean primaryPurpose = null;
  
  @NotNull
  private String termination = null;
  
  @NotNull
  private Boolean thirdPartyDisclosure = null;
  
  @NotNull
  private String thirdPartyName = null;

  
  /**
   * A unique Id of purpose
   **/
  @ApiModelProperty(value = "A unique Id of purpose")
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
  @JsonProperty("purposeCategoryId")
  public List<Integer> getPurposeCategoryId() {
    return purposeCategoryId;
  }
  public void setPurposeCategoryId(List<Integer> purposeCategoryId) {
    this.purposeCategoryId = purposeCategoryId;
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
   * A list of defined PII categories Ids.
   **/
  @ApiModelProperty(required = true, value = "A list of defined PII categories Ids.")
  @JsonProperty("piiCategoryId")
  public List<Integer> getPiiCategoryId() {
    return piiCategoryId;
  }
  public void setPiiCategoryId(List<Integer> piiCategoryId) {
    this.piiCategoryId = piiCategoryId;
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
    sb.append("class PurposeDTO {\n");
    
    sb.append("  purposeId: ").append(purposeId).append("\n");
    sb.append("  purposeCategoryId: ").append(purposeCategoryId).append("\n");
    sb.append("  consentType: ").append(consentType).append("\n");
    sb.append("  piiCategoryId: ").append(piiCategoryId).append("\n");
    sb.append("  primaryPurpose: ").append(primaryPurpose).append("\n");
    sb.append("  termination: ").append(termination).append("\n");
    sb.append("  thirdPartyDisclosure: ").append(thirdPartyDisclosure).append("\n");
    sb.append("  thirdPartyName: ").append(thirdPartyName).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
