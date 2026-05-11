/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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

package org.wso2.carbon.consent.mgt.core.model;

import java.util.List;

/**
 * Holds a purpose UUID and its resolved PII categories for receipt input construction.
 */
public class PurposePIICategoryBinding {

    private final String purposeId;
    private final List<PIICategory> piiCategories;

    public PurposePIICategoryBinding(String purposeId, List<PIICategory> piiCategories) {

        this.purposeId = purposeId;
        this.piiCategories = piiCategories;
    }

    public String getPurposeId() {

        return purposeId;
    }

    public List<PIICategory> getPiiCategories() {

        return piiCategories;
    }
}
