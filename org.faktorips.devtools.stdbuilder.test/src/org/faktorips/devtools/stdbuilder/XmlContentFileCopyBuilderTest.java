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

import java.nio.file.Path;

import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.model.builder.IJavaPackageStructure;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;

public class XmlContentFileCopyBuilderTest extends AbstractStdBuilderTest {

    private ITableStructure structure;
    private ITableContents contents;
    private APackageFragmentRoot destination;
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

    private AFile getContentsFile() {
        return ((AFolder)destination.getResource()).getFile(Path.of(filePath));
    }

    @Test
    public void testBuild() {
        assertFalse(getContentsFile().exists());
        ipsProject.getProject().build(ABuildKind.INCREMENTAL_BUILD, null);
        assertTrue(getContentsFile().exists());
    }

    private IJavaPackageStructure getPackageStructure() {
        return (IJavaPackageStructure)ipsProject.getIpsArtefactBuilderSet();
    }

    @Test
    public void testDelete() {
        assertFalse(getContentsFile().exists());
        ipsProject.getProject().build(ABuildKind.INCREMENTAL_BUILD, null);
        assertTrue(getContentsFile().exists());
        contents.getIpsSrcFile().getCorrespondingFile().delete(null);
        ipsProject.getProject().build(ABuildKind.INCREMENTAL_BUILD, null);
        assertFalse(getContentsFile().exists());
    }

}
