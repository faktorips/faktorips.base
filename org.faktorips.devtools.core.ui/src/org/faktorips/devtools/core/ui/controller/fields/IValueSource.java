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

import java.util.List;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;

/**
 * An {@link IValueSource} represents the values of {@link IValueSet} of specific
 * {@link ValueSetType}. The valuable information are presented in a List of Strings. An
 * {@link IValueSource} can also represent the values of an different {@link ValueSetType}s with a
 * particular datatype.
 */
public interface IValueSource {

    /**
     * Returns all values defined in a {@link ValueSet} or {@link ValueDatatype} which are
     * considered in an {@link IValueSource}.
     * 
     * @return List<String> containing all values.
     */
    public List<String> getValues();

    /**
     * Indicates if this {@link IValueSource} can be used under the given circumstances.
     * 
     * @return <code>true</code> if {@link IValueSource} is applicable.
     */
    public boolean isApplicable();
}
