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
 * This class represents an Faktor-IPS enum and holds the description for the enum-content and the
 * concrete enum-values loaded from the XML via SAX.
 */
public class IpsEnum<T> {

    private final List<T> enums;
    private final InternationalString description;

    public IpsEnum(List<T> enums, InternationalString description) {
        this.enums = enums;
        this.description = description;

    }

    /**
     * The description of the enum-content parsed from XML.
     * 
     * @return an {@link InternationalString} with the description of the enum-content
     */
    public InternationalString getDescription() {
        return description;
    }

    /**
     * The list of enums loaded from XML.
     * 
     * @return the concrete enum instances
     */
    public List<T> getEnums() {
        return enums;
    }

}
