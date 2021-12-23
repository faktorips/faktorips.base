/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Runtime.Version;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.model.internal.util.ValidatingDocumentBuilderHolder;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A collection of utility methods for XML handling.
 * 
 * @author Jan Ortmann
 */
public class XmlUtil {

    /**
     * Used for the project org.faktorips.devtools.stdbuilder because it uses jaxb-api as
     * dependency. Jaxb-api uses the same javax.xml package as {@link XMLConstants} from the JDK and
     * therefore causes the error: The package javax.xml is accessible from more than one module:
     * &lt;unnamed&gt;, java.xml
     */
    public static final String W3C_XML_SCHEMA_INSTANCE_NS_URI = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;

    /**
     * Used for the project org.faktorips.devtools.stdbuilder because it uses jaxb-api as
     * dependency. Jaxb-api uses the same javax.xml package as {@link XMLConstants} from the JDK and
     * therefore causes the error: The package javax.xml is accessible from more than one module:
     * &lt;unnamed&gt;, java.xml
     */
    public static final String XMLNS_ATTRIBUTE = XMLConstants.XMLNS_ATTRIBUTE;

    public static final String XML_IPS_DEFAULT_NAMESPACE = "http://www.faktorzehn.org"; //$NON-NLS-1$
    public static final String XML_ATTRIBUTE_SPACE = "xml:space"; //$NON-NLS-1$
    public static final String XML_ATTRIBUTE_SPACE_VALUE = "preserve"; //$NON-NLS-1$

    private static final Pattern XML_ELEMENT = Pattern.compile("(?<=[^?!/])>"); //$NON-NLS-1$
    private static final String PRESERVE_SPACE = " " + XML_ATTRIBUTE_SPACE + "=\"" + XML_ATTRIBUTE_SPACE_VALUE + "\">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static final Pattern INDENTED_CDATA = Pattern.compile(">[\\n\\r\\s]+<!\\[CDATA"); //$NON-NLS-1$
    private static final String INDENTED_CDATA_REPLACEMENT = "><![CDATA"; //$NON-NLS-1$
    private static final Pattern INDENTED_AFTER_CDATA = Pattern.compile("]]>[\\n\\r\\s]+</"); //$NON-NLS-1$
    private static final String INDENTED_AFTER_CDATA_REPLACEMENT = "]]></"; //$NON-NLS-1$
    private static final Pattern MIXED_CONTENT_WITH_EXTENSION_PROPERTIES_AND_LINE_BREAKS = Pattern
            .compile("(?<=>)\\R([ \\t]+)([ \\t]+)([^<\\r\\n]+)\\R\\1\\R\\2(?=<" //$NON-NLS-1$
                    + IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT
                    + ">)"); //$NON-NLS-1$
    private static final String MIXED_CONTENT_WITH_EXTENSION_PROPERTIES_AND_LINE_BREAKS_REPLACEMENT = "$3"; //$NON-NLS-1$
    private static final Pattern MIXED_CONTENT_WITH_EXTENSION_PROPERTIES = Pattern
            .compile("(?<=>)\\R([ \\t]+)([^<\\r\\n]+)\\R\\1(?=<" //$NON-NLS-1$
                    + IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT
                    + ">)"); //$NON-NLS-1$
    private static final String MIXED_CONTENT_WITH_EXTENSION_PROPERTIES_REPLACEMENT = "$2"; //$NON-NLS-1$
    private static final Pattern INDENTED_DATA = Pattern.compile("(?<=>)\\R(?: |\\t)+(\\R *)(?=<)"); //$NON-NLS-1$
    private static final String INDENTED_DATA_REPLACEMENT = "$1"; //$NON-NLS-1$

    private static final String CARRIAGE_RETURN = "\r"; //$NON-NLS-1$
    private static final String DOUBLE_CARRIAGE_RETURN = "\r\r"; //$NON-NLS-1$
    private static final String IDEOGRAPHIC_ESC = "&#12288;"; //$NON-NLS-1$
    private static final String IDEOGRAPHIC = "\u3000"; //$NON-NLS-1$
    private static final String MEDIUM_MATH_ESC = "&#8287;"; //$NON-NLS-1$
    private static final String MEDIUM_MATH = "\u205F"; //$NON-NLS-1$
    private static final String HAIR_ESC = "&#8202;"; //$NON-NLS-1$
    private static final String HAIR = "\u200A"; //$NON-NLS-1$
    private static final String THIN_ESC = "&#8201;"; //$NON-NLS-1$
    private static final String THIN = "\u2009"; //$NON-NLS-1$
    private static final String PUNCTUATION_ESC = "&#8200;"; //$NON-NLS-1$
    private static final String PUNCTUATION = "\u2008"; //$NON-NLS-1$
    private static final String FIGURE_ESC = "&#8199;"; //$NON-NLS-1$
    private static final String FIGURE = "\u2007"; //$NON-NLS-1$
    private static final String SIX_PER_EM_ESC = "&#8198;"; //$NON-NLS-1$
    private static final String SIX_PER_EM = "\u2006"; //$NON-NLS-1$
    private static final String FOUR_PER_EM_ESC = "&#8197;"; //$NON-NLS-1$
    private static final String FOUR_PER_EM = "\u2005"; //$NON-NLS-1$
    private static final String THREE_PER_EM_ESC = "&#8196;"; //$NON-NLS-1$
    private static final String THREE_PER_EM = "\u2004"; //$NON-NLS-1$
    private static final String EM_SPACE_ESC = "&#8195;"; //$NON-NLS-1$
    private static final String EM_SPACE = "\u2003"; //$NON-NLS-1$
    private static final String EN_SPACE_ESC = "&#8194;"; //$NON-NLS-1$
    private static final String EN_SPACE = "\u2002"; //$NON-NLS-1$
    private static final String EM_QUAD_ESC = "&#8193;"; //$NON-NLS-1$
    private static final String EM_QUAD = "\u2001"; //$NON-NLS-1$
    private static final String EN_QUAD_ESC = "&#8192;"; //$NON-NLS-1$
    private static final String EN_QUAD = "\u2000"; //$NON-NLS-1$
    private static final String ZERO_WIDTH_NO_BREAK_ESC = "&#65279;"; //$NON-NLS-1$
    private static final String ZERO_WIDTH_NO_BREAK = "\uFEFF"; //$NON-NLS-1$
    private static final String NARROW_NO_BREAK_ESC = "&#8239;"; //$NON-NLS-1$
    private static final String NARROW_NO_BREAK = "\u202F"; //$NON-NLS-1$
    private static final String NO_BREAK_ESC = "&#160;"; //$NON-NLS-1$
    private static final String NO_BREAK = "\u00A0"; //$NON-NLS-1$

    private static final Map<IpsObjectType, ThreadLocal<DocumentBuilder>> VALIDATORS = new ConcurrentHashMap<>();

    /**
     * This is a thread local variable because the document builder is not thread safe.
     */
    private static ThreadLocal<DocumentBuilder> docBuilderHolder = new DocBuilderHolder();

    /**
     * This is a thread local variable because the {@link Transformer} is not thread safe.
     */
    private static ThreadLocal<Transformer> transformerHolder = new ThreadLocal<>() {
        @Override
        protected Transformer initialValue() {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            try {
                return transformerFactory.newTransformer();
            } catch (TransformerConfigurationException e) {
                IpsLog.log(e);
            }
            return null;
        }
    };

    private XmlUtil() {
        // Utility class not to be instantiated.
    }

    private static Transformer getTransformer() {
        return transformerHolder.get();
    }

    public static final void setAttributeConvertNullToEmptyString(Element el, String attribute, String value) {
        if (value == null) {
            el.setAttribute(attribute, ""); //$NON-NLS-1$
        } else {
            el.setAttribute(attribute, value);
        }
    }

    public static final String getAttributeConvertEmptyStringToNull(Element el, String attribute) {
        String value = el.getAttribute(attribute);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return value;
    }

    public static final String dateToXmlDateString(Date date) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return gregorianCalendarToXmlDateString(calendar);
    }

    public static final String gregorianCalendarToXmlDateString(GregorianCalendar calendar) {
        if (calendar == null) {
            return StringUtils.EMPTY;
        }
        int month = calendar.get(Calendar.MONTH) + 1;
        int date = calendar.get(Calendar.DATE);
        return calendar.get(Calendar.YEAR) + "-" + (month < 10 ? "0" + month : "" + month) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + "-" + (date < 10 ? "0" + date : "" + date); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Parses the given XML String to a Date.
     */
    public static final Date parseXmlDateStringToDate(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return parseGregorianCalendar(s).getTime();
        } catch (XmlParseException e) {
            throw new IllegalArgumentException("Can't parse " + s + " to a date!"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static final GregorianCalendar parseGregorianCalendar(String s) throws XmlParseException {
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        try {
            StringTokenizer tokenizer = new StringTokenizer(s, "-"); //$NON-NLS-1$
            int year = Integer.parseInt(tokenizer.nextToken());
            int month = Integer.parseInt(tokenizer.nextToken());
            int date = Integer.parseInt(tokenizer.nextToken());
            return new GregorianCalendar(year, month - 1, date);
        } catch (NumberFormatException e) {
            throw new XmlParseException("Can't parse " + s + " to a date!", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Transforms the given node to a String.
     */
    public static final String nodeToString(Node node, String encoding) throws TransformerException {
        return nodeToString(node, encoding, false);
    }

    /**
     * Transforms the given node to a String.
     * 
     * @param node the node
     * @param encoding the used encoding
     * @param escapeBlanks if non standard blanks should be escaped
     * @return the node as {@link String}
     * @throws TransformerException for errors that occurred during the transformation process
     */
    public static final String nodeToString(Node node, String encoding, boolean escapeBlanks)
            throws TransformerException {
        boolean preserveSpace = removePreserveSpace(node);

        StringWriter writer = new StringWriter();
        nodeToWriter(node, writer, encoding);
        String xml = writer.toString();

        if (preserveSpace) {
            xml = addPreserveSpace(xml);
        }
        xml = noIndentationAroundCDATA(xml);
        xml = noIndentationToXmlDataContent(xml);
        xml = removeOrEscapeUnwantedCharacters(xml, escapeBlanks);

        return xml;
    }

    /**
     * In Java 9+ the bug JDK-8087303 changed the behavior of the XML serializer. Now it can not
     * distinguish whether the space is a meaningful content or just an indentation, so the
     * serializer regards the spaces as XML data content. This leads to spurious empty lines when
     * reading pretty printed XML files.
     * 
     * <p>
     * For Example:
     * </p>
     *
     * <pre>
     * {@code ···<DefaultValue isNull="true">LF}
     * </pre>
     *
     * The three spaces and the XML tag will be handled as two separate XML entities, and therefore
     * will be indented by each three spaces.
     *
     * <pre>
     * {@code ······LF
     * ···<DefaultValue isNull="true">LF}
     * </pre>
     * <p>
     * The problem is even more pronounced for mixed content, for example a default value with an
     * extension property. Here, we don't want any indentation around the value, as it might be a
     * String with leading and/or trailing whitespace.
     * </p>
     * 
     * @return the XML without spurious empty lines
     */
    private static String noIndentationToXmlDataContent(String xml) {
        // \R = carriage return and line feed pair, sole line feed, sole carriage return, vertical
        // tab, form feed, next line, line separator, paragraph separator; may backtrack into the
        // middle of a carriage return and line feed pair
        return INDENTED_DATA.matcher(
                MIXED_CONTENT_WITH_EXTENSION_PROPERTIES.matcher(
                        MIXED_CONTENT_WITH_EXTENSION_PROPERTIES_AND_LINE_BREAKS.matcher(xml)
                                .replaceAll(MIXED_CONTENT_WITH_EXTENSION_PROPERTIES_AND_LINE_BREAKS_REPLACEMENT))
                        .replaceAll(MIXED_CONTENT_WITH_EXTENSION_PROPERTIES_REPLACEMENT))
                .replaceAll(INDENTED_DATA_REPLACEMENT);
    }

    /**
     * Will always replace Duplicate Windows Line Breaks. If escapeBlanks is {@code true} this
     * method will also escape non standard blanks with the corresponding XML entity (e.g. non
     * breaking space {@code U+00A0} to {@code &#160;}).
     * <p>
     * Supported space characters are:
     * <ul>
     * <li>no-break space</li>
     * <li>narrow no-break space</li>
     * <li>zero width no-break space</li>
     * <li>en quad</li>
     * <li>em quad</li>
     * <li>en space</li>
     * <li>em space</li>
     * <li>three-per-em space</li>
     * <li>four-per-em space</li>
     * <li>six-per-em space</li>
     * <li>figure space</li>
     * <li>punctuation space</li>
     * <li>thin space</li>
     * <li>hair space</li>
     * <li>medium mathematical space</li>
     * <li>ideographic space</li>
     * </ul>
     * 
     * @param xml the XML to replace
     * @param escapeBlanks whether to escape non standard blanks
     * @return the replaced XML
     */
    private static String removeOrEscapeUnwantedCharacters(String xml, boolean escapeBlanks) {
        String[] searchList = null;
        String[] replacementList = null;

        if (escapeBlanks) {
            searchList = new String[] { NO_BREAK, NARROW_NO_BREAK,
                    ZERO_WIDTH_NO_BREAK, EN_QUAD,
                    EM_QUAD, EN_SPACE,
                    EM_SPACE, THREE_PER_EM,
                    FOUR_PER_EM, SIX_PER_EM,
                    FIGURE, PUNCTUATION,
                    THIN, HAIR,
                    MEDIUM_MATH, IDEOGRAPHIC,
                    DOUBLE_CARRIAGE_RETURN };
            replacementList = new String[] { NO_BREAK_ESC, NARROW_NO_BREAK_ESC,
                    ZERO_WIDTH_NO_BREAK_ESC, EN_QUAD_ESC,
                    EM_QUAD_ESC, EN_SPACE_ESC,
                    EM_SPACE_ESC, THREE_PER_EM_ESC,
                    FOUR_PER_EM_ESC, SIX_PER_EM_ESC,
                    FIGURE_ESC, PUNCTUATION_ESC,
                    THIN_ESC, HAIR_ESC,
                    MEDIUM_MATH_ESC, IDEOGRAPHIC_ESC,
                    CARRIAGE_RETURN };
        } else {
            searchList = new String[] { DOUBLE_CARRIAGE_RETURN };
            replacementList = new String[] { CARRIAGE_RETURN };
        }

        return StringUtils.replaceEach(xml, searchList, replacementList);
    }

    /**
     * Java 9+ respects {@code xml:space="preserve"} even when writing and ignores indentation
     * settings. We remove the attribute before the transformation and add it to the String
     * afterwards to prevent other tools from formatting the XML.
     * 
     * @return whether {@code xml:space="preserve"} was found on the node
     */
    private static boolean removePreserveSpace(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null && attributes.getNamedItem(XML_ATTRIBUTE_SPACE) != null) {
            attributes.removeNamedItem(XML_ATTRIBUTE_SPACE);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see XmlUtil#removePreserveSpace(Node)
     */
    private static String addPreserveSpace(String xml) {
        return XML_ELEMENT.matcher(xml).replaceFirst(PRESERVE_SPACE);
    }

    /**
     * Java 9+ adds indentation around CDATA. We remove it to avoid changes in XML files created
     * with Java &le; 8.
     */
    private static String noIndentationAroundCDATA(String xml) {
        return INDENTED_AFTER_CDATA
                .matcher(INDENTED_CDATA.matcher(xml).replaceAll(INDENTED_CDATA_REPLACEMENT))
                .replaceAll(INDENTED_AFTER_CDATA_REPLACEMENT);
    }

    /**
     * Transforms the given node to a string and writes in to the given writer.
     * <p>
     * The encoding that is used to transforms the string into bytes is defined by the writer. E.g.
     * a {@link OutputStreamWriter} is created with a char set / encoding. With a
     * {@link StringWriter} no encoding is necessary.
     * <p>
     * However, to get the encoding option set in the XML header e.g. {@code <?xml version="1.0"
     * encoding="Cp1252"?>}, it is necessary to pass the encoding to this method. Note that this
     * method does not check, if the writer's encoding and the given encoding are the same (as the
     * encoding is not available from the writer).
     */
    public static final void nodeToWriter(Node node, Writer writer, String encoding) throws TransformerException {
        Transformer transformer = getTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        // workaround to avoid linebreak after xml declaration
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, ""); //$NON-NLS-1$
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1"); //$NON-NLS-1$ //$NON-NLS-2$
        DOMSource source = new DOMSource(node);
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
    }

    public static final Document parseDocument(InputStream is) throws SAXException, IOException {
        return getDefaultDocumentBuilder().parse(is);
    }

    public static final DocumentBuilder getDefaultDocumentBuilder() {
        return docBuilderHolder.get();
    }

    /**
     * Creates an {@link DocumentBuilder} with a {@link Schema} for validation. Per default the
     * document builder will log XSD errors and warnings and only throws an {@link RuntimeException}
     * if an fatal errors occurs.
     * 
     * @param ipsObjectType The ips object type for loading the XSD schema file
     * @return An document builder with validating support
     */
    public static final DocumentBuilder getValidatingDocumentBuilder(IpsObjectType ipsObjectType) {
        return VALIDATORS.computeIfAbsent(ipsObjectType, ValidatingDocumentBuilderHolder::new).get();
    }

    /**
     * Creates an {@link DocumentBuilder} with a {@link Schema} for validation. The
     * {@code customErrorHandler} will be used instead of the default one.
     * <p>
     * e.g. use an custom {@link ErrorHandler} that adds all warnings and errors to a list.
     *
     * @param ipsObjectType The ips object type for loading the XSD schema file
     * @param customErrorHandler The error handler to use
     * @return An document builder with validating support
     */
    public static final DocumentBuilder getValidatingDocumentBuilder(IpsObjectType ipsObjectType,
            ErrorHandler customErrorHandler) {
        DocumentBuilder documentBuilder = getValidatingDocumentBuilder(ipsObjectType);
        documentBuilder.setErrorHandler(customErrorHandler);
        return documentBuilder;
    }

    /**
     * Writes an XML document to a file.
     * <p>
     * See also the
     * <a href='http://developers.sun.com/sw/building/codesamples/dom/doc/DOMUtil.java'>DOMUtil.java
     * example</a>.
     */
    public static void writeXMLtoFile(File file, Document doc, String doctype, int indentWidth, String encoding)
            throws TransformerException {

        writeXMLtoResult(new StreamResult(file), doc, doctype, indentWidth, encoding);
    }

    /**
     * Writes an XML document to a file.
     * <p>
     * See also the
     * <a href='http://developers.sun.com/sw/building/codesamples/dom/doc/DOMUtil.java'>DOMUtil.java
     * example</a>.
     */
    public static void writeXMLtoStream(OutputStream os, Document doc, String doctype, int indentWidth, String encoding)
            throws TransformerException {

        writeXMLtoResult(new StreamResult(os), doc, doctype, indentWidth, encoding);
    }

    /**
     * Writes an XML document to a DOM result object.
     * <p>
     * See also the
     * <a href='http://developers.sun.com/sw/building/codesamples/dom/doc/DOMUtil.java'>DOMUtil.java
     * example</a>.
     */
    private static void writeXMLtoResult(Result res, Document doc, String doctype, int indentWidth, String encoding)
            throws TransformerException {
        Source src = new DOMSource(doc);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
        // workaround to avoid linebreak after xml declaration
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, ""); //$NON-NLS-1$
        if (encoding != null) {
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        }
        if (doctype != null) {
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype);
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        if (indentWidth > 0) {
            // both settings are necessary, to accommodate versions in Java 1.4 and 1.5
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indentWidth)); //$NON-NLS-1$
        }
        transformer.transform(src, res);
    }

    public static final Element getFirstElement(Node parent, String tagName) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element element = (Element)nl.item(i);
                if (element.getNodeName().equals(tagName)) {
                    return (Element)nl.item(i);
                }
            }
        }
        return null;
    }

    /**
     * Returns the first Element node
     */
    public static final Element getFirstElement(Node parent) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                return (Element)nl.item(i);
            }
        }
        return null;
    }

    /**
     * Returns the child element with the given tag name and index. The index is the position of the
     * element considering all child elements with the given tag name. In contrast to
     * {@link Element#getElementsByTagName(String)} this method returns only the direct children,
     * not all descendants.
     * 
     * @param parent The parent node.
     * @param tagName the element tag name.
     * @param index The 0 based position of the child.
     * @return The element at the specified index
     * @throws IndexOutOfBoundsException if no element exists at the specified index.
     * 
     * @see Element#getElementsByTagName(java.lang.String)
     */
    public static final Element getElement(Node parent, String tagName, int index) {
        NodeList nl = parent.getChildNodes();
        int count = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element element = (Element)nl.item(i);
                if (element.getNodeName().equals(tagName)) {
                    if (count == index) {
                        return (Element)nl.item(i);
                    }
                    count++;
                }
            }
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the child element at the given index. The index is the position of the element
     * considering all child nodes of type element.
     * 
     * @param parent The parent node.
     * @param index The 0 based position of the child.
     * 
     * @throws IndexOutOfBoundsException if no element exists at the specified index.
     */
    public static final Element getElement(Node parent, int index) {
        NodeList nl = parent.getChildNodes();
        int count = 0;
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                if (count == index) {
                    return (Element)nl.item(i);
                }
                count++;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the node's text child node or <code>null</code> if the node hasn't got a text node.
     */
    public static final Text getTextNode(Node node) {
        node.normalize();
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
                return (Text)nl.item(i);
            }
        }
        return null;
    }

    /**
     * Returns the node's first CDATA section or <code>null</code> if the node hasn't got one.
     */
    public static final CDATASection getFirstCDataSection(Node node) {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
                return (CDATASection)nl.item(i);
            }
        }
        return null;
    }

    /**
     * Returns the node's first CDATA section if the node has one. If not, this returns the node's
     * text child node or <code>null</code> if the node hasn't got a text node.
     */
    public static final String getCDATAorTextContent(Node node) {
        if (XmlUtil.getFirstCDataSection(node) != null) {
            return XmlUtil.getFirstCDataSection(node).getData();
        } else if (XmlUtil.getTextNode(node) != null) {
            return XmlUtil.getTextNode(node).getData();
        }
        return null;
    }

    /**
     * Adds a child Element by the name given in childName to the parent and returns the child.
     */
    public static final Element addNewChild(Document doc, Node parent, String childName) {
        Element e = doc.createElement(childName);
        parent.appendChild(e);
        return e;
    }

    /**
     * Adds a TextNode containing the text to the parent and returns the TextNode.
     */
    public static final Node addNewTextChild(Document doc, Node parent, String text) {
        Node n = doc.createTextNode(text);
        parent.appendChild(n);
        return n;
    }

    /**
     * Adds a CDATASection containing the text to the parent and returns the CDATASection.
     */
    public static final Node addNewCDATAChild(Document doc, Node parent, String text) {
        Node n = doc.createCDATASection(text);
        parent.appendChild(n);
        return n;
    }

    /**
     * Adds a TextNode or, if text contains chars&gt;127, a CDATASection containing the text to the
     * parent and returns this new child.
     */
    public static final Node addNewCDATAorTextChild(Document doc, Node parent, String text) {
        if (text == null) {
            return null;
        }
        char[] chars = text.toCharArray();
        boolean toCDATA = false;
        for (int i = 0; i < chars.length && !toCDATA; i++) {
            if (chars[i] < 32 || 126 < chars[i]) {
                toCDATA = true;
            }
        }
        return toCDATA ? addNewCDATAChild(doc, parent, text) : addNewTextChild(doc, parent, text);
    }

    /**
     * Returns the value for the given property from the given parent element. The parent XML
     * element must have the following format:
     * 
     * <pre>
     * {@code <Parent>
     *   <Property isNull="false">value</Property>
     * </Parent>
     * }
     * </pre>
     * 
     * @throws NullPointerException if parent or propertyName is <code>null</code> or the parent
     *             element does not contain an element with the given propertyName.
     * @deprecated use {@link ValueToXmlHelper#getValueFromElement(Element, String)} instead
     */
    @Deprecated
    public String getValueFromElement(Element parent, String propertyName) {
        Element propertyEl = XmlUtil.getFirstElement(parent, propertyName);
        if (propertyEl == null) {
            throw new NullPointerException();
        }
        String isNull = parent.getAttribute("isNull"); //$NON-NLS-1$
        if (Boolean.valueOf(isNull).booleanValue()) {
            return null;
        } else {
            Text textNode = getTextNode(parent);
            if (textNode == null) {
                return ""; //$NON-NLS-1$
            } else {
                return textNode.getNodeValue();
            }
        }
    }

    public static final String getSchemaLocation(IpsObjectType ipsObjectType) {
        Version version = Abstractions.getVersion();
        String schemaLocation = String.format("https://doc.faktorzehn.org/schema/faktor-ips/%s/%s.xsd", //$NON-NLS-1$
                version.feature() + "." + version.interim(), //$NON-NLS-1$
                ipsObjectType.getXmlElementName());
        return schemaLocation;
    }

    private static final class DocBuilderHolder extends ThreadLocal<DocumentBuilder> {
        @Override
        protected DocumentBuilder initialValue() {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException e) throws SAXException {
                    throw e;
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    throw e;
                }

                @Override
                public void warning(SAXParseException e) throws SAXException {
                    throw e;
                }
            });
            return builder;
        }
    }
}
