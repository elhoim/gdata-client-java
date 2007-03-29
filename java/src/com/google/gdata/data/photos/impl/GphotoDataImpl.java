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


package com.google.gdata.data.photos.impl;

import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.Extension;
import com.google.gdata.data.ExtensionDescription;
import com.google.gdata.data.ExtensionPoint;
import com.google.gdata.data.ExtensionProfile;
import com.google.gdata.data.ValueConstruct;
import com.google.gdata.data.media.mediarss.MediaContent;
import com.google.gdata.data.media.mediarss.MediaCredit;
import com.google.gdata.data.media.mediarss.MediaDescription;
import com.google.gdata.data.media.mediarss.MediaGroup;
import com.google.gdata.data.media.mediarss.MediaKeywords;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.media.mediarss.MediaTitle;
import com.google.gdata.data.photos.GphotoData;
import com.google.gdata.data.photos.Namespaces;
import com.google.gdata.data.photos.impl.Extensions.GphotoConstruct;
import com.google.gdata.util.ParseException;

import java.util.Date;

/**
 * Basic implementation class for all of the {@link GphotoData} objects.  This
 * provides basic implementations of the standard methods that GphotoData
 * specifies as well as helper methods for subclasses to use when implementing
 * particular data interfaces.
 *
 * 
 */
public class GphotoDataImpl implements GphotoData {

  // The extension point we use for storing and retrieving data.
  private final ExtensionPoint extPoint;

  // The class of the extension point, cached to help declare extensions.
  private final Class<? extends ExtensionPoint> extClass;

  /**
   * Construct a new GphotoData implementation based on the given extension.
   */
  public GphotoDataImpl(ExtensionPoint extensionPoint) {
    this.extPoint = extensionPoint;
    this.extClass = extensionPoint.getClass();
  }

  /*
   * Declare the default gphoto:id, gphoto:type, and gphoto:rsslink extensions.
   */
  public void declareExtensions(ExtensionProfile extProfile) {
    declare(extProfile, GphotoId.getDefaultDescription());
    declare(extProfile, GphotoType.getDefaultDescription());
    declare(extProfile, GphotoRssLink.getDefaultDescription());
  }

  /**
   * Helper method to declare an extension as available only on the local entry
   * class.
   */
  protected void declare(ExtensionProfile extProfile,
      ExtensionDescription description) {
    extProfile.declare(extClass, description);

    // If we're declaring an entry class, we also need to declare that the
    // "BaseEntry" class has the same extensions, for auto-extension support.
    if (BaseEntry.class.isAssignableFrom(extClass)) {
      extProfile.declare(BaseEntry.class, description);
    }
  }

  /**
   * Helper method for subclasses to declare that they accept media extensions.
   */
  protected void declareMediaExtensions(ExtensionProfile extProfile) {
    declare(extProfile, MediaGroup.getDefaultDescription());

    extProfile.declare(MediaGroup.class, MediaContent
        .getDefaultDescription(true));
    extProfile.declare(MediaGroup.class,
        ExtensionDescription.getDefaultDescription(MediaTitle.class));
    extProfile.declare(MediaGroup.class,
        ExtensionDescription.getDefaultDescription(MediaDescription.class));
    extProfile.declare(MediaGroup.class, MediaKeywords.getDefaultDescription());
    ExtensionDescription thumbDesc = MediaThumbnail.getDefaultDescription();
    thumbDesc.setRepeatable(true);
    extProfile.declare(MediaGroup.class, thumbDesc);
    extProfile.declare(MediaGroup.class,
        ExtensionDescription.getDefaultDescription(MediaCredit.class));
  }

  /**
   * @return the Gphoto id.
   */
  public String getGphotoId() {
    return getSimpleValue(GphotoId.class);
  }

  /**
   * Sets the id of this entry.
   */
  public void setGphotoId(Long id) {
    if (id != null) {
      setExtension(new GphotoId(id));
    } else {
      removeExtension(GphotoId.class);
    }
  }

  /**
   * Sets the id of this entry.
   */
  public void setGphotoId(String id) {
    if (id != null) {
      setExtension(new GphotoId(id));
    } else {
      removeExtension(GphotoId.class);
    }
  }

  /**
   * Protected helper to get the simple value from a construct extension.
   */
  protected String getSimpleValue(Class<? extends ValueConstruct> extClass) {
    ValueConstruct construct = getExtension(extClass);
    return construct == null ? null : construct.getValue();
  }

  /**
   * Protected helper to get a date value from a construct.
   */
  protected Date getDateValue(Class<? extends ValueConstruct> extClass)
      throws ParseException {
    String strVal = getSimpleValue(extClass);

    if (strVal == null) return null;

    try {
      return new Date(Long.parseLong(strVal));
    } catch (NumberFormatException nfe) {
      throw new ParseException("Invalid timestamp: " + strVal);
    }
  }

  /**
   * Protected helper to get an integer value from a simple construct.
   */
  protected Integer getIntegerValue(Class<? extends ValueConstruct> extClass)
      throws ParseException {
    String strVal = getSimpleValue(extClass);

    if (strVal == null) return null;

    try {
      return new Integer(strVal);
    } catch (NumberFormatException nfe) {
      throw new ParseException("Invalid int: " + strVal);
    }
  }

  /**
   * Protected helper to get a long value from a simple construct.
   */
  protected Long getLongValue(Class<? extends ValueConstruct> extClass)
      throws ParseException {
    String strVal = getSimpleValue(extClass);

    if (strVal == null) return null;

    try {
      return new Long(strVal);
    } catch (NumberFormatException nfe) {
      throw new ParseException("Invalid long: " + strVal);
    }
  }

  /**
   * Protected helper to get a float value from a simple construct.
   */
  protected Float getFloatValue(Class<? extends ValueConstruct> extClass)
      throws ParseException {
    String strVal = getSimpleValue(extClass);

    if (strVal == null) return null;

    try {
      return new Float(strVal);
    } catch (NumberFormatException nfe) {
      throw new ParseException("Invalid float: " + strVal);
    }
  }

  /**
   * Protected helper to get a boolean value from a simple construct.
   */
  protected Boolean getBooleanValue(Class<? extends ValueConstruct> extClass) {
    String strVal = getSimpleValue(extClass);

    if (strVal == null) return null;

    return new Boolean(strVal);
  }

  /**
   * Get an extension by class.
   * @see ExtensionPoint#getExtension(Class)
   */
  protected <T extends Extension> T getExtension(Class<T> extClass) {
    return extPoint.getExtension(extClass);
  }

  /**
   * Set an extension.
   * @see ExtensionPoint#setExtension(Extension)
   */
  public void setExtension(Extension extension) {
    extPoint.setExtension(extension);
  }

  /**
   * Add an extension.
   * @see ExtensionPoint#addExtension(Extension)
   */
  public void addExtension(Extension extension) {
    extPoint.addExtension(extension);
  }

  /**
   * Remove an extension by class.
   * @see ExtensionPoint#removeExtension(Class)
   */
  public void removeExtension(Class<? extends Extension> extensionClass) {
    extPoint.removeExtension(extensionClass);
  }

  /**
   * Remove an extension.
   * @see ExtensionPoint#removeExtension(Extension)
   */
  public void removeExtension(Extension extension) {
    extPoint.removeExtension(extension);
  }

  /**
   * An extension that represents a gphoto:id, which is a string that is the
   * system-id for a particular item. This is distinguished from the atom:id,
   * which is the url that points to the item on the server, and is subject to
   * change. The gphoto:id of an item will never change.
   */
  public static class GphotoId extends GphotoConstruct {
    public GphotoId() {
      this((String) null);
    }

    public GphotoId(String id) {
      super("id", id);
    }

    public GphotoId(Long id) {
      this(id == null ? null : Long.toString(id));
    }

    public static ExtensionDescription getDefaultDescription() {
      return new ExtensionDescription(GphotoId.class,
          Namespaces.PHOTOS_NAMESPACE, "id");
    }
  }

  /**
   * Gphoto rss link class, used for gphoto:rsslink elements. This item is
   * provided for backwards compatibility with existing clients, and should not
   * be used for new code.
   *
   * @deprecated Clients should modify the alt-type themselves to request RSS.
   */
  public static class GphotoRssLink extends GphotoConstruct {
    public GphotoRssLink() {
      this(null);
    }

    public GphotoRssLink(String linkHref) {
      super("rsslink", linkHref);
    }

    public static ExtensionDescription getDefaultDescription() {
      return new ExtensionDescription(GphotoRssLink.class,
          Namespaces.PHOTOS_NAMESPACE, "rsslink");
    }
  }

  /**
   * Gphoto type class, used for gphoto:type elements. This class is provided
   * for backwards compatibility with existing clients, and should not be used
   * for new code.
   *
   * @deprecated Clients should use the atom:category to discover the kind of an
   *             item.
   */
  public static class GphotoType extends GphotoConstruct {
    public GphotoType() {
      this(null);
    }

    public GphotoType(String type) {
      super("type", type);
    }

    public static ExtensionDescription getDefaultDescription() {
      return new ExtensionDescription(GphotoType.class,
          Namespaces.PHOTOS_NAMESPACE, "type");
    }
  }
}
