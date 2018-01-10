package org.wso2.carbon.consent.mgt.endpoint.factories;

import org.wso2.carbon.consent.mgt.endpoint.ConsentsApiService;
import org.wso2.carbon.consent.mgt.endpoint.impl.ConsentsApiServiceImpl;

public class ConsentsApiServiceFactory {

   private final static ConsentsApiService service = new ConsentsApiServiceImpl();

   public static ConsentsApiService getConsentsApi()
   {
      return service;
   }
}
