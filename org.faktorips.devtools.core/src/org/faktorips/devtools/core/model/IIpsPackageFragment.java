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

import java.io.InputStream;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An IPS package fragment is a portion of the workspace corresponding to an entire package,
 * or to a portion thereof. The distinction between a package fragment and a package
 * is that a package with some name is the union of all package fragments in the class path
 * which have the same name.
 */
public interface IIpsPackageFragment extends IIpsElement {
    
    /**
     * The char used as separator for subpackages.
     */
    public final static char SEPARATOR = '.';
    
    /**
     * Returns the packagefragment which contains this one or null if this one is the default-package.
     */
    public IIpsPackageFragment getParentIpsPackageFragment();
    
    /**
     * Returns all packagfragments which are contained in this one.
     * @throws CoreException 
     */
    public IIpsPackageFragment[] getIpsChildPackageFragments() throws CoreException;

    /**
     * Returns all <code>IResource</code>s that do not correspond to
     * <code>IIpsPackageFragment</code>s contained in this PackageFragment. Returns an empty
     * array if no such resources are found.
     * @throws CoreException if the members of the corresponding resource cannot be accessed.
     */
    public IResource[] getNonIpsResources() throws CoreException;
    
    /**
     * Returns the package fragment root this package fragment belongs to.
     */
    public IIpsPackageFragmentRoot getRoot();
    
    /**
     * Returns an <code>org.eclipse.core.runtime.IPath</code> object representing for the package fragment name.
     */
    public IPath getRelativePath();
    
    /**
     * Returns a handle to the IPS source file with the given name. If the provided 
     * name doesn't have a file extension that fits to an IpsSrcFile null will 
     * be returned.  
     */
    public IIpsSrcFile getIpsSrcFile(String name);
    
    /**
     * Creates the IpsSrcFile with the indicated name. 
     * 
	 * @param name the file name
	 * @param source input stream providing the file's content
	 * @param force a flag controlling how to deal with resources that
	 *    are not in sync with the local file system
	 * @param monitor the given progress monitor
     * 
	 * @throws CoreException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This folder does not exist</li>
	 * <li> A <code>CoreException</code> occurred while creating an underlying resource
	 * <li> This root folder is read only
	 * <li> The name is not a valid src file name
	 * </ul>
     */
    public IIpsSrcFile createIpsFile(String name, InputStream source, boolean force, IProgressMonitor monitor) throws CoreException;
    
    /**
     * Creates the IpsSrcFile with the indicated name. 
     * 
	 * @param name the file name
	 * @param content the file's content
	 * @param force a flag controlling how to deal with resources that
	 *    are not in sync with the local file system
	 * @param monitor the given progress monitor
     * 
	 * @throws CoreException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This folder does not exist</li>
	 * <li> A <code>CoreException</code> occurred while creating an underlying resource
	 * <li> This root folder is read only
	 * <li> The name is not a valid src file name
	 * </ul>
     */
    public IIpsSrcFile createIpsFile(String name, String content, boolean force, IProgressMonitor monitor) throws CoreException;
    
    /**
     * Creates a IpsSrcFile that contains an IpsObject of the indicated type and
     * with the indicated name. The filename is constructed by appending the type
     * specific file extension to the object name (separated by a dot).
     * 
     * @param type the object's type
	 * @param name the file name
	 * @param force a flag controlling how to deal with resources that
	 *    are not in sync with the local file system
	 * @param monitor the given progress monitor
     * 
	 * @throws CoreException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This folder does not exist</li>
	 * <li> A <code>CoreException</code> occurred while creating an underlying resource
	 * <li> This root folder is read only
	 * <li> The name is not a valid object name
	 * </ul>
     */
    public IIpsSrcFile createIpsFile(IpsObjectType type, String ipsObjectName, boolean force, IProgressMonitor monitor) throws CoreException;

    /**
     * Creates a new IpsSrcFile based on a given template. The filename is constructed by 
     * appending the type specific file extension to the given object name (separated by a dot). 
     * The content of the IpsSrcFile is copied from the given template. If the template is a 
     * ITimedIpsObject, only the generation that is valid at the given date is copied. If no
     * generation is valid at the given date because the date lies before any valid generation
     * of the given template, the first generation of the given template is used.  
     * 
	 * @param name the file name
	 * @param template the source for the contents to copy from
	 * @param date the date to find the generation effective on if 
	 *             template is instance of ITimedIpsObject). Otherwise this parameter 
	 *             is ignored.
	 * @param force a flag controlling how to deal with resources that
	 *    are not in sync with the local file system
	 * @param monitor the given progress monitor
     * 
	 * @throws CoreException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This folder does not exist</li>
	 * <li> A <code>CoreException</code> occurred while creating an underlying resource
	 * <li> This root folder is read only
	 * <li> The name is not a valid object name
	 * <li> The template has no generation valid on the given date
	 * </ul>
     */
    public IIpsSrcFile createIpsFileFromTemplate(String name, IIpsObject template, GregorianCalendar date, boolean force, IProgressMonitor monitor) throws CoreException;
    
    /**
     * Returns the (unqualified) name of the corresponding folder in the file system.
     */
	public String getFolderName();
	
	/**
	 * Returns <code>true</code> if this IIpsPackageFragement is the default-package. 
	 * The default-package is the one with an empty String as name ("").
	 */
	public boolean isDefaultPackage();
}
