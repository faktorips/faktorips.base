/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.controller.fields;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.valueset.ValueSetType;

/**
 * An implementation of {@link IValueSource}. It considers all {@link ValueSetType}s having an
 * {@link EnumDatatype} as datatype.
 */
public class EnumDatatypeValueSource implements IValueSource {

    private ValueDatatype valueDatatype;

    public EnumDatatypeValueSource(ValueDatatype enumDatatype) {
        this.valueDatatype = enumDatatype;
    }

    @Override
    public List<String> getValues() {
        if (isApplicable()) {
            return Arrays.asList(((EnumDatatype)valueDatatype).getAllValueIds(true));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isApplicable() {
        return valueDatatype.isEnum();
    }

}
