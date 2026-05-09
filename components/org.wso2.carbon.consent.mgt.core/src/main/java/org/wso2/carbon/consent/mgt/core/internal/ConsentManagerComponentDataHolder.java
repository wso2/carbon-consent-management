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

import org.wso2.carbon.consent.mgt.core.listener.ConsentManagementListener;
import org.wso2.carbon.identity.event.services.IdentityEventService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;

/**
 * A class to keep the data of the consent manager component.
 */
public class ConsentManagerComponentDataHolder {

    private static ConsentManagerComponentDataHolder instance = new ConsentManagerComponentDataHolder();
    private DataSource dataSource;
    private IdentityEventService identityEventService;
    private List<ConsentManagementListener> consentManagementListeners = new ArrayList<>();

    public static ConsentManagerComponentDataHolder getInstance() {

        return instance;
    }

    public DataSource getDataSource() {

        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {

        this.dataSource = dataSource;
    }

    public IdentityEventService getIdentityEventService() {

        return identityEventService;
    }

    public void setIdentityEventService(IdentityEventService identityEventService) {

        this.identityEventService = identityEventService;
    }

    public List<ConsentManagementListener> getConsentManagementListeners() {

        return consentManagementListeners;
    }

    public void addConsentManagementListener(ConsentManagementListener listener) {

        consentManagementListeners.add(listener);
        consentManagementListeners.sort(Comparator.comparingInt(ConsentManagementListener::getDefaultOrderId));
    }

    public void removeConsentManagementListener(ConsentManagementListener listener) {

        consentManagementListeners.remove(listener);
    }
}
