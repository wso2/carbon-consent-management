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

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.countryElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.localityElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.piiControllerContactElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.piiControllerEmailElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.piiControllerNameElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.piiControllerOnBehalfElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.piiControllerPhoneElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.piiControllerPublicKeyElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.piiControllerUrlElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.postCodeElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.postOfficeBoxNumberElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.regionElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.PIIControllerElements.streetAddressElement;

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

        String addressCountry = getConfiguration(countryElement);
        String addressLocality = getConfiguration(localityElement);
        String addressRegion = getConfiguration(regionElement);
        String addressPostOfficeBoxNumber = getConfiguration(postOfficeBoxNumberElement);
        String addressPostCode = getConfiguration(postCodeElement);
        String addressStreetAddress = getConfiguration(streetAddressElement);

        String piiControllerName = getConfiguration(piiControllerNameElement);
        String piiControllerContact = getConfiguration(piiControllerContactElement);
        String piiControllerPhone = getConfiguration(piiControllerPhoneElement);
        String piiControllerEmail = getConfiguration(piiControllerEmailElement);
        boolean piiControllerOnBehalf = Boolean.parseBoolean(getConfiguration(piiControllerOnBehalfElement));
        String piiControllerURL = getConfiguration(piiControllerUrlElement);
        String publicKey = getConfiguration(piiControllerPublicKeyElement);

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
