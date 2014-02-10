/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsContainerEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPathTest extends AbstractIpsPluginTest {

    private static final String MY_RESOURCE_PATCH = "myResourcePatch";
    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
    }

    @Test
    public void testGetEntry() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        assertNull(path.getEntry(null));
        assertNull(path.getEntry("unknown"));

        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        IIpsObjectPathEntry entry0 = path.newSourceFolderEntry(srcFolder);

        path.newIpsProjectRefEntry(newIpsProject("Project2"));

        IFile archiveFile = ipsProject.getProject().getFile("archive.jar");
        IIpsObjectPathEntry entry2 = path.newArchiveEntry(archiveFile.getLocation());

        assertEquals(entry0, path.getEntry("src"));
        assertNull(path.getEntry("Project2"));
        assertEquals(entry2, path.getEntry("archive.jar"));

        assertNull(path.getEntry("unknwon"));
        assertNull(path.getEntry(null));
    }

    @Test
    public void testNewSrcFolderEntry() throws CoreException {
        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsSrcFolderEntry entry0 = path.newSourceFolderEntry(srcFolder);
        assertEquals(path, entry0.getIpsObjectPath());
        assertEquals(2, path.getEntries().length); // default test project contains already 1 entry
        assertEquals(entry0, path.getEntries()[1]);

        IIpsSrcFolderEntry entry1 = path.newSourceFolderEntry(srcFolder);
        assertEquals(path, entry1.getIpsObjectPath());
        assertEquals(3, path.getEntries().length);
        assertEquals(entry0, path.getEntries()[1]);
        assertEquals(entry1, path.getEntries()[2]);
    }

    @Test
    public void testNewProjectRefEntry() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsProjectRefEntry entry0 = path.newIpsProjectRefEntry(ipsProject);
        assertEquals(path, entry0.getIpsObjectPath());
        assertEquals(2, path.getEntries().length); // default test project contains already 1 entry
        assertEquals(entry0, path.getEntries()[1]);

        IIpsProjectRefEntry entry1 = path.newIpsProjectRefEntry(ipsProject);
        assertEquals(path, entry1.getIpsObjectPath());
        assertEquals(2, path.getEntries().length); // the same project should not be added twice
        assertEquals(entry0, path.getEntries()[1]);
        assertEquals(entry1, path.getEntries()[1]);

        IIpsProjectRefEntry entry2 = path.newIpsProjectRefEntry(this.newIpsProject("TestProject2"));
        assertEquals(path, entry2.getIpsObjectPath());
        assertEquals(3, path.getEntries().length);
        assertEquals(entry0, path.getEntries()[1]);
        assertEquals(entry2, path.getEntries()[2]);
    }

    @Test
    public void testContainsProjectRefEntry() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        assertTrue(path.containsProjectRefEntry(ipsProject));

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(path.containsProjectRefEntry(ipsProject2));

        path.removeProjectRefEntry(ipsProject);
        assertFalse(path.containsProjectRefEntry(ipsProject));
    }

    @Test
    public void testRemoveProjectRefEntry() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsProjectRefEntry entry0 = path.newIpsProjectRefEntry(ipsProject);
        assertEquals(path, entry0.getIpsObjectPath());
        assertEquals(2, path.getEntries().length); // default test project contains already 1 entry
        assertEquals(entry0, path.getEntries()[1]);
        assertTrue(path.containsProjectRefEntry(ipsProject));
        path.removeProjectRefEntry(ipsProject);
        assertFalse(path.containsProjectRefEntry(ipsProject));
        assertEquals(1, path.getEntries().length);

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(path.containsProjectRefEntry(ipsProject2));
        path.removeProjectRefEntry(ipsProject2);
        assertFalse(path.containsProjectRefEntry(ipsProject2));
        assertEquals(1, path.getEntries().length);
    }

    @Test
    public void testContainsArchiveEntry() throws Exception {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
        createArchive(ipsProject, archiveFile);
        IIpsArchive ipsArchive = path.newArchiveEntry(archiveFile.getLocation()).getIpsArchive();
        assertTrue(path.containsArchiveEntry(ipsArchive));

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(ipsProject2.getIpsObjectPath().containsArchiveEntry(ipsArchive));

        path.removeArchiveEntry(ipsArchive);
        assertFalse(path.containsArchiveEntry(ipsArchive));
    }

    @Test
    public void testRemoveArchiveEntry() throws Exception {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
        createArchive(ipsProject, archiveFile);
        IIpsArchiveEntry entry0 = path.newArchiveEntry(archiveFile.getLocation());
        IIpsArchive archive0 = entry0.getIpsArchive();
        assertEquals(path, entry0.getIpsObjectPath());
        assertEquals(2, path.getEntries().length); // default test project contains already 1 entry
        assertEquals(entry0, path.getEntries()[1]);
        assertTrue(path.containsArchiveEntry(entry0.getIpsArchive()));
        path.removeArchiveEntry(archive0);
        assertEquals(1, path.getEntries().length);
        path.removeArchiveEntry(archive0);
        assertEquals(1, path.getEntries().length);
    }

    @Test
    public void testContainsSrcFolderEntry() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IFolder folder = ipsProject.getProject().getFolder("testfolder");
        path.newSourceFolderEntry(folder);
        assertTrue(path.containsSrcFolderEntry(folder));

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(ipsProject2.getIpsObjectPath().containsSrcFolderEntry(folder));

        path.removeSrcFolderEntry(folder);
        assertFalse(path.containsSrcFolderEntry(folder));
    }

    @Test
    public void testRemoveSrcFolderEntry() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IFolder folder = ipsProject.getProject().getFolder("testfolder");
        IIpsSrcFolderEntry entry0 = path.newSourceFolderEntry(folder);
        assertEquals(path, entry0.getIpsObjectPath());
        assertEquals(2, path.getEntries().length); // default test project contains already 1 entry
        assertEquals(entry0, path.getEntries()[1]);
        assertTrue(path.containsSrcFolderEntry(folder));
        path.removeSrcFolderEntry(folder);
        assertFalse(path.containsSrcFolderEntry(folder));
        assertEquals(1, path.getEntries().length);

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(ipsProject2.getIpsObjectPath().containsSrcFolderEntry(folder));
        path.removeSrcFolderEntry(folder);
        assertFalse(path.containsSrcFolderEntry(folder));
        assertEquals(1, path.getEntries().length);
    }

    @Test
    public void testGetReferencedIpsProjects() throws CoreException {
        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        IIpsProject refProject1 = ipsProject.getIpsModel().getIpsProject("RefProject1");
        IIpsProject refProject2 = ipsProject.getIpsModel().getIpsProject("RefProject2");
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(refProject1);
        path.newSourceFolderEntry(srcFolder);
        path.newIpsProjectRefEntry(refProject2);

        IIpsProject[] projects = path.getReferencedIpsProjects();
        assertEquals(2, projects.length);
        assertEquals(refProject1, projects[0]);
        assertEquals(refProject2, projects[1]);
    }

    @Test
    public void testGetReferencedIpsProjects_refInContainer() throws CoreException {
        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        IIpsProject refProject1 = ipsProject.getIpsModel().getIpsProject("RefProject1");
        IIpsProject refProject2 = ipsProject.getIpsModel().getIpsProject("RefProject2");
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(refProject1);
        path.newSourceFolderEntry(srcFolder);
        path.newIpsProjectRefEntry(refProject2);
        IIpsObjectPathEntry[] entries = path.getEntries();
        IIpsContainerEntry newContainerEntry = mock(IIpsContainerEntry.class);
        path.setEntries(new IIpsObjectPathEntry[] { newContainerEntry });
        when(newContainerEntry.getType()).thenReturn(IIpsObjectPathEntry.TYPE_CONTAINER);
        when(newContainerEntry.isContainer()).thenReturn(true);
        when(newContainerEntry.resolveEntries()).thenReturn(Arrays.asList(entries));

        IIpsProject[] projects = path.getReferencedIpsProjects();

        assertEquals(2, projects.length);
        assertEquals(refProject1, projects[0]);
        assertEquals(refProject2, projects[1]);
    }

    @Test
    public void testGetProjectRefEntries_container() throws Exception {
        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        IIpsProject refProject1 = ipsProject.getIpsModel().getIpsProject("RefProject1");
        IIpsProject refProject2 = ipsProject.getIpsModel().getIpsProject("RefProject2");
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsProjectRefEntry refEntry1 = path.newIpsProjectRefEntry(refProject1);
        path.newSourceFolderEntry(srcFolder);
        IIpsProjectRefEntry refEntry2 = path.newIpsProjectRefEntry(refProject2);
        IIpsObjectPathEntry[] entries = path.getEntries();
        IIpsContainerEntry newContainerEntry = mock(IIpsContainerEntry.class);
        path.setEntries(new IIpsObjectPathEntry[] { newContainerEntry });
        when(newContainerEntry.getType()).thenReturn(IIpsObjectPathEntry.TYPE_CONTAINER);
        when(newContainerEntry.isContainer()).thenReturn(true);
        when(newContainerEntry.resolveEntries()).thenReturn(Arrays.asList(entries));

        IIpsProjectRefEntry[] refEntries = path.getProjectRefEntries();

        assertEquals(2, refEntries.length);
        assertEquals(refEntry1, refEntries[0]);
        assertEquals(refEntry2, refEntries[1]);
    }

    @Test
    public void testFindIpsSrcFileStartingWith() throws CoreException {
        IIpsProject ipsProject2 = newIpsProject("TestProject2");

        IpsObjectPath path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject2);
        ipsProject.setIpsObjectPath(path);

        IIpsObject obj1 = newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "MotorPolicy");
        IIpsObject obj2 = newIpsObject(ipsProject2, IpsObjectType.POLICY_CMPT_TYPE, "MotorPolicy2");

        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        Set<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();
        path.findIpsSrcFilesStartingWith(IpsObjectType.POLICY_CMPT_TYPE, "Motor", false, result, visitedEntries);
        assertEquals(2, result.size());
        assertTrue(result.contains(obj1.getIpsSrcFile()));
        assertTrue(result.contains(obj2.getIpsSrcFile()));
    }

    @Test
    public void testFindIpsSrcFiles() throws Exception {
        IIpsObject obj1 = newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT_TYPE, "a.b.A");
        IIpsObject obj2 = newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT_TYPE, "a.b.B");
        IIpsObject obj3 = newIpsObject(ipsProject, IpsObjectType.PRODUCT_CMPT_TYPE, "a.b.C");

        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        Set<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();
        ((IpsObjectPath)ipsProject.getIpsObjectPath()).findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE, result,
                visitedEntries);

        assertTrue(result.contains(obj1.getIpsSrcFile()));
        assertTrue(result.contains(obj2.getIpsSrcFile()));
        assertTrue(result.contains(obj3.getIpsSrcFile()));
    }

    @Test
    public void testGetOutputFolders() {
        IProject project = ipsProject.getProject();
        IpsObjectPath path = new IpsObjectPath(ipsProject);
        IFolder out0 = project.getFolder("out0");
        IFolder ext0 = project.getFolder("ext0");
        path.setOutputFolderForMergableSources(out0);
        path.setOutputFolderForDerivedSources(ext0);

        IIpsSrcFolderEntry entry0 = path.newSourceFolderEntry(project.getFolder("src0"));
        IFolder out1 = project.getFolder("out1");
        entry0.setSpecificOutputFolderForMergableJavaFiles(out1);
        IIpsSrcFolderEntry entry1 = path.newSourceFolderEntry(project.getFolder("src1"));
        IFolder out2 = project.getFolder("out2");
        entry1.setSpecificOutputFolderForMergableJavaFiles(out2);
        IIpsSrcFolderEntry entry2 = path.newSourceFolderEntry(project.getFolder("src1"));
        entry2.setSpecificOutputFolderForMergableJavaFiles(null);
        path.newIpsProjectRefEntry(ipsProject);

        // one output folder for all src folders
        path.setOutputDefinedPerSrcFolder(false);
        IFolder[] outFolders = path.getOutputFolders();
        assertEquals(1, outFolders.length);
        assertEquals(out0, outFolders[0]);

        // one output folder, but it is null
        path.setOutputFolderForMergableSources(null);
        outFolders = path.getOutputFolders();
        assertEquals(0, outFolders.length);

        // output defined per src folder
        path.setOutputDefinedPerSrcFolder(true);
        outFolders = path.getOutputFolders();
        assertEquals(2, outFolders.length);
        assertEquals(out1, outFolders[0]);
        assertEquals(out2, outFolders[1]);
    }

    @Test
    public void testValidate() throws CoreException {
        MessageList ml = ipsProject.validate();
        assertEquals(0, ml.size());

        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();

        // validate missing outputFolderGenerated
        IFolder folder1 = ipsProject.getProject().getFolder("none");
        path.setOutputFolderForMergableSources(folder1);
        path.setOutputDefinedPerSrcFolder(false);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertNotNull(ml.getMessageByCode(IIpsObjectPathEntry.MSGCODE_MISSING_FOLDER));

        // validate missing outputFolderExtension
        path.setOutputFolderForDerivedSources(folder1);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(2, ml.size());

        // validate missing folders only when general output folder needs to be defined
        path.setOutputDefinedPerSrcFolder(true);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(0, ml.size());
    }

    @Test
    public void testValidateOutputFolderMergableAndDerivedEmpty() throws Exception {
        MessageList ml = ipsProject.validate();
        assertEquals(0, ml.size());

        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();

        ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsObjectPath.MSGCODE_MERGABLE_OUTPUT_FOLDER_NOT_SPECIFIED));
        assertNull(ml.getMessageByCode(IIpsObjectPath.MSGCODE_DERIVED_OUTPUT_FOLDER_NOT_SPECIFIED));

        path.setOutputDefinedPerSrcFolder(false);
        path.setOutputFolderForMergableSources(null);
        path.setOutputFolderForDerivedSources(null);
        ipsProject.setProperties(props);

        ml = ipsProject.validate();
        assertNotNull(ml.getMessageByCode(IIpsObjectPath.MSGCODE_MERGABLE_OUTPUT_FOLDER_NOT_SPECIFIED));
        assertNotNull(ml.getMessageByCode(IIpsObjectPath.MSGCODE_DERIVED_OUTPUT_FOLDER_NOT_SPECIFIED));
    }

    @Test
    public void testConstructor() {

        try {
            new IpsObjectPath(null);
            fail();
        } catch (Exception e) {
        }

        IpsProject ipsProject = new IpsProject();
        IpsObjectPath path = new IpsObjectPath(ipsProject);
        assertSame(ipsProject, path.getIpsProject());
    }

    @Test
    public void testMoveEntries() throws Exception {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();

        IIpsObjectPathEntry entry0 = path.getEntries()[0]; // default test project contains already
        // 1 entry
        IIpsSrcFolderEntry entry1 = path.newSourceFolderEntry(ipsProject.getProject().getFolder("src"));
        IIpsSrcFolderEntry entry2 = path.newSourceFolderEntry(ipsProject.getProject().getFolder("src2"));
        IIpsSrcFolderEntry entry3 = path.newSourceFolderEntry(ipsProject.getProject().getFolder("src3"));

        assertEquals(4, path.getEntries().length);

        // move top two entries one position down
        int[] newIndices = path.moveEntries(new int[] { 0, 1 }, false);
        assertEquals(4, path.getEntries().length);
        assertEquals(2, newIndices.length);
        assertTrue((newIndices[0] == 1) || (newIndices[1] == 1)); // check if the expected indices
        // are contained in the
        assertTrue((newIndices[0] == 2) || (newIndices[1] == 2)); // returned array (no order
        // guaranteed)

        assertEquals(entry2, path.getEntries()[0]); // check if the IPS object path was really
        // modified in the
        assertEquals(entry0, path.getEntries()[1]); // expected manner
        assertEquals(entry1, path.getEntries()[2]);
        assertEquals(entry3, path.getEntries()[3]);

        // now move last three entries one position up
        newIndices = path.moveEntries(new int[] { 3, 1, 2 }, true);
        assertEquals(4, path.getEntries().length);
        assertEquals(3, newIndices.length);
        assertTrue((newIndices[0] == 0) || (newIndices[1] == 0) || (newIndices[2] == 0));
        assertTrue((newIndices[0] == 1) || (newIndices[1] == 1) || (newIndices[2] == 1));
        assertTrue((newIndices[0] == 2) || (newIndices[1] == 2) || (newIndices[2] == 2));

        assertEquals(entry0, path.getEntries()[0]);
        assertEquals(entry1, path.getEntries()[1]);
        assertEquals(entry3, path.getEntries()[2]);
        assertEquals(entry2, path.getEntries()[3]);

        // invalid values should not change the elements order
        newIndices = path.moveEntries(new int[] { -2, 42 }, true);
        assertEquals(entry0, path.getEntries()[0]);
        assertEquals(entry1, path.getEntries()[1]);
        assertEquals(entry3, path.getEntries()[2]);
        assertEquals(entry2, path.getEntries()[3]);

        // invalid values should not change the elements order
        newIndices = path.moveEntries(new int[] { -3, 21 }, false);
        assertEquals(entry0, path.getEntries()[0]);
        assertEquals(entry1, path.getEntries()[1]);
        assertEquals(entry3, path.getEntries()[2]);
        assertEquals(entry2, path.getEntries()[3]);
    }

    @Test
    public void testNewContainerEntry() throws Exception {
        String containerTypeId = "anyContainerId";
        String optionalPath = "anyOptionalPath";
        IpsObjectPath path = new IpsObjectPath(ipsProject);
        path.newSourceFolderEntry(ipsProject.getProject().getFolder("anyFolder"));

        IIpsContainerEntry containerEntry = path.newContainerEntry(containerTypeId, optionalPath);

        assertEquals(containerTypeId, containerEntry.getContainerTypeId());
        assertEquals(optionalPath, containerEntry.getOptionalPath());
    }

    @Test
    public void testFindExistingContainer_noResult() throws Exception {
        IpsObjectPath path = new IpsObjectPath(ipsProject);

        assertNull(path.findExistingContainer("containe", null));
    }

    @Test
    public void testFindExistingContainer_noOptionalPath() throws Exception {
        String containerTypeId = "anyContainerId";
        IpsObjectPath path = new IpsObjectPath(ipsProject);
        path.newSourceFolderEntry(ipsProject.getProject().getFolder("anyFolder"));
        IIpsContainerEntry containerEntry = path.newContainerEntry(containerTypeId, null);

        IIpsContainerEntry existingContainer = path.findExistingContainer(containerTypeId, null);

        assertEquals(containerEntry, existingContainer);
    }

    @Test
    public void testFindExistingContainer_withOptionalPath() throws Exception {
        String containerTypeId = "anyContainerId";
        String optionalPath = "anyOptionalPath";
        IpsObjectPath path = new IpsObjectPath(ipsProject);
        path.newSourceFolderEntry(ipsProject.getProject().getFolder("anyFolder"));
        IIpsContainerEntry containerEntry = path.newContainerEntry(containerTypeId, optionalPath);

        IIpsContainerEntry existingContainer = path.findExistingContainer(containerTypeId, optionalPath);

        assertEquals(containerEntry, existingContainer);
    }

    @Test
    public void testContainsResource_true() throws Exception {
        IpsObjectPath ipsObjectPath = new IpsObjectPath(ipsProject);
        IIpsProject mockProject = mock(IIpsProject.class);
        IIpsProject mockProject2 = mock(IIpsProject.class);
        ipsObjectPath.newIpsProjectRefEntry(mockProject);
        ipsObjectPath.newIpsProjectRefEntry(mockProject2);
        when(mockProject2.containsResource(MY_RESOURCE_PATCH)).thenReturn(true);

        assertTrue(ipsObjectPath.containsResource(MY_RESOURCE_PATCH));
    }

    @Test
    public void testContainsResource_flase() throws Exception {
        IpsObjectPath ipsObjectPath = new IpsObjectPath(ipsProject);
        IIpsProject mockProject = mock(IIpsProject.class);
        IIpsProject mockProject2 = mock(IIpsProject.class);
        ipsObjectPath.newIpsProjectRefEntry(mockProject);
        ipsObjectPath.newIpsProjectRefEntry(mockProject2);

        assertFalse(ipsObjectPath.containsResource(MY_RESOURCE_PATCH));
    }

    @Test
    public void testContainsResource_empty() throws Exception {
        IpsObjectPath ipsObjectPath = new IpsObjectPath(ipsProject);

        assertFalse(ipsObjectPath.containsResource(MY_RESOURCE_PATCH));
    }

    @Test
    public void testGetIndex() throws Exception {
        IpsObjectPath ipsObjectPath = new IpsObjectPath(ipsProject);
        IIpsArchiveEntry newArchiveEntry = ipsObjectPath.newArchiveEntry(new Path("anyPath"));
        IIpsContainerEntry newContainerEntry = ipsObjectPath.newContainerEntry("MyContainer", "myContainerPath");

        assertEquals(0, ipsObjectPath.getIndex(newArchiveEntry));
        assertEquals(1, ipsObjectPath.getIndex(newContainerEntry));
    }

    @Test
    public void testGetIndex_inContainer() throws Exception {
        IpsObjectPath ipsObjectPath = new IpsObjectPath(ipsProject);
        IIpsObjectPathEntry entry0 = mock(IIpsObjectPathEntry.class);
        IIpsContainerEntry entry1 = mock(IIpsContainerEntry.class);
        IIpsObjectPathEntry containerEntry2 = mock(IIpsObjectPathEntry.class);
        IIpsObjectPathEntry containerEntry3 = mock(IIpsObjectPathEntry.class);
        IIpsObjectPathEntry entry4 = mock(IIpsObjectPathEntry.class);
        when(entry1.resolveEntries()).thenReturn(Arrays.asList(containerEntry2, containerEntry3));
        ipsObjectPath.setEntries(new IIpsObjectPathEntry[] { entry0, entry1, entry4 });

        assertEquals(0, ipsObjectPath.getIndex(entry0));
        assertEquals(1, ipsObjectPath.getIndex(entry1));
        assertEquals(2, ipsObjectPath.getIndex(containerEntry2));
        assertEquals(3, ipsObjectPath.getIndex(containerEntry3));
        assertEquals(4, ipsObjectPath.getIndex(entry4));
    }

}
