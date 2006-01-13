package org.faktorips.devtools.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.faktorips.devtools.core.internal.model.MutableClRuntimeRepositoryToc;

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
     * Constant that identifies the Java package fragment root containg the generated Java sourcecode.
     * The Java package root can be obtained via the method <code>getJavaPackageFragmentRoot(int)</code>.
     * 
     * @deprecated
     */
    public final static int JAVA_ROOT_GENERATED_CODE = 0;
    
    /**
     * Constant that identifies the Java package fragment root containg the extension Java sourcecode
     * where te developer using FaktorIPS can add hs own code.
     * The Java package root can be obtained via the method <code>getJavaPackageFragmentRoot(int)</code>.
     * 
     * @deprecated
     */
    public final static int JAVA_ROOT_EXTENSION_CODE = 1;
    
    
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
     * Returns the corresponding Java package fragment root for the given kind. The kind
     * must be one of the constants <code>JAVA_ROOT_GENERATED_CODE</code> or 
     * <code>JAVA_ROOT_EXTENSION_CODE</code>.
     * 
     * @deprecated
     * @param kind The kind of Java package fragment root.
     * @throws IllegalArgumentException if the kind is not one of the constants described above. 
     */
    public IPackageFragmentRoot getJavaPackageFragmentRoot(int kind) throws CoreException;
        
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
    public IIpsObject getIpsObject(IpsObjectType type, String qualifiedName) throws CoreException;

    /**
     * Returns the IPS object with the indicated qualified name type.
     */
    public IIpsObject getIpsObject(QualifiedNameType nameType) throws CoreException;

    /**
     * Returns the prefix of the Java packages for the given package type.
     * <p>
     * The objects in an IPS package fragment are converted into Java sourcecode.
     * For one IPS object, there might be severall Java types in different packages.
     * For example for a policy component type we can generate a published interface
     * and an implementation class. All Java types of the same kind (published interface 
     * or implementation class) from objects in one IPS package fragment are
     * placed in the same Java package fragment. The name of the Java package
     * fragment is derived from the prefix returned by this method and the 
     * IPS package fragment's name. E.g. for an IPS package fragment <i>motor.coverages</i>
     * and a prefix <i>com.faktor10.model</i>,  we obtain for the published interfaces 
     * the following Java package name: <i>com.faktor10.model.motor.coverages</i>.
     * 
     * @deprecated
     */
    public String getJavaPackagePrefix(int kind) throws CoreException;

    /**
     * Returns the prefixes of the Java packages that contain Java types for
     * the IPS objects stored in this IPS package fragment root.
     * 
     * @see getJavaPackagePrefix(int)
     * @deprecated
     */
    public String[] getJavaPackagePrefixes() throws CoreException;
    
    /**
     * Returns the file in the package root's associated output folder, that contains the table of contents.
     * 
     * @throws CoreException if an error occurs while determining the file.
     */
    // TODO This should be moved to the standard builder plugin
    public IFile getTocFileInOutputFolder() throws CoreException;
    
    /**
     * Returns the table of contents for this package fragment root.
     * 
     * @throws CoreException if an error occurs while accessing the toc file.
     */
    // TODO This should be moved to the standard builder plugin
    public MutableClRuntimeRepositoryToc getRuntimeRepositoryToc() throws CoreException;
    
}
