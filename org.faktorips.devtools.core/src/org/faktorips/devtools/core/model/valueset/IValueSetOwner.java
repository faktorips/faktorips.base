/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.valueset;

import org.faktorips.devtools.core.model.IValueDatatypeProvider;

/**
 * Interface that marks an object as owning a value set.
 * 
 * @author Jan Ortmann
 */
public interface IValueSetOwner extends IValueDatatypeProvider {

    /**
     * Returns the set of allowed values.
     */
    public IValueSet getValueSet();

    /**
     * Sets the type of the value set defining the values valid for this attribute.
     * If the type of the current value set is the same as the new type, the attribute
     * remains unchanged. 
     */
    public void setValueSetType(ValueSetType type);

    /**
     * Returns <code>true</code> if the value set owned by this owner is updateabled.
     */
    public boolean isValueSetUpdateable();
    
}
