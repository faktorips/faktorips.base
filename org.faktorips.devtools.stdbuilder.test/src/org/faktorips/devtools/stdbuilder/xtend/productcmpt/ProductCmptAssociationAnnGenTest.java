/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xtend.productcmpt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAssociation;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAssociation;
import org.junit.Test;

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
