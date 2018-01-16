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
  
  
  
  private String piiPrincipalId = null;
  
  
  private List<ServiceDTO> services = new ArrayList<ServiceDTO>();
  
  
  private String collectionMethod = null;
  
  
  private String language = null;
  
  
  private List<PropertyDTO> properties = new ArrayList<PropertyDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("piiPrincipalId")
  public String getPiiPrincipalId() {
    return piiPrincipalId;
  }
  public void setPiiPrincipalId(String piiPrincipalId) {
    this.piiPrincipalId = piiPrincipalId;
  }

  
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
    
    sb.append("  piiPrincipalId: ").append(piiPrincipalId).append("\n");
    sb.append("  services: ").append(services).append("\n");
    sb.append("  collectionMethod: ").append(collectionMethod).append("\n");
    sb.append("  language: ").append(language).append("\n");
    sb.append("  properties: ").append(properties).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
