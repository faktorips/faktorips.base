/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.internal.fl.TableUsageFunctionsResolver;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.fl.FlFunction;
import org.junit.Before;
import org.junit.Test;

public class TableUsageFunctionsResolverTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ITableStructureUsage structUsage;
    private ITableContentUsage contentUsage;
    private ITableContents content;
    private ITableStructure structure;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt cmpt;

    final private String STRUCTURE_ROLENAME = "StructUsageRole";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        policyCmptType = newPolicyAndProductCmptType(project, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(project);
        cmpt = newProductCmpt(productCmptType, "Cmpt");
        structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "SearchStructure");

        content = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "Contents");
        structUsage = productCmptType.newTableStructureUsage();
        structUsage.addTableStructure(structure.getQualifiedName());
        structUsage.setRoleName(STRUCTURE_ROLENAME);

        contentUsage = cmpt.getProductCmptGeneration(0).newTableContentUsage();
    }

    @Test
    public void testGetFunctionsNoContent() {
        ITableContentUsage[] usages = { contentUsage };
        TableUsageFunctionsResolver resolver = new TableUsageFunctionsResolver(project, usages);

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

        contentUsage.setTableContentName(content.getName());

        ITableContentUsage[] usages = { contentUsage };
        TableUsageFunctionsResolver resolver = new TableUsageFunctionsResolver(project, usages);

        FlFunction<JavaCodeFragment>[] functions = resolver.getFunctions();

        assertEquals(0, functions.length);
    }

    @Test
    public void testFunctions() {

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

        content.setTableStructure(structure.getQualifiedName());
        contentUsage.setTableContentName(content.getName());

        ITableContentUsage[] usages = { contentUsage };
        TableUsageFunctionsResolver resolver = new TableUsageFunctionsResolver(project, usages);

        FlFunction<JavaCodeFragment>[] functions = resolver.getFunctions();

        assertEquals(2, functions.length);

        FlFunction<JavaCodeFragment> flFunction = functions[0];

        assertEquals("." + nameInteger, flFunction.getName());
        assertEquals(typeInteger, flFunction.getType().getName());

        FlFunction<JavaCodeFragment> flFunctionDecimal = functions[1];

        assertEquals("." + nameDecimal, flFunctionDecimal.getName());
        assertEquals(typeDecimal, flFunctionDecimal.getType().getName());
    }
}
