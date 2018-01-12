package org.wso2.carbon.consent.mgt.endpoint.impl.util;

import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class ConsentEndpointUtils {

    public static ConsentManager getConsentManager() {
        return (ConsentManager) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(ConsentManager.class, null);
    }
}
