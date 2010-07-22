/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

public class XmlContentFileCopyBuilderTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ITableStructure structure;
    private ITableContents contents;
    private IFolder destination;
    private String filePath;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        IIpsProjectProperties props = project.getProperties();
        project.setProperties(props);
        structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable");
        contents = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "TestTable");
        contents.setTableStructure(structure.getQualifiedName());
        String packageString = getPackageStructure().getPackage(DefaultBuilderSet.KIND_TABLE_CONTENT,
                contents.getIpsSrcFile());
        String packagePath = packageString.replace('.', '/');
        filePath = packagePath + "/TestTable.xml";
        destination = contents.getIpsPackageFragment().getRoot().getArtefactDestination(true);
    }

    private IFile getContentsFile() {
        return destination.getFile(new Path(filePath));
    }

    public void testBuild() throws CoreException {
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertTrue(getContentsFile().exists());
    }

    private IJavaPackageStructure getPackageStructure() throws CoreException {
        return project.getIpsArtefactBuilderSet();
    }

    public void testDelete() throws CoreException {
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        contents.getIpsSrcFile().getCorrespondingFile().delete(true, null);
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertFalse(getContentsFile().exists());
    }

}
