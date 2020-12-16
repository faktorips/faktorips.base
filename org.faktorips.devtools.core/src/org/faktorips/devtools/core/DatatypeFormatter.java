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

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.PrimitiveBooleanDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.BooleanDatatype;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.util.ArgumentCheck;

/**
 * Note that this class is to be moved to the IpsIUPlugin. Wherever possible call
 * IpsUIPlugin.getDatatypeFormatter() and use it instead of this class.
 * 
 */
public class DatatypeFormatter {

    private IpsPreferences preferences;

    public DatatypeFormatter(IpsPreferences ipsPreferences) {
        ArgumentCheck.notNull(ipsPreferences, this);
        preferences = ipsPreferences;
    }

    /**
     * Formats the given value according to the user preferences.
     * 
     * @param datatype The data type the value is a value of.
     * @param value The value as string
     */
    public String formatValue(ValueDatatype datatype, String value) {
        if (value == null) {
            return preferences.getNullPresentation();
        }
        if (datatype == null) {
            return value;
        }
        if (datatype instanceof EnumTypeDatatypeAdapter) {
            return formatValue((EnumTypeDatatypeAdapter)datatype, value);
        }
        if (datatype instanceof EnumDatatype) {
            return formatValue((EnumDatatype)datatype, value);
        }
        if (datatype instanceof BooleanDatatype || datatype instanceof PrimitiveBooleanDatatype) {
            if (Boolean.valueOf(value).booleanValue()) {
                return Messages.DatatypeFormatter_booleanTrue;
            }
            return Messages.DatatypeFormatter_booleanFalse;
        }
        return value;
    }

    /**
     * Formats the given value according to the user preferences.
     * 
     * @param datatype The data type the value is a value of.
     */
    private String formatValue(EnumDatatype datatype, String id) {
        if (!datatype.isSupportingNames()) {
            return id;
        }
        EnumTypeDisplay enumTypeDisplay = preferences.getEnumTypeDisplay();
        if (enumTypeDisplay.equals(EnumTypeDisplay.ID)) {
            return id;
        }
        if (!datatype.isParsable(id)) {
            return id;
        }
        String name = datatype.getValueName(id);
        if (name == null) {
            return id;
        }
        if (enumTypeDisplay.equals(EnumTypeDisplay.NAME)) {
            return name;
        }
        if (enumTypeDisplay.equals(EnumTypeDisplay.NAME_AND_ID)) {
            return name + " (" + id + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return id;
    }

    /**
     * Returns the to be displayed text of an enumeration. The property ENUM_TYPE_DISPLAY specifies
     * how the name and id will be formated. E.g. display only id or only name, or display both.
     */
    public String getFormatedEnumText(String id, String name) {
        EnumTypeDisplay enumTypeDisplay = preferences.getEnumTypeDisplay();
        if (enumTypeDisplay.equals(EnumTypeDisplay.NAME_AND_ID)) {
            return name + " (" + id + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        } else if (enumTypeDisplay.equals(EnumTypeDisplay.NAME)) {
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
