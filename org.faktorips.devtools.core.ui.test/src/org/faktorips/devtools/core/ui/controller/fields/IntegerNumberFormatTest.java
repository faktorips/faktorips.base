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

import java.util.Locale;

import junit.framework.TestCase;

import org.faktorips.datatype.ValueDatatype;

public class IntegerNumberFormatTest extends TestCase {

    private IntegerNumberFormat numberFormat;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        numberFormat = new IntegerNumberFormat(ValueDatatype.LONG);
    }

    public void testParseStringBoolean() {
        numberFormat.initFormat(Locale.GERMANY);
        String input = "1";
        String parsed = numberFormat.parse(input);
        assertEquals("1", parsed);

        input = "-1";
        parsed = numberFormat.parse(input);
        assertEquals("-1", parsed);

        input = "0";
        parsed = numberFormat.parse(input);
        assertEquals("0", parsed);

        input = "-0";
        parsed = numberFormat.parse(input);
        assertEquals("0", parsed);

        input = "1.000";
        parsed = numberFormat.parse(input);
        assertEquals("1000", parsed);

        input = "0.12";
        parsed = numberFormat.parse(input);
        assertEquals("12", parsed);

        input = "-0.23";
        parsed = numberFormat.parse(input);
        assertEquals("-23", parsed);

        input = "-123.23";
        parsed = numberFormat.parse(input);
        assertEquals("-12323", parsed);

        input = "1.000";
        parsed = numberFormat.parse(input);
        assertEquals("1000", parsed);

        input = "1000";
        parsed = numberFormat.parse(input);
        assertEquals("1000", parsed);

        input = "100.0";
        parsed = numberFormat.parse(input);
        assertEquals("1000", parsed);

        input = "123,123";
        parsed = numberFormat.parse(input);
        assertEquals(null, parsed);

        input = ",1,12";
        parsed = numberFormat.parse(input);
        assertEquals(null, parsed);

        input = "1.123";
        parsed = numberFormat.parse(input);
        assertEquals("1123", parsed);

        numberFormat.initFormat(Locale.US);
        input = "1";
        parsed = numberFormat.parse(input);
        assertEquals("1", parsed);

        input = "-1";
        parsed = numberFormat.parse(input);
        assertEquals("-1", parsed);

        input = "0";
        parsed = numberFormat.parse(input);
        assertEquals("0", parsed);

        input = "-0";
        parsed = numberFormat.parse(input);
        assertEquals("0", parsed);

        input = "0,12";
        parsed = numberFormat.parse(input);
        assertEquals("12", parsed);

        input = "-0,23";
        parsed = numberFormat.parse(input);
        assertEquals("-23", parsed);

        input = "-123,23";
        parsed = numberFormat.parse(input);
        assertEquals("-12323", parsed);

        input = "1,000";
        parsed = numberFormat.parse(input);
        assertEquals("1000", parsed);

        input = "1000.00";
        parsed = numberFormat.parse(input);
        assertEquals(null, parsed);

        input = "100,0";
        parsed = numberFormat.parse(input);
        assertEquals("1000", parsed);

        input = "illegal";
        parsed = numberFormat.parse(input);
        assertEquals(null, parsed);

        input = ".1.12";
        parsed = numberFormat.parse(input);
        assertEquals(null, parsed);

        input = "1.123";
        parsed = numberFormat.parse(input);
        assertEquals(null, parsed);
    }

    public void testFormatT() {
        numberFormat.initFormat(Locale.GERMANY);
        String input = "1";
        String formated = numberFormat.format(input);
        assertEquals("1", formated);

        input = "-1";
        formated = numberFormat.format(input);
        assertEquals("-1", formated);

        input = "0";
        formated = numberFormat.format(input);
        assertEquals("0", formated);

        input = "1000";
        formated = numberFormat.format(input);
        assertEquals("1000", formated);

        input = "1231231";
        formated = numberFormat.format(input);
        assertEquals("1231231", formated);

        input = "1.0";
        formated = numberFormat.format(input);
        assertEquals("1.0", formated);

        input = "1,0";
        formated = numberFormat.format(input);
        assertEquals("1,0", formated);

        numberFormat.initFormat(Locale.US);
        input = "1";
        formated = numberFormat.format(input);
        assertEquals("1", formated);

        input = "-1";
        formated = numberFormat.format(input);
        assertEquals("-1", formated);

        input = "0";
        formated = numberFormat.format(input);
        assertEquals("0", formated);

        input = "1000";
        formated = numberFormat.format(input);
        assertEquals("1000", formated);

        input = "1231231";
        formated = numberFormat.format(input);
        assertEquals("1231231", formated);

        input = "1.0";
        formated = numberFormat.format(input);
        assertEquals("1.0", formated);

        input = "1,0";
        formated = numberFormat.format(input);
        assertEquals("1,0", formated);

    }
}
