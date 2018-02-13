package org.wso2.carbon.consent.mgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.consent.mgt.endpoint.dto.PropertyDTO;
import org.wso2.carbon.consent.mgt.endpoint.dto.ServiceDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ConsentRequestDTO  {
  
  
  
  private List<ServiceDTO> services = new ArrayList<ServiceDTO>();
  
  
  private String collectionMethod = null;
  
  
  private String jurisdiction = null;
  
  
  private String language = null;
  
  
  private String policyURL = null;
  
  
  private List<PropertyDTO> properties = new ArrayList<PropertyDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("services")
  public List<ServiceDTO> getServices() {
    return services;
  }
  public void setServices(List<ServiceDTO> services) {
    this.services = services;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("collectionMethod")
  public String getCollectionMethod() {
    return collectionMethod;
  }
  public void setCollectionMethod(String collectionMethod) {
    this.collectionMethod = collectionMethod;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("jurisdiction")
  public String getJurisdiction() {
    return jurisdiction;
  }
  public void setJurisdiction(String jurisdiction) {
    this.jurisdiction = jurisdiction;
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
  @ApiModelProperty(value = "")
  @JsonProperty("policyURL")
  public String getPolicyURL() {
    return policyURL;
  }
  public void setPolicyURL(String policyURL) {
    this.policyURL = policyURL;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("properties")
  public List<PropertyDTO> getProperties() {
    return properties;
  }
  public void setProperties(List<PropertyDTO> properties) {
    this.properties = properties;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentRequestDTO {\n");
    
    sb.append("  services: ").append(services).append("\n");
    sb.append("  collectionMethod: ").append(collectionMethod).append("\n");
    sb.append("  jurisdiction: ").append(jurisdiction).append("\n");
    sb.append("  language: ").append(language).append("\n");
    sb.append("  policyURL: ").append(policyURL).append("\n");
    sb.append("  properties: ").append(properties).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
