/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;

public class TableRowBuilderTest extends AbstractStdBuilderTest {

    private final static String TABLE_STRUCTURE_NAME = "TestTable";

    private ITableStructure structure;

    private TableRowBuilder builder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        structure = newTableStructure(ipsProject, TABLE_STRUCTURE_NAME);
        builder = new TableRowBuilder(builderSet, DefaultBuilderSet.KIND_TABLE_ROW);
    }

    private IType getGeneratedJavaClass() {
        return getGeneratedJavaType(structure, false, true, TABLE_STRUCTURE_NAME + "Row");
    }

    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(structure);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaClass()));
    }

}
