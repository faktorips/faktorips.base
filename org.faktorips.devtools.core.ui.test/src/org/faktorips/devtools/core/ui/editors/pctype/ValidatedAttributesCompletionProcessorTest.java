/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;

public class ValidatedAttributesCompletionProcessorTest extends AbstractIpsPluginTest {

    private PolicyCmptType pcType;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        pcType = (PolicyCmptType)newIpsObject(project, IpsObjectType.POLICY_CMPT_TYPE, "policy");
    }

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
