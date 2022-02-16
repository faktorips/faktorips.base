/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartState;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.type.IAttribute;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for IpsCutAction.
 * 
 * @author Thorsten Guenther
 */
public class IpsPasteActionTest extends AbstractIpsPluginTest {

    IIpsProject project;
    IpsCutAction cutAction;
    IpsPasteAction pasteAction;
    IAttribute attribute;
    IPolicyCmptType pcType;
    IPolicyCmptType pcType2;
    IIpsPackageFragment pack;
    IIpsPackageFragmentRoot root;
    IFolder folder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        project = this.newIpsProject();
        root = project.getIpsPackageFragmentRoots()[0];
        pack = root.createPackageFragment("products.folder", true, null);

        pcType = newPolicyCmptTypeWithoutProductCmptType(project, "products.folder.TestPolicy");
        attribute = pcType.newAttribute();

        pcType2 = newPolicyCmptTypeWithoutProductCmptType(project, "products.folder.TestPolicy2");

        AFile archiveFile = createIpsArchiveFile(root);
        IIpsObjectPath objectPath = project.getIpsObjectPath();
        objectPath.newArchiveEntry(archiveFile.getLocation());
        project.setIpsObjectPath(objectPath);

        folder = project.getProject().getFolder("testFolder").unwrap();
        folder.create(true, true, null);
    }

    private AFile createIpsArchiveFile(IIpsPackageFragmentRoot targetRoot) throws Exception {
        IIpsProject tempProject = this.newIpsProject();
        newPolicyCmptTypeWithoutProductCmptType(tempProject, "test.PolicyInArchive1");
        newPolicyCmptTypeWithoutProductCmptType(tempProject, "test.PolicyInArchive2");

        IIpsPackageFragmentRoot tempRoot = tempProject.getSourceIpsPackageFragmentRoots()[0];
        AFile archiveFile = targetRoot.getIpsProject().getProject().getFile("test.ipsar");
        createArchive(tempRoot, archiveFile);

        tempProject.getProject().delete(null);
        return archiveFile;
    }

    @Test
    public void testRun() throws CoreRuntimeException {
        cutAction = newIpsCutAction(attribute);
        pasteAction = newIpsPasteAction(pcType);

        String old = new IpsObjectPartState(attribute).toString();
        assertEquals(1, pcType.getNumOfAttributes());
        cutAction.run();
        assertEquals(0, pcType.getNumOfAttributes());
        pasteAction.run();
        assertEquals(1, pcType.getNumOfAttributes());
        assertEquals(old.replaceFirst("id=\"[a-z0-9-]+\"", "id=\"1\""), new IpsObjectPartState(pcType.getAttributes()
                .get(0)).toString().replaceFirst("id=\"[a-z0-9-]+\"", "id=\"1\""));

        IpsCopyAction copyAction = newIpsCopyAction(pack);
        pasteAction = newIpsPasteAction(root);
        copyAction.run();
        assertEquals(1, root.getDefaultIpsPackageFragment().getChildIpsPackageFragments().length);
        pasteAction.run();
        assertEquals(2, root.getDefaultIpsPackageFragment().getChildIpsPackageFragments().length);
    }

    @Test
    public void testCopyPasteIpsObject2IpsPackageFragment() throws CoreRuntimeException {
        IIpsPackageFragment fragment = root.createPackageFragment("testTarget", true, null);

        newIpsCopyAction(pcType).run();
        assertEquals(0, fragment.getChildren().length);

        newIpsPasteAction(fragment).run();
        assertEquals(1, fragment.getChildren().length);

        newIpsCopyAction(new IIpsElement[] { pcType, pcType2 }).run();
        newIpsPasteAction(fragment).run();
        assertEquals(3, fragment.getChildren().length);
    }

    @Test
    public void testCopyPasteIpsPackageFragment2IpsPackageFragment() throws CoreRuntimeException {
        IIpsPackageFragment fragment = root.createPackageFragment("testTarget", true, null);

        newIpsCopyAction(pack).run();
        assertEquals(0, fragment.getChildren().length);

        newIpsPasteAction(fragment).run();
        assertEquals(0, fragment.getChildren().length);
        assertEquals(1, fragment.getChildIpsPackageFragments().length);

        IIpsPackageFragment newFragment = fragment.getChildIpsPackageFragments()[0];
        assertEquals(2, newFragment.getChildren().length);
    }

    @Test
    public void testCopyPasteIpsObject2Folder() throws CoreException {
        newIpsCopyAction(pcType).run();
        assertEquals(0, folder.members().length);
        newIpsPasteAction(folder).run();
        assertEquals(1, folder.members().length);

        newIpsCopyAction(new IIpsElement[] { pcType, pcType2 }).run();
        newIpsPasteAction(folder).run();
        assertEquals(3, folder.members().length);

    }

    @Test
    public void testCopyPasteIpsPackageFragment2Folder() throws CoreException {
        newIpsCopyAction(pack).run();
        assertEquals(0, folder.members().length);

        newIpsPasteAction(folder).run();
        assertEquals(1, folder.members().length);
        assertEquals(2, ((IFolder)folder.members()[0]).members().length);
    }

    @Test
    public void testCopyPasteIpsObjectFromArchive2IpsPackageFragment() throws CoreRuntimeException {
        IIpsPackageFragment fragment = root.createPackageFragment("test", true, null);

        IIpsObject obj1 = project.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "test.PolicyInArchive1");
        IIpsObject obj2 = project.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "test.PolicyInArchive2");

        newIpsCopyAction(obj1).run();
        assertEquals(0, fragment.getChildren().length);

        newIpsPasteAction(fragment).run();
        assertEquals(1, fragment.getChildren().length);

        newIpsCopyAction(new IIpsElement[] { obj1, obj2 }).run();
        newIpsPasteAction(fragment).run();
        assertEquals(3, fragment.getChildren().length);
    }

    @Test
    public void testCopyPasteIpsPackageFragmentFromArchive2IpsPackageFragment() throws CoreRuntimeException {
        IIpsPackageFragment fragment = root.createPackageFragment("test", true, null);

        IIpsPackageFragmentRoot root = project.findIpsPackageFragmentRoot("test.ipsar");
        IIpsPackageFragment packageFrgmt = root.getIpsPackageFragment("test");

        newIpsCopyAction(packageFrgmt).run();
        assertEquals(0, fragment.getChildren().length);

        newIpsPasteAction(fragment).run();
        assertEquals(0, fragment.getChildren().length);
        assertEquals(1, fragment.getChildIpsPackageFragments().length);
        assertEquals(2, fragment.getChildIpsPackageFragments()[0].getChildren().length);
    }

    @Test
    public void testCopyPasteIpsObjectFromArchive2Folder() throws CoreException {
        IIpsObject obj1 = project.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "test.PolicyInArchive1");
        IIpsObject obj2 = project.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "test.PolicyInArchive2");

        newIpsCopyAction(obj1).run();
        assertEquals(0, folder.members().length);
        newIpsPasteAction(folder).run();
        assertEquals(1, folder.members().length);

        newIpsCopyAction(new IIpsElement[] { obj1, obj2 }).run();
        newIpsPasteAction(folder).run();
        assertEquals(3, folder.members().length);
    }

    @Test
    public void testCopyPasteIpsPackageFragmentFromArchive2Folder() throws CoreException {
        IIpsPackageFragmentRoot root = project.findIpsPackageFragmentRoot("test.ipsar");
        IIpsPackageFragment packageFrgmt = root.getIpsPackageFragment("test");

        newIpsCopyAction(packageFrgmt).run();
        assertEquals(0, folder.members().length);

        newIpsPasteAction(folder).run();
        assertEquals(1, folder.members().length);
        assertEquals(2, ((IFolder)folder.members()[0]).members().length);
    }

    @Test
    public void testCopyPasteUnknown() throws Exception {
        IIpsPackageFragment destFragment = root.createPackageFragment("testTarget", true, null);

        // create source file
        String folderPath = folder.getRawLocation().toOSString();
        File file = new File(folderPath + "/unknown.xyz");
        assertTrue(file.createNewFile());
        FileWriter writer = new FileWriter(file);
        writer.write("dummy test file");
        writer.close();

        // add source file to clipboard
        Clipboard clipboard = new Clipboard(IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell()
                .getDisplay());
        FileTransfer fileTransfer = FileTransfer.getInstance();
        Transfer[] transfers = new Transfer[] { fileTransfer };
        Object[] data = new Object[] { new String[] { file.getAbsolutePath() } };
        clipboard.setContents(data, transfers);

        // paste clipboard
        newIpsPasteAction(destFragment).run();
        assertEquals(0, destFragment.getChildren().length);
        clipboard.dispose();

        AFolder destFolder = (AFolder)destFragment.getEnclosingResource();
        assertEquals(1, destFolder.getMembers().size());
    }

    private IpsCopyAction newIpsCopyAction(Object selection) {
        return new IpsCopyAction(new TestSelectionProvider(selection), IpsPlugin.getDefault().getWorkbench()
                .getActiveWorkbenchWindow().getShell());
    }

    private IpsPasteAction newIpsPasteAction(Object selection) {
        IpsPasteAction pasteAction = new IpsPasteAction(new TestSelectionProvider(selection), IpsPlugin.getDefault()
                .getWorkbench().getActiveWorkbenchWindow().getShell());
        pasteAction.setForceUseNameSuggestionIfFileExists(true);
        return pasteAction;
    }

    private IpsCutAction newIpsCutAction(Object selection) {
        return new IpsCutAction(new TestSelectionProvider(selection), IpsPlugin.getDefault().getWorkbench()
                .getActiveWorkbenchWindow().getShell());
    }

    private class TestSelectionProvider implements ISelectionProvider {
        Object selected;

        public TestSelectionProvider(Object selected) {
            this.selected = selected;
        }

        @Override
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
        }

        @Override
        public ISelection getSelection() {
            if (selected instanceof IIpsElement[]) {
                return new StructuredSelection(Arrays.asList((IIpsElement[])selected));
            }
            return new StructuredSelection(selected);
        }

        @Override
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        }

        @Override
        public void setSelection(ISelection selection) {
        }
    }
}
