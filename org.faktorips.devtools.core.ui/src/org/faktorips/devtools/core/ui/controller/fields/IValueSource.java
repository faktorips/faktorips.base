/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.List;

import org.faktorips.devtools.model.internal.valueset.EnumValueSet;

/**
 * Provides enumeration values as a list of strings. If no values can be provided, an empty list is
 * returned.
 */
public interface IValueSource {

    /**
     * Returns all values defined in as for example in an {@link EnumValueSet}. Returns an empty
     * list if this value source cannot provide any values.
     */
    public List<String> getValues();

}
