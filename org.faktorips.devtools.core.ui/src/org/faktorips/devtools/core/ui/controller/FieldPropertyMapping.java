/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller;

/**
 * Mapping between an edit field and a property of an object.
 * 
 * @author Jan Ortmann
 */
public interface FieldPropertyMapping {

    /**
     * Returns the field this is a mapping for.
     */
    public EditField getField();

    /**
     * Returns the object this is a mapping for one of it's properties.
     */
    public Object getObject();

    /**
     * Returns the property's name this is a mapping for.
     */
    public String getPropertyName();

    /**
     * Updates the object's property with the value from the edit field.
     */
    public void setPropertyValue();

    /**
     * Updates the value in the edit field with the value from the obejct's property.
     */
    public void setControlValue();

}
