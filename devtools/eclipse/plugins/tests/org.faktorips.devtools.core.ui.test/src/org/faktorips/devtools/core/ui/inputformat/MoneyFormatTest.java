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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MoneyFormatTest extends AbstractIpsPluginTest {

    private MoneyFormat moneyFormat;

    @Override
    @BeforeEach
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

        // expected exception for an invalid fraction length
        assertThrows(IllegalArgumentException.class, () -> moneyFormat.parse("1,123"));

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

        // expected exception for an invalid fraction length
        assertThrows(IllegalArgumentException.class, () -> moneyFormat.parse("1.123"));
    }

    @Test
    public void test_parse_NonBreakingSpace() {
        // non-breaking space (U+00A0) between amount and currency, as left over when pasting a
        // formatted amount using the JDK's CLDR locale provider
        moneyFormat.initFormat(Locale.GERMANY);
        String input = "1" + ' ' + "EUR";
        String parsed = moneyFormat.parse(input);
        assertEquals("1.00 EUR", parsed);
    }

    @Test
    public void test_parse_NarrowNonBreakingSpaceGroupingSeparator() {
        // locales (e.g. French) may use the narrow non-breaking space (U+202F) as grouping
        // separator within the amount; use the locale's actual symbols so the test does not
        // depend on a particular CLDR data version using U+00A0 vs. U+202F
        Locale locale = Locale.FRANCE;
        moneyFormat.initFormat(locale);
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
        String input = "1" + symbols.getGroupingSeparator() + "234" + symbols.getDecimalSeparator() + "56"
                + ' ' + "EUR";
        String parsed = moneyFormat.parse(input);
        assertEquals("1234.56 EUR", parsed);
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

    @Test
    public void testUpdateCurrentCurrency_IAE() {
        assertThrows(IllegalArgumentException.class, () -> {
            moneyFormat.updateCurrentCurrency("illegalValue");
        });
    }

    @Test
    public void testGetEnteredCurrencyCurrencyCode() {
        String[] enteredCurrency = moneyFormat.splitStringToBeParsed("2,000,000.30EUR");

        assertEquals("2,000,000.30", enteredCurrency[0]);
        assertEquals("EUR", enteredCurrency[1]);
    }

    @Test
    public void testGetEnteredCurrencySymbol() {
        String[] enteredCurrency = moneyFormat.splitStringToBeParsed("2.98€");

        assertEquals("2.98", enteredCurrency[0]);
        assertEquals("€", enteredCurrency[1]);
    }

    @Test
    public void testGetEnteredCurrencyTestWhitespace() {
        String[] enteredCurrency = moneyFormat.splitStringToBeParsed("2,000,000.30 EUR");

        assertEquals("2,000,000.30", enteredCurrency[0]);
        assertEquals("EUR", enteredCurrency[1]);
    }

    @Test
    public void testGetEnteredCurrencyNonBreakingSpace() {
        // non-breaking space (U+00A0), as used by the JDK's CLDR locale provider as a
        // grouping/currency separator and left over when pasting a formatted amount
        String[] enteredCurrency = moneyFormat.splitStringToBeParsed("2,000,000.30 EUR");

        assertEquals("2,000,000.30", enteredCurrency[0]);
        assertEquals("EUR", enteredCurrency[1]);
    }

    @Test
    public void testGetEnteredCurrencyNarrowNonBreakingSpaceGroupingSeparator() {
        // narrow non-breaking space (U+202F), used as a grouping separator by some locales
        String[] enteredCurrency = moneyFormat.splitStringToBeParsed("2 000 000.30 EUR");

        assertEquals("2 000 000.30", enteredCurrency[0]);
        assertEquals("EUR", enteredCurrency[1]);
    }

    @Test
    public void testGetEnteredCurrencyInvalidRegextChar() {
        String[] enteredCurrency = moneyFormat.splitStringToBeParsed("2,000,000.30EUR");

        enteredCurrency = moneyFormat.splitStringToBeParsed("2(€");

        assertEquals("2", enteredCurrency[0]);
        assertEquals("(€", enteredCurrency[1]);
    }

    @Test
    public void testVerifyInternalFormatsCorrectlyAfterInput() {
        moneyFormat.initFormat(Locale.GERMANY);
        VerifyEvent verifyEvent = createVerifyEvent("1,2 EUR", 3, 3, "3");

        moneyFormat.verifyInternal(verifyEvent, "1,23 EUR");

        assertTrue(verifyEvent.doit);
    }

    @Test
    public void testVerifyInternalToManyDecimalPlaces() {
        moneyFormat.initFormat(Locale.GERMANY);
        VerifyEvent verifyEvent = createVerifyEvent("1,23 EUR", 4, 4, "4");

        moneyFormat.verifyInternal(verifyEvent, "1,234 EUR");

        assertFalse(verifyEvent.doit);
    }

    @Test
    public void testVerifyInternalParseException() {
        moneyFormat.initFormat(Locale.GERMANY);
        VerifyEvent verifyEvent = createVerifyEvent("", 0, 0, "-");

        moneyFormat.verifyInternal(verifyEvent, "-");

        assertTrue(verifyEvent.doit);
    }

    @Test
    public void testVerifyInternalCurrencyPartToLong() {
        moneyFormat.initFormat(Locale.GERMANY);
        VerifyEvent verifyEvent = createVerifyEvent("1,23 EUR", 8, 8, "O");

        moneyFormat.verifyInternal(verifyEvent, "1,23 EURO");

        assertFalse(verifyEvent.doit);
    }

    @Test
    public void testVerifyInternalWrongSeperator() {
        moneyFormat.initFormat(Locale.GERMANY);
        VerifyEvent verifyEvent = createVerifyEvent("1 EUR", 1, 1, ".");

        moneyFormat.verifyInternal(verifyEvent, "1. EUR");

        assertFalse(verifyEvent.doit);
    }

    private VerifyEvent createVerifyEvent(String currentText, int start, int end, String insertedText) {
        Shell shell = PlatformUI.getWorkbench().getDisplay().getShells()[0];
        Text text = new Text(shell, SWT.NONE);
        text.setText(currentText);

        Event event = new Event();
        event.widget = text;
        event.start = start;
        event.end = end;
        event.text = insertedText;
        event.doit = true;
        return new VerifyEvent(event);
    }
}
