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

import static org.faktorips.devtools.model.abstraction.mapping.PathMapping.toEclipsePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.abstraction.AFile;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class LibraryIpsPackageFragmentTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private AFile archiveFile;
    private LibraryIpsPackageFragmentRoot root;
    private LibraryIpsPackageFragment pack;
    private IPolicyCmptType policy;
    private IPolicyCmptType coverage;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject archiveProject = newIpsProject("ArchiveProject");
        policy = newPolicyCmptTypeWithoutProductCmptType(archiveProject, "mycompany.motor.Policy");
        coverage = newPolicyCmptTypeWithoutProductCmptType(archiveProject, "mycompany.motor.Coverage");
        newPolicyCmptTypeWithoutProductCmptType(archiveProject, "mycompany.motor.collision.CollisionCoverage");

        project = newIpsProject();
        archiveFile = project.getProject().getFile("test.ipsar");

        createArchive(archiveProject, archiveFile);

        IIpsObjectPath path = project.getIpsObjectPath();
        path.newArchiveEntry(toEclipsePath(archiveFile.getWorkspaceRelativePath()));
        project.setIpsObjectPath(path);
        root = (LibraryIpsPackageFragmentRoot)project.getIpsPackageFragmentRoots()[1];
        pack = (LibraryIpsPackageFragment)root.getIpsPackageFragment("mycompany.motor");
    }

    @Test
    public void testGetChildren() throws CoreRuntimeException {
        IIpsElement[] children = pack.getChildren();
        assertEquals(2, children.length);
        assertTrue((children[0] instanceof IIpsSrcFile));
        assertTrue((children[1] instanceof IIpsSrcFile));
    }

    @Test
    public void testGetIpsSrcFile() {
        IIpsSrcFile file = pack.getIpsSrcFile(policy.getIpsSrcFile().getName());
        assertNotNull(file);
        assertEquals(pack, file.getParent());
    }

    @Test
    public void testFindIpsObjectsByIpsObjectType() throws CoreRuntimeException {
        List<IIpsObject> result = new ArrayList<>();
        pack.findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE, result);
        assertEquals(2, result.size());
        IIpsObject obj = project.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "mycompany.motor.Policy");
        assertTrue(result.contains(obj));
        obj = project.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "mycompany.motor.Coverage");
        assertTrue(result.contains(obj));
    }

    @Test
    public void testFindIpsObjects() throws CoreRuntimeException {
        List<IIpsObject> result = new ArrayList<>();
        pack.findIpsObjects(result);
        assertEquals(2, result.size());
        List<QualifiedNameType> qnts = new ArrayList<>();
        for (IIpsObject ipsObject : result) {
            qnts.add(ipsObject.getQualifiedNameType());
        }

        assertTrue(qnts.contains(policy.getQualifiedNameType()));
        assertTrue(qnts.contains(coverage.getQualifiedNameType()));
    }

    @Test
    public void testGetChildIpsPackageFragments() throws CoreRuntimeException {
        IIpsPackageFragment[] packs = pack.getChildIpsPackageFragments();
        assertEquals(1, packs.length);
        assertEquals("mycompany.motor.collision", packs[0].getName());
    }

    @Test
    public void testGetNonIpsResources() throws CoreRuntimeException {
        assertEquals(0, pack.getNonIpsResources().length);
    }

    @Test
    public void testGetCorrespondingResource() {
        assertNull(pack.getCorrespondingResource());
    }

    @Test
    public void testGetEnclosingResource() {
        assertEquals(archiveFile, pack.getEnclosingResource());
    }

    @Test
    public void testGetParent() {
        assertEquals(root, pack.getParent());
    }

    @Test
    public void testExists() {
        assertTrue(pack.exists());
        assertFalse(root.getIpsPackageFragment("unknownPack").exists());
    }

    @Test
    public void testHasChildIpsPackageFragments() throws CoreRuntimeException {
        IIpsPackageFragment empty = root.getIpsPackageFragment("mycompany.motor.Coverage");
        assertFalse(empty.hasChildIpsPackageFragments());

        assertTrue(pack.hasChildIpsPackageFragments());
        assertTrue(root.getDefaultIpsPackageFragment().hasChildIpsPackageFragments());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDelete() throws CoreRuntimeException {
        pack.delete();
    }

}
