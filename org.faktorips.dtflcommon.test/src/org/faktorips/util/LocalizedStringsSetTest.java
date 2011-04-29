/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.util;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

public class LocalizedStringsSetTest {

    private LocalizedStringsSet set;

    @Before
    public void setUp() throws Exception {
        // in this case the class name is the set's name.
        set = new LocalizedStringsSet(this.getClass());
    }

    @Test
    public void testGetLocalizedStringStringLocaleObjectArray() {
        GregorianCalendar calendar = new GregorianCalendar(2004, 11, 20);
        Date date = calendar.getTime();
        Double x = new Double(9.34);
        Object[] replacements = new Object[] { date, x };

        assertEquals("Date=12/20/04, x=9.34", set.getString("replacements", Locale.ENGLISH, replacements)); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Datum=20.12.04, x=9,34", set.getString("replacements", Locale.GERMAN, replacements)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetLocalizedStringStringLocaleObject() {
        assertEquals("The answer is 42", set.getString("singleReplacement", Locale.ENGLISH, new Integer(42))); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Die Antwort ist 42", set.getString("singleReplacement", Locale.GERMAN, new Integer(42))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetLocalizedStringStringLocale() {
        assertEquals("Hello world!", set.getString("simple", Locale.ENGLISH)); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Hallo Welt!", set.getString("simple", Locale.GERMAN)); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
