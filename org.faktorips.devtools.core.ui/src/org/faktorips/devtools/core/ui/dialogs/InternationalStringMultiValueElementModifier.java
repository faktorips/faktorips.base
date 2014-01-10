/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.ui.controls.tableedit.IElementModifier;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;
import org.faktorips.values.LocalizedString;

/**
 * Allows to access and modify {@link SingleValueHolder} instances for international strings.
 */
public class InternationalStringMultiValueElementModifier implements
        IElementModifier<SingleValueViewItem, LocalizedString> {

    /**
     * Assumes the given element is a {@link SingleValueHolder}. Returns its string value.
     * {@inheritDoc}
     */
    @Override
    public LocalizedString getValue(SingleValueViewItem element) {
        IInternationalString internationalString = getInternationalString(element);
        if (internationalString != null) {
            LocalizedString locString = internationalString.get(IpsPlugin.getMultiLanguageSupport()
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
     * {@link LocalizedString}. Sets the given string as new value of the given element.
     * {@inheritDoc}
     */
    @Override
    public void setValue(SingleValueViewItem element, LocalizedString value) {
        if (value == null) {
            return;
        }
        IInternationalString internationalString = getInternationalString(element);
        if (internationalString != null) {
            internationalString.add(value);
        }
    }

}
