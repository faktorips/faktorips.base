/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.GregorianCalendar;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 *
 */
public class XmlUtilTest extends XmlAbstractTestCase {

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGregorianCalendarToXmlDateString() {
        assertEquals("", XmlUtil.gregorianCalendarToXmlDateString(null)); //$NON-NLS-1$
        GregorianCalendar date = new GregorianCalendar(2005, 8, 9);
        assertEquals("2005-09-09", XmlUtil.gregorianCalendarToXmlDateString(date)); //$NON-NLS-1$
        date = new GregorianCalendar(2005, 9, 10);
        assertEquals("2005-10-10", XmlUtil.gregorianCalendarToXmlDateString(date)); //$NON-NLS-1$
    }

    public void testParse() {
        assertNull(XmlUtil.parseXmlDateStringToGregorianCalendar("")); //$NON-NLS-1$
        GregorianCalendar date = new GregorianCalendar(2005, 8, 9);
        assertEquals(date, XmlUtil.parseXmlDateStringToGregorianCalendar("2005-09-09")); //$NON-NLS-1$
        assertEquals(date, XmlUtil.parseXmlDateStringToGregorianCalendar("2005-9-9")); //$NON-NLS-1$
        date = new GregorianCalendar(2005, 9, 10);
        assertEquals(date, XmlUtil.parseXmlDateStringToGregorianCalendar("2005-10-10")); //$NON-NLS-1$
    }

    public void testNodeToString() throws TransformerException {
        Document doc = newDocument();
        XmlUtil.nodeToString(doc, "Cp1252"); //$NON-NLS-1$
    }

    public void testGetFirstElement() throws TransformerException, UnsupportedEncodingException, SAXException,
            IOException, ParserConfigurationException {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement"); //$NON-NLS-1$
        assertNotNull(docElement);
        Element testElement = XmlUtil.getFirstElement(docElement, "TestElement"); //$NON-NLS-1$
        assertNotNull(testElement);
        assertEquals("öäüÖÄÜß", testElement.getAttribute("value")); //$NON-NLS-1$ //$NON-NLS-2$
        assertNull(XmlUtil.getFirstElement(docElement, "UnknownElement")); //$NON-NLS-1$
    }

    public void testGetElementByIndex() throws Exception {
        Element rootEl = getTestDocument().getDocumentElement();
        assertEquals("TestElement", XmlUtil.getElement(rootEl, 0).getNodeName()); //$NON-NLS-1$
        assertEquals("DifferentElement", XmlUtil.getElement(rootEl, 1).getNodeName()); //$NON-NLS-1$
        assertEquals("TestElement", XmlUtil.getElement(rootEl, 2).getNodeName()); //$NON-NLS-1$
    }

    public void testGetElement() throws TransformerException, UnsupportedEncodingException, SAXException, IOException,
            ParserConfigurationException {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement"); //$NON-NLS-1$

        Element testElement = XmlUtil.getElement(docElement, "TestElement", 0); //$NON-NLS-1$
        assertNotNull(testElement);
        assertEquals("öäüÖÄÜß", testElement.getAttribute("value")); //$NON-NLS-1$ //$NON-NLS-2$

        testElement = XmlUtil.getElement(docElement, "TestElement", 1); //$NON-NLS-1$
        assertNotNull(testElement);
        assertEquals("2", testElement.getAttribute("value")); //$NON-NLS-1$ //$NON-NLS-2$

    }

    public void testGetTextNode() {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement"); //$NON-NLS-1$
        Element testElement = XmlUtil.getFirstElement(docElement, "TestElement"); //$NON-NLS-1$

        Text text = XmlUtil.getTextNode(testElement);
        assertNotNull(text);
        assertEquals("blabla", text.getData()); //$NON-NLS-1$

        // test after manually processing a document
        // e.g. using xsl transformation text nodes could be split into several sibling text nodes
        // this test ensures that the node will be normalized before returning the text of the child
        // text nodes
        // see Interface org.w3c.dom.Text
        Element child = doc.createElement("Child");
        testElement.appendChild(child);
        child.appendChild(doc.createTextNode("1"));
        child.appendChild(doc.createTextNode("2"));
        child.appendChild(doc.createTextNode("3"));
        assertEquals("123", XmlUtil.getTextNode(child).getData());
    }
}
