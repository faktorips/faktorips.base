/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.faktorips.devtools.model.builder.IJavaPackageStructure;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;

public class XmlContentFileCopyBuilderTest extends AbstractStdBuilderTest {

    private ITableStructure structure;
    private ITableContents contents;
    private IPackageFragmentRoot destination;
    private String filePath;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        structure = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE, "TestTable");
        contents = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "TestTable");
        contents.setTableStructure(structure.getQualifiedName());
        String packageString = getPackageStructure().getPackageName(contents.getIpsSrcFile(), true, true);
        String packagePath = packageString.replace('.', '/');
        filePath = packagePath + "/TestTable.xml";
        destination = contents.getIpsPackageFragment().getRoot().getArtefactDestination(true);
    }

    private IFile getContentsFile() {
        return ((IFolder)destination.getResource()).getFile(new Path(filePath));
    }

    @Test
    public void testBuild() throws CoreRuntimeException {
        assertFalse(getContentsFile().exists());
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertTrue(getContentsFile().exists());
    }

    private IJavaPackageStructure getPackageStructure() {
        return (IJavaPackageStructure)ipsProject.getIpsArtefactBuilderSet();
    }

    @Test
    public void testDelete() throws CoreRuntimeException {
        assertFalse(getContentsFile().exists());
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertTrue(getContentsFile().exists());
        contents.getIpsSrcFile().getCorrespondingFile().delete(null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertFalse(getContentsFile().exists());
    }

}
