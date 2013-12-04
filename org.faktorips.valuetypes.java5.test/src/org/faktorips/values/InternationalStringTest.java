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

package org.faktorips.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class InternationalStringTest {
    private static final String ENGLISH_TEXT = "english text";
    private static final String GERMAN_TEXT = "deutscher Text";

    private InternationalString internationalString;
    private LocalizedString englishLocalizedString;
    private LocalizedString germanLocalizedString;

    @Before
    public void setUp() {
        englishLocalizedString = new LocalizedString(Locale.ENGLISH, ENGLISH_TEXT);
        germanLocalizedString = new LocalizedString(Locale.GERMAN, GERMAN_TEXT);
        List<LocalizedString> list = new ArrayList<LocalizedString>();
        list.add(englishLocalizedString);
        list.add(germanLocalizedString);
        internationalString = new InternationalString(list);
    }

    @Test
    public void testGetter() {
        assertEquals(ENGLISH_TEXT, internationalString.get(Locale.ENGLISH));
        assertEquals(GERMAN_TEXT, internationalString.get(Locale.GERMAN));
        assertNull(internationalString.get(Locale.CHINESE));
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
        List<LocalizedString> list = new ArrayList<LocalizedString>();
        list.add(english2);
        list.add(german2);
        IInternationalString internationalString2 = new InternationalString(list);

        assertEquals(internationalString, internationalString2);
        assertEquals(internationalString2, internationalString);
        assertEquals(internationalString.hashCode(), internationalString2.hashCode());

        list.add(new LocalizedString(Locale.CHINESE, "abc"));
        internationalString2 = new InternationalString(list);
        assertFalse(internationalString.equals(internationalString2));
        assertFalse(internationalString2.equals(internationalString));

        list.clear();
        list.add(german2);
        internationalString2 = new InternationalString(list);
        assertFalse(internationalString.equals(internationalString2));
        assertFalse(internationalString2.equals(internationalString));
    }

    private Object serializeAndDeserialize() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(outputStream);
        objOut.writeObject(internationalString);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        ObjectInputStream objIn = new ObjectInputStream(inputStream);
        Object readObject = objIn.readObject();
        return readObject;
    }

    @Test
    public void testSerializable() throws Exception {
        Object readObject = serializeAndDeserialize();
        assertEquals(internationalString, readObject);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableMap() throws Exception {
        Object readObject = serializeAndDeserialize();

        Field internalField = ((IInternationalString)readObject).getClass().getDeclaredField("localizedStringMap");
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
