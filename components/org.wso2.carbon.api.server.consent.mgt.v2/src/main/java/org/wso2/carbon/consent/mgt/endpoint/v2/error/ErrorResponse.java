/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.consent.mgt.endpoint.v2.error;

import org.apache.commons.logging.Log;
import org.slf4j.MDC;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;

/**
 * Common ErrorResponse Object for all the server API related errors.
 */
public class ErrorResponse {

    private String code;
    private String message;
    private String description;
    private String ref;

    public ErrorResponse() {
    }

    public ErrorResponse(String code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * ErrorResponse Builder.
     */
    public static class Builder {
        private String code;
        private String message;
        private String description;

        public Builder() {
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public ErrorResponse build() {
            ErrorResponse error = new ErrorResponse();
            error.setCode(this.code);
            error.setMessage(this.message);
            error.setDescription(this.description);
            error.setRef(getCorrelation());
            return error;
        }

        public ErrorResponse build(Log log, Exception e, String message) {
            ErrorResponse error = build();
            String errorMessageFormat = "errorCode: %s | message: %s";
            String errorMsg = String.format(errorMessageFormat, error.getCode(), message);
            if (!isCorrelationIDPresent()) {
                errorMsg = String.format("correlationID: %s | " + errorMsg, error.getRef());
            }
            log.error(errorMsg, e);
            return error;
        }

        /**
         * Error response builder for bad requests without exceptions.
         *
         * @param log Logger.
         * @param message Error message.
         * @return ErrorResponse object.
         */
        public ErrorResponse build(Log log, String message) {
            ErrorResponse error = build();
            String errorMessageFormat = "errorCode: %s | message: %s";
            String errorMsg = String.format(errorMessageFormat, error.getCode(), message);
            if (!isCorrelationIDPresent()) {
                errorMsg = String.format("correlationID: %s | " + errorMsg, error.getRef());
            }

            if (log.isDebugEnabled()) {
                log.debug(errorMsg);
            }
            return error;
        }

        /**
         * Check whether correlation id present in the log MDC
         *
         * @return whether the correlation id is present
         */
        private static boolean isCorrelationIDPresent() {
            return MDC.get(ConsentConstants.CORRELATION_ID_MDC) != null;
        }

        /**
         * Get correlation id of current thread
         *
         * @return correlation-id
         */
        private static String getCorrelation() {
            String ref = null;
            if (isCorrelationIDPresent()) {
                ref = MDC.get(ConsentConstants.CORRELATION_ID_MDC);
            }
            return ref;
        }
    }
}
