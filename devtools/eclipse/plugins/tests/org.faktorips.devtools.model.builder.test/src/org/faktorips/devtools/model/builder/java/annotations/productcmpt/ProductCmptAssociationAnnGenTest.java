/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.productcmpt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAssociation;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductAssociation;
import org.junit.jupiter.api.Test;

public class ProductCmptAssociationAnnGenTest {

    private ProductCmptAssociationAnnGen annGen = new ProductCmptAssociationAnnGen();

    @Test
    public void testIsGenerateAnnotationFor() {
        XPolicyAssociation policyAssociation = mock(XPolicyAssociation.class);
        assertFalse(annGen.isGenerateAnnotationFor(policyAssociation));

        XProductAssociation productAssociation = mock(XProductAssociation.class);
        assertTrue(annGen.isGenerateAnnotationFor(productAssociation));
    }
}
