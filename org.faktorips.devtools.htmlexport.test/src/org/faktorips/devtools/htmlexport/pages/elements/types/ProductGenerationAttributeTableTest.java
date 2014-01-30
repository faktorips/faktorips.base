/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.GregorianCalendar;

import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
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
}
