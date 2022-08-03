/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enums;

import java.util.Locale;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.controls.InternationalStringDialogHandler;
import org.faktorips.devtools.core.ui.controls.tableedit.IElementModifier;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.values.LocalizedString;

/**
 * The cell modifier for string values in {@link IEnumValue enum values}
 */
public class EnumInternationalStringCellModifier implements IElementModifier<IEnumValue, LocalizedString> {

    private final int columnIndex;

    private final Locale locale;

    public EnumInternationalStringCellModifier(int columnIndex, Locale locale) {
        this.columnIndex = columnIndex;
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public LocalizedString getValue(IEnumValue element) {
        IInternationalString content = getInternationalString(element);
        return content.get(getLocale());
    }

    private IInternationalString getInternationalString(IEnumValue element) {
        IEnumAttributeValue enumAttributeValue = element.getEnumAttributeValues().get(columnIndex);
        return (IInternationalString)enumAttributeValue.getValue().getContent();
    }

    @Override
    public void setValue(IEnumValue element, LocalizedString value) {
        IInternationalString internationalString = getInternationalString(element);
        if (internationalString == null) {
            IEnumAttributeValue enumAttributeValue = element.getEnumAttributeValues().get(columnIndex);
            enumAttributeValue.setValue(ValueFactory.createValue(true, value.getValue()));
            internationalString = getInternationalString(element);
        }
        internationalString.add(value);
    }

    public InternationalStringDialogHandler getDialogHandler(Shell shell, IEnumValue enumValue) {
        final IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValues().get(columnIndex);
        return new InternationalStringDialogHandler(shell, enumAttributeValue) {

            @Override
            protected IInternationalString getInternationalString() {
                return (IInternationalString)enumAttributeValue.getValue().getContent();
            }
        };
    }

}
