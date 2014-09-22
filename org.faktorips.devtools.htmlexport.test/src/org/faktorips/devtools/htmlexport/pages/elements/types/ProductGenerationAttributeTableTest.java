/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.GregorianCalendar;

import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.htmlexport.pages.standard.AbstractXmlUnitHtmlExportTest;
import org.junit.Test;

public class ProductGenerationAttributeTableTest extends AbstractXmlUnitHtmlExportTest {

    @Test
    public void testIgnoreDerivedUnions() throws Exception {
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "productType");
        String targetRole = "targetRole";
        String subTargetRole = "subTargetRole";

        IAssociation derivedUnion = productCmptType.newAssociation();

        derivedUnion.setTargetRoleSingular(targetRole);
        derivedUnion.setTarget("target");
        derivedUnion.setDerivedUnion(true);

        IAssociation subsetOfDerivedUnion = productCmptType.newAssociation();

        subsetOfDerivedUnion.setSubsettedDerivedUnion(subTargetRole);
        subsetOfDerivedUnion.setTargetRoleSingular(subTargetRole);

        ProductCmpt productCmpt = newProductCmpt(productCmptType, "productCmpt");

        productCmpt.newGeneration(new GregorianCalendar());

        ProductGenerationAttributeTable productGenerationAttributeTable = new ProductGenerationAttributeTable(
                productCmpt, context);

        assertXPathExists(productGenerationAttributeTable, "//tr[@id='" + subTargetRole + "']");
        assertXPathNotExists(productGenerationAttributeTable, "//tr[@id='" + targetRole + "']");

    }

    @Test
    public void testAddTablStuctureeUsages() throws Exception {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "productType");
        ITableStructureUsage tableStructureUsage1 = productCmptType.newTableStructureUsage();
        tableStructureUsage1.setRoleName("tableStructureUsage1");
        tableStructureUsage1.setChangingOverTime(true);

        ProductCmpt productCmpt = newProductCmpt(productCmptType, "productCmpt");
        productCmpt.newPropertyValue(tableStructureUsage1);

        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar());
        ITableStructureUsage tableStructureUsage2 = productCmptType.newTableStructureUsage();
        tableStructureUsage2.setRoleName("tableStructureUsage2");
        tableStructureUsage2.setChangingOverTime(true);
        generation.newTableContentUsage(tableStructureUsage2);

        ProductGenerationAttributeTable productGenerationAttributeTable = new ProductGenerationAttributeTable(
                productCmpt, context);

        assertXPathExists(productGenerationAttributeTable, "//td[. = '" + tableStructureUsage1.getRoleName() + "']");
        assertXPathExists(productGenerationAttributeTable, "//td[. = '" + tableStructureUsage2.getRoleName() + "']");

    }
}
