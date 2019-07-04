/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class LibraryIpsPackageFragmentRootTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IpsArchiveEntry entry;
    private IFile archiveFile;
    private LibraryIpsPackageFragmentRoot root;
    private IPolicyCmptType type;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject archiveProject = newIpsProject("ArchiveProject");
        type = newPolicyCmptType(archiveProject, "motor.Policy");
        type.getIpsSrcFile().save(true, null);
        newPolicyCmptTypeWithoutProductCmptType(archiveProject, "motor.collision.CollisionCoverage").getIpsSrcFile()
                .save(true, null);
        newProductCmpt(archiveProject, "motor.MotorProduct").getIpsSrcFile().save(true, null);

        project = newIpsProject();
        archiveFile = project.getProject().getFile("test.ipsar");

        createArchive(archiveProject, archiveFile);

        IIpsObjectPath path = project.getIpsObjectPath();
        entry = (IpsArchiveEntry)path.newArchiveEntry(archiveFile.getFullPath());
        project.setIpsObjectPath(path);
        root = (LibraryIpsPackageFragmentRoot)project.getIpsPackageFragmentRoots()[1];
    }

    @Test
    public void testExists_ArchiveInSameProject() throws CoreException {
        assertTrue(root.exists());
        archiveFile.delete(true, null);
        assertFalse(root.exists());
    }

    @Test
    public void testExists_ArchiveInWorkspaceButDifferentProject() throws CoreException {
        IIpsProject project2 = newIpsProject("Project2");
        IIpsObjectPath path2 = project2.getIpsObjectPath();
        entry = (IpsArchiveEntry)path2.newArchiveEntry(archiveFile.getFullPath());
        project2.setIpsObjectPath(path2);
        root = (LibraryIpsPackageFragmentRoot)project2.getIpsPackageFragmentRoots()[1];

        assertTrue(root.exists());
        archiveFile.delete(true, null);
        assertFalse(root.exists());
    }

    @Test
    public void testExists_ArchiveOutsideWorkspace() throws Exception {

        File externalArchiveFile = File.createTempFile("externalArchiveFile", ".ipsar");
        externalArchiveFile.deleteOnExit();
        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(project, externalArchiveFile);
        ResourcesPlugin.getWorkspace().run(op, null);
        IPath externalArchivePath = new Path(externalArchiveFile.getAbsolutePath());

        IIpsObjectPath path = project.getIpsObjectPath();
        entry = (IpsArchiveEntry)path.newArchiveEntry(externalArchivePath);
        project.setIpsObjectPath(path);
        root = (LibraryIpsPackageFragmentRoot)project.getIpsPackageFragmentRoots()[2];

        assertTrue(root.exists());

        externalArchiveFile.delete();
        assertFalse(root.exists());
    }

    @Test
    public void testGetIpsObjectPathEntry() {
        assertEquals(entry.getArchiveLocation(), root.getIpsStorage().getLocation());
    }

    @Test
    public void testGetParent() {
        assertEquals(project, root.getParent());
    }

    @Test
    public void testGetIpsPackageFragments() throws CoreException {
        IIpsPackageFragment[] packs = root.getIpsPackageFragments();
        assertEquals(2, packs.length);
        assertEquals("motor", packs[0].getName());
        assertEquals("motor.collision", packs[1].getName());
    }

    @Test
    public void testGetNonIpsResources() throws CoreException {
        IResource[] res = root.getNonIpsResources();
        assertEquals(0, res.length);
    }

    @Test
    public void testGetCorrespondingResource() {
        assertEquals(archiveFile, root.getCorrespondingResource());
    }

    @Test
    public void testGetEnclosingResource() {
        assertEquals(archiveFile, root.getEnclosingResource());
    }

    @Test
    public void testFindIpsSourceFiles() throws CoreException {
        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        root.findIpsSourceFiles(IpsObjectType.POLICY_CMPT_TYPE, null, result);
        assertEquals(2, result.size());
        List<QualifiedNameType> qualifiedNameTypes = new ArrayList<QualifiedNameType>();
        for (IIpsSrcFile pcTypeSrcFile : result) {
            qualifiedNameTypes.add(pcTypeSrcFile.getQualifiedNameType());
        }
        assertTrue(qualifiedNameTypes.contains(new QualifiedNameType("motor.Policy", IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qualifiedNameTypes
                .contains(new QualifiedNameType("motor.collision.CollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE)));

        result = new ArrayList<IIpsSrcFile>();
        root.findIpsSourceFiles(IpsObjectType.PRODUCT_CMPT_TYPE, null, result);
        assertEquals(1, result.size());

        result = new ArrayList<IIpsSrcFile>();
        root.findIpsSourceFiles(IpsObjectType.PRODUCT_CMPT, null, result);
        assertEquals(1, result.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDelete() throws CoreException {
        root.delete();
    }

    @Test
    public void testEquals() throws Exception {
        assertTrue(root.equals(root));

        LibraryIpsPackageFragmentRoot sameAsRootOtherProject = new LibraryIpsPackageFragmentRoot(newIpsProject(),
                root.getIpsStorage());

        assertTrue(root.equals(sameAsRootOtherProject));
        assertEquals(root.hashCode(), sameAsRootOtherProject.hashCode());
    }

    @Test
    public void testEquals_sameNameNotEqual() throws Exception {
        assertTrue(root.equals(root));

        IIpsProject otherProject = newIpsProject();
        IIpsObjectPath path = otherProject.getIpsObjectPath();
        IFile otherArchiveFile = otherProject.getProject().getFile("test.ipsar");
        IpsArchiveEntry otherEntry = (IpsArchiveEntry)path.newArchiveEntry(otherArchiveFile.getFullPath());
        otherProject.setIpsObjectPath(path);

        LibraryIpsPackageFragmentRoot otherProjectRoot = new LibraryIpsPackageFragmentRoot(otherProject,
                otherEntry.getIpsStorage());

        assertFalse(root.equals(otherProjectRoot));
        assertEquals(root.hashCode(), otherProjectRoot.hashCode());
    }

}
