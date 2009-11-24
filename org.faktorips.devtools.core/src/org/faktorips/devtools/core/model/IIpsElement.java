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
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Common protocol for all elements provided by the IPS model.
 */
public interface IIpsElement {

    /**
     * The name of the name property.
     */
    public final static String PROPERTY_NAME = "name"; //$NON-NLS-1$

    /**
     * Returns the element's unqualified name.
     */
    public String getName();

    /**
     * Returns the model this element belongs to.
     */
    public IIpsModel getIpsModel();

    /**
     * Returns the project this element belongs to or null if this is the model.
     */
    public IIpsProject getIpsProject();

    /**
     * Returns true if this element exists. This is the case if every ancestor up to the
     * <code>IIpsProject</code> does exist and, if this element has a corresponding resource, that
     * resource exists as well.
     */
    public boolean exists();

    /**
     * Returns the resource corresponding to this element, e.g. a IpsPackageFragment containing
     * source files corresponds to a folder in the filesystem, a product definition project belongs
     * to a project and so on. If the element does not correspond to a resource, e.g. a product
     * definition object, the method returns null.
     */
    public IResource getCorrespondingResource();

    /**
     * Returns the resource the element is stored in. In contrast to
     * <code>getCorrespondingResource()</code> this methods never returns null. E.g. for a pd object
     * contained in a source file, the method returns the file the source file corresponds to.
     */
    public IResource getEnclosingResource();

    /**
     * Returns the parent element or null if this element has no parent. This is the case for the
     * IpsModel only.
     */
    public IIpsElement getParent();

    /**
     * Returns the element's immediate children or an empty array, if this element hasn't got any
     * children.
     */
    public IIpsElement[] getChildren() throws CoreException;

    /**
     * Returns true if this element has any children, otherwise false.
     */
    public boolean hasChildren() throws CoreException;

    /**
     * Returns the element's image.
     */
    public Image getImage();

    /**
     * Returns <code>true</code> if this element is contained in an archive, <code>false</code>
     * otherwise.
     */
    public boolean isContainedInArchive();

}
