/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype;

import java.util.Locale;
import java.util.Set;

/**
 * Base Interface for all model elements.
 * 
 * @author Daniel Hohenberger
 */
public interface IModelElement {
    /**
     * @return the value of the extension property defined by the given <code>propertyId</code> or
     *         <code>null</code> if the extension property's <code>isNull</code> attribute is
     *         <code>true</code>
     * @throws IllegalArgumentException if no such property exists
     */
    public Object getExtensionPropertyValue(String propertyId);

    /**
     * @return a set of the extension property ids defined for this element
     */
    public Set<String> getExtensionPropertyIds();

    /**
     * @return the qualified IPS object name
     */
    public String getName();

    /**
     * TODO
     * 
     * @return the label for the given locale or the element's name if no label exists for the given
     *         locale
     */
    public String getLabel(Locale locale);

    /**
     * @return the description for the given locale or an empty string if no description exists for
     *         the given locale
     */
    public String getDescription(Locale locale);

}
