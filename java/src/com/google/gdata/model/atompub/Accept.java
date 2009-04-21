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


package com.google.gdata.model.atompub;

import com.google.gdata.model.DefaultRegistry;
import com.google.gdata.model.Element;
import com.google.gdata.model.ElementCreator;
import com.google.gdata.model.ElementKey;
import com.google.gdata.model.ElementMetadata;
import com.google.gdata.model.QName;
import com.google.gdata.util.Namespaces;

/**
 * Specifies a type of representation that can be POSTed to a Collection.
 *
 * 
 */
public class Accept extends Element {

  /**
   * The key for this element.
   */
  public static final ElementKey<String,
      Accept> KEY = ElementKey.of(new QName(Namespaces.atomPubStandardNs,
      "accept"), String.class, Accept.class);

  /*
   * Generate the default metadata for this element.
   */
  static {
    ElementCreator builder =
        DefaultRegistry.build(KEY).setContentRequired(false);
  }

  /**
   * Default mutable constructor.
   */
  public Accept() {
    this(DefaultRegistry.get(KEY));
  }

  /**
   * Lets subclasses create an instance using custom metadata.
   */
  protected Accept(ElementMetadata<String, ? extends Accept> metadata) {
    super(metadata);
  }

  /**
   * Constructs a new instance by doing a shallow copy of data from an existing
   * {@link Element} instance. Will use the given {@link ElementMetadata} as the
   * metadata for the element.
   *
   * @param metadata metadata to use for this element.
   * @param source source element
   */
  public Accept(ElementMetadata<String, ? extends Accept> metadata,
      Element source) {
    super(metadata, source);
  }

  /**
   * Immutable constructor.
   *
   * @param value value.
   */
  public Accept(String value) {
    this();
    setValue(value);
    setImmutable(true);
  }

  /**
   * Returns the value.
   *
   * @return value
   */
  public String getValue() {
    return super.getTextValue(KEY);
  }

  /**
   * Sets the value.
   *
   * @param value value or <code>null</code> to reset
   */
  public void setValue(String value) {
    super.setTextValue(value);
  }

  /**
   * Returns whether it has the value.
   *
   * @return whether it has the value
   */
  public boolean hasValue() {
    return super.hasTextValue();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!sameClassAs(obj)) {
      return false;
    }
    Accept other = (Accept) obj;
    return eq(getValue(), other.getValue());
  }

  @Override
  public int hashCode() {
    int result = getClass().hashCode();
    if (getValue() != null) {
      result = 37 * result + getValue().hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return "{Accept value=" + getTextValue() + "}";
  }

}
