/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
        assertTrue(model.getAvailableConditionTypes().isEmpty());

        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType
                .newPolicyCmptTypeAttribute("VertragsAttribut");
        policyCmptTypeAttribute.setProductRelevant(true);

        assertEquals(1, model.getAvailableConditionTypes().size());

        productCmptType.newProductCmptTypeAttribute("ProduktAttribut");

        assertEquals(2, model.getAvailableConditionTypes().size());

        productCmptType.newAssociation();

        assertEquals(3, model.getAvailableConditionTypes().size());
    }
}
