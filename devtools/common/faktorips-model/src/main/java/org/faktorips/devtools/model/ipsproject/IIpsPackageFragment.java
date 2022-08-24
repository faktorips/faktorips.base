/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import java.io.InputStream;
import java.util.Comparator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

/**
 * An IPS package fragment is a portion of the workspace corresponding to an entire package, or to a
 * portion thereof. The distinction between a package fragment and a package is that a package with
 * some name is the union of all package fragments in the IPS object path which have the same name.
 */
public interface IIpsPackageFragment extends IIpsElement {

    /**
     * Constant that represents the name of the default package to make it explicit that the default
     * package name is an empty string.
     */
    String NAME_OF_THE_DEFAULT_PACKAGE = ""; //$NON-NLS-1$

    /**
     * The char used as separator for sub packages.
     */
    char SEPARATOR = '.';

    /**
     * The name of the sort order file.
     */
    String SORT_ORDER_FILE_NAME = ".sortorder"; //$NON-NLS-1$

    /**
     * Returns all IPS source files in the package. Returns an empty array if the package is empty.
     * 
     * @throws IpsException if an error occurs while searching for the files.
     */
    IIpsSrcFile[] getIpsSrcFiles() throws IpsException;

    /**
     * Returns the package fragment which contains this one or null if this one is the
     * default-package.
     */
    IIpsPackageFragment getParentIpsPackageFragment();

    /**
     * Returns all package fragments which are contained in this one.
     */
    IIpsPackageFragment[] getChildIpsPackageFragments() throws IpsException;

    /**
     * Returns the {@link Comparator} to be used when displaying this package fragment's contents in
     * an ordered fashion.
     */
    Comparator<IIpsElement> getChildOrderComparator();

    /**
     * Returns all <code>IResource</code>s that do not correspond to
     * <code>IIpsPackageFragment</code>s contained in this PackageFragment. Returns an empty array
     * if no such resources are found.
     * 
     * @throws IpsException if the members of the corresponding resource cannot be accessed.
     */
    AResource[] getNonIpsResources() throws IpsException;

    /**
     * Returns the package fragment root this package fragment belongs to.
     */
    IIpsPackageFragmentRoot getRoot();

    /**
     * Returns an <code>org.eclipse.core.runtime.IPath</code> object representing for the package
     * fragment name.
     */
    IPath getRelativePath();

    /**
     * Returns a handle to the IPS source file with the given name. If the provided name doesn't
     * have a file extension that fits to an IpsSrcFile <code>null</code> will be returned.
     */
    IIpsSrcFile getIpsSrcFile(String name);

    /**
     * Returns a handle to the IPS source file for the given filenameWithoutExtension. The file
     * extension is derived from the IPS object type.
     * 
     * @throws NullPointerException if type is <code>null</code>.
     */
    IIpsSrcFile getIpsSrcFile(String filenameWithoutExtension, IpsObjectType type);

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
     * @throws IpsException if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This folder does not exist</li>
     *             <li>A <code>CoreException</code> occurred while creating an underlying resource
     *             <li>This root folder is read only
     *             <li>The name is not a valid source file name
     *             </ul>
     */
    IIpsSrcFile createIpsFile(String name, InputStream source, boolean force, IProgressMonitor monitor)
            throws IpsException;

    /**
     * Creates the IpsSrcFile with the indicated name.
     * 
     * @param name the file name
     * @param content the file's content
     * @param force a flag controlling how to deal with resources that are not in sync with the
     *            local file system
     * @param monitor the given progress monitor
     * 
     * @throws IpsException if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This folder does not exist</li>
     *             <li>A <code>CoreException</code> occurred while creating an underlying resource
     *             <li>This root folder is read only
     *             <li>The name is not a valid source file name
     *             </ul>
     */
    IIpsSrcFile createIpsFile(String name, String content, boolean force, IProgressMonitor monitor)
            throws IpsException;

    /**
     * Creates a IpsSrcFile that contains an IpsObject of the indicated type and with the indicated
     * name. The filename is constructed by appending the type specific file extension to the object
     * name (separated by a dot).
     * 
     * @param type the object's type
     * @param ipsObjectName the file name
     * @param force a flag controlling how to deal with resources that are not in sync with the
     *            local file system
     * @param monitor the given progress monitor
     * 
     * @throws IpsException if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This folder does not exist</li>
     *             <li>A <code>CoreException</code> occurred while creating an underlying resource
     *             <li>This root folder is read only
     *             <li>The name is not a valid object name
     *             </ul>
     */
    IIpsSrcFile createIpsFile(IpsObjectType type, String ipsObjectName, boolean force, IProgressMonitor monitor)
            throws IpsException;

    /**
     * Creates a IpsPackageFragment below this one with the indicated name.
     * 
     * @param name the sub-package name
     * @param force a flag controlling how to deal with resources that are not in sync with the
     *            local file system
     * @param monitor the given progress monitor
     * 
     * @throws IpsException if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This folder does not exist</li>
     *             <li>A <code>CoreException</code> occurred while creating an underlying resource
     *             <li>This root folder is read only
     *             <li>The name is not a valid package name
     *             </ul>
     */
    IIpsPackageFragment createSubPackage(String name, boolean force, IProgressMonitor monitor)
            throws IpsException;

    /**
     * Returns an {@link IIpsPackageFragment} that is located in this package and is identified by
     * the name of this package extended with the sub package name. The name of the new package is
     * 'name.subPackageName'.
     * <p>
     * In contrast to {@link #createSubPackage(String, boolean, IProgressMonitor)} this method does
     * not create the folders for the package if they do not exists
     * 
     * @param subPackageFragmentName The last segment of the name of the sub package fragment
     * @return The {@link IIpsPackageFragment} that is located under this package and have the
     *             specified name segment appended.
     */
    IIpsPackageFragment getSubPackage(String subPackageFragmentName);

    /**
     * @return The the last segment of the package name.
     */
    String getLastSegmentName();

    /**
     * Returns <code>true</code> if this IIpsPackageFragement is the default-package. The
     * default-package is the one with an empty String as name ("").
     */
    boolean isDefaultPackage();

    /**
     * Returns <code>true</code> if this IIpsPackageFragement has at least one IIpsPackageFragement
     * as a child.
     */
    boolean hasChildIpsPackageFragments() throws IpsException;

    /**
     * Deletes this package fragment by deleting
     * <ul>
     * <li>all child package fragments
     * <li>all contained {@link IIpsSrcFile IIpsSrcFiles}
     * <li>the corresponding resource
     * </ul>
     * 
     * @throws UnsupportedOperationException If the package fragment is stored in an archive
     */
    @Override
    void delete() throws IpsException;

}
