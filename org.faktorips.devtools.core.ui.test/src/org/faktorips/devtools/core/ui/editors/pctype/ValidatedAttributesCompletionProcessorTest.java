/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.junit.Before;
import org.junit.Test;

public class ValidatedAttributesCompletionProcessorTest extends AbstractIpsPluginTest {

    private PolicyCmptType pcType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        pcType = (PolicyCmptType)newIpsObject(project, IpsObjectType.POLICY_CMPT_TYPE, "policy");
    }

    @Test
    public void testDoComputeCompletionProposals() throws Exception {
        IPolicyCmptTypeAttribute attr = pcType.newPolicyCmptTypeAttribute();
        attr.setName("anna");
        attr = pcType.newPolicyCmptTypeAttribute();
        attr.setName("anne");
        attr = pcType.newPolicyCmptTypeAttribute();
        attr.setName("anton");
        attr = pcType.newPolicyCmptTypeAttribute();
        attr.setName("albert");
        attr = pcType.newPolicyCmptTypeAttribute();
        attr.setName("Berta");

        IValidationRule rule = pcType.newRule();
        ValidatedAttributesContentProposalProvider processor = new ValidatedAttributesContentProposalProvider(rule);
        IContentProposal[] proposals = processor.getProposals("", 0);
        assertEquals(5, proposals.length);

        proposals = processor.getProposals("a", 1);
        assertEquals(4, proposals.length);

        proposals = processor.getProposals("an", 2);
        assertEquals(3, proposals.length);

        proposals = processor.getProposals("al", 2);
        assertEquals(1, proposals.length);

        proposals = processor.getProposals("An", 2);
        assertEquals(3, proposals.length);

        proposals = processor.getProposals("B", 1);
        assertEquals(1, proposals.length);

        proposals = processor.getProposals("b", 1);
        assertEquals(1, proposals.length);
    }

}
