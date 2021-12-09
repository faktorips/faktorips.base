/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.type;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IBaseMethod;

public interface IMethod extends ITypePart, IBaseMethod {

    public static final String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$

    public static final String MSGCODE_RETURN_TYPE_IS_INCOMPATIBLE = IBaseMethod.MSGCODE_PREFIX
            + "returnTypeIsIncompatibleWithOverriddenMethod"; //$NON-NLS-1$

    public static final String MSGCODE_MODIFIER_NOT_EQUAL = IBaseMethod.MSGCODE_PREFIX + "modifierNotEqual"; //$NON-NLS-1$

    public static final String MSGCODE_DUBLICATE_SIGNATURE = IBaseMethod.MSGCODE_PREFIX + "duplicateSignature"; //$NON-NLS-1$

    /**
     * Returns <code>true</code> if this is an abstract method, <code>false</code> otherwise.
     */
    public boolean isAbstract();

    /**
     * Sets if this is an abstract method or not.
     */
    public void setAbstract(boolean newValue);

    /**
     * Returns <code>true</code> if this method overrides the <code>otherMethod</code>. This method
     * could override a method of any super type of this method's type, so <code>this.getType</code>
     * must be a sub type of <code>otherMethod.getType</code>. Further the method signature have to
     * be the same. Returns <code>false</code> otherwise. Note that the type of the methods return
     * values are not checked due to this case is not valid.
     * 
     * @param otherMethod The method that overrides this one.
     * 
     * @throws CoreRuntimeException If there is an error in type hierarchy check.
     */
    public boolean overrides(IMethod otherMethod) throws CoreRuntimeException;

    /**
     * Returns the method overriding this one or <code>null</code> if no such method is found. The
     * search starts from the given type up the supertype hierarchy.
     * 
     * @param typeToSearchFrom The type to start the search from, must be a subtype of the type this
     *            method belongs to.
     * @param ipsProject The project which IPS object path is used to search.
     * 
     * @throws CoreRuntimeException If an error occurs while searching.
     */
    public IMethod findOverridingMethod(IType typeToSearchFrom, IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Returns the first method, that is overridden by this method.
     * 
     * @param ipsProject The project which IPS object path is used to search.
     * 
     * @throws CoreRuntimeException If an error occurs while searching.
     */
    public IMethod findOverriddenMethod(IIpsProject ipsProject) throws CoreRuntimeException;

}
