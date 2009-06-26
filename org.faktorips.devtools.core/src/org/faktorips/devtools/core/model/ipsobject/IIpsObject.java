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

package org.faktorips.devtools.core.model.ipsobject;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

/**
 *
 */
public interface IIpsObject extends IIpsObjectPartContainer {

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "IPSOBJECT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this policy component type is defined as
     * configurable by product, but the product component type name is not set.
     */
    public final static String MSGCODE_SAME_IPSOBJECT_IN_IPSOBEJECTPATH_AHEAD = MSGCODE_PREFIX
            + "sameIpsObjectInIpsObjectPathAhead"; //$NON-NLS-1$

    /**
     * Returns the object's type.
     */
    public IpsObjectType getIpsObjectType();

    /**
     * Returns the IPS source file this object is stored in.
     */
    public IIpsSrcFile getIpsSrcFile();

    /**
     * Returns <code>true</code> if the object was created from a parsable file content,
     * <code>false</code> otherwise.
     */
    public boolean isFromParsableFile();

    /**
     * Returns the object's qualified name. The qualified name is the name of the ips package
     * fragment's name followed by a dot followed by the object's unqualified name. So this is
     * basically the same concept as the qualified name of Java classes.
     * <p>
     * <b>Example:</b><br>
     * The qualified name of an ips object called 'MotorCoverage' in the package fragment
     * 'mycompany.motor' has the qualified name 'mycompany.motor.MotorCoverage'.
     */
    public String getQualifiedName();

    /**
     * Returns the object's name without the leading package name (if any).
     * For IpsObjects this method returns same as the method <code>getName()</code> which is defined in IIpsElement.
     * 
     * @see #getQualifiedName()
     * @see IIpsElement#getName()
     */
    public String getUnqualifiedName();
    
    /**
     * Returns the qualified name type for this ips object which is the combination of this ips
     * objects qualified name and its ips object type.
     */
    public QualifiedNameType getQualifiedNameType();

    /**
     * Returns the IPS package fragment the object is contained in.
     */
    public IIpsPackageFragment getIpsPackageFragment();

    /**
     * Returns the dependencies of this <code>IpsObject</code>. A <code>Dependency</code> contains
     * the <code>QualifiedNameType</code> of the <code>IpsObject</code> this <code>IpsObject</code>
     * depends on and an additional information whether the dependency is a deep one or not. A deep
     * dependency means that not only the direct dependency but also the dependencies of the
     * <code>IpsObject</code> this one depends on will be considered when the dependency graph is
     * evaluated.
     * <p>
     * We use <code>Dependency</code> instances instead of ips object references because an object
     * can depend on another object that does not exist, e.g. because the other object has been
     * deleted. However, if the deleted object is created again, we must rebuild this dependant
     * object so that the problem marker will be removed.
     * 
     * @throws CoreException
     */
    public IDependency[] dependsOn() throws CoreException;

}
