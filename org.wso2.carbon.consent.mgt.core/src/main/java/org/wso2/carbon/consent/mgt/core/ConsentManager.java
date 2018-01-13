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

package org.wso2.carbon.consent.mgt.core;

import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.internal.ConsentManagerConfiguration;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;

/**
 * Consent manager service implementation.
 */
public class ConsentManager {

    private PurposeDAO purposeDAO;
    private PurposeCategoryDAO purposeCategoryDAO;
    private PIICategoryDAO piiCategoryDAO;

    public ConsentManager(ConsentManagerConfiguration configuration) {
        this.purposeDAO = configuration.getPurposeDAO();
        purposeCategoryDAO = configuration.getPurposeCategoryDAO();
        piiCategoryDAO = configuration.getPiiCategoryDAO();
    }

    public Purpose addPurpose(Purpose purpose) throws ConsentManagementException {
        return purposeDAO.addPurpose(purpose);
    }

    public PurposeCategory addPurposeCategory(PurposeCategory purposeCategory) throws ConsentManagementException {
        return purposeCategoryDAO.addPurposeCategory(purposeCategory);
    }

    public PIICategory addPIICategory(PIICategory piiCategory) throws ConsentManagementException {
        return piiCategoryDAO.addPIICategory(piiCategory);
    }
}
