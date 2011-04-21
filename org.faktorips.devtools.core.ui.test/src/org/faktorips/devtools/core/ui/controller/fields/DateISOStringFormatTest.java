/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controller.fields;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.junit.Test;

public class DateISOStringFormatTest extends AbstractIpsPluginTest {

    @Test
    public void testFormatGermanLocale() {
        DateISOStringFormat format = new DateISOStringFormat();
        format.initFormat(Locale.GERMANY);

        String input;
        String formated;

        input = "1900-02-01";
        formated = format.format(input);
        assertEquals("01.02.1900", formated);

        input = null;
        formated = format.format(input);
        assertEquals(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation(), formated);

        input = "";
        formated = format.format(input);
        assertEquals(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation(), formated);
    }

    @Test
    public void testFormatUsLocale() {
        DateISOStringFormat format = new DateISOStringFormat();
        format.initFormat(Locale.US);

        String input;
        String formated;

        input = "1900-02-01";
        formated = format.format(input);
        assertEquals("02/01/1900", formated);

        input = null;
        formated = format.format(input);
        assertEquals(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation(), formated);

        input = "";
        formated = format.format(input);
        assertEquals(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation(), formated);
    }

    @Test
    public void testParseGermanLocale() throws Exception {
        DateISOStringFormat format = new DateISOStringFormat();
        format.initFormat(Locale.GERMANY);

        String input;
        String formated;

        input = "01.02.1900";
        formated = format.parse(input);
        assertEquals("1900-02-01", formated);

        input = "1.2.1970";
        formated = format.parse(input);
        assertEquals("1970-02-01", formated);

        input = "1.2.1972";
        formated = format.parse(input);
        assertEquals("1972-02-01", formated);

        input = "1.2.2013";
        formated = format.parse(input);
        assertEquals("2013-02-01", formated);

        input = "";
        formated = format.parse(input);
        assertEquals(null, formated);

        input = "";
        formated = format.parse(input);
        assertEquals(null, formated);
    }

}
