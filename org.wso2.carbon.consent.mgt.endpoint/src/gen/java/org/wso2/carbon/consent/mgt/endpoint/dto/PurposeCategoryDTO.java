package org.wso2.carbon.consent.mgt.endpoint.dto;

import io.swagger.annotations.ApiModel;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * The reason the PII Controller is collecting the PII.
 **/


@ApiModel(description = "The reason the PII Controller is collecting the PII.")
public class PurposeCategoryDTO  {
  

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PurposeCategoryDTO {\n");
    
    sb.append("}\n");
    return sb.toString();
  }
}
