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

package org.faktorips.util;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import junit.framework.TestCase;

/**
 *
 */
public class LocalizedStringsSetTest extends TestCase {

    private LocalizedStringsSet set;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        set = new LocalizedStringsSet(this.getClass()); // in this case the class name is the set's
                                                        // name.
    }

    /*
     * Class under test for String getLocalizedString(String, Locale, Object[])
     */
    public void testGetLocalizedStringStringLocaleObjectArray() {
        GregorianCalendar calendar = new GregorianCalendar(2004, 11, 20);
        Date date = calendar.getTime();
        Double x = new Double(9.34);
        Object[] replacements = new Object[] { date, x };

        assertEquals("Date=12/20/04, x=9.34", set.getString("replacements", Locale.ENGLISH, replacements));
        assertEquals("Datum=20.12.04, x=9,34", set.getString("replacements", Locale.GERMAN, replacements));
    }

    /*
     * Class under test for String getLocalizedString(String, Locale, Object)
     */
    public void testGetLocalizedStringStringLocaleObject() {
        assertEquals("The answer is 42", set.getString("singleReplacement", Locale.ENGLISH, new Integer(42)));
        assertEquals("Die Antwort ist 42", set.getString("singleReplacement", Locale.GERMAN, new Integer(42)));
    }

    /*
     * Class under test for String getLocalizedString(String, Locale)
     */
    public void testGetLocalizedStringStringLocale() {
        assertEquals("Hello world!", set.getString("simple", Locale.ENGLISH));
        assertEquals("Hallo Welt!", set.getString("simple", Locale.GERMAN));
    }

}
