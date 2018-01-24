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

package org.wso2.carbon.consent.mgt.core.dao;

import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;

import java.util.List;

public interface ReceiptDAO {

    int getPriority();

    void addReceipt(ReceiptInput receiptInput) throws ConsentManagementException;

    Receipt getReceipt(String receiptId) throws ConsentManagementException;

    void revokeReceipt(String receiptId) throws ConsentManagementException;

    List<ReceiptListResponse> searchReceipts(int limit, int offset, String piiPrincipalId, String spTenantDomain,
                                             String service, String state) throws ConsentManagementException;
}
