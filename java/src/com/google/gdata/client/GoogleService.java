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

import com.google.gdata.client.AuthTokenFactory.AuthToken;
import com.google.gdata.client.AuthTokenFactory.TokenListener;
import com.google.gdata.client.http.GoogleGDataRequest;
import com.google.gdata.client.http.GoogleGDataRequest.GoogleCookie;
import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.BaseFeed;
import com.google.gdata.data.DateTime;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.RedirectRequiredException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivateKey;
import java.util.Map;
import java.util.Set;

/**
 * The GoogleService class extends the basic GData {@link Service}
 * abstraction to add support for authentication and cookies.
 *
 * 
 */
public class GoogleService extends Service implements TokenListener {

  
  // Authentication token factory used to access Google services
  private AuthTokenFactory authTokenFactory;


  // Cookie manager.
  private CookieManager cookieManager;


  /**
   * Authentication failed, invalid credentials presented to server.
   */
  public static class InvalidCredentialsException
      extends AuthenticationException {
    public InvalidCredentialsException(String message) {
      super(message);
    }
  }


  /**
   * Authentication failed, account has been deleted.
   */
  public static class AccountDeletedException extends AuthenticationException {
    public AccountDeletedException(String message) {
      super(message);
    }
  }


  /**
   * Authentication failed, account has been disabled.
   */
  public static class AccountDisabledException extends AuthenticationException {
    public AccountDisabledException(String message) {
      super(message);
    }
  }


  /**
   * Authentication failed, account has not been verified.
   */
  public static class NotVerifiedException extends AuthenticationException {
    public NotVerifiedException(String message) {
      super(message);
    }
  }


  /**
   * Authentication failed, user did not agree to the terms of service.
   */
  public static class TermsNotAgreedException extends AuthenticationException {
    public TermsNotAgreedException(String message) {
      super(message);
    }
  }


  /**
   * Authentication failed, authentication service not available.
   */
  public static class ServiceUnavailableException extends
      AuthenticationException {
    public ServiceUnavailableException(String message) {
      super(message);
    }
  }


  /**
   * Authentication failed, CAPTCHA requires answering.
   */
  public static class CaptchaRequiredException extends AuthenticationException {

    private String captchaUrl;
    private String captchaToken;

    public CaptchaRequiredException(String message,
                                    String captchaUrl,
                                    String captchaToken) {
      super(message);
      this.captchaToken = captchaToken;
      this.captchaUrl = captchaUrl;
    }

    public String getCaptchaToken() { return captchaToken; }
    public String getCaptchaUrl() { return captchaUrl; }
  }


  /**
   * Authentication failed, the token's session has expired.
   */
  public static class SessionExpiredException extends AuthenticationException {
    public SessionExpiredException(String message) {
      super(message);
    }
  }

  
  /**
   * The UserToken encapsulates the token retrieved as a result of
   * authenticating to Google using a user's credentials.
   * 
   * @deprecated This class has been deprecated. Please use
   * {@link com.google.gdata.client.GoogleAuthTokenFactory.UserToken}
   * instead.
   */
  public static class UserToken extends GoogleAuthTokenFactory.UserToken {
    public UserToken(String token) {
      super(token);
    }
  }


  /**
   * Encapsulates the token used by web applications to login on behalf of
   * a user.
   * 
   * @deprecated This class has been deprecated. Please use
   * {@link com.google.gdata.client.GoogleAuthTokenFactory.AuthSubToken}
   * instead.
   */
  public static class AuthSubToken extends GoogleAuthTokenFactory.AuthSubToken {
    public AuthSubToken(String token, PrivateKey key) {
      super(token, key);
    }
  }

  
  /**
   * Constructs a GoogleService instance connecting to the service with name
   * {@code serviceName} for an application with the name
   * {@code applicationName}. The default domain (www.google.com) and the
   * default Google authentication methods will be used to authenticate.
   * A simple cookie manager is used.
   *
   * @param serviceName     the name of the Google service to which we are
   *                        connecting. Sample names of services might include
   *                        "cl" (Calendar), "mail" (GMail), or
   *                        "blogger" (Blogger)
   * @param applicationName the name of the client application accessing the
   *                        service.  Application names should preferably have
   *                        the format [company-id]-[app-name]-[app-version].
   *                        The name will be used by the Google servers to
   *                        monitor the source of authentication.
   */
  public GoogleService(String serviceName,
                       String applicationName) {

    this(serviceName, applicationName, "https", "www.google.com");
  }


  /**
   * Constructs a GoogleService instance connecting to the service with name
   * {@code serviceName} for an application with the name
   * {@code applicationName}.  The service will authenticate at the provided
   * {@code domainName}. The default Google authentication methods will be
   * used to authenticate. A simple cookie manager is used.
   *
   * @param serviceName     the name of the Google service to which we are
   *                        connecting. Sample names of services might include
   *                        "cl" (Calendar), "mail" (GMail), or
   *                        "blogger" (Blogger)
   * @param applicationName the name of the client application accessing the
   *                        service. Application names should preferably have
   *                        the format [company-id]-[app-name]-[app-version].
   *                        The name will be used by the Google servers to
   *                        monitor the source of authentication.
   * @param protocol        name of protocol to use for authentication
   *                        ("http"/"https")
   * @param domainName      the name of the domain hosting the login handler
   */
  public GoogleService(String serviceName,
                       String applicationName,
                       String protocol,
                       String domainName) {
    requestFactory = new GoogleGDataRequest.Factory();
    authTokenFactory =
        new GoogleAuthTokenFactory(serviceName, applicationName,
                                   protocol, domainName, this);
    cookieManager = new SimpleCookieManager();
    
    if (applicationName != null) {
      requestFactory.setHeader("User-Agent",
          applicationName + " " + getServiceVersion());
    } else {
      requestFactory.setHeader("User-Agent", getServiceVersion());
    }
  }


  /**
   * Returns the {@link AuthTokenFactory} currently associated with the service.
   */
  public AuthTokenFactory getAuthTokenFactory() {
    return authTokenFactory;
  }


  /**
   * Sets the {@link AuthTokenFactory} currently associated with the service.
   * 
   * @param authTokenFactory Authentication factory
   */
  public void setAuthTokenFactory(AuthTokenFactory authTokenFactory) {
    this.authTokenFactory = authTokenFactory;
  }


  /**
   * Returns the {@link CookieManager} currently associated with the service.
   */
  public CookieManager getCookieManager() {
    return cookieManager;
  }


  /**
   * Sets the {@link CookieManager} currently associated with the service.
   * 
   * @param cookieManager Cookie manager
   */
  public void setCookieManager(CookieManager cookieManager) {
    this.cookieManager = cookieManager;
  }


  public void tokenChanged(AuthToken newToken) {
    if (cookieManager != null) {
      // Flush any cookies that might contain session info for the
      // previous user.
      cookieManager.clearCookies();
    }
  }


  /**
   * Sets the credentials of the user to authenticate requests to the server.
   *
   * @param username the name of the user (an email address)
   * @param password the password of the user
   * @throws AuthenticationException if authentication failed.
   */
  public void setUserCredentials(String username, String password)
      throws AuthenticationException {

    setUserCredentials(username, password, null, null);
  }


  /**
   * Sets the credentials of the user to authenticate requests to the server.
   * A CAPTCHA token and a CAPTCHA answer can also be optionally provided
   * to authenticate when the authentication server requires that a
   * CAPTCHA be answered.
   *
   * @param username the name of the user (an email id)
   * @param password the password of the user
   * @param captchaToken the CAPTCHA token issued by the server
   * @param captchaAnswer the answer to the respective CAPTCHA token
   * @throws AuthenticationException if authentication failed
   */
  public void setUserCredentials(String username,
                                 String password,
                                 String captchaToken,
                                 String captchaAnswer)
      throws AuthenticationException {

    GoogleAuthTokenFactory googleAuthTokenFactory = getGoogleAuthTokenFactory();
    googleAuthTokenFactory.setUserCredentials(username, password,
                                              captchaToken, captchaAnswer);
    requestFactory.setAuthToken(authTokenFactory.getAuthToken());
  }

  /**
   * Sets the AuthToken that should be used to authenticate requests to the
   * server. This is useful if the caller has some other way of accessing the
   * AuthToken, versus calling getAuthToken with credentials to request the
   * AuthToken using ClientLogin.
   *
   * @param token the AuthToken in ascii form
   */
  public void setUserToken(String token) {
    
    GoogleAuthTokenFactory googleAuthTokenFactory = getGoogleAuthTokenFactory();
    googleAuthTokenFactory.setUserToken(token);
    requestFactory.setAuthToken(authTokenFactory.getAuthToken());
  }


  /**
   * Sets the AuthSub token to be used to authenticate a user.
   *
   * @param token the AuthSub token retrieved from Google
   */
  public void setAuthSubToken(String token) {
    setAuthSubToken(token, null);
  }


  /**
   * Sets the AuthSub token to be used to authenticate a user.  The token
   * will be used in secure-mode, and the provided private key will be used
   * to sign all requests.
   *
   * @param token the AuthSub token retrieved from Google
   * @param key the private key to be used to sign all requests
   */
  public void setAuthSubToken(String token,
                              PrivateKey key) {

    GoogleAuthTokenFactory googleAuthTokenFactory = getGoogleAuthTokenFactory();
    googleAuthTokenFactory.setAuthSubToken(token, key);
    requestFactory.setAuthToken(authTokenFactory.getAuthToken());
  }

  
  /**
   * Retrieves the authentication token for the provided set of credentials.
   *
   * @param username the name of the user (an email address)
   * @param password the password of the user
   * @param captchaToken the CAPTCHA token if CAPTCHA is required (Optional)
   * @param captchaAnswer the respective answer of the CAPTCHA token (Optional)
   * @param serviceName the name of the service to which a token is required
   * @param applicationName the application requesting the token
   * @return the token
   * @throws AuthenticationException if authentication failed
   */
  public String getAuthToken(String username,
                             String password,
                             String captchaToken,
                             String captchaAnswer,
                             String serviceName,
                             String applicationName)
      throws AuthenticationException {

    GoogleAuthTokenFactory googleAuthTokenFactory = getGoogleAuthTokenFactory();
    return googleAuthTokenFactory.getAuthToken(username, password, captchaToken,
                                               captchaAnswer, serviceName,
                                               applicationName);
  }


  /**
   * Makes a HTTP POST request to the provided {@code url} given the
   * provided {@code parameters}.  It returns the output from the POST
   * handler as a String object.
   *
   * @param url the URL to post the request
   * @param parameters the parameters to post to the handler
   * @return the output from the handler
   * @throws IOException if an I/O exception occurs while creating, writing,
   *                     or reading the request
   */
  public static String makePostRequest(URL url,
                                       Map<String, String> parameters)
      throws IOException {

    return GoogleAuthTokenFactory.makePostRequest(url, parameters);
  }


  /**
   * Enables or disables cookie handling.
   */
  public void setHandlesCookies(boolean handlesCookies) {
    if (cookieManager == null) {
      if (handlesCookies) {
        throw new IllegalArgumentException("No cookie manager defined");
      }
      return;
    }
    cookieManager.setCookiesEnabled(handlesCookies);
  }


  /**
   * Returns  {@code true} if the GoogleService is handling cookies.
   */
  public boolean handlesCookies() {
    if (cookieManager == null) {
      return false;
    }
    return cookieManager.cookiesEnabled();
  }


  /**
   * Adds a new GoogleCookie instance to the cache.
   */
  public void addCookie(GoogleCookie cookie) {
    if (cookieManager != null) {
      cookieManager.addCookie(cookie);
    }
  }


  /**
   * Returns the set of associated cookies returned by previous requests.
   */
  public Set<GoogleCookie> getCookies() {
    if (cookieManager == null) {
      throw new IllegalArgumentException("No cookie manager defined");
    }
    return cookieManager.getCookies();
  }


  @Override
  public GDataRequest createRequest(GDataRequest.RequestType type,
                                    URL requestUrl,
                                    ContentType contentType)
      throws IOException, ServiceException {
    GDataRequest request = super.createRequest(type, requestUrl, contentType);
    if (request instanceof GoogleGDataRequest) {
      ((GoogleGDataRequest) request).setService(this);
    }
    return request;
  }


  @Override
  public <E extends BaseEntry<?>> E getEntry(URL entryUrl,
                                             Class<E> entryClass,
                                             DateTime ifModifiedSince)
      throws IOException, ServiceException {

    try {
      return super.getEntry(entryUrl, entryClass, ifModifiedSince);
    } catch (RedirectRequiredException e) {
      entryUrl = handleRedirectException(e);
    } catch (SessionExpiredException e) {
      handleSessionExpiredException(e);
    }

    return super.getEntry(entryUrl, entryClass, ifModifiedSince);
  }


  @Override
  public <E extends BaseEntry<?>> E getEntry(URL entryUrl,
                                             Class<E> entryClass,
                                             String etag)
      throws IOException, ServiceException {

    try {
      return super.getEntry(entryUrl, entryClass, etag);
    } catch (RedirectRequiredException e) {
      entryUrl = handleRedirectException(e);
    } catch (SessionExpiredException e) {
      handleSessionExpiredException(e);
    }

    return super.getEntry(entryUrl, entryClass, etag);
  }


  @Override
  public <E extends BaseEntry<?>> E update(URL entryUrl, E entry)
      throws IOException, ServiceException {

    try {
      return super.update(entryUrl, entry);
    } catch (RedirectRequiredException e) {
      entryUrl = handleRedirectException(e);
    } catch (SessionExpiredException e) {
      handleSessionExpiredException(e);
    }

    return super.update(entryUrl, entry);
  }


  @Override
  public <E extends BaseEntry<?>> E insert(URL feedUrl, E entry)
      throws IOException, ServiceException {

    try {
      return super.insert(feedUrl, entry);
    } catch (RedirectRequiredException e) {
      feedUrl = handleRedirectException(e);
    } catch (SessionExpiredException e) {
      handleSessionExpiredException(e);
    }

    return super.insert(feedUrl, entry);
  }


  @Override
  public <F extends BaseFeed<?, ?>> F getFeed(URL feedUrl,
                                              Class<F> feedClass,
                                              DateTime ifModifiedSince)
      throws IOException, ServiceException {

    try {
      return super.getFeed(feedUrl, feedClass, ifModifiedSince);
    } catch (RedirectRequiredException e) {
      feedUrl = handleRedirectException(e);
    } catch (SessionExpiredException e) {
      handleSessionExpiredException(e);
    }

    return super.getFeed(feedUrl, feedClass, ifModifiedSince);
  }

  @Override
  public <F extends BaseFeed<?, ?>> F getFeed(URL feedUrl, Class<F> feedClass,
      String etag) throws IOException, ServiceException {
    try {
      return super.getFeed(feedUrl, feedClass, etag);
    } catch (RedirectRequiredException e) {
      feedUrl = handleRedirectException(e);
    } catch (SessionExpiredException e) {
      handleSessionExpiredException(e);
    }

    return super.getFeed(feedUrl, feedClass, etag);
  }

  @Override
  public <F extends BaseFeed<?, ?>> F getFeed(Query query,
                                              Class<F> feedClass,
                                              DateTime ifModifiedSince)
      throws IOException, ServiceException {

    try {
      return super.getFeed(query, feedClass, ifModifiedSince);
    } catch (RedirectRequiredException e) {
      query = new Query(handleRedirectException(e));
    } catch (SessionExpiredException e) {
      handleSessionExpiredException(e);
    }

    return super.getFeed(query, feedClass, ifModifiedSince);
  }

  @Override
  public <F extends BaseFeed<?, ?>> F getFeed(Query query, Class<F> feedClass,
      String etag) throws IOException, ServiceException {
    try {
      return super.getFeed(query, feedClass, etag);
    } catch (RedirectRequiredException e) {
      query = new Query(handleRedirectException(e));
    } catch (SessionExpiredException e) {
      handleSessionExpiredException(e);
    }

    return super.getFeed(query, feedClass, etag);
  }

  @Override
  public void delete(URL entryUrl) throws IOException, ServiceException {

    try {
      super.delete(entryUrl);
      return;
    } catch (RedirectRequiredException e) {
      entryUrl = handleRedirectException(e);
    } catch (SessionExpiredException e) {
      handleSessionExpiredException(e);
    }

    super.delete(entryUrl);
  }

  @Override
  public void delete(URL entryUrl, String etag)
      throws IOException, ServiceException {
    try {
      super.delete(entryUrl, etag);
      return;
    } catch (RedirectRequiredException e) {
      entryUrl = handleRedirectException(e);
    } catch (SessionExpiredException e) {
      handleSessionExpiredException(e);
    }

    super.delete(entryUrl);
  }

  /**
   * Handles a redirect exception by generating the new URL to use for the
   * redirect.
   */
  protected URL handleRedirectException(RedirectRequiredException redirect)
      throws ServiceException {
    try {
      return new URL(redirect.getRedirectLocation());
    } catch (MalformedURLException e) {
      throw new ServiceException("Invalid redirected-to URL - "
                                 + redirect.getRedirectLocation());
    }
  }

  /**
   * Delegates session expired exception to  {@link AuthTokenFactory}.
   */
  protected void handleSessionExpiredException(SessionExpiredException e)
      throws ServiceException {
    authTokenFactory.handleSessionExpiredException(e);
  }


  /**
   * Get the {@link GoogleAuthTokenFactory} current associated with this
   * service. If an auth token factory of a different type is configured,
   * an {@link IllegalStateException} is thrown.
   * 
   * @return Google authentication token factory.
   */
  private GoogleAuthTokenFactory getGoogleAuthTokenFactory() {
    if (!(authTokenFactory instanceof GoogleAuthTokenFactory)) {
      throw new IllegalStateException("Invalid authentication token factory");
    }
    return (GoogleAuthTokenFactory) authTokenFactory;
  }
}
