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

/**
 * Model class for PIICategory entries in a Purpose.
 */
public class PurposePIICategory {

    private Integer id;
    private String name;
    private String description;
    private Boolean sensitive;
    private String displayName;
    private Boolean mandatory;

    public PurposePIICategory(Integer id, String name, String description, Boolean sensitive, String displayName,
                              Boolean mandatory) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sensitive = sensitive;

        this.displayName = displayName;
        this.mandatory = mandatory;
    }

    public PurposePIICategory(Integer id, Boolean mandatory) {
        this.id = id;
        this.mandatory = mandatory;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }
}
