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

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.controller.fields.ICurrencyHolder;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;

/**
 * An input format for money datatypes.
 * 
 * @author dirmeier
 */
public class MoneyFormat extends AbstractInputFormat<String> implements ICurrencyHolder {

    private static final String CURRENCY_SYMBOL_EURO = "EUR"; //$NON-NLS-1$

    private static final String CURRENCY_SEPARATOR = " "; //$NON-NLS-1$

    private static final String VALID_AMOUNT_CHARS = "[-,.\\d]"; //$NON-NLS-1$

    private static Map<String, Currency> currencySymbols = new ConcurrentHashMap<>(4, 0.9f, 1);

    private DecimalNumberFormat amountFormat;

    private Currency currentCurrency;

    private boolean addCurrencySymbol = false;

    private Locale locale;

    protected MoneyFormat(Currency defaultCurrency) {
        super(IpsStringUtils.EMPTY, IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormattingLocale());
        currentCurrency = defaultCurrency;
    }

    public static MoneyFormat newInstance(Currency defaultCurrency) {
        MoneyFormat instance = new MoneyFormat(defaultCurrency);
        instance.initFormat();
        return instance;
    }

    @Override
    protected void initFormat(Locale locale) {
        assert locale != null;
        this.locale = locale;
        amountFormat = new DecimalNumberFormat(ValueDatatype.BIG_DECIMAL);
        amountFormat.initFormat(locale);
        setCurrentCurrency(currentCurrency);
    }

    @Override
    protected String formatInternal(String value) {
        Money money = Money.valueOf(value);
        if (money != Money.NULL) {
            setCurrentCurrency(money.getCurrency());
            String formattedAmount = amountFormat.getNumberFormat().format(money.getAmount());
            if (addCurrencySymbol && currentCurrency != null) {
                formattedAmount += CURRENCY_SEPARATOR + getCurrencySymbol();
            }
            return formattedAmount;
        } else {
            return null;
        }
    }

    /**
     * JAVA9 updated some localization settings. In this particular case, replaced the correct
     * {@code EUR} symbol with the wrong &#8364; symbol. According to ISO the &#8364; symbol is only
     * allowed in graphical representations.
     * 
     * @return the currency symbol configured in the {@link Locale}, or the {@code EUR} sign if the
     *             used {@link Currency} is Euro and the {@link Locale} has no country configured.
     */
    private String getCurrencySymbol() {
        if (Money.EUR.equals(currentCurrency) && locale.getCountry().isBlank()) {
            return CURRENCY_SYMBOL_EURO;
        }
        return currentCurrency.getSymbol(locale);
    }

    private void updateCurrencySumbols() {
        currencySymbols.put(currentCurrency.getSymbol(locale), currentCurrency);
    }

    @Override
    protected String parseInternal(String stringToBeParsed) {
        if (stringToBeParsed.isEmpty()) {
            // this is important to show null representation when the text field is empty
            return stringToBeParsed;
        }

        String amount;
        String[] splittedString = splitStringToBeParsed(stringToBeParsed);
        if (!IpsStringUtils.isEmpty(splittedString[0]) && !IpsStringUtils.isEmpty(splittedString[1])) {
            amount = amountFormat.parse(splittedString[0]);
            try {
                updateCurrentCurrency(splittedString[1]);
            } catch (IllegalArgumentException e) {
                return null;
            }
        } else {
            amount = amountFormat.parse(stringToBeParsed);
        }
        try {
            Decimal decimalAmount = Decimal.valueOf(amount);
            Money money = Money.valueOf(decimalAmount, currentCurrency);
            if (money != Money.NULL) {
                return money.toString();
            } else {
                return null;
            }
        } catch (NumberFormatException e) {
            return stringToBeParsed;
        }
    }

    protected String[] splitStringToBeParsed(String value) {
        String currency = value.replaceAll(VALID_AMOUNT_CHARS, IpsStringUtils.EMPTY).trim();
        String[] splittedString = new String[2];
        splittedString[0] = value.replace(currency, IpsStringUtils.EMPTY).trim();
        splittedString[1] = currency;
        return splittedString;
    }

    protected void updateCurrentCurrency(String currencyString) {
        if (currencySymbols.get(currencyString) != null) {
            String currencyCode = currencySymbols.get(currencyString).getCurrencyCode();
            currentCurrency = Currency.getInstance(currencyCode);
        } else {
            currentCurrency = Currency.getInstance(currencyString);
        }
    }

    public void setCurrentCurrency(Currency actualCurrency) {
        currentCurrency = actualCurrency;
        if (actualCurrency != null) {
            amountFormat.getNumberFormat().setCurrency(getCurrentCurrency());
            amountFormat.getNumberFormat().setMaximumFractionDigits(getCurrentCurrency().getDefaultFractionDigits());
            amountFormat.getNumberFormat().setMinimumFractionDigits(getCurrentCurrency().getDefaultFractionDigits());
            updateCurrencySumbols();
        }
    }

    public Currency getCurrentCurrency() {
        return currentCurrency;
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        amountFormat.verifyInternal(e, resultingText);

        if (e.doit) {
            try {
                BigDecimal number = (BigDecimal)amountFormat.getNumberFormat().parse(resultingText);
                e.doit = (number.scale() <= currentCurrency.getDefaultFractionDigits());
            } catch (ParseException e1) {
                e.doit = true;
            }

        }
        // allow entering another currency
        if (!e.doit) {
            if (resultingText.lastIndexOf(CURRENCY_SEPARATOR) == resultingText.length() - 1) {
                e.doit = true;
            }
            String[] split = resultingText.split(CURRENCY_SEPARATOR);
            if (split.length != 2) {
                return;
            }
            if (isParsable(amountFormat.getNumberFormat(), split[0]) && split[1].length() <= 3) {
                e.doit = true;
            }
        }
    }

    @Override
    public Currency getCurrency() {
        return currentCurrency;
    }

    public void setAddCurrencySymbol(boolean addCurrencySymbol) {
        this.addCurrencySymbol = addCurrencySymbol;
    }

    public boolean isAddCurrencySymbol() {
        return addCurrencySymbol;
    }

    protected static Map<String, Currency> getUsedCurrencies() {
        return currencySymbols;
    }

}
