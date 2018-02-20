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

public class PIICategory {

    private Integer id;
    private String name;
    private String description;
    private Boolean sensitive;
    private String displayName;

    private int tenantId;
    private String tenantDomain;

    public PIICategory(Integer id, String name, String description, Boolean sensitive, int tenantId, String displayName) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.sensitive = sensitive;
        this.tenantId = tenantId;
        this.displayName = displayName;
    }

    public PIICategory(String name, String description, Boolean sensitive, String displayName) {

        this.name = name;
        this.description = description;
        this.sensitive = sensitive;
        this.displayName = displayName;
    }

    public PIICategory(String name, String description, Boolean sensitive, int tenantId) {

        this.name = name;
        this.description = description;
        this.sensitive = sensitive;
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

    public Boolean getSensitive() {

        return sensitive;
    }

    public void setSensitive(Boolean sensitive) {

        this.sensitive = sensitive;
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

    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }
}
