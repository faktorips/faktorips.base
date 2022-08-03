/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class DefaultInternationalStringTest {

    private static final Locale DEFAULT_LOCALE = Locale.GERMAN;
    private static final String ENGLISH_TEXT = "english text";
    private static final String GERMAN_TEXT = "deutscher Text";
    private static final String KOREAN_TEXT = "'koreanischer' Text";

    private DefaultInternationalString internationalString;
    private LocalizedString englishLocalizedString;
    private LocalizedString germanLocalizedString;
    private LocalizedString koreanLocalizedString;

    @Before
    public void setUp() {
        englishLocalizedString = new LocalizedString(Locale.ENGLISH, ENGLISH_TEXT);
        germanLocalizedString = new LocalizedString(Locale.GERMAN, GERMAN_TEXT);
        koreanLocalizedString = new LocalizedString(Locale.KOREAN, KOREAN_TEXT);
        List<LocalizedString> list = new ArrayList<>();
        list.add(englishLocalizedString);
        list.add(germanLocalizedString);
        internationalString = new DefaultInternationalString(list, DEFAULT_LOCALE);
    }

    @Test
    public void testGetter() {
        assertEquals(ENGLISH_TEXT, internationalString.get(Locale.ENGLISH));
        assertEquals(ENGLISH_TEXT, internationalString.get(Locale.UK));
        assertEquals(ENGLISH_TEXT, internationalString.get(Locale.US));
        assertEquals(GERMAN_TEXT, internationalString.get(Locale.GERMAN));
        assertEquals(GERMAN_TEXT, internationalString.get(Locale.GERMANY));
        assertEquals(GERMAN_TEXT, internationalString.get(Locale.CHINESE));
        assertEquals(GERMAN_TEXT, internationalString.get(new Locale("")));
        DefaultInternationalString internationalString2 = new DefaultInternationalString(Arrays.asList(
                koreanLocalizedString, englishLocalizedString), Locale.KOREAN);
        assertEquals(KOREAN_TEXT, internationalString2.get(Locale.KOREAN));
        assertEquals(KOREAN_TEXT, internationalString2.get(Locale.KOREA));
        assertEquals(ENGLISH_TEXT, internationalString2.get(Locale.ENGLISH));
        assertEquals(ENGLISH_TEXT, internationalString2.get(Locale.UK));
        assertEquals(ENGLISH_TEXT, internationalString2.get(Locale.US));
        assertEquals(KOREAN_TEXT, internationalString2.get(Locale.GERMAN));
        assertEquals(KOREAN_TEXT, internationalString2.get(Locale.GERMANY));
        assertEquals(KOREAN_TEXT, internationalString2.get(Locale.CHINESE));
        assertEquals(KOREAN_TEXT, internationalString2.get(new Locale("")));
    }

    @Test
    public void testGetLocalizedStrings() {
        Collection<LocalizedString> contents = internationalString.getLocalizedStrings();
        assertNotNull(contents);
        assertEquals(2, contents.size());
        Iterator<LocalizedString> iterator = contents.iterator();
        assertEquals(englishLocalizedString, iterator.next());
        assertEquals(germanLocalizedString, iterator.next());
        contents.add(new LocalizedString(Locale.FRENCH, "abc"));
        assertEquals(2, internationalString.getLocalizedStrings().size());
    }

    @Test
    public void testEqualsAndHashCode() throws Exception {
        assertEquals(internationalString, internationalString);
        assertEquals(internationalString.hashCode(), internationalString.hashCode());

        LocalizedString english2 = new LocalizedString(new Locale("en"), new String(ENGLISH_TEXT));
        LocalizedString german2 = new LocalizedString(new Locale("de"), new String(GERMAN_TEXT));
        List<LocalizedString> list = new ArrayList<>();
        list.add(english2);
        list.add(german2);
        InternationalString internationalString2 = new DefaultInternationalString(list, DEFAULT_LOCALE);

        assertEquals(internationalString, internationalString2);
        assertEquals(internationalString2, internationalString);
        assertEquals(internationalString.hashCode(), internationalString2.hashCode());

        list.add(new LocalizedString(Locale.CHINESE, "abc"));
        internationalString2 = new DefaultInternationalString(list, DEFAULT_LOCALE);
        assertFalse(internationalString.equals(internationalString2));
        assertFalse(internationalString2.equals(internationalString));

        list.clear();
        list.add(german2);
        internationalString2 = new DefaultInternationalString(list, DEFAULT_LOCALE);
        assertFalse(internationalString.equals(internationalString2));
        assertFalse(internationalString2.equals(internationalString));
    }

    private Object serializeAndDeserialize() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(outputStream);
        objOut.writeObject(internationalString);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        ObjectInputStream objIn = new ObjectInputStream(inputStream);
        return objIn.readObject();
    }

    @Test
    public void testSerializable() throws Exception {
        Object readObject = serializeAndDeserialize();
        assertEquals(internationalString, readObject);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableMap() throws Exception {
        Object readObject = serializeAndDeserialize();

        Field internalField = ((InternationalString)readObject).getClass().getDeclaredField("localizedStringMap");
        assertNotNull(internalField);
        internalField.setAccessible(true);
        @SuppressWarnings("unchecked")
        // We know that international strings use a map of locales and localized Strings.
        Map<Locale, LocalizedString> internalMap = (Map<Locale, LocalizedString>)internalField.get(readObject);
        assertNotNull(internalMap);
        assertEquals(2, internalMap.size());
        internalMap.put(Locale.CHINESE, new LocalizedString(Locale.CHINESE, "abc"));
    }
}
