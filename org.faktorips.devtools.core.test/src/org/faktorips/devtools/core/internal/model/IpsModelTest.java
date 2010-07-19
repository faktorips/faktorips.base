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

package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IModificationStatusChangeListener;
import org.faktorips.devtools.core.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentArbitrarySortDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.util.ProjectUtil;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;

public class IpsModelTest extends AbstractIpsPluginTest {

    private IpsModel model;

    // JavaProjects for testGetNonIpsResources()
    private IJavaProject javaProject = null;
    private IJavaProject javaProject2 = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        model = (IpsModel)IpsPlugin.getDefault().getIpsModel();
    }

    public void testCreateIpsProject() throws CoreException {
        IProject project = newPlatformProject("TestProject");
        IJavaProject javaProject = addJavaCapabilities(project);
        IIpsProject ipsProject = model.createIpsProject(javaProject);
        assertNotNull(ipsProject);
        assertEquals(0, ipsProject.findDatatypes(true, false).length);
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        assertNotNull(path);
        assertEquals(0, path.getEntries().length);
    }

    public void testGetIpsProjects() throws CoreException {
        newPlatformProject("TestProject");
        IIpsProject[] ipsProjects = model.getIpsProjects();
        assertEquals(0, ipsProjects.length);

        IIpsProject project = newIpsProject("TestPdProject");
        ipsProjects = model.getIpsProjects();
        assertEquals(1, ipsProjects.length);
        assertEquals("TestPdProject", ipsProjects[0].getName());

        project.getProject().close(null);
        super.newIpsProject("TestProject2");
        ipsProjects = model.getIpsProjects();
        assertEquals(1, ipsProjects.length);
        assertEquals("TestProject2", ipsProjects[0].getName());
    }

    public void testGetIpsModelProjects() throws CoreException {
        IIpsProject modelProject = newIpsProject("ModelProject");
        IIpsProjectProperties properties = modelProject.getProperties();
        properties.setModelProject(true);
        properties.setProductDefinitionProject(false);
        modelProject.setProperties(properties);

        IIpsProject productDefinitionProject = newIpsProject("ProductDefinitionProject");
        properties = productDefinitionProject.getProperties();
        properties.setModelProject(false);
        properties.setProductDefinitionProject(true);
        productDefinitionProject.setProperties(properties);

        IIpsProject modelAndProductDefinitionProject = newIpsProject("ModelAndProductDefinitionProject");
        properties = modelAndProductDefinitionProject.getProperties();
        properties.setModelProject(true);
        properties.setProductDefinitionProject(true);
        modelAndProductDefinitionProject.setProperties(properties);

        IIpsProject[] modelProjects = getIpsModel().getIpsModelProjects();
        assertEquals(2, modelProjects.length);
        assertEquals(modelAndProductDefinitionProject, modelProjects[0]);
        assertEquals(modelProject, modelProjects[1]);
    }

    public void testGetIpsProductDefinitionProjects() throws CoreException {
        IIpsProject modelProject = newIpsProject("ModelProject");
        IIpsProjectProperties properties = modelProject.getProperties();
        properties.setModelProject(true);
        properties.setProductDefinitionProject(false);
        modelProject.setProperties(properties);

        IIpsProject productDefinitionProject = newIpsProject("ProductDefinitionProject");
        properties = productDefinitionProject.getProperties();
        properties.setModelProject(false);
        properties.setProductDefinitionProject(true);
        productDefinitionProject.setProperties(properties);

        IIpsProject modelAndProductDefinitionProject = newIpsProject("ModelAndProductDefinitionProject");
        properties = modelAndProductDefinitionProject.getProperties();
        properties.setModelProject(true);
        properties.setProductDefinitionProject(true);
        modelAndProductDefinitionProject.setProperties(properties);

        IIpsProject[] productDefinitionProjects = getIpsModel().getIpsProductDefinitionProjects();
        assertEquals(2, productDefinitionProjects.length);
        assertEquals(modelAndProductDefinitionProject, productDefinitionProjects[0]);
        assertEquals(productDefinitionProject, productDefinitionProjects[1]);
    }

    public void testGetIpsElement_ExistingIpsProject() throws Exception {
        IIpsProject ipsProject = newIpsProject("TestProject");

        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        assertEquals(model, model.getIpsElement(root));

        IProject project = root.getProject("TestProject");
        assertEquals(ipsProject, model.getIpsElement(project));

        IIpsPackageFragmentRoot packRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        IFolder rootFolder = (IFolder)packRoot.getCorrespondingResource();
        assertEquals(packRoot, model.getIpsElement(rootFolder));

        IFolder folderA = rootFolder.getFolder("a");
        IIpsPackageFragment packA = packRoot.getIpsPackageFragment("a");
        assertEquals(packA, model.getIpsElement(folderA));

        IFolder folderB = folderA.getFolder("b");
        IIpsPackageFragment packB = packRoot.getIpsPackageFragment("a.b");
        assertEquals(packB, model.getIpsElement(folderB));

        String filename = IpsObjectType.POLICY_CMPT_TYPE.getFileName("Policy");
        IFile file = folderB.getFile(filename);
        IIpsSrcFile srcFile = packB.getIpsSrcFile(filename);
        assertEquals(srcFile, model.getIpsElement(file));

        IFile textFile = folderB.getFile("Textfile.txt");
        assertNull(model.getIpsElement(textFile));
    }

    public void testGetIpsElement_NotExistingIpsProject() throws Exception {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject("TestProject");
        IIpsProject ipsProject = model.getIpsProject("TestProject");
        assertEquals(ipsProject, model.getIpsElement(project));

        IFolder rootFolder = project.getFolder("model");
        IIpsPackageFragmentRoot packRoot = ipsProject.getIpsPackageFragmentRoot("productdef");
        assertNotNull(packRoot);
        assertNull(model.getIpsElement(rootFolder));

        IFolder folderA = rootFolder.getFolder("a");
        assertNull(model.getIpsElement(folderA));

        String filename = IpsObjectType.POLICY_CMPT_TYPE.getFileName("Policy");
        IFile file = folderA.getFile(filename);
        assertNull(model.getIpsElement(file));

        IFile textFile = folderA.getFile("Textfile.txt");
        assertNull(model.getIpsElement(textFile));
    }

    public void testFindIpsElement() throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        assertEquals(model, model.getIpsElement(root));

        IProject project = root.getProject("TestProject");
        IIpsProject pdProject = model.getIpsProject("TestProject");
        assertNull(model.findIpsElement(project));

        pdProject = this.newIpsProject("TestIpsProject");
        project = pdProject.getProject();
        assertEquals(pdProject, model.findIpsElement(project));

        IFolder rootFolder = project.getFolder("productdef");
        IIpsPackageFragmentRoot pdRootFolder = pdProject.getIpsPackageFragmentRoot("productdef");
        assertEquals(pdRootFolder, model.findIpsElement(rootFolder));

        IFolder folderA = rootFolder.getFolder("a");
        IIpsPackageFragment pdFolderA = pdRootFolder.getIpsPackageFragment("a");
        assertNull(model.findIpsElement(folderA));
        folderA.create(true, true, null);
        assertEquals(pdFolderA, model.findIpsElement(folderA));
    }

    public void testChangeListenerSupport() throws CoreException {
        IIpsProject project = newIpsProject();
        IIpsSrcFile file = newPolicyCmptType(project, "TestPolicy").getIpsSrcFile();
        TestContentsChangeListener listener = new TestContentsChangeListener();

        model.removeChangeListener(listener); // test for npe

        model.addChangeListener(listener);
        ContentChangeEvent event = ContentChangeEvent.newWholeContentChangedEvent(file);
        assertNull(listener.lastEvent);
        model.notifyChangeListeners(event);
        assertEquals(event, listener.lastEvent);

        model.notifyChangeListeners(event);
        assertEquals(event, listener.lastEvent);

        model.removeChangeListener(listener);
        listener.lastEvent = null;
        model.notifyChangeListeners(event);
        assertNull(listener.lastEvent);
    }

    public void testModifcationStatusChangeListenerSupport() throws CoreException {
        IIpsProject project = newIpsProject();
        IIpsSrcFile file = newPolicyCmptType(project, "TestPolicy").getIpsSrcFile();
        TestModStatusListener listener = new TestModStatusListener();

        model.removeModificationStatusChangeListener(listener); // test for npe

        model.addModifcationStatusChangeListener(listener);
        ModificationStatusChangedEvent event = new ModificationStatusChangedEvent(file);
        assertNull(listener.lastEvent);
        model.notifyModificationStatusChangeListener(event);
        assertEquals(event, listener.lastEvent);

        model.notifyModificationStatusChangeListener(event);
        assertEquals(event, listener.lastEvent);

        model.removeModificationStatusChangeListener(listener);
        listener.lastEvent = null;
        model.notifyModificationStatusChangeListener(event);
        assertNull(listener.lastEvent);
    }

    public void testResourceChanged() throws IOException, CoreException {
        IIpsProject ipsProject = this.newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment pack = root.createPackageFragment("pack", true, null);
        IIpsSrcFile sourceFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        IPolicyCmptType object = (PolicyCmptType)sourceFile.getIpsObject();
        IPolicyCmptTypeAttribute attribute = object.newPolicyCmptTypeAttribute();
        attribute.setDescription("blabla");
        sourceFile.save(true, null);

        IFile file = sourceFile.getCorrespondingFile();
        assertTrue(file.exists());
        String encoding = ipsProject.getXmlFileCharset();
        String contents = StringUtil.readFromInputStream(file.getContents(), encoding);
        contents = StringUtils.replace(contents, "blabla", "something serious");
        ByteArrayInputStream is = new ByteArrayInputStream(contents.getBytes(encoding));
        file.setContents(is, true, false, null);

        object = (IPolicyCmptType)sourceFile.getIpsObject();
        attribute = object.getPolicyCmptTypeAttributes()[0];
        assertEquals("something serious", attribute.getDescription());
    }

    public void testGetIpsObjectExtensionProperties() {
        Class<?> extendedClass = model.getClass();
        ExtensionPropertyDefinition property = new StringExtensionPropertyDefinition();
        property.setPropertyId("prop1");
        property.setExtendedType(extendedClass);
        model.addIpsObjectExtensionProperty(property);
        List<IExtensionPropertyDefinition> props = Arrays.asList(model.getExtensionPropertyDefinitions(extendedClass,
                false));
        assertEquals(1, props.size());
        assertSame(property, props.get(0));
        props = Arrays.asList(model.getExtensionPropertyDefinitions(extendedClass, true));
        assertEquals(1, props.size());
        assertSame(property, props.get(0));
        assertEquals(0, model.getExtensionPropertyDefinitions(String.class, true).length);

        // test properties defined on one of the supertypes
        ExtensionPropertyDefinition property2 = new StringExtensionPropertyDefinition();
        property2.setPropertyId("prop2");
        property2.setExtendedType(extendedClass.getSuperclass());
        model.addIpsObjectExtensionProperty(property2);
        ExtensionPropertyDefinition property3 = new StringExtensionPropertyDefinition();
        property3.setPropertyId("prop3");
        property3.setExtendedType(extendedClass.getSuperclass().getSuperclass());
        model.addIpsObjectExtensionProperty(property3);

        props = Arrays.asList(model.getExtensionPropertyDefinitions(extendedClass, true));
        assertEquals(3, props.size());

        assertTrue(props.contains(property));
        assertTrue(props.contains(property2));
        assertTrue(props.contains(property3));
        props = Arrays.asList(model.getExtensionPropertyDefinitions(extendedClass, false));
        assertEquals(1, props.size());

        // test properties defined in one of the interfaces
        ExtensionPropertyDefinition property4 = new StringExtensionPropertyDefinition();
        property4.setPropertyId("prop4");
        property4.setExtendedType(IIpsModel.class);
        model.addIpsObjectExtensionProperty(property4);
        props = Arrays.asList(model.getExtensionPropertyDefinitions(extendedClass, true));
        assertEquals(4, props.size());
        assertTrue(props.contains(property)); // first the type's properties
        assertTrue(props.contains(property2)); // then the supertype's properties
        assertTrue(props.contains(property3)); // then the supertype's supertype's properties
        assertTrue(props.contains(property4)); // the the type's interface's properties
        props = Arrays.asList(model.getExtensionPropertyDefinitions(extendedClass, false));
        assertEquals(1, props.size());
    }

    public void testGetIpsObjectExtensionProperty() {
        ExtensionPropertyDefinition property = new StringExtensionPropertyDefinition();
        property.setPropertyId("prop1");
        property.setExtendedType(this.getClass());
        model.addIpsObjectExtensionProperty(property);

        assertEquals(property, model.getExtensionPropertyDefinition(getClass(), "prop1", false));

        // test properties defined on one of the supertypes
        ExtensionPropertyDefinition property2 = new StringExtensionPropertyDefinition();
        property2.setPropertyId("prop2");
        property2.setExtendedType(this.getClass().getSuperclass());
        model.addIpsObjectExtensionProperty(property2);
        ExtensionPropertyDefinition property3 = new StringExtensionPropertyDefinition();
        property3.setPropertyId("prop3");
        property3.setExtendedType(this.getClass().getSuperclass().getSuperclass());
        model.addIpsObjectExtensionProperty(property3);

        assertEquals(property2, model.getExtensionPropertyDefinition(getClass(), "prop2", true));
        assertEquals(property3, model.getExtensionPropertyDefinition(getClass(), "prop3", true));
        assertNull(model.getExtensionPropertyDefinition(getClass(), "prop2", false));
        assertNull(model.getExtensionPropertyDefinition(getClass(), "prop3", false));

        assertNull(model.getExtensionPropertyDefinition(getClass(), "unknownProp", true));
        assertNull(model.getExtensionPropertyDefinition(String.class, "prop2", false));
    }

    public void testGetPredefinedValueDatatypes() {
        ValueDatatype[] datatypes = model.getPredefinedValueDatatypes();
        assertTrue(datatypes.length > 0);
    }

    public void testIsPredefinedValueDatatype() {
        ValueDatatype[] datatypes = model.getPredefinedValueDatatypes();
        assertTrue(model.isPredefinedValueDatatype(datatypes[0].getQualifiedName()));
        assertFalse(model.isPredefinedValueDatatype("unknownDatatype"));
        assertFalse(model.isPredefinedValueDatatype(null));
    }

    public void testGetChangesInTimeNamingConvention() {
        IChangesOverTimeNamingConvention convention = model
                .getChangesOverTimeNamingConvention(IChangesOverTimeNamingConvention.VAA);
        assertNotNull(convention);
        assertEquals(IChangesOverTimeNamingConvention.VAA, convention.getId());

        convention = model.getChangesOverTimeNamingConvention(IChangesOverTimeNamingConvention.PM);
        assertNotNull(convention);
        assertEquals(IChangesOverTimeNamingConvention.PM, convention.getId());

        convention = model.getChangesOverTimeNamingConvention("unknown");
        assertNotNull(convention);
        assertEquals(IChangesOverTimeNamingConvention.VAA, convention.getId());
    }

    public void testGetNonIpsResources() throws CoreException {
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                IProject project = newPlatformProject("TestJavaProject");
                javaProject = addJavaCapabilities(project);
                IProject project2 = newPlatformProject("TestJavaProject2");
                javaProject2 = addJavaCapabilities(project2);
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

        newIpsProject("TestProject");

        assertNotNull(javaProject);
        assertNotNull(javaProject2);

        Object[] nonIpsResources = model.getNonIpsProjects();
        assertEquals(2, nonIpsResources.length);
        // compare handles (IProject)
        assertEquals(javaProject.getProject(), nonIpsResources[0]);
        assertEquals(javaProject2.getProject(), nonIpsResources[1]);
    }

    public void testClearValidationCache() throws CoreException {
        IIpsProject project = super.newIpsProject();
        IPolicyCmptType pcType = super.newPolicyCmptType(project, "TestedType");
        model.getValidationResultCache().putResult(pcType, new MessageList());

        assertNotNull(model.getValidationResultCache().getResult(pcType));

        model.clearValidationCache();

        assertNull(model.getValidationResultCache().getResult(pcType));
    }

    public void testRunAndQueueChangeEvents() throws CoreException {
        IIpsProject project = newIpsProject();
        final IPolicyCmptType typeA = newPolicyCmptType(project, "A");
        final IPolicyCmptType typeB = newPolicyCmptType(project, "B");

        IWorkspaceRunnable action = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                typeA.setDescription("blabla");
                typeA.setSupertype(typeB.getQualifiedName());
                typeA.getIpsSrcFile().save(true, monitor);
                typeB.setDescription("blabla");
                typeB.getIpsSrcFile().save(true, monitor);
                typeA.setDescription("New blabla");
                typeA.getIpsSrcFile().save(true, monitor);
            }

        };

        TestContentsChangeListener listener = new TestContentsChangeListener();
        model.addChangeListener(listener);
        TestModStatusListener modifyListener = new TestModStatusListener();
        model.addModifcationStatusChangeListener(modifyListener);

        model.runAndQueueChangeEvents(action, null);

        assertEquals(2, listener.changedFiles.size());
        assertChangedFileIn(typeA.getIpsSrcFile(), listener.changedFiles);
        assertChangedFileIn(typeB.getIpsSrcFile(), listener.changedFiles);

        assertEquals(2, modifyListener.modifiedFiles.size());
        assertChangedFileIn(typeA.getIpsSrcFile(), modifyListener.modifiedFiles);
        assertChangedFileIn(typeB.getIpsSrcFile(), modifyListener.modifiedFiles);

        listener.changedFiles.clear();
        modifyListener.modifiedFiles.clear();

        typeA.setDescription("blublu");
        typeA.getIpsSrcFile().save(true, null);

        assertEquals(1, listener.changedFiles.size());
        assertEquals(typeA.getIpsSrcFile(), listener.changedFiles.get(0));

        // two entries are expected for modifiedFiles. Both are for the same file. First entry is
        // expected due to the call to the setDescription() method, second due to the call to the
        // save() of the ips source file
        assertEquals(2, modifyListener.modifiedFiles.size());
        assertChangedFileIn(typeA.getIpsSrcFile(), modifyListener.modifiedFiles);
    }

    private void assertChangedFileIn(IIpsSrcFile ipsSrcFile, List<IIpsSrcFile> changedFiles) {
        for (IIpsSrcFile currIpsSrcFile : changedFiles) {
            if (ipsSrcFile == currIpsSrcFile) {
                return;
            }
        }
        fail("expected IpsSrcFile: " + ipsSrcFile.getName() + " not in List!");
    }

    private static class TestContentsChangeListener implements ContentsChangeListener {

        List<IIpsSrcFile> changedFiles = new ArrayList<IIpsSrcFile>();
        ContentChangeEvent lastEvent;

        @Override
        public void contentsChanged(ContentChangeEvent event) {
            changedFiles.add(event.getIpsSrcFile());
            lastEvent = event;
        }

    }

    private static class TestModStatusListener implements IModificationStatusChangeListener {

        List<IIpsSrcFile> modifiedFiles = new ArrayList<IIpsSrcFile>();
        ModificationStatusChangedEvent lastEvent;

        @Override
        public void modificationStatusHasChanged(ModificationStatusChangedEvent event) {
            lastEvent = event;
            modifiedFiles.add(event.getIpsSrcFile());
        }

    }

    public void testGetSortDefinition() throws CoreException, IOException {
        IIpsProject project = newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];

        root.createPackageFragment("products.hausrat", true, null);
        IIpsPackageFragment unfall = root.createPackageFragment("products.unfall", true, null);
        IpsPackageFragment leistungsarten = (IpsPackageFragment)root.createPackageFragment(
                "products.unfall.leistungsarten", true, null);
        root.createPackageFragment("products.unfall.leistungsartgruppen", true, null);
        IpsPackageFragment kranken = (IpsPackageFragment)root.createPackageFragment("products.kranken", true, null);

        IIpsPackageFragment products = root.getIpsPackageFragment("products");

        List<String> list = new ArrayList<String>();
        list.add("kranken");
        list.add("unfall");
        list.add("hausrat");
        createPackageOrderFile((IFolder)products.getCorrespondingResource(), list);
        list.clear();

        list.add("leistungsarten");
        list.add("leistungsartgruppen");
        createPackageOrderFile((IFolder)unfall.getCorrespondingResource(), list);
        list.clear();

        // test default sort definition
        IIpsPackageFragmentSortDefinition sortDef = model.getSortDefinition(products);
        assertNotNull(sortDef);
        assertEquals("", sortDef.toPersistenceContent());
        assertFalse(sortDef instanceof IIpsPackageFragmentArbitrarySortDefinition);

        // test sort definition 1. load
        sortDef = model.getSortDefinition(unfall);
        assertNotNull(sortDef);
        assertTrue(sortDef instanceof IIpsPackageFragmentArbitrarySortDefinition);

        // test later

        IFile file = leistungsarten.getSortOrderFile();
        file.delete(true, null);

        IIpsPackageFragmentSortDefinition sortDefKranken = model.getSortDefinition(kranken);
        assertNotNull(sortDefKranken);
        assertTrue(sortDefKranken instanceof IIpsPackageFragmentArbitrarySortDefinition);
        String[] fragments = ((IIpsPackageFragmentArbitrarySortDefinition)sortDefKranken).getSegmentNames();

        assertEquals(3, fragments.length);
        assertEquals("kranken", fragments[0]);
        assertEquals("unfall", fragments[1]);
        assertEquals("hausrat", fragments[2]);

        // test sort definition 2. load -> cached
        IIpsPackageFragmentSortDefinition sortDefKranken2 = model.getSortDefinition(kranken);
        assertEquals(sortDefKranken, sortDefKranken2);

        // test later
        file = kranken.getSortOrderFile();
        file.touch(null);

        // test deleted sort order
        sortDef = model.getSortDefinition(leistungsarten);
        assertNotNull(sortDef);
        assertFalse(sortDef instanceof IIpsPackageFragmentArbitrarySortDefinition);
        assertEquals("", sortDef.toPersistenceContent());

        // test sort definition 2. load -> cached & manipulated
        IIpsPackageFragmentSortDefinition sortDefKranken3 = model.getSortDefinition(kranken);
        assertNotNull(sortDefKranken3);
        assertNotSame(sortDefKranken3, sortDefKranken);
        fragments = ((IIpsPackageFragmentArbitrarySortDefinition)sortDefKranken).getSegmentNames();

        assertEquals(3, fragments.length);
        assertEquals("kranken", fragments[0]);
        assertEquals("unfall", fragments[1]);
        assertEquals("hausrat", fragments[2]);
    }

    public void testClearIpsSrcFileContentsCacheWhenFileDeleted() throws Exception {
        IIpsProject project = newIpsProject("TestProject");
        IPolicyCmptType pcType = newPolicyCmptTypeWithoutProductCmptType(project, "A");
        pcType.getIpsSrcFile().save(true, null);

        pcType = (IPolicyCmptType)project.findIpsObject(pcType.getQualifiedNameType());

        boolean status = false;
        int counter = 0;
        while (!status || counter > 200) {
            status = model.isCached(pcType.getIpsSrcFile());
            counter++;
        }
        assertTrue("The IpsSrcFile " + pcType.getIpsSrcFile() + " is not in the IpsModel cache as expected", status);

        pcType.getIpsSrcFile().getEnclosingResource().delete(true, null);

        status = false;
        counter = 0;
        while (!status || counter > 200) {
            status = !model.isCached(pcType.getIpsSrcFile());
            counter++;
        }

        assertTrue("The IpsSrcFile " + pcType.getIpsSrcFile()
                + " is in the IpsModel cache which is not expected since the resource changed listener "
                + "should be triggered by know and have the cache cleared.", status);
    }

    public void testSearchReferencingTestCases() throws CoreException {
        IIpsProject baseProject = newIpsProject("base");
        IIpsProject ipsProject = newIpsProject("next");

        ITestCase test1 = newTestCase(baseProject, "Test1");
        ITestCase test2 = newTestCase(ipsProject, "Test2");
        IProductCmpt cmpt = newProductCmpt(baseProject, "Cmpt");

        test1.newTestPolicyCmpt().setProductCmpt(cmpt.getQualifiedName());
        test2.newTestPolicyCmpt().setProductCmpt(cmpt.getQualifiedName());

        IIpsModel model = IpsPlugin.getDefault().getIpsModel();

        List<ITestCase> result = model.searchReferencingTestCases(cmpt);
        assertEquals(1, result.size());
        assertEquals(test1, result.get(0));

        ProjectUtil.addProjectReference(ipsProject, baseProject);

        result = model.searchReferencingTestCases(cmpt);
        assertEquals(2, result.size());
        assertTrue(result.contains(test1));
        assertTrue(result.contains(test2));
    }

}
