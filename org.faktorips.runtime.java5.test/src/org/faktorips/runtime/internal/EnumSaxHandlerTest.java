/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.faktorips.values.IInternationalString;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class EnumSaxHandlerTest {

    private InputStream is;
    private SAXParser saxParser;
    private EnumSaxHandler handler;

    @Before
    public void setUp() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        saxParser = factory.newSAXParser();
        handler = new EnumSaxHandler();
    }

    @Before
    public void tearDown() {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private InputStream createInputStream(String extension) {
        return EnumSaxHandlerTest.class.getClassLoader().getResourceAsStream(
                EnumSaxHandlerTest.class.getName().replace('.', '/') + (extension != null ? extension : ".xml"));
    }

    @Test
    public void testCorrectContent() throws Exception {
        is = createInputStream(null);
        saxParser.parse(is, handler);
        List<List<Object>> enumValueList = handler.getEnumValueList();
        assertEquals(3, enumValueList.size());
        List<Object> enumAttributeValues = enumValueList.get(0);
        assertEquals("a", enumAttributeValues.get(0));
        assertEquals("an", enumAttributeValues.get(1));
        enumAttributeValues = enumValueList.get(1);
        assertEquals("b", enumAttributeValues.get(0));
        assertEquals("bn", enumAttributeValues.get(1));
        enumAttributeValues = enumValueList.get(2);
        assertEquals("c", enumAttributeValues.get(0));
        assertEquals("cn", enumAttributeValues.get(1));
    }

    @Test
    public void testWrongContent() throws Exception {
        is = createInputStream("Wrong.xml");
        try {
            saxParser.parse(is, handler);
            fail("Exception expected because of wrong content.");
        } catch (SAXException e) {
            // ignore
        }
    }

    /**
     * Tests if the handler works correctly if the characters() method will be called multiple times
     * for a tag content. This happens for example if the value within a tag contains carriage
     * return characters.
     * 
     */
    @Test
    public void testCRContent() throws Exception {
        is = createInputStream("WithCRContent.xml");
        saxParser.parse(is, handler);
        List<List<Object>> enumValueList = handler.getEnumValueList();
        assertEquals(1, enumValueList.size());
        List<Object> values = enumValueList.get(0);
        assertEquals(1, values.size());
        Object value = values.get(0);
        assertEquals("a\rb\rc", value);
    }

    /**
     * Tests if the handler works correctly if the characters() method will be called multiple times
     * for a tag content. This happens for example if the value within a tag contains carriage
     * return characters.
     * 
     */
    @Test
    public void testInternationalContent() throws Exception {
        is = createInputStream("International.xml");
        saxParser.parse(is, handler);

        List<List<Object>> enumValueList = handler.getEnumValueList();
        assertEquals(2, enumValueList.size());
        List<Object> values = enumValueList.get(0);
        assertEquals(2, values.size());
        assertEquals("a", values.get(0));
        assertEquals("deText", ((IInternationalString)values.get(1)).get(Locale.GERMAN));
        assertEquals("enText", ((IInternationalString)values.get(1)).get(Locale.ENGLISH));
        values = enumValueList.get(1);
        assertEquals(2, values.size());
        assertEquals("b", values.get(0));
        assertEquals("deText2", ((IInternationalString)values.get(1)).get(Locale.GERMAN));
        assertEquals("enText2", ((IInternationalString)values.get(1)).get(Locale.ENGLISH));
    }
}
