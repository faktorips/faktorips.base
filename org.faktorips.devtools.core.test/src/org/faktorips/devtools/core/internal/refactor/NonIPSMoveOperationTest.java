/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class NonIPSMoveOperationTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IFolder folderSource;
    private IFolder folderTarget;
    private IFile file1;
    private IFile file2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        IProject project = ipsProject.getProject();
        folderSource = project.getFolder("source");
        folderTarget = project.getFolder("target");
        folderSource.create(true, true, null);
        folderTarget.create(true, true, null);
        assertTrue(folderSource.exists());
        assertTrue(folderTarget.exists());
        file1 = folderSource.getFile("file1");
        file2 = folderSource.getFile("file2");
        file1.create(new ByteArrayInputStream("File1".getBytes()), true, null);
        file2.create(new ByteArrayInputStream("File1".getBytes()), true, null);
        assertTrue(folderSource.exists());
        assertTrue(folderTarget.exists());
    }

    @Test
    public void testMoveFiles() throws Exception {
        NonIPSMoveOperation operation = new NonIPSMoveOperation(folderTarget.getProject(),
                new Object[] { file1, file2 }, folderTarget.getLocation().toOSString());
        operation.run(null);

        assertTrue(folderTarget.getFile("file1").exists());
        assertTrue(folderTarget.getFile("file2").exists());

        IIpsPackageFragment targetIpsPackageFragment = ipsProject.getIpsPackageFragmentRoots()[0]
                .getIpsPackageFragment("source");
        IResource source01 = ((IContainer)targetIpsPackageFragment.getEnclosingResource()).findMember("file1");
        assertTrue(source01 == null);
        IFile source1 = folderTarget.getFile("file1");

        operation = new NonIPSMoveOperation(new Object[] { source1 }, targetIpsPackageFragment);
        operation.run(null);
        source01 = ((IContainer)targetIpsPackageFragment.getEnclosingResource()).findMember("file1");
        assertTrue(source01.exists());

        // test move to project
        operation = new NonIPSMoveOperation(ipsProject.getProject(), new Object[] { source01 }, ipsProject.getProject()
                .getLocation().toOSString());
        operation.run(null);
        assertTrue(((IContainer)targetIpsPackageFragment.getEnclosingResource()).findMember("file1") == null);
        assertTrue(ipsProject.getProject().findMember("file1").exists());
    }

    @Test
    public void testMoveLinks() throws Exception {
        NonIPSMoveOperation operation = new NonIPSMoveOperation(folderTarget.getProject(), new Object[] {
                file1.getLocation().toOSString(), file2.getLocation().toOSString() }, folderTarget.getLocation()
                        .toOSString());
        operation.run(null);

        assertTrue(folderTarget.getFile("file1").exists());
        assertTrue(folderTarget.getFile("file2").exists());
    }
}
