/* Copyright (c) 2006 Google Inc.
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

package com.google.api.gbase.client;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Query;
import com.google.gdata.client.batch.BatchInterruptedException;
import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.BaseFeed;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.ExtensionProfile;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Creates a Google data service with extensions
 * specific to Google Base.
 *
 * The methods returning Atom feeds and entries in this class
 * default to returning instances of {@link GoogleBaseFeed} and
 * {@link GoogleBaseEntry}.
 *
 * <h4>Usage example<h4>
 * <pre>
 * GoogleBaseService service = new GoogleBaseService("mycompany-myapp-1.0");
 * service.setUserCredentials(username, password);
 * GoogleBaseQuery query = new GoogleBaseQuery(...);
 * query.setGoogleBaseQuery(...);
 * GoogleBaseFeed feed = service.query(query);
 * </pre>
 */
public class GoogleBaseService extends GoogleService {

  /**
   * The official name of the service.
   */
  public static final String GOOGLE_BASE_SERVICE = "gbase";

  /**
   * Version of this service.
   * @see #getServiceVersion()
   */
  public static final String GOOGLE_BASE_SERVICE_VERSION = "GBase-Java/" +
          GoogleBaseService.class.getPackage().getImplementationVersion();

  /** Name of the application passed to the constructor. */
  protected String application;

  /**
   * Creates a GoogleBaseService connecting to the default
   * authentication domain (www.google.com) using the HTTPS.
   *
   * @param applicationName the name of the client application accessing the
   *                        service.  Application names should preferably have
   *                        the format [company-id]-[app-name]-[app-version].
   *                        The name will be used by the Google servers to
   *                        monitor the source of authentication.
   */
  public GoogleBaseService(String applicationName) {
    super(GOOGLE_BASE_SERVICE, applicationName);
    this.application = applicationName;
    addExtensions();
  }

  /**
   * Creates a GoogleBaseService connecting to the default
   * authentication domain (www.google.com) using the HTTPS.
   *
   * @param applicationName the name of the client application accessing the
   *                        service.  Application names should preferably have
   *                        the format [company-id]-[app-name]-[app-version].
   *                        The name will be used by the Google servers to
   *                        monitor the source of authentication.
   * @param developerKey    developer key (ignored)
   */
  public GoogleBaseService(String applicationName, String developerKey) {
    this(applicationName);
  }

  /**
   * Creates a GoogleBaseService connecting to a specific
   * authentication domain using a specific protocol.
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
  public GoogleBaseService(String applicationName,
      String protocol,
      String domainName) {
    super(GOOGLE_BASE_SERVICE, applicationName, protocol, domainName);
    this.application = applicationName;
    addExtensions();
  }

  /**
   * Creates a GoogleBaseService connecting to a specific
   * authentication domain using a specific protocol.
   *
   * @param applicationName the name of the client application accessing the
   *                        service. Application names should preferably have
   *                        the format [company-id]-[app-name]-[app-version].
   *                        The name will be used by the Google servers to
   *                        monitor the source of authentication.
   * @param developerKey    developer key (ignored)
   * @param protocol        name of protocol to use for authentication
   *                        ("http"/"https")
   * @param domainName      the name of the domain hosting the login handler
   */
  public GoogleBaseService(String applicationName,
      String developerKey,
      String protocol,
      String domainName) {
    this(applicationName, protocol, domainName);
  }

  /**
   * Returns the Google Base feed associated with a particular feed URL, if
   * it's been modified since th especified date.
   *
   * @param feedUrl         the URL associated with a feed. This URL can include
   *                        GData query parameters.
   * @param ifModifiedSince used to set a precondition date that indicates the
   *                        feed should be returned only if it has been modified
   *                        after the specified date. A value of {@code null}
   *                        indicates no precondition.
   * @return Feed resource referenced by the input URL.
   * @throws IOException error sending request or reading the feed.
   * @throws com.google.gdata.util.NotModifiedException if the feed resource
   *          has not been modified since the specified precondition date.
   * @throws com.google.gdata.util.ParseException error parsing the returned
   *          feed data.
   * @throws com.google.gdata.util.ResourceNotFoundException invalid feed URL.
   * @throws ServiceException system error retrieving feed.
   * @see #getFeed(java.net.URL, Class, com.google.gdata.data.DateTime)
   */
  public GoogleBaseFeed getFeed(URL feedUrl, DateTime ifModifiedSince)
      throws IOException, ServiceException {
    return getFeed(feedUrl, GoogleBaseFeed.class, ifModifiedSince);
  }

  /**
   * Returns the Google base feed associated with a particular feed URL.
   *
   * @param feedUrl the URL associated with a feed. This URL can include
   *                GData query parameters.
   * @return Feed resource referenced by the input URL.
   * @throws IOException error sending request or reading the feed.
   * @throws com.google.gdata.util.ParseException error parsing the returned
   *   feed data.
   * @throws com.google.gdata.util.ResourceNotFoundException invalid feed URL.
   * @throws ServiceException system error retrieving feed.
   * @see #getFeed(java.net.URL, Class)
   */
  public GoogleBaseFeed getFeed(URL feedUrl)
      throws IOException, ServiceException {
    return getFeed(feedUrl, GoogleBaseFeed.class);
  }

  /**
   * Returns an Google Base entry instance, given the URL of the entry and an
   * if-modified-since date.
   *
   * @param entryUrl resource URL for the entry.
   * @param ifModifiedSince   used to set a precondition date that indicates the
   *                          entry should be returned only if it has been
   *                          modified after the specified date. A value of
   *                          {@code null} indicates no precondition.
   * @return the entry referenced by the URL parameter.
   * @throws IOException error communicating with the GData service.
   * @throws com.google.gdata.util.NotModifiedException if the entry resource
   *          has not been modified after the specified precondition date.
   * @throws com.google.gdata.util.ParseException error parsing the returned
   *          entry.
   * @throws com.google.gdata.util.ResourceNotFoundException if the entry URL
   *          is not valid.
   * @throws com.google.gdata.util.ServiceForbiddenException if the GData
   *          service cannot get the entry resource due to access constraints.
   * @throws ServiceException if a system error occurred when retrieving
   *          the entry.
   * @see #getEntry(java.net.URL, Class, DateTime)
   */
  public GoogleBaseEntry getEntry(URL entryUrl, DateTime ifModifiedSince)
      throws IOException, ServiceException {
    return getEntry(entryUrl, GoogleBaseEntry.class, ifModifiedSince);
  }

  /**
   * Returns an Google Base entry instance, given the URL of the entry.
   *
   * @param entryUrl resource URL for the entry.
   * @return the entry referenced by the URL parameter.
   * @throws IOException error communicating with the GData service.
   * @throws com.google.gdata.util.ParseException error parsing the returned
   *          entry.
   * @throws com.google.gdata.util.ResourceNotFoundException if the entry URL
   *          is not valid.
   * @throws com.google.gdata.util.ServiceForbiddenException if the GData
   *          service cannot get the entry resource due to access constraints.
   * @throws ServiceException if a system error occurred when retrieving
   *          the entry.
   * @see #getEntry(java.net.URL, Class)
   */
  public GoogleBaseEntry getEntry(URL entryUrl)
      throws IOException, ServiceException {
    return getEntry(entryUrl, GoogleBaseEntry.class);
  }

  /**
   * Executes a GData query against the target service and returns the
   * {@link GoogleBaseFeed} containing entries that match the query result, if
   * it's been modified since the specified date.
   *
   * @param query Query instance defining target feed and query parameters,
   *              usually a {@link GoogleBaseQuery}
   * @param ifModifiedSince used to set a precondition date that indicates the
   *          query result feed should be returned only if contains entries
   *          that have been modified after the specified date.  A value of
   *          {@code null} indicates no precondition.
   * @throws IOException error communicating with the GData service.
   * @throws com.google.gdata.util.NotModifiedException if the query resource
   *          does not contain entries modified since the specified precondition
   *          date.
   * @throws com.google.gdata.util.ServiceForbiddenException feed does not
   *          support the query.
   * @throws com.google.gdata.util.ParseException error parsing the returned
   *          feed data.
   * @throws ServiceException query request failed.
   * @see #query(com.google.gdata.client.Query, Class, DateTime)
   */
  public GoogleBaseFeed query(Query query, DateTime ifModifiedSince)
      throws IOException, ServiceException {
    return query(query, GoogleBaseFeed.class, ifModifiedSince);
  }

  /**
   * Executes a GData query against the target service and returns the
   * {@link com.google.gdata.data.Feed} containing entries that match the
   * query result.
   *
   * @param query Query instance defining target feed and query parameters,
   *              usually a {@link GoogleBaseQuery}
   * @throws IOException error communicating with the GData service.
   * @throws com.google.gdata.util.NotModifiedException if the query resource
   *          does not contain entries modified since the specified precondition
   *          date.
   * @throws com.google.gdata.util.ServiceForbiddenException feed does not
   *          support the query.
   * @throws com.google.gdata.util.ParseException error parsing the returned
   *          feed data.
   * @throws ServiceException query request failed.
   */
  public GoogleBaseFeed query(Query query)
      throws IOException, ServiceException {
    return query(query, GoogleBaseFeed.class);
  }

  public <E extends BaseEntry> E update(URL url, E e)
      throws IOException, ServiceException {
    addApplicationAttribute(e);
    return super.update(url, e);
  }

  public <E extends BaseEntry> E insert(URL url, E e)
      throws IOException, ServiceException {
    addApplicationAttribute(e);
    return super.insert(url, e);
  }

  public <F extends BaseFeed> F batch(URL url, F f)
      throws IOException, ServiceException, BatchInterruptedException {
    addApplicationAttribute(f);
    return super.batch(url, f);
  }


  /**
   * Sets the application attribute using the name passed to the
   * constructor.
   *
   * @param e
   */
  private void addApplicationAttribute(BaseEntry e) {
    GoogleBaseAttributesExtension attrs = e.getExtension(GoogleBaseAttributesExtension.class);
    if (attrs == null) {
      return;
    }
    attrs.setApplication(application);
  }

  /**
   * Sets the application attribute using the name passed to the constructor
   * on a batch feed.
   *
   * @param batchFeed
   */
  private void addApplicationAttribute(BaseFeed batchFeed) {
    BatchOperationType defaultType = BatchUtils.getBatchOperationType(batchFeed);
    if (defaultType == null) {
        defaultType = BatchOperationType.INSERT;
    }
    List<BaseEntry> entries = batchFeed.getEntries();
    for (BaseEntry entry: entries) {
      BatchOperationType type = BatchUtils.getBatchOperationType(entry);
      if (type == null) {
        type = defaultType;
      }
      if (type == BatchOperationType.INSERT ||
          type == BatchOperationType.UPDATE) {
        addApplicationAttribute(entry);
      }
    }
  }

  /**
   * Adds Google Base extensions (g:namespaces) to a
   * Google data service.
   */
  private void addExtensions() {
    ExtensionProfile extensionProfile = getExtensionProfile();
    GoogleBaseNamespaces.declareAllExtensions(extensionProfile);
    BatchUtils.declareExtensions(extensionProfile);
  }

  /**
   * Returns the google base service version followed by the gdata
   * service version.
   *
   * @return both versions, separated by a space
   * @see #GOOGLE_BASE_SERVICE_VERSION
   */
  @Override
  public String getServiceVersion() {
    return GOOGLE_BASE_SERVICE_VERSION + " " + super.getServiceVersion();
  }
}
