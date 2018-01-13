/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.consent.mgt.core.exception;

/**
 * This class is an {@link RuntimeException} which is idint
 */
public class ConsentManagementRuntimeException extends RuntimeException {

    private String errorCode;

    public ConsentManagementRuntimeException() {
        super();
    }

    public ConsentManagementRuntimeException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ConsentManagementRuntimeException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ConsentManagementRuntimeException(Throwable cause) {
        super(cause);
    }

    public ConsentManagementRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean
            writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    protected String getErrorCode() {
        return errorCode;
    }

    protected void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}