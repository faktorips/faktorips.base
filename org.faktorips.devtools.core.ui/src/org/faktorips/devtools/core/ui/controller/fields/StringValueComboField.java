/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.IpsPlugin;

public class StringValueComboField extends ComboField<String> {

    public StringValueComboField(Combo combo) {
        super(combo);
    }

    @Override
    public String parseContent() {
        return prepareObjectForGet(getText());
    }

    @Override
    public void setValue(String newValue) {
        setText(prepareObjectForSet(newValue));
    }

    /**
     * Returns the null-representation-string defined by the user (see IpsPreferences) if the given
     * object is <code>null</code>, the unmodified object otherwise.
     * 
     */
    public String prepareObjectForSet(String object) {
        if (object == null && supportsNullStringRepresentation()) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        }
        return "" + object; //$NON-NLS-1$
    }

    /**
     * Returns <code>null</code> if the given value is the null-representation-string, the
     * unmodified value otherwise.
     */
    public String prepareObjectForGet(String value) {
        if (supportsNullStringRepresentation()
                && IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(value)) {
            return null;
        }
        return value;
    }

}
