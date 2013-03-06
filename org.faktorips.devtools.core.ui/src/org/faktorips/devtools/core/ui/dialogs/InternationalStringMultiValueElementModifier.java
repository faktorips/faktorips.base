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

package org.faktorips.devtools.core.ui.dialogs;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.ui.controls.tableedit.IElementModifier;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;

/**
 * Allows to access and modify {@link SingleValueHolder} instances for international strings.
 */
public class InternationalStringMultiValueElementModifier implements
        IElementModifier<SingleValueViewItem, ILocalizedString> {

    /**
     * Assumes the given element is a {@link SingleValueHolder}. Returns its string value.
     * {@inheritDoc}
     */
    @Override
    public ILocalizedString getValue(SingleValueViewItem element) {
        IInternationalString internationalString = getInternationalString(element);
        if (internationalString != null) {
            ILocalizedString locString = internationalString.get(IpsPlugin.getMultiLanguageSupport()
                    .getLocalizationLocaleOrDefault(element.getSingleValueHolder().getIpsProject()));
            return locString;
        }
        return null;
    }

    private IInternationalString getInternationalString(SingleValueViewItem element) {
        SingleValueHolder item = element.getSingleValueHolder();
        if (item == null || item.getValue() == null) {
            return null;
        }
        Object content = item.getValue().getContent();
        if (content instanceof IInternationalString) {
            return (IInternationalString)content;
        } else {
            throw new IllegalArgumentException("Unsupported type " //$NON-NLS-1$
                    + (content == null ? "<null>" : content.getClass().getName()) //$NON-NLS-1$
                    + " in modifier found. Must be IInternationalString."); //$NON-NLS-1$            
        }
    }

    /**
     * Assumes the given element is a {@link SingleValueHolder} and the given value is a
     * {@link ILocalizedString}. Sets the given string as new value of the given element.
     * {@inheritDoc}
     */
    @Override
    public void setValue(SingleValueViewItem element, ILocalizedString value) {
        if (value == null) {
            return;
        }
        IInternationalString internationalString = getInternationalString(element);
        if (internationalString != null) {
            internationalString.add(value);
        }
    }

}
