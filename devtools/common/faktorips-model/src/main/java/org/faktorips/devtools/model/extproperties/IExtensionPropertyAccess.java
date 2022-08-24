/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.extproperties;

import java.util.Collection;

/**
 * The interface defines methods to access the values of extension properties.
 * 
 * @see org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition
 * 
 * @author Jan Ortmann
 */
public interface IExtensionPropertyAccess {

    /**
     * Getting all {@link IExtensionPropertyDefinition extension property definitions} that are
     * defined for this object.
     * 
     * @return a set with extension property definitions that are relevant for this object
     */
    Collection<IExtensionPropertyDefinition> getExtensionPropertyDefinitions();

    /**
     * Get the {@link IExtensionPropertyDefinition} with the specified property id if there is any
     * relevant. Returns <code>null</code> if there is no matching extension property.
     * 
     * @param propertyId The id of the extension property you want to have the definition for.
     * @return The {@link IExtensionPropertyDefinition} you requested or <code>null</code>
     */
    IExtensionPropertyDefinition getExtensionPropertyDefinition(String propertyId);

    /**
     * Returns true if a definition of the extension property is available in the current eclipse
     * installation.
     * 
     * @param propertyId The id of an extension property.
     * @return true if a definition of the extension property is available in the current eclipse
     *             installation, otherwise false.
     */
    boolean isExtPropertyDefinitionAvailable(String propertyId);

    /**
     * Returns the object's value for extension property identified by the id.
     * 
     * @param propertyId The id of an extension property.
     * 
     * @throws IllegalArgumentException if no extension property with given id is defined for this
     *             object's type.
     */
    Object getExtPropertyValue(String propertyId);

    /**
     * Sets the object's value for the extension property identified by the id. The method does not
     * check if extension property is defined in the current installation.
     * 
     * @param propertyId The id of an extension property.
     * @param value The object's new value for the property.
     * 
     * @throws IllegalArgumentException if no extension property with given id is defined for this
     *             object's type.
     */
    void setExtPropertyValue(String propertyId, Object value);

    /**
     * Removes all obsolete extension properties.
     */
    void removeObsoleteExtensionProperties();
}
