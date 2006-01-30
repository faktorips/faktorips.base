package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRuleDef;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 */
public class ValidatedAttributesCompletionProcessor extends
		AbstractCompletionProcessor {

	private IPolicyCmptType pcType;

	private IValidationRuleDef rule;

	public ValidatedAttributesCompletionProcessor(IValidationRuleDef rule) {
		ArgumentCheck.notNull(rule);
		this.rule = rule;
		this.pcType = (IPolicyCmptType) rule.getIpsObject();
		setIpsProject(pcType.getIpsProject());
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.devtools.core.ui.AbstractCompletionProcessor#doComputeCompletionProposals(java.lang.String,
	 *      java.util.List)
	 */
	protected void doComputeCompletionProposals(String prefix,
			int documentOffset, List result) throws Exception {
		prefix = prefix.toLowerCase();
		IAttribute[] attributes = pcType.getSupertypeHierarchy().getAllAttributes(pcType);
		List validatedAttributes = Arrays.asList(rule.getValidatedAttributes());
		for (int i = 0; i < attributes.length; i++) {
			if (!validatedAttributes.contains(attributes[i].getName())
					&& attributes[i].getName().toLowerCase().startsWith(prefix)) {
				addToResult(result, attributes[i], documentOffset);
			}
		}
	}

	private void addToResult(List result, IAttribute attribute,
			int documentOffset) {
		String name = attribute.getName();
		CompletionProposal proposal = new CompletionProposal(name, 0,
				documentOffset, name.length(), attribute.getImage(), name,
				null, attribute.getDescription());
		result.add(proposal);
	}
}
