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
import java.util.List;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.model.valueset.ValueSetType;

/**
 * An implementation of {@link IValueSource}. It considers all {@link ValueSetType}s having an
 * {@link EnumDatatype} as datatype.
 */
public class EnumDatatypeValueSource implements IValueSource {

    EnumDatatype enumDatatype;

    public EnumDatatypeValueSource(EnumDatatype enumDatatype) {
        this.enumDatatype = enumDatatype;
    }

    @Override
    public List<String> getValues() {
        return Arrays.asList(enumDatatype.getAllValueIds(true));
    }

    @Override
    public boolean hasValues() {
        return false;
    }

}
