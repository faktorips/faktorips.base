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

package org.faktorips.devtools.core.ui.controls.valuesets;

import java.util.Arrays;
import java.util.List;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;

/**
 * Helper class to extract values (or value IDs respectively) from an {@link IEnumValueSet} or
 * {@link EnumDatatype}.
 * 
 * @author Stefan Widmaier
 */
public class ValueListExtractor {

    public static List<String> extractValues(EnumDatatype valueDatatype) {
        return Arrays.asList(valueDatatype.getAllValueIds(true));
    }

    public static List<String> extractValues(IEnumValueSet valueSet) {
        return valueSet.getValuesAsList();
    }

}
