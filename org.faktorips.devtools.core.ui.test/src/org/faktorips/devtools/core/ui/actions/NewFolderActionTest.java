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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class NewFolderActionTest extends AbstractIpsPluginTest {

    private NewFolderAction action = new NewFolderAction(null, null);

    private IIpsProject proj;
    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragment modelPackage;
    private IIpsPackageFragment emptyPackage;

    private IFolder folder;
    private IFolder subFolder;
    private IFile file;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        proj = newIpsProject("TestProject");

        // Content of proj
        root = proj.getIpsPackageFragmentRoots()[0];
        modelPackage = root.createPackageFragment("subpackage.model", true, null);
        emptyPackage = root.createPackageFragment("subpackage.model.emptypackage", true, null);

        folder = ((IProject)proj.getCorrespondingResource()).getFolder("testfolder");
        folder.create(true, false, null);
        subFolder = folder.getFolder("subfolder");
        subFolder.create(true, false, null);
        file = folder.getFile("test.txt");
        file.create(null, true, null);
    }

    public void testCreateFolder() {
        // --- Tests for IpsPackageFragments ---
        IFolder targetFolder = ((IFolder)emptyPackage.getCorrespondingResource()).getFolder("toBeCreated");
        assertFalse(targetFolder.exists());

        // create handle by dot-separated folder/pathname
        action.createFolder((IFolder)root.getCorrespondingResource(), "subpackage.model.emptypackage.toBeCreated");
        assertTrue(targetFolder.exists());

        // create handle for existing folder (subfolder)
        action.createFolder((IFolder)root.getCorrespondingResource(), "subpackage.model");
        assertTrue(targetFolder.exists());

        // test creation of parentfolders
        IFolder newPackage = ((IFolder)root.getCorrespondingResource()).getFolder("newPackage");
        IFolder newSubPackage = newPackage.getFolder("newSubPackage");
        targetFolder = newSubPackage.getFolder("toBeCreated");
        assertFalse(newPackage.exists());
        assertFalse(newSubPackage.exists());
        assertFalse(targetFolder.exists());
        action.createFolder((IFolder)root.getCorrespondingResource(), "newPackage.newSubPackage.toBeCreated");
        assertTrue(targetFolder.exists());
        assertTrue(newSubPackage.exists());
        assertEquals(newSubPackage, targetFolder.getParent());
        assertTrue(newPackage.exists());
        assertEquals(newPackage, targetFolder.getParent().getParent());

        // --- Tests for IResources ---
        targetFolder = subFolder.getFolder("toBeCreated");
        assertFalse(targetFolder.exists());

        // create handle by dot-separated folder/pathname
        action.createFolder(folder, "subfolder.toBeCreated");
        assertTrue(targetFolder.exists());

        // create handle for existing folder (subfolder)
        action.createFolder(folder, "subfolder");
        assertTrue(targetFolder.exists());

        // test creation of parentfolders
        IFolder newFolder = folder.getFolder("newFolder");
        IFolder newSubFolder = newFolder.getFolder("newSubFolder");
        targetFolder = newSubFolder.getFolder("toBeCreated");
        assertFalse(newFolder.exists());
        assertFalse(newSubFolder.exists());
        assertFalse(targetFolder.exists());
        action.createFolder(folder, "newFolder.newSubFolder.toBeCreated");
        assertTrue(targetFolder.exists());
        assertTrue(newSubFolder.exists());
        assertEquals(newSubFolder, targetFolder.getParent());
        assertTrue(newFolder.exists());
        assertEquals(newFolder, targetFolder.getParent().getParent());

    }

    public void testGetFolder() {
        // --- Tests for IpsPackageFragments ---
        IFolder targetFolder = ((IFolder)emptyPackage.getCorrespondingResource()).getFolder("toBeCreated");
        assertFalse(targetFolder.exists());

        // create handle by dot-separated folder/pathname
        IFolder newFolder = action.getFolder((IFolder)root.getCorrespondingResource(),
                "subpackage.model.emptypackage.toBeCreated");
        assertFalse(newFolder.exists());
        assertEquals(targetFolder, newFolder);

        // create handle for existing folder (subfolder)
        newFolder = action.getFolder((IFolder)root.getCorrespondingResource(), "subpackage.model");
        assertTrue(newFolder.exists());
        assertEquals(modelPackage.getCorrespondingResource(), newFolder);

        // --- Tests for IResources ---
        targetFolder = subFolder.getFolder("toBeCreated");
        assertFalse(targetFolder.exists());

        // create handle by dot-separated folder/pathname
        newFolder = action.getFolder(folder, "subfolder.toBeCreated");
        assertFalse(newFolder.exists());
        assertEquals(targetFolder, newFolder);

        // create handle for existing folder (subfolder)
        newFolder = action.getFolder(folder, "subfolder");
        assertTrue(newFolder.exists());
        assertEquals(subFolder, newFolder);
    }
}
