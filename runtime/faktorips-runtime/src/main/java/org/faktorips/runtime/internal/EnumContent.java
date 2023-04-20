/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.List;

import org.faktorips.values.InternationalString;

/**
 *
 * Class used as tuple to load the description and values of an extensible enum.
 */
public class EnumContent {

    private final InternationalString description;

    private final List<List<Object>> enumValues;

    public EnumContent(List<List<Object>> enumValues, InternationalString description) {
        this.enumValues = enumValues;
        this.description = description;
    }

    /**
     * A list of parameters for each enum parsed from the XML.
     * 
     * @return the list of enums and their parameters used to initialize them
     */
    public List<List<Object>> getEnumValues() {
        return enumValues;
    }

    /**
     * The description in the {@code EnumContent} parsed from the XML.
     * 
     * @return the description as {@link InternationalString}
     */
    public InternationalString getDescription() {
        return description;
    }
}
