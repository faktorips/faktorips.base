/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Locale;

import org.junit.Test;

public class LocalizedStringTest {

    @Test
    public void testGetter() throws Exception {
        String string = "anyString";
        Locale locale = Locale.GERMAN;
        LocalizedString localizedString = new LocalizedString(locale, string);

        assertEquals(string, localizedString.getValue());
        assertEquals(locale, localizedString.getLocale());
    }

    @Test
    public void testEqualsAndHashCode() throws Exception {
        String value = "testString";
        LocalizedString stringGe1 = new LocalizedString(Locale.GERMAN, value);
        assertEquals(stringGe1, stringGe1);
        assertEquals(stringGe1.hashCode(), stringGe1.hashCode());

        LocalizedString stringGe2 = new LocalizedString(new Locale("de"), new String(value));

        assertEquals(stringGe1, stringGe2);
        assertEquals(stringGe2, stringGe1);
        assertEquals(stringGe1.hashCode(), stringGe2.hashCode());

        LocalizedString stringEn1 = new LocalizedString(Locale.ENGLISH, value);
        assertFalse(stringEn1.equals(stringGe1));
        assertFalse(stringGe1.equals(stringEn1));

        LocalizedString stringGeOtherText = new LocalizedString(new Locale("de"), value + "x");
        assertFalse(stringGe1.equals(stringGeOtherText));
        assertFalse(stringGeOtherText.equals(stringGe1));
    }

}
