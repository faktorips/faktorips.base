/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;

public class ValidatedAttributesCompletionProcessorTest extends AbstractIpsPluginTest {

	private PolicyCmptType pcType;
	
	public void setUp() throws Exception{
		super.setUp();
		IIpsProject project = newIpsProject("TestProject");
		pcType = (PolicyCmptType)newIpsObject(project, IpsObjectType.POLICY_CMPT_TYPE, "policy");
		
	}
	
	/*
	 * Test method for 'org.faktorips.devtools.core.ui.editors.pctype.ValidatedAttributesCompletionProcessor.doComputeCompletionProposals(String, int, List)'
	 */
	public void testDoComputeCompletionProposals() throws Exception {
		IAttribute attr = pcType.newAttribute();
		attr.setName("anna");
		attr = pcType.newAttribute();
		attr.setName("anne");
		attr = pcType.newAttribute();
		attr.setName("anton");
		attr = pcType.newAttribute();
		attr.setName("albert");
		attr = pcType.newAttribute();
		attr.setName("Berta");
		
		IValidationRule rule = pcType.newRule();
		ValidatedAttributesCompletionProcessor processor = new ValidatedAttributesCompletionProcessor(rule);
		List proposals = new ArrayList();
		processor.doComputeCompletionProposals("", 0, proposals);
		assertEquals(5, proposals.size());
		
		proposals = new ArrayList();
		processor.doComputeCompletionProposals("a", 0, proposals);
		assertEquals(4, proposals.size());

		proposals = new ArrayList();
		processor.doComputeCompletionProposals("an", 0, proposals);
		assertEquals(3, proposals.size());

		proposals = new ArrayList();
		processor.doComputeCompletionProposals("al", 0, proposals);
		assertEquals(1, proposals.size());

		proposals = new ArrayList();
		processor.doComputeCompletionProposals("An", 0, proposals);
		assertEquals(3, proposals.size());

		proposals = new ArrayList();
		processor.doComputeCompletionProposals("B", 0, proposals);
		assertEquals(1, proposals.size());

		proposals = new ArrayList();
		processor.doComputeCompletionProposals("b", 0, proposals);
		assertEquals(1, proposals.size());

	}

}
