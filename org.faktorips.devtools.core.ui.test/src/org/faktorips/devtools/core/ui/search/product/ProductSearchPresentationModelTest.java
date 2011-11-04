/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.product;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

public class ProductSearchPresentationModelTest extends AbstractIpsPluginTest {

    private ProductSearchPresentationModel model;
    private IIpsProject ipsProject;
    private IProductCmptType productCmptType;
    private IPolicyCmptType policyCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        model = new ProductSearchPresentationModel();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragsArt");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);

        model.setProductCmptType(productCmptType);
    }

    @Test
    public void testConditionsWithSearchableElements() {
        assertTrue(model.getConditionsWithSearchableElements().isEmpty());

        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType
                .newPolicyCmptTypeAttribute("VertragsAttribut");
        policyCmptTypeAttribute.setProductRelevant(true);

        assertEquals(1, model.getConditionsWithSearchableElements().size());

        productCmptType.newProductCmptTypeAttribute("ProduktAttribut");

        assertEquals(2, model.getConditionsWithSearchableElements().size());

        productCmptType.newAssociation();

        assertEquals(3, model.getConditionsWithSearchableElements().size());
    }
}
