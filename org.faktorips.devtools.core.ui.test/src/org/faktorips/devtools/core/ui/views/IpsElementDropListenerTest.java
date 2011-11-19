/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsElementDropListenerTest extends AbstractIpsPluginTest {

    @Test
    public void testGetTransferedElements() throws CoreException {
        IIpsProject prj = super.newIpsProject();
        PolicyCmptType type = newPolicyCmptType(prj, "first.second.Name");

        IFolder folder = prj.getProject().getFolder("Folder");
        folder.create(true, true, new NullProgressMonitor());

        IFile file = prj.getProject().getFile("File");
        file.create(new ByteArrayInputStream("Content".getBytes()), true, new NullProgressMonitor());

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

    private String getPath(IResource res) {
        return res.getRawLocation().toOSString();
    }

    private TransferData getData() {
        return FileTransfer.getInstance().getSupportedTypes()[0];
    }

    private class TestListener extends IpsElementDropListener {

        private FileTransfer transfer;

        private TestListener(FileTransfer transfer) {
            this.transfer = transfer;
        }

        @Override
        public void dragEnter(DropTargetEvent event) {
        }

        @Override
        public void drop(DropTargetEvent event) {
        }

        @Override
        public void dropAccept(DropTargetEvent event) {
        }

        @Override
        protected FileTransfer getTransfer() {
            return transfer;
        }

    }

}
