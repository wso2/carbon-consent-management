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

    private List<PurposeDAO> purposeDAOs;
    private List<PurposeCategoryDAO> purposeCategoryDAOs;
    private List<PIICategoryDAO> piiCategoryDAOs;
    private List<ReceiptDAO> receiptDAOs;
    private ConsentConfigParser configParser;
    private List<PIIController> piiControllers;

    public List<PurposeDAO> getPurposeDAOs() {

        return purposeDAOs;
    }

    public void setPurposeDAOs(List<PurposeDAO> purposeDAOs) {

        this.purposeDAOs = purposeDAOs;
    }

    public List<PurposeCategoryDAO> getPurposeCategoryDAOs() {

        return purposeCategoryDAOs;
    }

    public void setPurposeCategoryDAOs(List<PurposeCategoryDAO> purposeCategoryDAOs) {

        this.purposeCategoryDAOs = purposeCategoryDAOs;
    }

    public List<PIICategoryDAO> getPiiCategoryDAOs() {

        return piiCategoryDAOs;
    }

    public void setPiiCategoryDAOs(List<PIICategoryDAO> piiCategoryDAOs) {

        this.piiCategoryDAOs = piiCategoryDAOs;
    }

    public List<ReceiptDAO> getReceiptDAOs() {

        return receiptDAOs;
    }

    public void setReceiptDAOs(List<ReceiptDAO> receiptDAOs) {

        this.receiptDAOs = receiptDAOs;
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
