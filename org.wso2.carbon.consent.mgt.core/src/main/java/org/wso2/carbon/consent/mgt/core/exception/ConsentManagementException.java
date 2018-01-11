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
 * Base exception for consent management feature.
 */
public class ConsentManagementException extends Exception {

    private static final long serialVersionUID = 2806215535431246551L;
    private String errorCode;

    public ConsentManagementException() {
        super();
    }

    public ConsentManagementException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ConsentManagementException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ConsentManagementException(Throwable cause) {
        super(cause);
    }

    protected void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    protected String getErrorCode() {
        return errorCode;
    }
}
