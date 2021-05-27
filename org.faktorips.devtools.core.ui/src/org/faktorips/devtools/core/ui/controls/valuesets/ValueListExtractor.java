/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.valuesets;

import java.util.Arrays;
import java.util.List;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.model.valueset.IEnumValueSet;

/**
 * Helper class to extract values (or value IDs respectively) from an {@link IEnumValueSet} or
 * {@link EnumDatatype}.
 * 
 * @author Stefan Widmaier
 */
public class ValueListExtractor {

    private ValueListExtractor() {
        // Utility class not to be instantiated
    }

    public static List<String> extractValues(EnumDatatype valueDatatype, boolean includeNull) {
        return Arrays.asList(valueDatatype.getAllValueIds(includeNull));
    }

    public static List<String> extractValues(IEnumValueSet valueSet) {
        return valueSet.getValuesAsList();
    }

}
