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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.GregorianCalendar;

import org.junit.Test;
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
    public void testRemoveIds() {
        Document inputDoc = newDocument();
        Element root = inputDoc.createElement("root"); //$NON-NLS-1$
        root.setAttribute("id", "4711");
        Element element = inputDoc.createElement("element"); //$NON-NLS-1$
        element.setAttribute("id", "23");
        root.appendChild(element);
        Element extensionPropertiesElement = inputDoc.createElement("ExtensionProperties"); //$NON-NLS-1$
        extensionPropertiesElement.setAttribute("id", "ext");
        root.appendChild(extensionPropertiesElement);
        inputDoc.appendChild(root);

        XmlUtil.removeIds(root);

        assertThat(root.hasAttribute("id"), is(false));
        assertThat(element.hasAttribute("id"), is(false));
        assertThat(extensionPropertiesElement.hasAttribute("id"), is(true));
        assertThat(extensionPropertiesElement.getAttribute("id"), is("ext"));
    }
}
