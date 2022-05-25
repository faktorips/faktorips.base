/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import static org.faktorips.testsupport.IpsMatchers.hasInvalidObject;
import static org.faktorips.testsupport.IpsMatchers.hasSize;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.faktorips.abstracttest.builder.TestArtefactBuilderSetInfo;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.abstracttest.test.XmlAbstractTestCase;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.mapping.PathMapping;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.model.datatype.IDynamicEnumDatatype;
import org.faktorips.devtools.model.datatype.IDynamicValueDatatype;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.IpsModel.EclipseIpsModel;
import org.faktorips.devtools.model.internal.datatype.DynamicEnumDatatype;
import org.faktorips.devtools.model.internal.enums.EnumContent;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPart;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.internal.tablestructure.TableStructure;
import org.faktorips.devtools.model.internal.testcase.TestCase;
import org.faktorips.devtools.model.internal.testcasetype.TestCaseType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.util.BeanUtil;
import org.faktorips.devtools.model.util.IpsProjectUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.util.StringUtil;
import org.junit.After;
import org.junit.AfterClass;
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

    protected final TestChangeListener contentsChangeListener;
    private TestIpsModelExtensions testIpsModelExtensions;

    public AbstractIpsPluginTest() {
        super();
        contentsChangeListener = new TestChangeListener();
    }

    @Before
    @SuppressWarnings("deprecation")
    public void setUp() throws Exception {
        IpsLog.setSuppressLoggingDuringTest(false);
        if (Abstractions.isEclipseRunning()) {
            ((EclipseIpsModel)IpsModel.get()).stopListeningToResourceChanges();
            setAutoBuild(false);
        }
        testIpsModelExtensions = TestIpsModelExtensions.get();
        testIpsModelExtensions.setFeatureVersionManagers(new TestIpsFeatureVersionManager());

        ICoreRunnable runnable = monitor -> {
            if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                System.out.println("AbstractIpsPlugin.setUp(): Start deleting projects.");
            }
            if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                System.out.println("AbstractIpsPlugin.setUp(): Projects deleted.");
            }
            // also starts the listening process
            IpsModel.reInit();
        };

        if (Abstractions.isEclipseRunning()) {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
        } else {
            AWorkspace workspace = Abstractions.getWorkspace();
            workspace.run(runnable, null);
        }
        getIpsModel().addChangeListener(contentsChangeListener);
    }

    @After
    public void tearDown() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            testIpsModelExtensions.close();
            IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
            for (IProject project : projects) {
                deleteProject(project);
            }
        } else {
            Abstractions.getWorkspace().getRoot().getProjects().stream().forEach(p -> p.delete(null));
        }

        getIpsModel().removeChangeListener(contentsChangeListener);
        tearDownExtension();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        if (!Abstractions.isEclipseRunning()) {
            File workspace = Abstractions.getWorkspace().getRoot().unwrap();
            workspace.deleteOnExit();
        }
    }

    private void deleteProject(IProject project) throws IpsException {
        try {
            project.delete(true, true, null);
        } catch (CoreException e) {
            // We are running into some race condition on Windows
            // Wait a little and try to delete one more time when this happens
            if (project.exists() && !SystemUtils.IS_OS_WINDOWS) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.interrupted();
                }
                try {
                    project.delete(true, true, null);
                } catch (CoreException e1) {
                    throw new IpsException(e1);
                }
            }
        }
    }

    protected Document createXmlDocument(String xmlTag) throws ParserConfigurationException {
        Document xmlDocument = getDocumentBuilder().newDocument();
        xmlDocument.appendChild(xmlDocument.createElement(xmlTag));
        return xmlDocument;
    }

    protected void tearDownExtension() throws Exception {

    }

    protected void suppressLoggingDuringExecutionOfThisTestCase() {
        IpsLog.setSuppressLoggingDuringTest(true);
    }

    protected void createArchive(IIpsProject projectToArchive, AFile archiveFile) throws Exception {
        File file = createFileIfNecessary(archiveFile);

        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(projectToArchive, file);
        Abstractions.getWorkspace().run(op, null);

        createLinkIfNecessary(archiveFile, file);
    }

    protected void createArchive(IIpsPackageFragmentRoot rootToArchive, AFile archiveFile) throws Exception {
        File file = createFileIfNecessary(archiveFile);

        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(rootToArchive, file);
        Abstractions.getWorkspace().run(op, null);

        createLinkIfNecessary(archiveFile, file);
    }

    /**
     * Creates a links to the given file in the workspace.
     */
    protected void createLinkIfNecessary(AFile archiveFile, File file) {
        if (Abstractions.isEclipseRunning()) {
            IFile eclipseFile = archiveFile.unwrap();
            if (!eclipseFile.isLinked() && !archiveFile.exists()) {
                try {
                    eclipseFile.createLink(new Path(file.getAbsolutePath()), 0, new NullProgressMonitor());
                } catch (CoreException e) {
                    throw new IpsException(e);
                }
            }
        }
    }

    protected File createFileIfNecessary(AFile archiveFile) {
        File file = null;
        if (archiveFile.exists()) {
            file = archiveFile.getLocation().toFile();
        } else {
            archiveFile.create(InputStream.nullInputStream(), null);
            file = archiveFile.getLocation().toFile();
        }
        return file;
    }

    /**
     * Creates a new IpsProject with a random name. If you need a static name for your test, use
     * {@link #newIpsProject(String)} but choose a unique name because it is not save that eclipse
     * removed the resource of another test running before.
     */
    protected IIpsProject newIpsProject() {
        return newIpsProject(UUID.randomUUID().toString());
    }

    /**
     * Creates a new IpsProject with the given name.
     */
    protected IIpsProject newIpsProject(String name) {
        List<Locale> supportedLocales = new ArrayList<>();
        supportedLocales.add(Locale.GERMAN);
        supportedLocales.add(Locale.US);
        return newIpsProject(name, supportedLocales);
    }

    /**
     * Creates a new IPS project with multi-language support for the given locales. The first locale
     * in the list will be used as default language.
     */
    protected IIpsProject newIpsProject(List<Locale> supportedLocales) {
        return newIpsProject(UUID.randomUUID().toString(), supportedLocales);
    }

    /**
     * Creates a new IPS project with the given name and multi-language support for the given
     * locales. The first locale in the list will be used as default language.
     */
    private IIpsProject newIpsProject(String name, List<Locale> supportedLocales) {
        return newIpsProjectBuilder().name(name).supportedLocales(supportedLocales).build();
    }

    /**
     * Returns an {@linkplain IpsProjectBuilder} that allows easy creation of
     * {@linkplain IIpsProject IPS Projects}.
     */
    protected IpsProjectBuilder newIpsProjectBuilder() {
        return new IpsProjectBuilder(this);
    }

    /**
     * Creates a new platform project with the given name and opens it.
     */
    protected AProject newPlatformProject(String name) {
        return new PlatformProjectBuilder().name(name).build();
    }

    /**
     * Creates a new platform project with the given name and description and opens it.
     */
    protected AProject newPlatformProject(String name, IProjectDescription description) {
        return new PlatformProjectBuilder().name(name).description(description).build();
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
    protected AJavaProject addJavaCapabilities(IProject project) throws CoreException {
        return addJavaCapabilities(Wrappers.wrap(project).as(AProject.class));
    }

    /**
     * Creates a new Java Project for the given platform project.
     */
    protected AJavaProject addJavaCapabilities(AProject project) throws CoreException {
        return JavaProjectUtil.addJavaCapabilities(project);
    }

    /**
     * Converts the given Java project to a Java 11 module project.
     */
    protected void convertToModuleProject(AJavaProject javaProject) {
        JavaProjectUtil.convertToModuleProject(javaProject.unwrap());
    }

    /**
     * Converts the given Java project to a Java 11 module project.
     */
    protected void convertToModuleProject(IJavaProject javaProject) {
        JavaProjectUtil.convertToModuleProject(javaProject);
    }

    protected void addIpsCapabilities(AProject project) {
        if (Abstractions.isEclipseRunning()) {
            try {
                IpsProjectUtil.addNature(project.unwrap(), IIpsProject.NATURE_ID);
            } catch (CoreException e) {
                throw new IpsException(e);
            }
        }
        AFolder rootFolder = project.getFolder("productdef");
        rootFolder.create(null);
        IIpsProject ipsProject = IIpsModel.get().getIpsProject(project.getName());
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
        String version = Abstractions.getVersion().toString();
        properties.setMinRequiredVersionNumber("org.faktorips.feature", version); //$NON-NLS-1$
        properties.setValidateIpsSchema(false);
        ipsProject.setProperties(properties);
    }

    protected void setTestArtefactBuilderSet(IIpsProjectProperties properties, IIpsProject project) {

        // Create the builder set for the project
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet(
                new IIpsArtefactBuilder[] { new TestArtefactBuilder() });
        builderSet.setIpsProject(project);

        // Set the builder set id in the project's properties
        properties.setBuilderSetId(TestIpsArtefactBuilderSet.ID);

        // Add the new builder set to the builder set infos of the IPS model
        IpsModel ipsModel = (IpsModel)IIpsModel.get();
        IIpsArtefactBuilderSetInfo[] builderSetInfos = ipsModel.getIpsArtefactBuilderSetInfos();
        List<IIpsArtefactBuilderSetInfo> newBuilderSetInfos = new ArrayList<>(
                builderSetInfos.length + 1);
        for (IIpsArtefactBuilderSetInfo info : builderSetInfos) {
            newBuilderSetInfos.add(info);
        }
        newBuilderSetInfos.add(new TestArtefactBuilderSetInfo(builderSet));
        ipsModel.setIpsArtefactBuilderSetInfos(
                newBuilderSetInfos.toArray(new IIpsArtefactBuilderSetInfo[newBuilderSetInfos.size()]));
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
        if (Abstractions.isEclipseRunning()) {
            IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
            if (autoBuild != ResourcesPlugin.getWorkspace().isAutoBuilding()) {
                description.setAutoBuilding(autoBuild);
                ResourcesPlugin.getWorkspace().setDescription(description);
            }
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
            AFolder parentFolder,
            String name) {

        AFolder newRootFolder;
        if (parentFolder == null) {
            newRootFolder = ipsProject.getProject().getFolder(name);
        } else {
            newRootFolder = parentFolder.getFolder(name);
        }
        newRootFolder.create(null);

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(newRootFolder);
        entry.setUniqueQualifier(name);
        ipsProject.setIpsObjectPath(path);

        return ipsProject.findIpsPackageFragmentRoot(name);
    }

    /**
     * Creates a new ipsobject in the indicated project's first source folder. If the qualifiedName
     * includes a package name, the package is created if it does not already exists.
     */
    protected IIpsObject newIpsObject(IIpsProject ipsProject, IpsObjectType type, String qualifiedName) {
        IIpsPackageFragmentRoot root = ipsProject.getSourceIpsPackageFragmentRoots()[0];
        return newIpsObject(root, type, qualifiedName);
    }

    /**
     * Creates a new ipsobject in the indicated package fragment root. If the qualifiedName includes
     * a package name, the package is created if it does not already exists.
     */
    protected IIpsObject newIpsObject(final IIpsPackageFragmentRoot root,
            final IpsObjectType type,
            final String qualifiedName) {

        return newIpsObject(root, type, qualifiedName, true);
    }

    /**
     * Creates a new ipsobject in the indicated package fragment root. If the qualifiedName includes
     * a package name, the package is created if it does not already exists.
     */
    private IIpsObject newIpsObject(final IIpsPackageFragmentRoot root,
            final IpsObjectType type,
            final String qualifiedName,
            final boolean createAutoProductCmptType) {

        final String packName = StringUtil.getPackageName(qualifiedName);
        final String unqualifiedName = StringUtil.unqualifiedName(qualifiedName);
        ICoreRunnable runnable = $ -> {
            IIpsPackageFragment pack = root.getIpsPackageFragment(packName);
            if (!pack.exists()) {
                pack = root.createPackageFragment(packName, true, null);
            }
            IIpsSrcFile file = pack.createIpsFile(type, unqualifiedName, true, null);
            IIpsObject ipsObject = file.getIpsObject();
            if (createAutoProductCmptType && ipsObject instanceof IPolicyCmptType) {
                ((IPolicyCmptType)ipsObject).setConfigurableByProductCmptType(true);
                ((IPolicyCmptType)ipsObject).setProductCmptType(qualifiedName + "ProductCmpt");
                ProductCmptType productCmptType = newProductCmptType(root, qualifiedName + "ProductCmpt");
                productCmptType.setConfigurationForPolicyCmptType(true);
                productCmptType.setPolicyCmptType(qualifiedName);
            } else if (!createAutoProductCmptType && ipsObject instanceof IPolicyCmptType) {
                ((IPolicyCmptType)ipsObject).setConfigurableByProductCmptType(false);
            }
        };
        Abstractions.getWorkspace().run(runnable, null);
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
     * @throws IpsException If the enum content could not be created.
     */
    protected EnumContent newEnumContent(final IIpsPackageFragmentRoot root, final String qualifiedName) {

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
     * @throws IpsException If the enum content could not be created.
     */
    protected EnumContent newEnumContent(final IIpsProject ipsProject, final String qualifiedName) {
        return (EnumContent)newIpsObject(ipsProject, IpsObjectType.ENUM_CONTENT, qualifiedName);
    }

    /**
     * Creates a new enum content that is based on the given enum type. The product component is
     * stored in the same package fragment root as the type. If the qualifiedName includes a package
     * name, the package is created if it does not already exists.
     */
    protected EnumContent newEnumContent(IEnumType type, String qualifiedName) {
        EnumContent enumContent = (EnumContent)newIpsObject(type.getIpsPackageFragment().getRoot(),
                IpsObjectType.ENUM_CONTENT, qualifiedName);
        enumContent.setEnumType(type.getQualifiedName());
        enumContent.getIpsSrcFile().save(null);
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
     * @throws IpsException If the enum type could not be created.
     */
    protected EnumType newEnumType(final IIpsPackageFragmentRoot root, final String qualifiedName) {
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
     * @throws IpsException If the enum type could not be created.
     */
    protected EnumType newEnumType(final IIpsProject ipsProject, final String qualifiedName) {
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
     * @throws IpsException If the enum type could not be created.
     */
    protected EnumType newDefaultEnumType(final IIpsProject ipsProject, final String qualifiedName) {

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
    protected PolicyCmptType newPolicyCmptType(final IIpsPackageFragmentRoot root, final String qualifiedName) {
        return (PolicyCmptType)newIpsObject(root, IpsObjectType.POLICY_CMPT_TYPE, qualifiedName);
    }

    /**
     * Creates a new policy component type in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected PolicyCmptType newPolicyCmptType(IIpsProject ipsProject, String qualifiedName) {
        return (PolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, qualifiedName);
    }

    /**
     * Creates a new policy component type in the project's first package fragment root. Does not
     * create a product component type. If the qualifiedName includes a package name, the package is
     * created if it does not already exists.
     */
    protected PolicyCmptType newPolicyCmptTypeWithoutProductCmptType(IIpsProject ipsProject, String qualifiedName) {
        return (PolicyCmptType)newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.POLICY_CMPT_TYPE,
                qualifiedName, false);
    }

    /**
     * Creates a new product component type in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exist.
     */
    protected ProductCmptType newProductCmptType(IProductCmptType supertype, String qualifiedName) {
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
    protected ProductCmptType newProductCmptType(IIpsProject ipsProject, String qualifiedName) {
        return newProductCmptType(ipsProject, qualifiedName, true);
    }

    /**
     * Creates a new product component type in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exist.
     * Creates the product component type's default categories if they have not yet been created.
     * Sets the product component type to configure the given policy component type.
     */
    protected ProductCmptType newProductCmptType(IIpsProject ipsProject,
            String qualifiedName,
            String policyCmptTypeQualifiedName) {
        ProductCmptType newProductCmptType = newProductCmptType(ipsProject, qualifiedName, true);
        newProductCmptType.setConfigurationForPolicyCmptType(true);
        newProductCmptType.setPolicyCmptType(policyCmptTypeQualifiedName);
        return newProductCmptType;
    }

    /**
     * Creates a new product component type in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exist.
     * Creates the product component type's default categories if they have not yet been created.
     * Sets the product component type to configure the given policy component type.
     */
    protected ProductCmptType newProductCmptType(IIpsProject ipsProject,
            String qualifiedName,
            IPolicyCmptType policyCmptType) {
        return newProductCmptType(ipsProject, qualifiedName, policyCmptType.getQualifiedName());
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
            boolean createDefaultCategories) {
        ProductCmptType productCmptType = (ProductCmptType)newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT_TYPE,
                qualifiedName);
        productCmptType.setConfigurationForPolicyCmptType(false);
        if (createDefaultCategories) {
            createDefaultCategoriesForProductCmptTypeAsNecessary(productCmptType);
        }
        return productCmptType;
    }

    /**
     * Creates a new product component type in the indicated package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exist.
     */
    protected ProductCmptType newProductCmptType(final IIpsPackageFragmentRoot root, final String qualifiedName) {
        ProductCmptType productCmptType = (ProductCmptType)newIpsObject(root, IpsObjectType.PRODUCT_CMPT_TYPE,
                qualifiedName);
        productCmptType.setConfigurationForPolicyCmptType(false);
        createDefaultCategoriesForProductCmptTypeAsNecessary(productCmptType);
        return productCmptType;
    }

    private void createDefaultCategoriesForProductCmptTypeAsNecessary(IProductCmptType productCmptType) {

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

        productCmptType.getIpsSrcFile().save(null);
    }

    /**
     * Creates a new product component type in the indicated package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exist.
     * Creates the default categories if they have not yet been created.
     */
    protected PolicyCmptType newPolicyAndProductCmptType(IIpsProject ipsProject,
            String policyCmptTypeName,
            String productCmptTypeName) {
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
            boolean createDefaultCategories) {

        IPolicyCmptType policyCmptType = (IPolicyCmptType)newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0],
                IpsObjectType.POLICY_CMPT_TYPE, policyCmptTypeName, false);
        ProductCmptType productCmptType;
        productCmptType = newProductCmptType(ipsProject, productCmptTypeName, createDefaultCategories);
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(policyCmptTypeName);
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType(productCmptTypeName);
        policyCmptType.getIpsSrcFile().save(null);
        productCmptType.getIpsSrcFile().save(null);
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
     * @throws IpsException if an error occurs while saving the files.
     */
    public IPolicyCmptTypeAssociation newComposition(IPolicyCmptType from, IPolicyCmptType to, boolean save) {
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
            from.getIpsSrcFile().save(null);
            to.getIpsSrcFile().save(null);
        }
        return master2detail;
    }

    /**
     * Creates a new association between 'from' and 'to' type. The role name singular is set to "a_"
     * + the target's unqualified name. The plural name is the singular name followed by an 's'. Min
     * cardinality is 1, max cardinality is '*'.
     * 
     * @throws IpsException if an error occurs while saving the files.
     */
    public IPolicyCmptTypeAssociation newAssociation(IPolicyCmptType from, IPolicyCmptType to) {
        IPolicyCmptTypeAssociation association = from.newPolicyCmptTypeAssociation();
        association.setAssociationType(AssociationType.ASSOCIATION);
        association.setTarget(to.getQualifiedName());
        association.setTargetRoleSingular("a_" + to.getUnqualifiedName());
        association.setTargetRolePlural("a_" + to.getUnqualifiedName() + "s");
        association.setMinCardinality(1);
        association.setMaxCardinality(Integer.MAX_VALUE);

        from.getIpsSrcFile().save(null);
        to.getIpsSrcFile().save(null);
        return association;
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
     * @throws IpsException if an error occurs while saving the files.
     */
    public IPolicyCmptTypeAssociation newComposition(IPolicyCmptType from, IPolicyCmptType to) {
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
     * @throws IpsException if an error occurs while saving the files.
     */
    public IProductCmptTypeAssociation newAggregation(IProductCmptType from, IProductCmptType to) {
        return newAggregation(from, to, true);
    }

    /**
     * Creates a new aggregation between 'from' and 'to' type. The role name singular is set to the
     * target's unqualified name. The plural name is the singular name followed by an 's'. Min
     * cardinality is 1, max cardinality is '*'.
     * 
     * @throws IpsException if an error occurs while saving the files.
     */
    public IProductCmptTypeAssociation newAggregation(IProductCmptType from, IProductCmptType to, boolean save) {
        IProductCmptTypeAssociation agg = from.newProductCmptTypeAssociation();
        agg.setAssociationType(AssociationType.AGGREGATION);
        agg.setTarget(to.getQualifiedName());
        agg.setTargetRoleSingular(to.getUnqualifiedName());
        agg.setTargetRolePlural(to.getUnqualifiedName() + "s");
        agg.setMinCardinality(1);
        agg.setMaxCardinality(Integer.MAX_VALUE);
        if (save) {
            from.getIpsSrcFile().save(null);
            to.getIpsSrcFile().save(null);
        }
        return agg;
    }

    /**
     * Creates a new product component that is based on the given product component type and has one
     * generation with it's valid from date set 2012-07-18, 00:00:00. The product component is
     * stored in the same package fragment root as the type. If the qualifiedName includes a package
     * name, the package is created if it does not already exists.
     */
    protected ProductCmpt newProductCmpt(IProductCmptType type, String qualifiedName) {
        IProductCmpt productCmpt = (IProductCmpt)newIpsObject(type.getIpsPackageFragment().getRoot(),
                IpsObjectType.PRODUCT_CMPT, qualifiedName);
        return setupProductCmpt(productCmpt, type);
    }

    /**
     * Creates a new product template that is based on the given product component type and has one
     * generation with it's valid from date set 2012-07-18, 00:00:00. The product template is stored
     * in the same package fragment root as the type. If the qualifiedName includes a package name,
     * the package is created if it does not already exists.
     */
    protected ProductCmpt newProductTemplate(IProductCmptType type, String qualifiedName) {
        IProductCmpt productCmpt = (IProductCmpt)newIpsObject(type.getIpsPackageFragment().getRoot(),
                IpsObjectType.PRODUCT_TEMPLATE, qualifiedName);
        return setupProductCmpt(productCmpt, type);
    }

    private ProductCmpt setupProductCmpt(IProductCmpt productCmpt, IProductCmptType type) {
        productCmpt.setProductCmptType(type.getQualifiedName());
        productCmpt.newGeneration(new GregorianCalendar(2012, 06, 18, 0, 0, 0));
        productCmpt.getIpsSrcFile().save(null);
        return (ProductCmpt)productCmpt;
    }

    /**
     * Creates a new product component in the indicated package fragment root. If the qualifiedName
     * includes a package name, the package is created if it does not already exists.
     */
    protected ProductCmpt newProductCmpt(IIpsPackageFragmentRoot root, String qualifiedName) {
        return (ProductCmpt)newIpsObject(root, IpsObjectType.PRODUCT_CMPT, qualifiedName);
    }

    /**
     * Creates a new product component in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected ProductCmpt newProductCmpt(IIpsProject project, String qualifiedName) {
        return (ProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, qualifiedName);
    }

    /**
     * Creates a new product template in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     * <p>
     * Note that this method does neither set the type nor creates any generations for the template.
     */
    protected ProductCmpt newProductTemplate(IIpsProject project, String qualifiedName) {
        ProductCmpt template = (ProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_TEMPLATE, qualifiedName);
        return template;
    }

    /**
     * Creates a new ipsobject in the indicated package fragment root. If the qualifiedName includes
     * a package name, the package is created if it does not already exists.
     */
    protected IIpsObject newIpsObject(IIpsPackageFragment pack, IpsObjectType type, String unqualifiedName) {
        IIpsSrcFile file = pack.createIpsFile(type, unqualifiedName, true, null);
        return file.getIpsObject();
    }

    /**
     * Creates a new table structure in the indicated package fragment root. If the qualifiedName
     * includes a package name, the package is created if it does not already exists.
     */
    protected TableStructure newTableStructure(final IIpsPackageFragmentRoot root, final String qualifiedName) {
        return (TableStructure)newIpsObject(root, IpsObjectType.TABLE_STRUCTURE, qualifiedName);
    }

    /**
     * Creates a new table structure in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected TableStructure newTableStructure(IIpsProject ipsProject, String qualifiedName) {
        return (TableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE, qualifiedName);
    }

    /**
     * Creates a new table content that is based on the given table structure and has one generation
     * with it's valid from date set to the current working date. The table content is stored in the
     * same package fragment root as the structure. If the qualifiedName includes a package name,
     * the package is created if it does not already exists.
     */
    protected TableContents newTableContents(ITableStructure ts0, String qualifiedName) {
        TableContents tableContents = (TableContents)newIpsObject(ts0.getIpsPackageFragment().getRoot(),
                IpsObjectType.TABLE_CONTENTS, qualifiedName);
        tableContents.setTableStructure(ts0.getQualifiedName());
        tableContents.getIpsSrcFile().save(null);
        return tableContents;
    }

    /**
     * Creates a new table content in the indicated package fragment root. If the qualifiedName
     * includes a package name, the package is created if it does not already exists.
     */
    protected TableContents newTableContents(IIpsPackageFragmentRoot root, String qualifiedName) {
        return (TableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS, qualifiedName);
    }

    /**
     * Creates a new table content in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected TableContents newTableContents(IIpsProject project, String qualifiedName) {
        return (TableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, qualifiedName);
    }

    /**
     * Creates a new test case type in the indicated package fragment root. If the qualifiedName
     * includes a package name, the package is created if it does not already exists.
     */
    protected TestCaseType newTestCaseType(final IIpsPackageFragmentRoot root, final String qualifiedName) {
        return (TestCaseType)newIpsObject(root, IpsObjectType.TEST_CASE_TYPE, qualifiedName);
    }

    /**
     * Creates a new test case type in the project's first package fragment root. If the
     * qualifiedName includes a package name, the package is created if it does not already exists.
     */
    protected TestCaseType newTestCaseType(IIpsProject ipsProject, String qualifiedName) {
        return (TestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, qualifiedName);
    }

    /**
     * Creates a new test case that is based on the given test case type. The test case is stored in
     * the same package fragment root as the structure. If the qualifiedName includes a package
     * name, the package is created if it does not already exists.
     */
    protected TestCase newTestCase(ITestCaseType tCase, String qualifiedName) {
        TestCase testCase = (TestCase)newIpsObject(tCase.getIpsPackageFragment().getRoot(), IpsObjectType.TEST_CASE,
                qualifiedName);
        testCase.setTestCaseType(tCase.getQualifiedName());
        testCase.getIpsSrcFile().save(null);
        return testCase;
    }

    /**
     * Creates a new test case in the indicated package fragment root. If the qualifiedName includes
     * a package name, the package is created if it does not already exists.
     */
    protected TestCase newTestCase(IIpsPackageFragmentRoot root, String qualifiedName) {
        return (TestCase)newIpsObject(root, IpsObjectType.TEST_CASE, qualifiedName);
    }

    /**
     * Creates a new test case in the project's first package fragment root. If the qualifiedName
     * includes a package name, the package is created if it does not already exists.
     */
    protected TestCase newTestCase(IIpsProject project, String qualifiedName) {
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
     * registered to the provided IpsProject as defined datatypes. The qualifiedName of a registered
     * datatype is the unqualified class name of the enum type class. The enum type class must have
     * the following methods:
     * <ol>
     * <li>public static final &lt;EnumValue &gt; getAllValues()</li>
     * <li>public String getId()</li>
     * <li>public String getName()</li>
     * <li>public boolean isValueOf(String)</li>
     * <li>public String toString(), must return the id of the enum value</li>
     * <li>public static final &lt;EnumValue &gt; valueOf(String), the id is provided to this method
     * and an enum values is supposed to be returned by this method</li>
     * </ol>
     */
    protected IDynamicEnumDatatype[] newDefinedEnumDatatype(IIpsProject project, Class<?>[] adaptedClass)
            throws IpsException, IOException {

        ArrayList<IDynamicValueDatatype> dataTypes = new ArrayList<>(adaptedClass.length);
        IIpsProjectProperties properties = project.getProperties();
        IDynamicValueDatatype[] definedDatatypes = properties.getDefinedValueDatatypes();
        for (IDynamicValueDatatype definedDatatype : definedDatatypes) {
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
        project.setProperties(properties);
        return returnValue;
    }

    private void createEnumClassFileInProjectOutputLocation(IIpsProject project, Class<?> adaptedClass)
            throws IOException {
        IPath location = PathMapping.toEclipsePath(project.getJavaProject().getResource().getLocation());
        if (location != null) {
            IPath outputLocation = location
                    .append(Path.fromOSString(project.getJavaProject().getOutputLocation().toString())
                            .removeFirstSegments(1));
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
                    is = adaptedClass.getClassLoader()
                            .getResourceAsStream(adaptedClass.getName().replace('.', '/') + ".class");
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
    }

    /**
     * Copies the given project properties file and the given classes to the given project. The
     * classes are added to the classpath of the project.
     * 
     */
    protected void configureProject(IIpsProject project, String ipsProjectFileName, Class<?>[] dependencies) {
        java.nio.file.Path outputPath = project.getJavaProject().getOutputLocation();
        AFolder output = project.getProject().getFolder(outputPath);
        for (Class<?> dependencie : dependencies) {
            String name = dependencie.getName() + ".class";
            output.getFile(name).create(dependencie.getResourceAsStream(name), null);
        }
        AFile ipsproject = project.getProject().getFile(".ipsproject");
        if (ipsproject.exists()) {
            ipsproject.setContents(getClass().getResourceAsStream(ipsProjectFileName), false, null);
        } else {
            ipsproject.create(getClass().getResourceAsStream(ipsProjectFileName), null);
        }
    }

    /**
     * Sets the builderset as the one to be used by the indicated project. This method modifies the
     * project's properties and also registers the builderset in the model.
     */
    protected void setArtefactBuildset(IIpsProject project, IIpsArtefactBuilderSet builderset) {
        IIpsProjectProperties props = project.getProperties();
        props.setBuilderSetId(builderset.getId());
        project.setProperties(props);
        IpsModel model = ((IpsModel)project.getIpsModel());
        model.setIpsArtefactBuilderSetInfos(
                new IIpsArtefactBuilderSetInfo[] { new TestArtefactBuilderSetInfo(builderset) });
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

    protected IIpsModel getIpsModel() {
        return IIpsModel.get();
    }

    protected void testPropertyAccessReadWrite(Class<?> clazz, String propertyName) {
        testPropertyAccessReadOnly(clazz, propertyName);
        testPropertyAccessWriteOnly(clazz, propertyName);
    }

    protected void testPropertyAccessReadWrite(Class<?> clazz,
            String propertyName,
            Object object,
            Object testValueToSet) {
        testPropertyAccessReadWrite(clazz, propertyName);
        PropertyDescriptor prop = BeanUtil.getPropertyDescriptor(clazz, propertyName);
        boolean writeOk = false;
        try {
            prop.getWriteMethod().invoke(object, testValueToSet);
            writeOk = true;
            Object retValue = prop.getReadMethod().invoke(object);
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
        assertNotNull("Class " + clazz.getName() + " hasn't got a read method for property " + propertyName,
                readMethod);
        assertEquals(0, readMethod.getParameterTypes().length);
    }

    /**
     * Create the sort order file for {@link IIpsPackageFragment}s from a List of Strings.
     * 
     * @param folder Handle to the parent folder (IpsPackageFragment or IpsPackageFragmentRoot)
     * @param strings IpsPackageFragment names in sort order followed by IpsSrcFile names in order.
     */
    protected AFile createSortOrderFile(AFolder folder, String... strings) throws IOException {

        AFile file = folder.getFile(IIpsPackageFragment.SORT_ORDER_FILE_NAME);

        String print = "";
        String lineSeparator = System.lineSeparator();

        print = print.concat("# comment" + lineSeparator);

        for (String element : strings) {
            print = print.concat(element + lineSeparator);
        }

        byte[] bytes = print.getBytes(StringUtil.CHARSET_UTF8);

        ByteArrayInputStream is = new ByteArrayInputStream(bytes);

        if (file.exists()) {
            file.setContents(is, true, null);
        } else {
            file.create(is, null);
        }

        return file;
    }

    /**
     * Reads the first line of a file's contents from the given {@link InputStream}.
     */
    protected String getFirstLine(InputStream aStream) throws IOException {
        assertNotNull(aStream);
        BufferedReader aReader = new BufferedReader(new InputStreamReader(aStream));
        String aContent = aReader.readLine();
        aReader.close();

        return aContent;
    }

    /**
     * Reads the file's contents from the given {@link IIpsSrcFile}.
     */
    protected String getFileContent(IIpsSrcFile ipsSrcFile) throws IOException {
        assertNotNull(ipsSrcFile);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(ipsSrcFile.getContentFromEnclosingResource()))) {
            return reader.lines()
                    .collect(Collectors.joining("\n"));
        }
    }

    /**
     * Reads the file's contents from the given {@link IIpsSrcFile}.
     */
    protected String getFileContent(AFile file) throws IOException {
        assertNotNull(file);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getContents()))) {
            return reader.lines()
                    .collect(Collectors.joining("\n"));
        }
    }

    /**
     * Creates a file ("file.txt") with the given String as content and places it in the given
     * folder.
     */
    protected void createFileWithContent(AFolder parentFolder, String fileName, String content) {
        AFile file = parentFolder.getFile(fileName);
        if (!file.exists()) {
            file.create(new ByteArrayInputStream(content.getBytes()), null);
        }
    }

    /**
     * Prints the validation result of the given {@link IIpsObjectPartContainer} to the console if
     * the severity is at least at the warning level.
     */
    protected final void printValidationResult(IIpsObjectPartContainer ipsObjectPartContainer) {
        MessageList validationResult = ipsObjectPartContainer.validate(ipsObjectPartContainer.getIpsProject());
        if (validationResult.getSeverity() == Message.WARNING || validationResult.getSeverity() == Message.ERROR) {
            System.out.println(validationResult.getFirstMessage(Message.ERROR));
        }
    }

    /**
     * Clears the output folders of the given {@link IIpsProject} (to avoid code merging problems)
     * and performs a full build.
     */
    protected final void performFullBuild(IIpsProject ipsProject) {
        // To avoid code merging problems
        try {
            clearOutputFolders(ipsProject);
            ((IProject)ipsProject.getProject().unwrap()).build(IncrementalProjectBuilder.FULL_BUILD, null);
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

    private void clearOutputFolders(IIpsProject ipsProject) {
        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        if (ipsObjectPath.isOutputDefinedPerSrcFolder()) {
            for (IIpsSrcFolderEntry srcFolderEntry : ipsObjectPath.getSourceFolderEntries()) {
                AFolder outputFolderDerived = srcFolderEntry.getOutputFolderForDerivedJavaFiles();
                AFolder outputFolderMergable = srcFolderEntry.getOutputFolderForMergableJavaFiles();
                clearFolder(outputFolderDerived);
                clearFolder(outputFolderMergable);
            }
        } else {
            AFolder outputFolderDerived = ipsObjectPath.getOutputFolderForDerivedSources();
            AFolder outputFolderMergable = ipsObjectPath.getOutputFolderForMergableSources();
            clearFolder(outputFolderDerived);
            clearFolder(outputFolderMergable);
        }
    }

    private void clearFolder(AFolder folder) {
        for (AResource resource : folder) {
            resource.delete(null);
        }
    }

    protected final void assertOneValidationMessage(MessageList list,
            String code,
            Object invalidObject,
            String property,
            Severity severity) {

        assertThat(list, hasSize(1));
        Message message = list.getFirstMessage(severity);
        assertThat(message.getCode(), is(code));
        assertThat(message, hasInvalidObject(invalidObject, property));
    }

    protected final void assertPropertyChangedEvent(IIpsObjectPart part,
            String property,
            Object oldValue,
            Object newValue) {

        assertContentChangedEvent(part.getIpsSrcFile(), ContentChangeEvent.TYPE_PROPERTY_CHANGED);
        assertEquals(part, getLastContentChangeEvent().getPart());
        assertEquals(property, getLastContentChangeEvent().getFirstPropertyChangeEvent().getPropertyName());
        assertEquals(oldValue, getLastContentChangeEvent().getFirstPropertyChangeEvent().getOldValue());
        assertEquals(newValue, getLastContentChangeEvent().getFirstPropertyChangeEvent().getNewValue());
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

    protected final void setPartId(IIpsObjectPart part, String id) throws NoSuchFieldException, IllegalAccessException {
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

    /**
     * Gets the {@link IIpsProject}'s properties, applies the given modifier and sets the changed
     * properties.
     */
    protected void setProjectProperty(IIpsProject ipsProject, Consumer<IIpsProjectProperties> propertiesModifier) {
        IIpsProjectProperties properties = ipsProject.getProperties();
        propertiesModifier.accept(properties);
        ipsProject.setProperties(properties);
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

        public TestArtefactBuilder() {
            super(new TestIpsArtefactBuilderSet());
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void build(IIpsSrcFile ipsSrcFile) {

        }

        @Override
        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
            return false;
        }

        @Override
        public void delete(IIpsSrcFile ipsSrcFile) {

        }

        @Override
        public boolean isBuildingInternalArtifacts() {
            return false;
        }

    }

}
