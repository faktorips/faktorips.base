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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.faktorips.values.DefaultInternationalString;
import org.junit.After;
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

    @After
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

    @Test

    public void testNullContent() throws Exception {
        is = createInputStream("WithNull.xml");
        saxParser.parse(is, handler);
        List<List<Object>> enumValueList = handler.getEnumValueList();
        assertEquals(1, enumValueList.size());
        List<Object> values = enumValueList.get(0);
        assertEquals(2, values.size());
        assertNull(values.get(0));
        assertEquals("", values.get(1));
    }

    /**
     * Tests if the handler works correctly if the characters() method will be called multiple times
     * for a tag content. This happens for example if the value within a tag contains carriage
     * return characters.
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

    @Test
    public void testInternationalContent() throws Exception {
        is = createInputStream("International.xml");
        saxParser.parse(is, handler);

        List<List<Object>> enumValueList = handler.getEnumValueList();
        assertEquals(2, enumValueList.size());
        List<Object> values = enumValueList.get(0);
        assertEquals(2, values.size());
        assertEquals("a", values.get(0));
        assertTrue(values.get(1) instanceof DefaultInternationalString);
        DefaultInternationalString i11lString = (DefaultInternationalString)values.get(1);
        assertEquals(Locale.GERMAN, i11lString.getDefaultLocale());
        assertEquals("deText", i11lString.get(Locale.GERMAN));
        assertEquals("enText", i11lString.get(Locale.ENGLISH));
        assertEquals("deText", i11lString.get(Locale.CHINESE));

        values = enumValueList.get(1);
        assertEquals(2, values.size());
        assertEquals("b", values.get(0));
        assertTrue(values.get(1) instanceof DefaultInternationalString);
        i11lString = (DefaultInternationalString)values.get(1);
        assertEquals(Locale.ENGLISH, i11lString.getDefaultLocale());
        assertEquals("deText2", i11lString.get(Locale.GERMAN));
        assertEquals("enText2", i11lString.get(Locale.ENGLISH));
        assertEquals("enText2", i11lString.get(Locale.JAPANESE));
    }

    @Test
    public void testInternationalContentWithoutDefaultLocale() throws Exception {
        is = createInputStream("InternationalWithoutDefaultLocale.xml");
        saxParser.parse(is, handler);

        List<List<Object>> enumValueList = handler.getEnumValueList();
        assertTrue(enumValueList.get(0).get(1) instanceof DefaultInternationalString);
        DefaultInternationalString i11lString = (DefaultInternationalString)enumValueList.get(0).get(1);
        assertEquals(Locale.getDefault(), i11lString.getDefaultLocale());
        // defect xml -> use default locale. All we test here is that we can still read it and get
        // an international string with a default locale.

        assertTrue(enumValueList.get(1).get(1) instanceof DefaultInternationalString);
        i11lString = (DefaultInternationalString)enumValueList.get(1).get(1);
        assertEquals(Locale.getDefault(), i11lString.getDefaultLocale());
    }
}
