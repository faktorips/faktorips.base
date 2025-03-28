/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Locale;
import java.util.UUID;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsElementDropListenerTest extends AbstractIpsPluginTest {

    @Test
    public void testGetTransferedElements() {
        IIpsProject prj = super.newIpsProject();
        PolicyCmptType type = newPolicyCmptType(prj, "first.second.Name");

        AFolder folder = prj.getProject().getFolder("Folder");
        AFile file = prj.getProject().getFile("File");
        folder.create(new NullProgressMonitor());
        file.create(new ByteArrayInputStream("Content".getBytes()), new NullProgressMonitor());

        TransferData notFound = getData();
        TransferData folderData = getData();
        TransferData pack = getData();
        TransferData fileData = getData();
        TransferData typeData = getData();

        FileTransfer mockedTransfer = mock(FileTransfer.class);
        when(mockedTransfer.nativeToJava(notFound)).thenReturn(new String[] { "FAIL" });
        when(mockedTransfer.nativeToJava(folderData)).thenReturn(new String[] { getPath(folder) });
        when(mockedTransfer.nativeToJava(pack)).thenReturn(
                new String[] { getPath(type.getIpsPackageFragment().getCorrespondingResource()) });
        when(mockedTransfer.nativeToJava(fileData)).thenReturn(new String[] { getPath(file) });
        when(mockedTransfer.nativeToJava(typeData)).thenReturn(new String[] { getPath(type.getEnclosingResource()) });

        TestListener l = new TestListener(mockedTransfer);

        Object[] resultNotFound = l.getTransferedElements(notFound);
        Object[] resultFolder = l.getTransferedElements(folderData);
        Object[] resultPackage = l.getTransferedElements(pack);
        Object[] resultFile = l.getTransferedElements(fileData);
        Object[] resultType = l.getTransferedElements(typeData);

        assertEquals(0, resultNotFound.length);
        assertEquals(1, resultFolder.length);
        assertTrue(resultFolder[0] instanceof IContainer);
        assertEquals(1, resultPackage.length);
        assertTrue(resultPackage[0] instanceof IIpsElement);
        assertEquals(1, resultFile.length);
        assertTrue(resultFile[0] instanceof IFile);
        assertEquals(1, resultType.length);
        assertTrue(resultType[0] instanceof IIpsElement);

    }

    @Test
    public void testGetTransferedElements_nestedProjects() throws CoreException {
        String parent = "P" + UUID.randomUUID().toString();
        String child = "X" + UUID.randomUUID().toString();
        IProject parentProject;
        IFolder childFolder;
        parentProject = newPlatformProject(parent).unwrap();
        childFolder = parentProject.getFolder(child);
        childFolder.create(false, true, null);

        IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(child);
        description.setLocation(childFolder.getLocation());
        IIpsProject childProject = newIpsProjectBuilder().name(child).description(description)
                .supportedLocales(Locale.GERMAN).build();

        PolicyCmptType type = newPolicyCmptType(childProject, "first.second.Name");

        TransferData typeData = getData();
        FileTransfer mockedTransfer = mock(FileTransfer.class);
        when(mockedTransfer.nativeToJava(typeData))
                .thenReturn(new String[] { getPath(type.getEnclosingResource()) });
        TestListener l = new TestListener(mockedTransfer);
        Object[] resultType = l.getTransferedElements(typeData);

        assertEquals(1, resultType.length);
        assertTrue(resultType[0] instanceof IIpsElement);
    }

    private String getPath(AResource res) {
        return res.getLocation().toString();
    }

    private TransferData getData() {
        return FileTransfer.getInstance().getSupportedTypes()[0];
    }

    private static class TestListener extends IpsElementDropListener {

        private FileTransfer transfer;

        private TestListener(FileTransfer transfer) {
            this.transfer = transfer;
        }

        @Override
        public void dragEnter(DropTargetEvent event) {
            // nothing to do
        }

        @Override
        public void drop(DropTargetEvent event) {
            // nothing to do
        }

        @Override
        public void dropAccept(DropTargetEvent event) {
            // nothing to do
        }

        @Override
        protected FileTransfer getTransfer() {
            return transfer;
        }

        @Override
        public int getSupportedOperations() {
            return 0;
        }

    }

}
