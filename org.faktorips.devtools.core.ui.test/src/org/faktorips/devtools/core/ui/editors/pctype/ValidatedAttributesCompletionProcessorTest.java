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

package org.faktorips.devtools.core.ui.editors.pctype;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
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
        ValidatedAttributesCompletionProcessor processor = new ValidatedAttributesCompletionProcessor(rule);
        List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
        processor.doComputeCompletionProposals("", 0, proposals);
        assertEquals(5, proposals.size());

        proposals.clear();
        processor.doComputeCompletionProposals("a", 0, proposals);
        assertEquals(4, proposals.size());

        proposals.clear();
        processor.doComputeCompletionProposals("an", 0, proposals);
        assertEquals(3, proposals.size());

        proposals.clear();
        processor.doComputeCompletionProposals("al", 0, proposals);
        assertEquals(1, proposals.size());

        proposals.clear();
        processor.doComputeCompletionProposals("An", 0, proposals);
        assertEquals(3, proposals.size());

        proposals.clear();
        processor.doComputeCompletionProposals("B", 0, proposals);
        assertEquals(1, proposals.size());

        proposals.clear();
        processor.doComputeCompletionProposals("b", 0, proposals);
        assertEquals(1, proposals.size());
    }

}
