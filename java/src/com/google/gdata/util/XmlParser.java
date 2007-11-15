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


package com.google.gdata.util;

import com.google.gdata.util.common.xml.XmlWriter;


import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.ParserAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * XML parser.
 * <p>
 * This is a thin layer on top of a SAX parser. The key concept
 * necessary to understand this parser is <i>Element Handler</i>.
 * Element handlers are type-specific parsers. Each handler instance
 * contains an instance of the Java type corresponding to the XML
 * type it parses. At any given time, one handler is active, and zero
 * or more handlers are kept on the stack. This corresponds directly
 * to the set of currently opened XML tags.
 * <p>
 * To use this parser, one must define an {@link
 * XmlParser.ElementHandler} type (usually one per XML schema type),
 * specify the root element handler, and pass a reader to the 
 * {@link #parse(Reader, com.google.gdata.util.XmlParser.ElementHandler, String,
 *               String)} method.
 * <p>
 *
 * 
 * 
 * @see     XmlParser.ElementHandler
 */
public class XmlParser extends DefaultHandler {


  private static final Logger logger =
    Logger.getLogger(XmlParser.class.getName());


  // The SAXParserFactory used to create underlying SAXParser instances.
  private static SAXParserFactory parserFactory;

  /**
   * Defines a Java system property that can be set to override the
   * default JAXP SAX parser factory mechanism and guarantee that a
   * specific parser will be used.  If set, the value should be the
   * name of the SAXParserFactory implementation that should be used.
   */
  public static final String SAX_PARSER_PROPERTY =
    "com.google.gdata.SAXParserFactory";


  // The JDK system property for JAXP parser configuration.
  private static final String JDK_PARSER_PROPERTY =
    "javax.xml.parsers.SAXParserFactory";


  // This method must be synchronized because it is not otherwise thread
  // safe due to system property manipulation;  this is expensive, but
  // the method will only be used once (or a small number of times)
  // during XmlParser initialization.
  private static synchronized SAXParserFactory getSAXParserFactory()
      throws ParserConfigurationException, SAXException {

    String saxParserFactory = System.getProperty(SAX_PARSER_PROPERTY);
    if (saxParserFactory == null)
      return SAXParserFactory.newInstance();

    // Temporarily override the JDK parser selection system property
    // and create a parser based upon the request implementation.
    // Doing transient setting of a system property is unfortunate,
    // but JAXP provides no other explicit way to influence the
    // factory selection.   This should only happen once (or a very
    // small number of times) around the first usage(s) of XmlParser.
    String origParserFactory = System.getProperty(JDK_PARSER_PROPERTY);
    try {
      System.setProperty(JDK_PARSER_PROPERTY, saxParserFactory);
      return SAXParserFactory.newInstance();

    } finally {
      if (origParserFactory == null) {
        System.clearProperty(JDK_PARSER_PROPERTY);
      }  else {
        System.setProperty(JDK_PARSER_PROPERTY, origParserFactory);
      }
    }
  }

  /**
   * Base class for custom element handlers.
   * <p>
   * To implement a new element handler, one must create a new class
   * extending this class, override {@link #getChildHandler} if nested
   * elements need to be parsed, override {@link #processAttribute} if
   * attributes need to be parsed, and override {@link
   * #processEndElement()} to receive the text() value and post-process
   * the element.
   * <p>
   * If the handler wishes to store unrecognized XML contents in an {@link
   * XmlBlob} value, it must call {@link #initializeXmlBlob} either in the
   * constructor, in parent's {@link #getChildHandler}, or in {@link
   * #processAttribute}. The resulting {@link XmlBlob} value is available
   * following the invocation of {@link #processEndElement()}
   * through the object passed to {@link #initializeXmlBlob}.
   * <p>
   *
   * This class implements overridable methods to support unrecognized XML
   * parsing if desired.
   *
   * 
   */
  public static class ElementHandler {


    /** This element's QName. Used for error reporting. */
    public String qName;


    /** This element's text() value. */
    public String value;

    /** Temporary buffer for building up the text() value. */
    private StringBuffer buffer;

    /**
     * The current state of {@code xml:lang}.
     * See http://www.w3.org/TR/REC-xml/#sec-lang-tag for more information.
     */
    public String xmlLang;


    /**
     * The current state of {@code xml:base}.
     * See http://www.cafeconleche.org/books/xmljava/chapters/ch03s03.html for
     * more information.
     */
    public String xmlBase;


    /** Keeps track of the element stack. */
    ElementHandler parent;


    /**
     * If the handler is parsing unrecognized XML, this object stores the
     * output.
     */
    XmlBlob xmlBlob = null;
    
    
    /**
     * Flag indicating whether it's still OK to call {@link #initializeXmlBlob}.
     */
    boolean okToInitializeXmlBlob = true;


    /** Flag indicating whether mixed content unrecognized XML is allowed. */
    boolean mixedContent = false;


    /**
     * Flag indicating whether unrecognized XML should be processed for
     * full-text indexing. If set, the resulting string ready for indexing is
     * stored in {@link XmlBlob#fullText}. Non-contiguous strings within this
     * index are separated by '\n'.
     */
    boolean fullTextIndex = false;


    /** This element's inner XML writer. Used internally by XmlParser. */
    XmlWriter innerXml;


    /** Namespaces used by this blob. */
    Set<String> blobNamespaces = new HashSet<String>();


    /** String writer underlying {@link #innerXml}. */
    StringWriter innerXmlStringWriter;


    /** String writer underlying the full-text index string. */
    StringWriter fullTextIndexWriter;


    /**
     * Determines a handler for a child element.
     * <p>
     *
     * The default implementation doesn't recognize anything. The result is a
     * schema error <i>unless</i> the parent handler accepts unrecognized XML.
     *
     * @param   namespace
     *            Child element namespace URI.
     *
     * @param   localName
     *            Child element name.
     *
     * @param   attrs
     *            Child element attributes. These attributes will be
     *            communicated to the child element handler through its
     *            {@link #processAttribute} method. They are passed here because
     *            sometimes the value of some attribute determines the element's
     *            content type, so different element handlers may be needed.
     *
     * @return  Child element handler, or {@code null} if the child is
     *          unrecognized.
     *
     * @throws  ParseException
     *            Invalid child element.
     *
     * @throws  IOException
     *            Internal I/O exception (e.g., thrown by XML blob writer).
     */
    public ElementHandler getChildHandler(String namespace,
                                          String localName,
                                          Attributes attrs)
        throws ParseException, IOException {

      if (xmlBlob == null) {
        throw new ParseException("Unrecognized element '" + localName + "'.");
      } else {
        logger.fine("No child handler for " + localName +
                    ". Treating as arbitrary foreign XML.");
        return null;
      }
    }

    /**
     * Called to process an attribute. Designed to be overridden by derived
     * classes.
     *
     * @param   namespace
     *            Attribute namespace URI.
     *
     * @param   localName
     *            Attribute name.
     *
     * @param   value
     *            Attribute value.
     *
     * @throws  ParseException
     *            Invalid attribute.
     */
    public void processAttribute(String namespace,
                                 String localName,
                                 String value) throws ParseException {}


    /**
     * Called to process this element when the closing tag is encountered.
     * The default implementation refuses to accept text() content, unless
     * the handler is configured to accept unrecognized XML with mixed content.
     */
    public void processEndElement() throws ParseException {
      if (value != null && !value.trim().equals("") && !mixedContent) {
        throw new ParseException(
          "This element must not have any text() data.");
      }
    }


    /**
     * If a derived class wishes to retrieve all unrecognized XML in a blob,
     * it calls this method. It must be called in the constructor, in
     * the parent element handler, or in {@link #processAttribute}.
     *
     * @param   xmlBlob
     *            Supplies the XML blob that stores the resulting XML.
     *
     * @param   mixedContent
     *            Specifies that the handler accepts mixed content XML.
     *
     * @param   fullTextIndex
     *            Flag indicating whether unrecognized XML should be processed
     *            for full-text indexing. If set, the resulting string ready for
     *            indexing is stored in {@link XmlBlob#fullText}.
     */
    public void initializeXmlBlob(XmlBlob xmlBlob,
                                  boolean mixedContent,
                                  boolean fullTextIndex) throws IOException {

      assert okToInitializeXmlBlob;

      this.xmlBlob = xmlBlob;
      this.mixedContent = mixedContent;
      this.innerXmlStringWriter = new StringWriter();
      this.innerXml = new XmlWriter(innerXmlStringWriter);
      this.fullTextIndex = fullTextIndex;
      if (fullTextIndex) {
        this.fullTextIndexWriter = new StringWriter();
      }
    }


    /**
     * Utility routine that combines the current state of {@code xml:base}
     * with the specified URI to obtain an absolute URI.
     * <p>
     *
     * See http://www.cafeconleche.org/books/xmljava/chapters/ch03s03.html
     * for more information.
     *
     * @param   uriValue
     *            URI to be interpreted in the context of {@code xml:base}.
     *
     * @return  Corresponding absolute URI.
     *
     * @throws  ParseException
     *            Invalid URI.
     */
    public String getAbsoluteUri(String uriValue) throws ParseException {
      try {
        return getCumulativeXmlBase(xmlBase, uriValue);
      } catch (URISyntaxException e) {
        throw new ParseException(e.getMessage());
      }
    }

    /**
     * Utility method to return the value of an xsd:boolean attribute.
     *
     * @param attrs
     *          Elements attributes to test against.
     *
     * @param attrName
     *          Attribute name.
     *
     * @return the Boolean value if the attribute is present, or
     *         {@code null} otherwise.
     *
     * @throws ParseException if attribute value is not valid xsd:boolean.
     */
    public Boolean getBooleanAttribute(Attributes attrs, String attrName)
        throws ParseException {

      String value = attrs.getValue("", attrName);
      if (value == null) {
        return null;
      }

      if (value.equalsIgnoreCase("false") || value.equals("0")) {
        return Boolean.FALSE;
      }

      if (value.equalsIgnoreCase("true") || value.equals("1")) {
        return Boolean.TRUE;
      }

      throw new ParseException("Invalid value for " + attrName +
                               " attribute: " + value);
    }
  }


  /** Root element handler. */
  protected ElementHandler rootHandler;


  /** Root element namespace URI. */
  protected String rootNamespace;


  /** Root element name. */
  protected String rootElementName;


  /** Top of the element handler stack. */
  ElementHandler curHandler;


  /** Number of unrecognized elements on the stack. */
  int unrecognizedElements = 0;


  /** Document locator used to get line and column numbers for SAX events. */
  Locator locator;


  /**
   * Set of all namespace declarations valid at the current location.
   * Includes namespace declarations from all ancestor elements.
   */
  protected HashMap<String, Stack<XmlWriter.Namespace>> namespaceMap =
    new HashMap<String, Stack<XmlWriter.Namespace>>();


  /**
   * Namespace declarations for the current element.
   * Valid during {@link #startElement}.
   */
  ArrayList<XmlWriter.Namespace> namespaceDecls =
    new ArrayList<XmlWriter.Namespace>();


  /**
   * Parses XML.
   *
   * @param   reader
   *            Supplies the XML to parse.
   *
   * @param   rootHandler
   *            The root element handler corresponding to the expected document
   *            type.
   *
   * @param   rootNamespace
   *            Root element namespace URI.
   *
   * @param   rootElementName
   *            Root element name.
   *
   * @throws  IOException
   *            Thrown by {@code reader}.
   *
   * @throws  ParseException
   *            XML failed to validate against the schema implemented by
   *            {@code rootHandler}.
   */
  public void parse(Reader reader,
                    ElementHandler rootHandler,
                    String rootNamespace,
                    String rootElementName)
      throws IOException,
             ParseException {

    InputSource is = new InputSource(reader);

    this.rootHandler = rootHandler;
    this.rootNamespace = rootNamespace;
    this.rootElementName = rootElementName;
    parse(is);
  }


  
  /**
   * Parses XML.
   *
   * @param   input
   *            Supplies the XML to parse.
   *
   * @param   rootHandler
   *            The root element handler corresponding to the expected document
   *            type.
   *
   * @param   rootNamespace
   *            Root element namespace URI.
   *
   * @param   rootElementName
   *            Root element name.
   *
   * @throws  IOException
   *            Thrown by {@code input}.
   *
   * @throws  ParseException
   *            XML failed to validate against the schema implemented by
   *            {@code rootHandler}.
   */
  public void parse(InputStream input,
                    ElementHandler rootHandler,
                    String rootNamespace,
                    String rootElementName)
      throws IOException,
             ParseException {

    InputSource is = new InputSource(input);

    this.rootHandler = rootHandler;
    this.rootNamespace = rootNamespace;
    this.rootElementName = rootElementName;
    parse(is);
  }


  /**
   * Parses XML from a content source provided to the parser at
   * construction time.
   *
   * @param   rootHandler
   *            The root element handler corresponding to the expected document
   *            type.
   *
   * @param   rootNamespace
   *            Root element namespace URI.
   *
   * @param   rootElementName
   *            Root element name.
   *
   * @throws  IOException
   *            Thrown by {@code reader}.
   *
   * @throws  ParseException
   *            XML failed to validate against the schema implemented by
   *            {@code rootHandler}.
   */
  public void parse(ElementHandler rootHandler,
                    String rootNamespace,
                    String rootElementName)
      throws IOException,
             ParseException {

    throw new IllegalStateException("No content source defined");

  }


  /**
   * Parses XML.
   *
   * @param   is
   *            Supplies the XML to parse.
   *
   * @throws  IOException
   *            Thrown by {@code is}.
   *
   * @throws  ParseException
   *            XML failed to validate against the schema implemented by
   *            {@code rootHandler}.
   *
   */
  protected void parse(InputSource is)
      throws IOException,
             ParseException {

    try {

      // Lazy initialization of the parser factory.  There is a minor
      // init-time race condition here if two parsers are created
      // simultaneously, but the getSAXParserFactory() impl is thread-safe
      // and worse case scenario is that multiple parser factories are
      // initially created during the race.  Double-checked locking bug
      // makes it harder to do better w/out significant overhead.
      if (parserFactory == null) {
        parserFactory = getSAXParserFactory();
      }

      SAXParser sp = parserFactory.newSAXParser();
      ParserAdapter pa = new ParserAdapter(sp.getParser());
      pa.setContentHandler(this);
      pa.parse(is);

    } catch (SAXException e) {

      Exception rootException = e.getException();

      if (rootException instanceof ParseException) {

        throwParseException((ParseException)rootException);

      } else if (rootException instanceof IOException) {

        LogUtils.logException(logger, Level.WARNING, null, e);
        throw (IOException)rootException;

      } else {

        LogUtils.logException(logger, Level.WARNING, null, e);
        throw new ParseException(e);
      }
    } catch (ParserConfigurationException e) {

        LogUtils.logException(logger, Level.WARNING, null, e);
        throw new ParseException(e);
    }
  }


  /** Throws a parse exception with line/column information. */
  protected void throwParseException(ParseException e) throws ParseException {

    if (locator != null) {

      String elementLocation = "";
      if (curHandler != null) {
        elementLocation += ", element " + curHandler.qName;
      }

      String location =
        "[Line " + String.valueOf(locator.getLineNumber()) +
        ", Column " + String.valueOf(locator.getColumnNumber()) +
        elementLocation + "] ";

      LogUtils.logException(logger, Level.WARNING, location, e);

      throw new ParseException(location + e.getMessage());

    } else {

      LogUtils.logException(logger, Level.WARNING, null, e);
      throw e;
    }
  }


  /**
   * Computes a cumulative value of {@code xml:base} based on a prior value
   * and a new value. If the new value is an absolute URI, it is returned
   * unchanged. If the new value is a relative URI, it is combined with the
   * prior value.
   *
   * @param   curBase
   *            Current value of {@code xml:base} or {@code null}.
   *
   * @param   newBase
   *            New value of {@code xml:base}.
   *
   * @return  Combined value of {@code xml:base}, which is guaranteed to be
   *          an absolute URI.
   *
   * @throws  URISyntaxException
   *            Invalid value of {@code xml:base} (doesn't parse as a valid
   *            relative/absolute URI depending on {@code xml:base} context).
   */
  static String getCumulativeXmlBase(String curBase, String newBase)
      throws URISyntaxException {

    URI newBaseUri = new URI(newBase);

    if (curBase == null || curBase.equals("")) {

      // We require an absolute URI.
      if (!newBaseUri.isAbsolute()) {
        throw new URISyntaxException(
          newBase, "No xml:base established--need an absolute URI.");
      }

      return newBase;
    }

    URI curBaseUri = new URI(curBase);
    URI resultUri = curBaseUri.resolve(newBaseUri);
    assert resultUri.isAbsolute();
    return resultUri.toString();
  }


  /** SAX callback. */
  @Override
  public void startElement(String namespace,
                           String localName,
                           String qName,
                           Attributes attrs) throws SAXException {

    logger.fine("Start element " + qName);

    ElementHandler parentHandler = curHandler;

    if (curHandler == null &&
        namespace.equals(rootNamespace) &&
        localName.equals(rootElementName)) {

      curHandler = rootHandler;

    } else if (curHandler != null && unrecognizedElements == 0) {

      try {
        curHandler = curHandler.getChildHandler(namespace, localName, attrs);
      } catch (ParseException e) {
        throw new SAXException(e);
      } catch (IOException e) {
        throw new SAXException(e);
      }
    }

    if (curHandler != null && unrecognizedElements == 0) {

      curHandler.parent = parentHandler;
      curHandler.qName = qName;

      // Propagate xml:lang and xml:base.
      if (parentHandler != null) {
        curHandler.xmlLang = parentHandler.xmlLang;
        curHandler.xmlBase = parentHandler.xmlBase;
      }

      try {

        // First pass to extract xml:lang and xml:base.
        for (int i = attrs.getLength() - 1; i >= 0; --i) {

          String attrNamespace = attrs.getURI(i);
          String attrLocalName = attrs.getLocalName(i);
          String attrValue = attrs.getValue(i);

          if (attrNamespace.equals("http://www.w3.org/XML/1998/namespace")) {

            if (attrLocalName.equals("lang")) {

              curHandler.xmlLang = attrValue;
              logger.finer("xml:lang=" + attrValue);

            } else if (attrLocalName.equals("base")) {

              curHandler.xmlBase = getCumulativeXmlBase(curHandler.xmlBase,
                                                        attrValue);
              logger.finer("xml:base=" + curHandler.xmlBase);
            }
          }
        }

        // Second pass to process attributes.
        for (int i = attrs.getLength() - 1; i >= 0; --i) {

          String attrNamespace = attrs.getURI(i);
          String attrLocalName = attrs.getLocalName(i);
          String attrValue = attrs.getValue(i);

          logger.finer("Attribute " + attrLocalName + "='" + attrValue + "'");

          curHandler.processAttribute(attrNamespace, attrLocalName, attrValue);
        }

      } catch (ParseException e) {
        throw new SAXException(e);
      } catch (URISyntaxException e) {
        throw new SAXException(new ParseException(e.getMessage()));
      } catch (NumberFormatException e) {
        throw new SAXException(
          new ParseException("Invalid integer format. " + e.getMessage()));
      }

      // If the current handler accepts random XML, process the state acquired
      // so far.
      curHandler.okToInitializeXmlBlob = false;

      if (curHandler.xmlBlob != null) {

        // Store xml:lang and xml:base state, if any.
        if (curHandler.xmlLang != null) {
          curHandler.xmlBlob.setLang(curHandler.xmlLang);
        }

        if (curHandler.xmlBase != null) {
          curHandler.xmlBlob.setBase(curHandler.xmlBase);
        }
      }

    } else { // curHandler == null || unrecognizedElements > 0

      ++unrecognizedElements;

      if (curHandler == null) {
        curHandler = parentHandler;
      }

      // This element hasn't been recognized by the handler.
      // If the handler allows foreign XML, we'll start accumulating it as
      // a string.
      if (curHandler != null && curHandler.innerXml != null) {

        ArrayList<XmlWriter.Attribute> attrList =
          new ArrayList<XmlWriter.Attribute>(attrs.getLength());
        for (int i = attrs.getLength() - 1; i >= 0; --i) {

          String qNameAttr = attrs.getQName(i);
          ensureBlobNamespace(curHandler, qNameAttr);

          String value = attrs.getValue(i);

          XmlWriter.Attribute attr = new XmlWriter.Attribute(qNameAttr, value);
          attrList.add(attr);

          if (curHandler.fullTextIndex) {
            curHandler.fullTextIndexWriter.write(value);
            curHandler.fullTextIndexWriter.write(" ");
          }
        }

        try {
          ensureBlobNamespace(curHandler, qName);
          curHandler.innerXml.startElement(null, qName, attrList,
                                           namespaceDecls);
        } catch (IOException e) {
          throw new SAXException(e);
        }
      }
    }

    // Make way for next element's state.
    namespaceDecls.clear();
  }


  /** SAX callback. */
  @Override
  public void endElement(String namespace, String localName, String qName)
      throws SAXException {

    logger.fine("End element " + qName);

    if (unrecognizedElements > 0) {

      --unrecognizedElements;

      if (curHandler != null && curHandler.innerXml != null) {
        try {
          curHandler.innerXml.endElement();
        } catch (IOException e) {
          throw new SAXException(e);
        }
      }

    } else if (curHandler != null) {

      if (curHandler.xmlBlob != null) {

        StringBuffer blob = curHandler.innerXmlStringWriter.getBuffer();
        if (blob.length() != 0) {
          curHandler.xmlBlob.setBlob(blob.toString());

          if (curHandler.fullTextIndex) {
            curHandler.xmlBlob.setFullText(
              curHandler.fullTextIndexWriter.toString());
          }
        }
      }

      try {
        if (curHandler.buffer != null) {
          curHandler.value = curHandler.buffer.toString();

          // Free the memory associated with the buffer.
          curHandler.buffer = null;
        }
        curHandler.processEndElement();
      } catch (ParseException e) {
        throw new SAXException(e);
      }

      curHandler = curHandler.parent;
    }
  }


  /** SAX callback. */
  @Override
  public void characters(char[] text, int start, int len) throws SAXException {
    
    if (curHandler != null) {

      if (unrecognizedElements == 0) {
        
        if (curHandler.buffer == null) {
          curHandler.buffer = new StringBuffer();
        }
        
        curHandler.buffer.append(text, start, len);
      }

      if (curHandler.innerXml != null &&
          (curHandler.mixedContent || unrecognizedElements > 0)) {

        if (curHandler.fullTextIndex) {
          curHandler.fullTextIndexWriter.write(text, start, len);
          curHandler.fullTextIndexWriter.write("\n");
        }

        try {
          curHandler.innerXml.characters(new String(text, start, len));
        } catch (IOException e) {
          throw new SAXException(e);
        }
      }
    }
  }


  /** SAX callback. */
  @Override
  public void ignorableWhitespace(char[] text, int start, int len)
      throws SAXException {

    if (curHandler != null && curHandler.innerXml != null &&
        (curHandler.mixedContent || unrecognizedElements > 0)) {

      try {
        curHandler.innerXml.writeUnescaped(new String(text, start, len));
      } catch (IOException e) {
        throw new SAXException(e);
      }
    }
  }


  /** SAX callback. */
  @Override
  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }


  /** SAX callback. */
  @Override
  public void startPrefixMapping(String alias, String uri) {

    Stack<XmlWriter.Namespace> mapping = namespaceMap.get(alias);
    if (mapping == null) {
      mapping = new Stack<XmlWriter.Namespace>();
      namespaceMap.put(alias, mapping);
    }

    XmlWriter.Namespace ns = new XmlWriter.Namespace(alias, uri);
    mapping.push(ns);
    namespaceDecls.add(ns);
  }


  /** SAX callback. */
  @Override
  public void endPrefixMapping(String alias) {
    namespaceMap.get(alias).pop();
  }


  /** Ensures that the namespace from the QName is stored with the blob. */
  private void ensureBlobNamespace(ElementHandler handler, String qName) {

    // Get the namespace.
    XmlWriter.Namespace ns = null;
    String alias = qName.substring(0, Math.max(0, qName.indexOf(":")));
    if (alias.equals("xml")) {
      // "xml:" doesn't need a declaration.
      return;
    }

    Stack<XmlWriter.Namespace> mapping = namespaceMap.get(alias);
    if (mapping != null) {
      ns = mapping.peek();
    }

    // The namespace might be null for a namespace-less element.
    assert alias.length() == 0 || ns != null :
      "Namespace alias '" + alias + "' should be mapped in 'namespaceMap'.";

    // Make sure the namespace described within the blob.
    if (ns != null && !handler.blobNamespaces.contains(alias)) {
      handler.blobNamespaces.add(alias);
      handler.xmlBlob.namespaces.add(new XmlNamespace(alias, ns.getUri()));
    }
  }
}
