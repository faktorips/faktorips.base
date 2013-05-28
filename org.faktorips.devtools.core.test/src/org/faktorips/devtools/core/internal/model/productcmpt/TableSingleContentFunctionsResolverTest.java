/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
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

        FlFunction[] functions = resolver.getFunctions();

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

        FlFunction[] functions = resolver.getFunctions();

        assertEquals(0, functions.length);
    }

    @Test
    public void testFunctionsTableInRootPackage() throws CoreException {

        String keyColumn = "keyColumn";
        createColumn("String", keyColumn);

        IUniqueKey uniqueKey = structure.newUniqueKey();
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

        FlFunction[] functions = resolver.getFunctions();

        assertEquals(2, functions.length);

        FlFunction flFunction = functions[0];

        assertEquals(TABLE_STRUCTURE_NAME + "." + nameInteger, flFunction.getName());
        assertEquals(typeInteger, flFunction.getType().getName());

        FlFunction flFunctionDecimal = functions[1];

        assertEquals(TABLE_STRUCTURE_NAME + "." + nameDecimal, flFunctionDecimal.getName());
        assertEquals(typeDecimal, flFunctionDecimal.getType().getName());
    }
}
