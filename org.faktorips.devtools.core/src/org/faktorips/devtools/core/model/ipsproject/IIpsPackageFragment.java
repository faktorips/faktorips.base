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

package org.faktorips.devtools.core.model.ipsproject;

import java.io.InputStream;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;

/**
 * An IPS package fragment is a portion of the workspace corresponding to an entire package, or to a
 * portion thereof. The distinction between a package fragment and a package is that a package with
 * some name is the union of all package fragments in the ips object path which have the same name.
 */
public interface IIpsPackageFragment extends IIpsElement {

    /**
     * Constant that represents the name of the default package to make it explicit that the default
     * package name is an empty string.
     */
    public final static String NAME_OF_THE_DEFAULT_PACKAGE = ""; //$NON-NLS-1$

    /**
     * The char used as separator for subpackages.
     */
    public final static char SEPARATOR = '.';

    /**
     * The name of the sort order file.
     */
    public static final String SORT_ORDER_FILE_NAME = ".sortorder"; //$NON-NLS-1$

    /**
     * Returns all ips source files in the package. Returns an empty array if the package is empty.
     * 
     * @throws CoreException if an error occurs while searching for the files.
     */
    public IIpsSrcFile[] getIpsSrcFiles() throws CoreException;

    /**
     * Returns the packagefragment which contains this one or null if this one is the
     * default-package.
     */
    public IIpsPackageFragment getParentIpsPackageFragment();

    /**
     * Returns all packagfragments which are contained in this one.
     * 
     * @throws CoreException
     */
    public IIpsPackageFragment[] getChildIpsPackageFragments() throws CoreException;

    /**
     * Returns the child packages sorted by the sort definition.
     * 
     * @throws CoreException
     * 
     * @see #getSortDefinition()
     */
    public IIpsPackageFragment[] getSortedChildIpsPackageFragments() throws CoreException;

    /**
     * Reads the sort definition of the IIpsPackageFragment and its siblings. Returns the default
     * sort definition if no sort definition exists.
     * 
     * @throws CoreException
     */
    public IIpsPackageFragmentSortDefinition getSortDefinition() throws CoreException;

    /**
     * Set a new sort definition for this IIpsPackageFragment and its siblings. Set the current sort
     * definition to default if <code>null</code> is passed.
     * 
     * @throws CoreException
     */
    public void setSortDefinition(IIpsPackageFragmentSortDefinition newDefinition) throws CoreException;

    /**
     * Returns all <code>IResource</code>s that do not correspond to
     * <code>IIpsPackageFragment</code>s contained in this PackageFragment. Returns an empty array
     * if no such resources are found.
     * 
     * @throws CoreException if the members of the corresponding resource cannot be accessed.
     */
    public IResource[] getNonIpsResources() throws CoreException;

    /**
     * Returns the package fragment root this package fragment belongs to.
     */
    public IIpsPackageFragmentRoot getRoot();

    /**
     * Returns an <code>org.eclipse.core.runtime.IPath</code> object representing for the package
     * fragment name.
     */
    public IPath getRelativePath();

    /**
     * Returns a handle to the IPS source file with the given name. If the provided name doesn't
     * have a file extension that fits to an IpsSrcFile <code>null</code> will be returned.
     */
    public IIpsSrcFile getIpsSrcFile(String name);

    /**
     * Returns a handle to the IPS source file for the given filenameWithoutExtension. The file
     * extension is derived from the ips object type.
     * 
     * @throws NullPointerException if type is <code>null</code>.
     */
    public IIpsSrcFile getIpsSrcFile(String filenameWithoutExtension, IpsObjectType type);

    /**
     * Creates the IpsSrcFile with the indicated name. This method tries to close the provided
     * stream.
     * 
     * @param name the file name
     * @param source input stream providing the file's content
     * @param force a flag controlling how to deal with resources that are not in sync with the
     *            local file system
     * @param monitor the given progress monitor
     * 
     * @throws CoreException if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This folder does not exist</li>
     *             <li>A <code>CoreException</code> occurred while creating an underlying resource
     *             <li>This root folder is read only
     *             <li>The name is not a valid src file name
     *             </ul>
     */
    public IIpsSrcFile createIpsFile(String name, InputStream source, boolean force, IProgressMonitor monitor)
            throws CoreException;

    /**
     * Creates the IpsSrcFile with the indicated name.
     * 
     * @param name the file name
     * @param content the file's content
     * @param force a flag controlling how to deal with resources that are not in sync with the
     *            local file system
     * @param monitor the given progress monitor
     * 
     * @throws CoreException if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This folder does not exist</li>
     *             <li>A <code>CoreException</code> occurred while creating an underlying resource
     *             <li>This root folder is read only
     *             <li>The name is not a valid src file name
     *             </ul>
     */
    public IIpsSrcFile createIpsFile(String name, String content, boolean force, IProgressMonitor monitor)
            throws CoreException;

    /**
     * Creates a IpsSrcFile that contains an IpsObject of the indicated type and with the indicated
     * name. The filename is constructed by appending the type specific file extension to the object
     * name (separated by a dot).
     * 
     * @param type the object's type
     * @param name the file name
     * @param force a flag controlling how to deal with resources that are not in sync with the
     *            local file system
     * @param monitor the given progress monitor
     * 
     * @throws CoreException if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This folder does not exist</li>
     *             <li>A <code>CoreException</code> occurred while creating an underlying resource
     *             <li>This root folder is read only
     *             <li>The name is not a valid object name
     *             </ul>
     */
    public IIpsSrcFile createIpsFile(IpsObjectType type, String ipsObjectName, boolean force, IProgressMonitor monitor)
            throws CoreException;

    /**
     * Creates a new IpsSrcFile based on a given template. The filename is constructed by appending
     * the type specific file extension to the given object name (separated by a dot). The content
     * of the IpsSrcFile is copied from the given template. If the template is a ITimedIpsObject,
     * only the generation that is valid at the given date is copied. If no generation is valid at
     * the given date because the date lies before any valid generation of the given template, the
     * first generation of the given template is used.
     * 
     * @param name the file name
     * @param template the source for the contents to copy from
     * @param date the date to find the generation effective on if template is instance of
     *            ITimedIpsObject). Otherwise this parameter is ignored.
     * @param force a flag controlling how to deal with resources that are not in sync with the
     *            local file system
     * @param monitor the given progress monitor
     * 
     * @throws CoreException if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This folder does not exist</li>
     *             <li>A <code>CoreException</code> occurred while creating an underlying resource
     *             <li>This root folder is read only
     *             <li>The name is not a valid object name
     *             <li>The template has no generation valid on the given date
     *             </ul>
     */
    public IIpsSrcFile createIpsFileFromTemplate(String name,
            IIpsObject template,
            GregorianCalendar date,
            boolean force,
            IProgressMonitor monitor) throws CoreException;

    /**
     * Creates a IpsPackageFragment below this one with the indicated name.
     * 
     * @param name the sub-package name
     * @param force a flag controlling how to deal with resources that are not in sync with the
     *            local file system
     * @param monitor the given progress monitor
     * 
     * @throws CoreException if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This folder does not exist</li>
     *             <li>A <code>CoreException</code> occurred while creating an underlying resource
     *             <li>This root folder is read only
     *             <li>The name is not a valid package name
     *             </ul>
     */
    public IIpsPackageFragment createSubPackage(String name, boolean force, IProgressMonitor monitor)
            throws CoreException;

    /**
     * @return The the last segment of the package name.
     */
    public String getLastSegmentName();

    /**
     * Returns <code>true</code> if this IIpsPackageFragement is the default-package. The
     * default-package is the one with an empty String as name ("").
     */
    public boolean isDefaultPackage();

    /**
     * Returns <code>true</code> if this IIpsPackageFragement has at least one IIpsPackageFragement
     * as a child.
     * 
     * @throws CoreException
     */
    public boolean hasChildIpsPackageFragments() throws CoreException;

    /**
     * Returns a handle to the sort order file of this IpsPackageFragment.
     */
    public IFile getSortOrderFile();

}
