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
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.InterceptingConsentManager;
import org.wso2.carbon.consent.mgt.core.connector.ConsentMgtInterceptor;
import org.wso2.carbon.consent.mgt.core.connector.PIIController;
import org.wso2.carbon.consent.mgt.core.connector.impl.DefaultPIIController;
import org.wso2.carbon.consent.mgt.core.constant.ConsentConstants;
import org.wso2.carbon.consent.mgt.core.dao.JdbcTemplate;
import org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.dao.ReceiptDAO;
import org.wso2.carbon.consent.mgt.core.dao.impl.PIICategoryDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.PurposeCategoryDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.PurposeDAOImpl;
import org.wso2.carbon.consent.mgt.core.dao.impl.ReceiptDAOImpl;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementRuntimeException;
import org.wso2.carbon.consent.mgt.core.model.ConsentManagerConfigurationHolder;
import org.wso2.carbon.consent.mgt.core.util.ConsentConfigParser;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * OSGi declarative services component which handles registration and un-registration of consent management service.
 */

@Component(
        name = "carbon.consent.mgt.component",
        immediate = true
)
public class ConsentManagerComponent {

    private static final Log log = LogFactory.getLog(ConsentManagerComponent.class);
    private List<PIIController> piiControllers = new ArrayList<>();
    private List<PurposeDAO> purposeDAOs = new ArrayList<>();
    private List<PIICategoryDAO> piiCategoryDAOs = new ArrayList<>();
    private List<PurposeCategoryDAO> purposeCategoryDAOs = new ArrayList<>();
    private List<ReceiptDAO> receiptDAOs = new ArrayList<>();
    private List<ConsentMgtInterceptor> consentMgtInterceptors = new ArrayList<>();
    private RealmService realmService;

    /**
     * Register ConsentManager as an OSGi service.
     *
     * @param componentContext OSGi service component context.
     */
    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            BundleContext bundleContext = componentContext.getBundleContext();
            ConsentConfigParser configParser = new ConsentConfigParser();
            DataSource dataSource = initDataSource(configParser);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            bundleContext.registerService(PIIController.class.getName(), new DefaultPIIController(configParser), null);
            bundleContext.registerService(PurposeDAO.class.getName(), new PurposeDAOImpl(jdbcTemplate), null);
            bundleContext.registerService(ReceiptDAO.class.getName(), new ReceiptDAOImpl(jdbcTemplate), null);
            bundleContext.registerService(PIICategoryDAO.class.getName(), new PIICategoryDAOImpl(jdbcTemplate), null);
            bundleContext.registerService(PurposeCategoryDAO.class.getName(), new PurposeCategoryDAOImpl
                    (jdbcTemplate), null);

            ConsentManagerConfigurationHolder configHolder = new ConsentManagerConfigurationHolder();
            configHolder.setPurposeDAOs(purposeDAOs);
            configHolder.setPurposeCategoryDAOs(purposeCategoryDAOs);
            configHolder.setPiiCategoryDAOs(piiCategoryDAOs);
            configHolder.setReceiptDAOs(receiptDAOs);
            configHolder.setConfigParser(configParser);
            configHolder.setPiiControllers(piiControllers);
            configHolder.setRealmService(realmService);

            bundleContext.registerService(ConsentManager.class.getName(), new InterceptingConsentManager
                    (configHolder, consentMgtInterceptors), null);
            log.info("ConsentManagerComponent is activated.");
        } catch (Throwable e) {
            log.error("Error while activating ConsentManagerComponent.", e);
        }

    }

    @Reference(
            name = "realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        this.realmService = realmService;
        if (realmService != null && log.isDebugEnabled()) {
            log.debug("RealmService is registered in ConsentManager service.");
        }
    }

    protected void unsetRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("RealmService is unregistered in ConsentManager service.");
        }
        this.realmService = null;
    }

    @Reference(
            name = "pii.controller",
            service = org.wso2.carbon.consent.mgt.core.connector.PIIController.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPIIController"
    )
    protected void setPIIController(PIIController piiController) {

        if (piiController != null) {
            if (log.isDebugEnabled()) {
                log.debug("PII Controller is registered in ConsentManager service.");
            }

            piiControllers.add(piiController);
            piiControllers.sort(Comparator.comparingInt(PIIController::getPriority));
        }
    }

    protected void unsetPIIController(PIIController piiController) {

        if (log.isDebugEnabled()) {
            log.debug("PII Controller is unregistered in ConsentManager service.");
        }
        piiControllers.remove(piiController);
    }

    @Reference(
            name = "purpose.dao",
            service = org.wso2.carbon.consent.mgt.core.dao.PurposeDAO.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPurpose"
    )
    protected void setPurpose(PurposeDAO purposeDAO) {

        if (purposeDAO != null) {
            if (log.isDebugEnabled()) {
                log.debug("Purpose DAO is registered in ConsentManager service.");
            }

            purposeDAOs.add(purposeDAO);
            purposeDAOs.sort(Comparator.comparingInt(PurposeDAO::getPriority));
        }
    }

    protected void unsetPurpose(PurposeDAO purposeDAO) {

        if (log.isDebugEnabled()) {
            log.debug("Purpose DAO is unregistered in ConsentManager service.");
        }
        purposeDAOs.remove(purposeDAO);
    }

    @Reference(
            name = "purposeCategory.dao",
            service = org.wso2.carbon.consent.mgt.core.dao.PurposeCategoryDAO.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPurposeCategory"
    )
    protected void setPurposeCategory(PurposeCategoryDAO purposeCategoryDAO) {

        if (purposeCategoryDAO != null) {
            if (log.isDebugEnabled()) {
                log.debug("Purpose Category DAO is registered in ConsentManager service.");
            }

            purposeCategoryDAOs.add(purposeCategoryDAO);
            purposeCategoryDAOs.sort(Comparator.comparingInt(PurposeCategoryDAO::getPriority));
        }
    }

    protected void unsetPurposeCategory(PurposeCategoryDAO piiCategoryDAO) {

        if (log.isDebugEnabled()) {
            log.debug(" Purpose Category DAO is unregistered in ConsentManager service.");
        }
        purposeCategoryDAOs.remove(piiCategoryDAO);
    }

    @Reference(
            name = "piiCategory.dao",
            service = org.wso2.carbon.consent.mgt.core.dao.PIICategoryDAO.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPIICategory"
    )
    protected void setPIICategory(PIICategoryDAO piiCategory) {

        if (piiCategory != null) {
            if (log.isDebugEnabled()) {
                log.debug("PII Category DAO is registered in ConsentManager service.");
            }

            piiCategoryDAOs.add(piiCategory);
            piiCategoryDAOs.sort(Comparator.comparingInt(PIICategoryDAO::getPriority));
        }
    }

    protected void unsetPIICategory(PIICategoryDAO piiCategoryDAO) {

        if (log.isDebugEnabled()) {
            log.debug(" PII Category DAO is unregistered in ConsentManager service.");
        }
        piiCategoryDAOs.remove(piiCategoryDAO);
    }

    @Reference(
            name = "receipt.dao",
            service = org.wso2.carbon.consent.mgt.core.dao.ReceiptDAO.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetReceiptDAO"
    )
    protected void setReceiptDAO(ReceiptDAO receiptDAO) {

        if (receiptDAO != null) {
            if (log.isDebugEnabled()) {
                log.debug("Receipt DAO is registered in ConsentManager service.");
            }

            receiptDAOs.add(receiptDAO);
            receiptDAOs.sort(Comparator.comparingInt(ReceiptDAO::getPriority));
        }
    }

    protected void unsetReceiptDAO(ReceiptDAO receiptDAO) {

        if (log.isDebugEnabled()) {
            log.debug(" Receipt DAO is unregistered in ConsentManager service.");
        }
        receiptDAOs.remove(receiptDAO);
    }

    @Reference(
            name = "consent.interceptor",
            service = org.wso2.carbon.consent.mgt.core.connector.ConsentMgtInterceptor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConsentInterceptor"
    )
    protected void setConsentInterceptor(ConsentMgtInterceptor interceptor) {

        if (interceptor != null) {
            if (log.isDebugEnabled()) {
                log.debug("Consent Management Interceptor is registered in ConsentManager service.");
            }

            consentMgtInterceptors.add(interceptor);
            consentMgtInterceptors.sort(Comparator.comparingInt(ConsentMgtInterceptor::getOrder));
        }
    }

    protected void unsetConsentInterceptor(ConsentMgtInterceptor interceptor) {

        if (log.isDebugEnabled()) {
            log.debug("Consent Management Interceptor is unregistered in ConsentManager service.");
        }
        consentMgtInterceptors.remove(interceptor);
    }

    private DataSource initDataSource(ConsentConfigParser configParser) {

        String dataSourceName = configParser.getConsentDataSource();
        DataSource dataSource;
        Context ctx;
        try {
            ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(dataSourceName);

            if (log.isDebugEnabled()) {
                log.debug(String.format("Data source: %s found in context.", dataSourceName));
            }

            return dataSource;
        } catch (NamingException e) {
            throw new ConsentManagementRuntimeException(ConsentConstants.ErrorMessages
                    .ERROR_CODE_DATABASE_INITIALIZATION.getMessage(),
                    ConsentConstants.ErrorMessages
                            .ERROR_CODE_DATABASE_INITIALIZATION.getCode(), e);
        }
    }
}
