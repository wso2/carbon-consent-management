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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The model representing a version of a consent purpose.
 */
public class PurposeVersion {

    private int purposeId;
    private String version;
    private String uuid;
    private String description;
    private int tenantId;
    private List<PurposePIICategory> purposePIICategories = new ArrayList<>();
    private Map<String, String> properties;

    public PurposeVersion() {

    }

    public PurposeVersion(int purposeId, String version, String description, int tenantId, String uuid) {

        this.purposeId = purposeId;
        this.version = version;
        this.description = description;
        this.tenantId = tenantId;
        this.uuid = uuid;
    }

    public int getPurposeId() {

        return purposeId;
    }

    public void setPurposeId(int purposeId) {

        this.purposeId = purposeId;
    }

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
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

    public List<PurposePIICategory> getPurposePIICategories() {

        return purposePIICategories;
    }

    public void setPurposePIICategories(List<PurposePIICategory> purposePIICategories) {

        this.purposePIICategories = purposePIICategories == null ? new ArrayList<>() : purposePIICategories;
    }

    public Map<String, String> getProperties() {

        return properties;
    }

    public void setProperties(Map<String, String> properties) {

        this.properties = properties;
    }
}
