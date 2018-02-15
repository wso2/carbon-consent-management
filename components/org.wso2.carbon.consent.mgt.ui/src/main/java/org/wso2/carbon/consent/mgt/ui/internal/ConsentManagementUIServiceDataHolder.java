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

package org.wso2.carbon.consent.mgt.ui.internal;

import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * This singleton data holder contains all the data required by the Consent UI OSGi bundle
 */
public class ConsentManagementUIServiceDataHolder {

    private static ConsentManagementUIServiceDataHolder instance = new ConsentManagementUIServiceDataHolder();


    private ConsentManager consentManager;
    private RealmService realmService;

    private ConsentManagementUIServiceDataHolder() {

    }

    public static ConsentManagementUIServiceDataHolder getInstance() {

        return instance;
    }

    public ConsentManager getConsentManager() {

        return consentManager;
    }

    public void setConsentManager(ConsentManager consentManager) {

        this.consentManager = consentManager;
    }

    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }
}
