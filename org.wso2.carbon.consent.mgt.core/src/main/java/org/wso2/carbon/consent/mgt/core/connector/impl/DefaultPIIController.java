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

import static org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants.PIIControllerElements.countryElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants.PIIControllerElements.localityElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants.PIIControllerElements.piiControllerContactElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants.PIIControllerElements.piiControllerEmailElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants.PIIControllerElements.piiControllerNameElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants.PIIControllerElements.piiControllerPhoneElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants.PIIControllerElements.postCodeElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants.PIIControllerElements.postOfficeBoxNumberElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants.PIIControllerElements.regionElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants.PIIControllerElements.streetAddressElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants.PIIControllerElements.piiControllerOnBehalfElement;
import static org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants.PIIControllerElements.piiControllerUrlElement;

/**
 * This is the default implementation of PII controller
 */
public class DefaultPIIController implements PIIController {
    @Override
    public PiiController getControllerInfo(String tenantDomain) {

        //TODO need to verify inputs

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

        Address address = new Address(addressCountry, addressLocality, addressRegion, addressPostOfficeBoxNumber,
                addressPostCode, addressStreetAddress);

        return new PiiController(piiControllerName, piiControllerOnBehalf, piiControllerContact, piiControllerEmail,
                piiControllerPhone, piiControllerURL, address);
    }

    private String getConfiguration(String configElement) {
        Map<String, Object> configuration = ConsentConfigParser.getInstance().getConfiguration();
        if (configuration.get(configElement) != null) {
            return configuration.get(configElement).toString();
        }
        return null;
    }
}
