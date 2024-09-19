/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import org.faktorips.datatype.AbstractPrimitiveDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumType;

/**
 * Collection of utility methods for data types.
 *
 * @author Jan Ortmann
 */
public class DatatypeUtil {

    private DatatypeUtil() {

    }

    /**
     * Returns <code>true</code> if the given data type is an extensible enumeration type. Returns
     * <code>false</code> if data type is <code>null</code>.
     */
    public static final boolean isExtensibleEnumType(Datatype datatype) {
        if ((datatype == null) || !(datatype instanceof EnumTypeDatatypeAdapter)) {
            return false;
        }
        IEnumType enumType = ((EnumTypeDatatypeAdapter)datatype).getEnumType();
        return enumType.isExtensible();
    }

    /**
     * Returns <code>true</code> if the first data type is covariant to the second one, which means
     * it is either the same type or it is a subtype of the second one.
     *
     * @param datatype1 The data type that is checked to be covariant to the second one
     * @param datatype2 The second data type to check
     *
     * @return <code>true</code> if the first data type is covariant to the second one
     */
    public static final boolean isCovariant(ValueDatatype datatype1, ValueDatatype datatype2) {
        if (datatype1 == null || datatype2 == null) {
            return false;
        } else if (datatype1.equals(datatype2)) {
            return true;
        } else {
            if (datatype1 instanceof EnumTypeDatatypeAdapter enumTypeDatatypeAdapter) {
                return enumTypeDatatypeAdapter.isCovariant(datatype2);
            } else {
                return false;
            }
        }
    }

    public static boolean isNullValue(ValueDatatype datatype, String value) {
        return value == null || datatype.isNull(value);
    }

    public static boolean isPrimitiveNullValue(ValueDatatype datatype, String value) {
        if (datatype instanceof AbstractPrimitiveDatatype) {
            return datatype.isNull(value);
        }
        return false;
    }

    public static boolean isNonNull(ValueDatatype datatype, String... values) {
        for (String value : values) {
            if (isNullValue(datatype, value)) {
                return false;
            }
        }
        return true;
    }

}
