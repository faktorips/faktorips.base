/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.type;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IBaseMethod;

public interface IMethod extends ITypePart, IBaseMethod {

    public static final String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$

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
     * @throws CoreException If there is an error in type hierarchy check.
     */
    public boolean overrides(IMethod otherMethod) throws CoreException;

    /**
     * Returns the method overriding this one or <code>null</code> if no such method is found. The
     * search starts from the given type up the supertype hierarchy.
     * 
     * @param typeToSearchFrom The type to start the search from, must be a subtype of the type this
     *            method belongs to.
     * @param ipsProject The project which IPS object path is used to search.
     * 
     * @throws CoreException If an error occurs while searching.
     */
    public IMethod findOverridingMethod(IType typeToSearchFrom, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the first method, that is overridden by this method.
     * 
     * @param ipsProject The project which IPS object path is used to search.
     * 
     * @throws CoreException If an error occurs while searching.
     */
    public IMethod findOverriddenMethod(IIpsProject ipsProject) throws CoreException;

}
