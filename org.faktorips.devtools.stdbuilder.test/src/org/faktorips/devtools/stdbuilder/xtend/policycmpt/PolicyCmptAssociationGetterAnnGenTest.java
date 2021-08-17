/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.policycmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAssociation;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAssociation;
import org.junit.Test;

public class PolicyCmptAssociationGetterAnnGenTest {

    private static final String INVERSE_ASSOCIATION = "inverseAssociation";

    private String annInverseAssociation = "@IpsInverseAssociation(\"" + INVERSE_ASSOCIATION + "\")"
            + System.lineSeparator();

    private PolicyCmptAssociationGetterAnnGen annGen = new PolicyCmptAssociationGetterAnnGen();

    @Test
    public void testIsGenerateAnnotationFor() {
        XPolicyAssociation policyAssociation = mock(XPolicyAssociation.class);
        assertTrue(annGen.isGenerateAnnotationFor(policyAssociation));

        XProductAssociation productAssociation = mock(XProductAssociation.class);
        assertFalse(annGen.isGenerateAnnotationFor(productAssociation));
    }

    @Test
    public void testCreateAnnInverseAssociation() {
        XPolicyAssociation association = mock(XPolicyAssociation.class);

        XPolicyAssociation inverseAssociation = mock(XPolicyAssociation.class);
        when(inverseAssociation.getName(false)).thenReturn(INVERSE_ASSOCIATION);
        when(association.getInverseAssociation()).thenReturn(inverseAssociation);

        assertEquals(annInverseAssociation, annGen.createAnnInverseAssociation(association).getSourcecode());
    }
}
