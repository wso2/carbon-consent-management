package org.wso2.carbon.consent.mgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ConsentAddResponseDTO  {
  
  
  @NotNull
  private String consentReceiptID = null;
  
  
  private String language = null;
  
  @NotNull
  private String piiPrincipalId = null;
  
  @NotNull
  private String tenantDomain = null;

  
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

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentAddResponseDTO {\n");
    
    sb.append("  consentReceiptID: ").append(consentReceiptID).append("\n");
    sb.append("  language: ").append(language).append("\n");
    sb.append("  piiPrincipalId: ").append(piiPrincipalId).append("\n");
    sb.append("  tenantDomain: ").append(tenantDomain).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
