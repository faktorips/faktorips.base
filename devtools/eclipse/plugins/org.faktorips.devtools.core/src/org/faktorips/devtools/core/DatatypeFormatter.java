/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core;

import org.faktorips.devtools.model.plugin.IDatatypeFormatter;
import org.faktorips.devtools.model.plugin.NamedDataTypeDisplay;
import org.faktorips.util.ArgumentCheck;

/**
 * Note that this class is to be moved to the IpsIUPlugin. Wherever possible call
 * IpsUIPlugin.getDatatypeFormatter() and use it instead of this class.
 * 
 */
public class DatatypeFormatter implements IDatatypeFormatter {

    private IpsPreferences preferences;

    public DatatypeFormatter(IpsPreferences ipsPreferences) {
        ArgumentCheck.notNull(ipsPreferences, this);
        preferences = ipsPreferences;
    }

    @Override
    public String getNullPresentation() {
        return preferences.getNullPresentation();
    }

    @Override
    public NamedDataTypeDisplay getNamedDataTypeDisplay() {
        return preferences.getNamedDataTypeDisplay();
    }

    /**
     * Returns the to be displayed text of an enumeration. The property ENUM_TYPE_DISPLAY specifies
     * how the name and id will be formated. E.g. display only id or only name, or display both.
     */
    public String getFormatedEnumText(String id, String name) {
        NamedDataTypeDisplay dataTypeDisplay = preferences.getNamedDataTypeDisplay();
        if (dataTypeDisplay.equals(NamedDataTypeDisplay.NAME_AND_ID)) {
            return name + " (" + id + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        } else if (dataTypeDisplay.equals(NamedDataTypeDisplay.NAME)) {
            return name;
        } else {
            return id;
        }
    }

    public String getBooleanTrueDisplay() {
        return Messages.DatatypeFormatter_booleanTrue;
    }

    public String getBooleanFalseDisplay() {
        return Messages.DatatypeFormatter_booleanFalse;
    }

}
