/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IModificationStatusChangeListener;
import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.model.internal.ipsobject.IpsObject;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class IpsModelTest extends AbstractIpsPluginTest {

    private IpsModel model;

    // JavaProjects for testGetNonIpsResources()
    private IJavaProject javaProject = null;
    private IJavaProject javaProject2 = null;

    @SuppressWarnings("deprecation")
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        model = IpsModel.get();
    }

    @Test
    public void testCreateIpsProject() throws Exception {
        IProject project = newPlatformProject("TestProject");
        IJavaProject javaProject = addJavaCapabilities(project);
        IIpsProject ipsProject = model.createIpsProject(javaProject);
        assertNotNull(ipsProject);
        assertNotNull(ipsProject.getName());
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        assertNotNull(path);
        assertEquals(0, path.getEntries().length);
        assertNotNull(path.getIpsProject());
        assertNotNull(path.getIpsProject().getName());
        Thread.sleep(1000);
        IpsObjectPath path2 = ((IpsProject)ipsProject).getIpsObjectPathInternal();
        assertNotNull(path2.getIpsProject());
        assertNotNull(path2.getIpsProject().getName());
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testGetIpsElement_NonIpsProject() throws Exception {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("TestProject");
        project.create(null);
        project.open(null);

        IFolder rootFolder = project.getFolder("model");
        rootFolder.create(true, true, null);
        assertNull(model.getIpsElement(rootFolder));

        IFolder folderA = rootFolder.getFolder("a");
        folderA.create(true, true, null);
        assertNull(model.getIpsElement(folderA));

        String filename = IpsObjectType.POLICY_CMPT_TYPE.getFileName("Policy");
        IFile file = folderA.getFile(filename);
        file.create(new ByteArrayInputStream(new byte[0]), true, null);
        assertNotNull(model.getIpsElement(file));

        IFile textFile = folderA.getFile("Textfile.txt");
        textFile.create(new ByteArrayInputStream(new byte[0]), true, null);
        assertNull(model.getIpsElement(textFile));
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testResourceChanged() throws IOException, CoreException {
        IIpsProject ipsProject = this.newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment pack = root.createPackageFragment("pack", true, null);
        IIpsSrcFile sourceFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        IPolicyCmptType object = (PolicyCmptType)sourceFile.getIpsObject();
        IPolicyCmptTypeAttribute attribute = object.newPolicyCmptTypeAttribute();
        IDescription description = attribute.getDescription(Locale.US);
        description.setText("blabla");
        sourceFile.save(true, null);

        IFile file = sourceFile.getCorrespondingFile();
        assertTrue(file.exists());
        String encoding = ipsProject.getXmlFileCharset();
        String contents = StringUtil.readFromInputStream(file.getContents(), encoding);
        contents = StringUtils.replace(contents, "blabla", "something serious");
        ByteArrayInputStream is = new ByteArrayInputStream(contents.getBytes(encoding));
        file.setContents(is, true, false, null);

        object = (IPolicyCmptType)sourceFile.getIpsObject();
        attribute = object.getPolicyCmptTypeAttributes().get(0);
        assertEquals("something serious", attribute.getDescriptionText(Locale.US));
    }

    @Test
    public void testForceReloadOfCachedIpsSrcFileContents() throws CoreException {
        IIpsProject ipsProject = this.newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment pack = root.createPackageFragment("pack", true, null);
        IIpsSrcFile sourceFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        IPolicyCmptType object = (PolicyCmptType)sourceFile.getIpsObject();
        IPolicyCmptTypeAttribute attribute = object.newPolicyCmptTypeAttribute();
        IDescription description = attribute.getDescription(Locale.US);
        description.setText("blabla");
        sourceFile.save(true, null);

        IPolicyCmptTypeAttribute newAttribute = object.newPolicyCmptTypeAttribute();
        newAttribute.setDatatype("String");
        assertTrue(sourceFile.isDirty());
        ipsProject.getEnclosingResource().refreshLocal(0, new NullProgressMonitor());
        object = (IPolicyCmptType)sourceFile.getIpsObject();
        assertEquals(2, object.getPolicyCmptTypeAttributes().size());
        assertNotNull(object.getPolicyCmptTypeAttributes().get(1));
        assertTrue(sourceFile.isDirty());
    }

    @Test
    public void testForceReloadOfCachedIpsSrcFileContents_forExternalResources() throws Exception {
        IIpsProject ipsProject = newIpsProject();
        IpsModel ipsModel = (IpsModel)ipsProject.getIpsModel();

        // Resource does not exist in workspace, therefore will always return -1 as modification
        // stamp
        IResource externalResource = ipsProject.getProject().getFile("foo.bar");
        IpsObjectType ipsObjectType = mock(IpsObjectType.class);
        IpsSrcFile ipsSrcFile = mock(IpsSrcFile.class);
        when(ipsSrcFile.exists()).thenReturn(true);
        when(ipsSrcFile.getIpsObjectType()).thenReturn(ipsObjectType);
        when(ipsSrcFile.getEnclosingResource()).thenReturn(externalResource);
        when(ipsSrcFile.getContentFromEnclosingResource()).thenAnswer(withNewXmlInputStream());

        IpsObject ipsObject = mock(IpsObject.class);
        when(ipsObjectType.newObject(ipsSrcFile)).thenReturn(ipsObject);
        when(ipsObject.getIpsSrcFile()).thenReturn(ipsSrcFile);

        // prime the cache
        ipsModel.getIpsSrcFileContent(ipsSrcFile);

        // clear the cache
        IResourceChangeEvent event = mock(IResourceChangeEvent.class);
        when(event.getType()).thenReturn(IResourceChangeEvent.PRE_REFRESH);
        ipsModel.resourceChanged(event);

        // reload, as cache entry should be marked invalid
        ipsModel.getIpsSrcFileContent(ipsSrcFile);
        verify(ipsSrcFile, times(2)).getContentFromEnclosingResource();
    }

    private Answer<InputStream> withNewXmlInputStream() {
        return new Answer<InputStream>() {

            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                return new ByteArrayInputStream("<Foo/>".getBytes());
            }
        };
    }

    @Test
    public void testGetPredefinedValueDatatypes() {
        ValueDatatype[] datatypes = model.getPredefinedValueDatatypes();
        assertTrue(datatypes.length > 0);
    }

    @Test
    public void testIsPredefinedValueDatatype() {
        ValueDatatype[] datatypes = model.getPredefinedValueDatatypes();
        assertTrue(model.isPredefinedValueDatatype(datatypes[0].getQualifiedName()));
        assertFalse(model.isPredefinedValueDatatype("unknownDatatype"));
        assertFalse(model.isPredefinedValueDatatype(null));
    }

    @Test
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

    @Test
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

    @Test
    public void testClearValidationCache() throws CoreException {
        IIpsProject project = super.newIpsProject();
        IPolicyCmptType pcType = super.newPolicyCmptType(project, "TestedType");
        model.getValidationResultCache().putResult(pcType, new MessageList());

        assertNotNull(model.getValidationResultCache().getResult(pcType));

        model.clearValidationCache();

        assertNull(model.getValidationResultCache().getResult(pcType));
    }

    @Test
    public void testRunAndQueueChangeEvents() throws CoreException {
        IIpsProject project = newIpsProject();
        final IPolicyCmptType typeA = newPolicyCmptType(project, "A");
        final IDescription typeADescription = typeA.newDescription();
        typeADescription.setLocale(Locale.US);
        final IPolicyCmptType typeB = newPolicyCmptType(project, "B");
        final IDescription typeBDescription = typeB.newDescription();
        typeBDescription.setLocale(Locale.US);

        IWorkspaceRunnable action = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                typeADescription.setText("blabla");
                typeA.setSupertype(typeB.getQualifiedName());
                typeA.getIpsSrcFile().save(true, monitor);
                typeBDescription.setText("blabla");
                typeB.getIpsSrcFile().save(true, monitor);
                typeADescription.setText("New blabla");
                typeA.getIpsSrcFile().save(true, monitor);
            }

        };

        TestContentsChangeListener listener = new TestContentsChangeListener();
        model.addChangeListener(listener);
        TestModStatusListener modifyListener = new TestModStatusListener();
        model.addModifcationStatusChangeListener(modifyListener);

        model.runAndQueueChangeEvents(action, null);

        // Randomly failing test. Excessive message to try and understand it.
        assertEquals("Expected 2 changed files (" + typeA.getIpsSrcFile() + ", " + typeB.getIpsSrcFile()
                + "), but found " + listener.changedFiles.size()
                + listener.changedFiles.stream().map(IIpsSrcFile::toString).collect(Collectors.joining(", ", ")", ")")),
                2, listener.changedFiles.size());
        assertChangedFileIn(typeA.getIpsSrcFile(), listener.changedFiles);
        assertChangedFileIn(typeB.getIpsSrcFile(), listener.changedFiles);

        assertEquals(2, modifyListener.modifiedFiles.size());
        assertChangedFileIn(typeA.getIpsSrcFile(), modifyListener.modifiedFiles);
        assertChangedFileIn(typeB.getIpsSrcFile(), modifyListener.modifiedFiles);

        listener.changedFiles.clear();
        modifyListener.modifiedFiles.clear();

        typeADescription.setText("blublu");
        typeA.getIpsSrcFile().save(true, null);

        assertEquals(1, listener.changedFiles.size());
        assertEquals(typeA.getIpsSrcFile(), listener.changedFiles.get(0));

        // two entries are expected for modifiedFiles. Both are for the same file. First entry is
        // expected due to the call to the setDescription() method, second due to the call to the
        // save() of the ips source file
        assertEquals(2, modifyListener.modifiedFiles.size());
        assertChangedFileIn(typeA.getIpsSrcFile(), modifyListener.modifiedFiles);
    }

    @Test
    public void testRunAndQueueChangeEvents_ErrorHandling() throws CoreException {
        IIpsProject project = newIpsProject();
        final IPolicyCmptType typeA = newPolicyCmptType(project, "A");
        final IDescription typeADescription = typeA.newDescription();
        typeADescription.setLocale(Locale.US);
        typeADescription.setText("foo");
        typeA.getIpsSrcFile().save(true, null);
        final List<IStatus> logs = new LinkedList<IStatus>();
        ILog log = IpsLog.get();
        ILogListener logListener = (status, plugin) -> logs.add(status);
        log.addLogListener(logListener);
        try {
            IWorkspaceRunnable action = new IWorkspaceRunnable() {

                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    typeADescription.setText("blabla");
                    throw new CoreException(new Status(Status.ERROR, "MyPlugin", "MyMessage"));
                }

            };

            model.runSafe(action, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, null,
                    Collections.singleton(typeADescription.getIpsSrcFile()));

            assertEquals("foo", typeADescription.getText());
            assertEquals(1, logs.size());
            assertEquals(Status.ERROR, logs.get(0).getSeverity());
            assertEquals("MyMessage", logs.get(0).getMessage());
        } finally {
            log.removeLogListener(logListener);
        }
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

    @Test
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

    @Test(expected = UnsupportedOperationException.class)
    public void testDelete() throws CoreException {
        IIpsModel.get().delete();
    }

    @Test
    public void testGetVersionProvider_defaultVersionProvider() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IIpsProjectProperties properties = mock(IIpsProjectProperties.class);
        when(ipsProject.getReadOnlyProperties()).thenReturn(properties);

        IVersionProvider<?> versionProvider = model.getVersionProvider(ipsProject);

        assertThat(versionProvider, instanceOf(DefaultVersionProvider.class));
    }

    @Test
    public void testGetVersionProvider_cachedVersionProvider() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IIpsProjectProperties properties = mock(IIpsProjectProperties.class);
        when(ipsProject.getReadOnlyProperties()).thenReturn(properties);

        IVersionProvider<?> versionProvider1 = model.getVersionProvider(ipsProject);
        IVersionProvider<?> versionProvider2 = model.getVersionProvider(ipsProject);

        assertNotNull(versionProvider1);
        assertNotNull(versionProvider2);
        assertSame(versionProvider1, versionProvider2);
    }

}
