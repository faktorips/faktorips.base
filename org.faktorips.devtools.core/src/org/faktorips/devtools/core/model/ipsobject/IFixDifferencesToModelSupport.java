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

package org.faktorips.devtools.core.model.ipsobject;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Interface that marks an IPS object as being able to fix difference between it's own assumed state
 * of the model and the real state of the model. E.g. a product component might contain a value for
 * an attribute named "maxSumInsured", but the model does not contain the attribute any longer. In
 * this case the call of {@link #fixAllDifferencesToModel(IIpsProject)} would remove the attribute
 * value from the product component.
 * 
 * @author Daniel Hohenberger
 */
public interface IFixDifferencesToModelSupport {

    /**
     * Returns <code>true</code> if this element contains structural differences to its
     * corresponding model element.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search for ips objects
     *            needed during the fix.
     * 
     * @throws CoreException if an error occurs while checking.
     */
    public boolean containsDifferenceToModel(IIpsProject ipsProject) throws CoreException;

    /**
     * Fixes all differences between this element and its corresponding model element.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search for IPS objects
     *            needed during the fix.
     * 
     * @throws CoreException if an error occurs while fixing.
     */
    public void fixAllDifferencesToModel(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the IPS source file this object is stored in. Duplicated here for cleaner
     * inheritance.
     * 
     * @see org.faktorips.devtools.core.model.ipsobject.IIpsObject#getIpsSrcFile()
     */
    public IIpsSrcFile getIpsSrcFile();

}
