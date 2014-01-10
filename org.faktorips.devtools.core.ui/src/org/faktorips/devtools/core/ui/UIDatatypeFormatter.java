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

package org.faktorips.devtools.core.ui;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.DatatypeFormatter;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.inputformat.ValueSetFormat;

public class UIDatatypeFormatter {

    /**
     * Formats the given value according to the user preferences.
     * <p>
     * Note that this method delegates some calls to {@link DatatypeFormatter} in the core plugin.
     * Eventually the {@link DatatypeFormatter}-code should be moved to this class. See MTS#530
     * <p>
     * Supports the null-presentation mechanism.
     * 
     * @param datatype The data type the value is a value of.
     * @param value The value as string
     */
    public String formatValue(ValueDatatype datatype, String value) {
        return IpsUIPlugin.getDefault().getInputFormat(datatype, null).format(value);
    }

    /**
     * @see ValueSetFormat
     */
    public String formatValueSet(IValueSet valueSet) {
        return ValueSetFormat.newInstance(valueSet.getValueSetOwner()).format(valueSet);
    }

}
