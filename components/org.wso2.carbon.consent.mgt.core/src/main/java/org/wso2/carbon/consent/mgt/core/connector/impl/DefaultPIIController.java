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

package org.wso2.carbon.consent.mgt.core.connector.impl;

import org.wso2.carbon.consent.mgt.core.connector.PIIController;
import org.wso2.carbon.consent.mgt.core.model.Address;
import org.wso2.carbon.consent.mgt.core.model.PiiController;
import org.wso2.carbon.consent.mgt.core.util.ConsentConfigParser;

import java.util.Map;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.COUNTRY_ELEMENT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.LOCALITY_ELEMENT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.PII_CONTROLLER_CONTACT_ELEMENT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.PII_CONTROLLER_EMAIL_ELEMENT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.PII_CONTROLLER_NAME_ELEMENT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.PII_CONTROLLER_ON_BEHALF_ELEMENT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.PII_CONTROLLER_PHONE_ELEMENT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.PII_CONTROLLER_PUBLIC_KEYE_LEMENT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.PII_CONTROLLER_URL_ELEMENT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.POST_CODE_ELEMENT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.POST_OFFICE_BOX_NUMBER_ELEMENT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.REGION_ELEMENT;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.STREET_ADDRESS_ELEMENT;

/**
 * This is the default implementation of PII controller
 */
public class DefaultPIIController implements PIIController {

    private final ConsentConfigParser configParser;

    public DefaultPIIController(ConsentConfigParser configParser) {

        this.configParser = configParser;
    }

    @Override
    public int getPriority() {

        return 1;
    }

    @Override
    public PiiController getControllerInfo(String tenantDomain) {

        String addressCountry = getConfiguration(COUNTRY_ELEMENT);
        String addressLocality = getConfiguration(LOCALITY_ELEMENT);
        String addressRegion = getConfiguration(REGION_ELEMENT);
        String addressPostOfficeBoxNumber = getConfiguration(POST_OFFICE_BOX_NUMBER_ELEMENT);
        String addressPostCode = getConfiguration(POST_CODE_ELEMENT);
        String addressStreetAddress = getConfiguration(STREET_ADDRESS_ELEMENT);

        String piiControllerName = getConfiguration(PII_CONTROLLER_NAME_ELEMENT);
        String piiControllerContact = getConfiguration(PII_CONTROLLER_CONTACT_ELEMENT);
        String piiControllerPhone = getConfiguration(PII_CONTROLLER_PHONE_ELEMENT);
        String piiControllerEmail = getConfiguration(PII_CONTROLLER_EMAIL_ELEMENT);
        boolean piiControllerOnBehalf = Boolean.parseBoolean(getConfiguration(PII_CONTROLLER_ON_BEHALF_ELEMENT));
        String piiControllerURL = getConfiguration(PII_CONTROLLER_URL_ELEMENT);
        String publicKey = getConfiguration(PII_CONTROLLER_PUBLIC_KEYE_LEMENT);

        Address address = new Address(addressCountry, addressLocality, addressRegion, addressPostOfficeBoxNumber,
                addressPostCode, addressStreetAddress);

        return new PiiController(piiControllerName, piiControllerOnBehalf, piiControllerContact, piiControllerEmail,
                piiControllerPhone, piiControllerURL, address, publicKey);
    }

    private String getConfiguration(String configElement) {

        Map<String, Object> configuration = configParser.getConfiguration();
        if (configuration.get(configElement) != null) {
            return configuration.get(configElement).toString();
        }
        return null;
    }
}
