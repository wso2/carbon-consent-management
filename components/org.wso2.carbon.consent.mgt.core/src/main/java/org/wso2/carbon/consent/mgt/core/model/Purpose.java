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

package org.wso2.carbon.consent.mgt.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The model representing a purpose of a given consent.
 */
public class Purpose {

    private Integer id;
    private String name;
    private String description;
    private String group;
    private String groupType;
    private List<PIICategory> piiCategories =  new ArrayList<>();
    private List<PurposePIICategory> purposePIICategories = new ArrayList<>();
    private int tenantId;
    private String tenantDomain;

    public void setId(Integer id) {

        this.id = id;
    }

    public Purpose(Integer id, String name, String description, String group, String groupType, int tenantId) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.group = group;
        this.groupType = groupType;
        this.tenantId = tenantId;
    }

    public Purpose(Integer id, String name) {

        this.id = id;
        this.name = name;
    }

    public Purpose(String name, String description, String group, String groupType) {

        this.name = name;
        this.description = description;
        this.group = group;
        this.groupType = groupType;
    }

    public Purpose(String name, String description, String group, String groupType,
                   List<PurposePIICategory> purposePIICategories) {

        this.name = name;
        this.description = description;
        this.group = group;
        this.groupType = groupType;
        this.purposePIICategories = purposePIICategories;
    }

    public Purpose(String name, String description, String group, String groupType, int tenantId) {

        this.name = name;
        this.description = description;
        this.group = group;
        this.groupType = groupType;
        this.tenantId = tenantId;
    }

    public Purpose(Integer id, String name, String description, String group,
                   String groupType, int tenantId, List<PurposePIICategory> purposePIICategories) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.group = group;
        this.groupType = groupType;
        this.tenantId = tenantId;
        this.purposePIICategories = purposePIICategories;
    }

    public Purpose(Integer id, String name, String description, int tenantId) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.tenantId = tenantId;
    }

    public Integer getId() {

        return id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public int getTenantId() {

        return tenantId;
    }

    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    /**
     * This method is deprecated, use 'getPurposePIICategories' instead.
     */
    @Deprecated
    public List<PIICategory> getPiiCategories() {

        return piiCategories;
    }

    /**
     * This method is deprecated, use 'setPurposePIICategories' instead.
     */
    @Deprecated
    public void setPiiCategories(List<PIICategory> piiCategories) {

        this.piiCategories = piiCategories;
    }

    public List<PurposePIICategory> getPurposePIICategories() {

        return purposePIICategories;
    }

    public void setPurposePIICategories(List<PurposePIICategory> purposePIICategories) {

        this.purposePIICategories = purposePIICategories;
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
}
