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


package com.google.gdata.util.common.xml.parsing;

import com.google.gdata.util.common.xml.parsing.SecureGenericXMLFactory.SecureDocumentBuilderFactory;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;

/**
 * A DOM parser factory which returns secure XML parsers based on the JDK's
 * inbuilt Xerces parser.
 *
 * 
 */
public class SecureJDKXercesDOMFactory extends SecureDocumentBuilderFactory {

  /**
   * Constructs the DOM parser factory.
   */
  public SecureJDKXercesDOMFactory() {
    super(new DocumentBuilderFactoryImpl());
  }

  /**
   * Overrides DocumentBuilderFactory.newInstance to ensure secured factory is
   * returned in the case someone calls newInstance() on an existing factory.
   *
   * @returns another secured factory.
   */
  static public SecureJDKXercesDOMFactory newInstance() {
    return new SecureJDKXercesDOMFactory();
  }
}
