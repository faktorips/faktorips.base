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

import java.util.Currency;
import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;

public class MoneyFormatTest extends AbstractIpsPluginTest {

    private MoneyFormat moneyFormat;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        moneyFormat = new MoneyFormat(Currency.getInstance("EUR"));
    }

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
        assertEquals(null, parsed);

        input = ",1,12";
        parsed = moneyFormat.parse(input);
        assertEquals(null, parsed);

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

        input = "illegal";
        parsed = moneyFormat.parse(input);
        assertEquals(null, parsed);

        input = ".1.12";
        parsed = moneyFormat.parse(input);
        assertEquals(null, parsed);

        try {
            input = "1.123";
            parsed = moneyFormat.parse(input);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }
    }

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

    }

}
