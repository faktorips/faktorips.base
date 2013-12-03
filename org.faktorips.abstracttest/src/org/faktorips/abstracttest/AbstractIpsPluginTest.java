/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
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
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.abstracttest.builder.TestArtefactBuilderSetInfo;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.abstracttest.test.XmlAbstractTestCase;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.Util;
import org.faktorips.devtools.core.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.core.internal.model.DynamicEnumDatatype;
import org.faktorips.devtools.core.internal.model.DynamicValueDatatype;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.enums.EnumContent;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
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
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.util.BeanUtil;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.junit.After;
import org.junit.Before;
import org.w3c.dom.Document;

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

    protected static final String DEFAULT_CATEGORY_NAME_VALIDATION_RULES = "validationRules";
    protected static final String DEFAULT_CATEGORY_NAME_TABLE_STRUCTURE_USAGES = "tables";
    protected static final String DEFAULT_CATEGORY_NAME_PRODUCT_CMPT_TYPE_ATTRIBUTES = "productAttributes";
    protected static final String DEFAULT_CATEGORY_NAME_POLICY_CMPT_TYPE_ATTRIBUTES = "policyAttributes";
    protected static final String DEFAULT_CATEGORY_NAME_FORMULA_SIGNATURE_DEFINITIONS = "formulas";

    private final TestChangeListener contentsChangeListener;

    public AbstractIpsPluginTest() {
        super();
        contentsChangeListener = new TestChangeListener();
    }

    @Before
    public void setUp() throws Exception {
        IpsPlugin.getDefault().setSuppressLoggingDuringTest(false);
        ((IpsModel)IpsPlugin.getDefault().getIpsModel()).stopListeningToResourceChanges();
        IpsPlugin.getDefault().setFeatureVersionManagers(
                new IIpsFeatureVersionManager[] { new TestIpsFeatureVersionManager() });
        setAutoBuild(false);

        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                    System.out.println("AbstractIpsPlugin.setUp(): Start deleting projects.");
                }
                if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                    System.out.println("AbstractIpsPlugin.setUp(): Projects deleted.");
                }
                IpsPlugin.getDefault().reinitModel(); // also starts the listening process
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

        getIpsModel().addChangeListener(contentsChangeListener);
    }

    @After
    public void tearDown() throws Exception {
        IpsPlugin.getDefault().setFeatureVersionManagers(null);
        IpsPlugin.getDefault().setSuppressLoggingDuringTest(false);
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject project : projects) {
            project.delete(true, true, null);
        }
        getIpsModel().removeChangeListener(contentsChangeListener);
        tearDownExtension();
    }

    protected Document createXmlDocument(String xmlTag) throws ParserConfigurationException {
        Document xmlDocument = getDocumentBuilder().newDocument();
        xmlDocument.appendChild(xmlDocument.createElement(xmlTag));
        return xmlDocument;
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
     * Creates a new IpsProject with a random name. If you need a static name for your test, use
     * {@link #newIpsProject(String)} but choose a unique name because it is not save that eclipse
     * removed the resource of another test running before.
     */
    protected IIpsProject newIpsProject() throws CoreException {
        return newIpsProject(UUID.randomUUID().toString());
    }

    /**
     * Creates a new IpsProject with the given name.
     */
    protected IIpsProject newIpsProject(String name) throws CoreException {
        List<Locale> supportedLocales = new ArrayList<Locale>();
        supportedLocales.add(Locale.GERMAN);
        supportedLocales.add(Locale.US);
        return newIpsProject(name, supportedLocales);
    }

    /**
     * Creates a new IPS project with multi-language support for the given locales. The first locale
     * in the list will be used as default language.
     */
    protected IIpsProject newIpsProject(List<Locale> supportedLocales) throws CoreException {
        return newIpsProject(UUID.randomUUID().toString(), supportedLocales);
    }

    /**
     * Creates a new IPS project with the given name and multi-language support for the given
     * locales. The first locale in the list will be used as default language.
     */
    private IIpsProject newIpsProject(String name, List<Locale> supportedLocales) throws CoreException {
        return newIpsProjectBuilder().name(name).supportedLocales(supportedLocales).build();
    }

    /**
     * Returns an {@linkplain IpsProjectBuilder} that allows easy creation of
     * {@linkplain IpsProject IPS Projects}.
     */
    protected IpsProjectBuilder newIpsProjectBuilder() {
        return new IpsProjectBuilder(this);
    }

    /**
     * Creates a new platform project with the given name and opens it.
     */
    protected IProject newPlatformProject(String name) throws CoreException {
        return new PlatformProjectBuilder().name(name).build();
    }

    /*
     * TODO AW 03-12-2012: Attempt to move project creation code to IpsProjectBuilder and
     * PlatformProjectBuilder - The following methods could not be moved because they can be
     * overwritten by subclasses and are called during project creation (template method pattern
     * style). E.g. setTestArtefactBuilderSet is overwritten in AbstractStdBuilderSetTest. This is
     * very bad because it prevents us from moving that code to other classes.
     */

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
        javaProject.setRawClasspath(entries, null);
        addSystemLibraries(javaProject);
        return javaProject;
    }

    private void addSystemLibraries(IJavaProject javaProject) throws JavaModelException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        newEntries[oldEntries.length] = JavaRuntime.getDefaultJREContainerEntry();
        javaProject.setRawClasspath(newEntries, null);
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

        IIpsProjectProperties properties = ipsProject.getProperties();
        setTestArtefactBuilderSet(properties, ipsProject);
        // @formatter:off
        properties.setPredefinedDatatypesUsed(new String[] {
                Datatype.DECIMAL.getName(),
                Datatype.MONEY.getName(),
                Datatype.INTEGER.getName(),
                Datatype.PRIMITIVE_INT.getName(),
                Datatype.PRIMITIVE_LONG.getName(),
                Datatype.PRIMITIVE_BOOLEAN.getName(),
                Datatype.STRING.getName(),
                Datatype.BOOLEAN.getName() });
        // @formatter:on
        properties
                .setMinRequiredVersionNumber(
                        "org.faktorips.feature", (String)Platform.getBundle("org.faktorips.devtools.core").getHeaders().get("Bundle-Version")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ipsProject.setProperties(properties);
    }

    protected void setTestArtefactBuilderSet(IIpsProjectProperties properties, IIpsProject project)
            throws CoreException {

        // Create the builder set for the project
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet(
                new IIpsArtefactBuilder[] { new TestArtefactBuilder() });
        builderSet.setIpsProject(project);

        // Set the builder set id in the project's properties
        properties.setBuilderSetId(TestIpsArtefactBuilderSet.ID);

        // Add the new builder set to the builder set infos of the IPS model
        IpsModel ipsModel = (IpsModel)IpsPlugin.getDefault().getIpsModel();
        IIpsArtefactBuilderSetInfo[] builderSetInfos = ipsModel.getIpsArtefactBuilderSetInfos();
        List<IIpsArtefactBuilderSetInfo> newBuilderSetInfos = new ArrayList<IIpsArtefactBuilderSetInfo>(
                builderSetInfos.length + 1);
        for (IIpsArtefactBuilderSetInfo info : builderSetInfos) {
            newBuilderSetInfos.add(info);
        }
        newBuilderSetInfos.add(new TestArtefactBuilderSetInfo(builderSet));
        ipsModel.setIpsArtefactBuilderSetInfos(newBuilderSetInfos
                .toArray(new IIpsArtefactBuilderSetInfo[newBuilderSetInfos.size()]));
    }

    protected void addClasspathEntry(IJavaProject project, IClasspathEntry entry) throws JavaModelException {
        IClasspathEntry[] entries = project.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[entries.length] = entry;
        project.setRawClasspath(newEntries, null);
    }

    protected void waitForIndexer() throws JavaModelException {
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
                } else if (!createAutoProductCmptType && ipsObject instanceof IPolicyCmptType) {
                    ((IPolicyCmptType)ipsObject).setConfigurableByProductCmptType(false);
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
        enumType.setExtensible(false);
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
     * qualifiedName includes a package name, the package is created if it does not already exist.
     */
    protected ProductCmptType newProductCmptType(IProductCmptType supertype, String qualifiedName) throws CoreException {
        ProductCmptType newType = (ProductCmptType)newIpsObject(supertype.getIpsProject(),
                IpsObjectType.PRODUCT_CMPT_TYPE, qualifiedName);
        newType.setSupertype(supertype.getQualifiedName());
        newType.setConfigurationForPolicyCmptType(supertype.isConfigurationForPolicyCmptType());
        createDefaultCategoriesForProductCmptTypeAsNecessary(newType);
        return newType;
    }

    /**
     * Creates a new product component type in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exist.
     * Creates the product component type's default categories if they have not yet been created.
     */
    protected ProductCmptType newProductCmptType(IIpsProject ipsProject, String qualifiedName) throws CoreException {
        return newProductCmptType(ipsProject, qualifiedName, true);
    }

    /**
     * Creates a new product component type in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exist.
     * 
     * @param createDefaultCategories <code>true</code> if default categories should be created.
     *            <code>false</code> if not. e.g. Use <code>false</code> if a product component type
     *            is intended to have a super type.
     */
    protected ProductCmptType newProductCmptType(IIpsProject ipsProject,
            String qualifiedName,
            boolean createDefaultCategories) throws CoreException {
        ProductCmptType productCmptType = (ProductCmptType)newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT_TYPE,
                qualifiedName);
        if (createDefaultCategories) {
            createDefaultCategoriesForProductCmptTypeAsNecessary(productCmptType);
        }
        return productCmptType;
    }

    /**
     * Creates a new product component type in the indicated package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exist.
     */
    protected ProductCmptType newProductCmptType(final IIpsPackageFragmentRoot root, final String qualifiedName)
            throws CoreException {

        ProductCmptType productCmptType = (ProductCmptType)newIpsObject(root, IpsObjectType.PRODUCT_CMPT_TYPE,
                qualifiedName);
        createDefaultCategoriesForProductCmptTypeAsNecessary(productCmptType);
        return productCmptType;
    }

    private void createDefaultCategoriesForProductCmptTypeAsNecessary(IProductCmptType productCmptType)
            throws CoreException {

        boolean defaultFormulasFound = false;
        boolean defaultPolicyAttributesFound = false;
        boolean defaultProductAttributesFound = false;
        boolean defaultTablesFound = false;
        boolean defaultValidationRulesFound = false;

        for (IProductCmptCategory category : productCmptType.findCategories(productCmptType.getIpsProject())) {
            if (category.isDefaultForFormulaSignatureDefinitions()) {
                defaultFormulasFound = true;
            }
            if (category.isDefaultForPolicyCmptTypeAttributes()) {
                defaultPolicyAttributesFound = true;
            }
            if (category.isDefaultForProductCmptTypeAttributes()) {
                defaultProductAttributesFound = true;
            }
            if (category.isDefaultForTableStructureUsages()) {
                defaultTablesFound = true;
            }
            if (category.isDefaultForValidationRules()) {
                defaultValidationRulesFound = true;
            }
            if (defaultFormulasFound && defaultPolicyAttributesFound && defaultProductAttributesFound
                    && defaultTablesFound && defaultValidationRulesFound) {
                break;
            }
        }

        if (!defaultFormulasFound) {
            IProductCmptCategory defaultCategory = productCmptType
                    .newCategory(DEFAULT_CATEGORY_NAME_FORMULA_SIGNATURE_DEFINITIONS);
            defaultCategory.setDefaultForFormulaSignatureDefinitions(true);
        }
        if (!defaultPolicyAttributesFound) {
            IProductCmptCategory defaultCategory = productCmptType
                    .newCategory(DEFAULT_CATEGORY_NAME_POLICY_CMPT_TYPE_ATTRIBUTES);
            defaultCategory.setDefaultForPolicyCmptTypeAttributes(true);
        }
        if (!defaultProductAttributesFound) {
            IProductCmptCategory defaultCategory = productCmptType
                    .newCategory(DEFAULT_CATEGORY_NAME_PRODUCT_CMPT_TYPE_ATTRIBUTES);
            defaultCategory.setDefaultForProductCmptTypeAttributes(true);
        }
        if (!defaultTablesFound) {
            IProductCmptCategory defaultCategory = productCmptType
                    .newCategory(DEFAULT_CATEGORY_NAME_TABLE_STRUCTURE_USAGES);
            defaultCategory.setDefaultForTableStructureUsages(true);
        }
        if (!defaultValidationRulesFound) {
            IProductCmptCategory defaultCategory = productCmptType.newCategory(DEFAULT_CATEGORY_NAME_VALIDATION_RULES);
            defaultCategory.setDefaultForValidationRules(true);
        }

        productCmptType.getIpsSrcFile().save(true, null);
    }

    /**
     * Creates a new product component type in the indicated package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exist.
     * Creates the default categories if they have not yet been created.
     */
    protected PolicyCmptType newPolicyAndProductCmptType(IIpsProject ipsProject,
            String policyCmptTypeName,
            String productCmptTypeName) throws CoreException {
        return newPolicyAndProductCmptType(ipsProject, policyCmptTypeName, productCmptTypeName, true);
    }

    /**
     * Creates a new product component type in the indicated package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exist.
     * 
     * @param createDefaultCategories <code>true</code> if default categories should be created.
     *            <code>false</code> if not. e.g. Use <code>false</code> if a product component type
     *            is intended to have a super type.
     */
    protected PolicyCmptType newPolicyAndProductCmptType(IIpsProject ipsProject,
            String policyCmptTypeName,
            String productCmptTypeName,
            boolean createDefaultCategories) throws CoreException {

        IPolicyCmptType policyCmptType = (IPolicyCmptType)newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0],
                IpsObjectType.POLICY_CMPT_TYPE, policyCmptTypeName, false);
        ProductCmptType productCmptType;
        productCmptType = newProductCmptType(ipsProject, productCmptTypeName, createDefaultCategories);
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(policyCmptTypeName);
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType(productCmptTypeName);
        policyCmptType.getIpsSrcFile().save(true, null);
        productCmptType.getIpsSrcFile().save(true, null);
        return (PolicyCmptType)policyCmptType;
    }

    /**
     * Creates a new composition (master-detail) between 'from' and 'to' type and the inverse
     * detail-master association. The role name singular is set to the target's unqualified name.
     * The plural name is the singular name followed by an 's'. Min cardinality is 1, max
     * cardinality is '*'.
     * 
     * @param save <code>true</code> if the file that contain to and from are saved after adding the
     *            associations
     * 
     * @throws CoreException if an error occurs while saving the files.
     */
    public IPolicyCmptTypeAssociation newComposition(IPolicyCmptType from, IPolicyCmptType to, boolean save)
            throws CoreException {
        IPolicyCmptTypeAssociation master2detail = from.newPolicyCmptTypeAssociation();
        master2detail.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        master2detail.setTarget(to.getQualifiedName());
        master2detail.setTargetRoleSingular(to.getUnqualifiedName());
        master2detail.setTargetRolePlural(to.getUnqualifiedName() + "s");
        master2detail.setMinCardinality(1);
        master2detail.setMaxCardinality(Integer.MAX_VALUE);

        IPolicyCmptTypeAssociation detail2master = to.newPolicyCmptTypeAssociation();
        detail2master.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        detail2master.setTarget(from.getQualifiedName());
        detail2master.setTargetRoleSingular(from.getUnqualifiedName());
        detail2master.setTargetRolePlural(from.getUnqualifiedName() + "s");
        detail2master.setMinCardinality(1);
        detail2master.setMaxCardinality(1);

        // inverse settings
        master2detail.setInverseAssociation(detail2master.getName());
        detail2master.setInverseAssociation(master2detail.getName());

        if (save) {
            from.getIpsSrcFile().save(true, null);
            to.getIpsSrcFile().save(true, null);
        }
        return master2detail;
    }

    /**
     * Creates a new composition (master-detail) between 'from' and 'to' type and the inverse
     * detail-master association. The role name singular is set to the target's unqualified name.
     * The plural name is the singular name followed by an 's'. Min cardinality is 1, max
     * cardinality is '*'.
     * <p>
     * The files containing from and to are saved. If you don't want to save the file use
     * {@link #newComposition(IPolicyCmptType, IPolicyCmptType, boolean)}.
     * 
     * @throws CoreException if an error occurs while saving the files.
     */
    public IPolicyCmptTypeAssociation newComposition(IPolicyCmptType from, IPolicyCmptType to) throws CoreException {
        return newComposition(from, to, true);
    }

    /**
     * Creates a new aggregation between 'from' and 'to' type. The role name singular is set to the
     * target's unqualified name. The plural name is the singular name followed by an 's'. Min
     * cardinality is 1, max cardinality is '*'.
     * <p>
     * The file containing from and to are saved. If you don't want to save the file use
     * {@link #newComposition(IPolicyCmptType, IPolicyCmptType, boolean)}.
     * 
     * @throws CoreException if an error occurs while saving the files.
     */
    public IProductCmptTypeAssociation newAggregation(IProductCmptType from, IProductCmptType to) throws CoreException {
        return newAggregation(from, to, true);
    }

    /**
     * Creates a new aggregation between 'from' and 'to' type. The role name singular is set to the
     * target's unqualified name. The plural name is the singular name followed by an 's'. Min
     * cardinality is 1, max cardinality is '*'.
     * 
     * @throws CoreException if an error occurs while saving the files.
     */
    public IProductCmptTypeAssociation newAggregation(IProductCmptType from, IProductCmptType to, boolean save)
            throws CoreException {
        IProductCmptTypeAssociation agg = from.newProductCmptTypeAssociation();
        agg.setAssociationType(AssociationType.AGGREGATION);
        agg.setTarget(to.getQualifiedName());
        agg.setTargetRoleSingular(to.getUnqualifiedName());
        agg.setTargetRolePlural(to.getUnqualifiedName() + "s");
        agg.setMinCardinality(1);
        agg.setMaxCardinality(Integer.MAX_VALUE);
        if (save) {
            from.getIpsSrcFile().save(true, null);
            to.getIpsSrcFile().save(true, null);
        }
        return agg;
    }

    /**
     * Creates a new product component that is based on the given product component type and has one
     * generation with it's valid from date set 2012-07-18, 01:01:01. The product component is
     * stored in the same package fragment root as the type. If the qualifiedName includes a package
     * name, the package is created if it does not already exists.
     */
    protected ProductCmpt newProductCmpt(IProductCmptType type, String qualifiedName) throws CoreException {
        IProductCmpt productCmpt = (IProductCmpt)newIpsObject(type.getIpsPackageFragment().getRoot(),
                IpsObjectType.PRODUCT_CMPT, qualifiedName);
        productCmpt.setProductCmptType(type.getQualifiedName());
        productCmpt.newGeneration(new GregorianCalendar(2012, 06, 18, 1, 1, 1));
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
        tableContents.newGeneration(new GregorianCalendar());
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
    protected TestCase newTestCase(ITestCaseType tCase, String qualifiedName) throws CoreException {
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
        boolean writeOk = false;
        try {
            prop.getWriteMethod().invoke(object, new Object[] { testValueToSet });
            writeOk = true;
            Object retValue = prop.getReadMethod().invoke(object, new Object[0]);
            assertEquals("Getter method for property " + propertyName + " of class " + clazz.getName()
                    + " does not return the expected value", testValueToSet, retValue);
            assertNotNull("Setter method for property " + propertyName + " of class " + clazz.getName()
                    + " hasn't triggered a change event", getLastContentChangeEvent());
        } catch (Exception e) {
            if (writeOk) {
                fail("An exception occured while reading property " + propertyName + " of class " + clazz.getName());
            } else {
                fail("An exception occured while setting property " + propertyName + " of class " + clazz.getName());
            }
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

    /**
     * Performs the Faktor-IPS 'Rename' refactoring for the given {@link IIpsObjectPartContainer}
     * and provided new name.
     */
    protected final RefactoringStatus performRenameRefactoring(IIpsObjectPartContainer ipsObjectPartContainer,
            String newName) throws CoreException {

        return performRenameRefactoring(ipsObjectPartContainer, newName, null, false);
    }

    /**
     * Performs the Faktor-IPS 'Rename' refactoring for the given {@link IProductCmpt} and provided
     * new name, thereby allowing to adapt the runtime id.
     */
    protected final RefactoringStatus performRenameRefactoring(IProductCmpt productCmpt,
            String newName,
            boolean adaptRuntimeId) throws CoreException {

        return performRenameRefactoring(productCmpt, newName, null, adaptRuntimeId);
    }

    /**
     * Performs the Faktor-IPS 'Rename' refactoring for the given {@link IIpsObjectPartContainer},
     * provided new name and provided new plural name.
     */
    protected final RefactoringStatus performRenameRefactoring(IIpsObjectPartContainer ipsObjectPartContainer,
            String newName,
            String newPluralName) throws CoreException {

        return performRenameRefactoring(ipsObjectPartContainer, newName, newPluralName, false);
    }

    private RefactoringStatus performRenameRefactoring(IIpsObjectPartContainer ipsObjectPartContainer,
            String newName,
            String newPluralName,
            boolean adaptRuntimeId) throws CoreException {

        printValidationResult(ipsObjectPartContainer);

        IIpsRefactoring ipsRenameRefactoring = IpsPlugin.getIpsRefactoringFactory().createRenameRefactoring(
                ipsObjectPartContainer, newName, newPluralName, adaptRuntimeId);

        return performRefactoring(ipsRenameRefactoring);
    }

    /**
     * Performs the Faktor-IPS 'Pull Up' refactoring for the given {@link IIpsObjectPart} and target
     * {@link IIpsObjectPartContainer}.
     */
    protected final RefactoringStatus performPullUpRefactoring(IIpsObjectPart ipsObjectPart,
            IIpsObjectPartContainer targetIpsObjectPartContainer) throws CoreException {

        printValidationResult(ipsObjectPart);

        IIpsRefactoring ipsPullUpRefactoring = IpsPlugin.getIpsRefactoringFactory().createPullUpRefactoring(
                ipsObjectPart, targetIpsObjectPartContainer);

        return performRefactoring(ipsPullUpRefactoring);
    }

    /**
     * Performs the Faktor-IPS 'Move' refactoring for the given {@link IIpsObject} and provided
     * target {@link IIpsPackageFragment}.
     */
    protected final RefactoringStatus performMoveRefactoring(IIpsObject ipsObject,
            IIpsPackageFragment targetIpsPackageFragment) throws CoreException {

        printValidationResult(ipsObject);

        IIpsRefactoring ipsMoveRefactoring = IpsPlugin.getIpsRefactoringFactory().createMoveRefactoring(ipsObject,
                targetIpsPackageFragment);

        return performRefactoring(ipsMoveRefactoring);
    }

    /**
     * Performs a composite Faktor-IPS 'Move' refactoring for the given {@link IIpsObject}s and
     * provided target {@link IIpsPackageFragment}.
     */
    protected final RefactoringStatus performCompositeMoveRefactoring(Set<IIpsObject> ipsObjects,
            IIpsPackageFragment targetIpsPackageFragment) throws CoreException {

        for (IIpsObject ipsObject : ipsObjects) {
            printValidationResult(ipsObject);
            ipsObject.getIpsSrcFile().save(true, null);
        }

        IIpsRefactoring ipsCompositeMoveRefactoring = IpsPlugin.getIpsRefactoringFactory()
                .createCompositeMoveRefactoring(ipsObjects, targetIpsPackageFragment);

        return performRefactoring(ipsCompositeMoveRefactoring);
    }

    private RefactoringStatus performRefactoring(IIpsRefactoring ipsRefactoring) throws CoreException {
        PerformRefactoringOperation operation = new PerformRefactoringOperation(ipsRefactoring.toLtkRefactoring(),
                CheckConditionsOperation.ALL_CONDITIONS);
        ResourcesPlugin.getWorkspace().run(operation, new NullProgressMonitor());
        RefactoringStatus conditionStatus = operation.getConditionStatus();
        return conditionStatus;
    }

    /**
     * Prints the validation result of the given {@link IIpsObjectPartContainer} to the console if
     * the severity is at least at the warning level.
     */
    protected final void printValidationResult(IIpsObjectPartContainer ipsObjectPartContainer) throws CoreException {
        MessageList validationResult = ipsObjectPartContainer.validate(ipsObjectPartContainer.getIpsProject());
        if (validationResult.getSeverity() == Message.WARNING || validationResult.getSeverity() == Message.ERROR) {
            System.out.println(validationResult.getFirstMessage(Message.ERROR));
        }
    }

    /**
     * Clears the output folders of the given {@link IIpsProject} (to avoid code merging problems)
     * and performs a full build.
     */
    protected final void performFullBuild(IIpsProject ipsProject) throws CoreException {
        clearOutputFolders(ipsProject); // To avoid code merging problems
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

    private void clearOutputFolders(IIpsProject ipsProject) throws CoreException {
        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        if (ipsObjectPath.isOutputDefinedPerSrcFolder()) {
            for (IIpsSrcFolderEntry srcFolderEntry : ipsObjectPath.getSourceFolderEntries()) {
                IFolder outputFolderDerived = srcFolderEntry.getOutputFolderForDerivedJavaFiles();
                IFolder outputFolderMergable = srcFolderEntry.getOutputFolderForMergableJavaFiles();
                clearFolder(outputFolderDerived);
                clearFolder(outputFolderMergable);
            }
        } else {
            IFolder outputFolderDerived = ipsObjectPath.getOutputFolderForDerivedSources();
            IFolder outputFolderMergable = ipsObjectPath.getOutputFolderForMergableSources();
            clearFolder(outputFolderDerived);
            clearFolder(outputFolderMergable);
        }
    }

    private void clearFolder(IFolder folder) throws CoreException {
        for (IResource resource : folder.members()) {
            resource.delete(true, null);
        }
    }

    protected final void assertOneValidationMessage(MessageList list,
            String code,
            Object invalidObject,
            String property,
            int severity) {

        Message expectedMessage = list.getFirstMessage(severity);
        assertEquals(1, list.size());
        assertEquals(code, expectedMessage.getCode());
        assertEquals(new ObjectProperty(invalidObject, property), expectedMessage.getInvalidObjectProperties()[0]);
    }

    protected final void assertPropertyChangedEvent(IIpsObjectPart part,
            String property,
            Object oldValue,
            Object newValue) {

        assertContentChangedEvent(part.getIpsSrcFile(), ContentChangeEvent.TYPE_PROPERTY_CHANGED);
        assertEquals(part, getLastContentChangeEvent().getPart());
        assertNotNull(getLastContentChangeEvent().getPropertyChangeEvent());
        assertEquals(property, getLastContentChangeEvent().getPropertyChangeEvent().getPropertyName());
        assertEquals(oldValue, getLastContentChangeEvent().getPropertyChangeEvent().getOldValue());
        assertEquals(newValue, getLastContentChangeEvent().getPropertyChangeEvent().getNewValue());
    }

    protected final void assertSingleContentChangeEvent() {
        assertEquals(1, contentsChangeListener.numberContentChangeEvents);
    }

    protected final void assertWholeContentChangedEvent(IIpsSrcFile ipsSrcFile) {
        assertContentChangedEvent(ipsSrcFile, ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED);
    }

    protected final void assertContentChangedEvent(IIpsSrcFile ipsSrcFile, int eventType) {
        assertEquals(ipsSrcFile, getLastContentChangeEvent().getIpsSrcFile());
        assertEquals(eventType, getLastContentChangeEvent().getEventType());
    }

    protected final void setPartId(IIpsObjectPart part, String id) throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        Field field = IpsObjectPart.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(part, id);
    }

    protected final ContentChangeEvent getLastContentChangeEvent() {
        return contentsChangeListener.lastContentChangeEvent;
    }

    protected final void resetLastContentChangeEvent() {
        contentsChangeListener.lastContentChangeEvent = null;
    }

    protected final void resetNumberContentChangeEvents() {
        contentsChangeListener.numberContentChangeEvents = 0;
    }

    private static class TestChangeListener implements ContentsChangeListener {

        private ContentChangeEvent lastContentChangeEvent;

        private int numberContentChangeEvents;

        @Override
        public void contentsChanged(ContentChangeEvent event) {
            lastContentChangeEvent = event;
            numberContentChangeEvents++;
        }

    }

    private static class TestArtefactBuilder extends AbstractArtefactBuilder {

        public TestArtefactBuilder() throws CoreException {
            super(new TestIpsArtefactBuilderSet());
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void build(IIpsSrcFile ipsSrcFile) throws CoreException {

        }

        @Override
        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
            return false;
        }

        @Override
        public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {

        }

        @Override
        public boolean isBuildingInternalArtifacts() {
            return false;
        }

    }

}
