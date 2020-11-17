/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.GregorianCalendar;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.htmlexport.pages.standard.AbstractXmlUnitHtmlExportTest;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.IAssociation;
import org.junit.Test;

public class ProductGenerationAttributeTableTest extends AbstractXmlUnitHtmlExportTest {

    @Test
    public void testIgnoreDerivedUnions() throws Exception {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "productType");
        String targetRole = "targetRole";
        String subTargetRole = "subTargetRole";

        IAssociation derivedUnion = productCmptType.newAssociation();

        derivedUnion.setTargetRoleSingular(targetRole);
        derivedUnion.setTarget("target");
        derivedUnion.setDerivedUnion(true);

        IAssociation subsetOfDerivedUnion = productCmptType.newAssociation();

        subsetOfDerivedUnion.setSubsettedDerivedUnion(subTargetRole);
        subsetOfDerivedUnion.setTargetRoleSingular(subTargetRole);

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "productCmpt");

        productCmpt.newGeneration(new GregorianCalendar());

        ProductGenerationAttributeTable productGenerationAttributeTable = new ProductGenerationAttributeTable(
                productCmpt, context);

        assertXPathExists(productGenerationAttributeTable, "//tr[@id='" + subTargetRole + "']");
        assertXPathNotExists(productGenerationAttributeTable, "//tr[@id='" + targetRole + "']");

    }

    @Test
    public void testAddTableStuctureUsages() throws Exception {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "productType");
        ITableStructureUsage tableStructureUsage1 = productCmptType.newTableStructureUsage();
        tableStructureUsage1.setRoleName("tableStructureUsage1");
        tableStructureUsage1.setChangingOverTime(false);

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "productCmpt");
        productCmpt.newPropertyValues(tableStructureUsage1);

        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar());
        ITableStructureUsage tableStructureUsage2 = productCmptType.newTableStructureUsage();
        tableStructureUsage2.setRoleName("tableStructureUsage2");
        tableStructureUsage2.setChangingOverTime(true);
        generation.newTableContentUsage(tableStructureUsage2);

        ProductGenerationAttributeTable productGenerationAttributeTable = new ProductGenerationAttributeTable(
                productCmpt, context);

        assertXPathExists(productGenerationAttributeTable, "//td/descendant::span");
        assertXPathExists(productGenerationAttributeTable, "//td[. = '" + tableStructureUsage2.getRoleName() + "']");
    }

    @Test
    public void testAddFormulas() throws Exception {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "productType");
        IProductCmptTypeMethod formulaProductCmpt = productCmptType.newFormulaSignature("ProduktFormel");
        formulaProductCmpt.setDatatype(Datatype.STRING.getName());
        formulaProductCmpt.setChangingOverTime(false);

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "productCmpt");
        productCmpt.newPropertyValues(formulaProductCmpt);

        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar());
        IProductCmptTypeMethod formulaProductCmptGen = productCmptType.newFormulaSignature("ProduktFormelGen");
        formulaProductCmptGen.setDatatype(Datatype.STRING.getName());
        generation.newFormula(formulaProductCmptGen);

        ProductGenerationAttributeTable productGenerationAttributeTable = new ProductGenerationAttributeTable(
                productCmpt, context);

        assertXPathExists(productGenerationAttributeTable, "//td/descendant::span");
        assertXPathExists(productGenerationAttributeTable,
                "//td[. = '" + formulaProductCmptGen.getFormulaName() + "']");
    }

    @Test
    public void testAddValidationRules() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "policyType", "productType");
        IValidationRule staticRule = policyCmptType.newRule();
        staticRule.setConfigurableByProductComponent(true);
        staticRule.setChangingOverTime(false);
        staticRule.setName("Stat");
        IValidationRule dynamicRule = policyCmptType.newRule();
        dynamicRule.setConfigurableByProductComponent(true);
        dynamicRule.setChangingOverTime(true);
        dynamicRule.setName("Dyn");
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "productCmpt");
        productCmpt.newPropertyValues(staticRule);

        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar());
        generation.newPropertyValues(dynamicRule);

        ProductGenerationAttributeTable productGenerationAttributeTable = new ProductGenerationAttributeTable(
                productCmpt, context);

        assertXPathExists(productGenerationAttributeTable, "//td/descendant::span");
        assertXPathExists(productGenerationAttributeTable, "//td[. = '" + dynamicRule.getName() + "']");
    }

    @Test
    public void testAddValidationRules_NoGeneration() throws Exception {
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "policyType", "productType");
        IValidationRule staticRule = policyCmptType.newRule();
        staticRule.setConfigurableByProductComponent(true);
        staticRule.setChangingOverTime(false);
        staticRule.setName("Stat");
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmptType.setChangingOverTime(false);

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "productCmpt");
        productCmpt.newPropertyValues(staticRule);

        ProductGenerationAttributeTable productGenerationAttributeTable = new ProductGenerationAttributeTable(
                productCmpt, context);

        assertXPathExists(productGenerationAttributeTable, "//td[. = '" + staticRule.getName() + "']");
    }
}
