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

package org.faktorips.devtools.stdbuilder.table;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

public class TableImplBuilderTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ITableStructure structure;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        IIpsProjectProperties props = project.getProperties();
        project.setProperties(props);
        structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable");
    }

    public void testDelete() throws CoreException {
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        IFile file = getTableImpleBuilder().getJavaFile(structure.getIpsSrcFile());
        assertTrue(file.exists());
        structure.getIpsSrcFile().getCorrespondingFile().delete(true, null);
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        file = getTableImpleBuilder().getJavaFile(structure.getIpsSrcFile());
        assertFalse(file.exists());
    }

    private TableImplBuilder getTableImpleBuilder() throws CoreException {
        IIpsArtefactBuilder[] builders = project.getIpsArtefactBuilderSet().getArtefactBuilders();
        for (int i = 0; i < builders.length; i++) {
            if (builders[i].getClass() == TableImplBuilder.class) {
                return (TableImplBuilder)builders[i];
            }
        }
        throw new RuntimeException("The " + TableImplBuilder.class + " is not in the builder set.");
    }

}
