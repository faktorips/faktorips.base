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
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.abstraction.AFile;
import org.faktorips.devtools.model.abstraction.AFolder;
import org.faktorips.devtools.model.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.junit.Before;
import org.junit.Test;

public class IpsPackageFragmentRootTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsPackageFragmentRoot ipsRoot;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        ipsRoot = (IpsPackageFragmentRoot)ipsProject.getIpsPackageFragmentRoots()[0];
    }

    @Test
    public void testGetIpsObjectPathEntry() {
        IIpsObjectPathEntry entry = ipsRoot.getIpsObjectPathEntry();
        assertNotNull(entry);
    }

    @Test
    public void testGetArtefactDestination() throws CoreRuntimeException {
        APackageFragmentRoot destination = ipsRoot.getArtefactDestination(false);
        assertNotNull(destination);
        IIpsSrcFolderEntry srcEntry = ipsProject.getIpsObjectPath().getSourceFolderEntries()[0];
        Path outputPath = srcEntry.getOutputFolderForMergableJavaFiles().getWorkspaceRelativePath();
        assertEquals(outputPath, destination.getPath());
        destination = ipsRoot.getArtefactDestination(true);
        Path outputPathDerived = srcEntry.getOutputFolderForDerivedJavaFiles().getWorkspaceRelativePath();
        assertEquals(outputPathDerived, destination.getPath());
    }

    @Test
    public void testGetIpsProject() {
        assertEquals(ipsProject, ipsRoot.getIpsProject());
    }

    @Test
    public void testGetIpsPackageFragments() throws CoreRuntimeException {
        IIpsPackageFragment defaultFolder = ipsRoot.getIpsPackageFragment("");
        assertEquals(1, ipsRoot.getIpsPackageFragments().length);
        assertEquals(defaultFolder, ipsRoot.getIpsPackageFragments()[0]);

        IIpsPackageFragment folderA = ipsRoot.createPackageFragment("a", true, null);
        IIpsPackageFragment folderB = ipsRoot.createPackageFragment("a.b", true, null);
        IIpsPackageFragment folderC = ipsRoot.createPackageFragment("c", true, null);
        IIpsPackageFragment folderD = ipsRoot.createPackageFragment("c.d", true, null);

        IIpsElement[] children = ipsRoot.getIpsPackageFragments();

        assertEquals(5, ipsRoot.getIpsPackageFragments().length);
        assertEquals(defaultFolder, children[0]);
        assertEquals(folderA, children[1]);
        assertEquals(folderB, children[2]);
        assertEquals(folderC, children[3]);
        assertEquals(folderD, children[4]);

        // test if folders that aren't packages because they don't adhere to the naming convention
        // are igored
        AFolder invalidPack = ((AFolder)ipsRoot.getCorrespondingResource()).getFolder("invalid package");
        invalidPack.create(null);
        children = ipsRoot.getIpsPackageFragments();
        assertEquals(5, ipsRoot.getIpsPackageFragments().length);
    }

    @Test
    public void testGetIpsPackageFragment() {
        IIpsPackageFragment f = ipsRoot.getIpsPackageFragment("folder");
        assertFalse(f.exists());
    }

    @Test
    public void testCreatePackageFragment() throws CoreRuntimeException {
        IIpsPackageFragment f = ipsRoot.createPackageFragment("a.b", true, null);
        assertTrue(f.exists());
        assertEquals(ipsRoot, f.getParent());
        AFolder folderB = (AFolder)f.getCorrespondingResource();
        assertTrue(folderB.exists());
        assertEquals("b", folderB.getName());
        AFolder folderA = (AFolder)folderB.getParent();
        assertNotNull(folderA);
        assertTrue(folderA.exists());
        assertEquals("a", folderA.getName());
    }

    @Test
    public void testGetCorrespondingResource() {
        AFolder folder = (AFolder)ipsRoot.getCorrespondingResource();
        assertTrue(folder.exists());
    }

    @Test
    public void testExists() throws CoreRuntimeException {
        assertTrue(ipsRoot.exists());
        IIpsPackageFragmentRoot root2 = ipsProject.getIpsPackageFragmentRoot("unknown");
        assertFalse(root2.exists());
        AFolder corrFolder2 = (AFolder)root2.getCorrespondingResource();
        corrFolder2.create(null);
        assertFalse(root2.exists());

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newSourceFolderEntry(corrFolder2);
        ipsProject.setIpsObjectPath(path);
        assertTrue(root2.exists());
    }

    @Test
    public void testGetIpsObject() throws CoreRuntimeException {
        IIpsPackageFragment pack = ipsRoot.createPackageFragment("a.b", true, null);
        IIpsSrcFile file = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Test", true, null);
        IIpsObject ipsObject = ipsRoot.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "a.b.Test");
        assertNotNull(ipsObject);
        assertEquals(file.getIpsObject(), ipsObject);

        assertNull(ipsRoot.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "c.Unknown"));
    }

    @Test
    public void testFindIpsObjectsStartingWith() throws CoreRuntimeException {
        IIpsObject ob1 = newIpsObject(ipsRoot, IpsObjectType.POLICY_CMPT_TYPE, "pack1.MotorPolicy");
        IIpsObject ob2 = newIpsObject(ipsRoot, IpsObjectType.POLICY_CMPT_TYPE, "pack2.MotorCoverage");

        IIpsSrcFile file1 = ob1.getIpsSrcFile();
        IIpsSrcFile file2 = ob2.getIpsSrcFile();

        ArrayList<IIpsSrcFile> result = new ArrayList<>();
        ipsRoot.findIpsSourceFilesStartingWithInternal(IpsObjectType.POLICY_CMPT_TYPE, "MotorP", false, result);
        assertEquals(1, result.size());
        assertTrue(result.contains(file1));

        result.clear();
        ipsRoot.findIpsSourceFilesStartingWithInternal(IpsObjectType.POLICY_CMPT_TYPE, "Motor", false, result);
        assertEquals(2, result.size());
        assertTrue(result.contains(file1));
        assertTrue(result.contains(file2));

        // root does not exist
        IpsPackageFragmentRoot root2 = (IpsPackageFragmentRoot)ipsProject.getIpsPackageFragmentRoot("notExistingRoot");
        result.clear();
        root2.findIpsSourceFilesStartingWithInternal(IpsObjectType.POLICY_CMPT_TYPE, "Motor", false, result);
        assertEquals(0, result.size());

        // ipsobjecttype null
        try {
            ipsRoot.findIpsSourceFilesStartingWithInternal(null, "M", true, result);
            fail();
        } catch (NullPointerException e) {
        }

        // prefix null
        try {
            ipsRoot.findIpsSourceFilesStartingWithInternal(IpsObjectType.POLICY_CMPT_TYPE, null, true, result);
            fail();
        } catch (NullPointerException e) {
        }

        // result null
        try {
            ipsRoot.findIpsSourceFilesStartingWithInternal(IpsObjectType.POLICY_CMPT_TYPE, "M", true, null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetIpsDefaultPackageFragment() {
        IIpsPackageFragment def = ipsRoot.getDefaultIpsPackageFragment();
        assertEquals(def.getName(), "");
    }

    @Test
    public void testGetNonIpsResources() throws CoreRuntimeException {
        IIpsPackageFragment fragment = ipsRoot.createPackageFragment("fragment", true, null);
        IIpsPackageFragment subFragment = ipsRoot.createPackageFragment("fragment.sub", true, null);

        AFolder rootHandle = (AFolder)ipsRoot.getCorrespondingResource();
        AFile nonIpsFile = rootHandle.getFile("nonIpsFile");
        nonIpsFile.create(null, null);
        AFile nonIpsFile2 = rootHandle.getFile("nonIpsFile2");
        nonIpsFile2.create(null, null);

        Object[] nonIpsResources = ipsRoot.getNonIpsResources();
        assertEquals(2, nonIpsResources.length);
        List<?> list = Arrays.asList(nonIpsResources);
        assertTrue(list.contains(nonIpsFile));
        assertTrue(list.contains(nonIpsFile2));
        assertFalse(list.contains(fragment));
        assertFalse(list.contains(subFragment));
    }

    @Test
    public void testDelete() throws CoreRuntimeException {
        IIpsPackageFragment childPackage = ipsRoot.createPackageFragment("foo", true, null);

        ipsRoot.delete();

        assertFalse(childPackage.exists());
        assertFalse(ipsRoot.exists());
    }

}
