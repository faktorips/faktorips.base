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

package org.faktorips.devtools.core.model.bf;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IMethod;

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
public interface IMethodCallBFE extends IBFElement {

    public final static String PROPERTY_TARGET = "target"; //$NON-NLS-1$
    public final static String PROPERTY_EXECUTABLE_METHOD_NAME = "executableMethodName"; //$NON-NLS-1$

    // validation message codes
    public static final String MSGCODE_PREFIX = "METHODCALLBFE-"; //$NON-NLS-1$
    public static final String MSGCODE_TARGET_NOT_SPECIFIED = MSGCODE_PREFIX + "targetNotSpecified"; //$NON-NLS-1$
    public static final String MSGCODE_TARGET_DOES_NOT_EXIST = MSGCODE_PREFIX + "targetDoesNotExist"; //$NON-NLS-1$
    public static final String MSGCODE_TARGET_NOT_VALID_TYPE = MSGCODE_PREFIX + "targetNotValidType"; //$NON-NLS-1$
    public static final String MSGCODE_METHOD_NOT_SPECIFIED = MSGCODE_PREFIX + "methodNotSpecified"; //$NON-NLS-1$
    public static final String MSGCODE_METHOD_DOES_NOT_EXIST = MSGCODE_PREFIX + "methodDoesNotExist"; //$NON-NLS-1$

    /**
     * The target of this action. A target is not relevant for an in line action. For a method call
     * action the target is the parameter upon which the method call is applied. For the business
     * function call action the target is the business function to call.
     */
    public void setTarget(String target);

    /**
     * Returns the target of this action. The target might be <code>null</code> in case of a in line
     * action.
     * 
     * @see #setTarget(String)
     */
    public String getTarget();

    /**
     * Returns the parameter of this action. Only method call actions can have a parameter.
     * Otherwise this method returns <code>null</code>.
     * 
     * @see #setTarget(String)
     */
    public IParameterBFE getParameter();

    /**
     * The name of the method that is executed by this action. Only relevant for method call
     * actions. The method has to be a parameterless method of the specified parameter.
     */
    public void setExecutableMethodName(String name);

    /**
     * Returns the name of the method that is executed by this action.
     * 
     * @see #setExecutableMethodName(String)
     */
    public String getExecutableMethodName();

    /**
     * Tries to find the method defined by the executable method name and the target. The method is
     * assumed to have no parameters. If no method is found <code>null</code> will be returned.
     * 
     * @param ipsProject the IPS project that is utilized to start the search from.
     * 
     * @throws CoreException if an exception occurs during the execution of this method
     */
    public IMethod findMethod(IIpsProject ipsProject) throws CoreException;

}
