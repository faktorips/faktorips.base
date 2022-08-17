/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.Messages;
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
        assertEquals(format.getNullString(), formated);

        input = "";
        formated = format.format(input);
        assertEquals(format.getNullString(), formated);
    }

    @Test
    public void testNullStringRep() {
        DateISOStringFormat format = new DateISOStringFormat();
        format.initFormat(Locale.GERMANY);
        format.setNullString(Messages.DefaultValueRepresentation_EditField);
        String input = "";
        String formated = format.format(input);
        assertEquals(Messages.DefaultValueRepresentation_EditField, formated);

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
        assertEquals(format.getNullString(), formated);

        input = "";
        formated = format.format(input);
        assertEquals(format.getNullString(), formated);
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

    @Test
    public void testMapDateToObject() throws Exception {
        DateISOStringFormat format = new DateISOStringFormat();
        format.initFormat(Locale.GERMANY);

        GregorianCalendar cal = new GregorianCalendar(2000, 0, 1);
        Date date = cal.getTime();
        String object = format.mapDateToObject(date);

        assertEquals("2000-01-01", object);
    }

    @Test
    public void testMapObjectToDate() throws Exception {
        DateISOStringFormat format = new DateISOStringFormat();
        format.initFormat(Locale.GERMANY);

        GregorianCalendar cal = new GregorianCalendar(2000, 0, 1);
        Date expectedDate = cal.getTime();

        Date actDate = format.mapObjectToDate("2000-01-01");
        assertEquals(expectedDate, actDate);
    }

}
