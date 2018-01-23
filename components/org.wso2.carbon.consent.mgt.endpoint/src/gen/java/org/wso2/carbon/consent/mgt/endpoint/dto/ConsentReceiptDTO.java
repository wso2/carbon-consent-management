package org.wso2.carbon.consent.mgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.consent.mgt.endpoint.dto.PiiControllerDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ServiceResponseDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ConsentReceiptDTO  {
  
  
  @NotNull
  private String version = null;
  
  @NotNull
  private String jurisdiction = null;
  
  @NotNull
  private Long consentTimestamp = null;
  
  @NotNull
  private String collectionMethod = null;
  
  @NotNull
  private String consentReceiptID = null;
  
  
  private String publicKey = null;
  
  
  private String language = null;
  
  @NotNull
  private String piiPrincipalId = null;
  
  
  private String tenantDomain = null;
  
  @NotNull
  private List<PiiControllerDTO> piiControllers = new ArrayList<PiiControllerDTO>();
  
  @NotNull
  private String policyUrl = null;
  
  @NotNull
  private List<ServiceResponseDTO> services = new ArrayList<ServiceResponseDTO>();
  
  @NotNull
  private Boolean sensitive = null;
  
  @NotNull
  private List<String> spiCat = new ArrayList<String>();

  
  /**
   * The version of this specification to which a receipt conforms. The value MUST be \u201CKI-CR-v1.1.0\u201D for this version of the specification.
   **/
  @ApiModelProperty(required = true, value = "The version of this specification to which a receipt conforms. The value MUST be \u201CKI-CR-v1.1.0\u201D for this version of the specification.")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  
  /**
   * The jurisdiction(s) applicable to this transaction.
   **/
  @ApiModelProperty(required = true, value = "The jurisdiction(s) applicable to this transaction.")
  @JsonProperty("jurisdiction")
  public String getJurisdiction() {
    return jurisdiction;
  }
  public void setJurisdiction(String jurisdiction) {
    this.jurisdiction = jurisdiction;
  }

  
  /**
   * Date and time of the consent transaction. The JSON value is expressed as the number of seconds since 1970-01-01 00:00:00 GMT
   **/
  @ApiModelProperty(required = true, value = "Date and time of the consent transaction. The JSON value is expressed as the number of seconds since 1970-01-01 00:00:00 GMT")
  @JsonProperty("consentTimestamp")
  public Long getConsentTimestamp() {
    return consentTimestamp;
  }
  public void setConsentTimestamp(Long consentTimestamp) {
    this.consentTimestamp = consentTimestamp;
  }

  
  /**
   * A description of the method by which consent was obtained.
   **/
  @ApiModelProperty(required = true, value = "A description of the method by which consent was obtained.")
  @JsonProperty("collectionMethod")
  public String getCollectionMethod() {
    return collectionMethod;
  }
  public void setCollectionMethod(String collectionMethod) {
    this.collectionMethod = collectionMethod;
  }

  
  /**
   * A unique number for each Consent Receipt.
   **/
  @ApiModelProperty(required = true, value = "A unique number for each Consent Receipt.")
  @JsonProperty("consentReceiptID")
  public String getConsentReceiptID() {
    return consentReceiptID;
  }
  public void setConsentReceiptID(String consentReceiptID) {
    this.consentReceiptID = consentReceiptID;
  }

  
  /**
   * The PII Controller\u2019s public key.
   **/
  @ApiModelProperty(value = "The PII Controller\u2019s public key.")
  @JsonProperty("publicKey")
  public String getPublicKey() {
    return publicKey;
  }
  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  
  /**
   * Language in which the consent was obtained as for ISO 639-1:2002 [ISO 639].
   **/
  @ApiModelProperty(value = "Language in which the consent was obtained as for ISO 639-1:2002 [ISO 639].")
  @JsonProperty("language")
  public String getLanguage() {
    return language;
  }
  public void setLanguage(String language) {
    this.language = language;
  }

  
  /**
   * PII Principal-provided identifier. E.g., email address.
   **/
  @ApiModelProperty(required = true, value = "PII Principal-provided identifier. E.g., email address.")
  @JsonProperty("piiPrincipalId")
  public String getPiiPrincipalId() {
    return piiPrincipalId;
  }
  public void setPiiPrincipalId(String piiPrincipalId) {
    this.piiPrincipalId = piiPrincipalId;
  }

  
  /**
   * Tenant domain of the service (SP).
   **/
  @ApiModelProperty(value = "Tenant domain of the service (SP).")
  @JsonProperty("tenantDomain")
  public String getTenantDomain() {
    return tenantDomain;
  }
  public void setTenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
  }

  
  /**
   * An array that contains one or more items where each item represents one PII Controller.
   **/
  @ApiModelProperty(required = true, value = "An array that contains one or more items where each item represents one PII Controller.")
  @JsonProperty("piiControllers")
  public List<PiiControllerDTO> getPiiControllers() {
    return piiControllers;
  }
  public void setPiiControllers(List<PiiControllerDTO> piiControllers) {
    this.piiControllers = piiControllers;
  }

  
  /**
   * A link to the PII Controller\u2019s privacy statement/policy and applicable terms of use in effect when the consent was obtained, and the receipt was issued.
   **/
  @ApiModelProperty(required = true, value = "A link to the PII Controller\u2019s privacy statement/policy and applicable terms of use in effect when the consent was obtained, and the receipt was issued.")
  @JsonProperty("policyUrl")
  public String getPolicyUrl() {
    return policyUrl;
  }
  public void setPolicyUrl(String policyUrl) {
    this.policyUrl = policyUrl;
  }

  
  /**
   * An array that contains one or more items where each item represents one Service.
   **/
  @ApiModelProperty(required = true, value = "An array that contains one or more items where each item represents one Service.")
  @JsonProperty("services")
  public List<ServiceResponseDTO> getServices() {
    return services;
  }
  public void setServices(List<ServiceResponseDTO> services) {
    this.services = services;
  }

  
  /**
   * Indicates whether the consent interaction contains PII that is designated sensitive or not sensitive.
   **/
  @ApiModelProperty(required = true, value = "Indicates whether the consent interaction contains PII that is designated sensitive or not sensitive.")
  @JsonProperty("sensitive")
  public Boolean getSensitive() {
    return sensitive;
  }
  public void setSensitive(Boolean sensitive) {
    this.sensitive = sensitive;
  }

  
  /**
   * A listing of categories where PII data collected is sensitive.
   **/
  @ApiModelProperty(required = true, value = "A listing of categories where PII data collected is sensitive.")
  @JsonProperty("spiCat")
  public List<String> getSpiCat() {
    return spiCat;
  }
  public void setSpiCat(List<String> spiCat) {
    this.spiCat = spiCat;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentReceiptDTO {\n");
    
    sb.append("  version: ").append(version).append("\n");
    sb.append("  jurisdiction: ").append(jurisdiction).append("\n");
    sb.append("  consentTimestamp: ").append(consentTimestamp).append("\n");
    sb.append("  collectionMethod: ").append(collectionMethod).append("\n");
    sb.append("  consentReceiptID: ").append(consentReceiptID).append("\n");
    sb.append("  publicKey: ").append(publicKey).append("\n");
    sb.append("  language: ").append(language).append("\n");
    sb.append("  piiPrincipalId: ").append(piiPrincipalId).append("\n");
    sb.append("  tenantDomain: ").append(tenantDomain).append("\n");
    sb.append("  piiControllers: ").append(piiControllers).append("\n");
    sb.append("  policyUrl: ").append(policyUrl).append("\n");
    sb.append("  services: ").append(services).append("\n");
    sb.append("  sensitive: ").append(sensitive).append("\n");
    sb.append("  spiCat: ").append(spiCat).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
