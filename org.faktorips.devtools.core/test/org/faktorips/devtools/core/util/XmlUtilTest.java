/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testGregorianCalendarToXmlDateString() {
        assertEquals("", XmlUtil.gregorianCalendarToXmlDateString(null));
        GregorianCalendar date = new GregorianCalendar(2005, 8, 9);
        assertEquals("2005-09-09", XmlUtil.gregorianCalendarToXmlDateString(date));
        date = new GregorianCalendar(2005, 9, 10);
        assertEquals("2005-10-10", XmlUtil.gregorianCalendarToXmlDateString(date));
    }
    
    public void testParse() {
        assertNull(XmlUtil.parseXmlDateStringToGregorianCalendar(""));
        GregorianCalendar date = new GregorianCalendar(2005, 8, 9);
        assertEquals(date, XmlUtil.parseXmlDateStringToGregorianCalendar("2005-09-09"));
        assertEquals(date, XmlUtil.parseXmlDateStringToGregorianCalendar("2005-9-9"));
        date = new GregorianCalendar(2005, 9, 10);
        assertEquals(date, XmlUtil.parseXmlDateStringToGregorianCalendar("2005-10-10"));
    }

    public void testNodeToString() throws TransformerException {
        Document doc = newDocument();
        XmlUtil.nodeToString(doc, "Cp1252");
    }
    
    public void testGetFirstElement() throws TransformerException, UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement");
        assertNotNull(docElement);
        Element testElement = XmlUtil.getFirstElement(docElement, "TestElement");
        assertNotNull(testElement);
        assertEquals("öäüÖÄÜß", testElement.getAttribute("value"));
        assertNull(XmlUtil.getFirstElement(docElement, "UnkownElement"));
    }
    
    public void testGetElement() throws TransformerException, UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement");

        Element testElement = XmlUtil.getElement(docElement, "TestElement", 0);
        assertNotNull(testElement);
        assertEquals("öäüÖÄÜß", testElement.getAttribute("value"));
        
        testElement = XmlUtil.getElement(docElement, "TestElement", 1);
        assertNotNull(testElement);
        assertEquals("2", testElement.getAttribute("value"));
        
    }
    
    public void testGetTextNode() {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement");
        Element testElement = XmlUtil.getFirstElement(docElement, "TestElement");

        Text text = XmlUtil.getTextNode(testElement);
        assertNotNull(text);
        assertEquals("blabla", text.getData());
    }
    

}
