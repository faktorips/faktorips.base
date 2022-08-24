/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import java.util.Currency;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.MoneyDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.values.Money;

/**
 * Modifies an input string to fit the specified format.
 */
public enum ValueConverter {

    /**
     * Converts the input to a money string by adding the default currency abbreviation defined in
     * the {@link IIpsProject}.
     */
    TO_MONEY {

        @Override
        public ValueDatatype targetType() {
            return ValueDatatype.MONEY;
        }

        @Override
        public String convert(String input, IIpsProject project) {
            Currency currency = project.getReadOnlyProperties().getDefaultCurrency();
            MoneyDatatype moneyDT = ValueDatatype.MONEY;
            try {
                Money value = moneyDT.getValue(input + currency.getCurrencyCode());
                return value.toString();
            } catch (IllegalArgumentException e) {
                return input;
            }
        }

    };

    public abstract ValueDatatype targetType();

    public abstract String convert(String input, IIpsProject project);

    public static ValueConverter getByTargetType(ValueDatatype type) {
        for (ValueConverter converter : values()) {
            if (converter.targetType().equals(type)) {
                return converter;
            }
        }
        return null;
    }
}
