/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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

import org.faktorips.runtime.model.type.ModelElement;

/**
 * Base Interface for all model elements.
 * 
 * @author Daniel Hohenberger
 * @deprecated Use {@link ModelElement} directly. Will be removed in Faktor-IPS 3.20+
 */
@Deprecated
public interface IModelElement {

    /**
     * Returns the value for the given extension property identified by the specified id.
     * <p>
     * Note: At the moment only {@link String} is supported as extension property value. This
     * methods returns {@link Object} for future changes.
     * 
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
     * Returns the label for this model element in the specified locale. If there is no label in the
     * specified locale, it tries to find the label in the default locale. If there is also no label
     * in the default locale the element's name is returned.
     * 
     * @return the label for the given locale or the element's name if no label exists for the given
     *         locale nor in the default locale
     */
    public String getLabel(Locale locale);

    /**
     * Returns the description for this model element in the specified locale. If there is no
     * description in the specified locale, it tries to find the description in the default locale.
     * If there is also no description in the default locale it returns the empty string.
     * 
     * @return the description for the given locale or an empty string if no description exists for
     *         the given locale
     */
    public String getDescription(Locale locale);

}
