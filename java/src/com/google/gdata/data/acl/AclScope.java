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


package com.google.gdata.data.acl;

import com.google.gdata.data.AbstractExtension;
import com.google.gdata.data.AttributeGenerator;
import com.google.gdata.data.AttributeHelper;
import com.google.gdata.data.ExtensionDescription;
import com.google.gdata.util.ParseException;

/**
 * Describes the scope of an entry in an access control list.
 *
 * 
 */
@ExtensionDescription.Default(
    nsAlias = AclNamespace.gAclAlias,
    nsUri = AclNamespace.gAcl,
    localName = AclScope.SCOPE,
    isRequired = true)
public class AclScope extends AbstractExtension {

  /** XML "scope" element name */
  static final String SCOPE = "scope";

  /** XML "type" attribute name */
  private static final String TYPE = "type";

  /** XML "value" attribute name */
  private static final String VALUE = "value";

  /** predefined values for the "type" attribute */
  public enum Type {
    USER,
    DOMAIN,
    DEFAULT;
  }

  public AclScope() {
    super();
  }

  public AclScope(AclScope.Type type, String value) {
    super();
    setType(type);
    setValue(value);
    setImmutable(true);
  }

  /** type */
  private AclScope.Type type = null;
  public Type getType() { return type; }
  public void setType(AclScope.Type type) {
    throwExceptionIfImmutable();
    this.type = type;
  }

  /** value */
  private String value = null;
  public String getValue() { return value; }
  public void setValue(String value) {
    throwExceptionIfImmutable();
    this.value = value;
  }

  @Override
  protected void validate() {
    if (type == null) {
      throwExceptionForMissingAttribute(TYPE);
    }
    if (type == AclScope.Type.DEFAULT) {
      if (value != null) {
        throw new IllegalStateException(
            "attribute " + VALUE + " should not be set for default type");
      }
    } else if (value == null) {
      throwExceptionForMissingAttribute(VALUE);
    }
  }

  @Override
  public void putAttributes(AttributeGenerator generator) {
    generator.put(TYPE, type,
        new AttributeHelper.LowerCaseEnumToAttributeValue<AclScope.Type>());
    generator.put(VALUE, value);
  }

  @Override
  protected void consumeAttributes(AttributeHelper helper)
      throws ParseException {
    type = helper.consumeEnum(TYPE, true, AclScope.Type.class, null, 
        new AttributeHelper.LowerCaseEnumToAttributeValue<AclScope.Type>());
    value = helper.consume(VALUE, false);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!sameClassAs(o)) {
      return false;
    }
    AclScope vc = (AclScope)o;
    return eq(value, vc.value) && eq(type, vc.type);
  }

  @Override
  public int hashCode() {
    int result = getClass().hashCode();
    if (value != null) {
      result = 37 * result + value.hashCode();
    }
    if (type != null) {
      result = 37 * result + type.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return "[AclScope type=" + type + " value=" + value + "]";
  }
}