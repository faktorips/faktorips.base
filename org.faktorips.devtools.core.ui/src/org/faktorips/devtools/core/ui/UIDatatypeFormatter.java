/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.DatatypeFormatter;
import org.faktorips.devtools.core.ui.inputformat.AnyValueSetFormat;
import org.faktorips.devtools.model.valueset.IValueSet;

public class UIDatatypeFormatter {

    /**
     * Formats the given value according to the user preferences.
     * <p>
     * Note that this method delegates some calls to {@link DatatypeFormatter} in the core plugin.
     * Eventually the {@link DatatypeFormatter}-code should be moved to this class. See MTS#530
     * <p>
     * Supports the null-presentation mechanism.
     * 
     * @param datatype The data type the value is a value of. If datatype is null the method returns
     *            the value unchanged.
     * @param value The value as string
     */
    public String formatValue(ValueDatatype datatype, String value) {
        return IpsUIPlugin.getDefault().getInputFormat(datatype, null).format(value);
    }

    /**
     * @see AnyValueSetFormat
     */
    public String formatValueSet(IValueSet valueSet) {
        return AnyValueSetFormat.newInstance(valueSet.getValueSetOwner()).format(valueSet);
    }

}
