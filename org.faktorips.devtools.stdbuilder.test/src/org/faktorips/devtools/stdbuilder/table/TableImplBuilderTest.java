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
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;

public class TableImplBuilderTest extends AbstractStdBuilderTest {

    private final static String TABLE_STRUCTURE_NAME = "TestTable";

    private ITableStructure structure;

    private TableImplBuilder builder;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        structure = newTableStructure(ipsProject, TABLE_STRUCTURE_NAME);
        builder = new TableImplBuilder(builderSet, DefaultBuilderSet.KIND_TABLE_IMPL);
    }

    public void testDelete() throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        IFile file = getTableImpleBuilder().getJavaFile(structure.getIpsSrcFile());
        assertTrue(file.exists());
        structure.getIpsSrcFile().getCorrespondingFile().delete(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        file = getTableImpleBuilder().getJavaFile(structure.getIpsSrcFile());
        assertFalse(file.exists());
    }

    private TableImplBuilder getTableImpleBuilder() throws CoreException {
        IIpsArtefactBuilder[] builders = ipsProject.getIpsArtefactBuilderSet().getArtefactBuilders();
        for (IIpsArtefactBuilder builder2 : builders) {
            if (builder2.getClass() == TableImplBuilder.class) {
                return (TableImplBuilder)builder2;
            }
        }
        throw new RuntimeException("The " + TableImplBuilder.class + " is not in the builder set.");
    }

    private IType getGeneratedJavaClass() {
        return getGeneratedJavaType(structure, false, true, TABLE_STRUCTURE_NAME);
    }

    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(structure);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaClass()));
    }

}
