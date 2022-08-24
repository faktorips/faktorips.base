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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;

public interface IIpsObject extends IIpsObjectPartContainer, IDescribedElement {

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "IPSOBJECT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this policy component type is defined as
     * configurable by product, but the product component type name is not set.
     */
    String MSGCODE_SAME_IPSOBJECT_IN_IPSOBEJECTPATH_AHEAD = MSGCODE_PREFIX
            + "sameIpsObjectInIpsObjectPathAhead"; //$NON-NLS-1$

    /**
     * Returns the object's type.
     */
    IpsObjectType getIpsObjectType();

    /**
     * Returns the IPS source file this object is stored in.
     */
    @Override
    IIpsSrcFile getIpsSrcFile();

    /**
     * Returns <code>true</code> if the object was created from a parsable file content,
     * <code>false</code> otherwise.
     */
    boolean isFromParsableFile();

    /**
     * Returns the object's qualified name. The qualified name is the name of the IPS package
     * fragment's name followed by a dot followed by the object's unqualified name. So this is
     * basically the same concept as the qualified name of Java classes.
     * <p>
     * <strong>Example:</strong><br>
     * The qualified name of an IPS object called 'MotorCoverage' in the package fragment
     * 'mycompany.motor' has the qualified name 'mycompany.motor.MotorCoverage'.
     */
    String getQualifiedName();

    /**
     * Returns the object's name without the leading package name (if any). For IpsObjects this
     * method returns same as the method <code>getName()</code> which is defined in IIpsElement.
     * 
     * @see #getQualifiedName()
     * @see IIpsElement#getName()
     */
    String getUnqualifiedName();

    /**
     * Returns the qualified name type for this IPS object which is the combination of this IPS
     * objects qualified name and its IPS object type.
     */
    QualifiedNameType getQualifiedNameType();

    /**
     * Returns the IPS package fragment the object is contained in.
     */
    IIpsPackageFragment getIpsPackageFragment();

    /**
     * Returns the dependencies of this <code>IpsObject</code>. A <code>Dependency</code> contains
     * the <code>QualifiedNameType</code> of the <code>IpsObject</code> this <code>IpsObject</code>
     * depends on and an additional information whether the dependency is a deep one or not. A deep
     * dependency means that not only the direct dependency but also the dependencies of the
     * <code>IpsObject</code> this one depends on will be considered when the dependency graph is
     * evaluated.
     * <p>
     * We use <code>Dependency</code> instances instead of IPS object references because an object
     * can depend on another object that does not exist, e.g. because the other object has been
     * deleted. However, if the deleted object is created again, we must rebuild this dependent
     * object so that the problem marker will be removed.
     */
    IDependency[] dependsOn();

    /**
     * Returns the dependency details describing which parts and properties are causing the given
     * dependency.
     * 
     * @param dependency The dependency to get the details for, must not be <code>null</code>.
     * 
     * @return The details to the given Dependency. If no details exist for this dependency, an
     *             empty list is returned.
     * 
     * @throws NullPointerException If the given dependency is <code>null</code>.
     */
    List<IDependencyDetail> getDependencyDetails(IDependency dependency) throws IpsException;

    /**
     * Deletes the {@link IIpsSrcFile} this object is stored in.
     */
    @Override
    void delete() throws IpsException;

    /**
     * Creates a new IpsSrcFile based on a this IIpsObject. The filename is constructed by appending
     * the type specific file extension to the given object name (separated by a dot). The content
     * of the IpsSrcFile is copied from this object.
     * 
     * @param targetFragment the {@link IIpsPackageFragment} where the new file will be created
     * @param name the file name
     * @param force a flag controlling how to deal with resources that are not in sync with the
     *            local file system
     * @param monitor the given progress monitor
     * 
     * @throws RuntimeException if the element could not be created. Reasons include:
     *             <ul>
     *             <li>The folder does not exist</li>
     *             <li>A <code>CoreException</code> occurred while creating an underlying resource
     *             <li>The root folder is read only
     *             <li>The name is not a valid object name
     *             </ul>
     */
    IIpsSrcFile createCopy(IIpsPackageFragment targetFragment,
            String name,
            boolean force,
            IProgressMonitor monitor);
}
