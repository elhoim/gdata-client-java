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


package com.google.gdata.client.youtube;

import com.google.gdata.util.common.xml.XmlWriter;
import com.google.gdata.client.Service;
import com.google.gdata.client.media.MediaService;
import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.ExtensionProfile;
import com.google.gdata.data.ParseSource;
import com.google.gdata.data.youtube.CommentFeed;
import com.google.gdata.data.youtube.ComplaintFeed;
import com.google.gdata.data.youtube.FormUploadToken;
import com.google.gdata.data.youtube.FriendFeed;
import com.google.gdata.data.youtube.PlaylistFeed;
import com.google.gdata.data.youtube.PlaylistLinkFeed;
import com.google.gdata.data.youtube.RatingFeed;
import com.google.gdata.data.youtube.SubscriptionFeed;
import com.google.gdata.data.youtube.UserProfileFeed;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeNamespace;
import com.google.gdata.util.ServiceException;
import com.google.gdata.util.Version;
import com.google.gdata.util.VersionRegistry;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Java client service for the YouTube GData APIs.
 * 
 * 
 */
public class YouTubeService extends MediaService {
  private static final String SERVICE_NAME = "youtube";

  private static final String SERVICE_VERSION = "YouTube-Java/1.0";

  private static final URL DEFAULT_AUTH_URL;

  static {
    try {
      DEFAULT_AUTH_URL = new URL("https://www.google.com/youtube");
    } catch (MalformedURLException abnormal) {
      throw new IllegalStateException(abnormal);
    }
  }

  /**
   * All released version of the YouTube API.
   */
  public static class Versions {
    /**
     * Initial version of the API, based on GData version 1.
     */
    public static final Version V1 =
        new Version(YouTubeService.class, "1.0", Service.Versions.V1);

    /**
     * Newer version of the API, based on GData version 2.
     */
    public static final Version V2 =
        new Version(YouTubeService.class, "2.0", Service.Versions.V2);

    public static final Version[] ALL = { V1, V2 };
  }

  /**
   * Version 1 is currently the default version for clients.
   */
  public static final Version DEFAULT_VERSION =
      Service.initServiceVersion(YouTubeService.class, YouTubeService.Versions.V1);

  /**
   * Creates a new instance of the service with the given application name.
   * 
   * @param applicationName should be a string identifying the application using
   *        the API, usually in this format:
   *        [company-id]-[app-name]-[app-version].
   *        This is also used as the client id.
   */
  public YouTubeService(String applicationName) {
    this(applicationName, null, DEFAULT_AUTH_URL);
  }

  /**
   * Creates a new instance of the service with the given application name.
   * 
   * @param applicationName should be a string identifying the application using
   *        the API, usually in this format:
   *        [company-id]-[app-name]-[app-version].
   *        This is also used as the client id.
   * @param developerId the developer id to send in every request made through
   *        this client, can be null.
   */
  public YouTubeService(String applicationName, String developerId) {
    this(applicationName, developerId, DEFAULT_AUTH_URL);
  }

  /**
   * Creates a new instance of the service with the given application name and a
   * custom user authentication URL.
   * 
   * @param applicationName should be a string identifying the application using
   *        the API, usually in this format:
   *        [company-id]-[app-name]-[app-version].
   *        This is also used as the client id.
   * @param developerId the developer id to send in every request made through
   *        this client, can be null.
   * @param authBaseUrl the base URL pointing to the authentication server.
   */
  protected YouTubeService(String applicationName, String developerId, URL authBaseUrl) {
    super(SERVICE_NAME, applicationName, authBaseUrl.getProtocol(),
        authBaseUrl.getHost() + ":" + authBaseUrl.getPort() + authBaseUrl.getPath());
    getRequestFactory().setHeader("X-GData-Key", developerId != null ? "key=" + developerId : null);
    getRequestFactory().setHeader("X-GData-Client", applicationName);

    ExtensionProfile profile = getExtensionProfile();
    profile.addDeclarations(new ComplaintFeed());
    profile.addDeclarations(new CommentFeed());
    profile.addDeclarations(new FriendFeed());
    profile.addDeclarations(new PlaylistFeed());
    profile.addDeclarations(new PlaylistLinkFeed());
    profile.addDeclarations(new RatingFeed());
    profile.addDeclarations(new SubscriptionFeed());
    profile.addDeclarations(new UserProfileFeed());
    profile.addDeclarations(new VideoFeed());
  }

  @Override
  public String getServiceVersion() {
    return new StringBuilder()
        .append(SERVICE_VERSION)
        .append(' ')
        .append(super.getServiceVersion())
        .toString();
  }

  /**
   * Returns the current {@link Version} of the YouTube GData API.
   * 
   * @return version.
   */
  public static Version getVersion() {
    return VersionRegistry.get().getVersion(YouTubeService.class);
  }


  /**
   * Generate a form-upload token given the XML description of a new media entry.
   *
   * @param url link with rel={@link YouTubeNamespace#GET_UPLOAD_TOKEN_REL} on a user's
   *            upload feed
   * @param entry XML metadata of a new media entry
   */
  @SuppressWarnings("unchecked")
  public <E extends BaseEntry<?>> FormUploadToken getFormUploadToken(URL url, E entry) 
      throws ServiceException, IOException {

    if (entry == null) {
      throw new NullPointerException("Must supply entry");
    }

    Service.GDataRequest request = createInsertRequest(url);
    XmlWriter xw = request.getRequestWriter();
    entry.generateAtom(xw, extProfile);
    xw.flush();

    request.execute();

    ParseSource resultEntrySource = request.getParseSource();
    try {
      return FormUploadToken.parse(resultEntrySource.getInputStream());
    } finally {
      closeSource(resultEntrySource);
    }
  }
}
