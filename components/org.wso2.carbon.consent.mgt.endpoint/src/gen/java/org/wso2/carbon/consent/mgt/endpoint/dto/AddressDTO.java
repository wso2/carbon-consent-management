package org.wso2.carbon.consent.mgt.endpoint.dto;

import io.swagger.annotations.ApiModel;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * The physical address of PII controller. Postal address for contacting the PII Controller. This is complient with the schema https://schema.org/PostalAddress.
 **/


@ApiModel(description = "The physical address of PII controller. Postal address for contacting the PII Controller. This is complient with the schema https://schema.org/PostalAddress.")
public class AddressDTO  {
  
  
  
  private String addressCountry = null;
  
  
  private String addressLocality = null;
  
  
  private String addressRegion = null;
  
  
  private String postOfficeBoxNumber = null;
  
  
  private String postalCode = null;
  
  
  private String streetAddress = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("addressCountry")
  public String getAddressCountry() {
    return addressCountry;
  }
  public void setAddressCountry(String addressCountry) {
    this.addressCountry = addressCountry;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("addressLocality")
  public String getAddressLocality() {
    return addressLocality;
  }
  public void setAddressLocality(String addressLocality) {
    this.addressLocality = addressLocality;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("addressRegion")
  public String getAddressRegion() {
    return addressRegion;
  }
  public void setAddressRegion(String addressRegion) {
    this.addressRegion = addressRegion;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("postOfficeBoxNumber")
  public String getPostOfficeBoxNumber() {
    return postOfficeBoxNumber;
  }
  public void setPostOfficeBoxNumber(String postOfficeBoxNumber) {
    this.postOfficeBoxNumber = postOfficeBoxNumber;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("postalCode")
  public String getPostalCode() {
    return postalCode;
  }
  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("streetAddress")
  public String getStreetAddress() {
    return streetAddress;
  }
  public void setStreetAddress(String streetAddress) {
    this.streetAddress = streetAddress;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class AddressDTO {\n");
    
    sb.append("  addressCountry: ").append(addressCountry).append("\n");
    sb.append("  addressLocality: ").append(addressLocality).append("\n");
    sb.append("  addressRegion: ").append(addressRegion).append("\n");
    sb.append("  postOfficeBoxNumber: ").append(postOfficeBoxNumber).append("\n");
    sb.append("  postalCode: ").append(postalCode).append("\n");
    sb.append("  streetAddress: ").append(streetAddress).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
