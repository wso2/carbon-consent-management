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

import org.testng.Assert;
import org.testng.annotations.Test;

public class AddressTest {

    @Test
    public void testAddress() throws Exception {

        String addressCountry = "UK";
        String addressLocality = "EN";
        String addressRegion = "London";
        String postOfficeBoxNumber = "221B";
        String postalCode = "NW1 6XE";
        String streetAddress = "Baker St, Marylebone";

        Address address = new Address(addressCountry, addressLocality, addressRegion, postOfficeBoxNumber,
                postalCode, streetAddress);

        Assert.assertEquals(address.getAddressCountry(), addressCountry);
        Assert.assertEquals(address.getAddressLocality(), addressLocality);
        Assert.assertEquals(address.getAddressRegion(), addressRegion);
        Assert.assertEquals(address.getPostOfficeBoxNumber(), postOfficeBoxNumber);
        Assert.assertEquals(address.getPostalCode(), postalCode);
        Assert.assertEquals(address.getStreetAddress(), streetAddress);

        address = new Address();
        address.setAddressCountry(addressCountry);
        address.setAddressLocality(addressLocality);
        address.setAddressRegion(addressRegion);
        address.setPostOfficeBoxNumber(postOfficeBoxNumber);
        address.setPostalCode(postalCode);
        address.setStreetAddress(streetAddress);

        Assert.assertEquals(address.getAddressCountry(), addressCountry);
        Assert.assertEquals(address.getAddressLocality(), addressLocality);
        Assert.assertEquals(address.getAddressRegion(), addressRegion);
        Assert.assertEquals(address.getPostOfficeBoxNumber(), postOfficeBoxNumber);
        Assert.assertEquals(address.getPostalCode(), postalCode);
        Assert.assertEquals(address.getStreetAddress(), streetAddress);
    }

}