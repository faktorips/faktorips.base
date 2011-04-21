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

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;

import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;

/**
 * An input format for money datatypes.
 * 
 * @author dirmeier
 */
public class MoneyFormat extends AbstractInputFormat<String> implements ICurrencyHolder {

    private static final String CURRENCY_SEPARATOR = " "; //$NON-NLS-1$

    private DecimalNumberFormat amountFormat;

    private Currency actualCurrency;

    private boolean addCurrencySymbol = false;

    private Locale locale;

    public static MoneyFormat newInstance(Currency defaultCurrency) {
        MoneyFormat instance = new MoneyFormat(defaultCurrency);
        instance.initFormat();
        return instance;
    }

    protected MoneyFormat(Currency defaultCurrency) {
        actualCurrency = defaultCurrency;
    }

    @Override
    protected void initFormat(Locale locale) {
        this.locale = locale;
        amountFormat = new DecimalNumberFormat(ValueDatatype.BIG_DECIMAL);
        amountFormat.initFormat(locale);
        setActualCurrency(actualCurrency);
    }

    @Override
    protected String formatInternal(String value) {
        Money money = Money.valueOf(value);
        if (money != Money.NULL) {
            setActualCurrency(money.getCurrency());
            String formattedAmount = amountFormat.getNumberFormat().format(money.getAmount());
            if (addCurrencySymbol && actualCurrency != null) {
                formattedAmount += ' ' + actualCurrency.getSymbol(locale);
            }
            return formattedAmount;
        } else {
            return null;
        }
    }

    @Override
    protected String parseInternal(String stringToBeParsed) {
        if (stringToBeParsed.isEmpty()) {
            // this is important to show null representation when the text field is empty
            return stringToBeParsed;
        }

        String amount;
        if (stringToBeParsed.contains(CURRENCY_SEPARATOR)) {
            // text seems to contain currency
            String[] split = stringToBeParsed.split(CURRENCY_SEPARATOR);
            amount = amountFormat.parse(split[0]);
            try {
                actualCurrency = Currency.getInstance(split[1]);
            } catch (IllegalArgumentException e) {
                return null;
            }
        } else {
            amount = amountFormat.parse(stringToBeParsed);
        }
        try {
            Decimal decimalAmount = Decimal.valueOf(amount);
            Money money = Money.valueOf(decimalAmount, actualCurrency);
            if (money != Money.NULL) {
                return money.toString();
            } else {
                return null;
            }
        } catch (NumberFormatException e) {
            return stringToBeParsed;
        }
    }

    public void setActualCurrency(Currency actualCurrency) {
        this.actualCurrency = actualCurrency;
        if (actualCurrency != null) {
            amountFormat.getNumberFormat().setCurrency(getActualCurrency());
            amountFormat.getNumberFormat().setMaximumFractionDigits(getActualCurrency().getDefaultFractionDigits());
            amountFormat.getNumberFormat().setMinimumFractionDigits(getActualCurrency().getDefaultFractionDigits());
        }
    }

    public Currency getActualCurrency() {
        return actualCurrency;
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        amountFormat.verifyInternal(e, resultingText);

        if (e.doit) {
            try {
                BigDecimal number = (BigDecimal)amountFormat.getNumberFormat().parse(resultingText);
                e.doit = (number.scale() <= actualCurrency.getDefaultFractionDigits());
            } catch (ParseException e1) {
                // ignore
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
        return actualCurrency;
    }

    public void setAddCurrencySymbol(boolean addCurrencySymbol) {
        this.addCurrencySymbol = addCurrencySymbol;
    }

    public boolean isAddCurrencySymbol() {
        return addCurrencySymbol;
    }

}
