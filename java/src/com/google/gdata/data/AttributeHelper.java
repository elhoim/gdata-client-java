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


package com.google.gdata.data;

import com.google.gdata.util.ParseException;

import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Helps accessing tag attributes.
 *
 * The helper only checks attributes in the default namespace ("") and
 * rejects unknown attributes.
 *
 * The idea is to remove (consume) attributes as they are read
 * from the list and at the end make sure that all attributes have
 * been read, to detect whether unknown attributes have been
 * specified. This is done by the method {@link #assertAllConsumed()} usually
 * called from
 * {@link com.google.gdata.util.XmlParser.ElementHandler#processEndElement()}.
 *
 * 
 */
public class AttributeHelper {

  /** Maps attribute local name to string value. */
  protected final Map<String, String> attrs = new HashMap<String, String>();

  /** set of attributes that are duplicated */
  private Set<String> dups = new HashSet<String>();

  /** if the content has been consumed */
  private boolean contentConsumed = false;

  /** element's text content or {@code null} for no text content */
  private String content = null;

  /**
   * Creates a helper tied to a specific set of SAX attributes.
   *
   * @param attrs the SAX attributes to be processed
   * @param content element's text content
   */
  public AttributeHelper(Attributes attrs, String content) {
    // text content
    this.content = content == null ? null : content.trim();

    // attributes
    for (int i = 0; i < attrs.getLength(); i++) {
      if (attrs.getURI(i).length() != 0) {
        String attrLocalName = attrs.getLocalName(i);
        if (this.attrs.put(attrLocalName, attrs.getValue(i)) != null) {
          dups.add(attrLocalName);
        }
      } else {
        this.attrs.put(attrs.getQName(i), attrs.getValue(i));
      }
    }
  }

  /**
   * Gets the element's text content and removes it from the list.
   *
   * @return element's text content or {@code null} for no text content
   * @exception ParseException if required is set and the text content
   *   is not defined
   */
  public String consumeContent(boolean required) throws ParseException {
    if (content == null && required) {
      throw new ParseException("Missing required text content");
    }
    contentConsumed = true;
    return content;
  }

  /**
   * Creates a helper tied to a specific set of SAX attributes.
   *
   * @param attrs the SAX attributes to be processed
   */
  public AttributeHelper(Attributes attrs) {
    this(attrs, null);
  }

  /**
   * Gets the value of an attribute and remove it from the list.
   *
   * @param name attribute name
   * @param required indicates attributes is required
   * @return attribute value or null if not available
   * @exception ParseException if required is set and the attribute
   *   is not defined
   */
  public String consume(String name, boolean required) throws ParseException {
    String value = attrs.get(name);
    if (value == null) {
      if (required) {
        throw new ParseException("Missing attribute: '" + name + "'");
      }
      return null;
    }
    attrs.remove(name);
    return value;
  }

  /**
   * Gets the value of an integer attribute and remove it from the list.
   *
   * @param name attribute name
   * @param required indicates attribute is required
   * @param defaultValue the default value for an optional attribute (used
   *        if not present)
   * @return the integer value of this attribute
   * @exception ParseException if required is set and the attribute
   *   is not defined, or if the attribute value is not a valid integer
   */
  public int consumeInteger(String name, boolean required, int defaultValue)
      throws ParseException {
    String value = consume(name, required);
    if (value == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new ParseException("Invalid integer value for attribute: '" +
          name + "'");
    }
  }

  /**
   * Gets the value of an integer attribute and remove it from the list.
   *
   * @param name attribute name
   * @param required indicates attribute is required
   * @return the integer value of this attribute, 0 by default
   * @exception ParseException if required is set and the attribute
   *   is not defined, or if the attribute value is not a valid integer
   */
  public int consumeInteger(String name, boolean required)
      throws ParseException {
    return consumeInteger(name, required, 0);
  }

  /**
   * Gets the value of a long attribute and remove it from the list.
   *
   * @param name attribute name
   * @param required indicates attribute is required
   * @param defaultValue the default value for an optional attribute (used
   *        if not present)
   * @return the long value of this attribute
   * @exception ParseException if required is set and the attribute
   *   is not defined, or if the attribute value is not a valid long
   */
  public long consumeLong(String name, boolean required, long defaultValue)
      throws ParseException {
    String value = consume(name, required);
    if (value == null) {
      return defaultValue;
    }
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      throw new ParseException("Invalid long value for attribute: '" +
          name + "'", e);
    }
  }

  /**
   * Gets the value of a long attribute and remove it from the list.
   *
   * @param name attribute name
   * @param required indicates attribute is required
   * @return the long value of this attribute, 0 by default
   * @exception ParseException if required is set and the attribute
   *   is not defined, or if the attribute value is not a valid long
   */

  public long consumeLong(String name, boolean required)
      throws ParseException {
    return consumeLong(name, required, 0);
  }

  /**
   * Gets the value of a boolean attribute and remove it from the list. The
   * accepted values are based upon xsd:boolean syntax (true, false, 1, 0).
   *
   * @param name attribute name
   * @param required indicates attribute is required
   * @param defaultValue the default value for an optional attribute (used
   *        if not present)
   * @return the boolean value of this attribute
   * @exception ParseException if required is set and the attribute
   *   is not defined, or if the attribute value is neither {@code true}
   *   nor {@code false}.
   */
  public boolean consumeBoolean(String name, boolean required,
                                boolean defaultValue)
      throws ParseException {
    String value = consume(name, required);
    if (value == null) {
      return defaultValue;
    }
    if ("true".equals(value) || "1".equals(value)) {
      return true;
    } else if ("false".equals(value) || "0".equals(value)) {
      return false;
    } else {
      throw new ParseException("Invalid boolean value for attribute: '" +
          name + "'");
    }

  }

  /**
   * Gets the value of a boolean attribute and remove it from the list. The
   * accepted values are based upon xsd:boolean syntax (true, false, 1, 0).
   *
   * @param name attribute name
   * @param required indicates attribute is required
   * @return the boolean value of this attribute, false by default
   * @exception ParseException if required is set and the attribute
   *   is not defined, or if the attribute value is neither {@code true}
   *   nor {@code false}.
   */
  public boolean consumeBoolean(String name, boolean required)
      throws ParseException {
    return consumeBoolean(name, required, false);
  }

  /**
   * Defines a custom mapping of an enum value to an attribute value (similar to
   * a closure).
   */
  public static interface EnumToAttributeValue<T extends Enum<T>> {
    String getAttributeValue(T enumValue);
  }

  /**
   * Implements the most common custom mapping of an enum value to an attribute
   * value using the lower-case form of the enum name.
   */
  public static class LowerCaseEnumToAttributeValue<T extends Enum<T>>
      implements EnumToAttributeValue<T> {

    public String getAttributeValue(T enumValue) {
      return enumValue.name().toLowerCase();
    }
  }

  /**
   * Gets the value of an enumerated attribute and remove it from the list,
   * using a custom mapping of enum to attribute value.
   *
   * @param name                 attribute name
   * @param required             indicates attribute is required
   * @param enumClass            enumeration class
   * @param defaultValue         the default value for an optional attribute
   *                             (used if not present)
   * @param enumToAttributeValue custom mapping of enum to attribute value
   * @return an enumerated value
   * @throws ParseException if required is set and the attribute is not defined,
   *                        or if the attribute value is not a valid enumerated
   *                        value
   */
  public <T extends Enum<T>> T consumeEnum(String name, boolean required,
      Class<T> enumClass, T defaultValue,
      EnumToAttributeValue<T> enumToAttributeValue)
      throws ParseException {
    String value = consume(name, required);
    if (value == null) {
      return defaultValue;
    }
    for (T enumValue : enumClass.getEnumConstants()) {
      if (enumToAttributeValue.getAttributeValue(enumValue).equals(value)) {
        return enumValue;
      }
    }
    throw new ParseException("Invalid value for attribute : '" + name + "'");
  }

  /**
   * Gets the value of an enumerated attribute and remove it from the list.
   *
   * Enumerated values are case-insensitive.
   *
   * @param name attribute name
   * @param required indicates attribute is required
   * @param enumClass enumeration class
   * @param defaultValue the default value for an optional attribute (used
   *        if not present)
   * @return an enumerated value
   * @exception ParseException if required is set and the attribute
   *   is not defined, or if the attribute value is not a valid
   *   enumerated value
   */
  public <T extends Enum<T>> T consumeEnum(String name, boolean required,
      Class<T> enumClass, T defaultValue)
      throws ParseException {
    String value = consume(name, required);
    if (value == null) {
      return defaultValue;
    }

    try {
      return Enum.valueOf(enumClass, value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new ParseException("Invalid value for attribute : '" + name + "'",
          e);
    }
  }

  /**
   * Gets the value of an enumerated attribute and remove it from the list.
   *
   * Enumerated values are case-insensitive.
   *
   * @param name attribute name
   * @param required indicates attribute is required
   * @param enumClass enumeration class
   * @return an enumerated value or null if not present
   * @exception ParseException if required is set and the attribute
   *   is not defined, or if the attribute value is not a valid
   *   enumerated value
   */
  public <T extends Enum<T>> T consumeEnum(String name, boolean required,
      Class<T> enumClass)
      throws ParseException {
    return consumeEnum(name, required, enumClass, null);
  }

  /**
   * Makes sure all attributes have been removed from the list.
   *
   * To all attribute in the default namespace must correspond exactly
   * one call to consume*().
   *
   * @exception ParseException if an attribute in the default namespace 
   *    hasn't been removed
   */
  public void assertAllConsumed() throws ParseException {
    StringBuffer message = new StringBuffer();
    if (!attrs.isEmpty()) {
      message.append("Unknown attribute");
      if (attrs.size() > 1) {
        message.append('s');
      }
      message.append(':');
      for (String name : attrs.keySet()) {
        message.append(" '");
        message.append(name);
        message.append("' ");
      }
    }
    if (!dups.isEmpty()) {
      message.append("Duplicate attribute");
      if (dups.size() > 1) {
        message.append('s');
      }
      message.append(':');
      for(String dup: dups) {
        message.append(" '");
        message.append(dup);
        message.append("' ");
      }
    }
    if (!contentConsumed && content != null && content.length() != 0) {
      message.append("Unexpected text content ");
    }
    if (message.length() != 0) {
      throw new ParseException(message.toString());
    }
  }
}
