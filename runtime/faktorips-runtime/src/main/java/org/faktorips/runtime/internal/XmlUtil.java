/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
 * A collection of utility methods for XML DOM handling.
 */
public class XmlUtil {

    public static final String FAKTOR_IPS_SCHEMA_URL = "https://doc.faktorzehn.org/schema/faktor-ips/";

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

    /** Name of the XML element the containing the elements for the extension property values. */
    public static final String XML_EXT_PROPERTIES_ELEMENT = "ExtensionProperties"; //$NON-NLS-1$

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
                    + XML_EXT_PROPERTIES_ELEMENT
                    + ">)"); //$NON-NLS-1$
    private static final String MIXED_CONTENT_WITH_EXTENSION_PROPERTIES_AND_LINE_BREAKS_REPLACEMENT = "$3"; //$NON-NLS-1$
    private static final Pattern MIXED_CONTENT_WITH_EXTENSION_PROPERTIES = Pattern
            .compile("(?<=>)\\R([ \\t]+)([^<\\r\\n]+)\\R\\1(?=<" //$NON-NLS-1$
                    + XML_EXT_PROPERTIES_ELEMENT
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

    /**
     * This is a thread local variable because the {@link Transformer} is not thread safe.
     */
    private static ThreadLocal<Transformer> transformerHolder = new ThreadLocal<Transformer>() {
        @Override
        protected Transformer initialValue() {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            try {
                return transformerFactory.newTransformer();
            } catch (TransformerConfigurationException e) {
                //
            }
            return null;
        }
    };

    /**
     * This is a thread local variable because the document builder is not thread safe. For every
     * thread the method {@link #createDocumentBuilder()} is called automatically
     */
    private static ThreadLocal<DocumentBuilder> docBuilderHolder = new ThreadLocal<DocumentBuilder>() {
        @Override
        protected DocumentBuilder initialValue() {
            return createDocumentBuilder();
        }
    };

    private XmlUtil() {
        // Utility class not to be instantiated.
    }

    /**
     * @see #findFirstElement(Node, String) findFirstElement(Node, String) for null-safe processing
     */
    public static final Element getFirstElement(Node parent, String tagName) {
        return findFirstElement(parent, tagName).orElse(null);
    }

    public static final Optional<Element> findFirstElement(Node parent, String tagName) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element element = (Element)nl.item(i);
                if (element.getNodeName().equals(tagName)) {
                    return Optional.of((Element)nl.item(i));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the first Element node
     *
     * @see #findFirstElement(Node) findFirstElement(Node) for null-safe processing
     */
    public static final Element getFirstElement(Node parent) {
        return findFirstElement(parent).orElse(null);
    }

    /**
     * Returns the first Element node
     */
    public static final Optional<Element> findFirstElement(Node parent) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                return Optional.of((Element)nl.item(i));
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the child element with the given tag name and index. The index is the position of the
     * element considering all child elements with the given tag name.
     *
     * @param parent The parent node.
     * @param tagName the element tag name.
     * @param index The 0 based position of the child.
     * @return The element at the specified index
     * @throws IndexOutOfBoundsException if no element exists at the specified index.
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
     * Returns all child elements with the given tag name. Considers only direct children. Use
     * {@link Element#getElementsByTagName(String)} to search <em>all</em> descendants.
     *
     * @param parent The parent node.
     * @param tagName the element tag name.
     * @return all child elements with the matching tag name
     */
    public static final List<Element> getElements(Node parent, String tagName) {
        List<Element> elements = new ArrayList<>();
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element element = (Element)nl.item(i);
                if (element.getNodeName().equals(tagName)) {
                    elements.add(element);
                }
            }
        }
        return elements;
    }

    /**
     * Returns the node's text child node or <code>null</code> if the node hasn't got a text node.
     *
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
        if (getFirstCDataSection(node) != null) {
            return getFirstCDataSection(node).getData();
        } else if (getTextNode(node) != null) {
            return getTextNode(node).getData();
        }
        return null;
    }

    /**
     * Returns the value of the first element with the given node name, starts searching by the
     * given Element
     *
     * @param elem The first element (root or parent) element the search begins
     * @param nodeName The name searching for
     */
    public static final String getValueFromNode(Element elem, String nodeName) {
        return findFirstElement(elem, nodeName).map(e -> {
            Node child = e.getFirstChild();
            return child != null ? child.getNodeValue() : null;
        }).orElse(null);
    }

    /**
     * Returns a list of element's with the following criteria:
     * <ul>
     * <li>the node name must be equal to the given node name
     * <li>the node must contain an attribute with the attribute name
     * <li>the value of the attribute (with the given name) must be equal to the given value
     * </ul>
     */
    public static final List<Element> getElementsFromNode(Element elem,
            String nodeName,
            String attributeName,
            String attributeValue) {
        List<Element> result = new ArrayList<>();
        NodeList nl = elem.getChildNodes();
        for (int i = 0, max = nl.getLength(); i < max; i++) {
            if (!(nl.item(i) instanceof Element)) {
                continue;
            }
            Element el = (Element)nl.item(i);
            String typeAttr = el.getAttribute(attributeName);
            if (attributeValue.equals(typeAttr) && el.getNodeName().equals(nodeName)) {
                result.add(el);
            }
        }
        return result;
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
    public static void writeXMLtoResult(Result res, Document doc, String doctype, int indentWidth, String encoding)
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

    /**
     * @return a {@link DocumentBuilder} in a thread safe manner ({@link ThreadLocal}).
     */
    public static final DocumentBuilder getDocumentBuilder() {
        return docBuilderHolder.get();
    }

    public static final Document parseDocument(InputStream is) throws SAXException, IOException {
        return getDocumentBuilder().parse(is);
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
        return removeOrEscapeUnwantedCharacters(xml, escapeBlanks);
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
     * Will always replace Duplicate Windows Line Breaks and Unicode Control Characters. If
     * escapeBlanks is {@code true} this method will also escape non standard blanks with the
     * corresponding XML entity (e.g. non breaking space {@code U+00A0} to {@code &#160;}).
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
        ArrayList<String> searchList = new ArrayList<>();
        ArrayList<String> replacementList = new ArrayList<>();

        if (escapeBlanks) {
            searchList.addAll(Arrays.asList(NO_BREAK, NARROW_NO_BREAK,
                    ZERO_WIDTH_NO_BREAK, EN_QUAD,
                    EM_QUAD, EN_SPACE,
                    EM_SPACE, THREE_PER_EM,
                    FOUR_PER_EM, SIX_PER_EM,
                    FIGURE, PUNCTUATION,
                    THIN, HAIR,
                    MEDIUM_MATH, IDEOGRAPHIC,
                    DOUBLE_CARRIAGE_RETURN));
            replacementList.addAll(Arrays.asList(NO_BREAK_ESC, NARROW_NO_BREAK_ESC,
                    ZERO_WIDTH_NO_BREAK_ESC, EN_QUAD_ESC,
                    EM_QUAD_ESC, EN_SPACE_ESC,
                    EM_SPACE_ESC, THREE_PER_EM_ESC,
                    FOUR_PER_EM_ESC, SIX_PER_EM_ESC,
                    FIGURE_ESC, PUNCTUATION_ESC,
                    THIN_ESC, HAIR_ESC,
                    MEDIUM_MATH_ESC, IDEOGRAPHIC_ESC,
                    CARRIAGE_RETURN));
        } else {
            searchList.add(DOUBLE_CARRIAGE_RETURN);
            replacementList.add(CARRIAGE_RETURN);
        }

        for (int i = 0; i <= 31; i++) {
            if (i != '\t' && i != '\n') {
                String unicodeControlCharacter = "&#" + i + ";";
                searchList.add(unicodeControlCharacter);
                replacementList.add("");
            }
        }

        String[] searchArray = searchList.toArray(new String[searchList.size()]);
        String[] replacementArray = replacementList.toArray(new String[replacementList.size()]);

        return IpsStringUtils.replaceEach(xml, searchArray, replacementArray);
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

    private static Transformer getTransformer() {
        return transformerHolder.get();
    }

    private static final DocumentBuilder createDocumentBuilder() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e1) {
            throw new RuntimeException("Error creating document builder.", e1);
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
