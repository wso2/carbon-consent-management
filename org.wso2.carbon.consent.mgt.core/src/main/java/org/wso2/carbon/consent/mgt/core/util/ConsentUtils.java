package org.wso2.carbon.consent.mgt.core.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;

/**
 * This class is used to define the Utilities require in consent management feature.
 */
public class ConsentUtils {

    /**
     * This method can be used to generate a ConsentManagementServerException from ConsentConstants.ErrorMessages
     * object when no exception is thrown.
     * @param error ConsentConstants.ErrorMessages.
     * @param data data to replace if message needs to be replaced.
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
     * @param error ConsentConstants.ErrorMessages.
     * @param data data to replace if message needs to be replaced.
     * @param e Parent exception.
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
     * @param error ConsentConstants.ErrorMessages.
     * @param data data to replace if message needs to be replaced.
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
     * @param error ConsentConstants.ErrorMessages.
     * @param data data to replace if message needs to be replaced.
     * @param e Parent exception.
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
}
