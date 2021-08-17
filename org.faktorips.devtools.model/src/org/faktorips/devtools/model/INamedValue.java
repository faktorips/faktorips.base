/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import java.util.Objects;

/**
 * A named value has a technical ID and a human readable name.
 */
public interface INamedValue {

    /**
     * Returns the technical ID.
     */
    String getId();

    /**
     * Returns the human readable name.
     */
    String getName();

    /**
     * Returns the object's String representation, unless it is an {@link INamedValue}, then it
     * returns its name.
     */
    static String getName(Object object) {
        return object instanceof INamedValue ? ((INamedValue)object).getName() : Objects.toString(object);
    }

}