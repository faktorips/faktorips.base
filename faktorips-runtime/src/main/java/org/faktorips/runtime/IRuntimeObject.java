/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.Set;

/**
 * Base class for runtime objects offering access to extension properties.
 * 
 * @author Daniel Hohenberger
 */
public interface IRuntimeObject {

    /**
     * @return a set of the extension property ids defined for this element.
     */
    Set<String> getExtensionPropertyIds();

    /**
     * @param propertyId the id of the desired extension property.
     * 
     * @return the value of the extension property defined by the given <code>propertyId</code> or
     *             <code>null</code> if the extension property's <code>isNull</code> attribute is
     *             <code>true</code>.
     * @throws IllegalArgumentException if no such property exists.
     */
    Object getExtensionPropertyValue(String propertyId);

}
