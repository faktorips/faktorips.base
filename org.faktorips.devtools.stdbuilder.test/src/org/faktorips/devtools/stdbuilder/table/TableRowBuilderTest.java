/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.table;

import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Before;
import org.junit.Test;

public class TableRowBuilderTest extends AbstractStdBuilderTest {

    private final static String TABLE_STRUCTURE_NAME = "TestTable";

    private ITableStructure structure;

    private TableRowBuilder builder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        structure = newTableStructure(ipsProject, TABLE_STRUCTURE_NAME);
        builder = new TableRowBuilder(builderSet);
    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(structure);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaClass()));
    }

    private IType getGeneratedJavaClass() {
        return getGeneratedJavaClass(structure, false, TABLE_STRUCTURE_NAME + "Row");
    }

}
