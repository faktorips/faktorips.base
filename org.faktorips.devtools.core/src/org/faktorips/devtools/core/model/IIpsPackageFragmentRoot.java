/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A package fragment root contains a set of package fragments.
 * It corresponds to an underlying resource which is either a folder,
 * JAR, or zip.  In the case of a folder, all descendant folders represent
 * package fragments.  For a given child folder representing a package fragment, 
 * the corresponding package name is composed of the folder names between the folder 
 * for this root and the child folder representing the package, separated by '.'.
 * In the case of a JAR or zip, the contents of the archive dictates 
 * the set of package fragments in an analogous manner.
 */
public interface IIpsPackageFragmentRoot extends IIpsElement {
    
    /**
     * Returns true if this package fragment root contains source files.
     */
    public boolean containsSourceFiles();
    
    /**
     * Returns the entry in the ips object path that results in this package fragment root.
     * E.g. an entry defining a source folder leads to an ips package fragment root.
     * @throws CoreException if an excpetion occurs while accessing the object path or this package fragment root
     * does not exist. 
     */
    public IIpsObjectPathEntry getIpsObjectPathEntry() throws CoreException;
    
    /**
     * Returns the artefact destination for the artefacts generated on behalf of the ips objects within this
     * ips package fragment root.
     */
    public IFolder getArtefactDestination() throws CoreException;

    /**
     * Returns the package fragments contained in this root folder. 
     * Returns an empty array if this root folder does not contain any folders.
     */
    public IIpsPackageFragment[] getIpsPackageFragments() throws CoreException;
    
    /**
     * Returns the package fragment with the indicated name.
     */
    public IIpsPackageFragment getIpsPackageFragment(String name);
    
    /**
     * Returns all <code>IResource</code> objects that do not correspond to
     * IpsPackageFragments contained in this PackageFragmentRoot. Returns an
     * empty array if no such resources are found.
     */
    public Object[] getNonIpsResources() throws CoreException;
    
    /**
     * Returns the default-package.
     */
    public IIpsPackageFragment getIpsDefaultPackageFragment();
    
    /**
     * Creates the IPS package fragment with the indicated name. Note that if the name
     * contains one or more dots (.), one folder in the filesystem is
     * created for each token between the dots.
     * 
	 * @param name the given dot-separated package name
	 * @param force a flag controlling how to deal with resources that
	 *    are not in sync with the local file system
	 * @param monitor the given progress monitor
     * 
	 * @throws CoreException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This root folder does not exist</li>
	 * <li> A <code>CoreException</code> occurred while creating an underlying resource</li>
	 * <li> This root folder is read only</li>
	 * <li> The name is not a valid package name</li>
	 * </ul>
     */
    public IIpsPackageFragment createPackageFragment(String name, boolean force, IProgressMonitor monitor) throws CoreException;

    /**
     * Returns the IPS object with the indicated type and qualified name.
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException;

    /**
     * Returns the IPS object with the indicated qualified name type.
     */
    public IIpsObject findIpsObject(QualifiedNameType nameType) throws CoreException;

}
