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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.faktorips.runtime.XmlAbstractTestCase;
import org.junit.Test;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class XmlUtilTest extends XmlAbstractTestCase {

    private static final String LF = System.lineSeparator();
    private static final String UTF8 = "UTF-8";
    private static final String XML_EXT_PROPERTIES_ELEMENT = "ExtensionProperties";

    @Test
    public void testGetFirstElement() {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement");
        assertNotNull(docElement);
        Element testElement = XmlUtil.getFirstElement(docElement, "TestElement");
        assertNotNull(testElement);
        assertEquals("öäüÖÄÜß", testElement.getAttribute("value"));
        assertNull(XmlUtil.getFirstElement(docElement, "UnknownElement"));
    }

    @Test
    public void testFindFirstElement() {
        Document doc = getTestDocument();
        Optional<Element> docElement = XmlUtil.findFirstElement(doc, "DocElement");
        assertTrue(docElement.isPresent());
        Optional<Element> testElement = XmlUtil.findFirstElement(docElement.get(), "TestElement");
        assertTrue(testElement.isPresent());
        assertEquals("öäüÖÄÜß", testElement.get().getAttribute("value"));
        assertFalse(XmlUtil.findFirstElement(docElement.get(), "UnknownElement").isPresent());
    }

    @Test
    public void testGetElement() {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement");

        Element testElement = XmlUtil.getElement(docElement, "TestElement", 0);
        assertNotNull(testElement);
        assertEquals("öäüÖÄÜß", testElement.getAttribute("value"));

        testElement = XmlUtil.getElement(docElement, "TestElement", 1);
        assertNotNull(testElement);
        assertEquals("2", testElement.getAttribute("value"));

    }

    @Test
    public void testGetTextNode() {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement");
        Element testElement = XmlUtil.getFirstElement(docElement, "TestElement");

        Text text = XmlUtil.getTextNode(testElement);
        assertNotNull(text);
        assertEquals("blabla", text.getData());

        /*
         * test after manually processing a document e.g. using XSL transformation text nodes could
         * be split into several sibling text nodes this test ensures that the node will be
         * normalized before returning the text of the child text nodes see Interface
         * org.w3c.dom.Text
         */
        Element child = doc.createElement("Child");
        testElement.appendChild(child);
        child.appendChild(doc.createTextNode("1"));
        child.appendChild(doc.createTextNode("2"));
        child.appendChild(doc.createTextNode("3"));
        assertEquals("123", XmlUtil.getTextNode(child).getData());
    }

    @Test
    public void testGetValueFromNode() {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement");
        String testValue = XmlUtil.getValueFromNode(docElement, "ChildB");
        assertEquals("testValue", testValue);
    }

    @Test
    public void testGetElementsFromNode() {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement");
        List<Element> testElements = XmlUtil.getElementsFromNode(docElement, "ChildA", "type", "testtype1");
        assertEquals(2, testElements.size());
    }

    @Test
    public void testGetElements_multipleInOrder() {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement");

        List<Element> elements = XmlUtil.getElements(docElement, "ChildA");

        assertEquals(3, elements.size());
        for (int i = 0; i <= 2; i++) {
            assertEquals(Integer.toString(i), elements.get(i).getAttribute("id"));
        }
    }

    @Test
    public void testGetElements_noneFound() {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement");

        List<Element> elements = XmlUtil.getElements(docElement, "FooBar");

        assertNotNull(elements);
        assertEquals(0, elements.size());
    }

    @Test
    public void testNodeToString() throws TransformerException {
        Document doc = newDocument();
        XmlUtil.nodeToString(doc, "Cp1252"); //$NON-NLS-1$

        Element element = doc.createElement("el");
        doc.appendChild(element);
        CDATASection cdataSection = doc.createCDATASection("a" + LF + "b");
        element.appendChild(cdataSection);

        String string = XmlUtil.nodeToString(doc, "Cp1252");
        String expected = "<?xml version=\"1.0\" encoding=\"WINDOWS-1252\" standalone=\"no\"?>"
                + LF + "<el><![CDATA[a" + LF + "b]]></el>"
                + LF;
        assertEquals(expected, string);
    }

    @Test
    public void testNodeToString_CheckLinebreaks() throws TransformerException {
        Document doc = newDocument();
        doc.setXmlStandalone(true);

        Element element = doc.createElement("el");
        doc.appendChild(element);

        String string = XmlUtil.nodeToString(doc, UTF8);
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LF + "<el/>"
                + LF;
        assertEquals(expected, string);
    }

    @Test
    public void testNodeToString_PreserveSpace() throws TransformerException {
        Document doc = newDocument();
        doc.setXmlStandalone(true);

        Element root = doc.createElement("root");
        root.setAttribute(XmlUtil.XML_ATTRIBUTE_SPACE, XmlUtil.XML_ATTRIBUTE_SPACE_VALUE);
        Element element = doc.createElement("el");
        root.appendChild(element);
        doc.appendChild(root);

        String string = XmlUtil.nodeToString(root, UTF8);
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LF
                + "<root xml:space=\"preserve\">" + LF + " <el/>" + LF
                + "</root>" + LF;
        assertEquals(expected, string);
    }

    @Test
    public void testGetDocumentBuilder() throws UnsupportedEncodingException, SAXException, IOException {
        DocumentBuilder docBuilder = XmlUtil.getDocumentBuilder();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><abc/>";
        docBuilder.parse(new ByteArrayInputStream(xml.getBytes(UTF8)));
    }

    @Test
    public void testJava9NoIndentationToXmlDataContent() throws Exception {
        File xmlFile = createXmlFileAndSaveWithIdent();
        Document doc = XmlUtil.parseDocument(new FileInputStream(xmlFile));
        Element rootElement = XmlUtil.getFirstElement(doc, "root"); //$NON-NLS-1$

        // java9 transformer has empty lines
        assertThat(internalNodeToString(rootElement),
                is("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LF
                        + "<root>" + LF
                        + "   " + LF
                        + " <element>SOME_DATA</element>" + LF
                        + " " + LF
                        + "</root>" + LF));
        // java9 fix with regex, removes empty lines
        assertThat(XmlUtil.nodeToString(rootElement, UTF8),
                is("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LF
                        + "<root>" + LF
                        + " <element>SOME_DATA</element>" + LF
                        + "</root>" + LF));

        if (!xmlFile.delete()) {
            xmlFile.deleteOnExit();
        }
    }

    @Test
    public void testJava9NoIndentationToXmlDataContentWithTabs() throws Exception {
        Document doc = getTestDocument();
        Element root = XmlUtil.getFirstElement(doc, "DocElement"); //$NON-NLS-1$

        // TestDocument has Tabs on single empty lines
        assertThat(internalNodeToString(root),
                is("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LF
                        + "<DocElement>" + LF
                        + " \t" + LF
                        + " <TestElement value=\"öäüÖÄÜß\">blabla</TestElement>" + LF
                        + " \t" + LF
                        + " <DifferentElement/>" + LF
                        + " \t" + LF
                        + " <TestElement value=\"2\"/>" + LF
                        + "     " + LF
                        + " <!-- -->" + LF
                        + " \t" + LF
                        + " <ChildA id=\"0\" type=\"testtype1\"/>" + LF
                        + " \t" + LF
                        + " <ChildA id=\"1\" type=\"testtype1\"/>" + LF
                        + " \t" + LF
                        + " <ChildA id=\"2\" type=\"testtype2\"/>" + LF
                        + " \t" + LF
                        + " <ChildB>testValue</ChildB>" + LF
                        + " \t" + LF
                        + " <ChildC>" + LF
                        + "  \t\t" + LF
                        + "  <ChildA id=\"3\" type=\"deep\"/>" + LF
                        + "  \t" + LF
                        + " </ChildC>" + LF
                        + " " + LF
                        + "</DocElement>" + LF));
        // TestDocument has no Tabs and no empty lines
        assertThat(XmlUtil.nodeToString(root, UTF8),
                is("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LF
                        + "<DocElement>" + LF
                        + " <TestElement value=\"öäüÖÄÜß\">blabla</TestElement>" + LF
                        + " <DifferentElement/>" + LF
                        + " <TestElement value=\"2\"/>" + LF
                        + " <!-- -->" + LF
                        + " <ChildA id=\"0\" type=\"testtype1\"/>" + LF
                        + " <ChildA id=\"1\" type=\"testtype1\"/>" + LF
                        + " <ChildA id=\"2\" type=\"testtype2\"/>" + LF
                        + " <ChildB>testValue</ChildB>" + LF
                        + " <ChildC>" + LF
                        + "  <ChildA id=\"3\" type=\"deep\"/>" + LF
                        + " </ChildC>" + LF
                        + "</DocElement>" + LF));
    }

    @Test
    public void testJava9NoIndentationToXmlMixedContent() throws Exception {
        File xmlFile = createXmlFileAndSaveWithIdent((d, e) -> {
            e.appendChild(d.createTextNode(" SOME_DATA_WITH_LEADING_SPACE"));
            Element extensionProperties = d.createElement(XML_EXT_PROPERTIES_ELEMENT);
            extensionProperties.appendChild(d.createElement("Value"));
            e.appendChild(extensionProperties);
        });
        Document doc = XmlUtil.parseDocument(new FileInputStream(xmlFile));
        Element rootElement = XmlUtil.getFirstElement(doc, "root"); //$NON-NLS-1$

        // java9 transformer has empty lines
        assertThat(internalNodeToString(rootElement),
                is("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LF + "<root>" + LF + "   " + LF
                        + " <element>" + LF
                        + "       SOME_DATA_WITH_LEADING_SPACE" + LF
                        + "    " + LF
                        + "  <ExtensionProperties>" + LF
                        + "         " + LF
                        + "   <Value/>" + LF
                        + "       " + LF
                        + "  </ExtensionProperties>" + LF
                        + "    " + LF
                        + " </element>" + LF + " " + LF
                        + "</root>" + LF));
        // java9 fix with regex, removes empty lines
        assertThat(XmlUtil.nodeToString(rootElement, UTF8),
                is("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LF
                        + "<root>" + LF
                        + " <element> SOME_DATA_WITH_LEADING_SPACE<ExtensionProperties>" + LF
                        + "   <Value/>" + LF
                        + "  </ExtensionProperties>" + LF
                        + " </element>" + LF
                        + "</root>" + LF));

        if (!xmlFile.delete()) {
            xmlFile.deleteOnExit();
        }
    }

    @Test
    public void testEscapeSpacesToXml() throws TransformerException {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement"); //$NON-NLS-1$
        Element el = doc.createElement("ElementWithNonBreakingSpace");
        el.setTextContent(
                "For\u00A0some\u202Freason,\uFEFFsomeone\u2000used\u2001a\u2002lot\u2003of\u2004different\u2005spaces.\u2006Thanks\u2007to\u2008apache\u2009commons\u200Awe\u205Fcan\u3000replace them.");
        docElement.appendChild(el);
        String string = XmlUtil.nodeToString(doc, "UTF-8", true);
        assertThat(string, containsString(
                "For&#160;some&#8239;reason,&#65279;someone&#8192;used&#8193;a&#8194;lot&#8195;of&#8196;different&#8197;spaces.&#8198;Thanks&#8199;to&#8200;apache&#8201;commons&#8202;we&#8287;can&#12288;replace them."));
    }

    private String internalNodeToString(Element rootElement) throws TransformerException {
        StringWriter writer = new StringWriter();
        XmlUtil.nodeToWriter(rootElement, writer, UTF8);
        return writer.toString();
    }

    private File createXmlFileAndSaveWithIdent() throws IOException, TransformerException {
        return createXmlFileAndSaveWithIdent((d, e) -> e.appendChild(d.createTextNode("SOME_DATA")));
    }

    private File createXmlFileAndSaveWithIdent(BiConsumer<Document, Element> elementEditor)
            throws IOException, TransformerException {
        Document inputDoc = newDocument();
        Element root = inputDoc.createElement("root"); //$NON-NLS-1$
        Element element = inputDoc.createElement("element"); //$NON-NLS-1$
        elementEditor.accept(inputDoc, element);
        root.appendChild(element);
        inputDoc.appendChild(root);
        File file = File.createTempFile("xmltest", ".xml"); //$NON-NLS-1$//$NON-NLS-2$
        XmlUtil.writeXMLtoFile(file, inputDoc, null, 2, UTF8);
        return file;
    }
}
