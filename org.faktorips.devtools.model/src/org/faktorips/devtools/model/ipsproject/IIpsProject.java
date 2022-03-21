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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IClassLoaderProvider;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.builder.ExtendedExprCompiler;
import org.faktorips.devtools.model.builder.IDependencyGraph;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.util.Tree;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.model.enumtype.EnumType;

/**
 * Project to develop IPS objects.
 */
public interface IIpsProject extends IIpsElement {

    /**
     * The id of the Faktor-IPS project nature.
     */
    public static final String NATURE_ID = IpsModelActivator.PLUGIN_ID + ".ipsnature"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "IPSPROJECT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the project's property file is missing.
     */
    public static final String MSGCODE_MISSING_PROPERTY_FILE = MSGCODE_PREFIX + "MissingPropertyFile"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the project's property file's contents is not
     * parsable.
     */
    public static final String MSGCODE_UNPARSABLE_PROPERTY_FILE = MSGCODE_PREFIX + "UnparsablePropertyFile"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that no version manager was installed for a required
     * feature.
     */
    public static final String MSGCODE_NO_VERSIONMANAGER = MSGCODE_PREFIX + "NoVersionManager"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the version of the required feature is too low.
     */
    public static final String MSGCODE_VERSION_TOO_LOW = MSGCODE_PREFIX + "VersionTooLow"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the version of the required feature is only
     * compatible
     */
    public static final String MSGCODE_COMPATIBLE_VERSIONS = MSGCODE_PREFIX + "CompatibleVersions"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the version of the required feature is *not*
     * compatible.
     */
    public static final String MSGCODE_INCOMPATIBLE_VERSIONS = MSGCODE_PREFIX + "IncompatibleVersions"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the migration information for this project for a
     * feature is invalid.
     */
    public static final String MSGCODE_INVALID_MIGRATION_INFORMATION = MSGCODE_PREFIX + "InvalidMigrationInformation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the corresponding Java Project has build path
     * errors.
     */
    public static final String MSGCODE_JAVA_PROJECT_HAS_BUILDPATH_ERRORS = MSGCODE_PREFIX
            + "JavaProjectHasBuildPathErrors"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the base package generated name is duplicated
     * specified in different projects.
     */
    public static final String MSGCODE_DUPLICATE_TOC_FILE_PATH_IN_DIFFERENT_PROJECTS = MSGCODE_PREFIX
            + "DuplicateTocFilePathInDifferentProjects"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is a cycle in the IPS object path.
     */
    public static final String MSGCODE_CYCLE_IN_IPS_OBJECT_PATH = MSGCODE_PREFIX + "CycleInIpsObjectPath"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exist two runtime ids which collide.
     */
    public static final String MSGCODE_RUNTIME_ID_COLLISION = MSGCODE_PREFIX + "RuntimeIdCollision"; //$NON-NLS-1$

    /**
     * The file extension for IPS projects.
     */
    public static final String PROPERTY_FILE_EXTENSION = "ipsproject"; //$NON-NLS-1$

    /**
     * The file extension for IPS projects but with a dot added before.
     */
    public static final String PROPERTY_FILE_EXTENSION_INCL_DOT = "." + PROPERTY_FILE_EXTENSION; //$NON-NLS-1$

    /**
     * Returns the corresponding platform project.
     */
    public AProject getProject();

    /**
     * Returns the corresponding Java project.
     */
    public AJavaProject getJavaProject();

    /**
     * Returns the Java naming convention that is used in this project.
     */
    public IJavaNamingConvention getJavaNamingConvention();

    public IClassLoaderProvider getClassLoaderProviderForJavaProject();

    /**
     * Returns a new ClassLoader that loads the classes that are accessible via the Java project's
     * build path. The parent of the new class loader is the System class loader.
     * 
     * @see ClassLoader#getSystemClassLoader()
     */
    public ClassLoader getClassLoaderForJavaProject();

    /**
     * Returns a new ClassLoader that loads the classes that are accessible via the Java project's
     * build path. The parent of the new class loader is the System class loader.
     * 
     * @param parent The parent class loader.
     * 
     * @throws NullPointerException if <code>parent</code> is <code>null</code>.
     * 
     * @since 3.1.0
     */
    public ClassLoader getClassLoaderForJavaProject(ClassLoader parent);

    /**
     * Returns <code>true</code> if the corresponding Java Project doesn't contain any errors that
     * prevent executing the Java byte code. A Java project is considered error free if it doesn't
     * contain any problem marker indicating that the Java sourcecode couldn't be compiled. Returns
     * <code>null</code> if either the corresponding platform project is closed, the corresponding
     * Java project doesn't exist or it hasn't been build yet. Returns <code>false</code> otherwise.
     * 
     * @param checkRequiredJavaProjects <code>true</code> if the Java project's required by this
     *            Java project are also checked.
     * 
     * @throws IpsException if an error occurs while checking the Java project.
     */
    public Boolean isJavaProjectErrorFree(boolean checkRequiredJavaProjects) throws IpsException;

    /**
     * Returns all {@link IIpsProject IpsProjects} that are directly or indirectly referenced in the
     * project's {@link IIpsObjectPath} to this IPS project.
     * 
     * @see IIpsObjectPath
     */
    public List<IIpsProject> getAllReferencedIpsProjects();

    /**
     * Returns all direct {@link IIpsProject IIpsProjects} referenced in the project's
     * {@link IIpsObjectPath} to this IPS project. For all referenced IPS projects @see
     * {@link #getAllReferencedIpsProjects()} .
     * 
     * @see IIpsObjectPath
     */
    public List<IIpsProject> getDirectlyReferencedIpsProjects();

    /**
     * Returns <code>true</code> if this project is referenced by the other project. Returns
     * <code>false</code> if the other project is <code>null</code> or the other project is this
     * project.
     * 
     * @param considerIndirect <code>true</code> if the method should return <code>true</code> for
     *            indirect references.
     * 
     */
    public boolean isReferencedBy(IIpsProject otherProject, boolean considerIndirect);

    /**
     * Returns all IPS projects that reference this one in their IPS object path.
     * 
     * @param includeIndirect <code>true</code> if also indirect references should
     * 
     */
    public IIpsProject[] findReferencingProjects(boolean includeIndirect);

    /**
     * Returns all {@link IIpsProject IIpsProjects} that reference this IPS project, excluding
     * projects that are referenced by another result. If you visualize the project dependencies as
     * a directed graph, only the leaves of this graph are returned.
     * <p>
     * This method promises the best performance for searching referencing types. If you use any
     * find method with all the returned project, you could find every type referencing this project
     * without searching an object path twice.
     * <p>
     * If there is no referencing project, this project is returned.
     * <p>
     * Although you get a minimal set of IPS projects for your search, you have to look for
     * duplicate results. E.g. in your project structure you have a project called
     * <em>ipsProject</em> and you have two projects <em>RefProject1</em> and <em>RefProject2</em>
     * that both referencing <em>ipsProject</em>. In <em>ipsProject</em> there is a
     * <code>ProductCmptType</code> and a derived <code>ProductCmpt</code> you want to find. Because
     * there are maybe other <code>ProductCmpt</code>s referencing you <code>ProductCmptType</code>
     * you have to search in all IPS projects, referencing your <em>ipsProject</em>. This is exactly
     * the usage of this method. The problem is, you get two projects, both referencing
     * <em>ipsProject</em>. If you add all results of search in project <em>RefProject1</em> and
     * <em>RefProject2</em> you found the <code>ProductCmpt</code> in <em>ipsProject</em> twice.
     * 
     * 
     * @return The IPS projects referencing this project excluding projects that are referenced by
     *         another result
     */
    public IIpsProject[] findReferencingProjectLeavesOrSelf();

    /**
     * Returns <code>true</code> if this project depends on the other project, because it is
     * referenced <strong>directly or indirectly</strong> in the project's object path. Returns
     * <code>false</code>, if otherProject is <code>null</code>. Returns <code>false</code> if
     * otherProject equals this project.
     * 
     * @see IIpsObjectPath
     */
    public boolean isReferencing(IIpsProject otherProject);

    /**
     * This method returns the dependency graph for this project. The dependency graph is designed
     * to resolve reverse dependencies. For example it is able to retrieve all objects that have
     * dependencies to an other specified object.
     * <p>
     * The dependency graph is updated by the builder. It is persisted to disk to not create the
     * whole graph after every restart. It is fully rebuild in clean builds.
     * 
     * @return The {@link IDependencyGraph} for this project.
     * @see IDependencyGraph
     */
    public IDependencyGraph getDependencyGraph();

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
     * <p>
     * Creating a copy of the project properties is costly. If read only access is required, use
     * {@link #getReadOnlyProperties()} instead.
     */
    public IIpsProjectProperties getProperties();

    /**
     * Returns a read only copy of the project's properties.
     */
    public IIpsProjectProperties getReadOnlyProperties();

    /**
     * Sets the project's properties and stores the properties in the project's property file
     * (".ipsproject").
     * 
     * @throws IpsException if an error occurs while saving the properties to the file.
     */
    public void setProperties(IIpsProjectProperties properties) throws IpsException;

    /**
     * Returns the file that stores the project's properties. Note that the file need not exist.
     */
    public AFile getIpsProjectPropertiesFile();

    /**
     * Returns the char set / encoding in that the IIpsSrcFile contents is stored.
     */
    public String getXmlFileCharset();

    /**
     * Returns the char set / encoding in that plain text files are stored.
     */
    public String getPlainTextFileCharset();

    /**
     * Returns an {@link ExtendedExprCompiler} instance that is configured with the default set
     * operations and functions. Functions that are added via the FunctionResolver extension point
     * are also included.
     * 
     */
    public ExtendedExprCompiler newExpressionCompiler();

    /**
     * Returns a copy of the project's object path. Note that a copy and not a reference is
     * returned. If you want to update the project's path, the updated object path has to b e
     * explicitly set on the project via the <code>setIpsObjectPath()</code> method.
     */
    public IIpsObjectPath getIpsObjectPath();

    /**
     * Returns <code>true</code> if the given object is accessible via the project's object path,
     * otherwise <code>false</code>.
     * <p>
     * If the IPS object is stored in one the project's source folders, it is of course accessible
     * via the project's object path. But if the IPS object belongs to a different project, it is
     * only accessible via the project's object path if the other project is (directly or
     * indirectly) referenced in the path.
     * <p>
     * If two objects with the same qualified name exist on the IPS object path, only the first one
     * (defined by the order of the object path entries) is accessible, the second one is shadowed
     * by the first one.
     * <p>
     * If the given ipsObject is <code>null</code>, the method returns <code>false</code>.
     */
    public boolean isAccessibleViaIpsObjectPath(IIpsObject ipsObject);

    /**
     * Returns all output folders specified in the project's object path.
     */
    public AFolder[] getOutputFolders();

    /**
     * Sets the new object path.
     */
    public void setIpsObjectPath(IIpsObjectPath newPath) throws IpsException;

    /**
     * Returns the language in that the expression language's functions are used. E.g. the
     * <code>if</code> function is called IF in English, but WENN in German.
     */
    public Locale getFormulaLanguageLocale();

    /**
     * Returns the naming convention for changes over time used in the generated Java sourcecode.
     */
    public IChangesOverTimeNamingConvention getChangesInTimeNamingConventionForGeneratedCode();

    /**
     * Returns <code>true</code> if this project contains a model definition, otherwise
     * <code>false</code>.
     */
    public boolean isModelProject();

    /**
     * Returns <code>true</code> if this project contains a product definition (that means it
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
     * <p>
     * If the project references {@link IIpsContainerEntry container entries} in its IPS object path
     * the result also contains resolved {@link IIpsPackageFragmentRoot roots} that are included in
     * the container.
     * <p>
     * The {@link IIpsPackageFragmentRoot package fragment roots} of referenced projects are not
     * resolved.
     * 
     */
    public IIpsPackageFragmentRoot[] getIpsPackageFragmentRoots();

    /**
     * Returns the project's package fragment roots or an empty array if none is found.
     * <p>
     * If the project references {@link IIpsContainerEntry container entries} in its IPS object path
     * and the parameter <em>resolveContainerEntries</em> is set to <code>true</code> the result
     * also contains resolved {@link IIpsPackageFragmentRoot roots} that are included in the
     * container. If <em>resolveContainerEntries</em> is set to <code>false</code> the
     * {@link IIpsPackageFragmentRoot roots} of the {@link IIpsContainerEntry container entries} are
     * not resolved.
     * <p>
     * The {@link IIpsPackageFragmentRoot package fragment roots} of referenced projects are not
     * resolved.
     */
    public IIpsPackageFragmentRoot[] getIpsPackageFragmentRoots(boolean resolveContainerEntries);

    /**
     * Searches and returns the root folder by the indicated name.<br>
     * Returns <code>null</code> if the root doesn't exists or an error occurs during search.
     */
    public IIpsPackageFragmentRoot findIpsPackageFragmentRoot(String name);

    /**
     * Returns all <code>IResource</code> objects that do not correspond to
     * <code>IpsPackageFragmentRoots</code> contained in this Project. Returns an empty array if no
     * such resources are found.
     * <p>
     * This method filters out folders that are output locations of the Java project corresponding
     * to this <code>IIpsProject</code>. Both default output locations of the Java project and
     * output locations of class path entries are examined.
     */
    public AResource[] getNonIpsResources() throws IpsException;

    /**
     * Returns the project's package fragment roots contains source code or an empty array if none
     * is found.
     */
    public IIpsPackageFragmentRoot[] getSourceIpsPackageFragmentRoots();

    /**
     * Returns the first object with the indicated type and qualified name found on the object path.
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName);

    /**
     * Returns the first object with the indicated qualified name type found on the object path.
     */
    public IIpsObject findIpsObject(QualifiedNameType nameType);

    /**
     * Returns the first policy component type with the given qualified name found on the path.
     * Returns <code>null</code> if no such type is found. Returns <code>null</code> if the
     * qualified name is <code>null</code>.
     * 
     */
    public IPolicyCmptType findPolicyCmptType(String qualifiedName);

    /**
     * Returns the first product component type with the given qualified name found on the path.
     */
    public IProductCmptType findProductCmptType(String qualifiedName);

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
     * @throws IpsException If an error occurs during the search.
     */
    public IProductCmpt findProductCmpt(String qualifiedName) throws IpsException;

    /**
     * Returns the product template with the given qualified name or <code>null</code> if no such
     * product template exists. If more than one product template with the given name exists, the
     * first one found is returned.
     * 
     * @param qualifiedName The qualified name to find the product template for.
     * 
     * @return The first product component identified by the given qualified name that has been
     *         found.
     * 
     */
    public IProductCmpt findProductTemplate(String qualifiedName);

    /**
     * Returns a collection of ipsSrcfiles containing product components with the given unqualified
     * name or an empty collection if no such product component exists.
     * 
     * @param unqualifiedName The unqualified name to find the ipsSrcFiles of product components
     *            for.
     * @return A collection containing ipsSrcfiles which names match the given unqualified name.
     */
    public Collection<IIpsSrcFile> findProductCmptByUnqualifiedName(String unqualifiedName);

    /**
     * Returns the enumeration type with the given qualified name or <code>null</code> if no such
     * enumeration type exists. If more than one enumeration type with the given name exists, the
     * first one found is returned.
     * 
     * @param qualifiedName The qualified name to find the enumeration type for.
     * 
     * @return The first enumeration type identified by the given qualified name that has been
     *         found.
     * 
     * @throws NullPointerException If qualifiedName is <code>null</code>.
     */
    public IEnumType findEnumType(String qualifiedName);

    /**
     * Returns all {@link IEnumType} objects found in this IPS project. An empty list will be
     * returned if none is found.
     */
    public List<IEnumType> findEnumTypes(boolean includeAbstract, boolean includeNotContainingValues);

    /**
     * Returns the first enumeration content that is found within this IPS project that references
     * the provided enumeration type.
     * 
     * @throws NullPointerException if the provided parameter is <code>null</code>
     */
    public IEnumContent findEnumContent(IEnumType enumType);

    /**
     * Returns the product component with the given runtime id or <code>null</code> if no such
     * product component exists. If more than one product component with the given id exists, the
     * first one found is returned.
     * 
     * @param runtimeId The runtime-id to find the product component for.
     * @throws IpsException if an error occurs during search.
     */
    public IProductCmpt findProductCmptByRuntimeId(String runtimeId) throws IpsException;

    /**
     * Returns all <code>IIpsSrcFile</code>s representing <code>TableContents</code> that are based
     * on the given <code>TableStructure</code> in this and all referenced projects. If the
     * <code>structure</code> is null, the method returns all TableContentsSrcFiles found in the
     * class path.
     * 
     * @param structure The product components type product component will be searched for.
     * @throws IpsException if an error occurs while searching
     */
    public List<IIpsSrcFile> findAllTableContentsSrcFiles(ITableStructure structure);

    /**
     * Returns the first IPS source file on the IPS object path with the the indicated qualified
     * name and type. Returns <code>null</code> if no such file was found.
     */
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType qNameType);

    /**
     * Returns the first IPS source file with the the indicated IPS object type and qualified name
     * found on the object path.<br>
     * Returns <code>null</code> if the source file wasn't found (not exists).
     */
    public IIpsSrcFile findIpsSrcFile(IpsObjectType type, String qualifiedName);

    /**
     * Returns <code>true</code> if more than one {@link IIpsSrcFile} with the indicated qualified
     * name type is found on the path. Returns <code>false</code> if no such object is found or just
     * one {@link IIpsSrcFile} was found.
     * 
     * @param qNameType representing the {@link QualifiedNameType} of the searched
     *            {@link IIpsSrcFile}
     */
    public boolean findDuplicateIpsSrcFile(QualifiedNameType qNameType);

    /**
     * Returns <code>true</code> if more than one {@link IIpsSrcFile} with the indicated qualified
     * name type is found on the path. Returns <code>false</code> if no such object is found or just
     * one {@link IIpsSrcFile} was found.
     * 
     * @param type representing the {@link IpsObjectType} of the searched {@link IIpsSrcFile}
     * @param qualifiedName representing the qualified name of the searched {@link IIpsSrcFile}
     */
    public boolean findDuplicateIpsSrcFile(IpsObjectType type, String qualifiedName);

    /**
     * Returns all IPS source files of the given type found on the class path.
     */
    public IIpsSrcFile[] findIpsSrcFiles(IpsObjectType type) throws IpsException;

    /**
     * Adds all IPS source files within this IpsProject and the IpsProjects this one depends on to
     * the given list.
     */
    public void findAllIpsSrcFiles(List<IIpsSrcFile> result) throws IpsException;

    /**
     * Returns all IPS source files within this IpsProject and the IpsProjects this one depends on
     * and match the given filter (object type list).
     * 
     * @throws IpsException if an error occurs while searching
     */
    public List<IIpsSrcFile> findAllIpsSrcFiles(IpsObjectType... filter);

    /**
     * Adds all IPS source files that are accessible through IPS source folder entries to the result
     * list.
     */
    public void collectAllIpsSrcFilesOfSrcFolderEntries(List<IIpsSrcFile> result);

    /**
     * Returns all IPS source files represents product components that are based on the given
     * product component type (either directly or because they are based on a sub type of the given
     * type) in this and all referenced projects. If productCmptType is null, the method returns all
     * source files (product components) found on the class path.
     * 
     * @param includeSubtypes If <code>true</code> is passed also product component that are based
     *            on sub types of the given product components type are returned, otherwise only
     *            product components that are directly based on the given type are returned.
     * 
     */
    public IIpsSrcFile[] findAllProductCmptSrcFiles(IProductCmptType productCmptType, boolean includeSubtypes)
            throws IpsException;

    /**
     * Returns all templates which are based on the given {@link IProductCmptType}. If the parameter
     * includeTemplatesForSubtypes is true also templates based on subtypes will be returned.
     * 
     * @param productCmptType The product component type that should that is used by the templates
     * @param includeTemplatesForSubtypes <code>true</code> to include subtypes while searching for
     *            templates
     * @return Returns a list of {@link IIpsSrcFile}, every source file holds a template that is
     *         based on the given type
     */
    public List<IIpsSrcFile> findAllProductTemplates(IProductCmptType productCmptType,
            boolean includeTemplatesForSubtypes);

    /**
     * Returns all templates that are compatible to the given product component type. A template is
     * compatible if it is based on the same product component type or on any supertype of the
     * specified one.
     * 
     * @param productCmptType The product component type to which the returned templates are
     *            compatible to
     * @return A list of all templates found from this project (including referenced projects) that
     *         are compatible to the specified type
     */
    public List<IIpsSrcFile> findCompatibleProductTemplates(IProductCmptType productCmptType);

    /**
     * Returns all <code>IIpsSrcFile</code>s representing test cases that are based on the given
     * <code>ITestCaseType</code> in this and all referenced projects. If the
     * <code>testCaseType</code> is null, the method returns all TestCaseSrcFiles found in the class
     * path.
     * 
     * @param testCaseType The <code>TestCaseType</code> to search the <code>TestCase</code>s for
     * @throws IpsException if an exception occurs while searching
     */
    public IIpsSrcFile[] findAllTestCaseSrcFiles(ITestCaseType testCaseType) throws IpsException;

    /**
     * Returns all <code>IIpsSrcFile</code>s representing <code>EnumContent</code>s that are based
     * on the given <code>IEnumType</code> in this and all referenced projects. You could specify
     * whether to include all sub types of <code>enumType</code> or not If the <code>enumType</code>
     * is null, the method returns all EnumContentSrcFiles found in the class path.
     * 
     * @param includingSubtypes <code>true</code> if sub types of <code>enumType</code> should be
     *            included in the search
     * @throws IpsException if an exception occurs while searching
     */
    public IIpsSrcFile[] findAllEnumContentSrcFiles(IEnumType enumType, boolean includingSubtypes)
            throws IpsException;

    /**
     * Returns all data types accessible on the project's path.
     * 
     * @param valuetypesOnly true if only value data types should be returned.
     * @param includeVoid true if <code>Datatype.VOID</code> should be included.
     */
    public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid);

    /**
     * Returns all data types accessible on the project's path.
     * 
     * @param valuetypesOnly true if only value data types should be returned.
     * @param includeVoid true if <code>Datatype.VOID</code> should be included.
     * @param includePrimitives true if primitive data types are included.
     */
    public Datatype[] findDatatypes(boolean valuetypesOnly, boolean includeVoid, boolean includePrimitives);

    /**
     * Returns all data types accessible on the project's path.
     * 
     * @param valuetypesOnly true if only value data types should be returned.
     * @param includeVoid true if <code>Datatype.VOID</code> should be included.
     * @param includePrimitives true if primitive data types are included.
     * @param excludedDatatypes A list of data types that should NOT be included, may be
     *            <code>null</code> if none shall be excluded.
     */
    public Datatype[] findDatatypes(boolean valuetypesOnly,
            boolean includeVoid,
            boolean includePrimitives,
            List<Datatype> excludedDatatypes);

    /**
     * Returns all data types accessible on the project's path.
     * 
     * @param valuetypesOnly true if only value data types should be returned.
     * @param includeVoid true if <code>Datatype.VOID</code> should be included.
     * @param includePrimitives true if primitive data types are included.
     * @param excludedDatatypes A list of data types that should NOT be included, may be
     *            <code>null</code> if none shall be excluded.
     * @param includeAbstract true if abstract data types should be included.
     */
    public Datatype[] findDatatypes(boolean valuetypesOnly,
            boolean includeVoid,
            boolean includePrimitives,
            List<Datatype> excludedDatatypes,
            boolean includeAbstract);

    /**
     * Returns all enumeration data types accessible on the project's IPS object path.
     */
    public EnumDatatype[] findEnumDatatypes();

    /**
     * Returns the first data type found on the path with the given qualified name. Returns
     * <code>null</code> if no data type with the given name is found.
     */
    public Datatype findDatatype(String qualifiedName);

    /**
     * Returns the first value data type found on the path with the given qualified name. Returns
     * <code>null</code> if no value data type with the given name is found. Returns
     * <code>null</code> if qualifiedName is <code>null</code>.
     */
    public ValueDatatype findValueDatatype(String qualifiedName);

    /**
     * Returns the code generation helper for the given data type or <code>null</code> if no helper
     * is available for the given data type.
     */
    public DatatypeHelper getDatatypeHelper(Datatype datatype);

    /**
     * Returns the code generation helper for the given data type or <code>null</code> if no helper
     * is available for the given data type.
     * 
     * @param qName The qualified data type name.
     */
    public DatatypeHelper findDatatypeHelper(String qName);

    /**
     * Returns the value set types that are allowed for the given data type. The type
     * {@link ValueSetType#UNRESTRICTED} is always returned and is the first element in the array.
     * If data type is <code>null</code> then an array with <code>UNRESTRICTED</code> is returned.
     * 
     * @throws IpsException if an error occurs while retrieving the value set types,
     *             possible reasons are that the data types files can't be read or the XML can't be
     *             parsed.
     */
    public List<ValueSetType> getValueSetTypes(ValueDatatype datatype) throws IpsException;

    /**
     * Returns <code>true</code> if the given value set type is available for the given value data
     * type. Returns <code>false</code> otherwise. Returns <code>false</code> if
     * <code>valueSetType</code> is <code>null</code>. If <code>datatype</code> is <code>null</code>
     * and the <code>valueSetType</code> is unrestricted, this method returns <code>true</code>.
     * 
     * If this method returns <code>true</code>, it is guaranteed that the value set type is
     * returned by {@link #getValueSetTypes(ValueDatatype)}.
     */
    public boolean isValueSetTypeApplicable(ValueDatatype datatype, ValueSetType valueSetType);

    /**
     * Returns the <code>IpsArtefactBuilderSet</code> that is currently active for this project. If
     * no IpsArtefactBuilderSet is active for this project an <code>EmptyBuilderSet</code> is
     * returned.
     */
    public IIpsArtefactBuilderSet getIpsArtefactBuilderSet();

    /**
     * Reinitializes the <code>IpsProject</code>s <code>IpsArtefactBuilderSet</code>.
     */
    public void reinitializeIpsArtefactBuilderSet();

    /**
     * Returns the runtime id prefix configured for this project.
     */
    public String getRuntimeIdPrefix();

    /**
     * Returns the strategy used to name product components. This method never returns
     * <code>null</code>.
     */
    public IProductCmptNamingStrategy getProductCmptNamingStrategy();

    /**
     * Returns the strategy used to name database tables used for persisting policy component types.
     * Returns <code>null</code> if persistence support is not enabled for this IPS project.
     */
    public ITableNamingStrategy getTableNamingStrategy();

    /**
     * Returns the strategy used to name database columns used for persisting policy component
     * types. Returns <code>null</code> if persistence support is not enabled for this IPS project.
     */
    public ITableColumnNamingStrategy getTableColumnNamingStrategy();

    /**
     * Validates the project and returns the result as list of messages.
     */
    public MessageList validate() throws IpsException;

    /**
     * Returns the naming conventions used for this project.
     */
    public IIpsProjectNamingConventions getNamingConventions();

    /**
     * Checks all given product components against all product components in the IPS object path for
     * duplicate runtime IDs.
     * 
     * @param cmptsToCheck List of product components to check
     * 
     * @return A list of messages. For each combination of two product components with duplicate
     *         runtime id a new message is created. This message has only one invalid object
     *         property, containing the product component given to this method.
     * 
     * @throws IpsException if an error occurs during search.
     */
    public MessageList checkForDuplicateRuntimeIds(IIpsSrcFile... cmptsToCheck) throws IpsException;

    /**
     * Returns <code>true</code> if the given resource will be excluded from the product
     * definition.<br>
     * If the given resource is relevant for the product definition the method returns
     * <code>false</code>. Returns <code>false</code> if resource is <code>null</code>.
     */
    public boolean isResourceExcludedFromProductDefinition(AResource resource);

    /**
     * This method checks whether this project has a resource with the specified path. The path is
     * relative to the project's object path entries.
     * 
     * @param path The path of the requested resource
     * @return <code>true</code> if the resource could be found in this project's entries,
     *         <code>false</code> if not
     */
    public boolean containsResource(String path);

    /**
     * Retrieves the contents of a file in the {@link IIpsObjectPath}. Returns <code>null</code> if
     * no resource is found at the given path.
     * <p>
     * Callers of this method are responsible for closing the stream after use.
     */
    public InputStream getResourceAsStream(String path);

    /**
     * Returns the {@link IVersionProvider} that is configured for this project. This may be an
     * {@link IVersionProvider} extended via extension point or if none is configured it is the
     * {@code DefaultVersionProvider} reading the version directly from
     * {@link IIpsProjectProperties#getVersion()}. In case of extended {@link IVersionProvider} the
     * one configured in {@link IIpsProjectProperties#getVersionProviderId()} is used.
     * 
     * @return The version provider that is configured to use by this project. If none is configured
     *         the {@code DefaultVersionProvider} is returned.
     */
    public IVersionProvider<?> getVersionProvider();

    /**
     * Deletes all contained {@link IIpsPackageFragmentRoot IIpsPackageFragmentRoots} and the
     * corresponding project folder.
     */
    @Override
    public void delete() throws IpsException;

    /**
     * Returns a Set of {@link IIpsSrcFile} representing all {@link EnumType EnumTypes} that are
     * used to define markers for {@link IValidationRule IValidationRules}.
     */
    public LinkedHashSet<IIpsSrcFile> getMarkerEnums();

    /**
     * This method is called when the project internal caches should be cleaned up. For example when
     * the IPS object path has changed.
     * 
     */
    public void clearCaches();

    /**
     * Returns the hierarchy of the given template as a tree of source files. The root of the tree
     * is the source file of the given template, all source files of product components and
     * templates that (directly or indirectly) reference the given template are nodes in that tree.
     * Returns an empty tree if no template is given or the given product component is not a
     * template.
     * 
     * @param template the template whose hierarchy is returned
     * @return a tree with the hierarchy of the given template. The tree is empty if no template or
     *         a non-template product component is given.
     */
    public Tree<IIpsSrcFile> findTemplateHierarchy(IProductCmpt template);

}
