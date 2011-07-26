/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;

public class XmlContentFileCopyBuilderTest extends AbstractStdBuilderTest {

    private ITableStructure structure;
    private ITableContents contents;
    private IFolder destination;
    private String filePath;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        structure = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE, "TestTable");
        contents = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "TestTable");
        contents.setTableStructure(structure.getQualifiedName());
        String packageString = getPackageStructure().getPackage(builderSet.getArtefactBuilders()[0],
                contents.getIpsSrcFile());
        String packagePath = packageString.replace('.', '/');
        filePath = packagePath + "/TestTable.xml";
        destination = contents.getIpsPackageFragment().getRoot().getArtefactDestination(true);
    }

    private IFile getContentsFile() {
        return destination.getFile(new Path(filePath));
    }

    @Test
    public void testBuild() throws CoreException {
        assertFalse(getContentsFile().exists());
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertTrue(getContentsFile().exists());
    }

    private IJavaPackageStructure getPackageStructure() {
        return (IJavaPackageStructure)ipsProject.getIpsArtefactBuilderSet();
    }

    @Test
    public void testDelete() throws CoreException {
        assertFalse(getContentsFile().exists());
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertTrue(getContentsFile().exists());
        contents.getIpsSrcFile().getCorrespondingFile().delete(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertFalse(getContentsFile().exists());
    }

}
