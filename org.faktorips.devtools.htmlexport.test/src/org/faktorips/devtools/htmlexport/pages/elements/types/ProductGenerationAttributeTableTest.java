/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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
