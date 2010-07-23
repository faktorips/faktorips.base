/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

    /**
     * Returns <code>true</code> if the given data type is an enumeration type with a separate
     * content containing the values. Returns <code>false</code> if data type is <code>null</code>.
     */
    public static final boolean isEnumTypeWithSeparateContent(Datatype datatype) {
        if (datatype == null) {
            return false;
        }
        if (!(datatype instanceof EnumTypeDatatypeAdapter)) {
            return false;
        }
        IEnumType enumType = ((EnumTypeDatatypeAdapter)datatype).getEnumType();
        return !enumType.isContainingValues();
    }

}
