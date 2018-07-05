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
 * limitations under the License
 */

package org.wso2.carbon.consent.mgt.ui.dto;

import java.util.ArrayList;
import java.util.List;

public class PurposeRequestDTO {

    private String purpose = null;
    private String description = null;
    private String group = null;
    private String groupType = null;
    private boolean mandatory = false;
    private List<PiiCategoryDTO> piiCategories = new ArrayList<PiiCategoryDTO>();

    public String getPurpose() {

        return purpose;
    }

    public void setPurpose(String purpose) {

        this.purpose = purpose;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public List<PiiCategoryDTO> getPiiCategories() {

        return piiCategories;
    }

    public void setPiiCategories(List<PiiCategoryDTO> piiCategories) {

        this.piiCategories = piiCategories;
    }

    public String getGroup() {

        return group;
    }

    public void setGroup(String group) {

        this.group = group;
    }

    public String getGroupType() {

        return groupType;
    }

    public void setGroupType(String groupType) {

        this.groupType = groupType;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }
}
