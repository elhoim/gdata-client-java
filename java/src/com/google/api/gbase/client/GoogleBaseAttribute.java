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

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * An internal representation for a tag in the g: namespace.
 */
public class GoogleBaseAttribute {

  /** Attribute name (with spaces) and type. */
  private final GoogleBaseAttributeId attributeId;

  /** Content of the tag, as text or null for complex tags. */
  private String textValue;

  /** Tag sub-elements (element name, element value) if there are any. */
  private Map<String, String> subElements;

  /** Corresponds to the XML attribute {@code access="private"}. */
  private boolean privateAccess;

  /**
   * Creates a new GoogleBaseAttribute with a name and no type.
   *
   * @param name
   */
  public GoogleBaseAttribute(String name) {
    this(new GoogleBaseAttributeId(name, null), null);
  }

  /**
   * Creates a new GoogleBaseAttribute with a name and type.
   *
   * @param name
   * @param type attribute type, one of the TYPE_* defined in this class,
   *   or null
   */
  public GoogleBaseAttribute(String name, GoogleBaseAttributeType type) {
    this(name, type, null);
  }

  /**
   * Creates a new Extension attribute with a name, type and string content.
   *
   * @param name
   * @param type type attribute type, one of the types defined in 
   *   {@link GoogleBaseAttributeType}, some new type or null
   * @param textValue content of the attribute, as a string
   * @exception NullPointerException if argument name is null
   */
  public GoogleBaseAttribute(String name,
                             GoogleBaseAttributeType type,
                             String textValue) {
    this(new GoogleBaseAttributeId(name, type), textValue);
  }

  /**
   * Creates a new GoogleBaseAttribute with a name and type.
   *
   * @param name
   * @param type attribute type, one of the TYPE_* defined in this class,
   *   or null
   * @param privateAccess if this attributeId is private
   */
  public GoogleBaseAttribute(String name, GoogleBaseAttributeType type,
                             boolean privateAccess) {
    this(name, type, privateAccess, null);
  }

  /**
   * Creates a new Extension attribute with a name, type and string content.
   *
   * @param name
   * @param type type attribute type, one of the types defined in
   *   {@link GoogleBaseAttributeType}, some new type or null
   * @param privateAccess if this attribute is private
   * @param textValue content of the attribute, as a string
   * @exception NullPointerException if argument name is null
   */
  public GoogleBaseAttribute(String name,
                             GoogleBaseAttributeType type,
                             boolean privateAccess,
                             String textValue) {
    this(new GoogleBaseAttributeId(name, type), textValue);
    setPrivate(privateAccess);
  }

  /**
   * Creates a new Extension attribute with an {@link GoogleBaseAttributeId} and 
   * string content.
   *
   * @param attributeId attribute name and type
   * @param textValue content of the attribute, as a string
   * @exception NullPointerException if argument name is null
   */
  public GoogleBaseAttribute(GoogleBaseAttributeId attributeId, String textValue) {
    this.attributeId = attributeId;
    this.textValue = textValue;
  }

  /**
   * Generates a string representation of this attribute.
   *
   * The output of this method is in no particular format.
   * @return a string representation
   */
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("[");
    buf.append(attributeId);
    buf.append(": ");
    if (hasValue()) {
      buf.append(textValue);
    }
    if (hasSubElements()) {
      buf.append(subElements.toString());
    }
    buf.append("]");
    return buf.toString();
  }

  /** Gets the attribute name. */
  public String getName() {
    return attributeId.getName();
  }

  /** Gets the type of the attribute, or null. */
  public GoogleBaseAttributeType getType() {
    return attributeId.getType();
  }

  /**
   * Returns the attribute identity, name and type.
   * 
   * @return attribute name and type
   */
  public GoogleBaseAttributeId getAttributeId() {
    return attributeId;
  }

  /** Gets the attribute value, as a string (or null). */
  public String getValueAsString() {
    return textValue;
  }

  /**
   * Checks whether the attribute should only be shown to the owner of
   * this item.
   */
  public boolean isPrivate() {
    return privateAccess;
  }

  /**
   * Declares the attribute as being private or public (the default).
   *
   * @param privateAccess
   */
  public void setPrivate(boolean privateAccess) {
    this.privateAccess = privateAccess;
  }

  /**
   * Sets the attribute value, as a string.
   *
   * @param value attribute value
   */
  public void setValue(String value) {
    this.textValue = value;
  }

  /** Returns true if the attribute has some text content. */
  public boolean hasValue() {
    return textValue != null;
  }

  /** Returns true if the attribute has sub-tags. */
  public boolean hasSubElements() {
    return subElements != null && !subElements.isEmpty();
  }

  /**
   * Sets the value of a sub-tag.
   *
   * @param name tag name
   * @param value tag content, as a string or null to remove
   *   the sub-element
   */
  public void setSubElement(String name, String value) {
    if (value == null) {
      removeSubElement(name);
    } else {
      if (subElements == null) {
        subElements = new HashMap<String, String>();
      }
      subElements.put(name, value);
    }
  }

  /**
   * Gets the value of a sub-tag.
   *
   * @param name
   * @return sub-tag text content or null
   */
  public String getSubElementValue(String name) {
    if (subElements == null) {
      return null;
    }
    return subElements.get(name);
  }

  /**
   * Checks whether a specific sub-element exists.
   *
   * @param name element name
   * @return true if the sub-element exists
   */
  public boolean hasSubElement(String name) {
    if (subElements == null) {
      return false;
    }
    return subElements.containsKey(name);
  }

  /**
   * Removes a sub-element.
   *
   * @param name element names
   */
  public void removeSubElement(String name) {
    if (subElements != null) {
      subElements.remove(name);
    }
  }

  /**
   * Gets the name of all sub-elements in this attribute.
   *
   * @return a collection of element names, which might
   *   be empty but not null
   */
  public Collection<? extends String> getSubElementNames() {
    if (subElements == null) {
      return Collections.emptyList();
    }
    return subElements.keySet();
  }

  @Override
  public int hashCode() {
    int retval = 27 + attributeId.hashCode();
    if (textValue != null) {
      retval += 49 * textValue.hashCode();
    }
    if (subElements != null) {
      retval += subElements.hashCode();
    }
    return retval;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof GoogleBaseAttribute)) {
      return false;
    }

    GoogleBaseAttribute other = (GoogleBaseAttribute)o;
    return attributeId.equals(other.attributeId) &&
        privateAccess == other.privateAccess &&
        equalsMaybeNull(textValue, other.textValue) &&
        equalsMaybeNull(subElements, other.subElements);
  }
  private static boolean equalsMaybeNull(Object a, Object b) {
    if (a == null) {
      return b == null;
    }
    return a.equals(b);
  }
}
