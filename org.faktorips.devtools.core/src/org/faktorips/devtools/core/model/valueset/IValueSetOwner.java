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
     * Sets the type of the value set.
     */
    public void setValueSetType(ValueSetType type);

    /**
     * Returns <code>true</code> if the value set owned by this owner is updateabled.
     */
    public boolean isValueSetUpdateable();

}
