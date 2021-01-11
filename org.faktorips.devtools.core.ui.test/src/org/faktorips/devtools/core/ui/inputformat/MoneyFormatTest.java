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
import static org.junit.Assert.fail;

import java.util.Currency;
import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.junit.Before;
import org.junit.Test;

public class MoneyFormatTest extends AbstractIpsPluginTest {

    private MoneyFormat moneyFormat;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        moneyFormat = new MoneyFormat(Currency.getInstance("EUR"));
    }

    @Test
    public void testParseStringBoolean() {
        moneyFormat.initFormat(Locale.GERMANY);
        String input = "1";
        String parsed = moneyFormat.parse(input);
        assertEquals("1.00 EUR", parsed);

        input = "-1";
        parsed = moneyFormat.parse(input);
        assertEquals("-1.00 EUR", parsed);

        input = "0";
        parsed = moneyFormat.parse(input);
        assertEquals("0.00 EUR", parsed);

        input = "-0";
        parsed = moneyFormat.parse(input);
        assertEquals("0.00 EUR", parsed);

        input = "0,12";
        parsed = moneyFormat.parse(input);
        assertEquals("0.12 EUR", parsed);

        input = "-0,23";
        parsed = moneyFormat.parse(input);
        assertEquals("-0.23 EUR", parsed);

        input = "-123,23";
        parsed = moneyFormat.parse(input);
        assertEquals("-123.23 EUR", parsed);

        input = "1.000";
        parsed = moneyFormat.parse(input);
        assertEquals("1000.00 EUR", parsed);

        input = "1000,00";
        parsed = moneyFormat.parse(input);
        assertEquals("1000.00 EUR", parsed);

        input = "100.0";
        parsed = moneyFormat.parse(input);
        assertEquals("1000.00 EUR", parsed);

        input = "illegal";
        parsed = moneyFormat.parse(input);
        assertEquals("illegal", parsed);

        input = ",1,12";
        parsed = moneyFormat.parse(input);
        assertEquals(",1,12", parsed);

        try {
            input = "1,123";
            parsed = moneyFormat.parse(input);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }

        moneyFormat.initFormat(Locale.US);
        input = "1";
        parsed = moneyFormat.parse(input);
        assertEquals("1.00 EUR", parsed);

        input = "0,12";
        parsed = moneyFormat.parse(input);
        assertEquals("12.00 EUR", parsed);

        input = "0.12";
        parsed = moneyFormat.parse(input);
        assertEquals("0.12 EUR", parsed);

        input = "-0.23";
        parsed = moneyFormat.parse(input);
        assertEquals("-0.23 EUR", parsed);

        input = "-123.23";
        parsed = moneyFormat.parse(input);
        assertEquals("-123.23 EUR", parsed);

        input = "1,000";
        parsed = moneyFormat.parse(input);
        assertEquals("1000.00 EUR", parsed);

        input = "1000.00";
        parsed = moneyFormat.parse(input);
        assertEquals("1000.00 EUR", parsed);

        input = "100,0";
        parsed = moneyFormat.parse(input);
        assertEquals("1000.00 EUR", parsed);

        input = "100,0 €";
        parsed = moneyFormat.parse(input);
        assertEquals("1000.00 EUR", parsed);

        input = "illegal";
        parsed = moneyFormat.parse(input);
        assertEquals("illegal", parsed);

        input = ".1.12";
        parsed = moneyFormat.parse(input);
        assertEquals(".1.12", parsed);

        try {
            input = "1.123";
            parsed = moneyFormat.parse(input);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testFormatT() {
        moneyFormat.initFormat(Locale.GERMANY);
        String input = "1 EUR";
        String formated = moneyFormat.format(input);
        assertEquals("1,00", formated);

        input = "1.0EUR";
        formated = moneyFormat.format(input);
        assertEquals("1,00", formated);

        input = "1.23 EUR";
        formated = moneyFormat.format(input);
        assertEquals("1,23", formated);

        input = "1.23 USD";
        formated = moneyFormat.format(input);
        assertEquals("1,23", formated);

        input = "-1.23 USD";
        formated = moneyFormat.format(input);
        assertEquals("-1,23", formated);

        input = "-1.23 USD";
        formated = moneyFormat.format(input);
        assertEquals("-1,23", formated);

        input = "0 USD";
        formated = moneyFormat.format(input);
        assertEquals("0,00", formated);

        input = "123123.1 USD";
        formated = moneyFormat.format(input);
        assertEquals("123.123,10", formated);

        moneyFormat.setAddCurrencySymbol(true);
        input = "1.23 EUR";
        formated = moneyFormat.format(input);
        assertEquals("1,23 €", formated);

        moneyFormat.setAddCurrencySymbol(false);
        moneyFormat.initFormat(Locale.US);
        input = "1 EUR";
        formated = moneyFormat.format(input);
        assertEquals("1.00", formated);

        input = "1.0 EUR";
        formated = moneyFormat.format(input);
        assertEquals("1.00", formated);

        input = "1.23EUR";
        formated = moneyFormat.format(input);
        assertEquals("1.23", formated);

        input = "1.23 USD";
        formated = moneyFormat.format(input);
        assertEquals("1.23", formated);

        input = "123123.1USD";
        formated = moneyFormat.format(input);
        assertEquals("123,123.10", formated);

        input = "illegalValue";
        formated = moneyFormat.format(input);
        assertEquals("illegalValue", formated);

        input = null;
        formated = moneyFormat.format(input);
        assertEquals(moneyFormat.getNullString(), formated);

        input = "";
        formated = moneyFormat.format(input);
        assertEquals(moneyFormat.getNullString(), formated);

    }

    @Test
    public void testUpdateCurrentCurrency() {
        moneyFormat.initFormat(Locale.US);
        moneyFormat.formatInternal("1.23 USD");
        moneyFormat.updateCurrentCurrency("USD");
        assertEquals(Currency.getInstance("USD"), moneyFormat.getCurrency());

        moneyFormat.initFormat(Locale.GERMANY);
        moneyFormat.setAddCurrencySymbol(true);
        moneyFormat.formatInternal("1.23 EUR");
        moneyFormat.updateCurrentCurrency("€");
        assertEquals(Currency.getInstance("EUR"), moneyFormat.getCurrency());

        moneyFormat.updateCurrentCurrency("EUR");
        assertEquals(Currency.getInstance("EUR"), moneyFormat.getCurrency());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateCurrentCurrency_IAE() {
        moneyFormat.updateCurrentCurrency("illegalValue");
    }

    @Test
    public void test_getEnteredCurrency_CurrencyCode() {
        String[] enteredCurrency = moneyFormat.splitStringToBeParsed("2,000,000.30EUR");

        assertEquals("2,000,000.30", enteredCurrency[0]);
        assertEquals("EUR", enteredCurrency[1]);
    }

    @Test
    public void test_getEnteredCurrency_Symbol() {
        String[] enteredCurrency = moneyFormat.splitStringToBeParsed("2.98€");

        assertEquals("2.98", enteredCurrency[0]);
        assertEquals("€", enteredCurrency[1]);
    }

    @Test
    public void test_getEnteredCurrency_TestWhitespace() {
        String[] enteredCurrency = moneyFormat.splitStringToBeParsed("2,000,000.30 EUR");

        assertEquals("2,000,000.30", enteredCurrency[0]);
        assertEquals("EUR", enteredCurrency[1]);
    }

    @Test
    public void test_getEnteredCurrency_invalidRegextChar() {
        String[] enteredCurrency = moneyFormat.splitStringToBeParsed("2,000,000.30EUR");

        enteredCurrency = moneyFormat.splitStringToBeParsed("2(€");

        assertEquals("2", enteredCurrency[0]);
        assertEquals("(€", enteredCurrency[1]);
    }
}
