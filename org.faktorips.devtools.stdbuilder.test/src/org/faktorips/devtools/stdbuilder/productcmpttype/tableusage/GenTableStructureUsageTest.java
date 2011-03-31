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

package org.faktorips.devtools.stdbuilder.productcmpttype.tableusage;

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.stringParam;
import static org.junit.Assert.assertTrue;

import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptTypeBuilderTest;
import org.junit.Before;
import org.junit.Test;

public class GenTableStructureUsageTest extends ProductCmptTypeBuilderTest {

    private GenTableStructureUsage genTableStructureUsage;

    private ITableStructureUsage tableStructureUsage;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        tableStructureUsage = productCmptType.newTableStructureUsage();
        tableStructureUsage.setRoleName("foo");
        genTableStructureUsage = new GenTableStructureUsage(genProductCmptType, tableStructureUsage);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterface() {
        genTableStructureUsage.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, tableStructureUsage);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementation() {
        genTableStructureUsage.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                tableStructureUsage);
        expectField(0, javaClassGeneration, genTableStructureUsage.getMemberVarName());
        expectMethod(javaClassGeneration, genTableStructureUsage.getMethodNameGetTableUsage());
        expectMethod(javaClassGeneration, genTableStructureUsage.getMethodNameSetUsedTableName(), stringParam());
    }

}
