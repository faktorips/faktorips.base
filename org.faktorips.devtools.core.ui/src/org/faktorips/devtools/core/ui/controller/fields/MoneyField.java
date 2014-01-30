/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.Currency;

import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.inputformat.MoneyFormat;

/**
 * This class is a {@link EditField} for money values. The combo control is populated with the
 * currency objects returned by IpsUIPlugin#getCurrencies(). The text control is managed by a
 * FormattedTextField with decimal format.
 * <p/>
 * This field in essence is a "composite" field. It uses a field for each the text and the combo
 * control and forwards events sent by them to its own listeners.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class MoneyField extends FormattingTextField<String> {

    protected boolean immediatelyNotifyListener = false;

    private CurrencySymbolPainter currencySymbolPainter;

    public MoneyField(Text textControl, Currency defaultCurrency) {
        this(textControl, defaultCurrency, true);
    }

    public MoneyField(Text textControl, Currency defaultCurrency, boolean formatOnFocusLost) {
        super(textControl, MoneyFormat.newInstance(defaultCurrency), formatOnFocusLost);
        currencySymbolPainter = new CurrencySymbolPainter(getFormat());
        textControl.addPaintListener(currencySymbolPainter);
    }

    @Override
    public MoneyFormat getFormat() {
        return (MoneyFormat)super.getFormat();
    }

}
