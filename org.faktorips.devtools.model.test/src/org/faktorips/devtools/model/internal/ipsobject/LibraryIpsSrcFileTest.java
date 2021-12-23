/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import static org.faktorips.devtools.abstraction.mapping.PathMapping.toEclipsePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.IPath;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class LibraryIpsSrcFileTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragment pack;
    private IIpsSrcFile srcFile;
    private IPolicyCmptType originalType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject archiveProject = newIpsProject("ArchiveProject");
        originalType = newPolicyCmptType(archiveProject, "motor.Policy");
        originalType.newPolicyCmptTypeAttribute();
        originalType.getIpsSrcFile().save(true, null);

        project = newIpsProject();
        AFile archiveFile = project.getProject().getFile("test.ipsar");
        IPath archivePath = toEclipsePath(archiveFile.getWorkspaceRelativePath());
        createArchive(archiveProject, archiveFile);

        IIpsObjectPath path = project.getIpsObjectPath();
        path.newArchiveEntry(archivePath);
        project.setIpsObjectPath(path);

        root = project.getIpsPackageFragmentRoots()[1];
        pack = root.getIpsPackageFragment("motor");
        srcFile = pack.getIpsSrcFile(IpsObjectType.POLICY_CMPT_TYPE.getFileName("Policy"));
    }

    @Test
    public void testExists() {
        assertTrue(srcFile.exists());

        srcFile = pack.getIpsSrcFile(IpsObjectType.POLICY_CMPT_TYPE.getFileName("Unknown"));
        assertFalse(srcFile.exists());
    }

    @Test
    public void testGetIpsObject() {
        assertNotNull(srcFile.getIpsObject());
        IPolicyCmptType type = (IPolicyCmptType)srcFile.getIpsObject();
        assertEquals(originalType.getProductCmptType(), type.getProductCmptType());
        assertEquals(1, type.getNumOfAttributes());
    }

    @Test
    public void testGetParent() {
        assertEquals(pack, srcFile.getParent());
    }

    @Test
    public void testGetIpsPackageFragment() {
        assertEquals(pack, srcFile.getIpsPackageFragment());
    }

    @Test
    public void testGetCorrespondingFile() {
        assertNull(srcFile.getCorrespondingFile());
    }

    @Test
    public void testGetCorrespondingResource() {
        assertNull(srcFile.getCorrespondingResource());
    }

    @Test
    public void testGetContentFromEnclosingResource() throws CoreRuntimeException {
        assertNotNull(srcFile.getContentFromEnclosingResource());
    }

    @Test
    public void testGetEnclosingResource() {
        assertEquals(root.getCorrespondingResource(), srcFile.getEnclosingResource());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDelete() throws CoreRuntimeException {
        srcFile.delete();
    }

}
