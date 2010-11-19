/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.abstracttest;

import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.launching.JavaRuntime;
import org.faktorips.abstracttest.builder.TestArtefactBuilderSetInfo;
import org.faktorips.abstracttest.test.XmlAbstractTestCase;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.Util;
import org.faktorips.devtools.core.internal.model.DynamicEnumDatatype;
import org.faktorips.devtools.core.internal.model.DynamicValueDatatype;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.enums.EnumContent;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.internal.model.testcase.TestCase;
import org.faktorips.devtools.core.internal.model.testcasetype.TestCaseType;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.core.util.BeanUtil;
import org.faktorips.util.StringUtil;

/**
 * Base class for all plugin test cases. Has a factory method to create an ips project including the
 * underlying platform project.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractIpsPluginTest extends XmlAbstractTestCase {

    protected static final String OUTPUT_FOLDER_NAME_DERIVED = "extension"; //$NON-NLS-1$
    protected static final String OUTPUT_FOLDER_NAME_MERGABLE = "src"; //$NON-NLS-1$
    protected static final String BASE_PACKAGE_NAME_DERIVED = "org.faktorips.sample.model"; //$NON-NLS-1$
    protected static final String BASE_PACKAGE_NAME_MERGABLE = "org.faktorips.sample.model"; //$NON-NLS-1$

    public AbstractIpsPluginTest() {
        super();
    }

    public AbstractIpsPluginTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        IpsPlugin.getDefault().setSuppressLoggingDuringTest(false);
        ((IpsModel)IpsPlugin.getDefault().getIpsModel()).stopListeningToResourceChanges();
        IpsPlugin.getDefault().setFeatureVersionManagers(
                new IIpsFeatureVersionManager[] { new TestIpsFeatureVersionManager() });
        setAutoBuild(false);
        IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(new GregorianCalendar());

        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                    System.out.println("AbstractIpsPlugin.setUp(): Start deleting projects.");
                }
                waitForIndexer();
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
                for (IProject project : projects) {
                    project.close(null);
                    project.delete(true, true, null);
                }
                if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                    System.out.println("AbstractIpsPlugin.setUp(): Projects deleted.");
                }
                IpsPlugin.getDefault().reinitModel(); // also starts the listening process
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

    }

    @Override
    protected final void tearDown() throws Exception {
        IpsPlugin.getDefault().setSuppressLoggingDuringTest(false);
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject project : projects) {
            project.close(null);
            project.delete(true, true, null);
        }
        tearDownExtension();
    }

    protected void tearDownExtension() throws Exception {

    }

    protected void suppressLoggingDuringExecutionOfThisTestCase() {
        IpsPlugin.getDefault().setSuppressLoggingDuringTest(true);
    }

    protected void createArchive(IIpsProject projectToArchive, IFile archiveFile) throws Exception {
        File file = createFileIfNecessary(archiveFile);

        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(projectToArchive, file);
        ResourcesPlugin.getWorkspace().run(op, null);

        createLinkIfNecessary(archiveFile, file);
    }

    protected void createArchive(IIpsPackageFragmentRoot rootToArchive, IFile archiveFile) throws Exception {
        File file = createFileIfNecessary(archiveFile);

        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(rootToArchive, file);
        ResourcesPlugin.getWorkspace().run(op, null);

        createLinkIfNecessary(archiveFile, file);
    }

    /**
     * Creates a links to the given file in the workspace.
     */
    protected void createLinkIfNecessary(IFile archiveFile, File file) throws CoreException {
        if (!archiveFile.isLinked() && !archiveFile.exists()) {
            archiveFile.createLink(new Path(file.getAbsolutePath()), 0, new NullProgressMonitor());
        }
    }

    protected File createFileIfNecessary(IFile archiveFile) {
        File file = null;
        if (archiveFile.exists()) {
            file = archiveFile.getLocation().toFile();
        } else {
            file = archiveFile.getLocation().toFile();
        }
        return file;
    }

    /**
     * Creates a new IpsProject.
     */
    protected IIpsProject newIpsProject() throws CoreException {
        return newIpsProject("TestProject");
    }

    /**
     * Creates a new IpsProject.
     */
    protected IIpsProject newIpsProject(final String name) throws CoreException {
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                IProject project = newPlatformProject(name);
                addJavaCapabilities(project);
                addIpsCapabilities(project);
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

        return IpsPlugin.getDefault().getIpsModel().getIpsProject(name);
    }

    /**
     * Creates a new platform project with the given name and opens it.
     */
    protected IProject newPlatformProject(final String name) throws CoreException {
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                internalNewPlatformProject(name);
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
        return workspace.getRoot().getProject(name);
    }

    /**
     * Creates a new platform project with the given name and opens it.
     */
    private IProject internalNewPlatformProject(final String name) throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(name);
        project.create(null);
        project.open(null);
        return project;
    }

    /**
     * Creates a new Java Project for the given platform project.
     */
    protected IJavaProject addJavaCapabilities(IProject project) throws CoreException {
        IJavaProject javaProject = JavaCore.create(project);
        // add Java nature
        Util.addNature(project, JavaCore.NATURE_ID);
        javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
        // create bin folder and set as output folder.
        IFolder binFolder = project.getFolder("bin");
        if (!binFolder.exists()) {
            binFolder.create(true, true, null);
        }
        IFolder srcFolder = project.getFolder(OUTPUT_FOLDER_NAME_MERGABLE);
        javaProject.setOutputLocation(binFolder.getFullPath(), null);
        if (!srcFolder.exists()) {
            srcFolder.create(true, true, null);
        }
        IFolder extFolder = project.getFolder(OUTPUT_FOLDER_NAME_DERIVED);
        if (!extFolder.exists()) {
            extFolder.create(true, true, null);
        }
        IPackageFragmentRoot srcRoot = javaProject.getPackageFragmentRoot(srcFolder);
        IPackageFragmentRoot extRoot = javaProject.getPackageFragmentRoot(extFolder);
        IClasspathEntry[] entries = new IClasspathEntry[2];
        entries[0] = JavaCore.newSourceEntry(srcRoot.getPath());
        entries[1] = JavaCore.newSourceEntry(extRoot.getPath());
        // set classpath
        javaProject.setRawClasspath(entries, null);
        addSystemLibraries(javaProject);
        return javaProject;
    }

    protected void addClasspathEntry(IJavaProject project, IClasspathEntry entry) throws JavaModelException {
        IClasspathEntry[] entries = project.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[entries.length] = entry;
        project.setRawClasspath(newEntries, null);
    }

    protected void addIpsCapabilities(IProject project) throws CoreException {
        Util.addNature(project, IIpsProject.NATURE_ID);
        IFolder rootFolder = project.getFolder("productdef");
        rootFolder.create(true, true, null);
        IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project.getName());
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.setOutputDefinedPerSrcFolder(true);
        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(rootFolder);
        entry.setSpecificBasePackageNameForMergableJavaClasses(BASE_PACKAGE_NAME_MERGABLE);
        entry.setSpecificOutputFolderForMergableJavaFiles(project.getFolder(OUTPUT_FOLDER_NAME_MERGABLE));
        entry.setSpecificBasePackageNameForDerivedJavaClasses(BASE_PACKAGE_NAME_DERIVED);
        entry.setSpecificOutputFolderForDerivedJavaFiles(project.getFolder(OUTPUT_FOLDER_NAME_DERIVED));

        ipsProject.setIpsObjectPath(path);

        // TODO: wichtig dies erzeugt eine Abhaengigkeit vom StdBuilder Projekt.
        // Dies muss ueberarbeitet werden
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setBuilderSetId("org.faktorips.devtools.stdbuilder.ipsstdbuilderset");
        props.setPredefinedDatatypesUsed(new String[] { "Decimal", "Money", "Integer", "int", "boolean", "String",
                "Boolean" });

        props.setMinRequiredVersionNumber(
                "org.faktorips.feature", (String)Platform.getBundle("org.faktorips.devtools.core").getHeaders().get("Bundle-Version")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ipsProject.setProperties(props);
    }

    private void addSystemLibraries(IJavaProject javaProject) throws JavaModelException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        newEntries[oldEntries.length] = JavaRuntime.getDefaultJREContainerEntry();
        javaProject.setRawClasspath(newEntries, null);
    }

    private void waitForIndexer() throws JavaModelException {
        SearchEngine engine = new SearchEngine();
        engine.searchAllTypeNames(new char[] {}, SearchPattern.R_EXACT_MATCH, new char[] {},
                SearchPattern.R_EXACT_MATCH, IJavaSearchConstants.CLASS,
                SearchEngine.createJavaSearchScope(new IJavaElement[0]), new TypeNameRequestor() {
                }, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
    }

    protected void setAutoBuild(boolean autoBuild) throws CoreException {
        IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
        if (autoBuild != ResourcesPlugin.getWorkspace().isAutoBuilding()) {
            description.setAutoBuilding(autoBuild);
            ResourcesPlugin.getWorkspace().setDescription(description);
        }
    }

    /**
     * Creates a new ips package fragment root with the given name. It will be created into the
     * indicated parent folder in the given ips project. If the parent folder is null the project
     * will be the parent of the new package fragment root.
     * 
     * @param ipsProject The ips project in which to create a new package fragment root
     * @param parentFolder The folder in which to create the new package fragment root
     * @param name The name of the new package fragment root
     * @return A handle to the new package fragment root
     */
    protected IIpsPackageFragmentRoot newIpsPackageFragmentRoot(IIpsProject ipsProject,
            IFolder parentFolder,
            String name) throws CoreException {

        IFolder newRootFolder;
        if (parentFolder == null) {
            newRootFolder = ipsProject.getProject().getFolder(name);
        } else {
            newRootFolder = parentFolder.getFolder(name);
        }
        newRootFolder.create(false, true, null);

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newSourceFolderEntry(newRootFolder);
        ipsProject.setIpsObjectPath(path);

        return ipsProject.findIpsPackageFragmentRoot(name);
    }

    /**
     * Creates a new ipsobject in the indicated project's first source folder. If the qualifiedName
     * includes a package name, the package is created if it does not already exists.
     */
    protected IIpsObject newIpsObject(IIpsProject ipsProject, IpsObjectType type, String qualifiedName)
            throws CoreException {
        IIpsPackageFragmentRoot root = ipsProject.getSourceIpsPackageFragmentRoots()[0];
        return newIpsObject(root, type, qualifiedName);
    }

    /**
     * Creates a new ipsobject in the indicated package fragment root. If the qualifiedName includes
     * a package name, the package is created if it does not already exists.
     */
    protected IIpsObject newIpsObject(final IIpsPackageFragmentRoot root,
            final IpsObjectType type,
            final String qualifiedName) throws CoreException {

        return newIpsObject(root, type, qualifiedName, true);
    }

    /**
     * Creates a new ipsobject in the indicated package fragment root. If the qualifiedName includes
     * a package name, the package is created if it does not already exists.
     */
    private IIpsObject newIpsObject(final IIpsPackageFragmentRoot root,
            final IpsObjectType type,
            final String qualifiedName,
            final boolean createAutoProductCmptType) throws CoreException {

        final String packName = StringUtil.getPackageName(qualifiedName);
        final String unqualifiedName = StringUtil.unqualifiedName(qualifiedName);
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                IIpsPackageFragment pack = root.getIpsPackageFragment(packName);
                if (!pack.exists()) {
                    pack = root.createPackageFragment(packName, true, null);
                }
                IIpsSrcFile file = pack.createIpsFile(type, unqualifiedName, true, null);
                IIpsObject ipsObject = file.getIpsObject();
                if (createAutoProductCmptType && ipsObject instanceof IPolicyCmptType) {
                    ((IPolicyCmptType)ipsObject).setConfigurableByProductCmptType(true);
                    ((IPolicyCmptType)ipsObject).setProductCmptType(qualifiedName + "ProductCmpt");
                    newProductCmptType(root, qualifiedName + "ProductCmpt");
                }
            }
        };
        ResourcesPlugin.getWorkspace().run(runnable, null);
        IIpsPackageFragment pack = root.getIpsPackageFragment(packName);
        return pack.getIpsSrcFile(type.getFileName(unqualifiedName)).getIpsObject();
    }

    /**
     * Creates a new enum content in the indicated package fragment root. If the qualified name
     * includes a package name, the package is created if it does not already exists.
     * 
     * @param root The package fragment root in which to create the new enum content.
     * @param qualifiedName The qualified name of the new enum content.
     * 
     * @return The newly created enum content.
     * 
     * @throws CoreException If the enum content could not be created.
     */
    protected EnumContent newEnumContent(final IIpsPackageFragmentRoot root, final String qualifiedName)
            throws CoreException {

        return (EnumContent)newIpsObject(root, IpsObjectType.ENUM_CONTENT, qualifiedName);
    }

    /**
     * Creates a new enum content in the project's first package fragment root. If the qualified
     * name includes a package name, the package is created if it does not already exist.
     * 
     * @param ipsProject The ips project in which to create the new enum content.
     * @param qualifiedName The qualified name of the new enum content.
     * 
     * @return The newly created enum content.
     * 
     * @throws CoreException If the enum content could not be created.
     */
    protected EnumContent newEnumContent(final IIpsProject ipsProject, final String qualifiedName) throws CoreException {
        return (EnumContent)newIpsObject(ipsProject, IpsObjectType.ENUM_CONTENT, qualifiedName);
    }

    /**
     * Creates a new enum content that is based on the given enum type. The product component is
     * stored in the same package fragment root as the type. If the qualifiedName includes a package
     * name, the package is created if it does not already exists.
     */
    protected EnumContent newEnumContent(IEnumType type, String qualifiedName) throws CoreException {
        EnumContent enumContent = (EnumContent)newIpsObject(type.getIpsPackageFragment().getRoot(),
                IpsObjectType.ENUM_CONTENT, qualifiedName);
        enumContent.setEnumType(type.getQualifiedName());
        enumContent.getIpsSrcFile().save(true, null);
        return enumContent;
    }

    /**
     * Creates a new enum type in the indicated package fragment root. If the qualified name
     * includes a package name, the package is created if it does not already exists.
     * 
     * @param root The package fragment root in which to create the new enum type.
     * @param qualifiedName The qualified name of the new enum type.
     * 
     * @return The newly created enum type.
     * 
     * @throws CoreException If the enum type could not be created.
     */
    protected EnumType newEnumType(final IIpsPackageFragmentRoot root, final String qualifiedName) throws CoreException {
        return (EnumType)newIpsObject(root, IpsObjectType.ENUM_TYPE, qualifiedName);
    }

    /**
     * Creates a new enum type in the project's first package fragment root. If the qualified name
     * includes a package name, the package is created if it does not already exist.
     * 
     * @param ipsProject The ips project in which to create the new enum type.
     * @param qualifiedName The qualified name of the new enum type.
     * 
     * @return The newly created enum type.
     * 
     * @throws CoreException If the enum type could not be created.
     */
    protected EnumType newEnumType(final IIpsProject ipsProject, final String qualifiedName) throws CoreException {
        return (EnumType)newIpsObject(ipsProject, IpsObjectType.ENUM_TYPE, qualifiedName);
    }

    /**
     * Creates a new default enum type in the project's first package fragment root. The enum type
     * has two attributes named "id" and "name" but does not contain any values, so it is defined as
     * containing values.
     * 
     * If the qualified name includes a package name, the package is created if it does not already
     * exist.
     * 
     * @param ipsProject The ips project in which to create the new enum type.
     * @param qualifiedName The qualified name of the new enum type.
     * 
     * @return The newly created enum type.
     * 
     * @throws CoreException If the enum type could not be created.
     */
    protected EnumType newDefaultEnumType(final IIpsProject ipsProject, final String qualifiedName)
            throws CoreException {

        EnumType enumType = newEnumType(ipsProject, qualifiedName);
        enumType.setContainingValues(true);
        enumType.newEnumLiteralNameAttribute();
        IEnumAttribute idAttr = enumType.newEnumAttribute();
        idAttr.setName("id");
        idAttr.setDatatype(Datatype.STRING.getQualifiedName());
        idAttr.setUnique(true);
        idAttr.setIdentifier(true);
        IEnumAttribute nameAttr = enumType.newEnumAttribute();
        nameAttr.setDatatype(Datatype.STRING.getQualifiedName());
        nameAttr.setName("name");
        nameAttr.setUsedAsNameInFaktorIpsUi(true);
        nameAttr.setUnique(true);

        return enumType;
    }

    /**
     * Creates a new policy component type in the indicated package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected PolicyCmptType newPolicyCmptType(final IIpsPackageFragmentRoot root, final String qualifiedName)
            throws CoreException {
        return (PolicyCmptType)newIpsObject(root, IpsObjectType.POLICY_CMPT_TYPE, qualifiedName);
    }

    /**
     * Creates a new policy component type in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected PolicyCmptType newPolicyCmptType(IIpsProject ipsProject, String qualifiedName) throws CoreException {
        return (PolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, qualifiedName);
    }

    /**
     * Creates a new policy component type in the project's first package fragment root. Does not
     * create a product component type. If the qualifiedName includes a package name, the package is
     * created if it does not already exists.
     */
    protected PolicyCmptType newPolicyCmptTypeWithoutProductCmptType(IIpsProject ipsProject, String qualifiedName)
            throws CoreException {
        return (PolicyCmptType)newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.POLICY_CMPT_TYPE,
                qualifiedName, false);
    }

    /**
     * Creates a new product component type in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected ProductCmptType newProductCmptType(IProductCmptType supertype, String qualifiedName) throws CoreException {
        ProductCmptType newType = newProductCmptType(supertype.getIpsProject(), qualifiedName);
        newType.setSupertype(supertype.getQualifiedName());
        newType.setConfigurationForPolicyCmptType(supertype.isConfigurationForPolicyCmptType());
        return newType;
    }

    /**
     * Creates a new product component type in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected ProductCmptType newProductCmptType(IIpsProject ipsProject, String qualifiedName) throws CoreException {
        return (ProductCmptType)newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT_TYPE, qualifiedName);
    }

    /**
     * Creates a new product component type in the indicated package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected ProductCmptType newProductCmptType(final IIpsPackageFragmentRoot root, final String qualifiedName)
            throws CoreException {
        return (ProductCmptType)newIpsObject(root, IpsObjectType.PRODUCT_CMPT_TYPE, qualifiedName);
    }

    /**
     * Creates a new product component type in the indicated package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected PolicyCmptType newPolicyAndProductCmptType(IIpsProject ipsProject,
            String policyCmptTypeName,
            String productCmptTypeName) throws CoreException {

        IPolicyCmptType policyCmptType = (IPolicyCmptType)newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0],
                IpsObjectType.POLICY_CMPT_TYPE, policyCmptTypeName, false);
        ProductCmptType productCmptType = newProductCmptType(ipsProject, productCmptTypeName);
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(policyCmptTypeName);
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType(productCmptTypeName);
        policyCmptType.getIpsSrcFile().save(true, null);
        productCmptType.getIpsSrcFile().save(true, null);
        return (PolicyCmptType)policyCmptType;
    }

    /**
     * Creates a new product component that is based on the given product component type and has one
     * generation with it's valid from date set to the current working date. The product component
     * is stored in the same package fragment root as the type. If the qualifiedName includes a
     * package name, the package is created if it does not already exists.
     */
    protected ProductCmpt newProductCmpt(IProductCmptType type, String qualifiedName) throws CoreException {
        IProductCmpt productCmpt = (IProductCmpt)newIpsObject(type.getIpsPackageFragment().getRoot(),
                IpsObjectType.PRODUCT_CMPT, qualifiedName);
        productCmpt.setProductCmptType(type.getQualifiedName());
        productCmpt.newGeneration(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
        productCmpt.getIpsSrcFile().save(true, null);
        return (ProductCmpt)productCmpt;
    }

    /**
     * Creates a new product component in the indicated package fragment root. If the qualifiedName
     * includes a package name, the package is created if it does not already exists.
     */
    protected ProductCmpt newProductCmpt(IIpsPackageFragmentRoot root, String qualifiedName) throws CoreException {
        return (ProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, qualifiedName);
    }

    /**
     * Creates a new product component in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected ProductCmpt newProductCmpt(IIpsProject project, String qualifiedName) throws CoreException {
        return (ProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, qualifiedName);
    }

    /**
     * Creates a new ipsobject in the indicated package fragment root. If the qualifiedName includes
     * a package name, the package is created if it does not already exists.
     */
    protected IIpsObject newIpsObject(IIpsPackageFragment pack, IpsObjectType type, String unqualifiedName)
            throws CoreException {
        IIpsSrcFile file = pack.createIpsFile(type, unqualifiedName, true, null);
        return file.getIpsObject();
    }

    /**
     * Creates a new table structure in the indicated package fragment root. If the qualifiedName
     * includes a package name, the package is created if it does not already exists.
     */
    protected TableStructure newTableStructure(final IIpsPackageFragmentRoot root, final String qualifiedName)
            throws CoreException {
        return (TableStructure)newIpsObject(root, IpsObjectType.TABLE_STRUCTURE, qualifiedName);
    }

    /**
     * Creates a new table structure in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected TableStructure newTableStructure(IIpsProject ipsProject, String qualifiedName) throws CoreException {
        return (TableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE, qualifiedName);
    }

    /**
     * Creates a new table content that is based on the given table structure and has one generation
     * with it's valid from date set to the current working date. The table content is stored in the
     * same package fragment root as the structure. If the qualifiedName includes a package name,
     * the package is created if it does not already exists.
     */
    protected TableContents newTableContents(ITableStructure ts0, String qualifiedName) throws CoreException {
        TableContents tableContents = (TableContents)newIpsObject(ts0.getIpsPackageFragment().getRoot(),
                IpsObjectType.TABLE_CONTENTS, qualifiedName);
        tableContents.setTableStructure(ts0.getQualifiedName());
        tableContents.newGeneration(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
        tableContents.getIpsSrcFile().save(true, null);
        return tableContents;
    }

    /**
     * Creates a new table content in the indicated package fragment root. If the qualifiedName
     * includes a package name, the package is created if it does not already exists.
     */
    protected TableContents newTableContents(IIpsPackageFragmentRoot root, String qualifiedName) throws CoreException {
        return (TableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS, qualifiedName);
    }

    /**
     * Creates a new table content in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected TableContents newTableContents(IIpsProject project, String qualifiedName) throws CoreException {
        return (TableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, qualifiedName);
    }

    /**
     * Creates a new test case type in the indicated package fragment root. If the qualifiedName
     * includes a package name, the package is created if it does not already exists.
     */
    protected TestCaseType newTestCaseType(final IIpsPackageFragmentRoot root, final String qualifiedName)
            throws CoreException {
        return (TestCaseType)newIpsObject(root, IpsObjectType.TEST_CASE_TYPE, qualifiedName);
    }

    /**
     * Creates a new test case type in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected TestCaseType newTestCaseType(IIpsProject ipsProject, String qualifiedName) throws CoreException {
        return (TestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, qualifiedName);
    }

    /**
     * Creates a new test case that is based on the given test case type. The test case is stored in
     * the same package fragment root as the structure. If the qualifiedName includes a package
     * name, the package is created if it does not already exists.
     */
    protected TestCase newTestCase(TestCaseType tCase, String qualifiedName) throws CoreException {
        TestCase testCase = (TestCase)newIpsObject(tCase.getIpsPackageFragment().getRoot(), IpsObjectType.TEST_CASE,
                qualifiedName);
        testCase.setTestCaseType(tCase.getQualifiedName());
        testCase.getIpsSrcFile().save(true, null);
        return testCase;
    }

    /**
     * Creates a new test case in the indicated package fragment root. If the qualifiedName includes
     * a package name, the package is created if it does not already exists.
     */
    protected TestCase newTestCase(IIpsPackageFragmentRoot root, String qualifiedName) throws CoreException {
        return (TestCase)newIpsObject(root, IpsObjectType.TEST_CASE, qualifiedName);
    }

    /**
     * Creates a new test case in the project's first package fragment root. If the qualifiedName
     * includes a package name, the package is created if it does not already exists.
     */
    protected TestCase newTestCase(IIpsProject project, String qualifiedName) throws CoreException {
        return (TestCase)newIpsObject(project, IpsObjectType.TEST_CASE, qualifiedName);
    }

    /**
     * Triggers a full build of the workspace.
     */
    protected void fullBuild() throws CoreException {
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

    /**
     * Triggers an incremental build of the workspace.
     */
    protected void incrementalBuild() throws CoreException {
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
    }

    /**
     * Expects an array of classes that comply to the enum type pattern. The enum types are
     * registered to the provided IpsProject as definded datatypes. The qualifiedName of a
     * registered datatype is the unqualified class name of the enum type class. The enum type class
     * must have the folloing methods: </p>
     * <ol>
     * <li>public final static &lt;EnumValue &gt; getAllValues()</li>
     * <li>public String getId()</li>
     * <li>public String getName()</li>
     * <li>public boolean isValueOf(String)</li>
     * <li>public String toString(), must return the id of the enum value</li>
     * <li>public final static &lt;EnumValue &gt; valueOf(String), the id is provided to this method
     * and an enum values is supposed to be returned by this method</li>
     * </ol>
     */
    protected DynamicEnumDatatype[] newDefinedEnumDatatype(IIpsProject project, Class<?>[] adaptedClass)
            throws CoreException, IOException {

        ArrayList<DynamicValueDatatype> dataTypes = new ArrayList<DynamicValueDatatype>(adaptedClass.length);
        IIpsProjectProperties properties = project.getProperties();
        DynamicValueDatatype[] definedDatatypes = properties.getDefinedValueDatatypes();
        for (DynamicValueDatatype definedDatatype : definedDatatypes) {
            dataTypes.add(definedDatatype);
        }

        for (Class<?> adaptedClas : adaptedClass) {
            DynamicEnumDatatype dataType = new DynamicEnumDatatype(project);
            dataType.setAdaptedClass(adaptedClas);
            dataType.setAllValuesMethodName("getAllValues");
            dataType.setGetNameMethodName("getName");
            dataType.setIsParsableMethodName("isValueOf");
            dataType.setIsSupportingNames(true);
            dataType.setQualifiedName(StringUtil.unqualifiedName(adaptedClas.getName()));
            dataType.setToStringMethodName("toString");
            dataType.setValueOfMethodName("valueOf");
            dataTypes.add(dataType);
            createEnumClassFileInProjectOutputLocation(project, adaptedClas);
        }

        DynamicEnumDatatype[] returnValue = dataTypes.toArray(new DynamicEnumDatatype[adaptedClass.length]);
        properties.setDefinedDatatypes(returnValue);
        properties.setJavaProjectContainsClassesForDynamicDatatypes(true);
        project.setProperties(properties);
        return returnValue;
    }

    private void createEnumClassFileInProjectOutputLocation(IIpsProject project, Class<?> adaptedClass)
            throws IOException, CoreException {

        IPath outputLocation = project.getJavaProject().getResource().getLocation()
                .append(project.getJavaProject().getOutputLocation().removeFirstSegments(1));
        IPath packagePath = outputLocation.append(adaptedClass.getPackage().getName().replace('.', '/'));
        File classFileDir = packagePath.toFile();
        if (!classFileDir.exists()) {
            classFileDir.mkdirs();
            IPath classFilePath = outputLocation.append(adaptedClass.getName().replace('.', '/') + ".class");
            File classFile = classFilePath.toFile();
            if (!classFile.exists()) {
                classFile.createNewFile();
            }
            FileOutputStream fos = null;
            InputStream is = null;
            try {
                is = adaptedClass.getClassLoader().getResourceAsStream(
                        adaptedClass.getName().replace('.', '/') + ".class");
                fos = new FileOutputStream(classFile);
                int value = is.read();
                while (value != -1) {
                    fos.write(value);
                    value = is.read();
                }
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Copies the given project properties file and the given classes to the given project. The
     * classes are added to the classpath of the project.
     * 
     */
    protected void configureProject(IIpsProject project, String ipsProjectFileName, Class<?>[] dependencies)
            throws CoreException {
        IPath outputPath = project.getJavaProject().getOutputLocation();
        IFolder output = project.getProject().getFolder(outputPath);
        for (Class<?> dependencie : dependencies) {
            String name = dependencie.getName() + ".class";
            output.getFile(name).create(dependencie.getResourceAsStream(name), true, null);
        }
        IFile ipsproject = project.getProject().getFile(".ipsproject");
        if (ipsproject.exists()) {
            ipsproject.setContents(getClass().getResourceAsStream(ipsProjectFileName), true, false, null);
        } else {
            ipsproject.create(getClass().getResourceAsStream(ipsProjectFileName), true, null);
        }
    }

    /**
     * Sets the builderset as the one to be used by the indicated project. This method modifies the
     * project's properties and also registers the builderset in the model.
     */
    protected void setArtefactBuildset(IIpsProject project, IIpsArtefactBuilderSet builderset) throws CoreException {
        IIpsProjectProperties props = project.getProperties();
        props.setBuilderSetId(builderset.getId());
        project.setProperties(props);
        IpsModel model = ((IpsModel)project.getIpsModel());
        model.setIpsArtefactBuilderSetInfos(new IIpsArtefactBuilderSetInfo[] { new TestArtefactBuilderSetInfo(
                builderset) });
        model.clearValidationCache();
    }

    /**
     * Subclasses can use this method to print out the MultiStatus of a CoreException. This is
     * expecially interesting for testcases that start a build because normally all you get is a
     * ResourceException with no detailed information.
     * 
     * @param status the status of a CoreException
     */
    protected void printOriginalStatus(IStatus status) {
        System.out.println(status.getMessage());
        if (status.getChildren().length == 0) {
            return;
        }
        IStatus[] statuus = status.getChildren();
        for (IStatus statuu : statuus) {
            printOriginalStatus(statuu);
        }
    }

    protected IpsModel getIpsModel() {
        return (IpsModel)IpsPlugin.getDefault().getIpsModel();
    }

    protected void testPropertyAccessReadWrite(Class<?> clazz, String propertyName) {
        testPropertyAccessReadOnly(clazz, propertyName);
        testPropertyAccessWriteOnly(clazz, propertyName);
    }

    protected void testPropertyAccessReadWrite(Class<?> clazz, String propertyName, Object object, Object testValueToSet) {
        testPropertyAccessReadWrite(clazz, propertyName);
        PropertyDescriptor prop = BeanUtil.getPropertyDescriptor(clazz, propertyName);
        ModelChangeListener listener = new ModelChangeListener();
        boolean writeOk = false;
        try {
            getIpsModel().addChangeListener(listener);
            prop.getWriteMethod().invoke(object, new Object[] { testValueToSet });
            writeOk = true;
            Object retValue = prop.getReadMethod().invoke(object, new Object[0]);
            assertEquals("Getter method for property " + propertyName + " of class " + clazz.getName()
                    + " does not return the expected value", testValueToSet, retValue);
            assertNotNull("Setter method for property " + propertyName + " of class " + clazz.getName()
                    + " hasn't triggered a change event", listener.lastEvent);
        } catch (Exception e) {
            if (writeOk) {
                fail("An exception occured while reading property " + propertyName + " of class " + clazz.getName());
            } else {
                fail("An exception occured while setting property " + propertyName + " of class " + clazz.getName());
            }
        } finally {
            getIpsModel().removeChangeListener(listener);
        }
    }

    protected void testPropertyAccessWriteOnly(Class<?> clazz, String propertyName) {
        PropertyDescriptor prop = BeanUtil.getPropertyDescriptor(clazz, propertyName);
        Method writeMethod = prop.getWriteMethod();
        assertNotNull("Class " + clazz.getName() + " hasn't got a write method for property " + propertyName,
                writeMethod);
        assertEquals("Class " + clazz.getName() + ": Write method for property " + propertyName
                + " must have exactly 1 argument", 1, writeMethod.getParameterTypes().length);
    }

    protected void testPropertyAccessReadOnly(Class<?> clazz, String propertyName) {
        PropertyDescriptor prop = BeanUtil.getPropertyDescriptor(clazz, propertyName);
        Method readMethod = prop.getReadMethod();
        assertNotNull("Class " + clazz.getName() + " hasn't got a read method for property " + propertyName, readMethod);
        assertEquals(0, readMethod.getParameterTypes().length);
    }

    /**
     * Create the sort order file for {@link IpsPackageFragment}s from a List of Strings.
     * 
     * @param folder Handle to the parent folder (IpsPackageFragment or IpsPackageFragmentRoot)
     * @param strings IpsPackageFragment names ikn sort order.
     */
    protected void createPackageOrderFile(IFolder folder, List<?> strings) throws IOException, CoreException {

        IFile file = folder.getFile(IIpsPackageFragment.SORT_ORDER_FILE_NAME);

        if (file.exists()) {
            file.delete(true, null);
        }

        String print = "";
        String lineSeparator = StringUtil.getSystemLineSeparator();

        print = print.concat("# comment" + lineSeparator);

        for (Object name : strings) {
            String element = (String)name;

            print = print.concat(element + lineSeparator);
        }

        byte[] bytes = print.getBytes(StringUtil.CHARSET_UTF8);

        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        file.create(is, true, null);
    }

    class ModelChangeListener implements ContentsChangeListener {

        ContentChangeEvent lastEvent;

        @Override
        public void contentsChanged(ContentChangeEvent event) {
            lastEvent = event;
        }

    }

    /**
     * Reads the first line of a file's contents from the given {@link InputStream}.
     */
    protected String getFileContent(InputStream aStream) throws IOException {
        assertNotNull(aStream);
        BufferedReader aReader = new BufferedReader(new InputStreamReader(aStream));
        String aContent = aReader.readLine();
        aReader.close();

        return aContent;
    }

    /**
     * Creates a file ("file.txt") with the given String as content and places it in the given
     * folder.
     */
    protected void createFileWithContent(IFolder parentFolder, String fileName, String content) throws CoreException {
        IFile file = parentFolder.getFile(fileName);
        if (!file.exists()) {
            file.create(new ByteArrayInputStream(content.getBytes()), true, null);
        }
    }

}
