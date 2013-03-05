/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.enums;

import java.util.Locale;

import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.core.ui.controls.InternationalStringDialogHandler;
import org.faktorips.devtools.core.ui.controls.tableedit.IElementModifier;
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
        LocalizedString result = content.get(getLocale());
        return result;
    }

    private IInternationalString getInternationalString(IEnumValue element) {
        IEnumAttributeValue enumAttributeValue = element.getEnumAttributeValues().get(columnIndex);
        IInternationalString content = (IInternationalString)enumAttributeValue.getValue().getContent();
        return content;
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