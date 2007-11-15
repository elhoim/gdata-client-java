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


package com.google.gdata.client;

import com.google.gdata.util.common.xml.XmlWriter;
import com.google.gdata.client.AuthTokenFactory.AuthToken;
import com.google.gdata.client.batch.BatchInterruptedException;
import com.google.gdata.client.http.HttpGDataRequest;
import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.BaseFeed;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.ExtensionProfile;
import com.google.gdata.data.Feed;
import com.google.gdata.data.ParseSource;
import com.google.gdata.data.batch.BatchInterrupted;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.introspection.ServiceDocument;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;
import com.google.gdata.util.VersionRegistry;
import com.google.gdata.util.VersionRegistry.Version;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * The Service class represents a client connection to a GData service.
 * It encapsulates all protocol-level interactions with the GData server
 * and acts as a helper class for higher level entities (feeds, entries,
 * etc) that invoke operations on the server and process their results.
 * <p>
 * This class provides the base level common functionality required to
 * access any GData service.  It is also designed to act as a base class
 * that can be customized for specific types of GData services.  Examples
 * of supported customizations include:
 * <ul>
 * <li><b>Authentication</b> - implementating a custom authentication
 * mechanism for services that require authentication and use something
 * other than HTTP basic or digest authentication.
 * <li><b>Extensions</b> - define expected ExtensionPoints and Extensions
 * with the {@link ExtensionProfile} associated with the service to allow
 * Atom/RSS extension elements to be automatically converted to/from the
 * {@link Feed}/{@link com.google.gdata.data.Entry} object model.
 * </ul>
 *
 * 
 */
public class Service {


  private static final String SERVICE_VERSION =
      "GData-Java/" + Service.class.getPackage().getImplementationVersion() +
      "(gzip)";         // Necessary to get GZIP encoded responses
  
  /**
   * The major version number of the Alpha version of the GData core protocol.
   */
  public static final int ALPHA_MAJOR = 1;
  
  /**
   * The Alpha version of the GData core protocol that was released in
   * May 2006 and is in use for all current GData services.
   */
  public static final Version ALPHA =
      new Version(Service.class, ALPHA_MAJOR, 0);
  
  /**
   * The major version number of the Beta version of the GData core protocol.
   */
  public static final int BETA_MAJOR = 2;
    
  /**
   * The upcoming Beta release of the GData core protocol that will bring
   * full alignment with the now standard Atom Publishing Protocol
   * specification, migration to OpenSearch 1.1, and other (TBD) features.
   */
  public static final Version BETA =
      new Version(Service.class, BETA_MAJOR, 0);

  static {
    // Initialize default version information for the GData client core.
    Version coreVersion = VersionRegistry.getVersionFromProperty(Service.class);
    if (coreVersion == null) {
      coreVersion = ALPHA;
    }
    initServiceVersion(coreVersion);
  }
  

  
  /**
   * The GDataRequest interface represents a streaming connection to a
   * GData service that can be used either to send request data to the
   * service using an OutputStream (or XmlWriter for XML content) or to
   * receive response data from the service as an InputStream (or
   * ParseSource for XML data).   The calling client then has full control
   * of the request data generation and response data parsing.  This can
   * be used to integrate GData services with an external Atom or RSS
   * parsing library, such as Rome.
   * <p>
   * A GDataRequest instance will be returned by the streaming client
   * APIs of the Service class.  The basic usage pattern is:
   * <p>
   * <pre>
   * GDataRequest request = ...     // createXXXRequest API call
   * try {
   *    OutputStream requestStream = request.getRequestStream();
   *    // stream request data, if any
   *
   *    request.execute()                // execute the request
   *
   *    InputStream responseStream = request.getResponseStream();
   *    // process the response data, if any
   * }
   * catch (IOException ioe) {
   *    // handle errors writing to / reading from server
   * } catch (ServiceException se) {
   *    // handle service invocation errors
   * }
   * </pre>
   *
   * @see Service#createEntryRequest(URL)
   * @see Service#createFeedRequest(URL)
   * @see Service#createInsertRequest(URL)
   * @see Service#createUpdateRequest(URL)
   * @see Service#createDeleteRequest(URL)
   */
  public interface GDataRequest {


    /**
     * The RequestType enumeration defines the set of expected GData
     * request types.  These correspond to the four operations of the
     * GData protocol:
     * <ul>
     * <li><b>QUERY</b> - query a feed, entry, or description document.</li>
     * <li><b>INSERT</b> - insert a new entry into a feed.</li>
     * <li><b>UPDATE</b> - update an existing entry within a feed.</li>
     * <li><b>DELETE</b> - delete an existing entry within a feed.</li>
     * <li><b>BATCH</b> - execute several insert/update/delete operations</li>
     * </ul>
     */
    public enum RequestType {
      QUERY, INSERT, UPDATE, DELETE, BATCH
    }


    /**
     * Sets the number of milliseconds to wait for a connection to the
     * remote GData service before timing out.
     *
     * @param timeout the read timeout.  A value of zero indicates an
     *        infinite timeout.
     * @throws IllegalArgumentException if the timeout value is negative.
     *
     * @see java.net.URLConnection#setConnectTimeout(int)
     */
    public void setConnectTimeout(int timeout);


    /**
     * Sets the number of milliseconds to wait for a response from the
     * remote GData service before timing out.
     *
     * @param timeout the read timeout.  A value of zero indicates an
     *        infinite timeout.
      @throws IllegalArgumentException if the timeout value is negative.
     *
     * @see java.net.URLConnection#setReadTimeout(int)
     */
    public void setReadTimeout(int timeout);

    /**
     * Sets the If-Modified-Since date precondition to be applied to the
     * request.  If this precondition is set, then the request will be
     * performed only if the target resource has been modified since the
     * specified date; otherwise, a {@code NotModifiedException} will be
     * thrown.   The default value is {@code null}, indicating no
     * precondition.
     *
     * @param conditionDate the date that should be used to limit the
     *          operation on the target resource.  The operation will only
     *          be performed if the resource has been modified later than
     *          the specified date.
     */
    public void setIfModifiedSince(DateTime conditionDate);

    /**
     * Sets a request header (and logs it, if logging is enabled)
     *
     * @param name the header name
     * @param value the header value
     */
    public void setHeader(String name, String value);

    /**
     * Sets request header (and log just the name but not the value, if
     * logging is enabled)
     *
     * @param name the header name
     * @param value the header value
     */
    public void setPrivateHeader(String name, String value);

    /**
     * Returns a stream that can be used to write request data to the
     * GData service.
     *
     * @return OutputStream that can be used to write GData request data.
     * @throws IOException error obtaining the request output stream.
     */
    public OutputStream getRequestStream() throws IOException;

    /**
     * Returns an XML writer that can be used to write XML request data
     * to the GData service.
     *
     * @return XmlWriter that can be used to write GData XML request data.
     * @throws IOException error obtaining the request writer.
     * @throws ServiceException error obtaining the request writer.
     */
    public XmlWriter getRequestWriter() throws IOException, ServiceException;

    /**
     * Executes the GData service request.
     *
     * @throws IOException error writing to or reading from GData service.
     * @throws com.google.gdata.util.ResourceNotFoundException invalid request 
     *         target resource.
     * @throws ServiceException system error executing request.
     */
    public void execute() throws IOException, ServiceException;

    /**
     * Returns the content type of the GData response.
     * <p>
     *
     * @return ContentType the GData response content type or {@code null}
     *                               if no response content.
     * @throws IllegalStateException attempt to read content type without
     *                               first calling {@link #execute()}.
     * @throws IOException error obtaining the response content type.
     * @throws ServiceException error obtaining the response content type.
     */
    public ContentType getResponseContentType()
        throws IOException, ServiceException;

    /**
     * Returns an input stream that can be used to read response data from the
     * GData service. Returns null if response data cannot be read as
     * an input stream. Use {@link getParseSource()} instead.
     * <p>
     * <b>The caller is responsible for ensuring that the input stream is
     * properly closed after the response has been read.</b>
     *
     * @return InputStream providing access to GData response input stream.
     * @throws IllegalStateException attempt to read response without
     *                               first calling {@link #execute()}.
     * @throws IOException error obtaining the response input stream.
     */
    public InputStream getResponseStream() throws IOException;

    /**
     * Returns a parse source that can be used to read response data from the
     * GData service. Parse source is an abstraction over input streams,
     * readers, and other forms of input.
     * <p>
     * <b>The caller is responsible for ensuring that input streams and
     * readers contained in the parse source are properly closed after
     * the response has been read.</b>
     *
     * @return ParseSource providing access to GData response data.
     * @throws IllegalStateException attempt to read response without
     *                               first calling {@link #execute()}.
     * @throws IOException error obtaining the response data.
     * @throws ServiceException error obtaining the response data.
     */
    public ParseSource getParseSource() throws IOException, ServiceException;
  }


  /**
   * The GDataRequestFactory interface defines a basic factory interface
   * for constructing a new GDataRequest interface.
   */
  public interface GDataRequestFactory {

    /**
     * Set a header that will be included in all requests. If header of
     * the same name was previously set, then replace the previous header
     * value.
     *
     * @param header the name of the header
     * @param value the value of the header, if null, then unset that header.
     */
    public void setHeader(String header, String value);

    /**
     * Set a header that will be included in all requests and do
     * not log the value.  Useful for values that are sensitive or
     * related to security. If header of the same name was previously set,
     * then replace the previous header value.
     *
     * @param header the name of the header
     * @param value the value of the header.  If null, then unset that header.
     */
    public void setPrivateHeader(String header, String value);

    /**
     * Set authentication token to be used on subsequent requests created via
     * {@link #getRequest(
     * com.google.gdata.client.Service.GDataRequest.RequestType, URL,
     * ContentType)}.
     * 
     * An {@link IllegalArgumentException} is thrown if an auth token
     * of the wrong type is passed, or if authentication is not supported.
     * 
     * @param authToken Authentication token.
     */
    public void setAuthToken(AuthToken authToken);

    /**
     * Creates a new GDataRequest instance of the specified RequestType.
     */
    public GDataRequest getRequest(GDataRequest.RequestType type,
                                   URL requestUrl,
                                   ContentType contentType)
      throws IOException, ServiceException;

    /**
     * Creates a new GDataRequest instance for querying a service.
     * This method pushes the query parameters down to the factory
     * method instead of serializing them as a URL. Some factory
     * implementations prefer to get access to query parameters
     * in their original form, not as a URL.
     */
    public GDataRequest getRequest(Query query, ContentType contentType)
      throws IOException, ServiceException;
  }


  /**
   * Initializes the version information for a specific service type.
   * Subclasses of {@link Service} will generally call this method from within
   * their static initializers to bind
   * version information for the associated service.
   * @param version the service version expected by this client library.
   */
  protected static void initServiceVersion(Version version) {
    ClientVersion.init(version);
  }

  /**
   * Returns the current {@link Version} of the GData core protocol.
   * @return protocol version.
   */
  public static Version getVersion() {
    return VersionRegistry.get().getVersion(Service.class);
  }

  /**
   * Constructs a new Service instance that is configured to accept arbitrary
   * extension data within feed or entry elements.
   */
  public Service() {

    // Set the default User-Agent value for requests
    requestFactory.setHeader("User-Agent", getServiceVersion());

    // The default extension profile is configured to accept arbitrary XML
    // at the feed or entry level.   A client never wants to lose any
    // foreign markup, so capture everything even if not explicitly
    // understood.
    new Feed().declareExtensions(extProfile);
  }

  /**
   * Returns information about the service version.
   */
  public String getServiceVersion() {  return SERVICE_VERSION; }



  protected ExtensionProfile extProfile = new ExtensionProfile();

  /**
   * Returns the {@link ExtensionProfile} that defines any expected extensions
   * to the base RSS/Atom content model.
   */
  public ExtensionProfile getExtensionProfile() {
    return extProfile;
  }

  /**
   * Sets the {@link ExtensionProfile} that defines any expected extensions
   * to the base RSS/Atom content model.
   */
  public void setExtensionProfile(ExtensionProfile v) {
    this.extProfile = v;
  }

  /**
   * The GDataRequestFactory associated with this service.  The default is
   * the base HttpGDataRequest Factory class.
   */
  protected GDataRequestFactory requestFactory = new HttpGDataRequest.Factory();


  /**
   * Returns the GDataRequestFactory currently associated with the service.
   */
  public GDataRequestFactory getRequestFactory() {
    return requestFactory;
  }


  /**
   * Sets the GDataRequestFactory currently associated with the service.
   */
  public void setRequestFactory(GDataRequestFactory requestFactory) {
    this.requestFactory = requestFactory;
  }


  /**
   * Creates a new GDataRequest for use by the service.
   * 
   * For query requests, use
   * {@link #createRequest(
   * com.google.gdata.client.Service.GDataRequest.RequestType,
   * URL, Query, ContentType)}
   * instead.
   */
  public GDataRequest createRequest(GDataRequest.RequestType type,
                                    URL requestUrl,
                                    ContentType inputType)
      throws IOException, ServiceException {

    GDataRequest request = 
        requestFactory.getRequest(type, requestUrl, inputType);
    setTimeouts(request);
    return request;
  }


  /**
   * Creates a new GDataRequest for querying the service.
   */
  private GDataRequest createRequest(Query query, ContentType inputType)
      throws IOException, ServiceException {

    GDataRequest request = requestFactory.getRequest(query, inputType);
    setTimeouts(request);
    return request;
  }


  /**
   * Sets timeout value for GDataRequest.
   */
  public void setTimeouts(GDataRequest request) {
    if (connectTimeout >= 0) {
      request.setConnectTimeout(connectTimeout);
    }
    if (readTimeout >= 0) {
      request.setReadTimeout(readTimeout);
    }
  }

  
  /**
   * Content type of data posted to the GData service.
   * Defaults to Atom using UTF-8 character set.
   */
  private ContentType contentType = ContentType.ATOM;

  /**
   * Returns the default ContentType for data associated with this GData
   * service.
   */
  public ContentType getContentType() { return contentType; }


  /**
   * Sets the default ContentType for writing data to the GData service.
   */
  public void setContentType(ContentType contentType) {
    this.contentType = contentType;
  }


  /**
   * Client-configured connection timeout value.  A value of -1 indicates
   * the client has not set any timeout.
   */
  protected int connectTimeout = -1;


  /**
   * Sets the default wait timeout (in milliseconds) for a connection to the
   * remote GData service.
   *
   * @param timeout the read timeout.  A value of zero indicates an
   *        infinite timeout.
   * @throws IllegalArgumentException if the timeout value is negative.
   *
   * @see java.net.URLConnection#setConnectTimeout(int)
   */
  public void setConnectTimeout(int timeout) {
    if (timeout < 0) {
      throw new IllegalArgumentException("Timeout value cannot be negative");
    }
    connectTimeout = timeout;
  }


  /**
   * Client configured read timeout value.  A value of -1 indicates
   * the client has not set any timeout.
   */
  int readTimeout = -1;


  /**
   * Sets the default wait timeout (in milliseconds) for a response from the
   * remote GData service.
   *
   * @param timeout the read timeout.  A value of zero indicates an
   *        infinite timeout.
    @throws IllegalArgumentException if the timeout value is negative.
   *
   * @see java.net.URLConnection#setReadTimeout(int)
   */
  public void setReadTimeout(int timeout) {
    if (timeout < 0) {
      throw new IllegalArgumentException("Timeout value cannot be negative");
    }
    readTimeout = timeout;
  }

  /**
   * Parse an entry of the specified class from a parse source.
   */
  protected <E extends BaseEntry<?>> E parseEntry(Class<E> entryClass,
                                                  ParseSource entrySource)
      throws IOException, ServiceException {

    E entry = BaseEntry.readEntry(entrySource, entryClass, extProfile);
    entry.setService(this);
    return entry;
  }

  /**
   * Returns the Atom introspection Service Document associated with a
   * particular feed URL.  This document provides service metadata about
   * the set of Atom services associated with the target feed URL.
   *
   * @param feedUrl the URL associated with a feed.   This URL can not include
   *        any query parameters.
   * @param serviceClass the class used to represent a service document.
   *
   * @return ServiceDocument resource referenced by the input URL.
   * @throws IOException error sending request or reading the feed.
   * @throws com.google.gdata.util.ParseException error parsing the returned 
   *         service data.
   * @throws com.google.gdata.util.ResourceNotFoundException invalid feed URL.
   * @throws ServiceException system error retrieving service document.
   */
  public <S extends ServiceDocument> S introspect(URL feedUrl,
                                                  Class<S> serviceClass)
      throws IOException, ServiceException {

    String feedQuery = feedUrl.getQuery();
    if (feedQuery == null || feedQuery.indexOf("alt=atom-service") == -1) {
      char appendChar = (feedQuery == null) ? '?' : '&';
      feedUrl = new URL(feedUrl.toString() + appendChar + "alt=atom-service");
    }

    InputStream responseStream = null;
    GDataRequest request = createFeedRequest(feedUrl);
    try {
      request.execute();
      responseStream = request.getResponseStream();
      if (responseStream == null) {
        throw new ServiceException("Unable to obtain service document");
      }

      S serviceDoc = serviceClass.newInstance();
      serviceDoc.parse(extProfile, responseStream);

      return serviceDoc;

    } catch (InstantiationException e) {
      throw new ServiceException("Unable to create service document instance",
                                 e);
    } catch (IllegalAccessException e) {
      throw new ServiceException("Unable to create service document instance",
                                 e);
    } finally {
      if (responseStream != null) {
        responseStream.close();
      }
    }
  }


  /**
   * Returns the Feed associated with a particular feed URL, if it's
   * been modified since the specified date.
   *
   * @param feedUrl the URL associated with a feed.   This URL can include
   *                GData query parameters.
   * @param feedClass the class used to represent a service Feed.
   * @param ifModifiedSince used to set a precondition date that indicates the
   *          feed should be returned only if it has been modified after the
   *          specified date. A value of {@code null} indicates no precondition.
   * @return Feed resource referenced by the input URL.
   * @throws IOException error sending request or reading the feed.
   * @throws com.google.gdata.util.NotModifiedException if the feed resource has
   *         not been modified since the specified precondition date.
   * @throws com.google.gdata.util.ParseException error parsing the returned
   *         feed data.
   * @throws com.google.gdata.util.ResourceNotFoundException invalid feed URL.
   * @throws ServiceException system error retrieving feed.
   */
  @SuppressWarnings("unchecked")
  public <F extends BaseFeed<?, ?>> F getFeed(URL feedUrl,
                                        Class<F> feedClass,
                                        DateTime ifModifiedSince)
      throws IOException, ServiceException {
    GDataRequest request = createFeedRequest(feedUrl);
    return getFeed(request, feedClass, ifModifiedSince);
  }


  /**
   * Returns the Feed associated with a particular query, if it's
   * been modified since the specified date.
   *
   * @param query feed query.
   * @param feedClass the class used to represent a service Feed.
   * @param ifModifiedSince used to set a precondition date that indicates the
   *          feed should be returned only if it has been modified after the
   *          specified date. A value of {@code null} indicates no precondition.
   * @return Feed resource referenced by the input URL.
   * @throws IOException error sending request or reading the feed.
   * @throws NotModifiedException if the feed resource has not been modified
   *          since the specified precondition date.
   * @throws ParseException error parsing the returned feed data.
   * @throws ResourceNotFoundException invalid feed URL.
   * @throws ServiceException system error retrieving feed.
   */
  public <F extends BaseFeed<?, ?>> F getFeed(Query query,
                                              Class<F> feedClass,
                                              DateTime ifModifiedSince)
      throws IOException, ServiceException {
    GDataRequest request = createFeedRequest(query);
    return getFeed(request, feedClass, ifModifiedSince);
  }


  /**
   * Returns the Feed associated with a particular feed URL, if it's
   * been modified since the specified date.
   *
   * @param request the GData request.
   * @param feedClass the class used to represent a service Feed.
   * @param ifModifiedSince used to set a precondition date that indicates the
   *          feed should be returned only if it has been modified after the
   *          specified date. A value of {@code null} indicates no precondition.
   * @return Feed resource referenced by the input URL.
   * @throws IOException error sending request or reading the feed.
   * @throws NotModifiedException if the feed resource has not been modified
   *          since the specified precondition date.
   * @throws ParseException error parsing the returned feed data.
   * @throws ResourceNotFoundException invalid feed URL.
   * @throws ServiceException system error retrieving feed.
   */
  private <F extends BaseFeed<?, ?>> F getFeed(GDataRequest request,
                                               Class<F> feedClass,
                                               DateTime ifModifiedSince)
      throws IOException, ServiceException {

    ParseSource feedSource = null;
    try {
      request.setIfModifiedSince(ifModifiedSince);
      request.execute();
      feedSource = request.getParseSource();

      BaseFeed<?, ?> feed =
          BaseFeed.readFeed(feedSource, feedClass, extProfile);
      feed.setService(this);
      return (F) feed;
    } finally {
      closeSource(feedSource);
    }
  }


  /**
   * Returns the Feed associated with a particular feed URL.
   *
   * @param feedUrl the URL associated with a feed.   This URL can include
   *                GData query parameters.
   * @param feedClass the class used to represent a service Feed.
   * @return Feed resource referenced by the input URL.
   * @throws IOException error sending request or reading the feed.
   * @throws com.google.gdata.util.ParseException error parsing the returned 
   *         feed data.
   * @throws com.google.gdata.util.ResourceNotFoundException invalid feed URL.
   * @throws ServiceException system error retrieving feed.
   */
  public <F extends BaseFeed<?, ?>> F getFeed(URL feedUrl, Class<F> feedClass)
      throws IOException, ServiceException {
    return getFeed(feedUrl, feedClass, null);
  }


  /**
   * Returns the Feed associated with a particular query.
   *
   * @param query feed query.
   * @param feedClass the class used to represent a service Feed.
   * @return Feed resource referenced by the input URL.
   * @throws IOException error sending request or reading the feed.
   * @throws ParseException error parsing the returned feed data.
   * @throws ResourceNotFoundException invalid feed URL.
   * @throws ServiceException system error retrieving feed.
   */
  public <F extends BaseFeed<?, ?>> F getFeed(Query query, Class<F> feedClass)
      throws IOException, ServiceException {
    return getFeed(query, feedClass, null);
  }


  /**
   * Executes a GData feed request against the target service and returns the
   * resulting feed results via an input stream.
   *
   * @param feedUrl URL that defines target feed.
   * @return GData request instance that can be used to read the feed data.
   * @throws IOException error communicating with the GData service.
   * @throws ServiceException creation of query feed request failed.
   *
   * @see Query#getUrl()
   */
  public GDataRequest createFeedRequest(URL feedUrl)
      throws IOException, ServiceException {
    return createRequest(GDataRequest.RequestType.QUERY, feedUrl,
                         contentType);
  }


  /**
   * Executes a GData query request against the target service and returns the
   * resulting feed results via an input stream.
   *
   * @param query feed query.
   * @return GData request instance that can be used to read the feed data.
   * @throws IOException error communicating with the GData service.
   * @throws ServiceException creation of query feed request failed.
   *
   * @see Query#getUrl()
   */
  public GDataRequest createFeedRequest(Query query)
      throws IOException, ServiceException {
    return createRequest(query, contentType);
  }


  /**
   * Returns an Atom entry instance, given the URL of the entry and an
   * if-modified-since date.
   *
   * @param entryUrl resource URL for the entry.
   * @param entryClass class used to represent service entries.
   * @param ifModifiedSince used to set a precondition date that indicates the
   *          entry should be returned only if it has been modified after the
   *          specified date. A value of {@code null} indicates no precondition.
   * @return the entry referenced by the URL parameter.
   * @throws IOException error communicating with the GData service.
   * @throws com.google.gdata.util.NotModifiedException if the entry resource 
   *         has not been modified
   *          after the specified precondition date.
   * @throws com.google.gdata.util.ParseException error parsing the returned 
   *         entry.
   * @throws com.google.gdata.util.ResourceNotFoundException if the entry URL 
   *         is not valid.
   * @throws com.google.gdata.util.ServiceForbiddenException if the GData 
   *          service cannot
   *          get the entry resource due to access constraints.
   * @throws ServiceException if a system error occurred when retrieving
   *          the entry.
   */
  public <E extends BaseEntry<?>> E getEntry(URL entryUrl,
                                             Class<E> entryClass,
                                             DateTime ifModifiedSince)
      throws IOException, ServiceException {

    ParseSource entrySource = null;
    GDataRequest request = createEntryRequest(entryUrl);
    try {

      request.setIfModifiedSince(ifModifiedSince);
      request.execute();
      entrySource = request.getParseSource();
      return parseEntry(entryClass, entrySource);

    } finally {
      closeSource(entrySource);
    }
  }


  /**
   * Returns an Atom entry instance, given the URL of the entry.
   *
   * @param entryUrl resource URL for the entry.
   * @param entryClass class used to represent service entries.
   * @return the entry referenced by the URL parameter.
   * @throws IOException error communicating with the GData service.
   * @throws com.google.gdata.util.ParseException error parsing the returned 
   *         entry.
   * @throws com.google.gdata.util.ResourceNotFoundException if the entry URL 
   *         is not valid.
   * @throws com.google.gdata.util.ServiceForbiddenException if the GData 
   *         service cannot
   *         get the entry resource due to access constraints.
   * @throws ServiceException if a system error occurred when retrieving
   *          the entry.
   */
  public <E extends BaseEntry<?>> E getEntry(URL entryUrl, Class<E> entryClass)
      throws IOException, ServiceException {
    return getEntry(entryUrl, entryClass, null);
  }


  /**
   * Returns a GDataRequest instance that can be used to access an
   * entry's contents as a stream, given the URL of the entry.
   *
   * @param entryUrl resource URL for the entry.
   * @return GData request instance that can be used to read the entry.
   * @throws IOException error communicating with the GData service.
   * @throws ServiceException entry request creation failed.
   */
  public GDataRequest createEntryRequest(URL entryUrl)
      throws IOException, ServiceException {
    return createRequest(GDataRequest.RequestType.QUERY, entryUrl,
                         contentType);
  }


  /**
   * Executes a GData query against the target service and returns the
   * {@link Feed} containing entries that match the query result, if it's been
   * modified since the specified date.
   * 
   * @param query Query instance defining target feed and query parameters.
   * @param feedClass the Class used to represent a service Feed.
   * @param ifModifiedSince used to set a precondition date that indicates the
   *        query result feed should be returned only if contains entries that
   *        have been modified after the specified date. A value of {@code null}
   *        indicates no precondition.
   * @throws IOException error communicating with the GData service.
   * @throws com.google.gdata.util.NotModifiedException if the query resource
   *         does not contain entries modified since the specified precondition
   *         date.
   * @throws com.google.gdata.util.ServiceForbiddenException feed does not
   *         support the query.
   * @throws com.google.gdata.util.ParseException error parsing the returned
   *         feed data.
   * @throws ServiceException query request failed.
   */
  public <F extends BaseFeed<?, ?>> F query(Query query,
                                            Class<F> feedClass,
                                            DateTime ifModifiedSince)
      throws IOException, ServiceException {

    // A query is really same as getFeed against the combined feed + query URL
    return getFeed(query, feedClass, ifModifiedSince);
  }


  /**
   * Executes a GData query against the target service and returns the
   * {@link Feed} containing entries that match the query result.
   *
   * @param query Query instance defining target feed and query parameters.
   * @param feedClass the Class used to represent a service Feed.
   * @throws IOException error communicating with the GData service.
   * @throws com.google.gdata.util.ServiceForbiddenException feed does not 
   *         support the query.
   * @throws com.google.gdata.util.ParseException error parsing the returned 
   *         feed data.
   * @throws ServiceException query request failed.
   */
  public <F extends BaseFeed<?, ?>> F query(Query query, Class<F> feedClass)
      throws IOException, ServiceException {

    // A query is really same as getFeed against the combined feed + query URL
    return query(query, feedClass, null);
  }


  /**
   * Inserts a new {@link com.google.gdata.data.Entry} into a feed associated
   * with the target service.  It will return the inserted Entry, including
   * any additional attributes or extensions set by the GData server.
   *
   * @param feedUrl the POST URI associated with the target feed.
   * @param entry the new entry to insert into the feed.
   * @return the newly inserted Entry returned by the service.
   * @throws IOException error communicating with the GData service.
   * @throws com.google.gdata.util.ParseException error parsing the return entry
   *         data.
   * @throws com.google.gdata.util.ServiceForbiddenException the inserted Entry
   *         has associated media
   *         content and can only be inserted using a media service.
   * @throws ServiceException insert request failed due to system error.
   *
   * @see BaseFeed#getEntryPostLink()
   * @see BaseFeed#insert(BaseEntry)
   */
  @SuppressWarnings("unchecked")
  public <E extends BaseEntry<?>> E insert(URL feedUrl, E entry)
      throws IOException, ServiceException {

    if (entry == null) {
      throw new NullPointerException("Must supply entry");
    }

    ParseSource resultEntrySource = null;
    try {
      GDataRequest request = createInsertRequest(feedUrl);
      XmlWriter xw = request.getRequestWriter();
      entry.generateAtom(xw, extProfile);
      xw.flush();

      request.execute();

      resultEntrySource = request.getParseSource();
      return (E) parseEntry(entry.getClass(), resultEntrySource);

    } finally {
      closeSource(resultEntrySource);
    }
  }


  /**
   * Executes several operations (insert, update or delete) on the entries
   * that are part of the input {@link Feed}. It will return another feed that
   * describes what was done while executing these operations.
   *
   * It is possible for one batch operation to fail even though other
   * operations have worked, so this method won't throw a ServiceException
   * unless something really wrong is going on. You need to check the
   * entries in the returned feed to know which operations succeeded
   * and which operations failed (see
   * {@link com.google.gdata.data.batch.BatchStatus}
   * and {@link com.google.gdata.data.batch.BatchInterrupted} extensions.)
   *
   * @param feedUrl the POST URI associated with the target feed.
   * @param inputFeed a description of the operations to execute, described
   *   using tags in the batch: namespace
   * @return a feed with the result of each operation in a separate
   *   entry
   * @throws IOException error communicating with the GData service.
   * @throws com.google.gdata.util.ParseException error parsing the return 
   *         entry data.
   * @throws ServiceException insert request failed due to system error.
   * @throws BatchInterruptedException if something really wrong was detected
   *   by the server while parsing the request, like invalid XML data. Some
   *   operations might have succeeded when this exception is thrown. Check
   *   {@link BatchInterruptedException#getFeed()}.
   *
   * @see BaseFeed#getEntryPostLink()
   * @see BaseFeed#insert(BaseEntry)
   */
  @SuppressWarnings("unchecked")
  public <F extends BaseFeed<?, ?>> F batch(URL feedUrl, F inputFeed)
      throws IOException, ServiceException, BatchInterruptedException {
    ParseSource resultFeedSource = null;
    GDataRequest request = createInsertRequest(feedUrl);
    try {
      XmlWriter xw = request.getRequestWriter();
      inputFeed.generateAtom(xw, extProfile);
      xw.flush();

      request.execute();

      resultFeedSource = request.getParseSource();
      F resultFeed = (F)
          BaseFeed.readFeed(resultFeedSource, inputFeed.getClass(),
              extProfile);
      resultFeed.setService(this);

      // Detect BatchInterrupted
      int count = resultFeed.getEntries().size();
      if (count > 0) {
        BaseEntry<?> entry = resultFeed.getEntries().get(count - 1);
        BatchInterrupted interrupted = BatchUtils.getBatchInterrupted(entry);
        if (interrupted != null) {
          throw new BatchInterruptedException(resultFeed, interrupted);
        }
      }

      return resultFeed;

    } finally {
      closeSource(resultFeedSource);
    }
  }

  /**
   * Creates a new GDataRequest that can be used to insert a new entry into
   * a feed using the request stream and to read the resulting entry
   * content from the response stream.
   *
   * @param feedUrl the POST URI associated with the target feed.
   * @return GDataRequest to interact with remote GData service.
   * @throws IOException error reading from or writing to the GData service.
   * @throws ServiceException insert request failed.
   */
  public GDataRequest createInsertRequest(URL feedUrl)
      throws IOException, ServiceException {
    return createRequest(GDataRequest.RequestType.INSERT, feedUrl, contentType);
  }

  /**
   * Creates a new GDataRequest that can be used to execute several
   * insert/update/delete operations in one request by writing a
   * feed into the request stream to read a feed containing the
   * result of the batch operations from the response stream.
   *
   * @param feedUrl the POST URI associated with the target feed.
   * @return GDataRequest to interact with remote GData service.
   * @throws IOException error reading from or writing to the GData service.
   * @throws ServiceException insert request failed.
   */
  public GDataRequest createBatchRequest(URL feedUrl)
      throws IOException, ServiceException {
    return createRequest(GDataRequest.RequestType.BATCH, feedUrl, contentType);
  }


  /**
   * Updates an existing {@link com.google.gdata.data.Entry} by writing
   * it to the specified entry edit URL.  The resulting Entry (after update)
   * will be returned.
   *
   * @param entryUrl the edit URL associated with the entry.
   * @param entry the modified Entry to be written to the server.
   * @return the updated Entry returned by the service.
   * @throws IOException error communicating with the GData service.
   * @throws com.google.gdata.util.ParseException error parsing the updated 
   *         entry data.
   * @throws ServiceException update request failed due to system error.
   *
   * @see BaseEntry#getEditLink()
   * @see BaseEntry#update()
   */
  @SuppressWarnings("unchecked")
  public <E extends BaseEntry<?>> E update(URL entryUrl, E entry)
      throws IOException, ServiceException {

    ParseSource resultEntrySource = null;
    GDataRequest request = createUpdateRequest(entryUrl);
    try {
      // Send the entry
      XmlWriter xw = request.getRequestWriter();
      entry.generateAtom(xw, extProfile);
      xw.flush();

      // Execute the request
      request.execute();

      // Handle the update
      resultEntrySource = request.getParseSource();
      return (E) parseEntry(entry.getClass(), resultEntrySource);

    } finally {
      closeSource(resultEntrySource);
    }
  }


  /**
   * Creates a new GDataRequest that can be used to update an existing
   * Atom entry.   The updated entry content can be written to the
   * GDataRequest request stream and the resulting updated entry can
   * be obtained from the GDataRequest response stream.
   *
   * @param entryUrl the edit URL associated with the entry.
   * @throws IOException error communicating with the GData service.
   * @throws ServiceException creation of update request failed.
   */
  public GDataRequest createUpdateRequest(URL entryUrl)
      throws IOException, ServiceException {

    return createRequest(GDataRequest.RequestType.UPDATE, entryUrl,
        contentType);
  }


  /**
   * Deletes an existing entry (and associated media content, if any) using the
   * specified edit URL.
   *
   * @param resourceUrl the edit or medit edit url associated with the
   *        resource.
   * @throws IOException error communicating with the GData service.
   * @throws com.google.gdata.util.ResourceNotFoundException invalid entry URL.
   * @throws ServiceException delete request failed due to system error.
   */
  public void delete(URL resourceUrl) throws IOException, ServiceException {

    GDataRequest request = createDeleteRequest(resourceUrl);
    request.execute();
  }


  /**
   * Creates a new GDataRequest that can be used to delete an Atom
   * entry.  For delete requests, no input is expected from the request
   * stream nor will any response data be returned.
   *
   * @param entryUrl the edit URL associated with the entry.
   * @throws IOException error communicating with the GData service.
   * @throws ServiceException creation of delete request failed.
   */
  public GDataRequest createDeleteRequest(URL entryUrl)
      throws IOException, ServiceException {

    return createRequest(GDataRequest.RequestType.DELETE, entryUrl,
        contentType);
  }


  /**
   * Closes streams and readers associated with a parse source.
   * 
   * @param source    Parse source.
   * @throws IOException
   */
  protected void closeSource(ParseSource source) throws IOException {
    if (source != null) {
      if (source.getInputStream() != null) {
        source.getInputStream().close();
      }
      if (source.getReader() != null) {
        source.getReader().close();
      }
    }
  }
}
