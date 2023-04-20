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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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

        EnumContent enumContent = handler.getEnumContent();
        List<List<Object>> enumValueList = enumContent.getEnumValues();

        assertThat(enumContent.getDescription(), is(notNullValue()));
        assertThat(enumContent.getDescription().get(Locale.ENGLISH), is("english"));
        assertThat(enumContent.getDescription().get(Locale.GERMAN), is("deutsch"));
        assertThat(enumValueList.size(), is(3));
        List<Object> enumAttributeValues = enumValueList.get(0);
        assertThat(enumAttributeValues.get(0), is("a"));
        assertThat(enumAttributeValues.get(1), is("an"));
        enumAttributeValues = enumValueList.get(1);
        assertThat(enumAttributeValues.get(0), is("b"));
        assertThat(enumAttributeValues.get(1), is("bn"));
        enumAttributeValues = enumValueList.get(2);
        assertThat(enumAttributeValues.get(0), is("c"));
        assertThat(enumAttributeValues.get(1), is("cn"));
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

        EnumContent enumContent = handler.getEnumContent();
        List<List<Object>> enumValueList = enumContent.getEnumValues();

        assertThat(enumContent.getDescription(), is(notNullValue()));
        assertThat(enumContent.getDescription().get(Locale.ENGLISH), is("english"));
        assertThat(enumContent.getDescription().get(Locale.GERMAN), is("deutsch"));
        assertThat(enumValueList.size(), is(1));
        List<Object> values = enumValueList.get(0);
        assertThat(values.size(), is(2));
        assertThat(values.get(0), is(nullValue()));
        assertThat(values.get(1), is(""));
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

        EnumContent enumContent = handler.getEnumContent();
        List<List<Object>> enumValueList = enumContent.getEnumValues();

        assertThat(enumContent.getDescription(), is(notNullValue()));
        assertThat(enumContent.getDescription().get(Locale.ENGLISH), is("english"));
        assertThat(enumContent.getDescription().get(Locale.GERMAN), is("deutsch"));
        assertThat(enumValueList.size(), is(1));
        List<Object> values = enumValueList.get(0);
        assertThat(values.size(), is(1));
        Object value = values.get(0);
        assertThat(value, is("a\rb\rc"));
    }

    @Test
    public void testInternationalContent() throws Exception {
        is = createInputStream("International.xml");
        saxParser.parse(is, handler);

        EnumContent enumContent = handler.getEnumContent();
        List<List<Object>> enumValueList = enumContent.getEnumValues();

        assertThat(enumContent.getDescription(), is(notNullValue()));
        assertThat(enumContent.getDescription().get(Locale.ENGLISH), is("english"));
        assertThat(enumContent.getDescription().get(Locale.GERMAN), is("deutsch"));
        assertThat(enumContent.getDescription(), is(instanceOf(DefaultInternationalString.class)));
        assertThat(((DefaultInternationalString)enumContent.getDescription()).getDefaultLocale(), is(Locale.GERMAN));
        assertThat(enumValueList.size(), is(2));
        List<Object> values = enumValueList.get(0);
        assertThat(values.size(), is(2));
        assertThat(values.get(0), is("a"));
        assertThat(values.get(1), is(instanceOf(DefaultInternationalString.class)));
        DefaultInternationalString i11lString = (DefaultInternationalString)values.get(1);
        assertThat(i11lString.getDefaultLocale(), is(Locale.GERMAN));
        assertThat(i11lString.get(Locale.GERMAN), is("deText"));
        assertThat(i11lString.get(Locale.ENGLISH), is("enText"));
        assertThat(i11lString.get(Locale.CHINESE), is("deText"));
        values = enumValueList.get(1);
        assertThat(values.size(), is(2));
        assertThat(values.get(0), is("b"));
        assertThat(values.get(1), is(instanceOf(DefaultInternationalString.class)));
        i11lString = (DefaultInternationalString)values.get(1);
        assertThat(i11lString.getDefaultLocale(), is(Locale.ENGLISH));
        assertThat(i11lString.get(Locale.GERMAN), is("deText2"));
        assertThat(i11lString.get(Locale.ENGLISH), is("enText2"));
        assertThat(i11lString.get(Locale.JAPANESE), is("enText2"));
    }

    @Test
    public void testInternationalContentWithoutDefaultLocale() throws Exception {
        is = createInputStream("InternationalWithoutDefaultLocale.xml");
        saxParser.parse(is, handler);

        EnumContent enumContent = handler.getEnumContent();
        List<List<Object>> enumValueList = enumContent.getEnumValues();

        assertThat(enumContent.getDescription(), is(notNullValue()));
        assertThat(enumContent.getDescription().get(Locale.ENGLISH), is("english"));
        assertThat(enumContent.getDescription().get(Locale.GERMAN), is("deutsch"));
        assertThat(enumValueList.get(0).get(1), is(instanceOf(DefaultInternationalString.class)));
        DefaultInternationalString i11lString = (DefaultInternationalString)enumValueList.get(0).get(1);
        assertThat(i11lString.getDefaultLocale(), is(Locale.getDefault()));
        // defect xml -> use default locale. All we test here is that we can still read it and get
        // an international string with a default locale.
        assertThat(enumValueList.get(1).get(1), is(instanceOf(DefaultInternationalString.class)));
        i11lString = (DefaultInternationalString)enumValueList.get(1).get(1);
        assertThat(i11lString.getDefaultLocale(), is(Locale.getDefault()));
    }

    @Test
    public void testEmptyDescription() throws Exception {
        is = createInputStream("NoDescription.xml");
        saxParser.parse(is, handler);

        EnumContent enumContent = handler.getEnumContent();
        enumContent.getEnumValues();

        assertThat(enumContent.getDescription(), is(notNullValue()));
        assertThat(enumContent.getDescription().get(Locale.ENGLISH), is(""));
        assertThat(enumContent.getDescription().get(Locale.GERMAN), is(""));
    }
}
