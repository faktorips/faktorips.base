package org.faktorips.datatype.classtypes;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsSame.sameInstance;

import java.util.Currency;
import java.util.Locale;

import org.junit.Test;

public class CurrencyDatatypeTest {

    @Test
    public void testGetValue() {
        CurrencyDatatype currencyType = new CurrencyDatatype();

        Currency usd = (Currency)currencyType.getValue("USD");

        assertThat(usd, is(notNullValue()));
    }

    @Test
    public void testGetValue_Null() {
        CurrencyDatatype currencyType = new CurrencyDatatype();

        Currency usd = (Currency)currencyType.getValue(null);

        assertThat(usd, is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValue_WrongValue_TooLong() {
        CurrencyDatatype currencyType = new CurrencyDatatype();

        currencyType.getValue("ABCDEFG");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValue_WrongValue_Unknown() {
        CurrencyDatatype currencyType = new CurrencyDatatype();

        currencyType.getValue("XYZ");
    }

    @Test
    public void testGetValueByName_UsingSymbol() {
        CurrencyDatatype currencyType = new CurrencyDatatype();

        Currency usd = (Currency)currencyType.getValueByName("$", Locale.US);

        assertThat(usd, is(notNullValue()));

        Currency autUsd = (Currency)currencyType.getValueByName("US$", Locale.forLanguageTag("AUT"));

        assertThat(autUsd, is(sameInstance(usd)));
    }

    @Test
    public void testGetValueByName_UsingName() {
        CurrencyDatatype currencyType = new CurrencyDatatype();

        Currency usd = (Currency)currencyType.getValueByName("US Dollar", Locale.US);

        assertThat(usd, is(notNullValue()));

        Currency germanUsd = (Currency)currencyType.getValueByName("US-Dollar", Locale.GERMAN);

        assertThat(germanUsd, is(sameInstance(usd)));
    }

    @Test
    public void testGetValueByName_UsingNameAndSymbol() {
        CurrencyDatatype currencyType = new CurrencyDatatype();

        Currency usd = (Currency)currencyType.getValueByName("US Dollar: $", Locale.US);

        assertThat(usd, is(notNullValue()));

        Currency germanUsd = (Currency)currencyType.getValueByName("US-Dollar: $", Locale.GERMAN);

        assertThat(germanUsd, is(sameInstance(usd)));

        Currency autUsd = (Currency)currencyType.getValueByName("US Dollar: US$", Locale.forLanguageTag("AUT"));

        assertThat(autUsd, is(sameInstance(usd)));
    }

    @Test
    public void testGetValueByName_NullLocale() {
        CurrencyDatatype currencyType = new CurrencyDatatype();

        Currency usd = (Currency)currencyType.getValueByName("$", null);

        assertThat(usd, is(notNullValue()));
    }

    @Test
    public void testGetValueByName_DefaultLocale() {
        CurrencyDatatype currencyType = new CurrencyDatatype();

        Currency usd = (Currency)currencyType.getValueByName("$");

        assertThat(usd, is(notNullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueByName_InValid() {
        CurrencyDatatype currencyType = new CurrencyDatatype();

        currencyType.getValueByName("$", Locale.forLanguageTag("AUT"));
    }

    @Test
    public void testGetValueName_DefaultLocale() {
        CurrencyDatatype currencyType = new CurrencyDatatype();

        assertThat(currencyType.getValueName("USD"), is("US Dollar: $"));
    }

    @Test
    public void testGetValueName_NullLocale() {
        CurrencyDatatype currencyType = new CurrencyDatatype();

        assertThat(currencyType.getValueName("USD", null), is("US Dollar: $"));
    }

    @Test
    public void testGetValueName() {
        CurrencyDatatype currencyType = new CurrencyDatatype();

        assertThat(currencyType.getValueName("USD", Locale.forLanguageTag("AUT")), is("US Dollar: US$"));
    }

    @Test
    public void testGetValueName_Null() {
        CurrencyDatatype currencyType = new CurrencyDatatype();

        assertThat(currencyType.getValueName(null), is(nullValue()));
    }
}
