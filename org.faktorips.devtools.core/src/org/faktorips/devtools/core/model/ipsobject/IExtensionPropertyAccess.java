/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model.ipsobject;

/**
 * The interface defines methods to access the values of extension properties.
 * 
 * @see org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition
 * 
 * @author Jan Ortmann
 */
public interface IExtensionPropertyAccess {

    /**
     * Returns true if a definition of the extension property is available in the current eclipse
     * installation.
     * 
     * @param propertyId The id of an extension property.
     * @return true if a definition of the extension property is available in the current eclipse
     *         installation, otherwise false.
     */
    public boolean isExtPropertyDefinitionAvailable(String propertyId);

    /**
     * Returns the object's value for extension property identified by the id.
     * 
     * @param propertyId The id of an extension property.
     * 
     * @throws IllegalArgumentException if no extension property with given id is defined for this
     *             object's type.
     */
    public Object getExtPropertyValue(String propertyId);

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
    public void setExtPropertyValue(String propertyId, Object value);

}
