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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.controller.fields.IValueSource;

/**
 * Provides the enum values for an {@link EnumDatatype}. Provides an empty list for all other
 * {@link ValueDatatype datatypes}.
 */
public class EnumDatatypeValueSource implements IValueSource {

    private ValueDatatype valueDatatype;

    public EnumDatatypeValueSource(ValueDatatype valueDatatype) {
        this.valueDatatype = valueDatatype;
    }

    /**
     * In case of an {@link EnumDatatype} its value ids are returned. In case of any other
     * {@link ValueDatatype} an empty list is returned.
     */
    @Override
    public List<String> getValues() {
        if (isApplicable()) {
            return Arrays.asList(((EnumDatatype)valueDatatype).getAllValueIds(true));
        }
        return Collections.emptyList();
    }

    /**
     * Returns <code>true</code> for {@link EnumDatatype enum datatypes}. <code>false</code> else.
     */
    public boolean isApplicable() {
        return valueDatatype.isEnum();
    }

}
