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

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class EnumSaxHandlerTest extends TestCase {

    public void testCorrectContent() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(
                EnumSaxHandlerTest.class.getName().replace('.', '/') + ".xml");
        EnumSaxHandler handler = new EnumSaxHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(is, handler);
        List<List<String>> enumValueList = handler.getEnumValueList();
        assertEquals(3, enumValueList.size());
        List<String> enumAttributeValues = enumValueList.get(0);
        assertEquals("a", enumAttributeValues.get(0));
        assertEquals("an", enumAttributeValues.get(1));
        enumAttributeValues = enumValueList.get(1);
        assertEquals("b", enumAttributeValues.get(0));
        assertEquals("bn", enumAttributeValues.get(1));
        enumAttributeValues = enumValueList.get(2);
        assertEquals("c", enumAttributeValues.get(0));
        assertEquals("cn", enumAttributeValues.get(1));
    }

    public void testWrongContent() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(
                EnumSaxHandlerTest.class.getName().replace('.', '/') + "Wrong.xml");
        EnumSaxHandler handler = new EnumSaxHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        try{
            saxParser.parse(is, handler);
            fail("Exception expected because of wrong content.");
        } catch(SAXException e){
        }

    }
}
