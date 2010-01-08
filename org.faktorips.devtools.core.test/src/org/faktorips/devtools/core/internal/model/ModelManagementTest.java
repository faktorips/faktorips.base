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

package org.faktorips.devtools.core.internal.model;

import java.io.File;
import java.io.FileWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.util.StringUtil;

/**
 * Test f�r threading issues (we hopefully once HAD).
 * 
 * @author Jan Ortmann
 */
public class ModelManagementTest extends AbstractIpsPluginTest {

    private IPolicyCmptType type;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IWorkspaceRunnable action = new IWorkspaceRunnable() {

            public void run(IProgressMonitor monitor) throws CoreException {
                IIpsProject project = newIpsProject("TestProject");
                IFolder folder = (IFolder)project.getIpsPackageFragmentRoots()[0].getCorrespondingResource();
                IFile file = folder.getFile("A.ipspct");
                System.out.println("" + file + ", exists=" + file.exists());
                type = newPolicyCmptType(project, "A");
                type.newPolicyCmptTypeAssociation();
                type.getIpsSrcFile().save(true, null);
            }

        };
        ResourcesPlugin.getWorkspace().run(action, null);
    }

    public void test1() {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Start test1() =====");
        }
        assertEquals(1, type.getNumOfAssociations());
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Finished test1() =====");
        }
    }

    public void test2() {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Start test2() =====");
        }
        assertEquals(1, type.getNumOfAssociations());
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Finished test2() =====");
        }
    }

    public void testDirectChangesToTheCorrespondingFile() throws Exception {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Start testDirectChangesToTheCorrespondingFile() =====");
        }
        IIpsSrcFile ipsFile = type.getIpsSrcFile();
        type.setDescription("Blabla");
        ipsFile.save(true, null);
        String encoding = type.getIpsProject().getXmlFileCharset();
        IFile file = type.getIpsSrcFile().getCorrespondingFile();
        String content = StringUtil.readFromInputStream(file.getContents(), encoding);
        content = content.replaceAll("Blabla", "NewBlabla");
        file.setContents(StringUtil.getInputStreamForString(content, encoding), true, false, null);
        type = (IPolicyCmptType)ipsFile.getIpsObject(); // forces a reload
        assertEquals("NewBlabla", type.getDescription());
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Finished testDirectChangesToTheCorrespondingFile() =====");
        }
    }

    public void testChangeDirectlyOnDiskWithoutUsingTheEclipseApi() throws Exception {
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Start testChangeDirectlyOnDiskWithoutUsingTheEclipseApi() =====");
        }
        IIpsSrcFile ipsFile = type.getIpsSrcFile();
        type.setDescription("Blabla");
        ipsFile.save(true, null);
        Thread.sleep(2000); // wait for 2 seconds, so that the file definitly has a
        // different timestamp, otherwise refreshLocal won't refresh!
        // file timestamps (at least under windows xp) only differ in seconds, not milliseconds!
        String encoding = type.getIpsProject().getXmlFileCharset();
        IFile file = type.getIpsSrcFile().getCorrespondingFile();
        String content = StringUtil.readFromInputStream(file.getContents(), encoding);
        content = content.replaceAll("Blabla", "NewBlabla");
        File ioFile = file.getLocation().toFile();
        FileWriter writer = new FileWriter(ioFile);
        writer.write(content);
        writer.flush();
        writer.close();

        // before the refresh, object shouldn't be changed
        type = (IPolicyCmptType)ipsFile.getIpsObject();
        assertEquals("Blabla", type.getDescription());

        // now refresh the file from disk
        file.refreshLocal(IResource.DEPTH_INFINITE, null);
        System.out.println("ModStamp=" + file.getModificationStamp());

        type = (IPolicyCmptType)ipsFile.getIpsObject(); // forces a reload
        assertEquals("NewBlabla", type.getDescription());
        if (IpsModel.TRACE_MODEL_MANAGEMENT) {
            System.out.println("===== Finished testChangeDirectlyOnDiskWithoutUsingTheEclipseApi() =====");
        }
    }
}
