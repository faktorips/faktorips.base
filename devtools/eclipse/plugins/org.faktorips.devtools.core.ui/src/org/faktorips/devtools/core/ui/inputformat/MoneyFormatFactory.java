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

import java.util.Currency;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Factory that creates a MoneyFormat for a data type with the default currency
 * 
 */
public class MoneyFormatFactory implements IDatatypeInputFormatFactory {

    @Override
    public IInputFormat<String> newInputFormat(ValueDatatype datatype, IIpsProject ipsProject) {
        Currency defaultCurrency = null;
        if (ipsProject != null) {
            defaultCurrency = ipsProject.getReadOnlyProperties().getDefaultCurrency();
        }
        MoneyFormat moneyFormat = MoneyFormat.newInstance(defaultCurrency);
        moneyFormat.setAddCurrencySymbol(true);
        return moneyFormat;
    }
}
