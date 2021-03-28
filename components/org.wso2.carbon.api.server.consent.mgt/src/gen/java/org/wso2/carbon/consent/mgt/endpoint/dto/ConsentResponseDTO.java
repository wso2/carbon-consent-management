package org.wso2.carbon.consent.mgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ConsentResponseDTO  {
  
  
  @NotNull
  private String consentReceiptID = null;
  
  
  private String language = null;
  
  @NotNull
  private String piiPrincipalId = null;
  
  @NotNull
  private String tenantDomain = null;
  
  
  private String state = null;
  
  
  private String spDisplayName = null;
  
  
  private String spDescription = null;

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("consentReceiptID")
  public String getConsentReceiptID() {
    return consentReceiptID;
  }
  public void setConsentReceiptID(String consentReceiptID) {
    this.consentReceiptID = consentReceiptID;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("language")
  public String getLanguage() {
    return language;
  }
  public void setLanguage(String language) {
    this.language = language;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("piiPrincipalId")
  public String getPiiPrincipalId() {
    return piiPrincipalId;
  }
  public void setPiiPrincipalId(String piiPrincipalId) {
    this.piiPrincipalId = piiPrincipalId;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("tenantDomain")
  public String getTenantDomain() {
    return tenantDomain;
  }
  public void setTenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("state")
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("spDisplayName")
  public String getSpDisplayName() {
    return spDisplayName;
  }
  public void setSpDisplayName(String spDisplayName) {
    this.spDisplayName = spDisplayName;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("spDescription")
  public String getSpDescription() {
    return spDescription;
  }
  public void setSpDescription(String spDescription) {
    this.spDescription = spDescription;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentResponseDTO {\n");
    
    sb.append("  consentReceiptID: ").append(consentReceiptID).append("\n");
    sb.append("  language: ").append(language).append("\n");
    sb.append("  piiPrincipalId: ").append(piiPrincipalId).append("\n");
    sb.append("  tenantDomain: ").append(tenantDomain).append("\n");
    sb.append("  state: ").append(state).append("\n");
    sb.append("  spDisplayName: ").append(spDisplayName).append("\n");
    sb.append("  spDescription: ").append(spDescription).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
