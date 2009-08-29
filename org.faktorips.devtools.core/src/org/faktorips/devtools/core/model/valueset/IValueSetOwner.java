/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.valueset;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IValueDatatypeProvider;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

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
     * Returns the list of allowed value set types.
     * 
     * @throws CoreException if an error occurs.
     */
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets the type of the value set.
     */
    public void setValueSetType(ValueSetType type);

    /**
     * Changes the type of the value set to the new type. The old value set is removed and a new
     * value set of the given new type is created.
     * 
     * @param newType The new value set type.
     * @return The new value set.
     */
    public IValueSet changeValueSetType(ValueSetType newType);

    /**
     * Returns <code>true</code> if the value set owned by this owner is updateabled.
     */
    public boolean isValueSetUpdateable();

}
