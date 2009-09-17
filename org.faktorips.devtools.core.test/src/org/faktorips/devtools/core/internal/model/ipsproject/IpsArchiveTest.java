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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * 
 * @author Jan Ortmann
 */
// TODO test mit pfad relativ zum workspace Testprojekt/lib/archive.jar
// TODO test von isContained() Methode im IpsArcheEntry
public class IpsArchiveTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IpsArchive archive;
    private IFile archiveFile;
    private File externalArchiveFile;
    private IPath archivePath;
    private IPath externalArchivePath;

    private IPolicyCmptType motorPolicyType;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("ArchiveProject");
        motorPolicyType = newPolicyCmptTypeWithoutProductCmptType(project, "motor.MotorPolicy");
        newPolicyCmptTypeWithoutProductCmptType(project, "motor.collision.SimpleCollisionCoverage");
        newPolicyCmptTypeWithoutProductCmptType(project, "motor.collision.ExtendedCollisionCoverage");
        newPolicyCmptTypeWithoutProductCmptType(project, "home.base.HomePolicy");

        archiveFile = project.getProject().getFile("test.ipsar");
        archivePath = archiveFile.getProjectRelativePath();
        createArchive(project, archiveFile);
        archive = new IpsArchive(project, archivePath);

        externalArchiveFile = File.createTempFile("externalArchiveFile", ".ipsar");
        externalArchiveFile.deleteOnExit();
        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(project, externalArchiveFile);
        op.run(null);
        externalArchivePath = Path.fromOSString(externalArchiveFile.getAbsolutePath());
    }

    /**
     * Tests if the access methods work correct if we change the underlying archive file on disk.
     */
    public void testModificationToUnderlyingFile() throws Exception {
        QualifiedNameType qnt = new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE);
        assertTrue(archive.contains(qnt));

        motorPolicyType.getIpsSrcFile().getCorrespondingFile().delete(IResource.ALWAYS_DELETE_PROJECT_CONTENT, null);
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());

        createArchive(project, archiveFile);
        assertFalse(archive.contains(qnt));
    }

    public void testGetBasePackageNameForMergableArtefacts() throws CoreException {
        String expPackage = motorPolicyType.getIpsSrcFile().getBasePackageNameForMergableArtefacts();
        assertEquals(expPackage, archive.getBasePackageNameForMergableArtefacts(motorPolicyType.getQualifiedNameType()));
    }

    public void testGetBasePackageNameForDerivedArtefacts() throws CoreException {
        String expPackage = motorPolicyType.getIpsSrcFile().getBasePackageNameForDerivedArtefacts();
        assertEquals(expPackage, archive.getBasePackageNameForDerivedArtefacts(motorPolicyType.getQualifiedNameType()));
    }

    public void testContains() throws CoreException {
        assertTrue(archive.contains(new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE)));
        assertFalse(archive.contains(new QualifiedNameType("Unknown", IpsObjectType.POLICY_CMPT_TYPE)));
    }

    public void testContainsPackage() throws CoreException {
        assertTrue(archive.containsPackage(""));
        assertFalse(archive.containsPackage(null));

        assertTrue(archive.containsPackage("motor"));
        assertTrue(archive.containsPackage("motor.collision"));
        assertTrue(archive.containsPackage("home"));
        assertFalse(archive.containsPackage("unknwon"));

    }

    public void testGetArchivePath() {
        assertEquals(archivePath, archive.getArchivePath());
    }

    public void testExists_FileInSameProject() throws CoreException {
        assertTrue(archive.exists());
        archiveFile.delete(true, null);
        assertFalse(archive.exists());
    }

    public void testExists_FileInWorkspaceButDifferentProject() throws CoreException {
        IIpsProject project2 = newIpsProject("Project2");
        IIpsArchive archive2 = new IpsArchive(project2, archiveFile.getFullPath());

        assertTrue(archive2.exists());
        archiveFile.delete(true, null);
        assertFalse(archive2.exists());
    }

    public void testExists_FileOutsideWorkspace() throws IOException {
        IpsArchive archive = new IpsArchive(project, externalArchivePath);
        assertTrue(archive.exists());

        externalArchiveFile.delete();
        assertFalse(archive.exists());

        externalArchivePath = new Path("//server/freigabe/abc.jar"); // UNC-Path
        archive = new IpsArchive(project, externalArchivePath);
        assertFalse(archive.exists());
    }

    public void testGetNoneEmptyPackages() throws CoreException {
        String[] packs = archive.getNonEmptyPackages();
        assertEquals(3, packs.length);
        assertEquals("home.base", packs[0]);
        assertEquals("motor", packs[1]);
        assertEquals("motor.collision", packs[2]);
    }

    public void testGetCorrespondingResource_FileInSameProject() {
        assertEquals(archiveFile, archive.getCorrespondingResource());
    }

    public void testGetCorrespondingResource_FileInWorkspaceButDifferentProject() throws CoreException {
        IIpsProject project2 = newIpsProject("Project2");
        IpsArchive archive2 = new IpsArchive(project2, archiveFile.getFullPath());

        assertEquals(archiveFile, archive2.getCorrespondingResource());
    }

    public void testGetCorrespondingResource_FileOutsideWorkspace() {
        IpsArchive ipsArchive = new IpsArchive(project, externalArchivePath);
        assertNull(ipsArchive.getCorrespondingResource());
    }

    public void testGetLocation_FileInSameProject() {
        assertEquals(archiveFile.getLocation(), archive.getLocation());
    }

    public void testGetLocation_FileInWorkspaceButDifferentProject() throws CoreException {
        IpsArchive ipsArchive = new IpsArchive(project, externalArchivePath);
        assertEquals(externalArchivePath, ipsArchive.getLocation());
    }

    public void testGetLocation_FileOutsideWorkspace() throws CoreException {
        IIpsProject project2 = newIpsProject("Project2");
        IpsArchive archive2 = new IpsArchive(project2, archiveFile.getFullPath());
        assertEquals(archiveFile.getLocation(), archive2.getLocation());
    }

    public void testGetNoneEmptySubpackages() throws CoreException {
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
    public void testGetNoneEmptySubpackages_DefaultPackageContainsIpsSrcFile() throws Exception {
        IIpsProject anotherProject = newIpsProject("AnotherArchiveProject");
        newPolicyCmptType(anotherProject, "SomeType");
        archiveFile = anotherProject.getProject().getFile("anotherArchive.ipsar");
        archivePath = archiveFile.getProjectRelativePath();
        createArchive(anotherProject, archiveFile);
        archive = new IpsArchive(anotherProject, archivePath);

        assertEquals(0, archive.getNonEmptySubpackages("").length);
    }

    public void testGetQNameTypes_FileInWorkspace() throws CoreException {
        Set<QualifiedNameType> qnt = archive.getQNameTypes();
        assertEquals(4, qnt.size());
        QualifiedNameType[] qntArray = new QualifiedNameType[qnt.size()];
        qnt.toArray(qntArray);

        assertEquals(new QualifiedNameType("home.base.HomePolicy", IpsObjectType.POLICY_CMPT_TYPE), qntArray[0]);
        assertEquals(new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE), qntArray[1]);
        assertEquals(
                new QualifiedNameType("motor.collision.ExtendedCollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE),
                qntArray[2]);
        assertEquals(new QualifiedNameType("motor.collision.SimpleCollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE),
                qntArray[3]);
    }

    public void testGetQNameTypes_FileOutsideWorkspace() throws CoreException, IOException {
        IIpsArchive archiveOutsideWorkspace = new IpsArchive(project, externalArchivePath);
        assertNotNull(archiveOutsideWorkspace);

        Set<QualifiedNameType> qnt = archiveOutsideWorkspace.getQNameTypes();

        // same as in testGetQNameTypes_FileInWorkspace()
        assertEquals(4, qnt.size());
        QualifiedNameType[] qntArray = new QualifiedNameType[qnt.size()];
        qnt.toArray(qntArray);

        assertEquals(new QualifiedNameType("home.base.HomePolicy", IpsObjectType.POLICY_CMPT_TYPE), qntArray[0]);
        assertEquals(new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE), qntArray[1]);
        assertEquals(
                new QualifiedNameType("motor.collision.ExtendedCollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE),
                qntArray[2]);
        assertEquals(new QualifiedNameType("motor.collision.SimpleCollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE),
                qntArray[3]);
    }

    public void testGetQNameType_Pack() throws CoreException {
        Set<QualifiedNameType> qnt = archive.getQNameTypes(null);
        assertEquals(0, qnt.size());

        qnt = archive.getQNameTypes("motor");
        assertEquals(1, qnt.size());
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

    public void testGetContent() throws CoreException {
        assertNull(archive.getContent(null));

        QualifiedNameType qnt = new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE);
        assertNotNull(archive.getContent(qnt));

        qnt = new QualifiedNameType("Unknown", IpsObjectType.POLICY_CMPT_TYPE);
        assertNull(archive.getContent(qnt));

        qnt = new QualifiedNameType("MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE);
        assertNull(archive.getContent(qnt));
    }

    public void testDuplicateEntryAndTestRefreshInWorkspace() throws Exception {
        // store archive time to check if the refresh is successful after re-creating the archive
        long timeBefore = archiveFile.getLocalTimeStamp();

        // check if duplicate entries will be overridden in the archive, e.g. test case runtime
        // xml's are generated
        // in the source and afterwards copied to the bin folder
        newIpsObject(project, IpsObjectType.TEST_CASE, "test.testcase");

        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);

        File file = createFileIfNecessary(archiveFile);
        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(project, file);
        op.setInclJavaBinaries(true);
        op.setInclJavaSources(true);
        ResourcesPlugin.getWorkspace().run(op, null);
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
    public void testWithDefaultPackage() throws Exception {
        IIpsProject dummyProject = newIpsProject("DummyProject");
        newPolicyCmptTypeWithoutProductCmptType(dummyProject, "DummyPolicy");

        IFile dummyArchiveFile = dummyProject.getProject().getFile("test.ipsar");

        IPath dummyArchivePath = dummyArchiveFile.getProjectRelativePath();
        createArchive(dummyProject, dummyArchiveFile);

        IIpsObjectPath ipsObjectPath = dummyProject.getIpsObjectPath();
        ipsObjectPath.newArchiveEntry(dummyArchivePath);
        dummyProject.setIpsObjectPath(ipsObjectPath);

        List<IIpsElement> childList = new ArrayList<IIpsElement>();
        IIpsPackageFragmentRoot[] roots = dummyProject.getIpsPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            IIpsPackageFragment[] fragments = roots[i].getIpsPackageFragments();
            for (int j = 0; j < fragments.length; j++) {
                collectChildren(fragments[j], childList);
            }
        }
        assertEquals(2, childList.size());
    }

    private void collectChildren(IIpsPackageFragment ipsPackageFragment, List<IIpsElement> childList)
            throws CoreException {
        IIpsElement[] childs = ipsPackageFragment.getChildren();
        for (int k = 0; k < childs.length; k++) {
            childList.add(childs[k]);
        }
        IIpsPackageFragment[] childFragments = ipsPackageFragment.getChildIpsPackageFragments();
        for (int k = 0; k < childFragments.length; k++) {
            collectChildren(childFragments[k], childList);
        }
    }
}
