/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;

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
        if (datatype == null) {
            return false;
        }
        if (!(datatype instanceof EnumTypeDatatypeAdapter)) {
            return false;
        }
        IEnumType enumType = ((EnumTypeDatatypeAdapter)datatype).getEnumType();
        return enumType.isExtensible();
    }

}
