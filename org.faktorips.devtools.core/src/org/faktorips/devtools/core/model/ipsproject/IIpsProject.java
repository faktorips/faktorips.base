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

import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.internal.model.DynamicValueDatatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.MessageList;

/**
 * Project to develop IPS objects.
 */
public interface IIpsProject extends IIpsElement, IProjectNature {

    /**
     * The id of the Faktor-IPS project nature.
     */
    public final static String NATURE_ID = IpsPlugin.PLUGIN_ID + ".ipsnature"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "IPSPROJECT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the project's property file is missing.
     */
    public final static String MSGCODE_MISSING_PROPERTY_FILE = MSGCODE_PREFIX + "MissingPropertyFile"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the project's property file's contents is not
     * parsable.
     */
    public final static String MSGCODE_UNPARSABLE_PROPERTY_FILE = MSGCODE_PREFIX + "UnparsablePropertyFile"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that no version manager was installed for a required
     * feature.
     */
    public final static String MSGCODE_NO_VERSIONMANAGER = MSGCODE_PREFIX + "NoVersionManager"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the version of the required feature is too low.
     */
    public final static String MSGCODE_VERSION_TOO_LOW = MSGCODE_PREFIX + "VersionTooLow"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the version of the required feature is only
     * compatible
     */
    public final static String MSGCODE_COMPATIBLE_VERSIONS = MSGCODE_PREFIX + "CompatibleVersions"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the version of the required feature is *not*
     * compatible.
     */
    public final static String MSGCODE_INCOMPATIBLE_VERSIONS = MSGCODE_PREFIX + "IncompatibleVersions"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the migration information for this project for a
     * feature is invalid.
     */
    public final static String MSGCODE_INVALID_MIGRATION_INFORMATION = MSGCODE_PREFIX + "InvalidMigrationInformation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the corresponding Java Project has build path
     * errors.
     */
    public final static String MSGCODE_JAVA_PROJECT_HAS_BUILDPATH_ERRORS = MSGCODE_PREFIX
            + "JavaProjectHasBuildPathErrors"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the base package generated name is duplicated
     * specified in different projects.
     */
    public final static String MSGCODE_DUPLICATE_TOC_FILE_PATH_IN_DIFFERENT_PROJECTS = MSGCODE_PREFIX
            + "DuplicateTocFilePathInDifferentProjects"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is a cycle in the ips object path.
     */
    public final static String MSGCODE_CYCLE_IN_IPS_OBJECT_PATH = MSGCODE_PREFIX + "CycleInIpsObjectPath"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exist two runtime ids which collide.
     */
    public final static String MSGCODE_RUNTIME_ID_COLLISION = MSGCODE_PREFIX + "RuntimeIdCollision"; //$NON-NLS-1$

    /**
     * Returns the corresponding platform project.
     */
    public IProject getProject();

    /**
     * Returns the corresponding Java project.
     */
    public IJavaProject getJavaProject();

    /**
     * Returns a new ClassLoader that loads the classes that are accessible via the Java project's
     * build path.
     * 
     * @throws CoreException if an error occurs while creating the classloader.
     */
    public ClassLoader getClassLoaderForJavaProject() throws CoreException;

    /**
     * Returns <code>true</code> if the corresponding Java Project doesn't contain any errors that
     * prevent executing the Java bytecode. A Java project is considered error free if it doesn't
     * contain any problem marker indicating that the Java sourcecode couldn't be compiled. Returns
     * <code>null</code> if either the corresponding platform project is closed, the Java
     * corresponding project doesn't exist or it hasn't been build yet. Returns <code>false</code>
     * otherwise.
     * 
     * @param checkRequiredJavaProjects <code>true</code> if the Java project's required by this
     *            Java project are also checked.
     * 
     * @throws CoreException if an error occurs while checking the Java project.
     * 
     * @see IJavaModelMarker#BUILDPATH_PROBLEM_MARKER
     * @see IJavaModelMarker#JAVA_MODEL_PROBLEM_MARKER
     */
    public Boolean isJavaProjectErrorFree(boolean checkRequiredJavaProjects) throws CoreException;

    /**
     * Returns all ips projects referenced in the project's ips object path.
     * 
     * @throws CoreException if an error occurs.
     * 
     * @see IIpsObjectPath
     */
    public IIpsProject[] getReferencedIpsProjects() throws CoreException;

    /**
     * Returns <code>true</code> if this project is referenced by the other project. Returns
     * <code>false</code> if the other project is <code>null</code> or the other project is this
     * project.
     * 
     * @param considerIndirect <code>true</code> if the method should return <code>true</code> for
     *            indirect references.
     * 
     * @throws CoreException if an error occurs.
     */
    public boolean isReferencedBy(IIpsProject otherProject, boolean considerIndirect) throws CoreException;

    /**
     * Returns all ips projects that reference this one in their ips object path.
     * 
     * @param includeIndirect <code>true</code> if also indirect references should
     * 
     * @throws CoreException if an errors occurs while searching the projects
     */
    public IIpsProject[] getReferencingProjects(boolean includeIndirect) throws CoreException;

    /**
     * Returns all {@link IIpsProject}s that reference this ips project, excluding projects that are
     * referenced by another result. If you visualize the project dependencies as a directed graph,
     * only the leaves of this graph are returned.
     * <p>
     * This method promises the best performance for searching referencing types. If you use any
     * find method with all the returned project, you could find every type referencing this project
     * without searching an object path twice.
     * <p>
     * If there is no referencing project, this project is returned.
     * <p>
     * Although you get a minimal set of ips projects for your search, you have to look for
     * duplicate results. E.g. in your project structure you have a project called <i>ipsProject</i>
     * and you have two projects <i>RefProject1</i> and <i>RefProject2</i> that both referencing
     * <i>ipsProject</i>. In <i>ipsProject</i> there is a <code>ProductCmptType</code> and a derived
     * <code>ProductCmpt</code> you want to find. Because there are maybe other
     * <code>ProductCmpt</code>s referencing you <code>ProductCmptType</code> you have to search in
     * all ips projects, referencing your <i>ipsProject</i>. This is exactly the usage of this
     * method. The problem is, you get two projects, both referencing <i>ipsProject</i>. If you add
     * all results of search in project <i>RefProject1</i> and <i>RefProject2</i> you found the
     * <code>ProductCmpt</code> in <i>ipsProject</i> twice.
     * 
     * 
     * @return The ips projects referencing this project excluding projects that are referenced by
     *         another result
     * @throws CoreException if an error occours while searching the project
     */
    public IIpsProject[] getReferencingProjectLeavesOrSelf() throws CoreException;

    /**
     * Returns <code>true</code> if this project depends on the other project, because it is
     * referenced <strong>directly or indirectly</strong> in the project's object path. Returns
     * <code>false</code>, if otherProject is <code>null</code>. Returns <code>false</code> if
     * otherProject equals this project.
     * 
     * @throws CoreException
     * 
     * @see IIpsObjectPath
     */
    public boolean isReferencing(IIpsProject otherProject) throws CoreException;

    /**
     * Returns <code>true</code> if the project can be build / Java sourcecode can be generated.
     * Returns <code>false</code> otherwise. E. g. if the project's properties file is missing the
     * project can't be build.
     */
    public boolean canBeBuild();

    /**
     * Returns the project's properties. Note that the method returns a copy of the properties, not
     * a reference. In order to update the project's properties the modified properties object has
     * to be set in the project via setProperties().
     */
    public IIpsProjectProperties getProperties();

    /**
     * Returns a read only object of the project's properties.
     */
    public IIpsProjectProperties getReadOnlyProperties();

    /**
     * Sets the project's properties and stores the properties in the project's property file
     * (".ipsproject").
     * 
     * @throws CoreException if an error occurs while saving the properties to the file.
     */
    public void setProperties(IIpsProjectProperties properties) throws CoreException;

    /**
     * Returns the file that stores the project's properties. Note that the file need not exist.
     */
    public IFile getIpsProjectPropertiesFile();

    /**
     * Returns the charset/encoding in that the IIpsSrcFile contents is stored.
     */
    public String getXmlFileCharset();

    /**
     * Returns the charset/encoding in that plain text files are stored.
     */
    public String getPlainTextFileCharset();

    /**
     * Returns an {@link ExtendedExprCompiler} instance that is configured with the default set
     * operations and functions. Functions that are added via the FunctionResolver extension point
     * are also included.
     */
    public ExtendedExprCompiler newExpressionCompiler();

    /**
     * Returns a copy of the project's object path. Note that a copy and not a reference is
     * returned. If you want to update the project's path, the updated object path has to b e
     * explicitly set on the project via the <code>setIpsObjectPath()</code> method.
     * 
     * @throws CoreException if an error occurs while retrieving the path
     */
    public IIpsObjectPath getIpsObjectPath() throws CoreException;

    /**
     * Returns <code>true</code> if the given object is accessible via the project's object path,
     * otherwise <code>false</code>.
     * <p>
     * If the ips object is stored in one the project's source folders, it is of cource accessible
     * via the project's object path. But if the ips object belongs to a different project, it is
     * only accessible via the project's object path if the other project is (directly or
     * indirectly) referenced in the path.
     * <p>
     * If two objects with the same qualified name exist on the ips object path, only the first one
     * (defined by the order of the object path entries) is accessible, the second one is shadowed
     * by the first one.
     * <p>
     * If the given ipsObject is <code>null</code>, the method returns <code>false</code>.
     * 
     * @throws CoreException if an error occurs while searching the path
     */
    public boolean isAccessibleViaIpsObjectPath(IIpsObject ipsObject) throws CoreException;

    /**
     * Returns all output folders specified in the project's object path.
     */
    public IFolder[] getOutputFolders() throws CoreException;

    /**
     * Sets the id of the current artefact builder.
     * 
     * @deprecated use IIpsProjectProperties to change the project properties
     */
    @Deprecated
    public void setCurrentArtefactBuilderSet(String id) throws CoreException;

    /**
     * Sets the new object path.
     */
    public void setIpsObjectPath(IIpsObjectPath newPath) throws CoreException;

    /**
     * Set the value datatypes allowed in the project.
     * 
     * @deprecated use IIpsProjectProperties to change the project properties
     */
    @Deprecated
    public void setValueDatatypes(ValueDatatype[] types) throws CoreException;

    /**
     * Returns the language in that the expression language's functions are used. E.g. the
     * <code>if</code> function is called IF in english, but WENN in german.
     */
    public Locale getExpressionLanguageFunctionsLanguage();

    /**
     * Returns the naming convention for changes over time used in the generated Java sourcecode.
     */
    public IChangesOverTimeNamingConvention getChangesInTimeNamingConventionForGeneratedCode();

    /**
     * Returns <code>true</code> if this project contains a model defininition, otherwise
     * <code>false</code>.
     */
    public boolean isModelProject();

    /**
     * Returns <code>true</code> if this project contains a product defininition (that means it
     * contains product components), otherwise <code>false</code>.
     */
    public boolean isProductDefinitionProject();

    /**
     * Returns <code>true</code> if this is a project that supports persistence, otherwise
     * <code>false</code>. Persistent projects can store and retrieve policy component types to/from
     * a relational database.
     */
    public boolean isPersistenceSupportEnabled();

    /**
     * Returns the root folder with the indicated name.
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot(String name);

    /**
     * Returns the project's package fragment roots or an empty array if none is found.
     */
    public IIpsPackageFragmentRoot[] getIpsPackageFragmentRoots() throws CoreException;

    /**
     * Searchs and returns the root folder by the indicated name.<br>
     * Returns <code>null</code> if the root doesn't exists or an error occurs during search.
     */
    public IIpsPackageFragmentRoot findIpsPackageFragmentRoot(String name);

    /**
     * Returns all <code>IResource</code> objects that do not correspond to
     * <code>IpsPackageFragmentRoots</code> contained in this Project. Returns an empty array if no
     * such resources are found.
     * <p>
     * This method filters out folders that are output locations of the javaproject corresponding to
     * this <code>IIpsProject</code>. Both default output locations of the javaproject and output
     * locations of classpath entries are examined.
     */
    public IResource[] getNonIpsResources() throws CoreException;

    /**
     * Returns the project's package fragment roots contains source code or an empty array if none
     * is found.
     */
    public IIpsPackageFragmentRoot[] getSourceIpsPackageFragmentRoots() throws CoreException;

    /**
     * Returns the first object with the indicated type and qualified name found on the objectpath.
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException;

    /**
     * Returns the first object with the indicated qualified name type found on the objectpath.
     */
    public IIpsObject findIpsObject(QualifiedNameType nameType) throws CoreException;

    /**
     * Returns the first policy component type with the given qualified name found on the path.
     * Returns <code>null</code> if no such type is found. Returns <code>null</code> if the
     * qualified name is <code>null</code>.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IPolicyCmptType findPolicyCmptType(String qualifiedName) throws CoreException;

    /**
     * Returns the first product component type with the given qualified name found on the path.
     */
    public IProductCmptType findProductCmptType(String qualifiedName) throws CoreException;

    /**
     * Returns the product component with the given qualified name or <code>null</code> if no such
     * product component exists. If more than one product component with the given name exists, the
     * first one found is returned.
     * 
     * @param qualifiedName The qualified name to find the product component for.
     * 
     * @return The first product component identified by the given qualified name that has been
     *         found.
     * 
     * @throws CoreException If an error occurs during the search.
     */
    public IProductCmpt findProductCmpt(String qualifiedName) throws CoreException;

    /**
     * Returns the enum type with the given qualified name or <code>null</code> if no such enum type
     * exists. If more than one enum type with the given name exists, the first one found is
     * returned.
     * 
     * @param qualifiedName The qualified name to find the enum type for.
     * 
     * @return The first enum type identified by the given qualified name that has been found.
     * 
     * @throws CoreException If an error occurs during the search.
     * @throws NullPointerException If qualifiedName is <code>null</code>.
     */
    public IEnumType findEnumType(String qualifiedName) throws CoreException;

    /**
     * Returns all {@link IEnumType} objects found in this IPS project. An empty list will be
     * returned if none is found.
     * 
     * @param includeAbstract
     * @param includeNotContainingValues
     * 
     * @throws CoreException if an exception occurs while processing the search.
     */
    public List<IEnumType> findEnumTypes(boolean includeAbstract, boolean includeNotContainingValues)
            throws CoreException;

    /**
     * Returns the first enumeration content that is found within this ips project that references
     * the provided enumeration type.
     * 
     * @throws CoreException if an exception occurs while processing the search
     * @throws NullPointerException if the provided parameter is <code>null</code>
     */
    public IEnumContent findEnumContent(IEnumType enumType) throws CoreException;

    /**
     * Returns the product component with the given runtime id or <code>null</code> if no such
     * product component exists. If more than one product component with the given id exists, the
     * first one found is returned.
     * 
     * @param runtimeId The runtime-id to find the product component for.
     * @throws CoreException if an error occurs during search.
     */
    public IProductCmpt findProductCmptByRuntimeId(String runtimeId) throws CoreException;

    /**
     * Fills the provided <code>java.util.List</code> with <code>ITableContent</code> objects that
     * are found in the workspace and are based on the provided <code>ITableStructure</code>.
     * 
     * @throws CoreException if an error occurs during search.
     */
    public void findTableContents(ITableStructure structure, List<ITableContents> tableContents) throws CoreException;

    /**
     * Returns all <code>IIpsSrcFile</code>s representing <code>TableContents</code> that are based
     * on the given <code>TableStructure</code> in this and all referenced projects. If the
     * <code>structure</code> is null, the method returns all TableContentsSrcFiles found in the
     * classpath.
     * 
     * @param structure The product components type product component will be searched for.
     * @param includeSubtypes If <code>true</code> is passed also product component that are based
     *            on subtypes of the given product components type are returned, otherwise only
     *            product components that are directly based on the given type are returned.
     * 
     */
    public IIpsSrcFile[] findAllTableContentsSrcFiles(ITableStructure structure) throws CoreException;

    /**
     * Returns the first ips source file on the ips object path with the the indicated qualified
     * name and type. Returns <code>null</code> if no such file was found.
     */
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType qNameType) throws CoreException;

    /**
     * Returns the first ips source file with the the indicated ips object type and qualified name
     * found on the objectpath.<br>
     * Returns <code>null</code> if the source file wasn't found (not exists).
     */
    public IIpsSrcFile findIpsSrcFile(IpsObjectType type, String qualifiedName) throws CoreException;

    //
    // Find methods with result array
    //

    /**
     * Returns all objects of the given type found on the classpath.
     * 
     * @deprecated use IIpsProject#findIpsSrcFiles(IProductCmptType, boolean) due to better
     *             performance
     */
    @Deprecated
    public IIpsObject[] findIpsObjects(IpsObjectType type) throws CoreException;

    /**
     * Returns all ips source files of the given type found on the classpath.
     */
    public IIpsSrcFile[] findIpsSrcFiles(IpsObjectType type) throws CoreException;

    /**
     * Returns all IpsObjects within this IpsProject and the IpsProjects this one depends on.
     * 
     * @deprecated use IIpsProject#findAllIpsSrcFiles(IProductCmptType, boolean) due to better
     *             performance
     */
    @Deprecated
    public void findAllIpsObjects(List<IIpsObject> result) throws CoreException;

    /**
     * Returns all ips source files within this IpsProject and the IpsProjects this one depends on.
     */
    public void findAllIpsSrcFiles(List<IIpsSrcFile> result) throws CoreException;

    /**
     * Returns all ips source files within this IpsProject and the IpsProjects this one depends on
     * and match the given filter (object type list).
     */
    public void findAllIpsSrcFiles(List<IIpsSrcFile> result, IpsObjectType[] filter) throws CoreException;

    /**
     * Puts all {@link IIpsSrcFile}s within this {@link IIpsProject} in the provided result list
     * that match the selection parameters.
     * 
     * @param result the ips source files that match the selection parameters are put in this list
     * @param ipsObjectType the selection parameter for the ips object type that is expected in the
     *            ips source file
     * @param packageFragment the package fragment where to look for the ips source file. If
     *            <code>null</code> is specified all package fragments are considered
     * @throws CoreException if an exception ocurs while searching
     */
    public void findAllIpsSrcFiles(List<IIpsSrcFile> result, IpsObjectType ipsObjectType, String packageFragment)
            throws CoreException;

    /**
     * Adds all ips source files that are accessible through ips source folder entries to the result
     * list.
     */
    public void collectAllIpsSrcFilesOfSrcFolderEntries(List<IIpsSrcFile> result) throws CoreException;

    /**
     * Returns all objects of the given type starting with the given prefix found on the ipsobject
     * path.
     * 
     * @deprecated use IIpsProject#findIpsSrcFilesStartingWith(IProductCmptType, boolean) due to
     *             better performance
     */
    @Deprecated
    public IIpsObject[] findIpsObjectsStartingWith(IpsObjectType type, String prefix, boolean ignoreCase)
            throws CoreException;

    /**
     * Returns all ips source files of the given type starting with the given prefix found on the
     * ipsobject path.
     */
    public IIpsSrcFile[] findIpsSrcFilesStartingWith(IpsObjectType type, String prefix, boolean ignoreCase)
            throws CoreException;

    /**
     * Returns all product components that are based on the given product component type (either
     * directly or because they are based on a subtype of the given type) in this and all referenced
     * projects. If productCmptType is null, the method returns all product components found on the
     * classpath.
     * 
     * @param pcTypeName The product components type product component will be searched for.
     * @param includeSubtypes If <code>true</code> is passed also product component that are based
     *            on subtypes of the given product components type are returned, otherwise only
     *            product components that are directly based on the given type are returned.
     * 
     * @deprecated use IIpsProject#findAllProductCmptSrcFiles(IProductCmptType, boolean) due to
     *             better performance
     */
    @Deprecated
    public IProductCmpt[] findAllProductCmpts(IProductCmptType productCmptType, boolean includeSubtypes)
            throws CoreException;

    /**
     * Returns all ips source files represents product components that are based on the given
     * product component type (either directly or because they are based on a subtype of the given
     * type) in this and all referenced projects. If productCmptType is null, the method returns all
     * source files (product components) found on the classpath.
     * 
     * @param pcTypeName The product components type product component will be searched for.
     * @param includeSubtypes If <code>true</code> is passed also product component that are based
     *            on subtypes of the given product components type are returned, otherwise only
     *            product components that are directly based on the given type are returned.
     * 
     */
    public IIpsSrcFile[] findAllProductCmptSrcFiles(IProductCmptType productCmptType, boolean includeSubtypes)
            throws CoreException;

    /**
     * Returns all <code>IIpsSrcFile</code>s representing test cases that are based on the given
     * <code>ITestCaseType</code> in this and all referenced projects. If the
     * <code>testCaseType</code> is null, the method returns all TestCaseSrcFiles found in the
     * classpath.
     * 
     * @param testCaseType The <code>TestCaseType</code> to search the <code>TestCase</code>s for
     * @throws CoreException if an exception occurs while searching
     */
    public IIpsSrcFile[] findAllTestCaseSrcFiles(ITestCaseType testCaseType) throws CoreException;

    /**
     * Returns all <code>IIpsSrcFile</code>s representing <code>EnumContent</code>s that are based
     * on the given <code>IEnumType</code> in this and all referenced projects. You could specify
     * whether to include all subtypes of <code>enumType</code> or not If the <code>enumType</code>
     * is null, the method returns all EnumContentSrcFiles found in the classpath.
     * 
     * @param testCaseType The <code>IEnumType</code> to search the <code>IEnumContent</code>s for
     * @param includingSubtypes <code>true</code> if subtypes of <code>enumType</code> should be
     *            included in the search
     * @throws CoreException if an exception occurs while searching
     */
    public IIpsSrcFile[] findAllEnumContentSrcFiles(IEnumType enumType, boolean includingSubtypes) throws CoreException;

    /**
     * Returns all product component generation that refer to the given object, identified by the
     * qualified name type. Returns an empty array if none is found.
     * 
     * @throws CoreException if an exception occurs while searching.
     */
    public IProductCmptGeneration[] findReferencingProductCmptGenerations(QualifiedNameType qualifiedNameType)
            throws CoreException;

    /**
     * Returns all test cases that refer to the product component identified by the given qualified
     * name. Returns an empty array if none is found.
     * 
     * @throws CoreException if an exception occurs while searching.
     */
    public ITestCase[] findReferencingTestCases(String qualifiedProductCmptName) throws CoreException;

    /**
     * Returns the supertype of the given policy component type, and all policy component types that
     * refer to the given policy component type. Returns an empty array if no references or
     * supertypes are found.
     * 
     * @throws CoreException if an exception occurs while searching.
     */
    public IPolicyCmptType[] findReferencingPolicyCmptTypes(IPolicyCmptType pcType) throws CoreException;

    /**
     * Returns all datatypes accessible on the project's path.
     * 
     * @param valuetypesOnly true if only value datatypes should be returned.
     * @param includeVoid true if <code>Datatype.VOID</code> should be included.
     * 
     * @throws CoreException if an exception occurs while searching for the datatypes.
     */
    public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid) throws CoreException;

    /**
     * Returns all datatypes accessible on the project's path.
     * 
     * @param valuetypesOnly true if only value datatypes should be returned.
     * @param includeVoid true if <code>Datatype.VOID</code> should be included.
     * @param includePrimitives true if primitive Datatypes are included.
     * 
     * @throws CoreException if an exception occurs while searching for the datatypes.
     */
    public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid, boolean includePrimitives)
            throws CoreException;

    /**
     * Returns all datatypes accessible on the project's path.
     * 
     * @param valuetypesOnly true if only value datatypes should be returned.
     * @param includeVoid true if <code>Datatype.VOID</code> should be included.
     * @param includePrimitives true if primitive Datatypes are included.
     * @param excludedDatatypes A list of datatypes that should NOT be included, may be
     *            <code>null</code> if none shall be excluded.
     * 
     * @throws CoreException if an exception occurs while searching for the datatypes.
     */
    public Datatype[] findDatatypes(boolean valuetypesOnly,
            boolean includeVoid,
            boolean includePrimitives,
            List<Datatype> excludedDatatypes) throws CoreException;

    /**
     * Returns all datatypes accessible on the project's path.
     * 
     * @param valuetypesOnly true if only value datatypes should be returned.
     * @param includeVoid true if <code>Datatype.VOID</code> should be included.
     * @param includePrimitives true if primitive Datatypes are included.
     * @param excludedDatatypes A list of datatypes that should NOT be included, may be
     *            code>null</code> if none shall be excluded.
     * @param includeAbstract true if abstract datatypes should be included.
     * 
     * @throws CoreException if an exception occurs while searching for the datatypes.
     */
    public Datatype[] findDatatypes(boolean valuetypesOnly,
            boolean includeVoid,
            boolean includePrimitives,
            List<Datatype> excludedDatatypes,
            boolean includeAbstract) throws CoreException;

    /**
     * Returns all enumeration datatypes accessible on the project's ips object path.
     * 
     * @throws CoreException if an exception occurs while searching for the datatypes.
     */
    public EnumDatatype[] findEnumDatatypes() throws CoreException;

    /**
     * Returns the first datatype found on the path with the given qualified name. Returns
     * <code>null</code> if no datatype with the given name is found.
     * 
     * @throws CoreException if an exception occurs while searching for the datatype.
     */
    public Datatype findDatatype(String qualifiedName) throws CoreException;

    /**
     * Returns the first value datatype found on the path with the given qualified name. Returns
     * <code>null</code> if no value datatype with the given name is found. Returns
     * <code>null</code> if qualifiedName is <code>null</code>.
     * 
     * @throws CoreException if an exception occurs while searching for the datatype.
     */
    public ValueDatatype findValueDatatype(String qualifiedName) throws CoreException;

    /**
     * Returns the code generation helper for the given datatype or <code>null</code> if no helper
     * is available for the given datatype.
     */
    public DatatypeHelper getDatatypeHelper(Datatype datatype);

    /**
     * Returns the code generation helper for the given datatype or <code>null</code> if no helper
     * is available for the given datatype.
     * 
     * @param qName The qualified datatype name.
     * 
     * @throws CoreException if an error occurs while searching for the datatype.
     */
    public DatatypeHelper findDatatypeHelper(String qName) throws CoreException;

    /**
     * Returns the value set types that are allowed for the given datatype. The type
     * {@link ValueSetType#UNRESTRICTED} is always returned and is the first element in the array.
     * If datatype is <code>null</code> then an array with <code>UNRESTRICTED</code> is returned.
     * 
     * @throws CoreException if an error occurs while retrieving the value set types, possible
     *             reasons are that the datatypes files can't be read or the xml can't be parsed.
     */
    public List<ValueSetType> getValueSetTypes(ValueDatatype datatype) throws CoreException;

    /**
     * Returns <code>true</code> if the given value set type is available for the given value
     * datatype. Returns <code>false</code> otherwise. Returns <code>false</code> if
     * <code>valueSetType</code> is <code>null</code>. If <code>datatype</code> is <code>null</code>
     * and the <code>valueSetType</code> is unrestricted, this method returns <code>true</code>.
     * 
     * If this method returns <code>true</code>, it is guaranteed that the value set type is
     * returned by {@link #getValueSetTypes(ValueDatatype)}.
     */
    public boolean isValueSetTypeApplicable(ValueDatatype datatype, ValueSetType valueSetType) throws CoreException;

    /**
     * Returns the <code>IpsArtefactBuilderSet</code> that is currently active for this project. If
     * no IpsArtefactBuilderSet is active for this project an <code>EmptyBuilderSet</code> is
     * returned.
     */
    public IIpsArtefactBuilderSet getIpsArtefactBuilderSet();

    /**
     * Reinitializes the <code>IpsProject</code>s <code>IpsArtefactBuilderSet</code>.
     * 
     * @throws CoreException when an exception arises during reinitialization
     */
    public void reinitializeIpsArtefactBuilderSet() throws CoreException;

    /**
     * Returns the runtime id prefix configured for this project.
     */
    public String getRuntimeIdPrefix();

    /**
     * Returns the strategy used to name product components. This method never returns
     * <code>null</code>.
     */
    public IProductCmptNamingStrategy getProductCmptNamingStrategy() throws CoreException;

    /**
     * Returns the strategy used to name database tables used for persisting policy component types.
     * Returns <code>null</code> if persistence support is not enabled for this IPS project.
     */
    public ITableNamingStrategy getTableNamingStrategy() throws CoreException;

    /**
     * Returns the strategy used to name database columns used for persisting policy component
     * types. Returns <code>null</code> if persistence support is not enabled for this IPS project.
     */
    public ITableColumnNamingStrategy getTableColumnNamingStrategy() throws CoreException;

    /**
     * Adds a new <code>DynamicValueDataType</code> to the project at runtime.
     * 
     * @param newDatatype
     * 
     * @throws CoreException
     * 
     * @deprecated Use {@link org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties} to
     *             change the project properties.
     */
    @Deprecated
    public void addDynamicValueDataType(DynamicValueDatatype newDatatype) throws CoreException;

    /**
     * Validates the project and returns the result as list of messages.
     */
    public MessageList validate() throws CoreException;

    /**
     * Returns the naming conventions used for this project.
     */
    public IIpsProjectNamingConventions getNamingConventions();

    /**
     * Checks all given product components against all product components in the ips object path for
     * duplicate runtime ids.
     * 
     * @return A list of messages. For each combination of two product components with duplicate
     *         runtime id a new message is created. This message has only one invalid object
     *         property, containing the product component given to this method.
     * 
     * @throws CoreException if an error occurs during search.
     */
    public MessageList checkForDuplicateRuntimeIds(IIpsSrcFile[] cmptsToCheck) throws CoreException;

    /**
     * Searches all product components in the ips object path for duplicate runtime ids.
     * 
     * @return A list of messages. For each combination of two product components with duplicate
     *         runtime id a new message is created. This message has two invalid object properties,
     *         each containing one of the two product components.
     * 
     * @throws CoreException if an error occurs during search.
     */
    public MessageList checkForDuplicateRuntimeIds() throws CoreException;

    /**
     * Returns <code>true</code> if the given resource will be excluded from the product definition.<br>
     * If the given resource is relevant for the product definition the method returns
     * <code>false</code>. Returns <code>false</code> if resource is <code>null</code>.
     */
    public boolean isResourceExcludedFromProductDefinition(IResource resource);

}
