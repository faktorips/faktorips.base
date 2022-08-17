/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.controls.tableedit.IElementModifier;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.devtools.model.value.ValueFactory;

/**
 * Allows to access and modify {@link SingleValueHolder} instances.
 * 
 * @author Stefan Widmaier
 */
public class StringMultiValueElementModifier implements IElementModifier<SingleValueViewItem, String> {

    /**
     * Assumes the given element is a {@link SingleValueHolder}. Returns its string value.
     * {@inheritDoc}
     */
    @Override
    public String getValue(SingleValueViewItem element) {
        ISingleValueHolder item = element.getSingleValueHolder();
        if (item == null || item.getValue() == null) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        }
        return (String)item.getValue().getContent();
    }

    /**
     * Assumes the given element is a {@link SingleValueHolder} and the given value is a
     * {@link String}. Sets the given string as new value of the given element. {@inheritDoc}
     */
    @Override
    public void setValue(SingleValueViewItem element, String value) {
        ISingleValueHolder singleValueHolder = element.getSingleValueHolder();
        singleValueHolder.setValue(ValueFactory.createStringValue(value));
    }

}
