/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.ValueDatatype;
import org.junit.Test;

public class DecimalNumberFormatTest extends AbstractIpsPluginTest {

    private DecimalNumberFormat decimalFormat;

    @Test
    public void testParseDecimalGermanLocale() {
        decimalFormat = new DecimalNumberFormat(ValueDatatype.DECIMAL);
        decimalFormat.initFormat(Locale.GERMANY);
        String input = "1";
        String parsed = decimalFormat.parse(input);
        assertEquals("1", parsed);

        input = "-1";
        parsed = decimalFormat.parse(input);
        assertEquals("-1", parsed);

        input = "0";
        parsed = decimalFormat.parse(input);
        assertEquals("0", parsed);

        input = "-0";
        parsed = decimalFormat.parse(input);
        assertEquals("0", parsed);

        input = "0,12";
        parsed = decimalFormat.parse(input);
        assertEquals("0.12", parsed);

        input = "-0,23";
        parsed = decimalFormat.parse(input);
        assertEquals("-0.23", parsed);

        input = "-123,23";
        parsed = decimalFormat.parse(input);
        assertEquals("-123.23", parsed);

        input = "1.000";
        parsed = decimalFormat.parse(input);
        assertEquals("1000", parsed);

        input = "1000,00";
        parsed = decimalFormat.parse(input);
        assertEquals("1000.00", parsed);

        input = "100.0";
        parsed = decimalFormat.parse(input);
        assertEquals("1000", parsed);

        input = "illegal";
        parsed = decimalFormat.parse(input);
        assertEquals("illegal", parsed);

        input = ",1,12";
        parsed = decimalFormat.parse(input);
        assertEquals(",1,12", parsed);

        input = "1,123";
        parsed = decimalFormat.parse(input);
        assertEquals("1.123", parsed);
    }

    @Test
    public void testParseDecimalUsLocal() {
        String input;
        String parsed;
        decimalFormat = new DecimalNumberFormat(ValueDatatype.DECIMAL);
        decimalFormat.initFormat(Locale.US);

        input = "1";
        parsed = decimalFormat.parse(input);
        assertEquals("1", parsed);

        input = "-1";
        parsed = decimalFormat.parse(input);
        assertEquals("-1", parsed);

        input = "0";
        parsed = decimalFormat.parse(input);
        assertEquals("0", parsed);

        input = "-0";
        parsed = decimalFormat.parse(input);
        assertEquals("0", parsed);

        input = "0.12";
        parsed = decimalFormat.parse(input);
        assertEquals("0.12", parsed);

        input = "-0.23";
        parsed = decimalFormat.parse(input);
        assertEquals("-0.23", parsed);

        input = "-123.23";
        parsed = decimalFormat.parse(input);
        assertEquals("-123.23", parsed);

        input = "1,000";
        parsed = decimalFormat.parse(input);
        assertEquals("1000", parsed);

        input = "1000.00";
        parsed = decimalFormat.parse(input);
        assertEquals("1000.00", parsed);

        input = "100,0";
        parsed = decimalFormat.parse(input);
        assertEquals("1000", parsed);

        input = "illegal";
        parsed = decimalFormat.parse(input);
        assertEquals("illegal", parsed);

        input = ".1.12";
        parsed = decimalFormat.parse(input);
        assertEquals(".1.12", parsed);

        input = "1.123";
        parsed = decimalFormat.parse(input);
        assertEquals("1.123", parsed);
    }

    @Test
    public void testFormatDecimalGermanLocale() {
        decimalFormat = new DecimalNumberFormat(ValueDatatype.DECIMAL);
        decimalFormat.initFormat(Locale.GERMANY);
        String input = "1";
        String formated = decimalFormat.format(input);
        assertEquals("1", formated);

        input = "1.0";
        formated = decimalFormat.format(input);
        assertEquals("1", formated);

        input = "1.23";
        formated = decimalFormat.format(input);
        assertEquals("1,23", formated);

        input = "1.23";
        formated = decimalFormat.format(input);
        assertEquals("1,23", formated);

        input = "-1.23";
        formated = decimalFormat.format(input);
        assertEquals("-1,23", formated);

        input = "-1.23";
        formated = decimalFormat.format(input);
        assertEquals("-1,23", formated);

        input = "0.00";
        formated = decimalFormat.format(input);
        assertEquals("0", formated);

        input = "123123.1";
        formated = decimalFormat.format(input);
        assertEquals("123.123,1", formated);
    }

    @Test
    public void testFormatDecimalUsLocale() {
        String input;
        String formated;
        decimalFormat = new DecimalNumberFormat(ValueDatatype.DECIMAL);
        decimalFormat.initFormat(Locale.US);
        input = "1";
        formated = decimalFormat.format(input);
        assertEquals("1", formated);

        input = "0.00";
        formated = decimalFormat.format(input);
        assertEquals("0", formated);

        input = "1.0";
        formated = decimalFormat.format(input);
        assertEquals("1", formated);

        input = "1.23";
        formated = decimalFormat.format(input);
        assertEquals("1.23", formated);

        input = "1.23";
        formated = decimalFormat.format(input);
        assertEquals("1.23", formated);

        input = "123123.1";
        formated = decimalFormat.format(input);
        assertEquals("123,123.1", formated);

        input = "illegalValue";
        formated = decimalFormat.format(input);
        assertEquals("illegalValue", formated);

        input = "";
        formated = decimalFormat.format(input);
        assertEquals(decimalFormat.getNullString(), formated);

        input = null;
        formated = decimalFormat.format(input);
        assertEquals(decimalFormat.getNullString(), formated);

        input = "12345678901234567890";
        formated = decimalFormat.format(input);
        assertEquals("12,345,678,901,234,567,890", formated);
    }
}
