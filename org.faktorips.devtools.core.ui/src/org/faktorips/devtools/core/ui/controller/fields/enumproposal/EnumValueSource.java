/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields.enumproposal;

import java.util.List;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.controller.fields.IValueSource;
import org.faktorips.devtools.model.valueset.IValueSetOwner;

/**
 * Provides enum values from an enum value set or enum datatype.
 */
public class EnumValueSource implements IValueSource {

    private final EnumValueSetSource valueSetSource;
    private final EnumDatatypeValueSource datatypeSource;

    public EnumValueSource(IValueSetOwner valueSetOwner, ValueDatatype valueDatatype) {
        valueSetSource = new EnumValueSetSource(valueSetOwner);
        datatypeSource = new EnumDatatypeValueSource(valueDatatype);
    }

    /**
     * <ul>
     * <li>First the value set is checked for values. If it is an enum value set its values are
     * returned.</li>
     * <li>If the value set cannot provide values the datatype is checked. If it is an enum datatype
     * its values are returned.</li>
     * <li>If neither value set nor datatype can provide enum values, an empty list is
     * returned.</li>
     * </ul>
     */
    @Override
    public List<String> getValues() {
        if (valueSetSource.isApplicable()) {
            return valueSetSource.getValues();
        }
        return datatypeSource.getValues();
    }

}
