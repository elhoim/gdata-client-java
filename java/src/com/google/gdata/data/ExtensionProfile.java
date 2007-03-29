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

import com.google.gdata.util.common.base.Pair;
import com.google.gdata.util.common.xml.XmlWriter;
import com.google.gdata.util.common.xml.XmlWriter.Attribute;
import com.google.gdata.util.Namespaces;
import com.google.gdata.util.ParseException;
import com.google.gdata.util.ServiceConfigurationException;
import com.google.gdata.util.XmlParser;

import org.xml.sax.Attributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.xml.sax.Attributes;

/**
 * Specifies a complete extension profile for an extended GData schema.
 * A profile is a set of allowed extensions for each type together with
 * additional properties.
 * <p>
 * For example, Calendar might allow {@code <gd:who>} within {@code
 * <atom:feed>}, and {@code <gd:when>}, {@code <gd:who>}, and {@code
 * <gd:where>} within {@code <atom:entry>}.
 *
 * 
 * 
 */
public class ExtensionProfile {

  /** Set of previously declared Kind.Adaptor classes. */
  private HashSet<Class<Kind.Adaptor>> declared =
      new HashSet<Class<Kind.Adaptor>>();

  /**
   * Adds the extension declarations associated with an {@link Kind.Adaptor}
   * instance, if the declaring class has not already added to this
   * profile.  The method is optimized to reduce the overhead of declaring
   * the same adaptor type multiple times within the same profile.
   */
  public void addDeclarations(Kind.Adaptor adaptor) {
    Class adaptorClass = adaptor.getClass();
    if (!declared.contains(adaptorClass)) {
      adaptor.declareExtensions(this);
      declared.add(adaptorClass);
    }
  }

  /**
   * Specifies that type {@code extendedType} can contain an extension
   * described by {@code extDescription}.
   */
  public synchronized void declare(Class<? extends ExtensionPoint> extendedType,
                                   ExtensionDescription extDescription) {

    ExtensionPoint.Manifest manifest = getOrCreateManifest(extendedType);

    Pair<String, String> extensionQName =
      new Pair(extDescription.getNamespace().getUri(),
               extDescription.getLocalName());

    manifest.supportedExtensions.put(extensionQName, extDescription);

    profile.put(extendedType, manifest);

    nsDecls = null;
  }


  /**
   * Declares that {@code extDesc} defines a feed extension.
   */
  public synchronized void declareFeedExtension(ExtensionDescription extDesc) {
    declare(BaseFeed.class, extDesc);
  }

  /**
   * Declares that {@code extClass} defines an entry extension.
   */
  public synchronized void declareFeedExtension(
      Class<? extends Extension> extClass) {
    declare(BaseFeed.class,
        ExtensionDescription.getDefaultDescription(extClass));
  }

  /**
   * Declares that {@code extDesc} defines an entry extension.
   */
  public synchronized void declareEntryExtension(ExtensionDescription extDesc) {
    declare(BaseEntry.class, extDesc);
  }

  /**
   * Declares that {@code extClass} defines an entry extension.
   */
  public synchronized void declareEntryExtension(
      Class<? extends Extension> extClass) {
    declare(BaseEntry.class,
        ExtensionDescription.getDefaultDescription(extClass));
  }

  /** Specifies that type {@code extendedType} can contain arbitrary XML. */
  public synchronized void declareArbitraryXmlExtension(
      Class extendedType) {

    ExtensionPoint.Manifest manifest = getOrCreateManifest(extendedType);
    manifest.arbitraryXml = true;
    profile.put(extendedType, manifest);

    nsDecls = null;
  }


  /** Specifies additional top-level namespace declarations. */
  public synchronized void declareAdditionalNamespace(XmlWriter.Namespace ns) {
    additionalNamespaces.add(ns);
  }


  /** Specifies the type of feeds nested within {@code <gd:feedLink>}. */
  public synchronized void declareFeedLinkProfile(ExtensionProfile profile) {
    feedLinkProfile = profile;
    nsDecls = null;
  }


  /** Retrieves the type of feeds nested within {@code <gd:feedLink>}. */
  public synchronized ExtensionProfile getFeedLinkProfile() {
    return feedLinkProfile;
  }


  /** Specifies the type of entries nested within {@code <gd:entryLink>}. */
  public synchronized void declareEntryLinkProfile(ExtensionProfile profile) {
    entryLinkProfile = profile;
    nsDecls = null;
  }


  /** Retrieves the type of entries nested within {@code <gd:entryLink>}. */
  public synchronized ExtensionProfile getEntryLinkProfile() {
    return entryLinkProfile;
  }


  /**
   * Retrieves an extension manifest for a specific class (or one of
   * its superclasses) or {@code null} if not specified.
   */
  public ExtensionPoint.Manifest getManifest(Class extendedType) {
    ExtensionPoint.Manifest manifest = null;
    while (extendedType != null) {
      manifest = profile.get(extendedType);
      if (manifest != null)
        return manifest;
      extendedType = extendedType.getSuperclass();
    }
    return null;
  }


  /** Retrieves a collection of all namespaces used by this profile. */
  public synchronized Collection<XmlWriter.Namespace> getNamespaceDecls() {

    if (nsDecls == null) {
      nsDecls = computeNamespaceDecls();
    }

    return nsDecls;
  }


  /** Internal storage for the profile. */
  private final Map<Class, ExtensionPoint.Manifest> profile =
    new HashMap<Class, ExtensionPoint.Manifest>();


  /** Additional namespaces. */
  private Collection<XmlWriter.Namespace> additionalNamespaces =
    new LinkedHashSet<XmlWriter.Namespace>();


  /** Nested feed link profile. */
  private ExtensionProfile feedLinkProfile;


  /** Nested entry link profile. */
  private ExtensionProfile entryLinkProfile;


  /** Namespace declarations cache. */
  private Collection<XmlWriter.Namespace> nsDecls = null;


  /** Profile supports auto-extension declaration */
  private boolean isAutoExtending = false;

  public void setAutoExtending(boolean v) { isAutoExtending = v; }
  public boolean isAutoExtending() { return isAutoExtending; }

  /** Internal helper routine. */
  private ExtensionPoint.Manifest getOrCreateManifest(Class extendedType) {
    ExtensionPoint.Manifest manifest = getManifest(extendedType);
    if (manifest != null) {
      return manifest;
    } else {
      return new ExtensionPoint.Manifest();
    }
  }


  private synchronized Collection<XmlWriter.Namespace> computeNamespaceDecls() {

    HashSet<XmlWriter.Namespace> result = new HashSet<XmlWriter.Namespace>();

    result.addAll(additionalNamespaces);

    for (ExtensionPoint.Manifest manifest: profile.values()) {
      result.addAll(manifest.getNamespaceDecls());
    }

    if (feedLinkProfile != null) {
      result.addAll(feedLinkProfile.computeNamespaceDecls());
    }

    if (entryLinkProfile != null) {
      result.addAll(entryLinkProfile.computeNamespaceDecls());
    }

    return Collections.unmodifiableSet(result);
  }

  /**
   * Reads the ExtensionProfile XML format.
   */
  public class Handler extends XmlParser.ElementHandler {

    private ExtensionProfile configProfile;
    private ClassLoader configLoader;
    private List<XmlWriter.Namespace> namespaces =
              new ArrayList<XmlWriter.Namespace>();

    public Handler(ExtensionProfile configProfile, ClassLoader configLoader)
        throws IOException {
      this.configProfile = configProfile;
      this.configLoader = configLoader;
    }


    public void validate() throws ServiceConfigurationException {
    }


    public void processEndElement() throws ParseException {
      try {
        validate();
      } catch (ServiceConfigurationException e) {
        throw new ParseException(e);
      }
    }


    public XmlParser.ElementHandler getChildHandler(String namespace,
                                                    String localName,
                                                    Attributes attrs)
        throws ParseException, IOException {

      if (namespace.equals(Namespaces.gdataConfig)) {

        if (localName.equals("namespaceDescription")) {

          String alias = attrs.getValue("", "alias");
          if (alias == null) {
            throw new ParseException(
                      "NamespaceDescription alias attribute is missing");
          }
          String uri = attrs.getValue("", "uri");
          if (uri == null) {
            throw new ParseException(
                      "NamespaceDescription uri attribute is missing");
          }

          XmlWriter.Namespace declaredNs = new XmlWriter.Namespace(alias, uri);
          namespaces.add(declaredNs);
          declareAdditionalNamespace(declaredNs);
          return new XmlParser.ElementHandler();

        } else if (localName.equals("extensionPoint")) {

          return new ExtensionPointHandler(configProfile, configLoader,
                                           namespaces, attrs);
        }
      }

      return super.getChildHandler(namespace, localName, attrs);
    }
  }

  /**
   * Reads the ExtensionPoint XML format
   */
  public class ExtensionPointHandler extends XmlParser.ElementHandler {

    private ExtensionProfile configProfile;
    private ClassLoader configLoader;

    private Class extensionPoint;
    private boolean arbitraryXml;
    private List<ExtensionDescription> extDescriptions =
      new ArrayList<ExtensionDescription>();
    private List<XmlWriter.Namespace> namespaces;

    public ExtensionPointHandler(ExtensionProfile configProfile,
                                 ClassLoader configLoader,
                                 List<XmlWriter.Namespace> namespaces,
                                 Attributes attrs)
        throws ParseException, IOException {

      this.configProfile = configProfile;
      this.configLoader = configLoader;
      this.namespaces = namespaces;

      String extendedClassName = attrs.getValue("", "extendedClass");
      if (extendedClassName == null) {
        throw new ParseException(
                    "ExtensionPoint extendedClass attribute is missing");
      }

      try {
        extensionPoint = configLoader.loadClass(extendedClassName);
      } catch (ClassNotFoundException e) {
        throw new ParseException("Unable to load ExtensionPoint class", e);
      }
      if (!ExtensionPoint.class.isAssignableFrom(extensionPoint)) {
        throw new ParseException(
                    "Extended classes must extend ExtensionPoint");
      }

      String arbitraryXmlAttr = attrs.getValue("", "arbitraryXml");
      if (arbitraryXmlAttr != null) {
        if (arbitraryXmlAttr.equals("true") || arbitraryXmlAttr.equals("1")) {
          arbitraryXml = true;
        } else if (arbitraryXmlAttr.equals("false") ||
                   arbitraryXmlAttr.equals("0")) {
          arbitraryXml = false;
        } else {
          throw new ParseException("Invalid value for arbitaryXml: " +
                                   arbitraryXml);
        }
      }
    }


    public void processEndElement() throws ParseException {

      if (arbitraryXml) {
        declareArbitraryXmlExtension(extensionPoint);
      }

      for (ExtensionDescription extDescription: extDescriptions) {
        declare(extensionPoint, extDescription);
      }
    }


    public XmlParser.ElementHandler getChildHandler(String namespace,
                                                    String localName,
                                                    Attributes attrs)
        throws ParseException, IOException {

      if (namespace.equals(Namespaces.gdataConfig)) {
        if (localName.equals("extensionDescription")) {

          ExtensionDescription extDescription = new ExtensionDescription();
          extDescriptions.add(extDescription);
          return extDescription.new Handler(configProfile, configLoader,
                                            namespaces, attrs);
        }
      }

      return super.getChildHandler(namespace, localName, attrs);
    }
  }


  /**
   * Parses XML in the ExtensionProfile format.
   *
   * @param   configProfile
   *            Extension profile description configuration extensions.
   *
   * @param   classLoader
   *            ClassLoader to load extension classes
   *
   * @param   stream
   *            InputStream from which to read the description
   */
  public void parseConfig(ExtensionProfile configProfile,
                          ClassLoader classLoader,
                          InputStream stream) throws IOException,
                                                   ParseException {

    Handler handler = new Handler(configProfile, classLoader);
    new XmlParser().parse(stream, handler, Namespaces.gdataConfig,
                          "extensionProfile");
  }

  /**
   * Generates XML in the external config format.
   *
   * @param   w
   *          Output writer.
   *
   * @param   extProfile
   *          Extension profile.
   *
   * @throws  IOException
   */
  public void generateConfig(XmlWriter w,
                             ExtensionProfile extProfile) throws IOException {

    w.startElement(Namespaces.gdataConfigNs, "extensionProfile", null, nsDecls);

    for (XmlWriter.Namespace namespace : additionalNamespaces) {

      List<Attribute> nsAttrs = new ArrayList<Attribute>();
      nsAttrs.add(new Attribute("alias", namespace.getAlias()));
      nsAttrs.add(new Attribute("uri", namespace.getUri()));
      w.simpleElement(Namespaces.gdataConfigNs, "namespaceDescription",
                      nsAttrs, null);
    }

    //
    // Get a list of the extended classes sorted by class name
    //
    TreeSet<Class> extensionSet = new TreeSet<Class>( 
        new Comparator<Class>() {
          public int compare(Class c1, Class c2) {
            return c1.getName().compareTo(c2.getName());
          }
          public boolean equals(Comparator c) {
            return this.getClass().equals(c.getClass());
          }
        });
    for (Class extensionPoint : profile.keySet()) {
      extensionSet.add(extensionPoint);
    }

    for (Class extensionPoint : extensionSet) {

      ExtensionPoint.Manifest  manifest = profile.get(extensionPoint);

      List<Attribute> ptAttrs = new ArrayList<Attribute>();
      ptAttrs.add(new Attribute("extendedClass", extensionPoint.getName()));
      ptAttrs.add(new Attribute("arbitraryXml", manifest.arbitraryXml));
      w.startElement(Namespaces.gdataConfigNs, "extensionPoint", ptAttrs, null);

      // Create an ordered list of the descriptions in this profile
      TreeSet<ExtensionDescription> descSet = 
        new TreeSet<ExtensionDescription>();

      for (ExtensionDescription extDescription : 
           manifest.getSupportedExtensions().values()) {
        descSet.add(extDescription);
      }

      // Now output based upon the ordered list
      for (ExtensionDescription extDescription : descSet) {
        extDescription.generateConfig(w, extProfile);
      }

      w.endElement(Namespaces.gdataConfigNs, "extensionPoint");
    }

    w.endElement(Namespaces.gdataConfigNs, "extensionProfile");
  }
}
