/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;

public class IpsPackageFragmentRootTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsPackageFragmentRoot ipsRoot;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        ipsRoot = (IpsPackageFragmentRoot)ipsProject.getIpsPackageFragmentRoots()[0];
    }

    public void testGetIpsObjectPathEntry() throws CoreException {
        IIpsObjectPathEntry entry = ipsRoot.getIpsObjectPathEntry();
        assertNotNull(entry);
    }

    public void testGetArtefactDestination() throws CoreException {
        IFolder destination = ipsRoot.getArtefactDestination(false);
        assertNotNull(destination);
        IIpsSrcFolderEntry srcEntry = ipsProject.getIpsObjectPath().getSourceFolderEntries()[0];
        IPath outputPath = srcEntry.getOutputFolderForMergableJavaFiles().getProjectRelativePath();
        assertEquals(outputPath, destination.getProjectRelativePath());
        destination = ipsRoot.getArtefactDestination(true);
        IPath outputPathDerived = srcEntry.getOutputFolderForDerivedJavaFiles().getProjectRelativePath();
        assertEquals(outputPathDerived, destination.getProjectRelativePath());
    }

    public void testGetIpsProject() {
        assertEquals(ipsProject, ipsRoot.getIpsProject());
    }

    public void testGetIpsPackageFragments() throws CoreException {
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
        IFolder invalidPack = ((IFolder)ipsRoot.getCorrespondingResource()).getFolder("invalid package");
        invalidPack.create(true, false, null);
        children = ipsRoot.getIpsPackageFragments();
        assertEquals(5, ipsRoot.getIpsPackageFragments().length);
    }

    public void testGetIpsPackageFragment() {
        IIpsPackageFragment f = ipsRoot.getIpsPackageFragment("folder");
        assertFalse(f.exists());
    }

    public void testCreatePackageFragment() throws CoreException {
        IIpsPackageFragment f = ipsRoot.createPackageFragment("a.b", true, null);
        assertTrue(f.exists());
        assertEquals(ipsRoot, f.getParent());
        IFolder folderB = (IFolder)f.getCorrespondingResource();
        assertTrue(folderB.exists());
        assertEquals("b", folderB.getName());
        IFolder folderA = (IFolder)folderB.getParent();
        assertTrue(folderA.exists());
        assertEquals("a", folderA.getName());
    }

    public void testGetCorrespondingResource() {
        IFolder folder = (IFolder)ipsRoot.getCorrespondingResource();
        assertTrue(folder.exists());
    }

    public void testGetChildren() {
    }

    public void testHasChildren() {
    }

    public void testExists() throws CoreException {
        assertTrue(ipsRoot.exists());
        IIpsPackageFragmentRoot root2 = ipsProject.getIpsPackageFragmentRoot("unknown");
        assertFalse(root2.exists());
        IFolder corrFolder2 = (IFolder)root2.getCorrespondingResource();
        corrFolder2.create(true, true, null);
        assertFalse(root2.exists());

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newSourceFolderEntry(corrFolder2);
        ipsProject.setIpsObjectPath(path);
        assertTrue(root2.exists());
    }

    public void testGetIpsObject() throws CoreException {
        IIpsPackageFragment pack = ipsRoot.createPackageFragment("a.b", true, null);
        IIpsSrcFile file = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Test", true, null);
        IIpsObject ipsObject = ipsRoot.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "a.b.Test");
        assertNotNull(ipsObject);
        assertEquals(file.getIpsObject(), ipsObject);

        assertNull(ipsRoot.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "c.Unknown"));
    }

    public void testFindIpsObjectsStartingWith() throws CoreException {
        IIpsObject ob1 = newIpsObject(ipsRoot, IpsObjectType.POLICY_CMPT_TYPE, "pack1.MotorPolicy");
        IIpsObject ob2 = newIpsObject(ipsRoot, IpsObjectType.POLICY_CMPT_TYPE, "pack2.MotorCoverage");

        IIpsSrcFile file1 = ob1.getIpsSrcFile();
        IIpsSrcFile file2 = ob2.getIpsSrcFile();

        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
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

    public void testGetIpsDefaultPackageFragment() {
        IIpsPackageFragment def = ipsRoot.getDefaultIpsPackageFragment();
        assertEquals(def.getName(), "");
    }

    public void testGetNonIpsResources() throws CoreException {
        IIpsPackageFragment fragment = ipsRoot.createPackageFragment("fragment", true, null);
        IIpsPackageFragment subFragment = ipsRoot.createPackageFragment("fragment.sub", true, null);

        IFolder rootHandle = (IFolder)ipsRoot.getCorrespondingResource();
        IFile nonIpsFile = rootHandle.getFile("nonIpsFile");
        nonIpsFile.create(null, true, null);
        IFile nonIpsFile2 = rootHandle.getFile("nonIpsFile2");
        nonIpsFile2.create(null, true, null);

        Object[] nonIpsResources = ipsRoot.getNonIpsResources();
        assertEquals(2, nonIpsResources.length);
        List<?> list = Arrays.asList(nonIpsResources);
        assertTrue(list.contains(nonIpsFile));
        assertTrue(list.contains(nonIpsFile2));
        assertFalse(list.contains(fragment));
        assertFalse(list.contains(subFragment));
    }

    public void testGetSortedIpsPackageFragments() throws CoreException, IOException {
        IIpsPackageFragment defaultFolder = ipsRoot.getIpsPackageFragment("");

        IIpsPackageFragment[] children = ipsRoot.getSortedIpsPackageFragments();
        assertEquals(children.length, 1);
        assertEquals(defaultFolder, children[0]);

        ipsRoot.createPackageFragment("hausrat", true, null);
        IIpsPackageFragment kranken = ipsRoot.createPackageFragment("kranken", true, null);
        ipsRoot.createPackageFragment("kranken.leistungsarten", true, null);
        ipsRoot.createPackageFragment("kranken.vertragsarten", true, null);
        ipsRoot.createPackageFragment("kranken.gruppenarten", true, null);
        ipsRoot.createPackageFragment("unfall", true, null);
        ipsRoot.createPackageFragment("haftpflicht", true, null);

        ArrayList<String> strings = new ArrayList<String>();
        strings.add("kranken");
        strings.add("unfall");
        strings.add("hausrat");
        strings.add("haftpflicht");

        createPackageOrderFile((IFolder)ipsRoot.getCorrespondingResource(), strings);

        strings.clear();
        strings.add("vertragsarten");
        strings.add("gruppenarten");
        strings.add("leistungsarten");

        createPackageOrderFile((IFolder)kranken.getCorrespondingResource(), strings);

        // sorted: valid files and entries
        children = ipsRoot.getSortedIpsPackageFragments();
        assertEquals(children.length, 8);
        assertEquals(children[0].getName(), "");
        assertEquals(children[1].getName(), "kranken");
        assertEquals(children[2].getName(), "kranken.vertragsarten");
        assertEquals(children[3].getName(), "kranken.gruppenarten");
        assertEquals(children[4].getName(), "kranken.leistungsarten");
        assertEquals(children[5].getName(), "unfall");
        assertEquals(children[6].getName(), "hausrat");
        assertEquals(children[7].getName(), "haftpflicht");
    }
}
