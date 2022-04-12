/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.NamedDatatype;
import org.faktorips.datatype.PrimitiveBooleanDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.BooleanDatatype;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;

public interface IDatatypeFormatter {

    public static final String DEFAULT_NULL_REPRESENTATION = "<null>"; //$NON-NLS-1$

    /**
     * Formats the given value according to the user preferences.
     * 
     * @param datatype The data type the value is a value of.
     * @param value The value as string
     */
    default String formatValue(ValueDatatype datatype, String value) {
        if (value == null) {
            return getNullPresentation();
        }
        if (datatype == null) {
            return value;
        }
        if (datatype instanceof EnumTypeDatatypeAdapter) {
            return formatValue((EnumDatatype)datatype, value);
        }
        if (datatype instanceof NamedDatatype) {
            return formatValue((NamedDatatype)datatype, value);
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
    default String formatValue(NamedDatatype datatype, String id) {
        if (!datatype.isSupportingNames()) {
            return id;
        }
        NamedDataTypeDisplay dataTypeDisplay = getNamedDataTypeDisplay();
        if (dataTypeDisplay.equals(NamedDataTypeDisplay.ID)) {
            return id;
        }
        if (!datatype.isParsable(id)) {
            return id;
        }
        String name = datatype.getValueName(id);
        if (name == null) {
            return id;
        }
        if (dataTypeDisplay.equals(NamedDataTypeDisplay.NAME)) {
            return name;
        }
        if (dataTypeDisplay.equals(NamedDataTypeDisplay.NAME_AND_ID)) {
            return name + " (" + id + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return id;
    }

    default String getNullPresentation() {
        return DEFAULT_NULL_REPRESENTATION;
    }

    default NamedDataTypeDisplay getNamedDataTypeDisplay() {
        return NamedDataTypeDisplay.NAME_AND_ID;
    }

}
