/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.valueset;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Interface that marks an object as owning a value set.
 * 
 * @author Jan Ortmann
 */
public interface IValueSetOwner extends IIpsElement {

    /**
     * Returns the set of allowed values.
     */
    public IValueSet getValueSet();

    /**
     * Returns the list of allowed value set types.
     * 
     * @throws CoreException If an error occurs.
     */
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets the type of the value set.
     */
    public void setValueSetType(ValueSetType type);

    /**
     * Changes the type of the value set to the new type. The old value set is removed and a new
     * value set of the given new type is created. Returns the new value set.
     * 
     * @param newType The new value set type.
     */
    public IValueSet changeValueSetType(ValueSetType newType);

    /**
     * Returns <code>true</code> if it is possible to update the value set owned by this owner.
     */
    public boolean isValueSetUpdateable();

    /**
     * Returns the value data type all values in the value set must be "instances" of.
     * 
     * @param ipsProject The project which IPS object path is used to search the data type. This is
     *            not necessarily the project this value set owner is part of.
     */
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) throws CoreException;

}
