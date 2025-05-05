/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Locale;

import org.faktorips.values.LocalizedString;
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

        LocalizedString stringGe2 = new LocalizedString(Locale.of("de"), new String(value));

        assertEquals(stringGe1, stringGe2);
        assertEquals(stringGe2, stringGe1);
        assertEquals(stringGe1.hashCode(), stringGe2.hashCode());

        LocalizedString stringEn1 = new LocalizedString(Locale.ENGLISH, value);
        assertFalse(stringEn1.equals(stringGe1));
        assertFalse(stringGe1.equals(stringEn1));

        LocalizedString stringGeOtherText = new LocalizedString(Locale.of("de"), value + "x");
        assertFalse(stringGe1.equals(stringGeOtherText));
        assertFalse(stringGeOtherText.equals(stringGe1));
    }

}
