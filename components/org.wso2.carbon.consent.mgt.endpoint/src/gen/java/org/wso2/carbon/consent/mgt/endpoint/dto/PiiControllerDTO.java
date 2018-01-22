package org.wso2.carbon.consent.mgt.endpoint.dto;

import org.wso2.carbon.consent.mgt.endpoint.dto.AddressDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class PiiControllerDTO  {
  
  
  @NotNull
  private String piiController = null;
  
  @NotNull
  private String contact = null;
  
  @NotNull
  private AddressDTO address = null;
  
  @NotNull
  private String email = null;
  
  @NotNull
  private String phone = null;
  
  
  private String onBehalf = null;
  
  
  private String piiControllerUrl = null;

  
  /**
   * Name of the first PII Controller who collects the data. This entity is accountable for compliance with the management of PII. The PII Controller determines the purpose(s) and type(s) of PII processing.
   **/
  @ApiModelProperty(required = true, value = "Name of the first PII Controller who collects the data. This entity is accountable for compliance with the management of PII. The PII Controller determines the purpose(s) and type(s) of PII processing.")
  @JsonProperty("piiController")
  public String getPiiController() {
    return piiController;
  }
  public void setPiiController(String piiController) {
    this.piiController = piiController;
  }

  
  /**
   * Contact name of the PII Controller. This field MUST contain a non-empty string.
   **/
  @ApiModelProperty(required = true, value = "Contact name of the PII Controller. This field MUST contain a non-empty string.")
  @JsonProperty("contact")
  public String getContact() {
    return contact;
  }
  public void setContact(String contact) {
    this.contact = contact;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("address")
  public AddressDTO getAddress() {
    return address;
  }
  public void setAddress(AddressDTO address) {
    this.address = address;
  }

  
  /**
   * Contact email address of the PII Controller. The direct email to contact the PII Controller regarding the consent or privacy contract.
   **/
  @ApiModelProperty(required = true, value = "Contact email address of the PII Controller. The direct email to contact the PII Controller regarding the consent or privacy contract.")
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }

  
  /**
   * Contact phone number of the PII Controller. The business phone number to contact the PII Controller regarding the consent.
   **/
  @ApiModelProperty(required = true, value = "Contact phone number of the PII Controller. The business phone number to contact the PII Controller regarding the consent.")
  @JsonProperty("phone")
  public String getPhone() {
    return phone;
  }
  public void setPhone(String phone) {
    this.phone = phone;
  }

  
  /**
   * A PII Processor acting on behalf of a PII Controller or PII Processor. For example, a third-party analytics service would be a PII Processor on behalf of the PII Controller, or a site operator acting on behalf of the PII Controller.
   **/
  @ApiModelProperty(value = "A PII Processor acting on behalf of a PII Controller or PII Processor. For example, a third-party analytics service would be a PII Processor on behalf of the PII Controller, or a site operator acting on behalf of the PII Controller.")
  @JsonProperty("onBehalf")
  public String getOnBehalf() {
    return onBehalf;
  }
  public void setOnBehalf(String onBehalf) {
    this.onBehalf = onBehalf;
  }

  
  /**
   * A URL for contacting the PII Controller.
   **/
  @ApiModelProperty(value = "A URL for contacting the PII Controller.")
  @JsonProperty("piiControllerUrl")
  public String getPiiControllerUrl() {
    return piiControllerUrl;
  }
  public void setPiiControllerUrl(String piiControllerUrl) {
    this.piiControllerUrl = piiControllerUrl;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PiiControllerDTO {\n");
    
    sb.append("  piiController: ").append(piiController).append("\n");
    sb.append("  contact: ").append(contact).append("\n");
    sb.append("  address: ").append(address).append("\n");
    sb.append("  email: ").append(email).append("\n");
    sb.append("  phone: ").append(phone).append("\n");
    sb.append("  onBehalf: ").append(onBehalf).append("\n");
    sb.append("  piiControllerUrl: ").append(piiControllerUrl).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
