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

package org.faktorips.devtools.core.internal.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentArbitrarySortDefinition;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IModificationStatusChangeListener;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;


/**
 *
 */
public class IpsModelTest extends AbstractIpsPluginTest {

    private IpsModel model;

    // JavaProjects for testGetNonIpsResources()
    private IJavaProject javaProject= null;
    private IJavaProject javaProject2= null;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        model = (IpsModel)IpsPlugin.getDefault().getIpsModel();
    }

    /*
     * @see PluginTest#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateIpsProject() throws CoreException {
    	IProject project = newPlatformProject("TestProject");
    	IJavaProject javaProject = addJavaCapabilities(project);
    	IIpsProject ipsProject = model.createIpsProject(javaProject);
    	assertNotNull(ipsProject);
    	assertEquals(0, ipsProject.getValueDatatypes(false).length);
    	IIpsObjectPath path = ipsProject.getIpsObjectPath();
    	assertNotNull(path);
    	assertEquals(0, path.getEntries().length);
    }

    public void testGetIpsProjects() throws CoreException {
        super.newPlatformProject("TestProject");
        IIpsProject[] ipsProjects = model.getIpsProjects();
        assertEquals(0, ipsProjects.length);

        IIpsProject project = super.newIpsProject("TestPdProject");
        ipsProjects = model.getIpsProjects();
        assertEquals(1, ipsProjects.length);
        assertEquals("TestPdProject", ipsProjects[0].getName());

        project.getProject().close(null);
        super.newIpsProject("TestProject2");
        ipsProjects = model.getIpsProjects();
        assertEquals(1, ipsProjects.length);
        assertEquals("TestProject2", ipsProjects[0].getName());
    }

    public void testGetIpsElement() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        assertEquals(model, model.getIpsElement(root));

        IProject project = root.getProject("TestProject");
        IIpsProject pdProject = model.getIpsProject("TestProject");
        assertEquals(pdProject, model.getIpsElement(project));

        IFolder rootFolder = project.getFolder("productdef");
        IIpsPackageFragmentRoot pdRootFolder = pdProject.getIpsPackageFragmentRoot("productdef");
        assertEquals(pdRootFolder, model.getIpsElement(rootFolder));

        IFolder folderA = rootFolder.getFolder("a");
        IIpsPackageFragment pdFolderA = pdRootFolder.getIpsPackageFragment("a");
        assertEquals(pdFolderA, model.getIpsElement(folderA));

        IFolder folderB = folderA.getFolder("b");
        IIpsPackageFragment pdFolderB = pdRootFolder.getIpsPackageFragment("a.b");
        assertEquals(pdFolderB, model.getIpsElement(folderB));

        String filename = IpsObjectType.POLICY_CMPT_TYPE.getFileName("test");
        IFile file = folderB.getFile(filename);
        IIpsSrcFile srcFile = pdFolderB.getIpsSrcFile(filename);
        assertEquals(srcFile, model.getIpsElement(file));
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
        IAttribute attribute = object.newAttribute();
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
        attribute = object.getAttributes()[0];
        assertEquals("something serious", attribute.getDescription());


    }

    public void testGetIpsObjectExtensionProperties() {
        Class extendedClass = model.getClass();
        ExtensionPropertyDefinition property = new StringExtensionPropertyDefinition();
        property.setPropertyId("prop1");
        property.setExtendedType(extendedClass);
        model.addIpsObjectExtensionProperty(property);
        IExtensionPropertyDefinition[] props = model.getExtensionPropertyDefinitions(extendedClass, false);
        assertEquals(1, props.length);
        assertSame(property, props[0]);
        props = model.getExtensionPropertyDefinitions(extendedClass, true);
        assertEquals(1, props.length);
        assertSame(property, props[0]);
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

        props = model.getExtensionPropertyDefinitions(extendedClass, true);
        assertEquals(3, props.length);
        assertSame(property, props[0]);
        assertSame(property2, props[1]);
        assertSame(property3, props[2]);
        props = model.getExtensionPropertyDefinitions(extendedClass, false);
        assertEquals(1, props.length);

        // test properties defined in one of the interfaces
        ExtensionPropertyDefinition property4 = new StringExtensionPropertyDefinition();
        property4.setPropertyId("prop4");
        property4.setExtendedType(IIpsModel.class);
        model.addIpsObjectExtensionProperty(property4);
        props = model.getExtensionPropertyDefinitions(extendedClass, true);
        assertEquals(4, props.length);
        assertSame(property, props[0]);  // first the type's properties
        assertSame(property2, props[1]); // then the supertype's properties
        assertSame(property3, props[2]); // then the supertype's supertype's properties
        assertSame(property4, props[3]); // the the type's interface's properties
        props = model.getExtensionPropertyDefinitions(extendedClass, false);
        assertEquals(1, props.length);
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
    	IChangesOverTimeNamingConvention convention = model.getChangesOverTimeNamingConvention(IChangesOverTimeNamingConvention.VAA);
    	assertNotNull(convention);
    	assertEquals(IChangesOverTimeNamingConvention.VAA, convention.getId());

    	convention = model.getChangesOverTimeNamingConvention(IChangesOverTimeNamingConvention.PM);
    	assertNotNull(convention);
    	assertEquals(IChangesOverTimeNamingConvention.PM, convention.getId());

    	convention = model.getChangesOverTimeNamingConvention("unknown");
    	assertNotNull(convention);
    	assertEquals(IChangesOverTimeNamingConvention.VAA, convention.getId());
    }

    public void testGetNonIpsResources() throws CoreException{
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                IProject project = newPlatformProject("TestJavaProject");
                javaProject= addJavaCapabilities(project);
                IProject project2 = newPlatformProject("TestJavaProject2");
                javaProject2= addJavaCapabilities(project2);
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

        newIpsProject("TestProject");

        assertNotNull(javaProject);
        assertNotNull(javaProject2);

        Object[] nonIpsResources= model.getNonIpsProjects();
        assertEquals(2, nonIpsResources.length);
        // compare handles (IProject)
        assertEquals(javaProject.getProject(), nonIpsResources[0]);
        assertEquals(javaProject2.getProject(), nonIpsResources[1]);
    }

    public void testClearValidationCache() throws CoreException{
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
        model.runAndQueueChangeEvents(action, null);

        assertEquals(2, listener.changedFiles.size());
        assertEquals(typeA.getIpsSrcFile(), listener.changedFiles.get(0));
        assertEquals(typeB.getIpsSrcFile(), listener.changedFiles.get(1));
    }

    class TestContentsChangeListener implements ContentsChangeListener {

        List changedFiles = new ArrayList();
        ContentChangeEvent lastEvent;

        public void contentsChanged(ContentChangeEvent event) {
            changedFiles.add(event.getIpsSrcFile());
            lastEvent = event;
        }

    };

    class TestModStatusListener implements IModificationStatusChangeListener {

        ModificationStatusChangedEvent lastEvent;

        public void modificationStatusHasChanged(ModificationStatusChangedEvent event) {
            lastEvent = event;
        }

    }

    public void testGetSortDefinition() throws CoreException, IOException {
        IIpsProject project = newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];

        root.createPackageFragment("products.hausrat", true, null);
        IIpsPackageFragment unfall = root.createPackageFragment("products.unfall", true, null);
        IIpsPackageFragment leistungsarten = root.createPackageFragment("products.unfall.leistungsarten", true, null);
        root.createPackageFragment("products.unfall.leistungsartgruppen", true, null);
        IIpsPackageFragment kranken = root.createPackageFragment("products.kranken", true, null);

        IIpsPackageFragment products = root.getIpsPackageFragment("products");

        List list = new ArrayList();
        list.add("kranken");
        list.add("unfall");
        list.add("hausrat");
        createPackageOrderFile((IFolder) products.getCorrespondingResource(), list);
        list.clear();

        list.add("leistungsarten");
        list.add("leistungsartgruppen");
        createPackageOrderFile((IFolder) unfall.getCorrespondingResource(), list);
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
        IFile file = getCorrespondingSortOrderFile(leistungsarten);
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
        file = getCorrespondingSortOrderFile(kranken);
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

    private IFile getCorrespondingSortOrderFile(IIpsPackageFragment fragment) {
        IFolder folder = null;

        if (fragment.isDefaultPackage()) {
            folder = (IFolder) fragment.getRoot().getCorrespondingResource();
        } else {
            folder = (IFolder) fragment.getParentIpsPackageFragment().getCorrespondingResource();
        }

        return folder.getFile(new Path(IpsPackageFragment.SORT_ORDER_FILE));
    }


}