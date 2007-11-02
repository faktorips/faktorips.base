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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 */
public class ValidatedAttributesCompletionProcessor extends
		AbstractCompletionProcessor {

	private IPolicyCmptType pcType;

	private IValidationRule rule;

	public ValidatedAttributesCompletionProcessor(IValidationRule rule) {
		ArgumentCheck.notNull(rule);
		this.rule = rule;
		this.pcType = (IPolicyCmptType) rule.getIpsObject();
		setIpsProject(pcType.getIpsProject());
	}

	/**
     * {@inheritDoc}
	 */
	public void doComputeCompletionProposals(String prefix,
			int documentOffset, List result) throws Exception {
		prefix = prefix.toLowerCase();
		IPolicyCmptTypeAttribute[] attributes = pcType.getSupertypeHierarchy().getAllAttributes(pcType);
		List validatedAttributes = Arrays.asList(rule.getValidatedAttributes());
		for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].getAttributeType()!=AttributeType.CONSTANT) {
                if (!validatedAttributes.contains(attributes[i].getName())
                        && attributes[i].getName().toLowerCase().startsWith(prefix)) {
                    addToResult(result, attributes[i], documentOffset);
                }
            }
		}
	}

	private void addToResult(List result, IPolicyCmptTypeAttribute attribute,
			int documentOffset) {
		String name = attribute.getName();
		CompletionProposal proposal = new CompletionProposal(name, 0,
				documentOffset, name.length(), attribute.getImage(), name,
				null, attribute.getDescription());
		result.add(proposal);
	}
}
