/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
// TODO test mit pfad relativ zum workspace Testprojekt/lib/archive.jar
// TODO test von isContained() Methode im IpsArcheEntry
public class IpsArchiveTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IpsArchive archive;
    private AFile archiveFile;
    private File externalArchiveFile;
    private Path archivePath;
    private Path externalArchivePath;

    private IPolicyCmptType motorPolicyType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("ArchiveProject");
        motorPolicyType = newPolicyCmptType(project, "motor.MotorPolicy");
        newPolicyCmptTypeWithoutProductCmptType(project, "motor.collision.SimpleCollisionCoverage");
        newPolicyCmptTypeWithoutProductCmptType(project, "motor.collision.ExtendedCollisionCoverage");
        newPolicyCmptTypeWithoutProductCmptType(project, "home.base.HomePolicy");
        AFile iconFile = ((AFolder)project.getIpsPackageFragmentRoots()[0].getCorrespondingResource())
                .getFile("myTest.gif");
        iconFile.create(new ByteArrayInputStream("imageContent".getBytes()), new NullProgressMonitor());
        IProductCmptType prodType = motorPolicyType.findProductCmptType(project);
        prodType.setInstancesIcon("myTest.gif");
        prodType.getIpsSrcFile().save(true, new NullProgressMonitor());

        archiveFile = project.getProject().getFile("test124.ipsar");
        archivePath = archiveFile.getWorkspaceRelativePath();
        createArchive(project, archiveFile);
        archive = new IpsArchive(project, archivePath);

        externalArchiveFile = File.createTempFile("externalArchiveFile", ".ipsar");
        externalArchiveFile.deleteOnExit();
        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(project, externalArchiveFile);
        op.run(null);
        externalArchivePath = PathUtil.fromOSString(externalArchiveFile.getAbsolutePath());
    }

    /**
     * Tests if the access methods work correct if we change the underlying archive file on disk.
     */
    @Test
    public void testModificationToUnderlyingFile() throws Exception {
        QualifiedNameType qnt = new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE);
        assertTrue(archive.contains(qnt.toPath()));

        motorPolicyType.getIpsSrcFile().getCorrespondingFile().delete(null);
        project.getProject().build(ABuildKind.INCREMENTAL_BUILD, new NullProgressMonitor());

        createArchive(project, archiveFile);
        assertFalse(archive.contains(qnt.toPath()));
    }

    @Test
    public void testGetBasePackageNameForMergableArtefacts() {
        archiveFile.refreshLocal(AResourceTreeTraversalDepth.RESOURCE_ONLY, null);
        project.getProject().refreshLocal(AResourceTreeTraversalDepth.INFINITE, null);
        String expPackage = motorPolicyType.getIpsSrcFile().getBasePackageNameForMergableArtefacts();
        MessageList msgList = project.validate();
        assertFalse(msgList.toString(), msgList.containsErrorMsg());
        assertEquals(expPackage,
                archive.getBasePackageNameForMergableArtefacts(motorPolicyType.getQualifiedNameType()));
    }

    @Test
    public void testGetBasePackageNameForDerivedArtefacts() {
        archiveFile.refreshLocal(AResourceTreeTraversalDepth.RESOURCE_ONLY, null);
        project.getProject().refreshLocal(AResourceTreeTraversalDepth.INFINITE, null);
        String expPackage = motorPolicyType.getIpsSrcFile().getBasePackageNameForDerivedArtefacts();
        QualifiedNameType qualifiedNameType = motorPolicyType.getQualifiedNameType();
        assertNotNull(qualifiedNameType);
        MessageList msgList = project.validate();
        assertFalse(msgList.toString(), msgList.containsErrorMsg());
        assertEquals(expPackage, archive.getBasePackageNameForDerivedArtefacts(qualifiedNameType));
    }

    @Test
    public void testContains() {
        assertTrue(archive
                .contains(new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE).toPath()));
        assertFalse(archive.contains(new QualifiedNameType("Unknown", IpsObjectType.POLICY_CMPT_TYPE).toPath()));
    }

    @Test
    public void testContains_icon() {
        assertTrue(archive.contains(Path.of("myTest.gif")));

        assertFalse(archive.contains(Path.of("test.png")));
    }

    @Test
    public void testContainsPackage() {
        assertTrue(archive.containsPackage(""));
        assertFalse(archive.containsPackage(null));

        assertTrue(archive.containsPackage("motor"));
        assertTrue(archive.containsPackage("motor.collision"));
        assertTrue(archive.containsPackage("home"));
        assertFalse(archive.containsPackage("unknwon"));
    }

    @Test
    public void testGetArchivePath() {
        assertEquals(archivePath, archive.getArchivePath());
    }

    @Test
    public void testExists_FileInSameProject() {
        assertTrue(archive.exists());
        archiveFile.delete(null);
        assertFalse(archive.exists());
    }

    @Test
    public void testExists_FileInWorkspaceButDifferentProject() {
        IIpsProject project2 = newIpsProject("Project2");
        IIpsArchive archive2 = new IpsArchive(project2, archiveFile.getWorkspaceRelativePath());

        assertTrue(archive2.exists());
        archiveFile.delete(null);
        assertFalse(archive2.exists());
    }

    @Test
    public void testExists_FileOutsideWorkspace() {
        IpsArchive archive = new IpsArchive(project, externalArchivePath);
        assertTrue(archive.exists());

        externalArchiveFile.delete();
        assertFalse(archive.exists());

        externalArchivePath = Path.of("//server/freigabe/abc.jar"); // UNC-Path
        archive = new IpsArchive(project, externalArchivePath);
        assertFalse(archive.exists());
    }

    @Test
    public void testGetNoneEmptyPackages() {
        String[] packs = archive.getNonEmptyPackages();
        assertEquals(3, packs.length);
        assertEquals("home.base", packs[0]);
        assertEquals("motor", packs[1]);
        assertEquals("motor.collision", packs[2]);
    }

    @Test
    public void testGetCorrespondingResource_FileInSameProject() {
        assertEquals(archiveFile, archive.getCorrespondingResource());
    }

    @Test
    public void testGetCorrespondingResource_FileInWorkspaceButDifferentProject() {
        IIpsProject project2 = newIpsProject("Project2");
        IpsArchive archive2 = new IpsArchive(project2, archiveFile.getWorkspaceRelativePath());

        assertEquals(archiveFile, archive2.getCorrespondingResource());
    }

    @Test
    public void testGetCorrespondingResource_FileOutsideWorkspace() {
        IpsArchive ipsArchive = new IpsArchive(project, externalArchivePath);
        assertNull(ipsArchive.getCorrespondingResource());
    }

    @Test
    public void testGetLocation_FileInSameProject() {
        assertEquals(archiveFile.getLocation(), archive.getLocation());
    }

    @Test
    public void testGetLocation_FileInWorkspaceButDifferentProject() {
        IpsArchive ipsArchive = new IpsArchive(project, externalArchivePath);
        assertEquals(externalArchivePath, ipsArchive.getLocation());
    }

    @Test
    public void testGetLocation_FileOutsideWorkspace() {
        IIpsProject project2 = newIpsProject("Project2");
        IpsArchive archive2 = new IpsArchive(project2, archiveFile.getWorkspaceRelativePath());
        assertEquals(archiveFile.getLocation(), archive2.getLocation());
    }

    @Test
    public void testGetNoneEmptySubpackages() {
        String[] subpacks = archive.getNonEmptySubpackages("");
        assertEquals(2, subpacks.length);
        assertEquals("home", subpacks[0]);
        assertEquals("motor", subpacks[1]);

        subpacks = archive.getNonEmptySubpackages("motor");
        assertEquals(1, subpacks.length);
        assertEquals("motor.collision", subpacks[0]);

        assertEquals(0, archive.getNonEmptySubpackages(null).length);

        subpacks = archive.getNonEmptySubpackages("noneExistingPack");
        assertEquals(0, subpacks.length);
    }

    /*
     * Test for bug #1498
     */
    @Test
    public void testGetNoneEmptySubpackages_DefaultPackageContainsIpsSrcFile() throws Exception {
        IIpsProject anotherProject = newIpsProject("AnotherArchiveProject");
        newPolicyCmptType(anotherProject, "SomeType");
        archiveFile = anotherProject.getProject().getFile("anotherArchive.ipsar");
        archivePath = archiveFile.getProjectRelativePath();
        createArchive(anotherProject, archiveFile);
        archive = new IpsArchive(anotherProject, archivePath);

        assertEquals(0, archive.getNonEmptySubpackages("").length);
    }

    @Test
    public void testGetQNameTypes_FileInWorkspace() {
        Set<QualifiedNameType> qnt = archive.getQNameTypes();
        assertEquals(5, qnt.size());
        QualifiedNameType[] qntArray = new QualifiedNameType[qnt.size()];
        qnt.toArray(qntArray);

        assertEquals(new QualifiedNameType("home.base.HomePolicy", IpsObjectType.POLICY_CMPT_TYPE), qntArray[0]);
        assertEquals(new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE), qntArray[1]);
        assertEquals(new QualifiedNameType("motor.MotorPolicyProductCmpt", IpsObjectType.PRODUCT_CMPT_TYPE),
                qntArray[2]);
        assertEquals(
                new QualifiedNameType("motor.collision.ExtendedCollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE),
                qntArray[3]);
        assertEquals(new QualifiedNameType("motor.collision.SimpleCollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE),
                qntArray[4]);
    }

    @Test
    public void testGetQNameTypes_FileOutsideWorkspace() {
        IIpsArchive archiveOutsideWorkspace = new IpsArchive(project, externalArchivePath);
        assertNotNull(archiveOutsideWorkspace);

        Set<QualifiedNameType> qnt = archiveOutsideWorkspace.getQNameTypes();

        // same as in testGetQNameTypes_FileInWorkspace()
        assertEquals(5, qnt.size());
        QualifiedNameType[] qntArray = new QualifiedNameType[qnt.size()];
        qnt.toArray(qntArray);

        assertEquals(new QualifiedNameType("home.base.HomePolicy", IpsObjectType.POLICY_CMPT_TYPE), qntArray[0]);
        assertEquals(new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE), qntArray[1]);
        assertEquals(new QualifiedNameType("motor.MotorPolicyProductCmpt", IpsObjectType.PRODUCT_CMPT_TYPE),
                qntArray[2]);
        assertEquals(
                new QualifiedNameType("motor.collision.ExtendedCollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE),
                qntArray[3]);
        assertEquals(new QualifiedNameType("motor.collision.SimpleCollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE),
                qntArray[4]);
    }

    @Test
    public void testGetQNameType_Pack() {
        Set<QualifiedNameType> qnt = archive.getQNameTypes(null);
        assertEquals(0, qnt.size());

        qnt = archive.getQNameTypes("motor");
        assertEquals(2, qnt.size());
        assertTrue(qnt.contains(new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE)));

        qnt = archive.getQNameTypes("motor.collision");
        assertEquals(2, qnt.size());
        assertTrue(qnt.contains(new QualifiedNameType("motor.collision.SimpleCollisionCoverage",
                IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qnt.contains(new QualifiedNameType("motor.collision.ExtendedCollisionCoverage",
                IpsObjectType.POLICY_CMPT_TYPE)));

        qnt = archive.getQNameTypes("home");
        assertEquals(0, qnt.size());

        qnt = archive.getQNameTypes("unknown");
        assertEquals(0, qnt.size());
    }

    @Test
    public void testGetContent() {
        assertNull(archive.getContent(null));

        QualifiedNameType qnt = new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE);
        assertNotNull(archive.getContent(qnt.toPath()));

        qnt = new QualifiedNameType("Unknown", IpsObjectType.POLICY_CMPT_TYPE);
        assertNull(archive.getContent(qnt.toPath()));

        qnt = new QualifiedNameType("MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE);
        assertNull(archive.getContent(qnt.toPath()));
    }

    @Test
    public void testDuplicateEntryAndTestRefreshInWorkspace() throws Exception {
        // store archive time to check if the refresh is successful after re-creating the archive
        long timeBefore = archiveFile.getLocalTimeStamp();

        // check if duplicate entries will be overridden in the archive, e.g. test case runtime
        // xml's are generated
        // in the source and afterwards copied to the bin folder
        newIpsObject(project, IpsObjectType.TEST_CASE, "test.testcase");

        project.getProject().build(ABuildKind.INCREMENTAL_BUILD, null);

        File file = createFileIfNecessary(archiveFile);
        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(project, file);
        op.setInclJavaBinaries(true);
        op.setInclJavaSources(true);
        Abstractions.getWorkspace().run(op, null);
        createLinkIfNecessary(archiveFile, file);
        // no exception test was successful

        // check if the archive file was refreshed
        assertFalse(timeBefore == archiveFile.getLocalTimeStamp());
    }

    /**
     * Test Archive with default package only.
     * 
     * ArchiveIpsPackageFragment#getChildIpsPackageFragments
     */
    @Test
    public void testWithDefaultPackage() throws Exception {
        IIpsProject dummyProject = newIpsProject("DummyProject");
        newPolicyCmptTypeWithoutProductCmptType(dummyProject, "DummyPolicy");

        AFile dummyArchiveFile = dummyProject.getProject().getFile("test.ipsar");

        Path dummyArchivePath = dummyArchiveFile.getWorkspaceRelativePath();
        createArchive(dummyProject, dummyArchiveFile);

        IIpsObjectPath ipsObjectPath = dummyProject.getIpsObjectPath();
        ipsObjectPath.newArchiveEntry(dummyArchivePath);
        dummyProject.setIpsObjectPath(ipsObjectPath);

        List<IIpsElement> childList = new ArrayList<>();
        IIpsPackageFragmentRoot[] roots = dummyProject.getIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot root : roots) {
            IIpsPackageFragment[] fragments = root.getIpsPackageFragments();
            for (IIpsPackageFragment fragment : fragments) {
                collectChildren(fragment, childList);
            }
        }
        assertEquals(2, childList.size());
    }

    private void collectChildren(IIpsPackageFragment ipsPackageFragment, List<IIpsElement> childList) {
        IIpsElement[] childs = ipsPackageFragment.getChildren();
        for (IIpsElement child : childs) {
            childList.add(child);
        }
        IIpsPackageFragment[] childFragments = ipsPackageFragment.getChildIpsPackageFragments();
        for (IIpsPackageFragment childFragment : childFragments) {
            collectChildren(childFragment, childList);
        }
    }

    @Test
    public void testGetResourceAsStream() throws IpsException, IOException {
        // Icon must be accessible via ipsObjectPath, thus it is create in the default root-folder
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        AFolder rootFolder = (AFolder)root.getEnclosingResource();
        AFile iconFile = rootFolder.getFile("test.gif");
        // fake content, this is not a valid gif-file
        iconFile.create(new ByteArrayInputStream("test".getBytes()), new NullProgressMonitor());
        IProductCmptType prodType = newProductCmptType(project, "motor.MotorProduct");
        prodType.setInstancesIcon("test.gif");
        prodType.getIpsSrcFile().save(true, new NullProgressMonitor());

        // as a custom icon test.gif is automatically included in the archive
        Path path = project.getProject().getFile("test.ipsar").getLocation();
        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(project, path.toFile());
        op.run(new NullProgressMonitor());

        // test files existence in the archive
        IpsArchive ipsArchive = new IpsArchive(project, path);
        ipsArchive.getQNameTypes();
        ipsArchive.getResourceAsStream("test.gif");

        // test the archived file's contents
        byte[] fileContent = new byte[4];
        InputStream inStream = ipsArchive.getResourceAsStream("test.gif");
        inStream.read(fileContent);
        assertEquals("test", new String(fileContent));
    }

    @Test
    public void testIsValid() throws Exception {

    }

    @Test
    public void testGetPath_ipsObject() throws Exception {
        JarEntry jarEntry = mock(JarEntry.class);
        when(jarEntry.getName()).thenReturn(IpsArchive.IPSOBJECTS_FOLDER + "/my/object/dings");

        Path path = archive.getPath(jarEntry);

        assertEquals(Path.of("my/object/dings"), path);
    }

    @Test
    public void testGetPath_ipsObjectInvalid() throws Exception {
        JarEntry jarEntry = mock(JarEntry.class);
        when(jarEntry.getName()).thenReturn(IpsArchive.IPSOBJECTS_FOLDER + " 2" + "/my/object/dings");

        Path path = archive.getPath(jarEntry);

        assertNull(path);
    }

    @Test
    public void testGetPath_noRelevantEntry() throws Exception {
        JarEntry jarEntry = mock(JarEntry.class);
        when(jarEntry.getName()).thenReturn("not/relevant/dings");

        Path path = archive.getPath(jarEntry);

        assertNull(path);
    }

    @Test
    public void testGetPath_iconPng() throws Exception {
        JarEntry jarEntry = mock(JarEntry.class);
        when(jarEntry.getName()).thenReturn("my/object/dings.png");

        Path path = archive.getPath(jarEntry);

        assertEquals(Path.of("my/object/dings.png"), path);
    }

    @Test
    public void testGetPath_iconGif() throws Exception {
        JarEntry jarEntry = mock(JarEntry.class);
        when(jarEntry.getName()).thenReturn("my/object/dings.gif");

        Path path = archive.getPath(jarEntry);

        assertEquals(Path.of("my/object/dings.gif"), path);
    }

}
