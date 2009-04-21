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


package com.google.gdata.model.atom;

import com.google.gdata.data.ILink;
import com.google.gdata.model.AttributeKey;
import com.google.gdata.model.ContentModel.Cardinality;
import com.google.gdata.model.DefaultRegistry;
import com.google.gdata.model.Element;
import com.google.gdata.model.ElementCreator;
import com.google.gdata.model.ElementKey;
import com.google.gdata.model.ElementMetadata;
import com.google.gdata.model.QName;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.Namespaces;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Class representing atom:link.
 */
public class Link extends Element implements ILink {

  /**
   * The Rel class defines constants for some common link relation types.
   */
  public static final class Rel {

    /**
     * Link provides the URI of the feed or entry. If this
     * relation appears on a feed that is the result of performing a
     * query, then this URI includes the same query parameters (or at
     * least querying this URI produces the same result as querying with
     * those parameters).
     */
    public static final String SELF = "self";

    /** Link provides the URI of previous page in a paged feed. */
    public static final String PREVIOUS = "previous";

    /** Link provides the URI of next page in a paged feed. */
    public static final String NEXT = "next";

    /**
     * Link provides the URI of an alternate format of the
     * entry's or feed's contents. The {@code type} property of the link
     * specifies a media type.
     */
    public static final String ALTERNATE = "alternate";

    /**
     * Link provides the URI of a related link to the entry
     */
    public static final String RELATED = "related";

    /**
     * Link provides the URI of the full feed (without any
     * query parameters).
     */
    public static final String FEED = Namespaces.gPrefix + "feed";

    /**
     * Link provides the URI that can be used to post new
     * entries to the feed. This relation does not exist if the feed is
     * read-only.
     */
    public static final String ENTRY_POST = Namespaces.gPrefix + "post";

    /**
     * Link provides the URI that can be used to edit the entry.
     * This relation does not exist if the entry is read-only.
     */
    public static final String ENTRY_EDIT = "edit";

    /**
     * Link provides the URI that can be used to edit the media
     * associated with an entry.  This relation does not exist if
     * there is no associated media or the media is read-only.
     */
    public static final String MEDIA_EDIT = "edit-media";

    /**
     * Previous media edit link relation value that will temporarily be
     * supported to enable back compatibility for Picasa Web.  This rel
     * will be deleted after all usage has been migrated to use
     * {@link #MEDIA_EDIT}.
     *
     * @deprecated use {@link Rel#MEDIA_EDIT} instead.
     */
    @Deprecated
    public static final String MEDIA_EDIT_BACKCOMPAT = "media-edit";

    /**
     * Link provides the URI that can be used to insert, update
     * and delete entries on this feed. This relation does not exist
     * if the feed is read-only or if batching not enabled on this
     * feed.
     */
    public static final String FEED_BATCH = Namespaces.gPrefix + "batch";

    /**
     * Link provides the URI that of link that provides the data
     * for the content in the feed.
     */
    public static final String VIA = "via";

    private Rel() {}
  }

  /**
   * The Type class contains several common link content types.
   */
  public static final class Type {


    /** Defines the link type used for Atom content. */
    public static final String ATOM = ContentType.ATOM.getMediaType();


    /** Defines the link type used for HTML content. */
    public static final String HTML = ContentType.TEXT_HTML.getMediaType();

    private Type() {}
  }

  /**
   * The key for this element.
   */
  public static final ElementKey<Void, Link> KEY = ElementKey.of(
      new QName(Namespaces.atomNs, "link"), Link.class);

  /**
   * The href attribute.
   */
  public static final AttributeKey<String> HREF = AttributeKey.of(
      new QName("href"));

  /**
   * The rel attribute.
   */
  public static final AttributeKey<String> REL = AttributeKey.of(
      new QName("rel"));

  /**
   * The type attribute.
   */
  public static final AttributeKey<String> TYPE = AttributeKey.of(
      new QName("type"));

  /**
   * The hreflang attribute.
   */
  public static final AttributeKey<String> HREFLANG = AttributeKey.of(
      new QName("hreflang"));

  /**
   * The title attribute.
   */
  public static final AttributeKey<String> TITLE = AttributeKey.of(
      new QName("title"));

  /**
   * The xml:lang attribute.
   */
  public static final AttributeKey<String> XML_LANG = AttributeKey.of(
      new QName(Namespaces.xmlNs, "lang"));

  /**
   * The length attribute.
   */
  public static final AttributeKey<Long> LENGTH = AttributeKey.of(
      new QName("length"), Long.class);

  /**
   * The etag attribute.
   */
  public static final AttributeKey<String> ETAG = AttributeKey.of(
      new QName(Namespaces.gNs, "etag"));

  /*
   * Generate the default metadata for this element.
   */
  static {
    ElementCreator builder = DefaultRegistry.build(KEY)
        .setCardinality(Cardinality.MULTIPLE);
    builder.addAttribute(REL);
    builder.addAttribute(TYPE);
    builder.addAttribute(HREF).setRequired(true);
    builder.addAttribute(HREFLANG);
    builder.addAttribute(TITLE);
    builder.addAttribute(XML_LANG);
    builder.addAttribute(LENGTH);
    builder.addAttribute(ETAG);
    builder.addElement(Content.KEY);
  }

  /**
   * Constructs a new instance using the default metadata.
   */
  public Link() {
    super(DefaultRegistry.get(KEY));
  }

  /**
   * Constructs a new instance using the specified element metadata.
   *
   * @param elementMetadata metadata describing the expected attributes and
   *        child elements.
   */
  public Link(ElementMetadata<?, ? extends Link> elementMetadata) {
    super(elementMetadata);
  }

  /**
   * Constructs a new instance by doing a shallow copy of data from an existing
   * {@link Element} instance. Will use the given {@link ElementMetadata} as the
   * metadata for the element.
   *
   * @param metadata metadata to use for this element.
   * @param source source element
   */
  public Link(ElementMetadata<Void, ? extends Link> metadata,
      Element source) {
    super(metadata, source);
  }

  /**
   * Constructs a new instance using the default metadata, and setting
   * the links rel, type, and href attributes.
   *
   * @deprecated Use {@link #Link(String, String, URI)} instead.
   */
  @Deprecated
  public Link(String rel, String type, String href) {
    this();
    setRel(rel);
    setType(type);
    setHref(href);
  }

  /**
   * Constructs a new instance using the default metadata, and setting
   * the links rel, type, and href attributes.
   */
  public Link(String rel, String type, URI href) {
    this();
    setRel(rel);
    setType(type);
    setHref(href);
  }

  /**
   * Link relation type.  Possible values include {@code self}, {@code
   * prev}, {@code next}, {@code enclosure}, etc.
   */
  public String getRel() {
    String rel = getAttributeValue(REL);
    return rel != null ? rel : Rel.ALTERNATE;
  }
  public void setRel(String v) {
    addAttribute(REL, v);
  }

  /** MIME type of the link target. */
  public String getType() {
    return getAttributeValue(TYPE);
  }
  public void setType(String v) {
    addAttribute(TYPE, v);
  }

  /** Link URI. */
  public String getHref() {
    return getAttributeValue(HREF);
  }
  public URI getHrefUri() {
    String href = getHref();
    try {
      return href == null ? null : new URI(href);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }
  public void setHref(String v) {
    addAttribute(HREF, v);
  }
  public void setHref(URI v) {
    String href = v == null ? null : v.toString();
    setHref(href);
  }

  /** Language of resource pointed to by href. */
  public String getHrefLang() {
    return getAttributeValue(HREFLANG);
  }
  public void setHrefLang(String v) {
    addAttribute(HREFLANG, v);
  }

  /** Link title. */
  public String getTitle() {
    return getAttributeValue(TITLE);
  }
  public void setTitle(String v) {
    addAttribute(TITLE, v);
  }

  /** Language of link title. */
  public String getTitleLang() {
    return getAttributeValue(XML_LANG);
  }
  public void setTitleLang(String v) {
    addAttribute(XML_LANG, v);
  }

  /** Length of the resource pointed to by href, in bytes. */
  protected long length = -1;
  public long getLength() {
    Long value = getAttributeValue(LENGTH);
    if (value == null) {
      return -1;
    }
    return value;
  }
  public void setLength(long v) {
    addAttribute(LENGTH, v);
  }

  /** Etag of linked resource or {@code null} if unknown. */
  public String getEtag() {
    return getAttributeValue(ETAG);
  }
  public void setEtag(String v) {
    addAttribute(ETAG, v);
  }

  /**
   * Return the content of the link, or {@code null} if no content has been set.
   * This is used to inline an atom:content element inside an atom:link element.
   *
   * @return the atom:content element, or null if none exists.
   */
  public Content getContent() {
    return getElement(Content.KEY);
  }

  /**
   * Sets the atom:content element nested inside this atom:link.
   *
   * @param c the content to place inside the link.
   */
  public void setContent(Content c) {
    addElement(c);
  }

  /**
   * Returns whether this link matches the given {@code rel} and {@code type}
   * values.
   *
   * @param relToMatch  {@code rel} value to match or {@code null} to match any
   *                    {@code rel} value.
   * @param typeToMatch {@code type} value to match or {@code null} to match any
   *                    {@code type} value.
   */
  public boolean matches(String relToMatch, String typeToMatch) {
    return (relToMatch == null || relToMatch.equals(getRel()))
        && (typeToMatch == null || typeToMatch.equals(getType()));
  }
}
