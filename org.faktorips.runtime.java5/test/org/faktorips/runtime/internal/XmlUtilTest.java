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

package org.faktorips.runtime.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.faktorips.runtime.XmlAbstractTestCase;
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
    
    public void testGetFirstElement() throws TransformerException, UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement"); 
        assertNotNull(docElement);
        Element testElement = XmlUtil.getFirstElement(docElement, "TestElement"); 
        assertNotNull(testElement);
        assertEquals("öäüÖÄÜß", testElement.getAttribute("value")); 
        assertNull(XmlUtil.getFirstElement(docElement, "UnknownElement")); 
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
        
        // test after manually processing a document
        //   e.g. using xsl transformation text nodes could be split into several sibling text nodes
        // this test ensures that the node will be normalized before returning the text of the child text nodes
        // see Interface org.w3c.dom.Text
        Element child = doc.createElement("Child");
        testElement.appendChild(child);
        child.appendChild(doc.createTextNode("1"));
        child.appendChild(doc.createTextNode("2"));
        child.appendChild(doc.createTextNode("3"));
        assertEquals("123", XmlUtil.getTextNode(child).getData());
    }

    public void testGetValueFromNode(){
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement");
        String testValue = XmlUtil.getValueFromNode(docElement, "ChildB"); 
        assertEquals("testValue", testValue);
    }

    public void testGetElementsFromNode(){
        Document doc = getTestDocument();
        Element docElement = XmlUtil.getFirstElement(doc, "DocElement"); 
        List<Element> testElements = XmlUtil.getElementsFromNode(docElement, "ChildA", "type", "testtype1");
        assertEquals(2, testElements.size());
    }
}
