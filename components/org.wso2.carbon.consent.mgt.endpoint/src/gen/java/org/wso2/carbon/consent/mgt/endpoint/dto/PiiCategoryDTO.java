package org.wso2.carbon.consent.mgt.endpoint.dto;

import io.swagger.annotations.ApiModel;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * PII Category
 **/


@ApiModel(description = "PII Category")
public class PiiCategoryDTO  {
  

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class PiiCategoryDTO {\n");
    
    sb.append("}\n");
    return sb.toString();
  }
}
