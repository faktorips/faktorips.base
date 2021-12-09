/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Interface that marks an IPS object as being able to fix difference between it's own assumed state
 * of the model and the real state of the model. E.g. a product component might contain a value for
 * an attribute named "maxSumInsured", but the model does not contain the attribute any longer. In
 * this case the call of {@link #fixAllDifferencesToModel(IIpsProject)} would remove the attribute
 * value from the product component.
 * 
 * @author Daniel Hohenberger
 */
public interface IFixDifferencesToModelSupport extends IIpsObject {

    /**
     * Returns <code>true</code> if this element contains structural differences to its
     * corresponding model element.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search for IPS objects
     *            needed during the fix.
     * 
     * @throws CoreRuntimeException if an error occurs while checking.
     */
    public boolean containsDifferenceToModel(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Fixes all differences between this element and its corresponding model element. In most cases
     * this method would calls the method {@link #computeDeltaToModel(IIpsProject)} and calls the
     * {@link IFixDifferencesComposite#fixAllDifferencesToModel()}. However some
     * {@link IFixDifferencesComposite} need to be configured properly to automatically fix all
     * differences .
     * 
     * @param ipsProject The IPS project which IPS object path is used to search for IPS objects
     *            needed during the fix.
     * 
     * @throws CoreRuntimeException if an error occurs while fixing.
     */
    public void fixAllDifferencesToModel(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Returns the IPS source file this object is stored in. Duplicated here for cleaner
     * inheritance.
     * 
     * @see org.faktorips.devtools.model.ipsobject.IIpsObject#getIpsSrcFile()
     */
    @Override
    public IIpsSrcFile getIpsSrcFile();

    /**
     * Returns the delta between this element and it's model type.
     * 
     * @param ipsProject The project which search path is used to search the type.
     * 
     * @throws CoreRuntimeException if an exception occurs while searching for the type.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public IFixDifferencesComposite computeDeltaToModel(IIpsProject ipsProject) throws CoreRuntimeException;

}
