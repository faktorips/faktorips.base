/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.Currency;

import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.controller.EditField;

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
        super(textControl, MoneyFormat.newInstance(defaultCurrency));
        currencySymbolPainter = new CurrencySymbolPainter(getFormat());
        textControl.addPaintListener(currencySymbolPainter);
    }

    @Override
    public MoneyFormat getFormat() {
        return (MoneyFormat)super.getFormat();
    }

}
