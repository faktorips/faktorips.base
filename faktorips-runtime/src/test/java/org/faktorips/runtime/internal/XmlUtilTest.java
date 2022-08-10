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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.faktorips.runtime.XmlAbstractTestCase;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class XmlUtilTest extends XmlAbstractTestCase {

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

}
