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

package org.wso2.carbon.consent.mgt.core.dao.impl;

import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;

/**
 * Default implementation of {@link PurposeCategoryDAO}. This handles {@link PurposeCategory} related DB operations.
 */
public class PurposeCategoryDAOImpl implements PurposeCategoryDAO {

    @Override
    public PurposeCategory addPurposeCategory(PurposeCategory purposeCategory) throws ConsentManagementException {
        return null;
    }

    @Override
    public PurposeCategory getPurposeCategoryByName(String name) throws ConsentManagementException {
        return null;
    }

    @Override
    public PurposeCategory getPurposeCategoryById(int id) throws ConsentManagementException {
        return null;
    }
}
