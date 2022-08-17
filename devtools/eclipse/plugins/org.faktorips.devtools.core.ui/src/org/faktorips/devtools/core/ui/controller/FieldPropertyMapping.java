/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

/**
 * Mapping between an edit field and a property of an object.
 * 
 * @author Jan Ortmann
 */
public interface FieldPropertyMapping<T> {

    /**
     * Returns the field this is a mapping for.
     */
    EditField<T> getField();

    /**
     * Returns the object this is a mapping for one of it's properties.
     */
    Object getObject();

    /**
     * Returns the property's name this is a mapping for.
     */
    String getPropertyName();

    /**
     * Updates the object's property with the value from the edit field.
     */
    void setPropertyValue();

    /**
     * Updates the value in the edit field with the value from the obejct's property.
     */
    void setControlValue();

    /**
     * Updates the value in the edit field with the value from the obejct's property.
     * 
     * @param force Whether the update should be forced, default is <code>false</code>
     */
    void setControlValue(boolean force);

    /**
     * Getting the current value of the property.
     * 
     * @return The current value of the property in the object
     */
    T getPropertyValue();
}
