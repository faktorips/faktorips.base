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

import java.util.Locale;

import org.faktorips.datatype.ValueDatatype;
import org.junit.Before;
import org.junit.Test;

public class IntegerNumberFormatTest {

    private IntegerNumberFormat numberFormat;

    @Before
    public void setUp() throws Exception {
        numberFormat = new IntegerNumberFormat(ValueDatatype.LONG);
    }

    @Test
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
        assertEquals("123,123", parsed);

        input = ",1,12";
        parsed = numberFormat.parse(input);
        assertEquals(",1,12", parsed);

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
        assertEquals("1000.00", parsed);

        input = "100,0";
        parsed = numberFormat.parse(input);
        assertEquals("1000", parsed);

        input = "illegal";
        parsed = numberFormat.parse(input);
        assertEquals("illegal", parsed);

        input = ".1.12";
        parsed = numberFormat.parse(input);
        assertEquals(".1.12", parsed);

        input = "1.123";
        parsed = numberFormat.parse(input);
        assertEquals("1.123", parsed);
    }

    @Test
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
