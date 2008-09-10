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


package com.google.gdata.data;

import com.google.gdata.util.common.xml.XmlWriter;
import com.google.gdata.client.CoreErrorDomain;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.Namespaces;
import com.google.gdata.util.ParseException;
import com.google.gdata.util.XmlParser;


import java.io.IOException;
import java.util.ArrayList;

/**
 * Variant of {@link Content} for entries that reference external content.
 *
 * 
 */
public class OutOfLineContent extends Content {

  /** @return the type of this content */
  @Override
  public int getType() {
    return Content.Type.MEDIA;
  }

  /** MIME Content type. */
  protected ContentType mimeType;
  /** @return the MIME content type */
  public ContentType getMimeType() { return mimeType; }
  /** Specifies the MIME Content type. */
  public void setMimeType(ContentType v) { mimeType = v; }

  /** @return  always null, since language is undefined for external content. */
  @Override
  public String getLang() { return null; }

  /**
   * External URI.
   */
  protected String uri;
  /** @return  the external URI */
  public String getUri() { return uri; }
  /** Specifies the external URI. */
  public void setUri(String v) { uri = v; }

  /**
   * Content length.  Value will be -1 if unknown.
   */
  protected long length;
  /** @return the content length. */
  public long getLength() { return length; }
  public void setLength(long v) { length = v; }

  /**
   * Generates XML in the Atom format.
   *
   * @param   w
   *            output writer
   * @param   extProfile
   *            Extension Profile for nested extensions
   *
   * @throws  IOException
   */
  @Override
  public void generateAtom(XmlWriter w, ExtensionProfile extProfile)
      throws IOException {

    ArrayList<XmlWriter.Attribute> attrs =
      new ArrayList<XmlWriter.Attribute>(2);

    if (mimeType != null) {
      attrs.add(new XmlWriter.Attribute("type", mimeType.getMediaType()));
    }

    if (uri != null) {
      attrs.add(new XmlWriter.Attribute("src", uri));
    }


    w.simpleElement(Namespaces.atomNs, "content", attrs, null);
  }

  /**
   * Generates XML in the RSS format.
   *
   * @param   w
   *            output writer
   * @param   extProfile
   *            Extension Profile for nested extensions
   *
   * @throws  IOException
   */
  @Override
  public void generateRss(XmlWriter w, ExtensionProfile extProfile)
      throws IOException {

    ArrayList<XmlWriter.Attribute> attrs =
      new ArrayList<XmlWriter.Attribute>(3);

    if (mimeType != null) {
      attrs.add(new XmlWriter.Attribute("type", mimeType.getMediaType()));
    }

    if (uri != null) {
      attrs.add(new XmlWriter.Attribute("url", uri));
    }

    if (length != -1) {
      attrs.add(new XmlWriter.Attribute("length", Long.toString(length)));
    }

    w.simpleElement(Namespaces.rssNs, "enclosure", attrs, null);
  }

  /** Parses XML in the Atom format. */
  public class AtomHandler extends XmlParser.ElementHandler {

    /**
     * Processes attributes.
     *
     * @throws   ParseException
     */
    @Override
    public void processAttribute(String namespace,
                                 String localName,
                                 String value)
        throws ParseException {

      if (namespace.equals("")) {
        if (localName.equals("type")) {
          try {
            mimeType = new ContentType(value);
          } catch (IllegalArgumentException e) {
            throw new ParseException(
                CoreErrorDomain.ERR.invalidMimeType, e);
          }
        } else if (localName.equals("src")) {
          uri = getAbsoluteUri(value);
        }
      }
    }

    @Override
    public void processEndElement() throws ParseException {
      if (uri == null) {
        throw new ParseException(
            CoreErrorDomain.ERR.missingSrcAttribute);
      }

      // Validate that external content element is empty.
      super.processEndElement();
    }
  }
}
