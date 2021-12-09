/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import org.eclipse.core.runtime.IAdaptable;
import org.faktorips.devtools.model.abstraction.AResource;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Common protocol for all elements provided by the IPS model.
 */
public interface IIpsElement extends IAdaptable {

    /** The name of the <code>name</code> property. */
    public static final String PROPERTY_NAME = "name"; //$NON-NLS-1$

    /**
     * Returns the element's unqualified name.
     */
    public String getName();

    /**
     * Returns the model this <code>IIpsElement</code> belongs to.
     */
    public IIpsModel getIpsModel();

    /**
     * Returns the <code>IIpsProject</code> this element belongs to or <code>null</code> if this is
     * the model.
     */
    public IIpsProject getIpsProject();

    /**
     * Returns <code>true</code> if this element exists. This is the case if every ancestor up to
     * the <code>IIpsProject</code> does exist and, if this element has a corresponding resource,
     * that resource exists as well.
     */
    public boolean exists();

    /**
     * Returns the resource corresponding to this element, e.g. an <code>IIpsPackageFragment</code>
     * containing source files corresponds to a folder in the file system, a product definition
     * project belongs to a project and so on. If the element does not correspond to a resource,
     * e.g. a product definition object, the operation will return <code>null</code>.
     * 
     * @see #getEnclosingResource()
     */
    public AResource getCorrespondingResource();

    /**
     * Returns the resource this <code>IIpsElement</code> is stored in. In contrast to
     * <code>getCorrespondingResource()</code> this methods never returns <code>null</code>. E.g.
     * for a pd object contained in a source file, the method returns the file the source file
     * corresponds to.
     * 
     * @see #getCorrespondingResource()
     */
    public AResource getEnclosingResource();

    /**
     * Returns the parent <code>IIpsElement</code> or <code>null</code> if this element has no
     * parent. This is the case for the <code>IIpsModel</code> only.
     */
    public IIpsElement getParent();

    /**
     * Returns the element's immediate children or an empty array, if this element hasn't got any
     * children.
     */
    public IIpsElement[] getChildren() throws CoreRuntimeException;

    /** Returns <code>true</code> if this element has any children, otherwise <code>false</code>. */
    public boolean hasChildren() throws CoreRuntimeException;

    /**
     * Returns <code>true</code> if this element is contained in an archive, <code>false</code>
     * otherwise.
     */
    public boolean isContainedInArchive();

    /**
     * Deletes this element. After calling this method neither this element nor any child is cached
     * by the {@link IIpsModel}.
     * <p>
     * The specific action is dependent on the implementation. Delete might for example mean to
     * remove the element from it's parent or to delete the enclosing resource. Implementations
     * should document these details.
     * 
     * @throws CoreRuntimeException If an error occurs during deletion
     * @throws UnsupportedOperationException If the element cannot be deleted
     */
    public void delete() throws CoreRuntimeException;

}
