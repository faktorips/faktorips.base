/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
        if (object == null && supportsNull()) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        }
        return "" + object; //$NON-NLS-1$
    }

    /**
     * Returns <code>null</code> if the given value is the null-representation-string, the
     * unmodified value otherwise.
     */
    public String prepareObjectForGet(String value) {
        if (supportsNull() && IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(value)) {
            return null;
        }
        return value;
    }

}
