/* Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.google.gdata.client.contacts;

import com.google.gdata.client.GoogleService;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.ContactGroupFeed;

/**
 * The ContactsService class extends the basic {@link GoogleService} abstraction
 * to define a service that is preconfigured for access to the Google Contacts
 * data API.
 *
 * 
 */
public class ContactsService extends GoogleService {

  /**
   * The abbreviated name of Google Contacts recognized by Google.  The service
   * name is used when requesting an authentication token.
   */
  public static final String CONTACTS_SERVICE = "cp";

  /**
   * The version ID of the service.
   */
  public static final String CONTACTS_SERVICE_VERSION = "GContacts-Java/" +
      ContactsService.class.getPackage().getImplementationVersion();

  /**
   * Constructs a ContactsService instance connecting to the Google Contacts
   * service for an application with the name {@code applicationName}.
   *
   * @param applicationName the name of the client application accessing the
   *                        service. Application names should preferably have
   *                        the format [company-id]-[app-name]-[app-version].
   *                        The name will be used by the Google servers to
   *                        monitor the source of authentication.
   */
  public ContactsService(String applicationName) {
    this(applicationName, "https", "www.google.com");
  }

  /**
   * Constructs a GoogleService instance connecting to the Google Contacts
   * service with name {@code serviceName} for an application with the name
   * {@code applicationName}.  The service will authenticate at the provided
   * {@code domainName}.
   *
   * @param applicationName the name of the client application accessing the
   *                        service. Application names should preferably have
   *                        the format [company-id]-[app-name]-[app-version].
   *                        The name will be used by the Google servers to
   *                        monitor the source of authentication.
   * @param protocol        name of protocol to use for authentication
   *                        ("http"/"https")
   * @param domainName      the name of the domain hosting the login handler
   */
  public ContactsService(String applicationName, String protocol,
      String domainName) {
    super(CONTACTS_SERVICE, applicationName, protocol, domainName);

    // Declare the extensions of the feeds
    new ContactFeed().declareExtensions(getExtensionProfile());
    new ContactGroupFeed().declareExtensions(getExtensionProfile());
    BatchUtils.declareExtensions(getExtensionProfile());
  }

  public String getServiceVersion() {
    return CONTACTS_SERVICE_VERSION + " " + super.getServiceVersion();
  }

}
