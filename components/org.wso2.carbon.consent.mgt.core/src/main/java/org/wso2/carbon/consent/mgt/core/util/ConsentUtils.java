package org.wso2.carbon.consent.mgt.core.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementRuntimeException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_INVALID_TENANT_DOMAIN;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_INVALID_TENANT_ID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_RETRIEVE_TENANT_DOMAIN;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_RETRIEVE_TENANT_ID;

/**
 * This class is used to define the Utilities require in consent management feature.
 */
public class ConsentUtils {

    /**
     * This method can be used to generate a ConsentManagementServerException from ConsentConstants.ErrorMessages
     * object when no exception is thrown.
     *
     * @param error ConsentConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return ConsentManagementServerException.
     */
    public static ConsentManagementServerException handleServerException(ConsentConstants.ErrorMessages error,
                                                                         String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return new ConsentManagementServerException(message, error.getCode());
    }

    /**
     * This method can be used to generate a ConsentManagementServerException from ConsentConstants.ErrorMessages
     * object when an exception is thrown.
     *
     * @param error ConsentConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @param e     Parent exception.
     * @return ConsentManagementServerException
     */
    public static ConsentManagementServerException handleServerException(ConsentConstants.ErrorMessages error,
                                                                         String data, Throwable e) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return new ConsentManagementServerException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a ConsentManagementClientException from ConsentConstants.ErrorMessages
     * object when no exception is thrown.
     *
     * @param error ConsentConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return ConsentManagementClientException.
     */
    public static ConsentManagementClientException handleClientException(ConsentConstants.ErrorMessages error,
                                                                         String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }

        return new ConsentManagementClientException(message, error.getCode());
    }

    /**
     * This method can be used to generate a ConsentManagementClientException from ConsentConstants.ErrorMessages
     * object when an exception is thrown.
     *
     * @param error ConsentConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @param e     Parent exception.
     * @return ConsentManagementClientException
     */
    public static ConsentManagementClientException handleClientException(ConsentConstants.ErrorMessages error,
                                                                         String data, Throwable e) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return new ConsentManagementClientException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a ConsentManagementRuntimeException from ConsentConstants.ErrorMessages
     * object when an exception is thrown.
     *
     * @param error ConsentConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @param e     Parent exception.
     * @return ConsentManagementRuntimeException
     */
    public static ConsentManagementRuntimeException handleRuntimeException(ConsentConstants.ErrorMessages error,
                                                                         String data, Throwable e) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return new ConsentManagementRuntimeException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a ConsentManagementRuntimeException from ConsentConstants.ErrorMessages
     * object when an exception is thrown.
     *
     * @param error ConsentConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return ConsentManagementRuntimeException
     */
    public static ConsentManagementRuntimeException handleRuntimeException(ConsentConstants.ErrorMessages error,
                                                                           String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return new ConsentManagementRuntimeException(message, error.getCode());
    }

    /**
     * Get tenant id corresponding to a tenant domain.
     *
     * @param realmService Realm service.
     * @param tenantDomain Tenant domain.
     * @return Tenant ID of the given tenant domain.
     * @throws ConsentManagementRuntimeException If the tenant domain is invalid.
     */
    public static int getTenantId(RealmService realmService, String tenantDomain) throws
            ConsentManagementException {

        int tenantId;
        try {
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_RETRIEVE_TENANT_ID, tenantDomain, e);
        }

        if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            throw handleClientException(ERROR_CODE_INVALID_TENANT_DOMAIN, tenantDomain);
        }
        return tenantId;
    }

    /**
     * Get tenant domain corresponding to a tenant id.
     *
     * @param realmService Realm service.
     * @param tenantId Tenant ID.
     * @return Tenant domain of teh given tenant id.
     * @throws ConsentManagementServerException If the tenant id is invalid.
     */
    public static String getTenantDomain(RealmService realmService, int tenantId) throws
            ConsentManagementServerException {

        String tenantDomain;
        try {
            tenantDomain = realmService.getTenantManager().getDomain(tenantId);
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_RETRIEVE_TENANT_DOMAIN, Integer.toString(tenantId), e);
        }
        if (tenantDomain == null) {
            throw handleServerException(ERROR_CODE_INVALID_TENANT_ID, Integer.toString(tenantId));
        } else {
            return tenantDomain;
        }
    }

    /**
     * Get the tenant domain from carbon context.
     *
     * @return Tenant domain.
     */
    public static String getTenantDomainFromCarbonContext() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    /**
     * Get the tenant id from carbon context.
     *
     * @return Tenant id.
     */
    public static int getTenantIdFromCarbonContext() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }
}
