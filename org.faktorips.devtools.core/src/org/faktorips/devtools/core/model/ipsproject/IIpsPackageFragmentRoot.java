/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model.ipsproject;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;

/**
 * A package fragment root contains a set of package fragments. It corresponds to an underlying
 * resource which is either a folder, JAR, or ZIP. In the case of a folder, all descendant folders
 * represent package fragments. For a given child folder representing a package fragment, the
 * corresponding package name is composed of the folder names between the folder for this root and
 * the child folder representing the package, separated by '.'. In the case of a JAR or ZIP, the
 * contents of the archive dictates the set of package fragments in an analogous manner.
 */
public interface IIpsPackageFragmentRoot extends IIpsElement {

    /**
     * Returns <code>true</code> if this package fragment root represents a source folder containing
     * modifiable source files, otherwise <code>false</code>.
     */
    public boolean isBasedOnSourceFolder();

    /**
     * Returns <code>true</code> if this package fragment root is based on an ips archive.
     */
    public boolean isBasedOnIpsArchive();

    /**
     * Returns the entry in the IPS object path that results in this package fragment root. E.g. an
     * entry defining a source folder leads to an IPS package fragment root.
     * 
     * @throws CoreException if an exception occurs while accessing the object path.
     */
    public IIpsObjectPathEntry getIpsObjectPathEntry() throws CoreException;

    /**
     * Returns the artifact destination for the artifacts generated on behalf of the IPS objects
     * within this IPS package fragment root.
     * 
     * @param derived determines if the artifact destination for derived resources or the
     *            destination for mergable resources is to return. If set to true the destination
     *            for the derived artifacts will be returned.
     */
    public IFolder getArtefactDestination(boolean derived) throws CoreException;

    /**
     * Returns the package fragments contained in this root folder. Returns an empty array if this
     * root folder does not contain any fragments.
     */
    public IIpsPackageFragment[] getIpsPackageFragments() throws CoreException;

    /**
     * Returns the package fragments contained in this root folder in the order defined by the sort
     * definitions. Returns an empty array if this root folder does not contain any fragments.
     * 
     * @see IIpsPackageFragmentSortDefinition
     */
    public IIpsPackageFragment[] getSortedIpsPackageFragments() throws CoreException;

    /**
     * Returns the package fragment with the indicated name or <code>null</code> if the given name
     * is not a valid package name. Note that the returned package fragment might not exists.
     * <p>
     * If a given name is valid as name for a package fragment is determined by
     * {@link IIpsProject#getNamingConventions()}.
     * 
     * @see IIpsProject#getNamingConventions()
     */
    public IIpsPackageFragment getIpsPackageFragment(String name);

    /**
     * Returns all <code>IResource</code> objects that do not correspond to
     * <code>IIpsPackageFragment</code>s contained in this PackageFragmentRoot. Returns an empty
     * array if no such resources are found.
     * 
     * @throws CoreException if the members of the corresponding resource cannot be accessed.
     */
    public IResource[] getNonIpsResources() throws CoreException;

    /**
     * Returns the default-package.
     */
    public IIpsPackageFragment getDefaultIpsPackageFragment();

    /**
     * Creates the IPS package fragment with the indicated name. Note that if the name contains one
     * or more dots (.), one folder in the file system is created for each token between the dots.
     * 
     * @param name the given dot-separated package name
     * @param force a flag controlling how to deal with resources that are not in sync with the
     *            local file system
     * @param monitor the given progress monitor
     * 
     * @throws CoreException if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This root folder does not exist</li>
     *             <li>A <code>CoreException</code> occurred while creating an underlying resource</li>
     *             <li>This root folder is read only</li>
     *             <li>The name is not a valid package name</li>
     *             </ul>
     */
    public IIpsPackageFragment createPackageFragment(String name, boolean force, IProgressMonitor monitor)
            throws CoreException;

    /**
     * Returns the IPS object with the indicated type and qualified name.
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException;

    /**
     * Returns the IPS source file with the indicated qualified name type.
     */
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType nameType) throws CoreException;

    /**
     * If this root is based on an archive, the method returns the archive, otherwise
     * <code>null</code> is returned.
     */
    public IIpsArchive getIpsArchive() throws CoreException;

}
