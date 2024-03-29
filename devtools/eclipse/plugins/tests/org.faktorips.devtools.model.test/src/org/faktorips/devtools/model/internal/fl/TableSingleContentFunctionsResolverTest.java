/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.fl;

import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.tablestructure.TableStructureType;
import org.faktorips.fl.FlFunction;
import org.junit.Before;
import org.junit.Test;

public class TableSingleContentFunctionsResolverTest extends AbstractIpsPluginTest {

    private static final String TABLE_STRUCTURE_NAME = "struc.Structure";
    private static final String TABLE_CONTENTS_QNAME = "table.Contents";
    private IIpsProject project;

    private ITableStructure structure;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");

        structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, TABLE_STRUCTURE_NAME);
        structure.setTableStructureType(TableStructureType.SINGLE_CONTENT);

    }

    @Test
    public void testGetFunctionsNoContent() {
        TableSingleContentFunctionsResolver resolver = new TableSingleContentFunctionsResolver(project);

        FlFunction<JavaCodeFragment>[] functions = resolver.getFunctions();

        assertEquals(0, functions.length);
    }

    private IColumn createColumn(String type, String name) {
        IColumn column = structure.newColumn();
        column.setDatatype(type);
        column.setName(name);

        return column;
    }

    @Test
    public void testNoStructure() {

        TableSingleContentFunctionsResolver resolver = new TableSingleContentFunctionsResolver(project);

        FlFunction<JavaCodeFragment>[] functions = resolver.getFunctions();

        assertEquals(0, functions.length);
    }

    @Test
    public void testFunctionsTableInRootPackage() {

        String keyColumn = "keyColumn";
        createColumn("String", keyColumn);

        IIndex uniqueKey = structure.newIndex();
        uniqueKey.setKeyItems(new String[] { keyColumn });

        String typeInteger = "Integer";
        String nameInteger = "value";

        createColumn(typeInteger, nameInteger);

        String typeDecimal = "Decimal";
        String nameDecimal = "factor";

        createColumn(typeDecimal, nameDecimal);

        ITableContents content = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS,
                TABLE_CONTENTS_QNAME);

        content.setTableStructure(structure.getQualifiedName());

        TableSingleContentFunctionsResolver resolver = new TableSingleContentFunctionsResolver(project);

        FlFunction<JavaCodeFragment>[] functions = resolver.getFunctions();

        assertEquals(2, functions.length);

        FlFunction<JavaCodeFragment> flFunction = functions[0];

        assertEquals(TABLE_STRUCTURE_NAME + "." + nameInteger, flFunction.getName());
        assertEquals(typeInteger, flFunction.getType().getName());

        FlFunction<JavaCodeFragment> flFunctionDecimal = functions[1];

        assertEquals(TABLE_STRUCTURE_NAME + "." + nameDecimal, flFunctionDecimal.getName());
        assertEquals(typeDecimal, flFunctionDecimal.getType().getName());
    }
}
