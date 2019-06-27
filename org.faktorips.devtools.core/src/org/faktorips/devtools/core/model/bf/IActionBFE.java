/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.bf;

import org.eclipse.core.runtime.CoreException;

/**
 * This class represents an action within a business function. Actions a points of execution within
 * the control flow of a business function. There are three different types of actions.
 * <p>
 * An in line action represents a piece of code that can be executed. An in line actions is
 * generated to a method on the business function class.
 * <p>
 * A method call action calls a method on a parameter that is specified for the business function.
 * Currently only parameter less methods on policy or product component type can be called.
 * <p>
 * A business function call action executes a specified business function that is called from within
 * a business function.
 * 
 * @author Peter Erzberger
 */
public interface IActionBFE extends IMethodCallBFE {

    public final static String XML_TAG = "Action"; //$NON-NLS-1$

    /**
     * Returns the business function of this action. Only business function call actions can return
     * a referenced business function. Otherwise {@link NullPointerException} is returned.
     * 
     * @see #setTarget(String)
     * @throws CoreException if an exception is thrown while trying to determine the business
     *             function using the specified target string
     */
    public IBusinessFunction findReferencedBusinessFunction() throws CoreException;

    /**
     * Returns the qualified name of the referenced business function. Only relevant for business
     * function call actions.
     */
    public String getReferencedBfQualifiedName();

    /**
     * Returns the unqualified name of the referenced business function. Only relevant for business
     * function call actions.
     */
    public String getReferencedBfUnqualifedName();

}
