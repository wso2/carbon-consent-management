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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.dao.impl.PIICategoryDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.PurposeCategoryDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.PurposeDAOImpl;
/**
 * OSGi declarative services component which handles registration and un-registration of consent management service.
 */

@Component(
        name = "carbon.consent.mgt.component",
        immediate = true
)
public class ConsentManagerComponent {

    private static final Log log = LogFactory.getLog(ConsentManagerComponent.class);
    /**
     *
     * Register ConsentManager as an OSGi service.
     *
     * @param componentContext OSGi service component context.
     */
    @Activate
    protected void activate(ComponentContext componentContext) {

        try {

            BundleContext bundleContext = componentContext.getBundleContext();
            PurposeDAO purposeDAO = new PurposeDAOImpl();
            PurposeCategoryDAO purposeCategoryDAO = new PurposeCategoryDAOImpl();
            PIICategoryDAO piiCategoryDAO = new PIICategoryDAOImpl();

            ConsentManagerConfiguration configurations = new ConsentManagerConfiguration();
            configurations.setPurposeDAO(purposeDAO);
            configurations.setPurposeCategoryDAO(purposeCategoryDAO);
            configurations.setPiiCategoryDAO(piiCategoryDAO);

            bundleContext.registerService(ConsentManager.class.getName(), new ConsentManager(configurations), null);
            log.info("ConsentManagerComponent is activated.");
        } catch (Throwable e) {
            log.error("Error while activating ConsentManagerComponent.", e);
        }

    }
}
