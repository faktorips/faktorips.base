package org.faktorips.devtools.core.model;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;

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
     * The type constant for the Java package fragment that contains the 
     * implementation classes.
     *  
     * @see getJavaPackageFragment(int)
     * @deprecated
     */
    public final static int JAVA_PACK_IMPLEMENTATION = 0;
    
    /**
     * The type constant for the Java package fragment that contains the 
     * published interfaces.
     * 
     * @see getJavaPackageFragment(int)
     * @deprecated
     */
    public final static int JAVA_PACK_PUBLISHED_INTERFACE = 128;
    
    /**
     * The type constant for the Java package fragment that contains the 
     * extension classes.
     *  
     * @see getJavaPackageFragment(int)
     * @deprecated
     */
    public final static int JAVA_PACK_EXTENSION = 256;
    
    /**
     * Returns the packagefragment which contains this one or null if this one is the default-package.
     */
    public IIpsPackageFragment getIpsParentPackageFragment();
    
    /**
     * Returns all packagfragments which are contained in this one.
     * @throws CoreException 
     */
    public IIpsPackageFragment[] getIpsChildPackageFragments() throws CoreException;
    
    /**
     * Returns the package fragment root this package fragment belongs to.
     */
    public IIpsPackageFragmentRoot getRoot();
    
    /**
     * Returns an <code>org.eclipse.core.runtime.IPath</code> object representing for the package fragment name.
     */
    public IPath getRelativePath();
    
    /**
     * Returns the Java package fragment that corresponds to the IPS package
     * fragment and is of the indicated kind.
     * 
     * @param kind A kind constant identifying the kind of package fragment.
     * @return The corresponding package fragment. Note that the package fragment might not
     * exists!
     * @deprecated
     * @throws IllegalArgumentException if the kind is illegal.   
     */
    public IPackageFragment getJavaPackageFragment(int kind) throws CoreException;
    
    /**
     * Returns all Java package fragments that correspond to this IPS package.
     * Note that the Java packages might not exists.
     * 
     * @deprecated
     */
    public IPackageFragment[] getAllJavaPackageFragments() throws CoreException;
    
    /**
     * Returns a handle to the IPS source file with the given name.  
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
    public IIpsSrcFile createIpsFile(IpsObjectType type, String pdObjectName, boolean force, IProgressMonitor monitor) throws CoreException;

    /**
     * Returns the (unqualified) name of the corresponding folder in the file system.
     */
	public String getFolderName();
	
	/**
	 * Returns <code>true</code> if this IIpsPackageFragement is the default-package. 
	 * The default-package is the one with an empty String as name ("").
	 */
	public boolean isDefaultPacakge();
}
