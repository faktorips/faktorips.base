/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.Arrays;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.junit.Before;
import org.junit.Test;

public class FormulaTest extends AbstractIpsPluginTest {

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IIpsProject ipsProject;
    private Formula formula;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("TestProject");
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);
    }

    @Test
    public void testGetTableContentUsages_ProductCmptGeneration() throws Exception {
        formula = new Formula(generation, "formula");
        assertEquals(0, formula.getTableContentUsages().length);

        ITableStructureUsage structureUsageGen = productCmptType.newTableStructureUsage();
        structureUsageGen.setRoleName("RateTableGen");
        ITableContentUsage contentUsageGen = generation.newTableContentUsage(structureUsageGen);

        ITableStructureUsage structureUsage = productCmptType.newTableStructureUsage();
        structureUsage.setChangingOverTime(false);
        structureUsage.setRoleName("RateTable");
        ITableContentUsage contentUsage = (ITableContentUsage)productCmpt.newPropertyValue(structureUsageGen);

        ITableContentUsage[] tableContentUsages = formula.getTableContentUsages();
        assertEquals(2, tableContentUsages.length);
        List<ITableContentUsage> asList = Arrays.asList(tableContentUsages);
        assertTrue(asList.contains(contentUsageGen));
        assertTrue(asList.contains(contentUsage));
    }

    @Test
    public void testGetTableContentUsages_ProductCmpt() throws Exception {
        formula = new Formula(productCmpt, "formula");
        assertEquals(0, formula.getTableContentUsages().length);

        ITableStructureUsage structureUsageGen = productCmptType.newTableStructureUsage();
        structureUsageGen.setRoleName("RateTable");
        ITableContentUsage contentUsageGen = generation.newTableContentUsage(structureUsageGen);

        ITableStructureUsage structureUsage = productCmptType.newTableStructureUsage();
        structureUsage.setChangingOverTime(false);
        structureUsage.setRoleName("RateTable");
        ITableContentUsage contentUsage = (ITableContentUsage)productCmpt.newPropertyValue(structureUsageGen);

        ITableContentUsage[] tableContentUsages = formula.getTableContentUsages();
        assertEquals(1, tableContentUsages.length);
        List<ITableContentUsage> asList = Arrays.asList(tableContentUsages);
        assertFalse(asList.contains(contentUsageGen));
        assertTrue(asList.contains(contentUsage));
    }

    @Test
    public void testFindMatchingProductCmptTypeAttributes_changingOverTime() throws Exception {
        IAttribute changingAttr = productCmptType.newProductCmptTypeAttribute("test1");
        IProductCmptTypeAttribute staticAttr = productCmptType.newProductCmptTypeAttribute("test2");
        staticAttr.setChangingOverTime(false);
        formula = new Formula(generation, "formula");

        List<IAttribute> matchingProductCmptTypeAttributes = formula.findMatchingProductCmptTypeAttributes();

        assertThat(matchingProductCmptTypeAttributes, hasItems(changingAttr, staticAttr));
        assertEquals(2, matchingProductCmptTypeAttributes.size());
    }

    @Test
    public void testFindMatchingProductCmptTypeAttributes_NOTchangingOverTime() throws Exception {
        productCmptType.newProductCmptTypeAttribute("test1");
        IProductCmptTypeAttribute staticAttr = productCmptType.newProductCmptTypeAttribute("test2");
        staticAttr.setChangingOverTime(false);
        formula = new Formula(productCmpt, "formula");

        List<IAttribute> matchingProductCmptTypeAttributes = formula.findMatchingProductCmptTypeAttributes();

        assertThat(matchingProductCmptTypeAttributes, hasItems((IAttribute)staticAttr));
        assertEquals(1, matchingProductCmptTypeAttributes.size());
    }

}
