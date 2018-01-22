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

package org.wso2.carbon.consent.mgt.core.internal;

import org.wso2.carbon.consent.mgt.core.connector.PIIController;
import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.dao.ReceiptDAO;
import org.wso2.carbon.consent.mgt.core.util.ConsentConfigParser;

import java.util.List;

public class ConsentManagerConfiguration {

    private PurposeDAO purposeDAO;
    private PurposeCategoryDAO purposeCategoryDAO;
    private PIICategoryDAO piiCategoryDAO;
    private ReceiptDAO receiptDAO;
    private ConsentConfigParser configParser;
    private List<PIIController> piiControllers;

    public PurposeDAO getPurposeDAO() {

        return purposeDAO;
    }

    public void setPurposeDAO(PurposeDAO purposeDAO) {

        this.purposeDAO = purposeDAO;
    }

    public PurposeCategoryDAO getPurposeCategoryDAO() {

        return purposeCategoryDAO;
    }

    public void setPurposeCategoryDAO(PurposeCategoryDAO purposeCategoryDAO) {

        this.purposeCategoryDAO = purposeCategoryDAO;
    }

    public PIICategoryDAO getPiiCategoryDAO() {

        return piiCategoryDAO;
    }

    public void setPiiCategoryDAO(PIICategoryDAO piiCategoryDAO) {

        this.piiCategoryDAO = piiCategoryDAO;
    }

    public ReceiptDAO getReceiptDAO() {

        return receiptDAO;
    }

    public void setReceiptDAO(ReceiptDAO receiptDAO) {

        this.receiptDAO = receiptDAO;
    }

    public ConsentConfigParser getConfigParser() {

        return configParser;
    }

    public void setConfigParser(ConsentConfigParser configParser) {

        this.configParser = configParser;
    }

    public List<PIIController> getPiiControllers() {

        return piiControllers;
    }

    public void setPiiControllers(List<PIIController> piiControllers) {

        this.piiControllers = piiControllers;
    }
}
