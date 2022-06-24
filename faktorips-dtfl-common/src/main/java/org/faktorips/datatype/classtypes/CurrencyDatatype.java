/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.datatype.classtypes;

import java.text.MessageFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.NamedDatatype;
import org.faktorips.datatype.ValueClassNameDatatype;
import org.faktorips.runtime.internal.IpsStringUtils;

public class CurrencyDatatype extends ValueClassNameDatatype implements NamedDatatype {

    public static final CurrencyDatatype DATATYPE = new CurrencyDatatype();

    private static final String SYMBOL_SEPARATOR = ": "; //$NON-NLS-1$

    public CurrencyDatatype() {
        super(Currency.class.getSimpleName());
    }

    @Override
    public Object getValue(String currencyCode) {
        if (StringUtils.isNotBlank(currencyCode)) {
            return Currency.getInstance(currencyCode);
        }
        return null;
    }

    @Override
    public boolean supportsCompare() {
        return false;
    }

    @Override
    public boolean isSupportingNames() {
        return true;
    }

    @Override
    public String getValueName(String currencyCode) {
        return getValueName(currencyCode, null);
    }

    @Override
    public String getValueName(String currencyCode, Locale locale) {
        if (IpsStringUtils.isBlank(currencyCode)) {
            return null;
        }
        Currency currency = (Currency)getValue(currencyCode);
        Locale usedLocale = locale != null ? locale : Locale.getDefault(Category.DISPLAY);
        String symbol = currency.getSymbol(usedLocale);
        String displayName = currency.getDisplayName(usedLocale);
        return Objects.equals(symbol, currencyCode) ? displayName : displayName + SYMBOL_SEPARATOR + symbol;
    }

    @Override
    public Object getValueByName(String symbol) {
        return getValueByName(symbol, null);
    }

    @Override
    public Object getValueByName(String name, Locale locale) {
        if (IpsStringUtils.isBlank(name)) {
            return null;
        }
        Locale usedLocale = locale != null ? locale : Locale.getDefault(Category.DISPLAY);
        int i = name.indexOf(SYMBOL_SEPARATOR);
        String usedName = i > 0 ? name.substring(0, i) : name;
        Optional<Currency> currency = Currency.getAvailableCurrencies().stream()
                .filter(c -> StringUtils.equals(c.getDisplayName(usedLocale), usedName))
                .findFirst();
        if (currency.isEmpty()) {
            String symbol = i > 0 ? name.substring(i + 2) : name;
            currency = Currency.getAvailableCurrencies().stream()
                    .filter(c -> StringUtils.equals(c.getSymbol(usedLocale), symbol))
                    .findFirst();
        }
        return currency
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageFormat.format("{0} is not an valid curency name.", name))); //$NON-NLS-1$ ;
    }
}
