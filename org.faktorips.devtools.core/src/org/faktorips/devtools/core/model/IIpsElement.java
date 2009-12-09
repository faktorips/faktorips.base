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

package org.faktorips.devtools.core.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Common protocol for all elements provided by the IPS model.
 * 
 * @author unascribed
 */
public interface IIpsElement extends IAdaptable {

    /** The name of the <tt>name</tt> property. */
    public final static String PROPERTY_NAME = "name"; //$NON-NLS-1$

    /** Returns the element's unqualified name. */
    public String getName();

    /** Sets the element's name. */
    public void setName(String name);

    /** Returns the model this <tt>IIpsElement</tt> belongs to. */
    public IIpsModel getIpsModel();

    /**
     * Returns the <tt>IIpsProject</tt> this element belongs to or <tt>null</tt> if this is the
     * model.
     */
    public IIpsProject getIpsProject();

    /**
     * Returns <tt>true</tt> if this element exists. This is the case if every ancestor up to the
     * <tt>IIpsProject</tt> does exist and, if this element has a corresponding resource, that
     * resource exists as well.
     */
    public boolean exists();

    /**
     * Returns the resource corresponding to this element, e.g. an <tt>IIpsPackageFragment</tt>
     * containing source files corresponds to a folder in the file system, a product definition
     * project belongs to a project and so on. If the element does not correspond to a resource,
     * e.g. a product definition object, the operation will return <tt>null</tt>.
     * 
     * @see #getEnclosingResource()
     */
    public IResource getCorrespondingResource();

    /**
     * Returns the resource this <tt>IIpsElement</tt> is stored in. In contrast to
     * <tt>getCorrespondingResource()</tt> this methods never returns <tt>null</tt>. E.g. for a pd
     * object contained in a source file, the method returns the file the source file corresponds
     * to.
     * 
     * @see #getCorrespondingResource()
     */
    public IResource getEnclosingResource();

    /**
     * Returns the parent <tt>IIpsElement</tt> or <tt>null</tt> if this element has no parent. This
     * is the case for the <tt>IIpsModel</tt> only.
     */
    public IIpsElement getParent();

    /**
     * Returns the element's immediate children or an empty array, if this element hasn't got any
     * children.
     */
    public IIpsElement[] getChildren() throws CoreException;

    /** Returns <tt>true</tt> if this element has any children, otherwise <tt>false</tt>. */
    public boolean hasChildren() throws CoreException;

    /** Returns this element's image. */
    public Image getImage();

    /** Returns <tt>true</tt> if this element is contained in an archive, <tt>false</tt> otherwise. */
    public boolean isContainedInArchive();

    /**
     * Returns the <tt>RenameRefactoring</tt> for this <tt>IIpsElement</tt>. The
     * <tt>RenameRefactoring</tt> can be used to safely rename this <tt>IIpsElement</tt>.
     * <p>
     * Returns <tt>null</tt> if this <tt>IIpsElement</tt> does not support the rename refactoring.
     * 
     * @see #isRenameRefactoringSupported()
     */
    public RenameRefactoring getRenameRefactoring();

    /**
     * Returns <tt>true</tt> if this <tt>IIpsElement</tt> supports the rename refactoring,
     * <tt>false</tt> if not.
     */
    public boolean isRenameRefactoringSupported();

}
