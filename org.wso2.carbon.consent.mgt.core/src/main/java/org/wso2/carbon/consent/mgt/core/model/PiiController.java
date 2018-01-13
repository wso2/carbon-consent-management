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
 * This model represents the PII controller model
 */
public class PiiController {

  private String piiController;
  private String contact = null;
  private Address address;
  private String email;
  private String phone;
  private String onBehalf = null;
  private String piiControllerUrl = null;

  public PiiController(String piiController, String contact, Address address, String email, String phone,
                       String onBehalf, String piiControllerUrl) {
    this.piiController = piiController;
    this.contact = contact;
    this.address = address;
    this.email = email;
    this.phone = phone;
    this.onBehalf = onBehalf;
    this.piiControllerUrl = piiControllerUrl;
  }

  public PiiController(String piiController, Address address, String email, String phone) {
    this.piiController = piiController;
    this.address = address;
    this.email = email;
    this.phone = phone;
  }

  public PiiController() {
  }

  public String getPiiController() {
    return piiController;
  }

  public void setPiiController(String piiController) {
    this.piiController = piiController;
  }

  public String getContact() {
    return contact;
  }

  public void setContact(String contact) {
    this.contact = contact;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getOnBehalf() {
    return onBehalf;
  }

  public void setOnBehalf(String onBehalf) {
    this.onBehalf = onBehalf;
  }

  public String getPiiControllerUrl() {
    return piiControllerUrl;
  }

  public void setPiiControllerUrl(String piiControllerUrl) {
    this.piiControllerUrl = piiControllerUrl;
  }
}
