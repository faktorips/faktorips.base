/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.GregorianCalendar;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang.SystemUtils;
import org.junit.Test;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class XmlUtilTest extends XmlAbstractTestCase {

    @Test
    public void testGregorianCalendarToXmlDateString() {
        assertEquals("", XmlUtil.gregorianCalendarToXmlDateString(null)); //$NON-NLS-1$
        GregorianCalendar date = new GregorianCalendar(2005, 8, 9);
        assertEquals("2005-09-09", XmlUtil.gregorianCalendarToXmlDateString(date)); //$NON-NLS-1$
        date = new GregorianCalendar(2005, 9, 10);
        assertEquals("2005-10-10", XmlUtil.gregorianCalendarToXmlDateString(date)); //$NON-NLS-1$
    }

    @SuppressWarnings("deprecation")
    // Test of deprecated method
    @Test
    public void testParseXmlDateStringToGregorianCalendar() {
        assertNull(XmlUtil.parseXmlDateStringToGregorianCalendar("")); //$NON-NLS-1$
        GregorianCalendar date = new GregorianCalendar(2005, 8, 9);
        assertEquals(date, XmlUtil.parseXmlDateStringToGregorianCalendar("2005-09-09")); //$NON-NLS-1$
        assertEquals(date, XmlUtil.parseXmlDateStringToGregorianCalendar("2005-9-9")); //$NON-NLS-1$
        date = new GregorianCalendar(2005, 9, 10);
        assertEquals(date, XmlUtil.parseXmlDateStringToGregorianCalendar("2005-10-10")); //$NON-NLS-1$
        try {
            XmlUtil.parseXmlDateStringToGregorianCalendar("200d-10-22"); //$NON-NLS-1$
            fail();
        } catch (IllegalArgumentException e) {
            // Expected exception.
        }
    }

    @Test
    public void testParseGregorianCalendar() throws XmlParseException {
        assertNull(XmlUtil.parseGregorianCalendar("")); //$NON-NLS-1$
        GregorianCalendar date = new GregorianCalendar(2005, 8, 9);
        assertEquals(date, XmlUtil.parseGregorianCalendar("2005-09-09")); //$NON-NLS-1$
        assertEquals(date, XmlUtil.parseGregorianCalendar("2005-9-9")); //$NON-NLS-1$
        date = new GregorianCalendar(2005, 9, 10);
        assertEquals(date, XmlUtil.parseGregorianCalendar("2005-10-10")); //$NON-NLS-1$
        try {
            XmlUtil.parseGregorianCalendar("200d-10-22"); //$NON-NLS-1$
            fail();
        } catch (XmlParseException e) {
            // Expected exception.
        }
    }

    @Test
    public void testNodeToString() throws TransformerException {
        Document doc = newDocument();
        XmlUtil.nodeToString(doc, "Cp1252"); //$NON-NLS-1$

        Element element = doc.createElement("el");
        doc.appendChild(element);
        CDATASection cdataSection = doc.createCDATASection("a" + SystemUtils.LINE_SEPARATOR + "b");
        element.appendChild(cdataSection);

        String string = XmlUtil.nodeToString(doc, "Cp1252");
        String expected = "<?xml version=\"1.0\" encoding=\"WINDOWS-1252\" standalone=\"no\"?>"
                + SystemUtils.LINE_SEPARATOR + "<el><![CDATA[a" + SystemUtils.LINE_SEPARATOR + "b]]></el>"
                + SystemUtils.LINE_SEPARATOR;
        assertEquals(expected, string);
    }

    @Test
    public void testNodeToString_CheckLinebreaks() throws TransformerException {
        Document doc = newDocument();
        doc.setXmlStandalone(true);

        Element element = doc.createElement("el");
        doc.appendChild(element);

        String string = XmlUtil.nodeToString(doc, "UTF-8");
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + SystemUtils.LINE_SEPARATOR + "<el/>"
                + SystemUtils.LINE_SEPARATOR;
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

        String string = XmlUtil.nodeToString(root, "UTF-8");
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + SystemUtils.LINE_SEPARATOR
                + "<root xml:space=\"preserve\">" + SystemUtils.LINE_SEPARATOR + " <el/>" + SystemUtils.LINE_SEPARATOR
                + "</root>" + SystemUtils.LINE_SEPARATOR;
        assertEquals(expected, string);
    }

    @Test
    public void testGetFirstElement() {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement"); //$NON-NLS-1$
        assertNotNull(docElement);
        Element testElement = XmlUtil.getFirstElement(docElement, "TestElement"); //$NON-NLS-1$
        assertNotNull(testElement);
        assertEquals("öäüÖÄÜß", testElement.getAttribute("value")); //$NON-NLS-1$ //$NON-NLS-2$
        assertNull(XmlUtil.getFirstElement(docElement, "UnknownElement")); //$NON-NLS-1$
    }

    @Test
    public void testGetElementByIndex() throws Exception {
        Element rootEl = getTestDocument().getDocumentElement();
        assertEquals("TestElement", XmlUtil.getElement(rootEl, 0).getNodeName()); //$NON-NLS-1$
        assertEquals("DifferentElement", XmlUtil.getElement(rootEl, 1).getNodeName()); //$NON-NLS-1$
        assertEquals("TestElement", XmlUtil.getElement(rootEl, 2).getNodeName()); //$NON-NLS-1$
    }

    @Test
    public void testGetElement() {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement"); //$NON-NLS-1$

        Element testElement = XmlUtil.getElement(docElement, "TestElement", 0); //$NON-NLS-1$
        assertNotNull(testElement);
        assertEquals("öäüÖÄÜß", testElement.getAttribute("value")); //$NON-NLS-1$ //$NON-NLS-2$

        testElement = XmlUtil.getElement(docElement, "TestElement", 1); //$NON-NLS-1$
        assertNotNull(testElement);
        assertEquals("2", testElement.getAttribute("value")); //$NON-NLS-1$ //$NON-NLS-2$

    }

    @Test
    public void testGetTextNode() {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement"); //$NON-NLS-1$
        Element testElement = XmlUtil.getFirstElement(docElement, "TestElement"); //$NON-NLS-1$

        Text text = XmlUtil.getTextNode(testElement);
        assertNotNull(text);
        assertEquals("blabla", text.getData()); //$NON-NLS-1$

        /*
         * Test after manually processing a document, e.g. using XSL transformation text nodes could
         * be split into several sibling text nodes this test ensures that the node will be
         * normalized before returning the text of the child text nodes (see Interface
         * org.w3c.dom.Text)
         */
        Element child = doc.createElement("Child"); //$NON-NLS-1$
        testElement.appendChild(child);
        child.appendChild(doc.createTextNode("1")); //$NON-NLS-1$
        child.appendChild(doc.createTextNode("2")); //$NON-NLS-1$
        child.appendChild(doc.createTextNode("3")); //$NON-NLS-1$
        assertEquals("123", XmlUtil.getTextNode(child).getData()); //$NON-NLS-1$
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
}
