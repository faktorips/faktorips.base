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

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
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
        structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "Structure");

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

        contentUsage.setTableContentName(content.getName());

        ITableContentUsage[] usages = { contentUsage };
        TableUsageFunctionsResolver resolver = new TableUsageFunctionsResolver(project, usages);

        FlFunction[] functions = resolver.getFunctions();

        assertEquals(0, functions.length);
    }

    @Test
    public void testFunctions() {

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

        content.setTableStructure(structure.getQualifiedName());
        contentUsage.setTableContentName(content.getName());

        ITableContentUsage[] usages = { contentUsage };
        TableUsageFunctionsResolver resolver = new TableUsageFunctionsResolver(project, usages);

        FlFunction[] functions = resolver.getFunctions();

        assertEquals(2, functions.length);

        FlFunction flFunction = functions[0];

        assertEquals("." + nameInteger, flFunction.getName());
        assertEquals(typeInteger, flFunction.getType().getName());

        FlFunction flFunctionDecimal = functions[1];

        assertEquals("." + nameDecimal, flFunctionDecimal.getName());
        assertEquals(typeDecimal, flFunctionDecimal.getType().getName());
    }
}
